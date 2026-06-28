package com.quizbuilder.repository;

import com.quizbuilder.model.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, String> {

    Page<QuizAttempt> findByCompletedTrueOrderByCompletedAtDesc(Pageable pageable);

    List<QuizAttempt> findByQuizIdAndCompletedTrue(String quizId);

    @Query("SELECT a FROM QuizAttempt a WHERE a.completed = true ORDER BY (CAST(a.score AS float) / a.totalQuestions) DESC, a.completedAt DESC")
    Page<QuizAttempt> findLeaderboard(Pageable pageable);

    @Query("SELECT AVG(CAST(a.score AS float) / a.totalQuestions) FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.completed = true")
    Double findAvgScoreByQuizId(@Param("quizId") String quizId);

    @Query("SELECT COUNT(a) FROM QuizAttempt a WHERE a.completed = true")
    Long countCompletedAttempts();
}
