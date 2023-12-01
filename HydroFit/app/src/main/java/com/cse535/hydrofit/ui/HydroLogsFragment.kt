package com.cse535.hydrofit.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.R
import com.cse535.hydrofit.databinding.FragmentHydroLogsBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val ARG_HYDRATION_VAL = "arg_hydration_val"

class HydroLogsFragment : Fragment() {

    private var _binding: FragmentHydroLogsBinding? = null
    private val binding get() = _binding!!

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

        binding.submitButton.setOnClickListener {
            val application = requireActivity().application as HydroFitApplication
            val sharedPreferences = application.sharedPreferences
            lifecycleScope.launch {

                try {
                    //TODO()
                    /**
                    val fitnessLevels = sharedPreferences?.getInt(ARG_FITNESS_VAL, 0) ?: 0
                    val hydroRequest = HydrationRequest(
                    timeSinceLastDrink = binding.editTextTimeSinceLastDrink.text.toString()
                    .toInt(),
                    lastDrinkAmount = binding.editTextAmountOfLastDrink.text.toString().toInt(),
                    fitnessLevels = fitnessLevels

                    )
                    val hydrofitAPI = application.hydrofitAPI
                    val hydration = withContext(Dispatchers.IO) {
                    hydrofitAPI?.getHydration(hydroRequest)
                    }
                     **/

                    val hydrationDeferred = CompletableDeferred<Float>()

                    withContext(Dispatchers.IO) {
                        // Simulate a response value out of 100
                        hydrationDeferred.complete(HYDRATION_METER)
                    }

                    // Wait for the deferred result
                    val fitness = hydrationDeferred.await()

                    fitness.also {
                        sharedPreferences?.edit()?.apply {
                            putFloat(ARG_HYDRATION_VAL, it)
                            apply()
                        }
                        if (!findNavController().popBackStack(R.id.homePageFragment, false)) {
                            findNavController().navigate(R.id.action_hydro_logs_to_home_page)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ERROR", e.toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}