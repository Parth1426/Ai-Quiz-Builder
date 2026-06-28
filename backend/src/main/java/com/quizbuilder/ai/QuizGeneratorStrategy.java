package com.quizbuilder.ai;

import com.quizbuilder.model.DifficultyLevel;

public interface QuizGeneratorStrategy {

    GeneratedQuizData generate(String topic, int questionCount, DifficultyLevel difficulty);

    String getProviderName();

    boolean isAvailable();
}
