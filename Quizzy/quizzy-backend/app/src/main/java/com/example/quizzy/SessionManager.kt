package com.example.quizzy

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("QuizzyPrefs", Context.MODE_PRIVATE)

    fun saveUser(id: Long, username: String) {
        prefs.edit().apply {
            putLong("USER_ID", id)
            putString("USERNAME", username)
            apply()
        }
    }

    fun getUserId(): Long = prefs.getLong("USER_ID", -1L)
    fun getUsername(): String? = prefs.getString("USERNAME", null)

    fun isLoggedIn(): Boolean = getUserId() != -1L

    fun logout() {
        prefs.edit().clear().apply()
    }
}
