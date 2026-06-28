package com.quizbuilder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts", indexes = {
    @Index(name = "idx_attempt_quiz_id", columnList = "quiz_id"),
    @Index(name = "idx_attempt_session_id", columnList = "session_id"),
    @Index(name = "idx_attempt_completed_at", columnList = "completed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "score")
    private Integer score;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "time_taken_seconds")
    private Long timeTakenSeconds;

    @Column(name = "completed")
    @Builder.Default
    private Boolean completed = false;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserAnswer> userAnswers = new ArrayList<>();
}
