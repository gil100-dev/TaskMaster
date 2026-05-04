package com.example.taskmasterfinalproject.data

// ספרייה המספקת גישה למשאבי המערכת והאפליקציה
import android.content.Context
// ספרייה לניהול העדפות משתמש קלות (Key-Value)
import android.content.SharedPreferences
// המודל המייצג את אפשרויות המיון
import com.example.taskmasterfinalproject.model.SortOption

// מחלקה לניהול ושמירת העדפות המשתמש (כגון מיון, מצב לילה וכו')
class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // פונקציה לשמירת אפשרות המיון הנבחרת
    fun saveSortOption(option: SortOption) {
        sharedPreferences.edit().putString(KEY_SORT_OPTION, option.name).apply()
    }

    // פונקציה לקבלת אפשרות המיון השמורה (ברירת מחדל: DEFAULT)
    fun getSortOption(): SortOption {
        val name = sharedPreferences.getString(KEY_SORT_OPTION, SortOption.DEFAULT.name)
        return try {
            SortOption.valueOf(name ?: SortOption.DEFAULT.name)
        } catch (e: IllegalArgumentException) {
            SortOption.DEFAULT
        }
    }

    // פונקציה לשמירת מצב הצגת משימות שהושלמו
    fun saveShowCompleted(show: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SHOW_COMPLETED, show).apply()
    }

    // פונקציה לקבלת מצב הצגת משימות שהושלמו
    fun getShowCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_COMPLETED, false) // ברירת מחדל: מוסתר
    }

    // פונקציה לשמירת מצב ערכת הנושא (לילה/יום/מערכת)
    fun saveThemeMode(mode: Int) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    // פונקציה לקבלת מצב ערכת הנושא
    fun getThemeMode(): Int {
        // ברירת מחדל: לפי הגדרות המערכת
        return sharedPreferences.getInt(KEY_THEME_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    // פונקציה לשמירת צבע הדגש (Accent Color)
    fun saveAccentColor(color: String) {
        sharedPreferences.edit().putString(KEY_ACCENT_COLOR, color).apply()
    }

    // פונקציה לקבלת צבע הדגש
    fun getAccentColor(): String {
        return sharedPreferences.getString(KEY_ACCENT_COLOR, "Indigo") ?: "Indigo"
    }

    // פונקציה לשמירת עצה מ-AI עבור משימה ספציפית
    fun saveAiAdvice(taskId: String, advice: String) {
        sharedPreferences.edit().putString(KEY_PREFIX_AI_ADVICE + taskId, advice).apply()
    }

    // פונקציה לקבלת עצה מ-AI שנשמרה עבור משימה
    fun getAiAdvice(taskId: String): String? {
        return sharedPreferences.getString(KEY_PREFIX_AI_ADVICE + taskId, null)
    }
    
    // פונקציה לשמירת פרטי הפעלה (Session) של המשתמש (login)
    fun saveSession(userId: Long) {
        val expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 שעות
        sharedPreferences.edit()
            .putLong(KEY_SESSION_USER_ID, userId)
            .putLong(KEY_SESSION_EXPIRY, expiry)
            .apply()
    }
    
    // פונקציה לניקוי ה-Session (logout)
    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_SESSION_USER_ID)
            .remove(KEY_SESSION_EXPIRY)
            .apply()
    }
    
    // פונקציה לקבלת מזהה המשתמש הנוכחי אם ה-Session בתוקף
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

    // אובייקט נלווה המכיל קבועים לשמות המפתחות ב-SharedPreferences
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
