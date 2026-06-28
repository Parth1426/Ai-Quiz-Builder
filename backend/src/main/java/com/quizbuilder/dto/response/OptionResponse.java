package com.quizbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionResponse {
    private String id;
    private String optionLabel;
    private String optionText;
    // isCorrect is NOT included in quiz response (revealed only in attempt results)
}
