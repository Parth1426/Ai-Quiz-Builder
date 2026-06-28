package com.quizbuilder.controller;

import com.quizbuilder.dto.request.GenerateQuizRequest;
import com.quizbuilder.dto.response.ApiResponse;
import com.quizbuilder.dto.response.QuizResponse;
import com.quizbuilder.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "Endpoints for generating, retrieving, and managing AI-generated quizzes")
@CrossOrigin
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Generate a new AI quiz",
            description = "Generates a quiz with the requested number of MCQs for the given topic and difficulty.")
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<QuizResponse>> generate(@Valid @RequestBody GenerateQuizRequest request) {
        QuizResponse result = quizService.generateAndSave(request);
        return ResponseEntity.ok(ApiResponse.success("Quiz generated successfully", result));
    }

    @Operation(summary = "Get a specific quiz by ID")
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(@PathVariable String quizId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizForTaking(quizId)));
    }

    @Operation(summary = "List all available quizzes (paginated)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(quizService.listQuizzes(page, size)));
    }

    @Operation(summary = "Get recently generated quizzes")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> recent(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getRecentQuizzes(limit)));
    }

    @Operation(summary = "Get all unique topics from generated quizzes")
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> topics() {
        return ResponseEntity.ok(ApiResponse.success(quizService.getAvailableTopics()));
    }

    @Operation(summary = "Delete a quiz by ID")
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.success("Quiz deleted", null));
    }
}
