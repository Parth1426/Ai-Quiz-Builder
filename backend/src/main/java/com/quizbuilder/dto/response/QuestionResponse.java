package com.quizbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quizbuilder.model.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {
    private String id;
    private String questionText;
    private Integer questionIndex;
    private DifficultyLevel difficulty;
    private String explanation;
    private List<OptionResponse> options;
}
