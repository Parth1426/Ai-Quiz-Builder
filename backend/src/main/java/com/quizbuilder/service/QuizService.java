package com.quizbuilder.service;

import com.quizbuilder.ai.AiQuizOrchestrator;
import com.quizbuilder.ai.GeneratedQuizData;
import com.quizbuilder.dto.request.GenerateQuizRequest;
import com.quizbuilder.dto.response.OptionResponse;
import com.quizbuilder.dto.response.QuestionResponse;
import com.quizbuilder.dto.response.QuizResponse;
import com.quizbuilder.exception.ResourceNotFoundException;
import com.quizbuilder.model.*;
import com.quizbuilder.repository.QuizAttemptRepository;
import com.quizbuilder.repository.QuizRepository;
import com.quizbuilder.repository.QuizTopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizTopicRepository topicRepository;
    private final AiQuizOrchestrator orchestrator;

    @Transactional
    @CacheEvict(value = {"quizList", "stats"}, allEntries = true)
    public QuizResponse generateAndSave(GenerateQuizRequest request) {
        log.info("Generating quiz for topic '{}' with {} questions at {} difficulty",
                request.getTopic(), request.getQuestionCount(), request.getDifficulty());

        GeneratedQuizData generated = orchestrator.generateQuiz(
                request.getTopic(),
                request.getQuestionCount(),
                request.getDifficulty()
        );

        Quiz quiz = Quiz.builder()
                .topic(request.getTopic())
                .category(request.getCategory() != null ? request.getCategory() : generated.getCategory())
                .difficulty(request.getDifficulty())
                .status(QuizStatus.READY)
                .aiProvider(generated.getAiProvider())
                .build();

        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < generated.getQuestions().size(); i++) {
            GeneratedQuizData.GeneratedQuestion gq = generated.getQuestions().get(i);
            Question q = Question.builder()
                    .quiz(quiz)
                    .questionText(gq.getQuestionText())
                    .questionIndex(i)
                    .difficulty(gq.getDifficulty() != null ? gq.getDifficulty() : request.getDifficulty())
                    .explanation(gq.getExplanation())
                    .build();

            List<Option> options = new ArrayList<>();
            for (GeneratedQuizData.GeneratedOption go : gq.getOptions()) {
                options.add(Option.builder()
                        .question(q)
                        .optionLabel(go.getLabel())
                        .optionText(go.getText())
                        .isCorrect(go.isCorrect())
                        .build());
            }
            q.setOptions(options);
            questions.add(q);
        }
        quiz.setQuestions(questions);

        Quiz saved = quizRepository.save(quiz);
        log.info("Saved quiz {} for topic '{}' from provider {}",
                saved.getId(), saved.getTopic(), saved.getAiProvider());

        recordTopicUsage(request.getTopic(), request.getCategory(), generated);
        return toResponse(saved, false);
    }

    private void recordTopicUsage(String topic, String category, GeneratedQuizData generated) {
        topicRepository.findByTopicNameIgnoreCase(topic).ifPresentOrElse(
                existing -> topicRepository.incrementUsageCount(existing.getTopicName()),
                () -> topicRepository.save(QuizTopic.builder()
                        .topicName(topic)
                        .category(category != null ? category : generated.getCategory())
                        .usageCount(1L)
                        .build())
        );
    }

    @Cacheable(value = "quiz", key = "#quizId")
    @Transactional(readOnly = true)
    public QuizResponse getQuiz(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));
        return toResponse(quiz, true);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizForTaking(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));
        return toResponse(quiz, true);
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> listQuizzes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return quizRepository.findByStatusOrderByCreatedAtDesc(QuizStatus.READY, pageable)
                .map(q -> toResponse(q, false));
    }

    @Cacheable(value = "recentQuizzes")
    @Transactional(readOnly = true)
    public List<QuizResponse> getRecentQuizzes(int limit) {
        return quizRepository.findRecentQuizzes(PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(q -> toResponse(q, false))
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"quiz", "quizList", "stats", "recentQuizzes"}, allEntries = true)
    public void deleteQuiz(String quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found: " + quizId);
        }
        quizRepository.deleteById(quizId);
        log.info("Deleted quiz {}", quizId);
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableTopics() {
        return quizRepository.findDistinctTopics();
    }

    private QuizResponse toResponse(Quiz quiz, boolean withOptions) {
        List<QuestionResponse> questionResponses = quiz.getQuestions().stream()
                .map(q -> QuestionResponse.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .questionIndex(q.getQuestionIndex())
                        .difficulty(q.getDifficulty())
                        .options(withOptions ? mapOptions(q) : null)
                        .build())
                .collect(Collectors.toList());

        List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndCompletedTrue(quiz.getId());
        Double avgScore = attemptRepository.findAvgScoreByQuizId(quiz.getId());

        return QuizResponse.builder()
                .id(quiz.getId())
                .topic(quiz.getTopic())
                .category(quiz.getCategory())
                .difficulty(quiz.getDifficulty())
                .status(quiz.getStatus())
                .aiProvider(quiz.getAiProvider())
                .createdAt(quiz.getCreatedAt())
                .questions(questionResponses)
                .questionCount(quiz.getQuestions().size())
                .totalAttempts(attempts.size())
                .averageScore(avgScore != null ? Math.round(avgScore * 1000.0) / 10.0 : null)
                .build();
    }

    private List<OptionResponse> mapOptions(Question q) {
        return q.getOptions().stream()
                .map(o -> OptionResponse.builder()
                        .id(o.getId())
                        .optionLabel(o.getOptionLabel())
                        .optionText(o.getOptionText())
                        .build())
                .collect(Collectors.toList());
    }
}
