package com.example.quizzy;

public class Badges {

    private int id;
    private String name;
    private String description;
    private boolean unlocked;

    public Badges(int id, String name, String description, boolean unlocked) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
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
}