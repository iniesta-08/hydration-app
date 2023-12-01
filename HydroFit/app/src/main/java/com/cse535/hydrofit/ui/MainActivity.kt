package com.cse535.hydrofit.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.NotificationScheduler
import com.cse535.hydrofit.R
import com.cse535.hydrofit.databinding.ActivityMainBinding
import com.cse535.hydrofit.stepcounter.StepsService

const val FITNESS_METER = 2f
const val HYDRATION_METER = 77f

const val ARG_ALARMS_SET = "arg_alarms_set"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.homePageFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        startStepCounterService()
        handleDeepLink(intent)
        val application = application as HydroFitApplication
        val sharedPreferences = application.sharedPreferences
        val alarmsSet = sharedPreferences?.getBoolean(ARG_ALARMS_SET, false) ?: false
        if (!alarmsSet) {
            val edit = sharedPreferences?.edit()
            edit?.putBoolean(ARG_ALARMS_SET, true);
            edit?.apply()
            val notificationScheduler = NotificationScheduler()
            notificationScheduler.scheduleNotifications(this)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri: Uri? = intent.data
            uri?.let {
                val path: String? = it.path
                if ("/logs" == path) {
                    navigateToLogs()
                }
            }
        }
    }

    private fun navigateToLogs() {
        navController.navigate(R.id.fitnessLogsFragment)
    }

    private fun startStepCounterService() {
        val intent = Intent(this, StepsService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val currentDestination = navController.currentDestination
                if (currentDestination != null && currentDestination.id != R.id.info_Fragment) {
                    // Navigate to the ProfileFragment using NavController
                    navController.navigate(R.id.info_Fragment)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}