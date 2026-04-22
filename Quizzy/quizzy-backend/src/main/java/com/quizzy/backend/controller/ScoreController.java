package com.quizzy.backend.controller;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.Student;
import com.quizzy.backend.model.QuizSession;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.StudentRepository;
import com.quizzy.backend.repository.QuizSessionRepository;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/score")
@CrossOrigin(origins = "*")
public class ScoreController {

    private final StudentRepository studentRepository;
    private final BadgeRepository badgeRepository;
    private final QuizSessionRepository sessionRepository;

    public ScoreController(StudentRepository studentRepository, 
                           BadgeRepository badgeRepository,
                           QuizSessionRepository sessionRepository) {
        this.studentRepository = studentRepository;
        this.badgeRepository = badgeRepository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserScore(@PathVariable Integer userId) {
        try {
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new Exception("Student not found for user_id: " + userId));
            
            // Logic to get the last 15 quiz sessions for further display
            List<QuizSession> last15Sessions = sessionRepository.findTop15ByUserIdAndCompletionIsNotNullOrderByCompletionDesc(userId);

            List<Map<String, Object>> quizScores = last15Sessions.stream()
                    .map(s -> {
                        Map<String, Object> scoreMap = new HashMap<>();
                        scoreMap.put("sessionId", s.getId());
                        scoreMap.put("score", s.getFinalscore());
                        scoreMap.put("date", s.getCompletion());
                        return scoreMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "totalScore", student.getTotalScore() != null ? student.getTotalScore() : 0,
                "scores", quizScores
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /*
    // TESTING ENDPOINT: Uncomment the following method to test the retrieval of the last 15 quiz sessions.
    @GetMapping("/test/history/{userId}")
    public ResponseEntity<?> testGetLast15Sessions(@PathVariable Integer userId) {
        try {
            List<QuizSession> last15Sessions = sessionRepository.findTop15ByUserIdAndCompletionIsNotNullOrderByCompletionDesc(userId);
            return ResponseEntity.ok(last15Sessions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    */

    @PostMapping("/update")
    @Transactional
    public ResponseEntity<?> updateScore(@RequestBody Map<String, Object> data) {
        try {
            Object userIdObj = data.get("user_id") != null ? data.get("user_id") : data.get("userId");
            if (userIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "user_id is required"));
            }
            Integer userId = Integer.valueOf(userIdObj.toString());
            
            Object pointsObj = data.get("points");
            if (pointsObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "points is required"));
            }
            Integer pointsEarned = Integer.valueOf(pointsObj.toString());

            Object totalQuestionsObj = data.get("total_questions");
            Integer totalQuestions = totalQuestionsObj != null ? Integer.valueOf(totalQuestionsObj.toString()) : 0;

            // 1. Update Student record
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new Exception("Student not found for user_id: " + userId));

            student.setTotalScore((student.getTotalScore() != null ? student.getTotalScore() : 0) + pointsEarned);
            student.setLastLogin(LocalDateTime.now());

            // 2. Handle Quiz Session
            Object sessionIdObj = data.get("session_id") != null ? data.get("session_id") : data.get("sessionId");
            if (sessionIdObj != null) {
                Long sessionId = Long.valueOf(sessionIdObj.toString());
                Optional<QuizSession> sessionOpt = sessionRepository.findById(sessionId);
                if (sessionOpt.isPresent()) {
                    QuizSession session = sessionOpt.get();
                    session.setCompletion(LocalDateTime.now());
                    session.setFinalscore(pointsEarned);
                    sessionRepository.save(session);
                }
            }

            // 3. Badge Logic
            String currentBadgesJson = student.getEarnedBadges();
            if (currentBadgesJson == null || currentBadgesJson.trim().isEmpty() || currentBadgesJson.equals("null")) {
                currentBadgesJson = "[]";
            }
            
            JSONArray earnedBadgesArray = new JSONArray(currentBadgesJson);
            List<Integer> earnedIds = new ArrayList<>();
            for (int i = 0; i < earnedBadgesArray.length(); i++) {
                earnedIds.add(earnedBadgesArray.getInt(i));
            }

            List<QuizSession> userSessions = sessionRepository.findAll().stream()
                    .filter(s -> s.getUserId() != null && s.getUserId().equals(userId))
                    .filter(s -> s.getCompletion() != null)
                    .collect(Collectors.toList());

            List<Badge> allBadges = badgeRepository.findAll();
            List<Badge> newlyUnlocked = new ArrayList<>();

            double scorePercent = totalQuestions > 0 ? (pointsEarned * 100.0) / totalQuestions : 0;

            for (Badge badge : allBadges) {
                if (earnedIds.contains(badge.getBadgeId())) continue;

                boolean shouldUnlock = false;
                String reqType = badge.getRequirementType();
                Integer reqVal = badge.getRequirementValue();

                switch (reqType.toUpperCase()) {
                    case "QUIZ_COUNT":
                        if (userSessions.size() >= reqVal) shouldUnlock = true;
                        break;
                    case "SCORE_PERCENT":
                        if (scorePercent >= reqVal) shouldUnlock = true;
                        break;
                    case "POINTS":
                        if (student.getTotalScore() >= reqVal) shouldUnlock = true;
                        break;
                    case "LEVELS_COUNT":
                        if (userSessions.size() >= reqVal) shouldUnlock = true;
                        break;
                    case "STREAK":
                        if (pointsEarned == totalQuestions && totalQuestions > 0) {
                            shouldUnlock = true;
                        }
                        break;
                    case "IMPROVEMENT":
                        if (userSessions.size() >= 2) {
                            QuizSession lastSession = userSessions.get(userSessions.size() - 2);
                            if (pointsEarned > (lastSession.getFinalscore() != null ? lastSession.getFinalscore() : 0)) {
                                shouldUnlock = true;
                            }
                        }
                        break;
                    case "BADGE_COUNT":
                        if (earnedIds.size() >= reqVal) shouldUnlock = true;
                        break;
                }

                if (shouldUnlock) {
                    earnedBadgesArray.put(badge.getBadgeId());
                    newlyUnlocked.add(badge);
                    earnedIds.add(badge.getBadgeId());
                }
            }

            student.setEarnedBadges(earnedBadgesArray.toString());
            studentRepository.save(student);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "totalScore", student.getTotalScore(),
                "newBadges", newlyUnlocked
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
