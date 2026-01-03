package com.example.taskmasterfinalproject.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id")
        val taskTitle = intent.getStringExtra("task_title")
        val taskDescription = intent.getStringExtra("task_description")

        val title = if (taskTitle.isNullOrBlank()) "Task Reminder" else taskTitle
        val description = if (taskDescription.isNullOrBlank()) "Your task is due." else taskDescription

        val priority = intent.getIntExtra("task_priority", 0)

        NotificationHelper.showTaskReminderNotification(
            context,
            taskId ?: "",
            title,
            description,
            priority
        )
    }
}
