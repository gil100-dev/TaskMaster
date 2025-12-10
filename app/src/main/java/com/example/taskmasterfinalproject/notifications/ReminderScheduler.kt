package com.example.taskmasterfinalproject.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.taskmasterfinalproject.model.Task

object ReminderScheduler {

    fun scheduleTaskReminder(context: Context, task: Task) {
        val triggerAtMillis = task.dueTimeMillis ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskReminderReceiver.EXTRA_TASK_DESCRIPTION, task.description)
            putExtra(TaskReminderReceiver.EXTRA_TASK_PRIORITY, task.priority ?: 0)
        }

        val requestCode = task.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(
            "ReminderScheduler",
            "scheduleTaskReminder: id=${task.id}, triggerAt=$triggerAtMillis, now=${System.currentTimeMillis()}"
        )

        if (triggerAtMillis > System.currentTimeMillis()) {
            try {
                Log.d(
                    "ReminderScheduler",
                    "Trying setExactAndAllowWhileIdle for task ${task.id} at $triggerAtMillis"
                )
                Toast.makeText(context, "Alarm set for this task", Toast.LENGTH_SHORT).show()

                // פה אנדרואיד 16 עלול לזרוק SecurityException אם אין הרשאת exact alarm
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (se: SecurityException) {
                // אין הרשאה ל-exact alarm – נופלים לפתרון עוקף ולא מקריסים את האפליקציה
                Log.e(
                    "ReminderScheduler",
                    "Exact alarm not allowed, falling back to set(): ${se.message}"
                )
                Toast.makeText(
                    context,
                    "Exact alarm not allowed, using regular alarm instead",
                    Toast.LENGTH_LONG
                ).show()

                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            Log.d(
                "ReminderScheduler",
                "Not scheduling alarm, due time is in the past for task ${task.id}"
            )
        }
    }

    fun cancelTaskReminder(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val requestCode = task.id.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}
