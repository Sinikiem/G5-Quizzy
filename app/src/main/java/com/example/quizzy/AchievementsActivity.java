package com.example.quizzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private static final String NAV_PREFS = "quizzy_navigation_state";
    private static final String KEY_LAST_MAIN_SCREEN = "last_main_screen";
    private static final String KEY_ACHIEVEMENTS_FILTER = "achievements_filter";

    private static final String COLOR_PURPLE = "#A874FF";
    private static final String COLOR_ORANGE = "#FFB26B";
    private static final String COLOR_TEXT_DARK = "#2F241C";
    private static final String COLOR_TEXT_SECONDARY = "#7B6A58";
    private static final String DARK_MODE_BG = "#121212";
    private static final String DARK_MODE_SURFACE = "#1E1E1E";

    private LinearLayout achievementsContainer;
    private SessionManager sessionManager;
    private boolean isDarkMode;
    private List<Badges> allBadges = new ArrayList<>();
    private String selectedFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsContainer = findViewById(R.id.achievementsContainer);
        sessionManager = new SessionManager(this);
        isDarkMode = sessionManager.isDarkMode();

        applyTheme();
        selectedFilter = getSavedAchievementsFilter();
        setupNavigationListeners();
        addFilterDropdown();
        loadUserBadges();
    }

    private void setupNavigationListeners() {
        findViewById(R.id.navHome).setOnClickListener(v -> navigateToMainScreen("Home"));
        findViewById(R.id.navAwards).setOnClickListener(v -> { /* current screen */ });
        findViewById(R.id.navPlan).setOnClickListener(v -> {
            Intent intent = new Intent(this, StudyPlanActivity.class);
            intent.putExtra("topic", "General Math");
            intent.putExtra("accuracy", 0.5);
            intent.putExtra("gradeLevel", 3);
            startActivity(intent);
        });
        findViewById(R.id.navGuardian).setOnClickListener(v -> navigateToMainScreen("Guardian"));
        findViewById(R.id.navSettings).setOnClickListener(v -> navigateToMainScreen("Settings"));
    }

    private void loadUserBadges() {
        int userId = (int) sessionManager.getUserId();
        if (userId == -1) {
            showError("User not logged in.");
            return;
        }
        QuizRepository.getUserBadges(userId, new QuizRepository.BadgeCallback() {
            @Override
            public void onSuccess(List<Badges> earnedBadges) {
                runOnUiThread(() -> {
                    List<Badges> catalogBadges = BadgeCatalog.getAllBadges();
                    allBadges = BadgeManager.mergeBadgeStates(catalogBadges, earnedBadges);
                    showBadges();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> showError(error));
            }
        });
    }

    private void applyTheme() {
        if (isDarkMode) {
            getWindow().setStatusBarColor(Color.parseColor(DARK_MODE_BG));
            getWindow().getDecorView().setSystemUiVisibility(0);
            View root = findViewById(R.id.achievementsRoot);
            if (root != null) root.setBackgroundColor(Color.parseColor(DARK_MODE_BG));
            achievementsContainer.setBackgroundColor(Color.parseColor(DARK_MODE_BG));
            TextView title = findViewById(R.id.tvAchievementsTitle);
            if (title != null) title.setTextColor(Color.WHITE);
            View bottomNav = findViewById(R.id.bottomNavContainer);
            if (bottomNav != null) {
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setCornerRadius(dpToPx(28));
                shape.setColor(Color.parseColor(DARK_MODE_SURFACE));
                bottomNav.setBackground(shape);
            }
        } else {
            getWindow().setStatusBarColor(Color.parseColor("#A874FF"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    private void showBadges() {
        achievementsContainer.removeAllViews();
        List<Badges> filteredBadges = getFilteredBadges();

        if (filteredBadges.isEmpty()) {
            displayEmptyState();
            return;
        }

        for (Badges badge : filteredBadges) {
            addBadgeCard(badge);
        }
    }

    /**
     * Creates an elevated card for each badge instead of a flat row.
     */
    private void addBadgeCard(Badges badge) {
        boolean unlocked = badge.isUnlocked();

        // Outer margin wrapper
        LinearLayout wrapper = new LinearLayout(this);
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        wrapperParams.setMargins(0, 0, 0, dpToPx(12));
        wrapper.setLayoutParams(wrapperParams);

        // CardView for elevation
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(16));
        card.setCardElevation(dpToPx(3));
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(isDarkMode
                ? Color.parseColor(DARK_MODE_SURFACE)
                : Color.WHITE);

        // Inner row
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Left accent bar
        View accent = new View(this);
        LinearLayout.LayoutParams accentParams = new LinearLayout.LayoutParams(dpToPx(4), dpToPx(44));
        accentParams.setMarginEnd(dpToPx(14));
        accent.setLayoutParams(accentParams);
        GradientDrawable accentDrawable = new GradientDrawable();
        accentDrawable.setShape(GradientDrawable.RECTANGLE);
        accentDrawable.setCornerRadius(dpToPx(4));
        accentDrawable.setColor(unlocked
                ? Color.parseColor(COLOR_PURPLE)
                : Color.parseColor("#BDBDBD"));
        accent.setBackground(accentDrawable);

        // Text column
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textLayout.setLayoutParams(textParams);

        TextView title = new TextView(this);
        title.setText(badge.getName());
        title.setTextSize(17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(isDarkMode ? Color.WHITE
                : Color.parseColor(COLOR_TEXT_DARK));

        TextView description = new TextView(this);
        description.setText(badge.getDescription());
        description.setTextSize(13f);
        description.setTextColor(isDarkMode ? Color.LTGRAY
                : Color.parseColor(COLOR_TEXT_SECONDARY));
        description.setPadding(0, dpToPx(4), 0, dpToPx(6));

        // Status tag
        TextView status = new TextView(this);
        status.setText(unlocked ? "Unlocked" : "Locked");
        status.setTextSize(12f);
        status.setTypeface(null, Typeface.BOLD);
        status.setTextColor(unlocked
                ? Color.parseColor("#2E7D32")
                : Color.parseColor("#9E9E9E"));

        textLayout.addView(title);
        textLayout.addView(description);
        textLayout.addView(status);

        // Icon
        TextView icon = new TextView(this);
        icon.setText(unlocked ? "🏆" : "🔒");
        icon.setTextSize(28f);
        icon.setPadding(dpToPx(12), 0, 0, 0);

        row.addView(accent);
        row.addView(textLayout);
        row.addView(icon);

        card.addView(row);
        wrapper.addView(card);
        achievementsContainer.addView(wrapper);
    }

    private void addFilterDropdown() {
        LinearLayout filterSpace = findViewById(R.id.filterSpace);
        filterSpace.removeAllViews();

        // Styled filter button
        TextView filterButton = new TextView(this);
        filterButton.setText("Filter by  ▼");
        filterButton.setTextSize(14f);
        filterButton.setTypeface(null, Typeface.BOLD);
        filterButton.setTextColor(Color.parseColor(COLOR_PURPLE));
        filterButton.setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(10));

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setShape(GradientDrawable.RECTANGLE);
        btnBg.setCornerRadius(dpToPx(20));
        btnBg.setColor(Color.parseColor(COLOR_PURPLE + "1A")); // 10% alpha
        filterButton.setBackground(btnBg);

        filterButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(AchievementsActivity.this, filterButton);
            popupMenu.getMenu().add("All");
            popupMenu.getMenu().add("Unlocked");
            popupMenu.getMenu().add("Locked");
            popupMenu.setOnMenuItemClickListener(item -> {
                selectedFilter = item.getTitle().toString();
                saveAchievementsFilter(selectedFilter);
                showBadges();
                return true;
            });
            popupMenu.show();
        });

        filterSpace.addView(filterButton);
    }

    private void displayEmptyState() {
        TextView empty = new TextView(this);
        String emptyText = "No badges available yet.";
        if ("Unlocked".equals(selectedFilter)) emptyText = "No unlocked badges found.";
        else if ("Locked".equals(selectedFilter)) emptyText = "No locked badges found.";
        empty.setText(emptyText);
        empty.setTextSize(18f);
        empty.setTextColor(isDarkMode ? Color.LTGRAY : Color.parseColor(COLOR_TEXT_SECONDARY));
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, dpToPx(60), 0, 0);
        achievementsContainer.addView(empty);
    }

    private void navigateToMainScreen(String targetScreen) {
        saveLastMainScreen(targetScreen);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("start_screen", targetScreen);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private SharedPreferences getNavPrefs() {
        return getSharedPreferences(NAV_PREFS, MODE_PRIVATE);
    }

    private void saveLastMainScreen(String screen) {
        getNavPrefs().edit().putString(KEY_LAST_MAIN_SCREEN, screen).apply();
    }

    private void saveAchievementsFilter(String filter) {
        getNavPrefs().edit().putString(KEY_ACHIEVEMENTS_FILTER, filter).apply();
    }

    private String getSavedAchievementsFilter() {
        return getNavPrefs().getString(KEY_ACHIEVEMENTS_FILTER, "All");
    }

    private void showError(String message) {
        achievementsContainer.removeAllViews();
        TextView errorView = new TextView(this);
        errorView.setText("Error: " + message);
        errorView.setTextSize(18f);
        errorView.setTextColor(Color.RED);
        errorView.setPadding(dpToPx(20), dpToPx(40), dpToPx(20), dpToPx(20));
        achievementsContainer.addView(errorView);
    }

    private List<Badges> getFilteredBadges() {
        if ("Unlocked".equals(selectedFilter)) return BadgeManager.getUnlockedBadges(allBadges);
        else if ("Locked".equals(selectedFilter)) return BadgeManager.getLockedBadges(allBadges);
        else return new ArrayList<>(allBadges);
    }
}