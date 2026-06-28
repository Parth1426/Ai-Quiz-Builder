package com.quizbuilder.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_answers", indexes = {
    @Index(name = "idx_user_answer_attempt_id", columnList = "attempt_id"),
    @Index(name = "idx_user_answer_question_id", columnList = "question_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "selected_option_id")
    private String selectedOptionId;

    @Column(name = "selected_label", length = 1)
    private String selectedLabel;

    @Column(name = "is_correct")
    private Boolean isCorrect;
}
