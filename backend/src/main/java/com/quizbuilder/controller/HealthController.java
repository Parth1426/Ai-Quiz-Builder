package com.quizbuilder.controller;

import com.quizbuilder.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Service health and info")
@CrossOrigin
public class HealthController {

    @Operation(summary = "Simple health check")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "UP",
                "service", "ai-quiz-builder",
                "version", "1.0.0"
        )));
    }
}
