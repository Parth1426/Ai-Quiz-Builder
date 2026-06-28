package com.quizbuilder.ai.local;

import com.quizbuilder.ai.GeneratedQuizData;
import com.quizbuilder.ai.QuizGeneratorStrategy;
import com.quizbuilder.model.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Locally-trained quiz generator.
 * <p>
 * Uses an in-memory curated knowledge base built from JSON datasets. This serves as
 * our "trained model" — a deterministic, reliable provider that works completely
 * offline, ideal in firewall-restricted corporate environments.
 * <p>
 * Generation strategy (in priority order):
 * <ol>
 *     <li>Exact topic match — selects questions from that topic and difficulty</li>
 *     <li>Fuzzy keyword match — finds related topics and pulls weighted questions</li>
 *     <li>Template-based synthesis — produces meaningful MCQ via patterns when no match</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalQuizGenerator implements QuizGeneratorStrategy {

    public static final String PROVIDER = "LocalKnowledgeModel-v1";

    private final KnowledgeBase knowledgeBase;
    private final TemplateQuestionGenerator templateGenerator;

    @Override
    public GeneratedQuizData generate(String topic, int questionCount, DifficultyLevel difficulty) {
        log.info("Local generator producing {} {} questions for topic '{}'", questionCount, difficulty, topic);

        List<KnowledgeEntry.QuestionTemplate> pool = collectQuestions(topic, difficulty, questionCount * 3);

        List<KnowledgeEntry.QuestionTemplate> selected = selectQuestions(pool, questionCount, difficulty);

        List<GeneratedQuizData.GeneratedQuestion> questions;
        if (selected.isEmpty()) {
            log.info("No curated questions found for '{}'. Using template synthesis.", topic);
            questions = templateGenerator.synthesize(topic, questionCount, difficulty);
        } else if (selected.size() < questionCount) {
            log.info("Found {} curated questions for '{}', synthesizing {} additional.",
                    selected.size(), topic, questionCount - selected.size());
            questions = new ArrayList<>(selected.stream().map(this::convertTemplate).toList());
            questions.addAll(templateGenerator.synthesize(topic, questionCount - selected.size(), difficulty));
        } else {
            questions = selected.stream().map(this::convertTemplate).collect(Collectors.toList());
        }

        return GeneratedQuizData.builder()
                .topic(topic)
                .category(determineCategory(topic))
                .aiProvider(PROVIDER)
                .questions(questions)
                .build();
    }

    private List<KnowledgeEntry.QuestionTemplate> collectQuestions(
            String topic, DifficultyLevel difficulty, int maxPoolSize) {

        Optional<KnowledgeEntry> exact = knowledgeBase.findExact(topic);
        List<KnowledgeEntry.QuestionTemplate> pool = new ArrayList<>();
        if (exact.isPresent()) {
            pool.addAll(exact.get().getQuestions());
        }

        if (pool.size() < maxPoolSize) {
            List<KnowledgeEntry> related = knowledgeBase.findRelated(topic, 3);
            for (KnowledgeEntry entry : related) {
                if (pool.size() >= maxPoolSize) break;
                if (exact.isPresent() && entry == exact.get()) continue;
                pool.addAll(entry.getQuestions());
            }
        }

        return pool;
    }

    private List<KnowledgeEntry.QuestionTemplate> selectQuestions(
            List<KnowledgeEntry.QuestionTemplate> pool, int count, DifficultyLevel difficulty) {

        if (pool.isEmpty()) return Collections.emptyList();

        // Stratify by difficulty
        Map<String, List<KnowledgeEntry.QuestionTemplate>> byDifficulty = pool.stream()
                .filter(q -> q.getDifficulty() != null)
                .collect(Collectors.groupingBy(q -> q.getDifficulty().toUpperCase()));

        List<KnowledgeEntry.QuestionTemplate> matchingDifficulty =
                new ArrayList<>(byDifficulty.getOrDefault(difficulty.name(), Collections.emptyList()));
        Collections.shuffle(matchingDifficulty);

        List<KnowledgeEntry.QuestionTemplate> selected = new ArrayList<>();
        int take = Math.min(count, matchingDifficulty.size());
        selected.addAll(matchingDifficulty.subList(0, take));

        if (selected.size() < count) {
            // Fall back to any difficulty for the remaining slots
            List<KnowledgeEntry.QuestionTemplate> remaining = pool.stream()
                    .filter(q -> !selected.contains(q))
                    .collect(Collectors.toList());
            Collections.shuffle(remaining);
            int needed = Math.min(count - selected.size(), remaining.size());
            selected.addAll(remaining.subList(0, needed));
        }

        return selected;
    }

    private GeneratedQuizData.GeneratedQuestion convertTemplate(KnowledgeEntry.QuestionTemplate template) {
        List<GeneratedQuizData.GeneratedOption> options = new ArrayList<>();
        List<String> opts = template.getOptions();
        String[] labels = {"A", "B", "C", "D"};
        for (int i = 0; i < opts.size() && i < 4; i++) {
            options.add(GeneratedQuizData.GeneratedOption.builder()
                    .label(labels[i])
                    .text(opts.get(i))
                    .isCorrect(i == template.getCorrectIndex())
                    .build());
        }
        return GeneratedQuizData.GeneratedQuestion.builder()
                .questionText(template.getQuestion())
                .difficulty(parseDifficulty(template.getDifficulty()))
                .explanation(template.getExplanation())
                .options(options)
                .build();
    }

    private DifficultyLevel parseDifficulty(String d) {
        if (d == null) return DifficultyLevel.MEDIUM;
        try {
            return DifficultyLevel.valueOf(d.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DifficultyLevel.MEDIUM;
        }
    }

    private String determineCategory(String topic) {
        return knowledgeBase.findExact(topic)
                .map(KnowledgeEntry::getCategory)
                .orElseGet(() -> knowledgeBase.findRelated(topic, 1).stream()
                        .map(KnowledgeEntry::getCategory)
                        .findFirst()
                        .orElse("General"));
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }

    @Override
    public boolean isAvailable() {
        return knowledgeBase.size() > 0;
    }
}
