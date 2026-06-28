package com.quizbuilder.ai;

import com.quizbuilder.model.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuizData {

    private String topic;
    private String category;
    private String aiProvider;
    private List<GeneratedQuestion> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedQuestion {
        private String questionText;
        private DifficultyLevel difficulty;
        private String explanation;
        private List<GeneratedOption> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedOption {
        private String label; // A, B, C, D
        private String text;
        private boolean isCorrect;
    }
}
