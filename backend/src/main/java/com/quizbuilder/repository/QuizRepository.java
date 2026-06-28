package com.quizbuilder.repository;

import com.quizbuilder.model.Quiz;
import com.quizbuilder.model.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, String> {

    Page<Quiz> findByStatusOrderByCreatedAtDesc(QuizStatus status, Pageable pageable);

    List<Quiz> findByTopicContainingIgnoreCaseAndStatus(String topic, QuizStatus status);

    @Query("SELECT DISTINCT q.topic FROM Quiz q WHERE q.status = 'READY' ORDER BY q.topic")
    List<String> findDistinctTopics();

    @Query("SELECT q FROM Quiz q WHERE q.status = 'READY' ORDER BY q.createdAt DESC")
    Page<Quiz> findRecentQuizzes(Pageable pageable);

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.status = :status")
    Long countByStatus(@Param("status") QuizStatus status);
}
