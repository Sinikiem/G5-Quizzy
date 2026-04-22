package com.quizzy.backend.controller;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.Student;
import com.quizzy.backend.model.QuizSession;
import com.quizzy.backend.model.UserBadge;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.StudentRepository;
import com.quizzy.backend.repository.QuizSessionRepository;
import com.quizzy.backend.repository.UserBadgeRepository;
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

/**
 * Controller responsible for managing student scores and achievement unlocking logic.
 * Ensures that score updates and badge achievements are synchronized between
 * the 'students' table and the 'user_badges' table.
 */
@RestController
@RequestMapping("/api/score")
@CrossOrigin(origins = "*")
public class ScoreController {

    private final StudentRepository studentRepository;
    private final BadgeRepository badgeRepository;
    private final QuizSessionRepository sessionRepository;
    private final UserBadgeRepository userBadgeRepository;

    public ScoreController(StudentRepository studentRepository, 
                           BadgeRepository badgeRepository,
                           QuizSessionRepository sessionRepository,
                           UserBadgeRepository userBadgeRepository) {
        this.studentRepository = studentRepository;
        this.badgeRepository = badgeRepository;
        this.sessionRepository = sessionRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    /**
     * Retrieves the score data and recent session history for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserScore(@PathVariable Integer userId) {
        try {
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new Exception("Student not found for user_id: " + userId));
            
            List<QuizSession> last15Sessions = sessionRepository.findTop15ByUserIdAndCompletionIsNotNullOrderByCompletionDesc(userId);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
            LocalDateTime startOfWeek = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();
            LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

            List<Map<String, Object>> allScores = last15Sessions.stream()
                    .map(this::mapSessionToMap)
                    .collect(Collectors.toList());

            List<Map<String, Object>> dailyScores = last15Sessions.stream()
                    .filter(s -> s.getCompletion() != null && !s.getCompletion().isBefore(startOfToday))
                    .map(this::mapSessionToMap)
                    .collect(Collectors.toList());

            List<Map<String, Object>> weeklyScores = last15Sessions.stream()
                    .filter(s -> s.getCompletion() != null && !s.getCompletion().isBefore(startOfWeek))
                    .map(this::mapSessionToMap)
                    .collect(Collectors.toList());

            List<Map<String, Object>> monthlyScores = last15Sessions.stream()
                    .filter(s -> s.getCompletion() != null && !s.getCompletion().isBefore(startOfMonth))
                    .map(this::mapSessionToMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "totalScore", student.getTotalScore() != null ? student.getTotalScore() : 0,
                "scores", allScores,
                "dailyScores", dailyScores,
                "weeklyScores", weeklyScores,
                "monthlyScores", monthlyScores
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> mapSessionToMap(QuizSession s) {
        Map<String, Object> scoreMap = new HashMap<>();
        scoreMap.put("sessionId", s.getId());
        scoreMap.put("score", s.getFinalscore());
        scoreMap.put("date", s.getCompletion());
        return scoreMap;
    }

    /**
     * Updates a user's score and evaluates if any new achievements have been earned.
     * Synchronizes badge unlocking across all relevant database tables.
     */
    @PostMapping("/update")
    @Transactional
    public ResponseEntity<?> updateScore(@RequestBody Map<String, Object> data) {
        try {
            Integer userId = getIntFromMap(data, "user_id", "userId");
            if (userId == null) return ResponseEntity.badRequest().body(Map.of("error", "user_id is required"));
            
            Integer pointsEarned = getIntFromMap(data, "points", null);
            if (pointsEarned == null) return ResponseEntity.badRequest().body(Map.of("error", "points is required"));

            Integer totalQuestions = getIntFromMap(data, "total_questions", null);
            if (totalQuestions == null) totalQuestions = 0;

            // 1. Update Student record
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new Exception("Student not found for user_id: " + userId));

            student.setTotalScore((student.getTotalScore() != null ? student.getTotalScore() : 0) + pointsEarned);
            student.setLastLogin(LocalDateTime.now());

            // 2. Handle Quiz Session
            updateQuizSession(data, pointsEarned);

            // 3. Badge Logic (Single Source of Truth synchronization)
            List<Badge> newlyUnlocked = processAchievements(student, pointsEarned, totalQuestions, userId);

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

    /**
     * Algorithm: Evaluates all available badges against the user's current performance and history.
     * Unlocked badges are saved both in the Student's JSON field and the UserBadge join table.
     */
    private List<Badge> processAchievements(Student student, int pointsEarned, int totalQuestions, Integer userId) {
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

            if (checkBadgeRequirement(badge, student, pointsEarned, totalQuestions, scorePercent, userSessions, earnedIds)) {
                // Add to newly unlocked list for response
                newlyUnlocked.add(badge);

                // Persist in Student JSON column
                earnedBadgesArray.put(badge.getBadgeId());
                earnedIds.add(badge.getBadgeId());

                // Persist in user_badges join table (Consistency Fix)
                persistUserBadge(userId, badge.getBadgeId());
            }
        }

        student.setEarnedBadges(earnedBadgesArray.toString());
        return newlyUnlocked;
    }

    private boolean checkBadgeRequirement(Badge badge, Student student, int points, int total, double percent, List<QuizSession> sessions, List<Integer> earnedIds) {
        String type = badge.getRequirementType().toUpperCase();
        Integer val = badge.getRequirementValue();

        switch (type) {
            case "QUIZ_COUNT": return sessions.size() >= val;
            case "SCORE_PERCENT": return percent >= val;
            case "POINTS": return student.getTotalScore() >= val;
            case "LEVELS_COUNT": return sessions.size() >= val;
            case "STREAK": return points == total && total > 0;
            case "IMPROVEMENT":
                if (sessions.size() < 2) return false;
                QuizSession last = sessions.get(sessions.size() - 2);
                return points > (last.getFinalscore() != null ? last.getFinalscore() : 0);
            case "BADGE_COUNT": return earnedIds.size() >= val;
            default: return false;
        }
    }

    /**
     * Ensures the UserBadge join table is updated whenever a badge is unlocked via score synchronization.
     */
    private void persistUserBadge(Integer userId, Integer badgeId) {
        Optional<UserBadge> existing = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId);
        if (existing.isEmpty()) {
            UserBadge ub = new UserBadge();
            ub.setUserId(userId);
            ub.setBadgeId(badgeId);
            ub.setUnlocked(true);
            ub.setUnlockedAt(LocalDateTime.now());
            userBadgeRepository.save(ub);
        }
    }

    private void updateQuizSession(Map<String, Object> data, Integer points) {
        Object sessionIdObj = data.get("session_id") != null ? data.get("session_id") : data.get("sessionId");
        if (sessionIdObj != null) {
            Long sessionId = Long.valueOf(sessionIdObj.toString());
            sessionRepository.findById(sessionId).ifPresent(session -> {
                session.setCompletion(LocalDateTime.now());
                session.setFinalscore(points);
                sessionRepository.save(session);
            });
        }
    }

    private Integer getIntFromMap(Map<String, Object> map, String key1, String key2) {
        Object val = map.get(key1);
        if (val == null && key2 != null) val = map.get(key2);
        return val != null ? Integer.valueOf(val.toString()) : null;
    }
}
