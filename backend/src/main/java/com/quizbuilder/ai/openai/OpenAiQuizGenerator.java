package com.quizbuilder.ai.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quizbuilder.ai.GeneratedQuizData;
import com.quizbuilder.ai.QuizGeneratorStrategy;
import com.quizbuilder.config.AiProperties;
import com.quizbuilder.exception.AiGenerationException;
import com.quizbuilder.model.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI-based quiz generator. Calls the Chat Completions endpoint and
 * requests structured JSON output. Falls back gracefully when no API key is
 * provided or the network is unreachable (e.g. firewall restrictions).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiQuizGenerator implements QuizGeneratorStrategy {

    public static final String PROVIDER = "OpenAI";

    private final AiProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    private RestClient buildClient() {
        return RestClient.builder()
                .baseUrl(props.getOpenai().getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getOpenai().getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public GeneratedQuizData generate(String topic, int questionCount, DifficultyLevel difficulty) {
        if (!isAvailable()) {
            throw new AiGenerationException("OpenAI API key is not configured");
        }

        String prompt = buildPrompt(topic, questionCount, difficulty);
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", props.getOpenai().getModel());
        payload.put("temperature", props.getOpenai().getTemperature());
        payload.put("max_tokens", props.getOpenai().getMaxTokens());
        payload.set("response_format", mapper.createObjectNode().put("type", "json_object"));

        ArrayNode messages = mapper.createArrayNode();
        messages.add(mapper.createObjectNode()
                .put("role", "system")
                .put("content", systemPrompt()));
        messages.add(mapper.createObjectNode()
                .put("role", "user")
                .put("content", prompt));
        payload.set("messages", messages);

        try {
            String responseBody = buildClient()
                    .post()
                    .uri("/chat/completions")
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            JsonNode root = mapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            return parseModelResponse(topic, content);
        } catch (RestClientException e) {
            log.warn("OpenAI request failed: {}", e.getMessage());
            throw new AiGenerationException("OpenAI request failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.warn("Failed to parse OpenAI response: {}", e.getMessage());
            throw new AiGenerationException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }

    private GeneratedQuizData parseModelResponse(String topic, String content) throws Exception {
        JsonNode parsed = mapper.readTree(content);
        JsonNode questionsNode = parsed.has("questions") ? parsed.get("questions") : parsed;

        if (!questionsNode.isArray()) {
            throw new AiGenerationException("OpenAI response did not contain a questions array");
        }

        List<GeneratedQuizData.GeneratedQuestion> questions = new ArrayList<>();
        for (JsonNode q : questionsNode) {
            String question = q.path("question").asText();
            String explanation = q.path("explanation").asText("");
            String difficulty = q.path("difficulty").asText("MEDIUM");
            JsonNode optionsArr = q.path("options");
            String correctLabel = q.path("correctAnswer").asText("A").toUpperCase();

            List<GeneratedQuizData.GeneratedOption> options = new ArrayList<>();
            String[] labels = {"A", "B", "C", "D"};
            for (int i = 0; i < optionsArr.size() && i < 4; i++) {
                String label = labels[i];
                options.add(GeneratedQuizData.GeneratedOption.builder()
                        .label(label)
                        .text(optionsArr.get(i).asText())
                        .isCorrect(label.equals(correctLabel))
                        .build());
            }

            questions.add(GeneratedQuizData.GeneratedQuestion.builder()
                    .questionText(question)
                    .difficulty(parseDifficulty(difficulty))
                    .explanation(explanation)
                    .options(options)
                    .build());
        }

        return GeneratedQuizData.builder()
                .topic(topic)
                .category("AI-Generated")
                .aiProvider(PROVIDER)
                .questions(questions)
                .build();
    }

    private DifficultyLevel parseDifficulty(String d) {
        try {
            return DifficultyLevel.valueOf(d.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DifficultyLevel.MEDIUM;
        }
    }

    private String systemPrompt() {
        return """
                You are an expert quiz designer. Generate high quality multiple-choice questions.
                Return ONLY valid JSON in this exact schema:
                {
                  "questions": [
                    {
                      "question": "string",
                      "options": ["string", "string", "string", "string"],
                      "correctAnswer": "A|B|C|D",
                      "explanation": "string",
                      "difficulty": "EASY|MEDIUM|HARD"
                    }
                  ]
                }
                Rules:
                - Each question must have exactly 4 options
                - Exactly one correct option
                - Explanations should educate the reader
                - Avoid ambiguous wording
                - Ensure factual accuracy
                """;
    }

    private String buildPrompt(String topic, int count, DifficultyLevel difficulty) {
        return String.format("""
                Generate %d multiple-choice quiz questions about "%s" at %s difficulty level.
                Make sure the questions are factually accurate, clearly written, and educational.
                Return strict JSON matching the agreed schema.
                """, count, topic, difficulty.name());
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }

    @Override
    public boolean isAvailable() {
        String key = props.getOpenai().getApiKey();
        return key != null && !key.isBlank() && key.length() > 10;
    }
}
