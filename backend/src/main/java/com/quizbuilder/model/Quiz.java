package com.quizbuilder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes", indexes = {
    @Index(name = "idx_quiz_topic", columnList = "topic"),
    @Index(name = "idx_quiz_status", columnList = "status"),
    @Index(name = "idx_quiz_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "category", length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private QuizStatus status = QuizStatus.GENERATING;

    @Column(name = "ai_provider", length = 50)
    private String aiProvider;

    @Column(name = "generation_prompt", columnDefinition = "TEXT")
    private String generationPrompt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("questionIndex ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuizAttempt> attempts = new ArrayList<>();
}
