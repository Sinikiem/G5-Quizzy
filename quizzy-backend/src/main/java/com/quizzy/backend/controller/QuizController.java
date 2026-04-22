package com.quizzy.backend.controller;

import com.quizzy.backend.model.QuizResponse;
import com.quizzy.backend.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/test")
    public String test() {
        return "Backend is working!";
    }

    /**
     * Endpoint to trigger AI quiz generation.
     * 
     * @param prompt natural language instructions for the AI.
     * @param userId optional ID of the user requesting the quiz.
     * @param level  optional grade level of the quiz.
     */
    @PostMapping("/quiz/generate")
    public ResponseEntity<?> generateQuiz(
            @RequestParam String prompt, 
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer level) {
        try {
            QuizResponse response = quizService.generateAndStore(prompt, userId, level);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Quiz generation failed: " + e.getMessage()));
        }
    }

    /**
     * Simple logging endpoint to allow the frontend to print messages to the backend console.
     */
    @PostMapping("/log")
    public ResponseEntity<Void> logToConsole(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message != null) {
            System.out.println("[FRONTEND LOG]: " + message);
        }
        return ResponseEntity.ok().build();
    }
}
