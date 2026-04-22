package com.quizzy.backend.controller;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.UserBadge;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.UserBadgeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class BadgeController {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeController(BadgeRepository badgeRepository,
                           UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    // ✅ GET BADGES (returns ONLY unlocked ones)
    @GetMapping("/{userId}/badges")
    public ResponseEntity<?> getUserBadges(@PathVariable Integer userId) {
        try {
            List<UserBadge> unlockedUserBadges =
                    userBadgeRepository.findByUserIdAndUnlockedTrue(userId);

            List<Map<String, Object>> response = new ArrayList<>();

            for (UserBadge userBadge : unlockedUserBadges) {

                Optional<Badge> badgeOpt = badgeRepository.findById(
                        userBadge.getBadgeId()
                );

                if (badgeOpt.isPresent()) {
                    Badge badge = badgeOpt.get();

                    response.add(Map.of(
                            "badgeId", badge.getBadgeId(),
                            "name", badge.getBadgeName(),
                            "description", badge.getDescription(),
                            "unlocked", true
                    ));
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ FIXED UNLOCK ENDPOINT (THIS WAS YOUR MAIN ISSUE)
    @PostMapping("/{userId}/badges/{badgeId}")
    public ResponseEntity<?> unlockBadge(
            @PathVariable Integer userId,
            @PathVariable Integer badgeId
    ) {
        try {
            Optional<UserBadge> existing =
                    userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId);

            if (existing.isPresent()) {
                UserBadge userBadge = existing.get();
                userBadge.setUnlocked(true);
                userBadgeRepository.save(userBadge);
            } else {
                UserBadge newBadge = new UserBadge();
                newBadge.setUserId(userId);
                newBadge.setBadgeId(badgeId);
                newBadge.setUnlocked(true);
                userBadgeRepository.save(newBadge);
            }

            return ResponseEntity.ok(Map.of("status", "unlocked"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}