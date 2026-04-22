package com.example.quizzy;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit interface defining the API endpoints for user achievements.
 * It provides methods for retrieving a user's earned badges and unlocking new ones.
 */
public interface BadgeApiService {

    /**
     * Fetches the list of achievement badges earned by a specific user.
     * 
     * @param userId The unique identifier of the user.
     * @return A [Call] object for the asynchronous retrieval of earned badges.
     */
    @GET("api/users/{userId}/badges")
    Call<List<Badges>> getUserBadges(@Path("userId") int userId);

    /**
     * Records the unlocking of a new achievement badge for a specific user.
     * 
     * @param userId  The unique identifier of the user.
     * @param badgeId The unique identifier of the badge to unlock.
     * @return A [Call] object for the asynchronous POST request.
     */
    @POST("api/users/{userId}/badges/{badgeId}")
    Call<Void> unlockBadge(
            @Path("userId") int userId,
            @Path("badgeId") int badgeId
    );
}
