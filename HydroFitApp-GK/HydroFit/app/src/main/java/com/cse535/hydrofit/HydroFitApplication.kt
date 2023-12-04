package com.cse535.hydrofit

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import com.cse535.hydrofit.network.HydroFitAPIService
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate


// PLACEHOLDER
private const val BASE_API = "https://hydrofit"

const val ARG_TODAYS_DATE = "arg_today_date"

class HydroFitApplication : Application() {

    var hydrofitAPI: HydroFitAPIService? = null

    val currentDate = MutableStateFlow<LocalDate>(LocalDate.now())

    var sharedPreferences: SharedPreferences? = null

    override fun onCreate() {
        super.onCreate()
        registerMidnightTimer()

        val retrofit =
            Retrofit.Builder().baseUrl(BASE_API).addConverterFactory(GsonConverterFactory.create())
                .build()

        hydrofitAPI = retrofit.create(HydroFitAPIService::class.java)

        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
    }

    private fun registerMidnightTimer() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(midnightBroadcastReceiver, intentFilter)
    }

    private val midnightBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val today = LocalDate.now()

            if (today != currentDate.value) {
                currentDate.value = today
            }
        }
    }
}