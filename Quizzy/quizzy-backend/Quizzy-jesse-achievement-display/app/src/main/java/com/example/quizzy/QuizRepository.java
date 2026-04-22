package com.example.quizzy;

import java.util.ArrayList;
import java.util.List;

public class QuizRepository {

    public static List<Question> currentQuizQuestions = new ArrayList<>();

    public static List<Badges> getUserBadges(int userId) {
        List<Badges> badges = new ArrayList<>();

        badges.add(new Badges(1, "First Quiz", "Complete your first quiz", true));
        badges.add(new Badges(2, "Perfect Score", "Score 100% on a quiz", false));
        badges.add(new Badges(3, "High Achiever", "Score 80% or higher", true));
        badges.add(new Badges(4, "Quiz Explorer", "Try different quiz levels", false));
        badges.add(new Badges(5, "Dedicated Learner", "Complete 10 quizzes", false));
        badges.add(new Badges(6, "Math Rookie", "Complete 3 math quizzes", true));
        badges.add(new Badges(7, "Math Master", "Complete 10 math quizzes", false));
        badges.add(new Badges(8, "On a Roll", "Answer 3 questions correctly in a row", false));
        badges.add(new Badges(9, "Getting Better", "Improve your score", true));
        badges.add(new Badges(10, "Badge Collector", "Collect 5 badges", false));

        return badges;
    }
}