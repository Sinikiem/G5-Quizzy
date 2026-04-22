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

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays all achievement badges available in the application.
 * It allows users to view their progress, filtering between earned (unlocked) 
 * and pending (locked) milestones.
 */
public class AchievementsActivity extends AppCompatActivity {

    // Storage Keys
    private static final String NAV_PREFS = "quizzy_navigation_state";
    private static final String KEY_LAST_MAIN_SCREEN = "last_main_screen";
    private static final String KEY_ACHIEVEMENTS_FILTER = "achievements_filter";

    // Theme Colors
    private static final String DARK_MODE_BG = "#121212";
    private static final String DARK_MODE_SURFACE = "#1E1E1E";
    private static final String LIGHT_MODE_TEXT_SECONDARY = "#6E6257";

    // UI Components
    private LinearLayout achievementsContainer;
    private SessionManager sessionManager;
    private boolean isDarkMode;

    // State
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

    /**
     * Initializes click listeners for the bottom navigation bar.
     */
    private void setupNavigationListeners() {
        findViewById(R.id.navHome).setOnClickListener(v -> navigateToMainScreen("Home"));
        findViewById(R.id.navAwards).setOnClickListener(v -> { /* Current Screen */ });
        findViewById(R.id.navGuardian).setOnClickListener(v -> navigateToMainScreen("Guardian"));
        findViewById(R.id.navSettings).setOnClickListener(v -> navigateToMainScreen("Settings"));
    }

    /**
     * Fetches badge data from the repository and merges it with the local catalog.
     */
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

    /**
     * Adjusts the UI appearance based on the user's dark mode preference.
     * Also sets the status bar color to match the background to remove the default purple bar.
     */
    private void applyTheme() {
        if (isDarkMode) {
            getWindow().setStatusBarColor(Color.parseColor(DARK_MODE_BG));
            getWindow().getDecorView().setSystemUiVisibility(0);

            View root = findViewById(R.id.achievementsRoot);
            if (root != null) root.setBackgroundColor(Color.parseColor(DARK_MODE_BG));
            
            achievementsContainer.setBackgroundColor(Color.parseColor(DARK_MODE_BG));
            
            TextView title = findViewById(R.id.tvAchievementsTitle);
            if (title != null) title.setTextColor(Color.WHITE);

            View filterSpace = findViewById(R.id.filterSpace);
            if (filterSpace != null) filterSpace.setBackgroundColor(Color.parseColor(DARK_MODE_SURFACE));

            View bottomNav = findViewById(R.id.bottomNavContainer);
            if (bottomNav != null) {
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setCornerRadius(dpToPx(28));
                shape.setColor(Color.parseColor(DARK_MODE_SURFACE));
                bottomNav.setBackground(shape);
            }
        } else {
            getWindow().setStatusBarColor(Color.parseColor("#FFFBF2"));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    /**
     * Algorithm: Programmatically constructs the list of badges based on the active filter.
     * 1. Clears existing views.
     * 2. Adds the filter dropdown at the top.
     * 3. Iterates through filtered badges and builds UI rows (Title, Description, Icon).
     * 4. Handles the "Empty State" if no badges match the filter.
     */
    private void showBadges() {
        achievementsContainer.removeAllViews();



        List<Badges> filteredBadges = getFilteredBadges();

        if (filteredBadges.isEmpty()) {
            displayEmptyState();
            return;
        }

        for (Badges badge : filteredBadges) {
            addBadgeRow(badge);
        }
    }

    private void addBadgeRow(Badges badge) {
        boolean unlocked = badge.isUnlocked();

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 30, 0, 30);

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
        title.setTextSize(30f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#2F241C"));

        TextView description = new TextView(this);
        description.setText(badge.getDescription());
        description.setTextSize(20f);
        description.setTextColor(Color.parseColor("#7B6A58"));
        description.setPadding(0, 8, 0, 0);

        textLayout.addView(title);
        textLayout.addView(description);

        row.addView(textLayout);

        TextView icon = new TextView(this);
        icon.setText(unlocked ? "🏆" : "🔒");
        icon.setTextSize(34f);
        icon.setPadding(24, 0, 0, 0);
        row.addView(icon);

        achievementsContainer.addView(row);
        addDivider();
    }

    private void addDivider() {
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        ));
        divider.setBackgroundColor(Color.parseColor("#D8D2C8"));
        achievementsContainer.addView(divider);
    }

    private void displayEmptyState() {
        TextView empty = new TextView(this);
        String emptyText = "No badges available yet.";
        if ("Unlocked".equals(selectedFilter)) emptyText = "No unlocked badges found.";
        else if ("Locked".equals(selectedFilter)) emptyText = "No locked badges found.";

        empty.setText(emptyText);
        empty.setTextSize(22f);
        empty.setTextColor(isDarkMode ? Color.LTGRAY : Color.parseColor(LIGHT_MODE_TEXT_SECONDARY));
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 100, 0, 0);
        achievementsContainer.addView(empty);
    }

    private void addFilterDropdown() {
        LinearLayout filterSpace = findViewById(R.id.filterSpace);
        filterSpace.removeAllViews();

        TextView filterButton = new TextView(this);
        filterButton.setText("Filter by ▼");
        filterButton.setTextSize(18f);
        filterButton.setTypeface(null, Typeface.BOLD);
        filterButton.setTextColor(Color.parseColor("#5A4A3B"));
        filterButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
        filterButton.setPadding(40, 22, 40, 22);
        filterButton.setElevation(4f);

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
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void navigateToMainScreen(String targetScreen) {
        saveLastMainScreen(targetScreen);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("start_screen", targetScreen);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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
        errorView.setPadding(20, 40, 20, 20);
        achievementsContainer.addView(errorView);
    }

    private List<Badges> getFilteredBadges() {
        if ("Unlocked".equals(selectedFilter)) {
            return BadgeManager.getUnlockedBadges(allBadges);
        } else if ("Locked".equals(selectedFilter)) {
            return BadgeManager.getLockedBadges(allBadges);
        } else {
            return new ArrayList<>(allBadges);
        }
    }
}
