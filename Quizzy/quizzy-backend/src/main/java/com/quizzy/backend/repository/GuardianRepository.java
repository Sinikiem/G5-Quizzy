package com.quizzy.backend.repository;

import com.quizzy.backend.model.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuardianRepository extends JpaRepository<Guardian, Integer> {
    Optional<Guardian> findByUserId(Integer userId);
}
