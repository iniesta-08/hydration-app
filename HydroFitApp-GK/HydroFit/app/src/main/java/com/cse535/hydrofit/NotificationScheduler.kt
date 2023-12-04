package com.cse535.hydrofit

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import java.util.Calendar

class NotificationScheduler {

    fun scheduleNotifications(context: Context) {
        scheduleDailyNotification(context)
        scheduleHourlyNotifications(context)
    }

    private fun scheduleDailyNotification(context: Context) {
        val dailyCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }

        val dailyIntent = Intent(NOTIFICATION_ACTION)
        dailyIntent.component =
            ComponentName(context, NotificationReceiver::class.java)
        dailyIntent.putExtra("type", "daily")


        val dailyPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            dailyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            dailyCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            dailyPendingIntent
        )
    }

    private fun scheduleHourlyNotifications(context: Context) {
        val hourlyIntent = Intent(NOTIFICATION_ACTION)
        hourlyIntent.component =
            ComponentName(context, NotificationReceiver::class.java)
        hourlyIntent.putExtra("type", "hourly")

        val hourlyPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            hourlyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            SystemClock.elapsedRealtime(),
            AlarmManager.INTERVAL_HOUR,
            hourlyPendingIntent
        )
    }
}
