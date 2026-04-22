package com.example.quizzy

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration

/**
 * Manages user sessions and application preferences using [SharedPreferences].
 * This class handles user authentication state, UI preferences (theme, charts),
 * and provides a simple API for data persistence.
 *
 * @param context The context used to access SharedPreferences and system configurations.
 */
class SessionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "QuizzyPrefs"
        
        // Keys for SharedPreferences
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_USERNAME = "USERNAME"
        private const val KEY_SELECTED_CHART = "SELECTED_CHART"
        private const val KEY_SELECTED_DISPLAY = "SELECTED_DISPLAY"
        private const val KEY_IS_DARK_MODE = "IS_DARK_MODE"
        
        // Default values
        private const val DEFAULT_USER_ID = -1L
    }

    /**
     * Persists user authentication data.
     * 
     * @param id The unique identifier for the user.
     * @param username The display name of the user.
     */
    fun saveUser(id: Long, username: String) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, id)
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    /**
     * Retrieves the current logged-in user's ID.
     * @return The user ID, or -1 if no user is logged in.
     */
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, DEFAULT_USER_ID)

    /**
     * Retrieves the current logged-in user's name.
     * @return The username, or null if not found.
     */
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    /**
     * Checks if a user is currently logged into the application.
     * @return True if a valid user ID exists in preferences.
     */
    fun isLoggedIn(): Boolean = getUserId() != DEFAULT_USER_ID

    /**
     * Saves the user's preferred chart type for the Guardian Dashboard.
     */
    fun saveSelectedChart(chart: String) {
        prefs.edit().putString(KEY_SELECTED_CHART, chart).apply()
    }

    /**
     * Retrieves the user's preferred chart type.
     */
    fun getSelectedChart(): String? {
        return prefs.getString(KEY_SELECTED_CHART, null)
    }

    /**
     * Saves the user's preferred display mode (e.g., "Latest Activity", "Charts") 
     * for the Guardian Dashboard.
     */
    fun saveSelectedDisplay(display: String) {
        prefs.edit().putString(KEY_SELECTED_DISPLAY, display).apply()
    }

    /**
     * Retrieves the user's preferred display mode.
     */
    fun getSelectedDisplay(): String? {
        return prefs.getString(KEY_SELECTED_DISPLAY, null)
    }

    /**
     * Sets the application theme preference.
     * @param isDarkMode True for Dark Mode, false for Light Mode.
     */
    fun setThemeMode(isDarkMode: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDarkMode).apply()
    }

    /**
     * Determines the current theme mode, falling back to system settings if no 
     * explicit preference is saved.
     */
    fun isDarkMode(): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val systemInDarkTheme = uiMode == Configuration.UI_MODE_NIGHT_YES
        return prefs.getBoolean(KEY_IS_DARK_MODE, systemInDarkTheme)
    }

    /**
     * Retrieves the dark mode preference with a provided system default.
     */
    fun isDarkMode(systemInDarkTheme: Boolean): Boolean {
        return prefs.getBoolean(KEY_IS_DARK_MODE, systemInDarkTheme)
    }

    /**
     * Clears all saved preferences, effectively logging out the user.
     */
    fun logout() {
        prefs.edit().clear().apply()
    }
}
