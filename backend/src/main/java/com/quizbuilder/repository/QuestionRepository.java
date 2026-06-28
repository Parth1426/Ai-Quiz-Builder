package com.quizbuilder.repository;

import com.quizbuilder.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findByQuizIdOrderByQuestionIndexAsc(String quizId);
    void deleteByQuizId(String quizId);
}
