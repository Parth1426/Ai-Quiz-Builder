package com.quizbuilder.ai;

import com.quizbuilder.ai.local.LocalQuizGenerator;
import com.quizbuilder.ai.openai.OpenAiQuizGenerator;
import com.quizbuilder.config.AiProperties;
import com.quizbuilder.exception.AiGenerationException;
import com.quizbuilder.model.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates the choice of AI provider used to generate a quiz. Implements
 * primary + fallback strategy: try the configured / preferred provider, and on
 * failure transparently fall back to the local trained model so the system
 * remains operational even when external services are blocked by firewalls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuizOrchestrator {

    private final AiProperties props;
    private final OpenAiQuizGenerator openAiGenerator;
    private final LocalQuizGenerator localGenerator;

    public GeneratedQuizData generateQuiz(String topic, int questionCount, DifficultyLevel difficulty) {
        List<QuizGeneratorStrategy> chain = buildStrategyChain();

        AiGenerationException lastError = null;
        for (QuizGeneratorStrategy strategy : chain) {
            try {
                if (!strategy.isAvailable()) {
                    log.debug("Strategy {} not available, skipping", strategy.getProviderName());
                    continue;
                }
                log.info("Attempting generation with provider: {}", strategy.getProviderName());
                return strategy.generate(topic, questionCount, difficulty);
            } catch (Exception e) {
                lastError = e instanceof AiGenerationException ae
                        ? ae : new AiGenerationException(e.getMessage(), e);
                log.warn("Provider {} failed: {} - trying next fallback",
                        strategy.getProviderName(), e.getMessage());
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new AiGenerationException("No AI provider available to generate quiz");
    }

    private List<QuizGeneratorStrategy> buildStrategyChain() {
        List<QuizGeneratorStrategy> chain = new ArrayList<>();
        String preferred = props.getProvider() != null ? props.getProvider().toLowerCase() : "mock";

        if ("openai".equals(preferred)) {
            chain.add(openAiGenerator);
            chain.add(localGenerator); // automatic fallback
        } else {
            chain.add(localGenerator);
            if (openAiGenerator.isAvailable()) {
                chain.add(openAiGenerator);
            }
        }
        return chain;
    }

    public String getActiveProvider() {
        for (QuizGeneratorStrategy s : buildStrategyChain()) {
            if (s.isAvailable()) return s.getProviderName();
        }
        return "none";
    }
}
