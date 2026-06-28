package com.quizbuilder.ai;

import com.quizbuilder.ai.local.KnowledgeBase;
import com.quizbuilder.ai.local.LocalQuizGenerator;
import com.quizbuilder.ai.local.TemplateQuestionGenerator;
import com.quizbuilder.model.DifficultyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LocalQuizGeneratorTest {

    @Autowired
    private LocalQuizGenerator generator;

    @Autowired
    private KnowledgeBase knowledgeBase;

    @BeforeEach
    void verifyAvailable() {
        assertTrue(generator.isAvailable(), "Local generator should be available after knowledge base loads");
        assertTrue(knowledgeBase.size() > 0, "Knowledge base should have entries");
    }

    @Test
    void generatesCuratedQuestionsForKnownTopic() {
        GeneratedQuizData quiz = generator.generate("Photosynthesis", 3, DifficultyLevel.EASY);
        assertNotNull(quiz);
        assertEquals(3, quiz.getQuestions().size());
        assertEquals("Photosynthesis", quiz.getTopic());
        quiz.getQuestions().forEach(q -> {
            assertEquals(4, q.getOptions().size());
            assertEquals(1, q.getOptions().stream().filter(GeneratedQuizData.GeneratedOption::isCorrect).count());
        });
    }

    @Test
    void fallsBackToTemplatesForUnknownTopic() {
        GeneratedQuizData quiz = generator.generate("Underwater Basket Weaving", 5, DifficultyLevel.MEDIUM);
        assertNotNull(quiz);
        assertEquals(5, quiz.getQuestions().size());
        quiz.getQuestions().forEach(q -> {
            assertEquals(4, q.getOptions().size());
            assertEquals(1, q.getOptions().stream().filter(GeneratedQuizData.GeneratedOption::isCorrect).count());
        });
    }

    @Test
    void respectsRequestedDifficulty() {
        GeneratedQuizData hard = generator.generate("Neural Networks", 3, DifficultyLevel.HARD);
        assertEquals(3, hard.getQuestions().size());
    }
}
