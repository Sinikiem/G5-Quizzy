package com.quizzy.backend.controller;

import com.quizzy.backend.model.QuizResponse;
import com.quizzy.backend.service.QuizService;
import org.springframework.web.bind.annotation.*;

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
    public QuizResponse generateQuiz(@RequestParam String prompt, @RequestParam(required = false) Integer userId) throws Exception {
        return quizService.generateAndStore(prompt, userId);
    }
}
