package com.example.taskmasterfinalproject.data

import android.content.Context
import android.content.SharedPreferences
import com.example.taskmasterfinalproject.model.SortOption

class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)



    fun saveSortOption(option: SortOption) {
        sharedPreferences.edit().putString(KEY_SORT_OPTION, option.name).apply()
    }

    fun getSortOption(): SortOption {
        val name = sharedPreferences.getString(KEY_SORT_OPTION, SortOption.DEFAULT.name)
        return try {
            SortOption.valueOf(name ?: SortOption.DEFAULT.name)
        } catch (e: IllegalArgumentException) {
            SortOption.DEFAULT
        }
    }

    fun saveShowCompleted(show: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SHOW_COMPLETED, show).apply()
    }

    fun getShowCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_COMPLETED, false) // Default hide completed
    }

    fun saveThemeMode(mode: Int) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getThemeMode(): Int {
        // Default to MODE_NIGHT_FOLLOW_SYSTEM = -1
        return sharedPreferences.getInt(KEY_THEME_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun saveAccentColor(color: String) {
        sharedPreferences.edit().putString(KEY_ACCENT_COLOR, color).apply()
    }

    fun getAccentColor(): String {
        return sharedPreferences.getString(KEY_ACCENT_COLOR, "Indigo") ?: "Indigo"
    }

    fun saveAiAdvice(taskId: String, advice: String) {
        sharedPreferences.edit().putString(KEY_PREFIX_AI_ADVICE + taskId, advice).apply()
    }

    fun getAiAdvice(taskId: String): String? {
        return sharedPreferences.getString(KEY_PREFIX_AI_ADVICE + taskId, null)
    }
    
    fun saveSession(userId: Long) {
        val expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 Hours
        sharedPreferences.edit()
            .putLong(KEY_SESSION_USER_ID, userId)
            .putLong(KEY_SESSION_EXPIRY, expiry)
            .apply()
    }
    
    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_SESSION_USER_ID)
            .remove(KEY_SESSION_EXPIRY)
            .apply()
    }
    
    fun getSessionUserId(): Long? {
        val userId = sharedPreferences.getLong(KEY_SESSION_USER_ID, -1L)
        if (userId == -1L) return null
        
        val expiry = sharedPreferences.getLong(KEY_SESSION_EXPIRY, 0L)
        if (System.currentTimeMillis() > expiry) {
            clearSession()
            return null
        }
        return userId
    }

    companion object {
        private const val PREFS_NAME = "taskmaster_prefs"
        private const val KEY_SORT_OPTION = "key_sort_option"
        private const val KEY_SHOW_COMPLETED = "key_show_completed"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ACCENT_COLOR = "key_accent_color"
        private const val KEY_PREFIX_AI_ADVICE = "ai_advice_"
        private const val KEY_SESSION_USER_ID = "session_user_id"
        private const val KEY_SESSION_EXPIRY = "session_expiry"
    }
}
