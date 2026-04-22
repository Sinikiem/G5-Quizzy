package com.example.quizzy;

/**
 * Data model representing an achievement badge in the system.
 * It contains details about the badge's identity, display information, 
 * and its current lock/unlock status for a user.
 */
public class Badges {
    private long badgeId;
    private String name;
    private String description;
    private boolean unlocked;

    /**
     * Default constructor for serialization purposes.
     */
    public Badges() {
    }

    /**
     * Constructs a new Badge with the specified details.
     * 
     * @param badgeId Unique identifier for the badge.
     * @param name Display name of the achievement.
     * @param description A short explanation of how to earn the badge.
     * @param unlocked Whether the current user has earned this badge.
     */
    public Badges(long badgeId, String name, String description, boolean unlocked) {
        this.badgeId = badgeId;
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
    }

    public long getBadgeId() {
        return badgeId;
    }

    /**
     * Alias for getBadgeId to support legacy or alternative naming conventions.
     * @return The badge unique identifier.
     */
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
