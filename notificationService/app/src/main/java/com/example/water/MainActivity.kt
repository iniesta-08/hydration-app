package com.example.water

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat

class MainActivity : ComponentActivity() {

    private companion object {
        const val CHANNEL_ID = "WaterReminderChannel"
        const val NOTIFICATION_ID = 1
    }

    private lateinit var hydrationLevelEditText: EditText
    private lateinit var setHydrationLevelButton: Button
    private lateinit var startReminderButton: Button
    private lateinit var updateWaterConsumedButton: Button

    private var hydrationLevel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hydrationLevelEditText = findViewById(R.id.hydrationLevelEditText)
        setHydrationLevelButton = findViewById(R.id.setHydrationLevelButton)
        startReminderButton = findViewById(R.id.startReminderButton)
        updateWaterConsumedButton = findViewById(R.id.updateWaterConsumedButton)

        setHydrationLevelButton.setOnClickListener {
            setHydrationLevel()
        }

        startReminderButton.setOnClickListener {
            startWaterReminder()
        }

        updateWaterConsumedButton.setOnClickListener {
            updateWaterConsumed()
        }
    }

    private fun setHydrationLevel() {
        val hydrationLevelStr = hydrationLevelEditText.text.toString()
        if (hydrationLevelStr.isNotEmpty()) {
            hydrationLevel = hydrationLevelStr.toInt()
            Toast.makeText(this, "Hydration level set to $hydrationLevel", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter a hydration level", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startWaterReminder() {
        if (hydrationLevel == 0) {
            Toast.makeText(this, "Please set a hydration level first", Toast.LENGTH_SHORT).show()
            return
        }

        // Schedule the reminder using AlarmManager
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val intervalMillis: Long = 10 * 200 // 1 minute (adjust as needed)
        val initialDelayMillis = SystemClock.elapsedRealtime() + intervalMillis

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            initialDelayMillis,
            intervalMillis,
            pendingIntent
        )

        Toast.makeText(this, "Water reminder started", Toast.LENGTH_SHORT).show()
    }

    private fun updateWaterConsumed() {
        // This is where you would update the amount of water consumed by the user
        // For simplicity, let's just increment the hydration level for demonstration purposes
        hydrationLevel++
        Toast.makeText(this, "Water consumed updated. Current level: $hydrationLevel", Toast.LENGTH_SHORT).show()
    }
}
