package com.quizbuilder.service;

import com.quizbuilder.dto.request.SubmitAttemptRequest;
import com.quizbuilder.dto.response.AttemptResultResponse;
import com.quizbuilder.dto.response.LeaderboardEntryResponse;
import com.quizbuilder.exception.ResourceNotFoundException;
import com.quizbuilder.model.*;
import com.quizbuilder.repository.QuestionRepository;
import com.quizbuilder.repository.QuizAttemptRepository;
import com.quizbuilder.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttemptService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;

    @Transactional
    @CacheEvict(value = {"leaderboard", "stats"}, allEntries = true)
    public AttemptResultResponse submit(SubmitAttemptRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + request.getQuizId()));

        if (quiz.getStatus() != QuizStatus.READY) {
            throw new IllegalArgumentException("Quiz is not ready to be attempted");
        }

        Map<String, Question> questionsById = quiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .userName(request.getUserName() != null ? request.getUserName() : "Anonymous")
                .sessionId(request.getSessionId())
                .totalQuestions(quiz.getQuestions().size())
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        int correctCount = 0;
        List<UserAnswer> userAnswers = new ArrayList<>();
        List<AttemptResultResponse.QuestionResultDetail> resultDetails = new ArrayList<>();

        for (SubmitAttemptRequest.AnswerEntry entry : request.getAnswers()) {
            Question question = questionsById.get(entry.getQuestionId());
            if (question == null) continue;

            Option correctOption = question.getOptions().stream()
                    .filter(Option::getIsCorrect)
                    .findFirst()
                    .orElse(null);

            Option selectedOption = findSelectedOption(question, entry);
            boolean isCorrect = selectedOption != null && Boolean.TRUE.equals(selectedOption.getIsCorrect());
            if (isCorrect) correctCount++;

            UserAnswer ua = UserAnswer.builder()
                    .attempt(attempt)
                    .questionId(question.getId())
                    .selectedOptionId(selectedOption != null ? selectedOption.getId() : null)
                    .selectedLabel(selectedOption != null ? selectedOption.getOptionLabel() : entry.getSelectedLabel())
                    .isCorrect(isCorrect)
                    .build();
            userAnswers.add(ua);

            resultDetails.add(buildQuestionDetail(question, selectedOption, correctOption));
        }

        attempt.setUserAnswers(userAnswers);
        attempt.setScore(correctCount);
        QuizAttempt savedAttempt = attemptRepository.save(attempt);
        log.info("Recorded attempt {} on quiz {} - score {}/{}",
                savedAttempt.getId(), quiz.getId(), correctCount, quiz.getQuestions().size());

        return buildResult(savedAttempt, quiz, resultDetails);
    }

    private Option findSelectedOption(Question question, SubmitAttemptRequest.AnswerEntry entry) {
        if (entry.getSelectedOptionId() != null) {
            return question.getOptions().stream()
                    .filter(o -> o.getId().equals(entry.getSelectedOptionId()))
                    .findFirst()
                    .orElse(null);
        }
        if (entry.getSelectedLabel() != null) {
            return question.getOptions().stream()
                    .filter(o -> o.getOptionLabel().equalsIgnoreCase(entry.getSelectedLabel()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private AttemptResultResponse.QuestionResultDetail buildQuestionDetail(
            Question question, Option selected, Option correct) {

        List<AttemptResultResponse.OptionResultDetail> optionDetails = question.getOptions().stream()
                .map(o -> AttemptResultResponse.OptionResultDetail.builder()
                        .id(o.getId())
                        .optionLabel(o.getOptionLabel())
                        .optionText(o.getOptionText())
                        .isCorrect(Boolean.TRUE.equals(o.getIsCorrect()))
                        .wasSelected(selected != null && selected.getId().equals(o.getId()))
                        .build())
                .collect(Collectors.toList());

        return AttemptResultResponse.QuestionResultDetail.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .selectedLabel(selected != null ? selected.getOptionLabel() : null)
                .selectedOptionText(selected != null ? selected.getOptionText() : null)
                .correctLabel(correct != null ? correct.getOptionLabel() : null)
                .correctOptionText(correct != null ? correct.getOptionText() : null)
                .isCorrect(selected != null && selected.getId().equals(correct != null ? correct.getId() : null))
                .explanation(question.getExplanation())
                .options(optionDetails)
                .build();
    }

    private AttemptResultResponse buildResult(QuizAttempt attempt, Quiz quiz,
                                              List<AttemptResultResponse.QuestionResultDetail> details) {
        double percentage = attempt.getTotalQuestions() == 0
                ? 0
                : Math.round((attempt.getScore() * 1000.0 / attempt.getTotalQuestions())) / 10.0;
        return AttemptResultResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTopic(quiz.getTopic())
                .userName(attempt.getUserName())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .percentage(percentage)
                .grade(grade(percentage))
                .timeTakenSeconds(attempt.getTimeTakenSeconds())
                .completedAt(attempt.getCompletedAt())
                .questionResults(details)
                .build();
    }

    @Transactional(readOnly = true)
    public AttemptResultResponse getAttempt(String attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));
        Quiz quiz = attempt.getQuiz();
        Map<String, Question> qById = quiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        Map<String, UserAnswer> userAnswerByQuestion = attempt.getUserAnswers().stream()
                .collect(Collectors.toMap(UserAnswer::getQuestionId, ua -> ua, (a, b) -> a));

        List<AttemptResultResponse.QuestionResultDetail> details = quiz.getQuestions().stream()
                .map(q -> {
                    Option correct = q.getOptions().stream()
                            .filter(Option::getIsCorrect)
                            .findFirst()
                            .orElse(null);
                    UserAnswer ua = userAnswerByQuestion.get(q.getId());
                    Option selected = ua != null && ua.getSelectedOptionId() != null
                            ? q.getOptions().stream()
                                .filter(o -> o.getId().equals(ua.getSelectedOptionId()))
                                .findFirst()
                                .orElse(null)
                            : null;
                    return buildQuestionDetail(q, selected, correct);
                })
                .collect(Collectors.toList());

        return buildResult(attempt, quiz, details);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(int limit) {
        List<QuizAttempt> attempts = attemptRepository.findLeaderboard(
                PageRequest.of(0, limit)).getContent();
        return IntStream.range(0, attempts.size())
                .mapToObj(i -> mapLeaderboardEntry(attempts.get(i), i + 1))
                .collect(Collectors.toList());
    }

    private LeaderboardEntryResponse mapLeaderboardEntry(QuizAttempt a, int rank) {
        double percentage = a.getTotalQuestions() == 0 ? 0 :
                Math.round((a.getScore() * 1000.0 / a.getTotalQuestions())) / 10.0;
        return LeaderboardEntryResponse.builder()
                .rank(rank)
                .attemptId(a.getId())
                .userName(a.getUserName())
                .quizTopic(a.getQuiz().getTopic())
                .score(a.getScore())
                .totalQuestions(a.getTotalQuestions())
                .percentage(percentage)
                .grade(grade(percentage))
                .timeTakenSeconds(a.getTimeTakenSeconds())
                .completedAt(a.getCompletedAt())
                .build();
    }

    private String grade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }
}
