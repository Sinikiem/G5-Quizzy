package com.example.quizzy;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private LinearLayout achievementsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsContainer = findViewById(R.id.achievementsContainer);

        int userId = 1;
        List<Badges> badges = QuizRepository.getUserBadges(userId);

        showBadges(badges);
    }

    private void showBadges(List<Badges> badges) {
        achievementsContainer.removeAllViews();

        if (badges == null || badges.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No badges available yet.");
            empty.setTextSize(22f);
            empty.setTextColor(Color.parseColor("#6E6257"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 100, 0, 0);
            achievementsContainer.addView(empty);
            return;
        }

        int maxToShow = Math.min(badges.size(), 10);

        for (int i = 0; i < maxToShow; i++) {
            Badges badge = badges.get(i);
            boolean unlocked = badge.isUnlocked();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 28, 0, 28);
            row.setAlpha(unlocked ? 1f : 0.55f);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            textLayout.setLayoutParams(textParams);

            TextView title = new TextView(this);
            title.setText(badge.getName());
            title.setTextSize(24f);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setTextColor(unlocked
                    ? Color.parseColor("#2F241C")
                    : Color.parseColor("#9E9E9E"));

            TextView description = new TextView(this);
            description.setText(badge.getDescription());
            description.setTextSize(18f);
            description.setPadding(0, 8, 0, 0);
            description.setTextColor(unlocked
                    ? Color.parseColor("#6E6257")
                    : Color.parseColor("#BDBDBD"));

            TextView status = new TextView(this);
            status.setText(unlocked ? "Unlocked" : "Locked");
            status.setTextSize(15f);
            status.setPadding(0, 10, 0, 0);
            status.setTextColor(unlocked
                    ? Color.parseColor("#2E7D32")
                    : Color.parseColor("#9E9E9E"));

            textLayout.addView(title);
            textLayout.addView(description);
            textLayout.addView(status);

            row.addView(textLayout);

            // Locked badges show trophy, unlocked badges show no icon
            if (!unlocked) {
                TextView trophy = new TextView(this);
                trophy.setText("🏆");
                trophy.setTextSize(30f);
                trophy.setPadding(20, 0, 0, 0);
                row.addView(trophy);
            }

            achievementsContainer.addView(row);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
            ));
            divider.setBackgroundColor(Color.parseColor("#D8D2C8"));

            achievementsContainer.addView(divider);
        }
    }
}