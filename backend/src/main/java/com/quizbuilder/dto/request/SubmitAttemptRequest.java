package com.quizbuilder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SubmitAttemptRequest {

    @NotBlank(message = "Quiz ID is required")
    private String quizId;

    @Size(max = 100)
    private String userName;

    @Size(max = 100)
    private String sessionId;

    @Min(value = 0)
    private Long timeTakenSeconds;

    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<AnswerEntry> answers;

    @Data
    public static class AnswerEntry {
        @NotBlank(message = "Question ID is required")
        private String questionId;

        private String selectedOptionId;

        @Pattern(regexp = "[A-D]", message = "Selected label must be A, B, C, or D")
        private String selectedLabel;
    }
}
