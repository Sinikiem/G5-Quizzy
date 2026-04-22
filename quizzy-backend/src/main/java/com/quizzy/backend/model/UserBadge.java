package com.quizzy.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges")
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_badge_id")
    private Long userBadgeId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "badge_id")
    private Integer badgeId;

    @Column(name = "unlocked")
    private Boolean unlocked;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    public UserBadge() {
    }

    public Long getUserBadgeId() {
        return userBadgeId;
    }

    public void setUserBadgeId(Long userBadgeId) {
        this.userBadgeId = userBadgeId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Integer badgeId) {
        this.badgeId = badgeId;
    }

    public Boolean getUnlocked() {
        return unlocked;
    }

    public void setUnlocked(Boolean unlocked) {
        this.unlocked = unlocked;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
}