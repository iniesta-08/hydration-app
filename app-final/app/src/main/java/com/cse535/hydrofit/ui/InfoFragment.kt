package com.cse535.hydrofit.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cse535.hydrofit.ARG_FCM_TOKEN
import com.cse535.hydrofit.ARG_GOAL_STEPS
import com.cse535.hydrofit.ARG_GOAL_WATER
import com.cse535.hydrofit.Gender
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.R
import com.cse535.hydrofit.Stats
import com.cse535.hydrofit.User
import com.cse535.hydrofit.databinding.FragmentInfoBinding
import com.cse535.hydrofit.network.SaveDeviceTokenRequest
import com.cse535.hydrofit.toJson
import com.cse535.hydrofit.toStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val ARG_STATS = "stats"
const val ARG_PROFILE_SHOWN = "arg_profile_shown"

class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application as HydroFitApplication
        val sharedPreferences = application.sharedPreferences
        val stats = sharedPreferences?.let { getStatsFromSharedPreferences(it) }
        binding.editTextEmail.setText(stats?.username ?: "")
        binding.editTextAge.setText(stats?.age?.toString() ?: "")
        binding.editTextName.setText(stats?.name ?: "")
        binding.editTextHeight.setText(stats?.height?.toString() ?: "")
        binding.editTextWeight.setText(stats?.weight?.toString() ?: "")
        binding.editTextStepLength.setText(stats?.stepLength?.toString() ?: "")
        stats?.let {
            binding.radioGroupGender.check(if (it.gender == Gender.MALE) R.id.radioButtonMale else R.id.radioButtonFemale)
        }


        binding.submitButton.setOnClickListener {
            val newStats = stats ?: Stats()
            if (sharedPreferences != null) {
                saveStatsToSharedPreferences(
                    sharedPreferences, newStats.copy(
                        username = binding.editTextEmail.text.toString(),
                        name = binding.editTextName.text.toString(),
                        height = binding.editTextHeight.text.toString().toInt(),
                        weight = binding.editTextWeight.text.toString().toInt(),
                        age = binding.editTextAge.text.toString().toInt(),
                        stepLength = binding.editTextStepLength.text.toString().toInt()
                    )
                )
                val editor = sharedPreferences.edit()
                editor?.putBoolean(ARG_PROFILE_SHOWN, true)

                val goalWater = stats?.let {
                    (it.weight.toDouble() * 2) / 3
                } ?: 125.0

                val goalSteps = stats?.let {
                    (it.weight.toDouble() / it.height) * 10000
                } ?: 10000.0

                editor?.putInt(ARG_GOAL_WATER, goalWater.toInt())
                editor?.putInt(ARG_GOAL_STEPS, goalSteps.toInt())
                editor?.apply()
            }

            lifecycleScope.launch {

                try {
                    //asyncOperation
                    val user = User(binding.editTextEmail.text.toString())
                    val op =
                        withContext(Dispatchers.IO) { application.hydrofitAPI?.createUser(user) }
                    val token = sharedPreferences?.getString(ARG_FCM_TOKEN, "").orEmpty()
                    val currentTime = LocalDateTime.now()
                    // Define the desired format
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    // Format the current time using the formatter
                    val timestamp = currentTime.format(formatter)
                    withContext(Dispatchers.IO) {
                        application.hydrofitAPI?.saveDeviceToken(
                            userName = user.username,
                            SaveDeviceTokenRequest(timestamp = timestamp, deviceToken = token)
                        )
                    }
                    op?.also {
                        //ui operation
                        Log.e("API", it.userName.orEmpty())
                        if (!findNavController().popBackStack(R.id.homePageFragment, false)) {
                            findNavController().navigate(R.id.action_info_to_home)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "API ERROR", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveStatsToSharedPreferences(
        sharedPreferences: SharedPreferences,
        stats: Stats
    ) {
        val statsJson = stats.toJson()

        val editor = sharedPreferences.edit()
        editor.putString(ARG_STATS, statsJson)
        editor.apply()
    }

    private fun getStatsFromSharedPreferences(sharedPreferences: SharedPreferences): Stats? {

        return sharedPreferences.getString(ARG_STATS, null)?.toStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}