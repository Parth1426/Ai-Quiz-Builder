package com.quizbuilder.dto.request;

import com.quizbuilder.model.DifficultyLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GenerateQuizRequest {

    @NotBlank(message = "Topic is required")
    @Size(min = 2, max = 200, message = "Topic must be between 2 and 200 characters")
    private String topic;

    @Min(value = 3, message = "Minimum 3 questions")
    @Max(value = 10, message = "Maximum 10 questions")
    private int questionCount = 5;

    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    @Size(max = 100)
    private String category;
}
