package com.example.quizzy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AchievementProcessor {

    public static List<AchievementDisplayItem> prepareAchievementsForDisplay(
            List<Badges> allBadges,
            List<Badges> earnedBadges,
            int score,
            int totalQuestions
    ) {
        List<AchievementDisplayItem> displayItems = new ArrayList<>();
        Set<Integer> earnedIds = new HashSet<>();

        if (earnedBadges != null) {
            for (Badges badge : earnedBadges) {
                earnedIds.add(badge.getId());
            }
        }

        for (Badges badge : allBadges) {
            boolean unlocked = earnedIds.contains(badge.getId());
            String statusText = buildStatusText(badge.getId(), unlocked, score, totalQuestions);

            displayItems.add(
                    new AchievementDisplayItem(
                            badge.getId(),
                            badge.getName(),
                            badge.getDescription(),
                            unlocked,
                            statusText
                    )
            );
        }

        return displayItems;
    }

    private static String buildStatusText(int badgeId, boolean unlocked, int score, int totalQuestions) {
        if (unlocked) {
            return "Unlocked";
        }

        switch (badgeId) {
            case 1:
                return "Complete your first quiz to unlock";
            case 2:
                return "Get all " + totalQuestions + " questions correct";
            case 3:
                return "Score at least " + getRequiredHighAchieverScore(totalQuestions) + "/" + totalQuestions;
            case 4:
                return "Try other quiz levels to unlock";
            case 5:
                return "Complete more quizzes to unlock";
            case 6:
                return "Complete more math quizzes to unlock";
            case 7:
                return "Keep playing to become a Math Master";
            case 8:
                return "Answer 3 questions correctly in a row";
            case 9:
                return "Improve your score in a future quiz";
            case 10:
                return "Earn more badges to unlock";
            default:
                return "Locked";
        }
    }

    private static int getRequiredHighAchieverScore(int totalQuestions) {
        return (int) Math.ceil(totalQuestions * 0.8);
    }

    public static String getResultMessage(int score, int totalQuestions) {
        if (score == totalQuestions) {
            return "Amazing! Perfect score!";
        } else if (score >= Math.ceil(totalQuestions * 0.8)) {
            return "Great job! You did very well!";
        } else if (score >= Math.ceil(totalQuestions * 0.5)) {
            return "Nice work! Keep practicing!";
        } else {
            return "Good try! Practice and come back stronger!";
        }
    }
}