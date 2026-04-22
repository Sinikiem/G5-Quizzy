package com.example.quizzy;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility class that manages the logic for achievement badges.
 * This includes filtering badges by status, merging local and remote badge states,
 * and triggering badge unlocks based on quiz performance.
 */
public class BadgeManager {

    private static final String TAG = "BadgeManager";

    /**
     * Filters a list of badges to return only those that are unlocked.
     * 
     * @param allBadges The source list of badges.
     * @return A new list containing only unlocked badges.
     */
    public static List<Badges> getUnlockedBadges(List<Badges> allBadges) {
        List<Badges> unlockedBadges = new ArrayList<>();

        if (allBadges == null) {
            return unlockedBadges;
        }

        for (Badges badge : allBadges) {
            if (badge.isUnlocked()) {
                unlockedBadges.add(badge);
            }
        }

        return unlockedBadges;
    }

    /**
     * Filters a list of badges to return only those that are currently locked.
     * 
     * @param allBadges The source list of badges.
     * @return A new list containing only locked badges.
     */
    public static List<Badges> getLockedBadges(List<Badges> allBadges) {
        List<Badges> lockedBadges = new ArrayList<>();

        if (allBadges == null) {
            return lockedBadges;
        }

        for (Badges badge : allBadges) {
            if (!badge.isUnlocked()) {
                lockedBadges.add(badge);
            }
        }

        return lockedBadges;
    }

    /**
     * Algorithm: Merges the global badge catalog with a user's specific earned badges.
     * This creates a full list where each badge correctly reflects its unlock status for the user.
     * 
     * Main steps:
     * 1. Collect all earned badge IDs into a Set for O(1) lookup.
     * 2. Iterate through the full catalog.
     * 3. Create new Badge objects with the 'unlocked' state determined by the Set.
     */
    public static List<Badges> mergeBadgeStates(List<Badges> allBadges, List<Badges> earnedBadges) {
        List<Badges> merged = new ArrayList<>();

        if (allBadges == null) {
            return merged;
        }

        Set<Long> earnedIds = new HashSet<>();
        if (earnedBadges != null) {
            for (Badges badge : earnedBadges) {
                earnedIds.add(badge.getId());
            }
        }

        for (Badges badge : allBadges) {
            boolean unlocked = earnedIds.contains(badge.getId());

            merged.add(new Badges(
                    badge.getBadgeId(),
                    badge.getName(),
                    badge.getDescription(),
                    unlocked
            ));
        }

        return merged;
    }

    /**
     * Checks if a specific badge exists and is unlocked in the provided list.
     */
    public static boolean hasBadge(List<Badges> badges, long badgeId) {
        if (badges == null) {
            return false;
        }

        for (Badges badge : badges) {
            if (badge.getId() == badgeId) {
                return true;
            }
        }

        return false;
    }

    /**
     * Counts the total number of unlocked badges in a list.
     */
    public static int countUnlockedBadges(List<Badges> badges) {
        if (badges == null) {
            return 0;
        }

        int count = 0;
        for (Badges badge : badges) {
            if (badge.isUnlocked()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Algorithm: Evaluates quiz performance and unlocks appropriate achievement badges.
     * 
     * Logic:
     * - Completing any quiz unlocks the "First Quiz" badge.
     * - 100% score unlocks "Perfect Score".
     * - >= 80% score unlocks "High Achiever".
     * - Level specific or score thresholds trigger additional badges.
     */
    public static void checkAndUnlockBadges(Context context, int score, int totalQuestions, int gradeLevel) {
        SessionManager sessionManager = new SessionManager(context);
        int userId = (int) sessionManager.getUserId();

        if (userId == -1) {
            Log.e(TAG, "Cannot unlock badges: user not logged in");
            return;
        }

        // Rule: Participation
        if (totalQuestions > 0) {
            unlockBadge(context, userId, 1); // First Quiz
        }

        // Rule: Accuracy
        if (totalQuestions > 0 && score == totalQuestions) {
            unlockBadge(context, userId, 2); // Perfect Score
        }

        // Rule: Performance Percentage
        if (totalQuestions > 0) {
            double percent = (score * 100.0) / totalQuestions;
            if (percent >= 80.0) {
                unlockBadge(context, userId, 3); // High Achiever
            }
        }

        // Rule: Grade Progression
        if (gradeLevel >= 4) {
            unlockBadge(context, userId, 4); // Quiz Explorer
        }

        // Rule: Raw Score Thresholds
        if (score >= 3) {
            unlockBadge(context, userId, 6); // Math Rookie
            unlockBadge(context, userId, 8); // On a Roll
        }

        if (score >= 1) {
            unlockBadge(context, userId, 9); // Getting Better
        }
    }

    /**
     * Returns a list of earned badges. 
     * Note: Current implementation is a placeholder.
     */
    public static List<Badges> getEarnedBadges(Context context) {
        return new ArrayList<>();
    }

    /**
     * Asynchronous Network Call: Informs the backend to unlock a specific badge for a user.
     */
    private static void unlockBadge(Context context, int userId, int badgeId) {
        BadgeApiService apiService = RetrofitClient
                .getInstance()
                .create(BadgeApiService.class);

        Call<Void> call = apiService.unlockBadge(userId, badgeId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Badge " + badgeId + " unlocked successfully for user " + userId);
                } else {
                    Log.e(TAG, "Failed to unlock badge " + badgeId + ". Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error while unlocking badge " + badgeId + ": " + t.getMessage());
            }
        });
    }
}
