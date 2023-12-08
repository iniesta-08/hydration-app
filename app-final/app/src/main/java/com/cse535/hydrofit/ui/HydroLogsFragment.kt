package com.cse535.hydrofit.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.R
import com.cse535.hydrofit.databinding.FragmentHydroLogsBinding
import com.cse535.hydrofit.network.HydrationRequest
import com.cse535.hydrofit.toStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

const val ARG_HYDRATION_VAL = "arg_hydration_val"
const val ARG_WATER_CONSUMED = "arg_water_consumed"

class HydroLogsFragment : Fragment() {

    private var _binding: FragmentHydroLogsBinding? = null
    private val binding get() = _binding!!

    var selectedTime: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydroLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the 24-hour view for the TimePicker
        binding.timePicker.setIs24HourView(true)

        // Set up the OnTimeChangedListener
        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            // Convert the selected time to a formatted string
            selectedTime = formatTime(hourOfDay, minute, 0)
            // Use the selectedTime string as needed
            // (e.g., display it in a TextView, pass it to an API, etc.)
        }

        binding.submitButton.setOnClickListener {
            val application = requireActivity().application as HydroFitApplication
            val sharedPreferences = application.sharedPreferences
            lifecycleScope.launch {

                try {
                    val fitnessLevels = sharedPreferences?.getInt(ARG_FITNESS_VAL, 0) ?: 0
                    var waterConsumed = sharedPreferences?.getInt(ARG_WATER_CONSUMED, 0) ?: 0
                    waterConsumed += binding.editTextAmountOfLastDrink.text.toString().toInt()
                    val currentTime = LocalDateTime.now()
                    // Define the desired format
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    // Format the current time using the formatter
                    val timestamp = currentTime.format(formatter)
                    val hydroRequest = HydrationRequest(
                        timestamp = timestamp,
                        lastdrinkTime = selectedTime.orEmpty(),
                        wateramount = waterConsumed
                    )
                    val hydrofitAPI = application.hydrofitAPI
                    val username =
                        sharedPreferences?.getString(ARG_STATS, null)?.toStats()?.username
                    val hydration = withContext(Dispatchers.IO) {
                        hydrofitAPI?.getHydration(username, hydroRequest)
                    }

                    hydration?.also {
                        sharedPreferences?.edit()?.apply {
                            putInt(ARG_HYDRATION_VAL, it.hydration)
                            putInt(ARG_WATER_CONSUMED, waterConsumed)
                            apply()
                        }
                        if (!findNavController().popBackStack(R.id.homePageFragment, false)) {
                            findNavController().navigate(R.id.action_hydro_logs_to_home_page)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "API ERROR", Toast.LENGTH_SHORT).show()
                    Log.e("ERROR", e.toString())
                }
            }
        }
    }

    private fun formatTime(hour: Int, minute: Int, second: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)

        val simpleDateFormat = SimpleDateFormat("HH-mm-ss", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}