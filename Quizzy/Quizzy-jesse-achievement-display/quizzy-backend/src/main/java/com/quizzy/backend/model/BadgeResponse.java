package com.quizzy.backend.model;

public class BadgeResponse {

    private Long badgeId;
    private String name;
    private String description;
    private boolean unlocked;

    public BadgeResponse(Long badgeId, String name, String description, boolean unlocked) {
        this.badgeId = badgeId;
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
    }

    public Long getBadgeId() {
        return badgeId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }
}