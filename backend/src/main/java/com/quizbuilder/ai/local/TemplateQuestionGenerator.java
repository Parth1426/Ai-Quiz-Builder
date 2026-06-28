package com.quizbuilder.ai.local;

import com.quizbuilder.ai.GeneratedQuizData;
import com.quizbuilder.model.DifficultyLevel;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Synthesizes meaningful generic MCQ when no curated content matches a topic.
 * <p>
 * Uses a small library of question templates that produce structurally valid
 * quizzes for any topic. While these are not as rich as curated questions,
 * they ensure the system always returns something usable in offline mode.
 */
@Component
public class TemplateQuestionGenerator {

    private static final List<QuestionPattern> PATTERNS = List.of(
            // Pattern 1: Core definition
            new QuestionPattern(
                    "What is the primary purpose or core function of \"%s\"?",
                    List.of(
                            "To establish foundational understanding and provide essential knowledge",
                            "To serve as pure entertainment with no practical value",
                            "To confuse and mislead practitioners",
                            "To exist without any meaningful objective"
                    ),
                    0,
                    "\"%s\" serves important purposes and has concrete objectives that guide its principles."
            ),
            // Pattern 2: Key characteristics
            new QuestionPattern(
                    "Which characteristic is NOT typically associated with \"%s\"?",
                    List.of(
                            "Involves structured learning and progressive skill development",
                            "Requires commitment and consistent practice over time",
                            "Can be mastered instantly without any effort",
                            "Has established principles and best practices"
                    ),
                    2,
                    "Mastery in any field requires time, effort, and sustained practice; there are no shortcuts."
            ),
            // Pattern 3: Essential skills/knowledge
            new QuestionPattern(
                    "Which skill or knowledge is fundamental for success in \"%s\"?",
                    List.of(
                            "Understanding basic principles and core concepts",
                            "Avoiding any form of study or preparation",
                            "Relying solely on guessing and luck",
                            "Ignoring established guidelines and standards"
                    ),
                    0,
                    "Foundation in core concepts is essential for progressing in any field of study."
            ),
            // Pattern 4: Common misconception
            new QuestionPattern(
                    "Which is a common misconception about \"%s\"?",
                    List.of(
                            "It requires dedication and continuous learning",
                            "It has no practical applications or real-world value",
                            "It demands focus and attention to detail",
                            "It involves following established methodologies"
                    ),
                    1,
                    "Many mistakenly believe that complex subjects lack practical value, but most disciplines have significant real-world applications."
            ),
            // Pattern 5: Importance/significance
            new QuestionPattern(
                    "Why is learning about \"%s\" considered important?",
                    List.of(
                            "It develops critical thinking and applicable skills",
                            "It serves no useful purpose",
                            "It's only important for entertainment",
                            "It has never impacted society or individuals"
                    ),
                    0,
                    "Understanding \"%s\" builds valuable skills and knowledge applicable to real-world scenarios."
            ),
            // Pattern 6: Types or categories
            new QuestionPattern(
                    "Which aspect represents a key dimension or type within \"%s\"?",
                    List.of(
                            "Different approaches, methodologies, or specialized areas",
                            "Topics that are completely unrelated",
                            "Fictional elements with no real substance",
                            "Randomly selected unrelated concepts"
                    ),
                    0,
                    "\"%s\" encompasses multiple approaches, specializations, and distinct areas of focus."
            ),
            // Pattern 7: Best practices
            new QuestionPattern(
                    "What is considered best practice when approaching \"%s\"?",
                    List.of(
                            "Start with fundamentals and build progressively",
                            "Skip basics and attempt advanced topics immediately",
                            "Never seek guidance from experts",
                            "Ignore established standards and guidelines"
                    ),
                    0,
                    "Starting with strong fundamentals and progressing systematically leads to better outcomes."
            ),
            // Pattern 8: Requirements
            new QuestionPattern(
                    "Which is typically required to excel in \"%s\"?",
                    List.of(
                            "Dedication, continuous learning, and persistent practice",
                            "Avoiding any form of instruction",
                            "Rejecting feedback and improvement suggestions",
                            "Refusing to engage with the material seriously"
                    ),
                    0,
                    "Excellence in any field requires commitment, ongoing education, and willingness to improve."
            ),
            // Pattern 9: Real-world application
            new QuestionPattern(
                    "How does understanding \"%s\" benefit practical applications?",
                    List.of(
                            "It provides frameworks and tools applicable to real situations",
                            "It has no connection to real-world problems",
                            "It serves only theoretical purposes",
                            "It actively hinders practical problem-solving"
                    ),
                    0,
                    "Knowledge of \"%s\" equips individuals with tools and frameworks for solving real problems."
            ),
            // Pattern 10: Learning progression
            new QuestionPattern(
                    "What is the recommended progression when mastering \"%s\"?",
                    List.of(
                            "Gradually build from basics through intermediate to advanced concepts",
                            "Attempt everything simultaneously regardless of difficulty",
                            "Focus only on entertaining aspects and ignore substance",
                            "Skip all foundational learning and jump to complex topics"
                    ),
                    0,
                    "A structured progression from foundational to advanced concepts ensures comprehensive understanding."
            ),
            // Pattern 11: Historical/contextual importance
            new QuestionPattern(
                    "Understanding the evolution and context of \"%s\" is valuable because:",
                    List.of(
                            "It explains current practices and how the field developed",
                            "History is completely irrelevant to modern practice",
                            "Past practices should never inform current approaches",
                            "Context has no bearing on understanding the subject"
                    ),
                    0,
                    "Context and history illuminate why current practices exist and how they've evolved."
            ),
            // Pattern 12: Challenges
            new QuestionPattern(
                    "Which challenge is commonly faced when learning \"%s\"?",
                    List.of(
                            "Balancing foundational knowledge with progressive skill development",
                            "Learning is instantaneous with no effort required",
                            "There are no obstacles or difficulties to overcome",
                            "Everyone masters it without any practice"
                    ),
                    0,
                    "Most learners face the challenge of balancing breadth and depth while building competence."
            ),
            // Pattern 13: Critical thinking
            new QuestionPattern(
                    "When evaluating information about \"%s\", what is crucial?",
                    List.of(
                            "Verify sources, consider multiple perspectives, and think critically",
                            "Accept all claims without question",
                            "Believe only the first source encountered",
                            "Reject anything that contradicts personal opinion"
                    ),
                    1,
                    "Critical evaluation of sources and perspectives is essential for accurate understanding."
            ),
            // Pattern 14: Expert perspective
            new QuestionPattern(
                    "Why is seeking expert guidance beneficial in \"%s\"?",
                    List.of(
                            "Experts provide validated knowledge and accelerate learning curves",
                            "Experts mislead learners intentionally",
                            "Expertise is irrelevant to learning outcomes",
                            "Learning is better done in complete isolation"
                    ),
                    0,
                    "Expert guidance provides validated approaches and helps avoid common pitfalls."
            ),
            // Pattern 15: Integration with other knowledge
            new QuestionPattern(
                    "How does \"%s\" typically relate to broader knowledge domains?",
                    List.of(
                            "It connects to multiple disciplines and cross-disciplinary concepts",
                            "It exists in complete isolation from other subjects",
                            "It contradicts all related fields",
                            "It has no connections to any other area of study"
                    ),
                    0,
                    "\"%s\" integrates with and draws from multiple related disciplines and fields."
            ),
            // Pattern 16: Professional/personal development
            new QuestionPattern(
                    "What personal or professional benefit can mastering \"%s\" provide?",
                    List.of(
                            "Enhanced capabilities, credibility, and career opportunities",
                            "No benefits whatsoever",
                            "Hindrance to professional growth",
                            "Irrelevance to any career path"
                    ),
                    0,
                    "Expertise in \"%s\" enhances professional competence and opens career opportunities."
            ),
            // Pattern 17: Problem-solving approach
            new QuestionPattern(
                    "When faced with a problem in \"%s\", the best approach is to:",
                    List.of(
                            "Apply learned principles systematically and adapt to context",
                            "React emotionally without analysis",
                            "Ignore the problem and hope it resolves",
                            "Use unrelated techniques from other domains"
                    ),
                    0,
                    "Systematic application of discipline-specific principles leads to effective solutions."
            ),
            // Pattern 18: Continuous improvement
            new QuestionPattern(
                    "Why is continuous learning important for \"%s\" expertise?",
                    List.of(
                            "The field evolves, new methods emerge, and stagnation leads to obsolescence",
                            "Once learned, the field never changes",
                            "Experts should never update their knowledge",
                            "Learning stops after initial instruction"
                    ),
                    0,
                    "Fields constantly evolve; continuous learning ensures practitioners remain current and effective."
            )
    );

