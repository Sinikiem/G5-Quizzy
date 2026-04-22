package com.example.quizzy;

import java.util.ArrayList;
import java.util.List;

public class QuizRepository {
    public static List<Question> currentQuizQuestions = new ArrayList<>();
    public static List<Badges> getUserBadges(int userId) {

        List<Badges> badges = new ArrayList<>();
        badges.add(new Badges(1, "Beginner", "Completed first quiz"));
        badges.add(new Badges(2, "Intermediate", "Completed 5 quizzes"));
        badges.add(new Badges(3, "Pro", "Scored 100%"));

        return badges;
    }
}
