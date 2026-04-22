package com.example.quizzy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizRepository {

    public static List<Question> currentQuizQuestions = new ArrayList<>();
    public static long currentSessionId = -1L;

    public interface BadgeCallback {
        void onSuccess(List<Badges> badges);
        void onError(String error);
    }

    public static void getUserBadges(int userId, BadgeCallback callback) {
        BadgeApiService apiService = RetrofitClient.getInstance().create(BadgeApiService.class);

        apiService.getUserBadges(userId).enqueue(new Callback<List<Badges>>() {
            @Override
            public void onResponse(Call<List<Badges>> call, Response<List<Badges>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load badges. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Badges>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Unknown error");
            }
        });
    }
}
