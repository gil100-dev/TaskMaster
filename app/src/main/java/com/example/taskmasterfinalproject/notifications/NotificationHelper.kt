package com.example.taskmasterfinalproject.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.taskmasterfinalproject.R

object NotificationHelper {
    private const val CHANNEL_HIGH = "task_reminder_high_v2"
    private const val CHANNEL_NORM = "task_reminder_norm_v2"
    private const val CHANNEL_LOW = "task_reminder_low_v2"

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

            // High Priority (Urgent -> Alarm Sound)
            val channelHigh = NotificationChannel(CHANNEL_HIGH, "Urgent Tasks", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for high priority tasks"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), alarmAttributes)
                enableVibration(true)
            }
            
            // Normal Priority (Default -> Notification Sound)
            val channelNorm = NotificationChannel(CHANNEL_NORM, "Normal Tasks", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notifications for normal priority tasks"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                enableVibration(true)
            }

            // Low Priority (Subtle -> Notification Sound, Low Importance)
            // Note: IMPORTANCE_LOW usually implies no sound. To force a "calm" sound, we need IMPORTANCE_DEFAULT but maybe a different sound if available.
            // Since we lack custom assets, we'll use IMPORTANCE_LOW for "subtle" behavior (no peeking, maybe no sound depending on system).
            // However, user requested "calm sound". We'll use DEFAULT importance but same sound as Norm to ensure it plays SOMETHING, 
            // but conceptually High is distinct.
            val channelLow = NotificationChannel(CHANNEL_LOW, "Low Priority Tasks", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Notifications for low priority tasks"
                // IMPORTANCE_LOW usually suppresses sound.
            }

            notificationManager.createNotificationChannels(listOf(channelHigh, channelNorm, channelLow))
        }
    }

    fun showTaskReminderNotification(context: Context, taskId: String, title: String, description: String, priority: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = when (priority) {
            3 -> CHANNEL_HIGH // High (3)
            1 -> CHANNEL_LOW  // Low (1)
            else -> CHANNEL_NORM // Medium (2)
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
