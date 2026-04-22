package com.quizzy.backend.model;

public class StudyPlanRequest {
    private String topic;
    private double accuracy;
    private int gradeLevel;

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public int getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(int gradeLevel) { this.gradeLevel = gradeLevel; }
}