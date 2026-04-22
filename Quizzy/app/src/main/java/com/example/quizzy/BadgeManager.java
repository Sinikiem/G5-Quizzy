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

public class BadgeManager {

    private static final String TAG = "BadgeManager";

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

    public static void checkAndUnlockBadges(Context context, int score, int totalQuestions, int gradeLevel) {
        int userId = 1;

        if (totalQuestions > 0) {
            unlockBadge(context, userId, 1); // First Quiz
        }

        if (totalQuestions > 0 && score == totalQuestions) {
            unlockBadge(context, userId, 2); // Perfect Score
        }

        if (totalQuestions > 0) {
            double percent = (score * 100.0) / totalQuestions;
            if (percent >= 80.0) {
                unlockBadge(context, userId, 3); // High Achiever
            }
        }

        if (gradeLevel >= 4) {
            unlockBadge(context, userId, 4); // Quiz Explorer
        }

        if (score >= 3) {
            unlockBadge(context, userId, 6); // Math Rookie
            unlockBadge(context, userId, 8); // On a Roll
        }

        if (score >= 1) {
            unlockBadge(context, userId, 9); // Getting Better
        }
    }

    public static List<Badges> getEarnedBadges(Context context) {
        return new ArrayList<>();
    }

    private static void unlockBadge(Context context, int userId, int badgeId) {
        BadgeApiService apiService = RetrofitClient
                .getInstance()
                .create(BadgeApiService.class);

        Call<Void> call = apiService.unlockBadge(userId, badgeId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Badge unlocked successfully: " + badgeId);
                } else {
                    Log.e(TAG, "Failed to unlock badge " + badgeId + ". Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error unlocking badge " + badgeId + ": " + t.getMessage());
            }
        });
    }
}