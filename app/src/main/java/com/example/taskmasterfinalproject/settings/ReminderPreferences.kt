package com.example.taskmasterfinalproject.settings

import android.content.Context

object ReminderPreferences {

    private const val PREFS_NAME = "task_prefs"
    const val KEY_REMIND_BEFORE_MINUTES = "remind_before_minutes"

    fun getRemindBeforeMinutes(context: Context): Int {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REMIND_BEFORE_MINUTES, 0)
    }

    fun setRemindBeforeMinutes(context: Context, minutes: Int) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_REMIND_BEFORE_MINUTES, minutes).apply()
    }
}


