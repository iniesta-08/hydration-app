package com.cse535.hydrofit.stepcounter


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cse535.hydrofit.ARG_GOAL_STEPS
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.R
import com.cse535.hydrofit.toStats
import com.cse535.hydrofit.ui.ARG_STATS
import com.cse535.hydrofit.ui.MainActivity
import kotlinx.coroutines.launch

class StepsService : LifecycleService(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var controller: StepCounterController

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "step_counter_channel"
        private const val NOTIFICATION_ID = 3
        private const val PENDING_INTENT_ID = 5
    }

    override fun onCreate() {
        super.onCreate()
        val notificationChannel = createNotificationChannel()
        registerNotificationChannel(notificationChannel)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        registerStepCounter(sensorManager)

        val application = application as HydroFitApplication
        val sharedPreferences = application.sharedPreferences
        controller = StepCounterController(
            lifecycleScope,
            application.currentDate,
            sharedPreferences
        )

        val notification = createNotification(controller.stats.value, sharedPreferences)
        startForeground(NOTIFICATION_ID, notification)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                controller.stats.collect {
                    val edit = sharedPreferences?.edit()
                    edit?.putInt(ARG_STEPS, it)
                    edit?.apply()
                    val updatedNotification = createNotification(it, sharedPreferences)
                    notificationManager.notify(NOTIFICATION_ID, updatedNotification)
                }
            }
        }
    }

    private fun createNotification(
        steps: Int,
        sharedPreferences: SharedPreferences?
    ): Notification {
        val stats = sharedPreferences?.getString(ARG_STATS, null)?.toStats()
        val goal = sharedPreferences?.getInt(ARG_GOAL_STEPS, 10000) ?: 10000
        val title = resources.getQuantityString(R.plurals.step_count, steps, steps)
        val progress = if (goal == 0) 0.0 else (steps.toDouble() * 100 / goal)
        val formattedProgress = String.format("%.2f", progress)
        val distanceTravelled =
            stats?.let { getDistanceTravelled(steps, it.stepLength) } ?: 0
        val content =
            "${distanceTravelled.toInt()} km · $formattedProgress% of your daily goal"

        return NotificationCompat.Builder(this@StepsService, NOTIFICATION_CHANNEL_ID)
            .setContentIntent(launchApplicationPendingIntent)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setOnlyAlertOnce(true)
            .setContentText(content)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .build()
    }

    private val launchApplicationPendingIntent
        get(): PendingIntent {
            val intent = Intent(applicationContext, MainActivity::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getActivity(this, PENDING_INTENT_ID, intent, flags)
        }

    private fun registerStepCounter(sensorManager: SensorManager) {
        val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val eventStepCount = it.values[0].toInt()
            controller.onStepCountChanged(eventStepCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    private fun createNotificationChannel(): NotificationChannel {
        val name = getString(R.string.step_counter_channel)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        return NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            setShowBadge(false)
        }
    }

    private fun registerNotificationChannel(channel: NotificationChannel) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getDistanceTravelled(steps: Int, stepLength: Int) = run {
        val distanceCentimeters = steps * stepLength
        distanceCentimeters.toDouble() / 100_000
    }
}