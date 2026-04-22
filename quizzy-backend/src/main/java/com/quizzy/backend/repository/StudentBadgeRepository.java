package com.quizzy.backend.repository;

import com.quizzy.backend.model.StudentBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentBadgeRepository extends JpaRepository<StudentBadge, Long> {
    Optional<StudentBadge> findByStudentIdAndBadgeId(Integer studentId, Integer badgeId);
    List<StudentBadge> findByStudentId(Integer studentId);
}
