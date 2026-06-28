package com.quizbuilder.controller;

import com.quizbuilder.dto.response.ApiResponse;
import com.quizbuilder.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Stats", description = "System statistics and metadata endpoints")
@CrossOrigin
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "Get global system statistics")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getStats()));
    }

    @Operation(summary = "Get list of curated topics from the trained knowledge base")
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getCuratedTopics() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getCuratedTopics()));
    }

    @Operation(summary = "Get available topic categories")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getAllCategories()));
    }
}
