package com.example.quizzy;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BadgeManager {
    private static final String PREFS_NAME = "QuizzyBadges";
    private static final String KEY_EARNED_BADGES = "earned_badge_ids";
    private static final String KEY_TOTAL_QUIZZES = "total_quizzes_completed";
    private static final String KEY_MATH_QUIZZES = "math_quizzes_completed";
    private static final String KEY_LEVELS_PLAYED = "levels_played";

    public static List<Badges> checkAndUnlockBadges(Context context, int score, int totalQuestions, int gradeLevel) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> earnedIds = new HashSet<>(prefs.getStringSet(KEY_EARNED_BADGES, new HashSet<>()));
        int totalQuizzes = prefs.getInt(KEY_TOTAL_QUIZZES, 0) + 1;
        int mathQuizzes = prefs.getInt(KEY_MATH_QUIZZES, 0);
        // Assuming Grade 3, 4, 5 might be math related for this example
        mathQuizzes++; 

        Set<String> levelsPlayed = new HashSet<>(prefs.getStringSet(KEY_LEVELS_PLAYED, new HashSet<>()));
        levelsPlayed.add(String.valueOf(gradeLevel));

        List<Badges> newBadges = new ArrayList<>();
        List<Badges> allBadges = BadgeCatalog.getAllBadges();

        for (Badges badge : allBadges) {
            if (earnedIds.contains(String.valueOf(badge.getId()))) continue;

            boolean shouldUnlock = false;
            switch (badge.getId()) {
                case 1: // First Quiz
                    shouldUnlock = true;
                    break;
                case 2: // Perfect Score
                    if (score == totalQuestions && totalQuestions > 0) shouldUnlock = true;
                    break;
                case 3: // High Achiever
                    if (score >= Math.ceil(totalQuestions * 0.8)) shouldUnlock = true;
                    break;
                case 4: // Quiz Explorer
                    if (levelsPlayed.size() >= 3) shouldUnlock = true;
                    break;
                case 5: // Dedicated Learner
                    if (totalQuizzes >= 5) shouldUnlock = true;
                    break;
                case 6: // Math Rookie
                    if (mathQuizzes >= 3) shouldUnlock = true;
                    break;
                case 7: // Math Master
                    if (mathQuizzes >= 10) shouldUnlock = true;
                    break;
                case 10: // Badge Collector
                    if (earnedIds.size() + newBadges.size() >= 5) shouldUnlock = true;
                    break;
                // Note: Cases 8 (streak) and 9 (improvement) would need more complex tracking
            }

            if (shouldUnlock) {
                earnedIds.add(String.valueOf(badge.getId()));
                newBadges.add(badge);
            }
        }

        // Save progress
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_EARNED_BADGES, earnedIds);
        editor.putInt(KEY_TOTAL_QUIZZES, totalQuizzes);
        editor.putInt(KEY_MATH_QUIZZES, mathQuizzes);
        editor.putStringSet(KEY_LEVELS_PLAYED, levelsPlayed);
        editor.apply();

        return newBadges;
    }

    public static List<Badges> getEarnedBadges(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> earnedIds = prefs.getStringSet(KEY_EARNED_BADGES, new HashSet<>());
        List<Badges> allBadges = BadgeCatalog.getAllBadges();
        List<Badges> earnedBadges = new ArrayList<>();

        for (Badges badge : allBadges) {
            if (earnedIds.contains(String.valueOf(badge.getId()))) {
                earnedBadges.add(badge);
            }
        }
        return earnedBadges;
    }
}
