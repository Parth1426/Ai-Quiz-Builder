package com.quizbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaderboardEntryResponse {
    private int rank;
    private String attemptId;
    private String userName;
    private String quizTopic;
    private int score;
    private int totalQuestions;
    private double percentage;
    private String grade;
    private Long timeTakenSeconds;
    private LocalDateTime completedAt;
}
