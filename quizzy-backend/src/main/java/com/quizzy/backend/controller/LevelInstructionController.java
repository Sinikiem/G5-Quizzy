package com.quizzy.backend.controller;

import com.quizzy.backend.model.LevelInstruction;
import com.quizzy.backend.model.QuizResponse;
import com.quizzy.backend.repository.LevelInstructionRepository;
import com.quizzy.backend.service.QuizService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LevelInstructionController {

    private final LevelInstructionRepository levelInstructionRepository;
    private final QuizService quizService;

    public LevelInstructionController(LevelInstructionRepository levelInstructionRepository,
                                      QuizService quizService) {
        this.levelInstructionRepository = levelInstructionRepository;
        this.quizService = quizService;
    }

    @GetMapping("/instructions/{gradeLevel}")
    public LevelInstruction getInstructionByGradeLevel(@PathVariable Integer gradeLevel) {
        return levelInstructionRepository.findByGradeLevel(gradeLevel)
                .orElseThrow(() -> new RuntimeException("No instruction found for grade level: " + gradeLevel));
    }

    @GetMapping("/generate-quiz/{gradeLevel}")
    public QuizResponse generateQuiz(@PathVariable Integer gradeLevel, @RequestParam(required = false) Integer userId) throws Exception {
        LevelInstruction instruction = levelInstructionRepository.findByGradeLevel(gradeLevel)
                .orElseThrow(() -> new RuntimeException("No instruction found for grade level: " + gradeLevel));

        String prompt = instruction.getPromptTemplate() + " " + instruction.getInstructionText();
        // Fixed: Pass gradeLevel as the third argument to match updated QuizService method signature
        return quizService.generateAndStore(prompt, userId, gradeLevel);
    }
}
