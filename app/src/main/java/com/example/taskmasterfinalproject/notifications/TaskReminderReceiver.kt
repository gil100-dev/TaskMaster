package com.example.taskmasterfinalproject.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskmasterfinalproject.R

class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "task_reminders_channel"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESCRIPTION = "extra_task_description"
        const val EXTRA_TASK_PRIORITY = "extra_task_priority"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TaskReminderReceiver", "onReceive called with intent=$intent")

        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task reminder"
        val description = intent.getStringExtra(EXTRA_TASK_DESCRIPTION) ?: ""
        val priority = intent.getIntExtra(EXTRA_TASK_PRIORITY, 0)

        val channelId = when (priority) {
            3 -> NotificationHelper.CHANNEL_ID_HIGH
            2 -> NotificationHelper.CHANNEL_ID_MEDIUM
            1 -> NotificationHelper.CHANNEL_ID_LOW
            else -> NotificationHelper.CHANNEL_ID_LOW
        }

        Log.d("TaskReminderReceiver", "Showing notification for taskId=$taskId, title=$title, description=$description, priority=$priority, channel=$channelId")
        Toast.makeText(context, "Task reminder fired: $title", Toast.LENGTH_LONG).show()

        val notificationId = taskId.hashCode()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}


