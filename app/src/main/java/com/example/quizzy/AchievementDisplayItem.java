package com.example.quizzy;

public class AchievementDisplayItem {
    private int id;
    private String name;
    private String description;
    private boolean unlocked;
    private String statusText;

    public AchievementDisplayItem(int id, String name, String description, boolean unlocked, String statusText) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
        this.statusText = statusText;
    }

    public int getId() {
        return id;
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

    public String getStatusText() {
        return statusText;
    }
}