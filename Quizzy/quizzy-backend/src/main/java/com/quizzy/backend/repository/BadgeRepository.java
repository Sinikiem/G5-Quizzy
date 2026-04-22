package com.quizzy.backend.repository;

import com.quizzy.backend.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Integer> {
    List<Badge> findByRequirementType(String requirementType);
}
