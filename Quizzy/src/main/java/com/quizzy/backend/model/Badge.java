package com.quizzy.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "badges")
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Integer badgeId;

    @Column(name = "badge_name", length = 100)
    private String badgeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirement_type", length = 50)
    private String requirementType;

    @Column(name = "requirement_value")
    private Integer requirementValue;

    @Column(name = "design_number")
    private Integer designNumber;

    public Badge() {}

    public Integer getBadgeId() { return badgeId; }
    public void setBadgeId(Integer badgeId) { this.badgeId = badgeId; }

    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirementType() { return requirementType; }
    public void setRequirementType(String requirementType) { this.requirementType = requirementType; }

    public Integer getRequirementValue() { return requirementValue; }
    public void setRequirementValue(Integer requirementValue) { this.requirementValue = requirementValue; }

    public Integer getDesignNumber() { return designNumber; }
    public void setDesignNumber(Integer designNumber) { this.designNumber = designNumber; }
}
