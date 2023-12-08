package com.cse535.hydrofit.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.R
import com.cse535.hydrofit.databinding.FragmentPermissionsBinding

private var PERMISSIONS_REQUIRED = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.ACTIVITY_RECOGNITION
)

/**
 * This [Fragment] requests permissions and, once granted, it will navigate to the next fragment
 */
class PermissionsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS_REQUIRED = PERMISSIONS_REQUIRED.plus(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!hasPermissions(requireContext())) {
            // Request camera-related permissions
            activityResultLauncher.launch(PERMISSIONS_REQUIRED)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentPermissionsBinding.inflate(inflater, container, false).also {
            if (hasPermissions(requireContext())) {
                navigateAhead()

            } else {
                Log.e(
                    PermissionsFragment::class.java.simpleName,
                    "Re-requesting permissions ..."
                )
                activityResultLauncher.launch(PERMISSIONS_REQUIRED)
            }
        }.root
    }

    companion object {
        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in PERMISSIONS_REQUIRED && !it.value)
                    permissionGranted = false
            }
            if (permissionGranted && permissions.isNotEmpty()) {
                navigateAhead()
            }
            if (!permissionGranted) {
                Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun navigateAhead() {
        lifecycleScope.launchWhenStarted {
            val application = requireActivity().application as HydroFitApplication
            val sharedPreferences = application.sharedPreferences
            if (sharedPreferences?.getBoolean(ARG_PROFILE_SHOWN, false) == true) {
                findNavController().navigate(R.id.action_permissions_to_home)
            } else {
                findNavController().navigate(R.id.action_permissions_to_info)
            }

        }
    }
}