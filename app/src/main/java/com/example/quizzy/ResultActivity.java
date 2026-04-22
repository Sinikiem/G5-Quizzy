package com.example.quizzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
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

    private static final String PREFS_NAME = "quizzy_progress";
    private static final String KEY_TOTAL_SCORE = "total_score";

    private static final String DARK_MODE_BACKGROUND = "#121212";
    private static final String COLOR_BADGE_ACCENT = "#FF4081";

    private LinearLayout achievementsContainer;
    private SessionManager sessionManager;
    private TextView tvResultTitle;
    private TextView tvFinalScore;
    private TextView tvResultMessage;
    private Button btnBackToDashboard;
    private Button btnStudyPlan;
    private View resultRoot;
    private boolean isDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initializeComponents();

        sessionManager = new SessionManager(this);
        isDarkMode = sessionManager.isDarkMode();

        applyTheme();

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        saveTotalScore(score);
        displayScores(score, totalQuestions);

        if (btnBackToDashboard != null) {
            btnBackToDashboard.setOnClickListener(v -> navigateToHome());
        }

        if (btnStudyPlan != null) {
            btnStudyPlan.setOnClickListener(v -> {
                int gradeLevel = getIntent().getIntExtra("gradeLevel", 3);
                double accuracy = getIntent().getDoubleExtra("accuracy", 0.5);
                String topic = gradeLevel == 3 ? "Addition & Subtraction" :
                        gradeLevel == 4 ? "Multiplication & Division" :
                                "Fractions & Decimals";
                Intent intent = new Intent(ResultActivity.this, StudyPlanActivity.class);
                intent.putExtra("topic", topic);
                intent.putExtra("accuracy", accuracy);
                intent.putExtra("gradeLevel", gradeLevel);
                startActivity(intent);
            });
        }

        syncScoreAndFetchNewBadges(score, totalQuestions);
    }

    private void initializeComponents() {
        resultRoot = findViewById(R.id.resultRoot);
        achievementsContainer = findViewById(R.id.achievementsContainer);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);
        btnStudyPlan = findViewById(R.id.btnStudyPlan);
    }

    private void applyTheme() {
        if (isDarkMode) {
            if (resultRoot != null) {
                resultRoot.setBackgroundColor(Color.parseColor(DARK_MODE_BACKGROUND));
            }
            if (tvResultTitle != null) tvResultTitle.setTextColor(Color.WHITE);
            if (tvFinalScore != null) tvFinalScore.setTextColor(Color.WHITE);
            if (tvResultMessage != null) tvResultMessage.setTextColor(Color.LTGRAY);
        }
    }

    private void displayScores(int score, int totalQuestions) {
        if (tvFinalScore != null) {
            String scoreText = String.format(Locale.getDefault(), "Score: %d / %d", score, totalQuestions);
            tvFinalScore.setText(scoreText);
        }

        if (tvResultMessage != null) {
            tvResultMessage.setText(AchievementProcessor.getResultMessage(score, totalQuestions));
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
        intent.putExtra("start_screen", "Home");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void saveTotalScore(int latestQuizScore) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentTotal = prefs.getInt(KEY_TOTAL_SCORE, 0);
        prefs.edit()
                .putInt(KEY_TOTAL_SCORE, currentTotal + latestQuizScore)
                .apply();
    }

    private void syncScoreAndFetchNewBadges(int correctAnswers, int totalQuestions) {
        long userId = sessionManager.getUserId();
        long sessionId = getIntent().getLongExtra("sessionId", -1L);

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
                    JSONObject response = NetworkClient.postSync("/score/update", body);
                    List<Badges> newlyEarned = parseNewBadges(response);

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
                    Log.e("SYNC", "Error syncing score", e);
                    runOnUiThread(() -> showError("Failed to sync progress."));
                }
            }).start();
        } catch (JSONException e) {
            Log.e("SYNC", "JSON preparation failed", e);
        }
    }

    private List<Badges> parseNewBadges(JSONObject response) throws JSONException {
        List<Badges> newlyEarned = new ArrayList<>();
        if (response.has("newBadges")) {
            JSONArray badgesArray = response.getJSONArray("newBadges");
            for (int i = 0; i < badgesArray.length(); i++) {
                JSONObject badgeObj = badgesArray.getJSONObject(i);
                newlyEarned.add(new Badges(
                        badgeObj.getLong("badgeId"),
                        badgeObj.getString("badgeName"),
                        badgeObj.getString("description"),
                        true
                ));
            }
        }
        return newlyEarned;
    }

    private void displayNewBadges(List<Badges> badges) {
        achievementsContainer.removeAllViews();

        for (int i = 0; i < badges.size(); i++) {
            Badges badge = badges.get(i);
            View badgeView = createBadgeCard(badge);

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

        GradientDrawable gradient = getBadgeCardGradient();
        gradient.setCornerRadius(32f);
        cardView.setBackground(gradient);

        FrameLayout rootLayout = new FrameLayout(this);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setGravity(Gravity.CENTER_VERTICAL);
        contentLayout.setPadding(40, 48, 40, 48);

        TextView iconView = new TextView(this);
        iconView.setText("🏆");
        iconView.setTextSize(40f);
        iconView.setPadding(0, 0, 32, 0);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(this);
        tvName.setText(badge.getName());
        tvName.setTextSize(22f);
        tvName.setTextColor(isDarkMode ? Color.parseColor("#D1C4E9") : Color.parseColor("#4A148C"));
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(badge.getDescription());
        tvDesc.setTextSize(16f);
        tvDesc.setTextColor(isDarkMode ? Color.LTGRAY : Color.parseColor("#6A1B9A"));
        tvDesc.setPadding(0, 8, 0, 0);

        textLayout.addView(tvName);
        textLayout.addView(tvDesc);

        contentLayout.addView(iconView);
        contentLayout.addView(textLayout);
        rootLayout.addView(contentLayout);
        rootLayout.addView(createNewTag());

        cardView.addView(rootLayout);
        return cardView;
    }

    private GradientDrawable getBadgeCardGradient() {
        if (isDarkMode) {
            return new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{Color.parseColor("#2C2C2C"), Color.parseColor("#1E1E1E")}
            );
        } else {
            return new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{Color.parseColor("#FFFFFF"), Color.parseColor("#F5F0FF")}
            );
        }
    }

    private View createNewTag() {
        TextView newMark = new TextView(this);
        newMark.setText("NEW");
        newMark.setTextSize(10f);
        newMark.setTextColor(Color.WHITE);
        newMark.setTypeface(null, Typeface.BOLD);
        newMark.setGravity(Gravity.CENTER);

        int size = 80;
        FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(size, size);
        newParams.gravity = Gravity.TOP | Gravity.END;
        newParams.setMargins(0, 16, 16, 0);
        newMark.setLayoutParams(newParams);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor(COLOR_BADGE_ACCENT));
        newMark.setBackground(circle);

        return newMark;
    }

    private void showNoNewBadges() {
        achievementsContainer.removeAllViews();
        TextView message = new TextView(this);
        message.setText("Great effort! You didn't unlock any new badges this time, but keep practicing to reach the next milestone.");
        message.setTextSize(18f);
        message.setTextColor(isDarkMode ? Color.LTGRAY : Color.parseColor("#7B1FA2"));
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