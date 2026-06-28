package com.quizbuilder.service;

import com.quizbuilder.ai.AiQuizOrchestrator;
import com.quizbuilder.ai.local.KnowledgeBase;
import com.quizbuilder.model.QuizStatus;
import com.quizbuilder.repository.QuizAttemptRepository;
import com.quizbuilder.repository.QuizRepository;
import com.quizbuilder.repository.QuizTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizTopicRepository topicRepository;
    private final KnowledgeBase knowledgeBase;
    private final AiQuizOrchestrator orchestrator;

    @Cacheable("stats")
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuizzes", quizRepository.countByStatus(QuizStatus.READY));
        stats.put("totalAttempts", attemptRepository.countCompletedAttempts());
        stats.put("availableTopics", topicRepository.count());
        stats.put("knowledgeBaseSize", knowledgeBase.size());
        stats.put("activeProvider", orchestrator.getActiveProvider());
        stats.put("availableCategories", knowledgeBase.getAllCategories());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<String> getCuratedTopics() {
        return knowledgeBase.getAllTopics();
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return knowledgeBase.getAllCategories();
    }
}
