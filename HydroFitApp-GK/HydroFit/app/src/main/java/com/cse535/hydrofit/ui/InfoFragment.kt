package com.cse535.hydrofit.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cse535.hydrofit.ARG_GOAL_STEPS
import com.cse535.hydrofit.ARG_GOAL_WATER
import com.cse535.hydrofit.Gender
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.R
import com.cse535.hydrofit.Stats
import com.cse535.hydrofit.databinding.FragmentInfoBinding
import com.cse535.hydrofit.toJson
import com.cse535.hydrofit.toStats

const val ARG_STATS = "stats"
const val ARG_PROFILE_SHOWN = "arg_profile_shown"

class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application as HydroFitApplication
        val sharedPreferences = application.sharedPreferences
        val stats = sharedPreferences?.let { getStatsFromSharedPreferences(it) }
        binding.editTextAge.setText(stats?.age?.toString() ?: "")
        binding.editTextName.setText(stats?.name ?: "")
        binding.editTextHeight.setText(stats?.height?.toString() ?: "")
        binding.editTextWeight.setText(stats?.weight?.toString() ?: "")
        binding.editTextPace.setText(stats?.pace?.toString() ?: "")
        binding.editTextStepLength.setText(stats?.stepLength?.toString() ?: "")
        stats?.let {
            binding.radioGroupGender.check(if (it.gender == Gender.MALE) R.id.radioButtonMale else R.id.radioButtonFemale)
        }


        binding.submitButton.setOnClickListener {
            val newStats = stats ?: Stats()
            if (sharedPreferences != null) {
                saveStatsToSharedPreferences(
                    sharedPreferences, newStats.copy(
                        name = binding.editTextName.text.toString(),
                        height = binding.editTextHeight.text.toString().toInt(),
                        weight = binding.editTextWeight.text.toString().toInt(),
                        age = binding.editTextAge.text.toString().toInt(),
                        pace = binding.editTextPace.text.toString().toInt(),
                        stepLength = binding.editTextStepLength.text.toString().toInt()
                    )
                )
                val editor = sharedPreferences.edit()
                editor?.putBoolean(ARG_PROFILE_SHOWN, true)
                val goalWater = stats?.let {
                    (it.weight * 2 )/ 3
                } ?: 125

                val goalSteps = stats?.let {
                    (it.weight / it.height) * 10000
                } ?: 10000

                editor?.putInt(ARG_GOAL_WATER, goalWater)
                editor?.putInt(ARG_GOAL_STEPS, goalSteps)
                editor?.apply()
            }
            if (!findNavController().popBackStack(R.id.homePageFragment, false)) {
                findNavController().navigate(R.id.action_info_to_home)
            }

        }

    }

    private fun saveStatsToSharedPreferences(sharedPreferences: SharedPreferences, stats: Stats) {
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