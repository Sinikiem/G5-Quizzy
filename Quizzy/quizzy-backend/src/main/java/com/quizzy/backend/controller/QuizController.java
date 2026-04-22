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

    @PostMapping("/quiz/generate")
    public ResponseEntity<?> generateQuiz(@RequestParam String prompt, @RequestParam(required = false) Integer userId) {
        try {
            QuizResponse response = quizService.generateAndStore(prompt, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Quiz generation failed: " + e.getMessage()));
        }
    }
}
