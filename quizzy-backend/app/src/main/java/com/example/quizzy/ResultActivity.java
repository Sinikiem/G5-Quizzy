package com.example.quizzy;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quizzy.network.NetworkClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private LinearLayout achievementsContainer;
    private SessionManager sessionManager;
    private TextView tvResultTitle;
    private TextView tvFinalScore;
    private TextView tvResultMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        achievementsContainer = findViewById(R.id.achievementsContainer);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        sessionManager = new SessionManager(this);

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        // Display Score and Message
        if (tvFinalScore != null) {
            tvFinalScore.setText(String.format(Locale.getDefault(), "Score: %d / %d", score, totalQuestions));
        }
        
        if (tvResultMessage != null) {
            tvResultMessage.setText(AchievementProcessor.getResultMessage(score, totalQuestions));
        }

        // Sync score and fetch new badges from backend
        syncScoreAndFetchNewBadges(score, totalQuestions);
    }

    private void syncScoreAndFetchNewBadges(int correctAnswers, int totalQuestions) {
        long userId = sessionManager.getUserId();
        long sessionId = QuizRepository.currentSessionId;

        if (userId == -1L) {
            showError("User not logged in.");
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("points", correctAnswers);
            body.put("total_questions", totalQuestions);

            if (sessionId != -1L) {
                body.put("session_id", sessionId);
            }

            new Thread(() -> {
                try {
                    // Sync with backend and get response containing newly earned badges
                    // The backend returns 'newBadges' field
                    JSONObject response = NetworkClient.postSync("/score/update", body);
                    Log.d("SYNC", "Score synced successfully. Response: " + response.toString());
                    
                    List<Badges> newlyEarned = new ArrayList<>();
                    if (response.has("newBadges")) {
                        JSONArray badgesArray = response.getJSONArray("newBadges");
                        for (int i = 0; i < badgesArray.length(); i++) {
                            JSONObject badgeObj = badgesArray.getJSONObject(i);
                            // Backend fields: badgeId, badgeName, description
                            newlyEarned.add(new Badges(
                                    badgeObj.getLong("badgeId"),
                                    badgeObj.getString("badgeName"),
                                    badgeObj.getString("description"),
                                    true
                            ));
                        }
                    }

                    runOnUiThread(() -> {
                        if (newlyEarned.isEmpty()) {
                            tvResultTitle.setText("Quiz Complete!");
                            showNoNewBadges();
                        } else {
                            tvResultTitle.setText("New Achievements! 🎉");
                            displayNewBadges(newlyEarned);
                        }
                    });
                } catch (Exception e) {
                    Log.e("SYNC", "Error: " + e.getMessage());
                    runOnUiThread(() -> showError("Failed to sync: " + e.getMessage()));
                }
            }).start();
        } catch (JSONException e) {
            Log.e("SYNC", "JSON error: " + e.getMessage());
        }
    }

    private void displayNewBadges(List<Badges> badges) {
        achievementsContainer.removeAllViews();

        for (int i = 0; i < badges.size(); i++) {
            Badges badge = badges.get(i);
            View badgeView = createBadgeCard(badge);
            
            // Basic entry animation
            badgeView.setAlpha(0f);
            badgeView.setTranslationY(50f);
            achievementsContainer.addView(badgeView);
            
            badgeView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(i * 200L)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private View createBadgeCard(Badges badge) {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 16, 0, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(32f);
        cardView.setCardElevation(12f);
        cardView.setUseCompatPadding(true);
        
        // Gradient Background for the card
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[] { Color.parseColor("#FFFFFF"), Color.parseColor("#F5F0FF") }
        );
        gradient.setCornerRadius(32f);
        cardView.setBackground(gradient);

        // FrameLayout to allow overlapping "NEW" tag
        FrameLayout rootLayout = new FrameLayout(this);
        
        // Content Layout
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setGravity(Gravity.CENTER_VERTICAL);
        contentLayout.setPadding(40, 48, 40, 48);

        // Badge Icon (Trophy)
        TextView iconView = new TextView(this);
        iconView.setText("🏆");
        iconView.setTextSize(40f);
        iconView.setPadding(0, 0, 32, 0);

        // Text Info
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        
        TextView tvName = new TextView(this);
        tvName.setText(badge.getName());
        tvName.setTextSize(22f);
        tvName.setTextColor(Color.parseColor("#4A148C")); // Deep Purple
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(badge.getDescription());
        tvDesc.setTextSize(16f);
        tvDesc.setTextColor(Color.parseColor("#6A1B9A"));
        tvDesc.setPadding(0, 8, 0, 0);

        textLayout.addView(tvName);
        textLayout.addView(tvDesc);

        contentLayout.addView(iconView);
        contentLayout.addView(textLayout);
        
        rootLayout.addView(contentLayout);

        // "NEW" Mark Circle
        TextView newMark = new TextView(this);
        newMark.setText("NEW");
        newMark.setTextSize(10f);
        newMark.setTextColor(Color.WHITE);
        newMark.setTypeface(null, Typeface.BOLD);
        newMark.setGravity(Gravity.CENTER);
        
        int size = 80; // px
        FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(size, size);
        newParams.gravity = Gravity.TOP | Gravity.END;
        newParams.setMargins(0, 16, 16, 0);
        newMark.setLayoutParams(newParams);
        
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor("#FF4081")); // Pink accent
        newMark.setBackground(circle);

        rootLayout.addView(newMark);
        
        cardView.addView(rootLayout);
        return cardView;
    }

    private void showNoNewBadges() {
        achievementsContainer.removeAllViews();
        TextView message = new TextView(this);
        message.setText("Great effort! You didn't unlock any new badges this time, but keep practicing to reach the next milestone.");
        message.setTextSize(18f);
        message.setTextColor(Color.parseColor("#7B1FA2"));
        message.setGravity(Gravity.CENTER);
        message.setPadding(40, 60, 40, 0);
        message.setLineSpacing(0, 1.2f);
        achievementsContainer.addView(message);
    }

    private void showError(String message) {
        achievementsContainer.removeAllViews();
        TextView errorView = new TextView(this);
        errorView.setText("Oops! " + message);
        errorView.setTextColor(Color.RED);
        errorView.setGravity(Gravity.CENTER);
        errorView.setPadding(0, 50, 0, 0);
        achievementsContainer.addView(errorView);
    }
}
