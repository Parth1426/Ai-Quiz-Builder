package com.quizbuilder.repository;

import com.quizbuilder.model.QuizTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizTopicRepository extends JpaRepository<QuizTopic, Long> {

    Optional<QuizTopic> findByTopicNameIgnoreCase(String topicName);

    List<QuizTopic> findByIsFeaturedTrue();

    List<QuizTopic> findByCategory(String category);

    Page<QuizTopic> findByTopicNameContainingIgnoreCase(String search, Pageable pageable);

    @Query("SELECT DISTINCT t.category FROM QuizTopic t ORDER BY t.category")
    List<String> findDistinctCategories();

    @Modifying
    @Transactional
    @Query("UPDATE QuizTopic t SET t.usageCount = t.usageCount + 1 WHERE t.topicName = :topicName")
    void incrementUsageCount(@Param("topicName") String topicName);

    @Query("SELECT t FROM QuizTopic t ORDER BY t.usageCount DESC")
    Page<QuizTopic> findTopByUsage(Pageable pageable);
}
