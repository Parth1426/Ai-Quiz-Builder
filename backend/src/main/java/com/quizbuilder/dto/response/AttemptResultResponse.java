package com.quizbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttemptResultResponse {
    private String attemptId;
    private String quizId;
    private String quizTopic;
    private String userName;
    private int score;
    private int totalQuestions;
    private double percentage;
    private String grade;
    private Long timeTakenSeconds;
    private LocalDateTime completedAt;
    private List<QuestionResultDetail> questionResults;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionResultDetail {
        private String questionId;
        private String questionText;
        private String selectedLabel;
        private String selectedOptionText;
        private String correctLabel;
        private String correctOptionText;
        @JsonProperty("isCorrect")
        private boolean isCorrect;
        private String explanation;
        private List<OptionResultDetail> options;
    }

    @Data
    @Builder
    public static class OptionResultDetail {
        private String id;
        private String optionLabel;
        private String optionText;
        @JsonProperty("isCorrect")
        private boolean isCorrect;
        @JsonProperty("wasSelected")
        private boolean wasSelected;
    }
}
