package com.example.taskmasterfinalproject.notifications

// ספרייה המשמשת לקבלת שידורי מערכת או אפליקציה
import android.content.BroadcastReceiver
// ספרייה המספקת גישה למשאבי המערכת והאפליקציה
import android.content.Context
// ספרייה המשמשת לתיאור פעולה לביצוע, משמשת כאן להעברת נתונים ל-Receiver
import android.content.Intent

// מחלקה זו משמשת לקבלת התראות תזכורת למשימות, יורשת מ-BroadcastReceiver
class TaskReminderReceiver : BroadcastReceiver() {

    // פונקציה זו נקראת כאשר מתקבל שידור (Broadcast), ומטפלת בהצגת ההתראה למשתמש
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
