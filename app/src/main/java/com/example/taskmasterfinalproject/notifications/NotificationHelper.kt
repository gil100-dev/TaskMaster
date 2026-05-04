package com.example.taskmasterfinalproject.notifications

// ספרייה המייצגת ערוץ התראות, נדרש עבור אנדרואיד 8.0 ומעלה
import android.app.NotificationChannel
// ספרייה לניהול התראות המערכת
import android.app.NotificationManager
// ספרייה המספקת גישה למשאבי המערכת והאפליקציה
import android.content.Context
// ספרייה להגדרת מאפייני שמע עבור התראות וצלצולים
import android.media.AudioAttributes
// ספרייה לגישה לצלצולים וצלילי התראה של המערכת
import android.media.RingtoneManager
// ספרייה המספקת מידע על גרסת המכשיר והמערכת
import android.os.Build
// ספרייה לתאימות לאחור ביצירת התראות
import androidx.core.app.NotificationCompat
// ספרייה המכילה את מזהי המשאבים של האפליקציה (כגון תמונות, מחרוזות)
import com.example.taskmasterfinalproject.R

// אובייקט עזר ("סינגלטון") לניהול ערוצי התראות והצגת התראות למשתמש
object NotificationHelper {
    private const val CHANNEL_HIGH = "task_reminder_high_v2"
    private const val CHANNEL_NORM = "task_reminder_norm_v2"
    private const val CHANNEL_LOW = "task_reminder_low_v2"

    // פונקציה ליצירת ערוצי התראות (Channels) הנדרשים עבור אנדרואיד O ומעלה
    fun createTaskReminderChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val alarmAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // משימה בעדיפות גבוהה (דחוף -> צליל אזעקה)
            val channelHigh = NotificationChannel(CHANNEL_HIGH, "Urgent Tasks", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for high priority tasks"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), alarmAttributes)
                enableVibration(true)
            }
            
            // משימה בעדיפות רגילה (ברירת מחדל -> צליל התראה)
            val channelNorm = NotificationChannel(CHANNEL_NORM, "Normal Tasks", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notifications for normal priority tasks"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                enableVibration(true)
            }

            // משימה בעדיפות נמוכה (עדין -> צליל התראה, חשיבות נמוכה)
            // הערה: IMPORTANCE_LOW בדרך כלל משתיק צלילים. כדי לכפות צליל "רגוע" אנו זקוקים ל-DEFAULT אך אולי צליל אחר אם קיים.
            // מאחר וחסרים לנו נכסים מותאמים אישית, נשתמש ב-LOW להתנהגות "עדינה" (ללא הצצה, אולי ללא צליל תלוי במערכת).
            // עם זאת, המשתמש ביקש "צליל רגוע". נשתמש ב-DEFAULT אך באותו צליל כמו Norm כדי להבטיח שינוגן משהו,
            // אך קונספטואלית High הוא שונה.
            val channelLow = NotificationChannel(CHANNEL_LOW, "Low Priority Tasks", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Notifications for low priority tasks"
                // IMPORTANCE_LOW בדרך כלל משתיק את הצליל.
            }

            notificationManager.createNotificationChannels(listOf(channelHigh, channelNorm, channelLow))
        }
    }

    // פונקציה להצגת התראת תזכורת למשימה בהתאם לעדיפות ולפרטים שהתקבלו
    fun showTaskReminderNotification(context: Context, taskId: String, title: String, description: String, priority: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = when (priority) {
            3 -> CHANNEL_HIGH // גבוה (3)
            1 -> CHANNEL_LOW  // נמוך (1)
            else -> CHANNEL_NORM // בינוני (2)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round) 
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setPriority(when(priority) {
                3 -> NotificationCompat.PRIORITY_HIGH
                1 -> NotificationCompat.PRIORITY_LOW
                else -> NotificationCompat.PRIORITY_DEFAULT
            })

        notificationManager.notify(taskId.hashCode(), builder.build())
    }
}
