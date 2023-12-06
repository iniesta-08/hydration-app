package com.example.water

import android.app.Activity
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
import android.widget.ProgressBar
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
    private lateinit var suggestActivityButton: Button
    private var hydrationLevel = 0
    private val INPUT_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hydrationLevelEditText = findViewById(R.id.hydrationLevelEditText)
        setHydrationLevelButton = findViewById(R.id.setHydrationLevelButton)
        startReminderButton = findViewById(R.id.startReminderButton)
        updateWaterConsumedButton = findViewById(R.id.updateWaterConsumedButton)
        suggestActivityButton = findViewById(R.id.suggestActivityButton)

        setHydrationLevelButton.setOnClickListener {
            setHydrationLevel()
        }

        startReminderButton.setOnClickListener {
            startWaterReminder()
        }

        updateWaterConsumedButton.setOnClickListener {
            updateWaterConsumed()
        }

        suggestActivityButton.setOnClickListener {
            suggestActivity()
        }

        val startInputButton = findViewById<Button>(R.id.startInputButton)
        startInputButton.setOnClickListener {
            val intent = Intent(this, InputActivity::class.java)
            startActivityForResult(intent, INPUT_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INPUT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val suggestedWaterIntake = data?.getIntExtra("suggestedWaterIntake", 0) ?: 0
            val height = data?.getDoubleExtra("height", 0.0) ?: 0.0
            val weight = data?.getDoubleExtra("weight", 0.0) ?: 0.0

            // Set the hydration level in MainActivity
            setHydrationLevelBasedOnSuggestion(suggestedWaterIntake, height, weight)
        }
    }

    private fun setHydrationLevelBasedOnSuggestion(suggestedWaterIntake: Int, height: Double, weight: Double) {
        // Calculate your hydration level based on the suggested water intake or any other logic
        // For simplicity, let's set hydration level as suggestedWaterIntake
        hydrationLevel = suggestedWaterIntake

        // Update UI or perform any other actions based on the calculated hydration level
        hydrationLevelEditText.setText(hydrationLevel.toString())
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

        // Update the ProgressBar based on the new hydration level
        val waterProgressBar = findViewById<ProgressBar>(R.id.waterProgressBar)
        waterProgressBar.progress = hydrationLevel

        Toast.makeText(this, "Water consumed updated. Current level: $hydrationLevel", Toast.LENGTH_SHORT).show()
    }


    private fun suggestActivity() {
        // You can customize this message based on your specific suggestion
        val suggestionMessage = "Stand up and take a short walk. It's good for your health."

        // Display a toast message or use a dialog to show the suggestion
        Toast.makeText(this, suggestionMessage, Toast.LENGTH_LONG).show()

        // Schedule the repeating notification using AlarmManager
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ActivityReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val intervalMillis: Long = 30 * 60 * 1000 // 30 minutes
        val initialDelayMillis = SystemClock.elapsedRealtime() + intervalMillis

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            initialDelayMillis,
            intervalMillis,
            pendingIntent
        )
    }
}
