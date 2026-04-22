package com.quizzy.backend.controller;

import com.quizzy.backend.model.BadgeResponse;
import com.quizzy.backend.service.BadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping("/{userId}/badges")
    public ResponseEntity<List<BadgeResponse>> getUserBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(badgeService.getBadgesForUser(userId));
    }

    @PostMapping("/{userId}/badges/{badgeId}")
    public ResponseEntity<Void> unlockBadge(
            @PathVariable Long userId,
            @PathVariable Long badgeId
    ) {
        badgeService.unlockBadge(userId, badgeId);
        return ResponseEntity.ok().build();
    }
}