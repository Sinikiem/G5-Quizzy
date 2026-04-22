package com.quizzy.backend.repository;

import com.quizzy.backend.model.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    /**
     * Retrieves all completed quiz sessions for a specific user,
     * ordered by completion date descending (most recent first).
     */
    List<QuizSession> findByUserIdAndCompletionIsNotNullOrderByCompletionDesc(Integer userId);

    /**
     * Retrieves the last 15 completed quiz sessions for a specific user,
     * ordered by completion date descending.
     * You can keep this for other parts of the app if needed.
     */
    List<QuizSession> findTop15ByUserIdAndCompletionIsNotNullOrderByCompletionDesc(Integer userId);
}