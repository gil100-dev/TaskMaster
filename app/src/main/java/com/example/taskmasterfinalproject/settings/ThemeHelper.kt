package com.example.taskmasterfinalproject.settings

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import com.example.taskmasterfinalproject.R
import com.example.taskmasterfinalproject.data.PreferencesManager

object ThemeHelper {

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
