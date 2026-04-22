package com.quizzy.backend.controller;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.Student;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.StudentRepository;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class BadgeController {

    private final BadgeRepository badgeRepository;
    private final StudentRepository studentRepository;

    public BadgeController(BadgeRepository badgeRepository, StudentRepository studentRepository) {
        this.badgeRepository = badgeRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/{userId}/badges")
    public ResponseEntity<?> getUserBadges(@PathVariable Integer userId) {
        try {
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new Exception("Student not found for user_id: " + userId));

            List<Badge> allBadges = badgeRepository.findAll();
            
            String earnedBadgesJson = student.getEarnedBadges();
            if (earnedBadgesJson == null || earnedBadgesJson.trim().isEmpty() || earnedBadgesJson.equals("null")) {
                earnedBadgesJson = "[]";
            }
            
            JSONArray earnedArray = new JSONArray(earnedBadgesJson);
            List<Integer> earnedIds = new ArrayList<>();
            for (int i = 0; i < earnedArray.length(); i++) {
                earnedIds.add(earnedArray.getInt(i));
            }

            List<Map<String, Object>> response = new ArrayList<>();
            for (Badge badge : allBadges) {
                Map<String, Object> badgeMap = new HashMap<>();
                badgeMap.put("badgeId", (long) badge.getBadgeId());
                badgeMap.put("name", badge.getBadgeName());
                badgeMap.put("description", badge.getDescription());
                badgeMap.put("unlocked", earnedIds.contains(badge.getBadgeId()));
                response.add(badgeMap);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
