package com.quizzy.backend.repository;

import com.quizzy.backend.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserIdAndUnlockedTrue(Integer userId);

    // ✅ REQUIRED FOR UNLOCK FIX
    Optional<UserBadge> findByUserIdAndBadgeId(Integer userId, Integer badgeId);
}