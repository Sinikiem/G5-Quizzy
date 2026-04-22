package com.quizzy.backend.controller;

import com.quizzy.backend.model.StudyPlanRequest;
import com.quizzy.backend.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StudyPlanController {

    private final AIService aiService;

    public StudyPlanController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/study-plan")
    public ResponseEntity<?> generateStudyPlan(@RequestBody StudyPlanRequest request) {
        try {
            String planJson = aiService.generateStudyPlan(
                    request.getTopic(),
                    request.getAccuracy(),
                    request.getGradeLevel()
            );
            return ResponseEntity.ok(planJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Study plan generation failed: " + e.getMessage()));
        }
    }
}