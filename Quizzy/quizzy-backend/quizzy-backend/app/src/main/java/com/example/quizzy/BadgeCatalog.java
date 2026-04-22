package com.example.quizzy;
import java.util.ArrayList;
import java.util.List;

public class BadgeCatalog {

    public static List<Badges> getAllBadges() {
        List<Badges> badges = new ArrayList<>();

        badges.add(new Badges(1, "First Quiz", "Complete your first quiz."));
        badges.add(new Badges(2, "Perfect Score", "Score 100% on a quiz."));
        badges.add(new Badges(3, "High Achiever", "Score 80% or higher on a quiz."));
        badges.add(new Badges(4, "Quiz Explorer", "Try different quiz levels."));
        badges.add(new Badges(5, "Dedicated Learner", "Complete 5 quizzes."));
        badges.add(new Badges(6, "Math Rookie", "Complete 3 math quizzes."));
        badges.add(new Badges(7, "Math Master", "Complete 10 math quizzes."));
        badges.add(new Badges(8, "On a Roll", "Answer 3 questions correctly in a row."));
        badges.add(new Badges(9, "Getting Better", "Improve your score from a previous quiz."));
        badges.add(new Badges(10, "Badge Collector", "Earn 5 badges."));

        return badges;
    }
}