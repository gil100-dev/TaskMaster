package com.example.taskmasterfinalproject.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.taskmasterfinalproject.model.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ReminderScheduler {

    fun scheduleTaskReminder(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check for permission before scheduling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Optional: You could log this or handle it silently.
            // The alarm will be inexact because the fallback logic will be used.
        }

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_description", task.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        task.dueTimeMillis?.let {
            try {
                // Use the best available API when permission is granted
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it, pendingIntent)
            } catch (e: SecurityException) {
                // Fallback for edge cases or devices where the permission check is insufficient
                alarmManager.set(AlarmManager.RTC_WAKEUP, it, pendingIntent)
            }
        }
    }

    fun cancelTaskReminder(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun requestExactAlarmPermissionIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Explain to the user why this is needed
                MaterialAlertDialogBuilder(context)
                    .setTitle("Permission Required")
                    .setMessage("To ensure task reminders are sent exactly on time, TaskMaster needs permission to schedule exact alarms.")
                    .setPositiveButton("Go to Settings") { _, _ ->
                        Intent().also {
                            it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            context.startActivity(it)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}
