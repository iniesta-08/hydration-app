package com.cse535.hydrofit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.cse535.hydrofit.ui.ARG_STATS
import com.cse535.hydrofit.ui.MainActivity

const val NOTIFICATION_ACTION = "com.example.SHOW_DEMO_NOTIFICATION"
const val ARG_GOAL_STEPS = "arg_goal_steps"
const val ARG_GOAL_WATER = "arg_goal_water"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val type = intent?.extras?.getString("type")
        if (type == "daily") {
            showGoalNotification(context)
        } else {
            showNotification(context)
        }
    }

    private fun showNotification(context: Context?) {

        val channelId = "Hydrofit_Hourly_Alert"
        val channelName = "Hydrofit_Hourly_Alert"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationChannel.description = "Alerts for Hydration and Fitness"

        val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse("hydrofit://logs"))

        notificationIntent.component = context?.let { ComponentName(it, MainActivity::class.java) }

        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Your Hourly Reminder")
            .setContentText("This is your hourly reminder to update your water input and exercise data.")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.createNotificationChannel(notificationChannel)

        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun showGoalNotification(context: Context?) {

        val application = context?.applicationContext as? HydroFitApplication
        val sharedPreferences = application?.sharedPreferences
        val stats = sharedPreferences?.getString(ARG_STATS, null)?.toStats()
        val goalWater = stats?.let {
            (it.weight * 2) / 3
        } ?: 125

        val goalSteps = stats?.let {
            (it.weight / it.height) * 10000
        } ?: 10000

        val edit = sharedPreferences?.edit()
        
        edit?.putInt(ARG_GOAL_WATER, goalWater)
        edit?.putInt(ARG_GOAL_STEPS, goalSteps)
        edit?.apply()


        val channelId = "Hydrofit_Daily_Alert"
        val channelName = "Hydrofit_Daily_Alert"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationChannel.description = "Daily Alerts for Goals"

        val notificationIntent = Intent()
        notificationIntent.component = context?.let { ComponentName(it, MainActivity::class.java) }

        val pendingIntent = PendingIntent.getActivity(
            context,
            4,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Good Morning!")
            .setContentText("Try to drink $goalWater Oz of water and walk or run $goalSteps Steps Today.")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.createNotificationChannel(notificationChannel)

        notificationManager.notify(2, notificationBuilder.build())
    }


}