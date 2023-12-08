package com.cse535.hydrofit.stepcounter

import android.content.SharedPreferences
import com.cse535.hydrofit.ARG_TODAYS_DATE
import com.cse535.hydrofit.ui.ARG_EXERCISE_DURATION
import com.cse535.hydrofit.ui.ARG_WATER_CONSUMED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

const val ARG_STEPS = "arg_steps"

class StepCounterController(
    coroutineScope: CoroutineScope,
    currentDateFlow: MutableStateFlow<LocalDate>,
    sharedPreferences: SharedPreferences?
) {

    private val _stats = MutableStateFlow(0)
    val stats: StateFlow<Int> = _stats.asStateFlow()

    init {
        coroutineScope.launch {
            val date = sharedPreferences?.getString(ARG_TODAYS_DATE, "")

            currentDateFlow.collect {
                if (date != it.toString()) {
                    _stats.value = 0
                    val edit = sharedPreferences?.edit()
                    edit?.putString(ARG_TODAYS_DATE, it.toString())
                    edit?.putInt(ARG_WATER_CONSUMED, 0)
                    edit?.putInt(ARG_EXERCISE_DURATION, 0)
                    edit?.apply()
                }
            }
        }
    }

    private val rawStepSensorReadings = MutableStateFlow(0)
    private var previousStepCount: Int? = null

    init {
        rawStepSensorReadings.drop(1).onEach { event ->
            val stepCountDifference = event - (previousStepCount ?: event)
            previousStepCount = event
            _stats.value += stepCountDifference
        }.launchIn(coroutineScope)
    }

    fun onStepCountChanged(newStepCount: Int) {
        rawStepSensorReadings.value = newStepCount
    }
}
