package com.quizbuilder.controller;

import com.quizbuilder.dto.request.SubmitAttemptRequest;
import com.quizbuilder.dto.response.ApiResponse;
import com.quizbuilder.dto.response.AttemptResultResponse;
import com.quizbuilder.dto.response.LeaderboardEntryResponse;
import com.quizbuilder.service.AttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attempts")
@RequiredArgsConstructor
@Tag(name = "Attempts", description = "Submit quiz attempts and view results / leaderboard")
@CrossOrigin
public class AttemptController {

    private final AttemptService attemptService;

    @Operation(summary = "Submit a quiz attempt and get the graded result")
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<AttemptResultResponse>> submit(@Valid @RequestBody SubmitAttemptRequest request) {
        AttemptResultResponse result = attemptService.submit(request);
        return ResponseEntity.ok(ApiResponse.success("Attempt graded", result));
    }

    @Operation(summary = "Retrieve a previously submitted attempt by ID")
    @GetMapping("/{attemptId}")
    public ResponseEntity<ApiResponse<AttemptResultResponse>> get(@PathVariable String attemptId) {
        return ResponseEntity.ok(ApiResponse.success(attemptService.getAttempt(attemptId)));
    }

    @Operation(summary = "Get the global leaderboard")
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> leaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(attemptService.getLeaderboard(limit)));
    }
}
