// Create a new Kotlin file named ActivityReminderReceiver.kt
package com.example.water

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class ActivityReminderReceiver : BroadcastReceiver() {

    private companion object {
        const val CHANNEL_ID = "ActivityReminderChannel"
        const val NOTIFICATION_ID = 2
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Activity Reminder")
            .setContentText("It's time to stand up and take a short walk.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "ActivityReminderChannel"
            val descriptionText = "Channel for Activity Reminder Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = descriptionText

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
