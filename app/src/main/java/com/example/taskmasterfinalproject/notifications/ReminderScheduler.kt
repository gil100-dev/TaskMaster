package com.example.taskmasterfinalproject.notifications

// ספרייה לגישה לשירותי ההתראות המתוזמנות של המערכת (Alarms)
import android.app.AlarmManager
// ספרייה ליצירת Intent הממתין לביצוע עתידי (ע"י ה-AlarmManager)
import android.app.PendingIntent
// ספרייה המספקת גישה למשאבי המערכת והאפליקציה
import android.content.Context
// ספרייה המשמשת לתיאור פעולה לביצוע
import android.content.Intent
// ספרייה המספקת מידע על גרסת המכשיר והמערכת
import android.os.Build
// ספרייה המכילה קבועים להגדרות המערכת (כגון מסך הגדרות הרשאות)
import android.provider.Settings
// המודל המייצג את המשימה
import com.example.taskmasterfinalproject.model.Task
// ספרייה ליצירת דיאלוגים בעיצוב Material Design
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// אובייקט עזר ("סינגלטון") לניהול תזמון וביטול תזכורות למשימות
object ReminderScheduler {

    // פונקציה לתזמון תזכורת למשימה ספציפית
    fun scheduleTaskReminder(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // בדיקת הרשאה לפני התזמון (עבור אנדרואיד 12 ומעלה)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // אופציונלי: ניתן לרשום לוג או לטפל בשקט.
            // ההתראה תהיה לא מדויקת כי תופעל לוגיקת ה-fallback.
        }

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_description", task.description)
            putExtra("task_priority", task.priority)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        task.dueTimeMillis?.let {
            try {
                // שימוש ב-API הטוב ביותר הזמין כאשר יש הרשאה
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it, pendingIntent)
            } catch (e: SecurityException) {
                // גיבוי למקרי קצה או מכשירים בהם בדיקת ההרשאה אינה מספיקה
                alarmManager.set(AlarmManager.RTC_WAKEUP, it, pendingIntent)
            }
        }
    }

    // פונקציה לביטול תזכורת קיימת עבור משימה
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

    // פונקציה לבקשת הרשאת "התראות מדויקות" מהמשתמש במידת הצורך
    fun requestExactAlarmPermissionIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // הסבר למשתמש מדוע זה נדרש
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
