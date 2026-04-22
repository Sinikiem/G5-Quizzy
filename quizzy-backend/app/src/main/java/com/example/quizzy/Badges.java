package com.example.quizzy;

public class Badges {
    private long badgeId;
    private String name;
    private String description;
    private boolean unlocked;

    public Badges() {
    }

    public Badges(long badgeId, String name, String description, boolean unlocked) {
        this.badgeId = badgeId;
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
    }

    public long getBadgeId() {
        return badgeId;
    }

    public long getId() {
        return badgeId;
    }

    public void setBadgeId(long badgeId) {
        this.badgeId = badgeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}