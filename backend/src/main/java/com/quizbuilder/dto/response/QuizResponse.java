package com.quizbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizbuilder.model.DifficultyLevel;
import com.quizbuilder.model.QuizStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizResponse {
    private String id;
    private String topic;
    private String category;
    private DifficultyLevel difficulty;
    private QuizStatus status;
    private String aiProvider;
    private LocalDateTime createdAt;
    private List<QuestionResponse> questions;
    private Integer questionCount;
    private Integer totalAttempts;
    private Double averageScore;
}
