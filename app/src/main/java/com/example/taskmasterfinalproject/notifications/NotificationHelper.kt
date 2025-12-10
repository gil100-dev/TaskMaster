package com.example.taskmasterfinalproject.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build

object NotificationHelper {

    const val CHANNEL_ID_LOW = "task_reminders_low"
    const val CHANNEL_ID_MEDIUM = "task_reminders_medium"
    const val CHANNEL_ID_HIGH = "task_reminders_high"

    fun createTaskReminderChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // Low priority channel
            val lowChannel = NotificationChannel(
                CHANNEL_ID_LOW,
                "Low priority tasks",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for low priority tasks"
                val defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(defaultNotificationSound, audioAttributes)
            }
            notificationManager.createNotificationChannel(lowChannel)

            // Medium priority channel
            val mediumChannel = NotificationChannel(
                CHANNEL_ID_MEDIUM,
                "Medium priority tasks",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for medium priority tasks"
                val defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(defaultNotificationSound, audioAttributes)
            }
            notificationManager.createNotificationChannel(mediumChannel)

            // High priority channel
            val highChannel = NotificationChannel(
                CHANNEL_ID_HIGH,
                "High priority tasks",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for high priority tasks"
                val defaultAlarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setSound(defaultAlarmSound, audioAttributes)
            }
            notificationManager.createNotificationChannel(highChannel)
        }
    }
}


