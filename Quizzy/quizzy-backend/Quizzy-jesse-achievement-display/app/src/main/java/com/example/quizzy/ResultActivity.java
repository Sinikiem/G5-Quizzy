package com.example.quizzy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.List;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private TextView tvFinalScore;
    private TextView tvResultMessage;
    private LinearLayout achievementsContainer;
    private Button btnBackToDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        achievementsContainer = findViewById(R.id.achievementsContainer);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        tvFinalScore.setText(String.format(Locale.getDefault(),
                "Final Score: %d / %d", score, totalQuestions));

        tvResultMessage.setText(AchievementProcessor.getResultMessage(score, totalQuestions));

        // Use BadgeManager to get real earned badges from SharedPreferences
        List<Badges> allBadges = BadgeCatalog.getAllBadges();
        List<Badges> earnedBadges = BadgeManager.getEarnedBadges(this);

        List<AchievementDisplayItem> displayItems =
                AchievementProcessor.prepareAchievementsForDisplay(
                        allBadges,
                        earnedBadges,
                        score,
                        totalQuestions
                );

        showAchievements(displayItems);

        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showAchievements(List<AchievementDisplayItem> items) {
        achievementsContainer.removeAllViews();

        for (AchievementDisplayItem item : items) {
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 24);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(24f);
            cardView.setCardElevation(8f);
            cardView.setUseCompatPadding(true);
            cardView.setCardBackgroundColor(item.isUnlocked()
                    ? Color.parseColor("#E8F5E9")
                    : Color.parseColor("#FFF3E0"));

            LinearLayout contentLayout = new LinearLayout(this);
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            contentLayout.setPadding(32, 32, 32, 32);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(item.getTitle());
            tvTitle.setTextSize(20f);
            tvTitle.setTextColor(Color.parseColor("#5A4A3B"));
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvDescription = new TextView(this);
            tvDescription.setText(item.getDescription());
            tvDescription.setTextSize(16f);
            tvDescription.setTextColor(Color.parseColor("#7B6A58"));
            tvDescription.setPadding(0, 12, 0, 12);

            TextView tvStatus = new TextView(this);
            tvStatus.setText(item.getStatusText());
            tvStatus.setTextSize(15f);
            tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
            tvStatus.setTextColor(item.isUnlocked()
                    ? Color.parseColor("#2E7D32")
                    : Color.parseColor("#EF6C00"));

            contentLayout.addView(tvTitle);
            contentLayout.addView(tvDescription);
            contentLayout.addView(tvStatus);

            cardView.addView(contentLayout);
            achievementsContainer.addView(cardView);
        }
    }
}
