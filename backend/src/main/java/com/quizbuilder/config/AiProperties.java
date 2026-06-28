package com.quizbuilder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiProperties {

    private String provider = "mock";
    private OpenAi openai = new OpenAi();
    private Quiz quiz = new Quiz();

    @Data
    public static class OpenAi {
        private String apiKey = "";
        private String model = "gpt-3.5-turbo";
        private int maxTokens = 2000;
        private double temperature = 0.7;
        private String baseUrl = "https://api.openai.com/v1";
    }

    @Data
    public static class Quiz {
        private int defaultQuestionCount = 5;
        private int minQuestionCount = 3;
        private int maxQuestionCount = 10;
        private String defaultDifficulty = "MEDIUM";
        private int generationTimeoutSeconds = 30;
        private int cacheTtlMinutes = 60;
    }
}
