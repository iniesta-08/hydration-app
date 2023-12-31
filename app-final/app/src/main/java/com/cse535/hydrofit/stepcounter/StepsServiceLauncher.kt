package com.cse535.hydrofit.stepcounter

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class StepsServiceLauncher : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.run {
            if (intent?.action == Intent.ACTION_BOOT_COMPLETED && hasPermissions(context)) {
                val launchIntent = Intent(applicationContext, StepsService::class.java)
                ContextCompat.startForegroundService(applicationContext, launchIntent)
            }
        }
    }

    private fun hasPermissions(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
    }

    @Suppress("SameParameterValue")
    private fun hasPermission(context: Context, permission: String): Boolean {
        val status = ContextCompat.checkSelfPermission(context, permission)
        return status == PackageManager.PERMISSION_GRANTED
    }
}