    public List<GeneratedQuizData.GeneratedQuestion> synthesize(
            String topic, int count, DifficultyLevel difficulty) {

        List<QuestionPattern> availablePatterns = selectPatternsByDifficulty(difficulty);
        List<QuestionPattern> shuffled = new ArrayList<>(availablePatterns);
        Collections.shuffle(shuffled);

        List<GeneratedQuizData.GeneratedQuestion> result = new ArrayList<>();
        String safeTopic = topic == null || topic.isBlank() ? "this topic" : topic.trim();

        for (int i = 0; i < count; i++) {
            QuestionPattern pattern = shuffled.get(i % shuffled.size());
            result.add(buildQuestion(pattern, safeTopic, difficulty));
        }
        return result;
    }

    /**
     * Select question patterns appropriate to difficulty level.
     * EASY: Definitions, characteristics, fundamentals
     * MEDIUM: Application, integration, problem-solving
     * HARD: Critical thinking, misconceptions, advanced concepts
     */
    private List<QuestionPattern> selectPatternsByDifficulty(DifficultyLevel difficulty) {
        return switch (difficulty) {
            case EASY -> List.of(
                    PATTERNS.get(0),  // Core definition
                    PATTERNS.get(2),  // Essential skills
                    PATTERNS.get(4),  // Importance
                    PATTERNS.get(5),  // Types/dimensions
                    PATTERNS.get(7)   // Requirements
            );
            case MEDIUM -> List.of(
                    PATTERNS.get(1),  // Key characteristics
                    PATTERNS.get(6),  // Best practices
                    PATTERNS.get(8),  // Real-world application
                    PATTERNS.get(9),  // Learning progression
                    PATTERNS.get(13), // Expert guidance
                    PATTERNS.get(16)  // Professional benefits
            );
            case HARD -> List.of(
                    PATTERNS.get(3),  // Common misconceptions
                    PATTERNS.get(10), // Historical context
                    PATTERNS.get(11), // Challenges
                    PATTERNS.get(12), // Critical thinking
                    PATTERNS.get(14), // Integration with other knowledge
                    PATTERNS.get(15), // Problem-solving
                    PATTERNS.get(17)  // Continuous improvement
            );
        };
    }

    private GeneratedQuizData.GeneratedQuestion buildQuestion(
            QuestionPattern pattern, String topic, DifficultyLevel difficulty) {

        // Randomize option order while tracking correct answer
        List<Integer> indices = new ArrayList<>(List.of(0, 1, 2, 3));
        Collections.shuffle(indices);

        String[] labels = {"A", "B", "C", "D"};
        List<GeneratedQuizData.GeneratedOption> options = new ArrayList<>();
        for (int i = 0; i < indices.size(); i++) {
            int originalIndex = indices.get(i);
            options.add(GeneratedQuizData.GeneratedOption.builder()
                    .label(labels[i])
                    .text(pattern.options.get(originalIndex))
                    .isCorrect(originalIndex == pattern.correctIndex)
                    .build());
        }

        return GeneratedQuizData.GeneratedQuestion.builder()
                .questionText(String.format(pattern.template, topic))
                .difficulty(difficulty)
                .explanation(pattern.explanation)
                .options(options)
                .build();
    }

    private record QuestionPattern(
            String template,
            List<String> options,
            int correctIndex,
            String explanation) {}
}
