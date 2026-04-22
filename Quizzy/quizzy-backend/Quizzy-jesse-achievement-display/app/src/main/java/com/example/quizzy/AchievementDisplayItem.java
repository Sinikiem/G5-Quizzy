package com.example.quizzy;
public class AchievementDisplayItem {

    private final int id;
    private final String title;
    private final String description;
    private final boolean unlocked;
    private final String statusText;

    public AchievementDisplayItem(int id, String title, String description, boolean unlocked, String statusText) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.unlocked = unlocked;
        this.statusText = statusText;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public String getStatusText() {
        return statusText;
    }
}