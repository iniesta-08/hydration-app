package com.cse535.hydrofit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cse535.hydrofit.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelId = "notification channel"
const val channelName = "com.cse535.hydrofit"
const val ARG_FCM_TOKEN = "arg_fcm_token"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val application = applicationContext as? HydroFitApplication
        val sharedPreferences = application?.sharedPreferences
        val edit = sharedPreferences?.edit()
        edit?.putString(ARG_FCM_TOKEN, token)
        edit?.apply()
        Log.e("TOKEN", token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let {
            showNotification(it.getOrDefault("fitness", "0"), it.getOrDefault("hydration", "0"))
        }


    }

    private fun showNotification(fitness: String, hydration: String) {

        val channelId = "Hydrofit_Hourly_Alert"
        val channelName = "Hydrofit_Hourly_Alert"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationChannel.description = "Alerts for Hydration and Fitness"

        val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse("hydrofit://logs"))

        notificationIntent.component = ComponentName(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            3,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Urgent Reminder")
            .setContentText("You Fitness Level is $fitness % and Hydration is $hydration %. Get going")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.createNotificationChannel(notificationChannel)

        notificationManager.notify(1, notificationBuilder.build())
    }

}