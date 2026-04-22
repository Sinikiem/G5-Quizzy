package com.quizzy.backend.service;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.BadgeResponse;
import com.quizzy.backend.model.UserBadge;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeService(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    public List<BadgeResponse> getBadgesForUser(Long userId) {
        List<Badge> allBadges = badgeRepository.findAll();
        List<BadgeResponse> response = new ArrayList<>();

        for (Badge badge : allBadges) {
            Optional<UserBadge> userBadgeOptional =
                    userBadgeRepository.findByUserIdAndBadge_BadgeId(userId, badge.getBadgeId());

            boolean unlocked = userBadgeOptional.map(UserBadge::isUnlocked).orElse(false);

            response.add(new BadgeResponse(
                    badge.getBadgeId(),
                    badge.getName(),
                    badge.getDescription(),
                    unlocked
            ));
        }

        return response;
    }

    public void unlockBadge(Long userId, Long badgeId) {
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("Badge not found: " + badgeId));

        Optional<UserBadge> existing =
                userBadgeRepository.findByUserIdAndBadge_BadgeId(userId, badgeId);

        if (existing.isPresent()) {
            UserBadge userBadge = existing.get();
            userBadge.setUnlocked(true);
            if (userBadge.getUnlockedAt() == null) {
                userBadge.setUnlockedAt(LocalDateTime.now());
            }
            userBadgeRepository.save(userBadge);
        } else {
            UserBadge userBadge = new UserBadge();
            userBadge.setUserId(userId);
            userBadge.setBadge(badge);
            userBadge.setUnlocked(true);
            userBadge.setUnlockedAt(LocalDateTime.now());
            userBadgeRepository.save(userBadge);
        }
    }
}