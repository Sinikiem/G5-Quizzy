package com.quizzy.backend.repository;

import com.quizzy.backend.model.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    /**
     * Retrieves the last 15 completed quiz sessions for a specific user, ordered by completion date descending.
     */
    List<QuizSession> findTop15ByUserIdAndCompletionIsNotNullOrderByCompletionDesc(Integer userId);
}
