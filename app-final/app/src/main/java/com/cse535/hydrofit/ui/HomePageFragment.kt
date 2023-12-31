package com.cse535.hydrofit.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.futured.donut.DonutSection
import com.cse535.hydrofit.ARG_GOAL_STEPS
import com.cse535.hydrofit.ARG_GOAL_WATER
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.R
import com.cse535.hydrofit.databinding.FragmentHomePageBinding

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomePageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnShowNotification.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_fitnessLogs)
        }
    }

    override fun onResume() {
        super.onResume()
        val application = requireActivity().application as HydroFitApplication
        val sharedPreferences = application.sharedPreferences
        val fitnessLevel = sharedPreferences?.getInt(ARG_FITNESS_VAL, 0) ?: 0
        val hydrationLevel = sharedPreferences?.getInt(ARG_HYDRATION_VAL, 0) ?: 0

        val color1 = if (fitnessLevel < 25) {
            Color.RED
        } else if (fitnessLevel < 50) {
            Color.YELLOW
        } else if (fitnessLevel < 75) {
            Color.parseColor("#FFA500")
        } else {
            Color.GREEN
        }

        val color2 = if (hydrationLevel < 2.5) {
            Color.RED
        } else if (hydrationLevel < 5) {
            Color.YELLOW
        } else if (hydrationLevel < 7.5) {
            Color.parseColor("#FFA500")
        } else {
            Color.GREEN
        }

        val goalWater = sharedPreferences?.getInt(ARG_GOAL_WATER, 125) ?: 125
        val goalSteps = sharedPreferences?.getInt(ARG_GOAL_STEPS, 10000) ?: 10000

        binding.fitnessAlertImg.visibility = if (fitnessLevel < 5) View.VISIBLE else View.GONE
        binding.WaterAlertImg.visibility = if (hydrationLevel < 50) View.VISIBLE else View.GONE

        binding.textLine1.text = "$goalWater Oz"
        binding.textLine3.text = "$goalSteps Steps"

        binding.textLine2.text = "${hydrationLevel * 10} %"
        binding.textLine4.text = "$fitnessLevel %"

        val section1 = DonutSection(
            name = "section_1",
            color = color1,
            amount = fitnessLevel.toFloat()
        )

        val section2 = DonutSection(
            name = "section_2",
            color = Color.WHITE,
            amount = (100 - fitnessLevel).toFloat()
        )

        binding.imageButton2.cap = 100f
        binding.imageButton2.submitData(listOf(section1, section2))

        val section3 = DonutSection(
            name = "section_1",
            color = color2,
            amount = hydrationLevel.toFloat()
        )

        val section4 = DonutSection(
            name = "section_2",
            color = Color.WHITE,
            amount = (10 - hydrationLevel).toFloat()
        )

        binding.imageButton1.cap = 10f
        binding.imageButton1.submitData(listOf(section3, section4))

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}