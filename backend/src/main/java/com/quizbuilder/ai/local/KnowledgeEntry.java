package com.quizbuilder.ai.local;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeEntry {

    private String topic;
    private String category;
    /** Optional synonyms / alternative names users might type for this topic. */
    private List<String> aliases;
    private List<QuestionTemplate> questions;

    public List<String> getAliases() {
        return aliases == null ? Collections.emptyList() : aliases;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTemplate {
        private String question;
        private List<String> options; // 4 options, A B C D
        private int correctIndex;     // 0..3
        private String explanation;
        private String difficulty;    // EASY / MEDIUM / HARD
    }
}
