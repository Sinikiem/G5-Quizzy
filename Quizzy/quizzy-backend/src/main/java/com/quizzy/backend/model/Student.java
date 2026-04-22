package com.quizzy.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(length = 100)
    private String avatar;

    @Column(name = "total_score")
    private Integer totalScore = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "badge_id")
    private Integer badgeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "earned_badges", columnDefinition = "jsonb")
    private String earnedBadges = "[]";

    public Student() {}

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Integer getBadgeId() { return badgeId; }
    public void setBadgeId(Integer badgeId) { this.badgeId = badgeId; }

    public String getEarnedBadges() { return earnedBadges; }
    public void setEarnedBadges(String earnedBadges) { this.earnedBadges = earnedBadges; }
}
