package com.quizzy.backend.config;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.Guardian;
import com.quizzy.backend.model.User;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.GuardianRepository;
import com.quizzy.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(BadgeRepository badgeRepository, UserRepository userRepository, GuardianRepository guardianRepository) {
        return args -> {
            // Initialize Badges
            if (badgeRepository.count() == 0) {
                List<Badge> badges = List.of(
                    createBadge(1, "First Quiz", "Complete your first quiz.", "QUIZ_COUNT", 1, 1),
                    createBadge(2, "Perfect Score", "Score 100% on a quiz.", "SCORE_PERCENT", 100, 2),
                    createBadge(3, "High Achiever", "Score 80% or higher on a quiz.", "SCORE_PERCENT", 80, 3),
                    createBadge(4, "Quiz Explorer", "Try different quiz levels.", "LEVELS_COUNT", 3, 4),
                    createBadge(5, "Dedicated Learner", "Complete 5 quizzes.", "QUIZ_COUNT", 5, 5),
                    createBadge(6, "Math Rookie", "Complete 3 math quizzes.", "POINTS", 15, 6),
                    createBadge(7, "Math Master", "Complete 10 math quizzes.", "POINTS", 50, 7),
                    createBadge(8, "On a Roll", "Answer 3 questions correctly in a row.", "STREAK", 3, 8),
                    createBadge(9, "Getting Better", "Improve your score from a previous quiz.", "IMPROVEMENT", 1, 9),
                    createBadge(10, "Badge Collector", "Earn 5 badges.", "BADGE_COUNT", 5, 10),
                    createBadge(11, "Point Starter", "Reach 10 total points.", "POINTS", 10, 1),
                    createBadge(12, "Point Pro", "Reach 100 total points.", "POINTS", 100, 2)
                );
                badgeRepository.saveAll(badges);
            }

            // Sync Guardians for all users as requested
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                if (guardianRepository.findByUserId(user.getUserId()).isEmpty()) {
                    Guardian guardian = new Guardian();
                    guardian.setUserId(user.getUserId());
                    guardianRepository.save(guardian);
                }
            }
        };
    }

    private Badge createBadge(Integer id, String name, String desc, String type, Integer value, Integer design) {
        Badge badge = new Badge();
        badge.setBadgeId(id);
        badge.setBadgeName(name);
        badge.setDescription(desc);
        badge.setRequirementType(type);
        badge.setRequirementValue(value);
        badge.setDesignNumber(design);
        return badge;
    }
}
