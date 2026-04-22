package com.quizzy.backend.controller;

import com.quizzy.backend.model.QuizSession;
import com.quizzy.backend.repository.QuizSessionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guardian")
@CrossOrigin(origins = "*")
public class GuardianController {

    private final QuizSessionRepository quizSessionRepository;

    public GuardianController(QuizSessionRepository quizSessionRepository) {
        this.quizSessionRepository = quizSessionRepository;
    }

    @GetMapping("/{userId}/latest-sessions")
    public List<Map<String, Object>> getLatestSessions(@PathVariable Integer userId) {
        List<QuizSession> allUserSessions =
                quizSessionRepository.findByUserIdAndCompletionIsNotNullOrderByCompletionDesc(userId);

        List<Map<String, Object>> response = new ArrayList<>();

        int limit = Math.min(15, allUserSessions.size());

        for (int i = 0; i < limit; i++) {
            QuizSession session = allUserSessions.get(i);

            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("displaySessionNumber", i + 1); // 1 = most recent, 2 = next, etc.
            sessionMap.put("id", session.getId());
            sessionMap.put("userId", session.getUserId());
            sessionMap.put("questionsJson", session.getQuestionsJson());
            sessionMap.put("createdAt", session.getCreatedAt());
            sessionMap.put("completion", session.getCompletion());
            sessionMap.put("finalscore", session.getFinalscore());

            response.add(sessionMap);
        }

        return response;
    }
}