package com.example.quizzy;

import java.util.ArrayList;
import java.util.List;

/**
 * Static catalog defining all achievement badges available in the application.
 * This serves as the single source of truth for badge metadata such as names and descriptions.
 */
public class BadgeCatalog {

    /**
     * Retrieves the complete list of available badges in their default (locked) state.
     * 
     * @return A list of [Badges] representing the entire achievement system.
     */
    public static List<Badges> getAllBadges() {
        List<Badges> badges = new ArrayList<>();

        // Standard Participation & Performance Badges
        badges.add(new Badges(1, "First Quiz", "Complete your first quiz.", false));
        badges.add(new Badges(2, "Perfect Score", "Score 100% on a quiz.", false));
        badges.add(new Badges(3, "High Achiever", "Score 80% or higher on a quiz.", false));
        badges.add(new Badges(4, "Quiz Explorer", "Try different quiz levels.", false));
        badges.add(new Badges(5, "Dedicated Learner", "Complete 10 quizzes.", false));

        // Math-Specific Milestones
        badges.add(new Badges(6, "Math Rookie", "Complete 3 math quizzes.", false));
        badges.add(new Badges(7, "Math Master", "Complete 10 math quizzes.", false));

        // Progression & Streak Badges
        badges.add(new Badges(8, "On a Roll", "Answer 3 questions correctly in a row.", false));
        badges.add(new Badges(9, "Getting Better", "Improve your score.", false));
        badges.add(new Badges(10, "Badge Collector", "Collect 5 badges.", false));

        return badges;
    }
}
