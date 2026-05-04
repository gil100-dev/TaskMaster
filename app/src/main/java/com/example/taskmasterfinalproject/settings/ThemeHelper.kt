package com.example.taskmasterfinalproject.settings

// ספרייה המייצגת מסך באפליקציה (Activity)
import android.app.Activity
// ספרייה המאפשרת שליטה על מצבי הלילה/יום (DayNight) והערכות נושא באפליקציה
import androidx.appcompat.app.AppCompatDelegate
// ספרייה המכילה את משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// מחלקה לניהול העדפות המשתמש (SharedPreferences)
import com.example.taskmasterfinalproject.data.PreferencesManager

// אובייקט עזר ("סינגלטון") לניהול והחלת ערכות נושא (Themes) באפליקציה
object ThemeHelper {

    // פונקציה להחלת ערכת הנושא הנבחרת (צבע ומצב לילה/יום) על ה-Activity הנוכחי
    fun applyTheme(activity: Activity) {
        val prefs = PreferencesManager(activity)
        val mode = prefs.getThemeMode()
        AppCompatDelegate.setDefaultNightMode(mode)

        val accent = prefs.getAccentColor()
        val themeId = when (accent) {
            "Green" -> R.style.Theme_TaskMasterFinalProject_Green
            "Orange" -> R.style.Theme_TaskMasterFinalProject_Orange
            "Purple" -> R.style.Theme_TaskMasterFinalProject_Purple
            "Indigo" -> R.style.Theme_TaskMasterFinalProject
            else -> R.style.Theme_TaskMasterFinalProject
        }
        activity.setTheme(themeId)
    }
}
