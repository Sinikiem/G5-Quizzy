package com.quizzy.backend.controller;

import com.quizzy.backend.model.Guardian;
import com.quizzy.backend.model.Student;
import com.quizzy.backend.model.User;
import com.quizzy.backend.repository.GuardianRepository;
import com.quizzy.backend.repository.StudentRepository;
import com.quizzy.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/guardian")
@CrossOrigin(origins = "*")
public class GuardianController {

    private final GuardianRepository guardianRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public GuardianController(GuardianRepository guardianRepository,
                              StudentRepository studentRepository,
                              UserRepository userRepository) {
        this.guardianRepository = guardianRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}/student-score")
    public ResponseEntity<?> getStudentScore(@PathVariable Integer userId) {
        try {
            // 1. Ensure this user has a guardian entry (Auto-association)
            if (guardianRepository.findByUserId(userId).isEmpty()) {
                Guardian newGuardian = new Guardian();
                newGuardian.setUserId(userId);
                guardianRepository.save(newGuardian);
            }

            // 2. Fetch the user's own student record and profile info
            // Based on your instruction: "associate same user_id in users to same guardian in guardians"
            Optional<Student> studentOpt = studentRepository.findByUserId(userId);
            Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));

            if (studentOpt.isPresent() && userOpt.isPresent()) {
                Student student = studentOpt.get();
                User user = userOpt.get();

                return ResponseEntity.ok(Map.of(
                    "studentName", user.getUsername(),
                    "totalScore", student.getTotalScore() != null ? student.getTotalScore() : 0
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Profile data for user " + userId + " not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
