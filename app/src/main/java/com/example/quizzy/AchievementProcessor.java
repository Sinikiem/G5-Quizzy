package com.example.quizzy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for processing and formatting achievement-related data for the UI.
 * This class handles the creation of display models for badges and generates 
 * user-friendly feedback messages based on quiz performance.
 */
public class AchievementProcessor {

    /**
     * Algorithm: Transforms raw badge data into a list of items optimized for UI display.
     * 
     * Main steps:
     * 1. Create a Set of earned badge IDs for O(1) membership checking.
     * 2. Iterate through all possible badges.
     * 3. Determine unlock status and build descriptive status text for each.
     * 4. Wrap the data into [AchievementDisplayItem] objects.
     */
    public static List<AchievementDisplayItem> prepareAchievementsForDisplay(
            List<Badges> allBadges,
            List<Badges> earnedBadges,
            int score,
            int totalQuestions
    ) {
        List<AchievementDisplayItem> displayItems = new ArrayList<>();
        Set<Long> earnedIds = new HashSet<>();

        if (earnedBadges != null) {
            for (Badges badge : earnedBadges) {
                earnedIds.add(badge.getId());
            }
        }

        if (allBadges != null) {
            for (Badges badge : allBadges) {
                boolean unlocked = earnedIds.contains(badge.getId());
                String statusText = buildStatusText(badge.getId(), unlocked, score, totalQuestions);

                displayItems.add(
                        new AchievementDisplayItem(
                                (int) badge.getId(),
                                badge.getName(),
                                badge.getDescription(),
                                unlocked,
                                statusText
                        )
                );
            }
        }

        return displayItems;
    }

    /**
     * Generates an encouraging performance message based on the quiz score percentage.
     * 
     * @param score          The number of correct answers.
     * @param totalQuestions The total number of questions in the quiz.
     * @return A string containing a human-readable feedback message.
     */
    public static String getResultMessage(int score, int totalQuestions) {
        if (totalQuestions <= 0) {
            return "Good try!";
        }

        double percent = (score * 100.0) / totalQuestions;

        // Message Logic: Threshold-based feedback
        if (percent == 100.0) {
            return "Amazing! Perfect score!";
        } else if (percent >= 80.0) {
            return "Great job! You did really well!";
        } else if (percent >= 50.0) {
            return "Nice work! Keep practicing!";
        } else {
            return "Don’t give up! Try again and improve!";
        }
    }

    /**
     * Helper to build a localized status description for a badge.
     * For locked badges, it provides a hint on how to unlock it.
     */
    private static String buildStatusText(long badgeId, boolean unlocked, int score, int totalQuestions) {
        if (unlocked) {
            return "Unlocked";
        }

        // Logic: Mapping Badge IDs to unlock requirement hints
        switch ((int) badgeId) {
            case 1:
                return "Complete your first quiz";
            case 2:
                return "Score 100% on a quiz";
            case 3:
                return "Score 80% or higher";
            case 4:
                return "Try different quiz levels";
            case 5:
                return "Complete 5 quizzes";
            case 6:
                return "Complete 3 math quizzes";
            case 7:
                return "Complete 10 math quizzes";
            case 8:
                return "Answer 3 questions in a row";
            case 9:
                return "Improve your score";
            case 10:
                return "Earn 5 badges";
            case 11:
                return "Reach 10 total points";
            case 12:
                return "Reach 100 total points";
            default:
                return "Locked";
        }
    }
}
