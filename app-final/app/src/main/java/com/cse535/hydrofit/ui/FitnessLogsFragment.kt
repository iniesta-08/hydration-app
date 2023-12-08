package com.cse535.hydrofit.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cse535.hydrofit.HydroFitApplication
import com.cse535.hydrofit.R
import com.cse535.hydrofit.databinding.FragmentFitnessLogsBinding
import com.cse535.hydrofit.network.FitnessRequest
import com.cse535.hydrofit.stepcounter.ARG_STEPS
import com.cse535.hydrofit.toStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs

const val ARG_FITNESS_VAL = "arg_fitness_val"
const val ARG_EXERCISE_DURATION = "arg_exercise_duration"

class FitnessLogsFragment : Fragment() {

    private var _binding: FragmentFitnessLogsBinding? = null
    private val binding get() = _binding!!

    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(requireContext()) }
    private var currentRecording: Recording? = null
    private lateinit var videoCapture: VideoCapture<Recorder>
    private lateinit var camera: Camera

    private lateinit var countDownTimer: CountDownTimer
    private var isCountdownRunning = false
    private var rate = 0
    private var heartRate: TextView? = null
    private var progress: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFitnessLogsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        heartRate = binding.editTextHeartRate
        progress = binding.progressBar

        val application = requireActivity().application as HydroFitApplication
        val sharedPreferences = application.sharedPreferences
        val steps = sharedPreferences?.getInt(ARG_STEPS, 0) ?: 0
        binding.editTextSteps.setText(steps.toString())

        binding.btnSubmit.setOnClickListener {
            lifecycleScope.launch {

                try {
                    var exerciseDuration = sharedPreferences?.getInt(ARG_EXERCISE_DURATION, 0) ?: 0
                    exerciseDuration += binding.editTextSteps.text.toString().toInt()

                    val hydrofitAPI = application.hydrofitAPI ?: return@launch
                    val currentTime = LocalDateTime.now()
                    // Define the desired format
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    // Format the current time using the formatter
                    val timestamp = currentTime.format(formatter)
                    val fitnessRequest = FitnessRequest(
                        timestamp = timestamp,
                        stepcount = exerciseDuration,
                        exercise = binding.editTextExerciseDuration.text.toString().toInt(),
                        heartrate = binding.editTextHeartRate.text.toString().toInt()
                    )
                    val username =
                        sharedPreferences?.getString(ARG_STATS, null)?.toStats()?.username
                    val fitness = withContext(Dispatchers.IO) {
                        hydrofitAPI.getFitness(username, fitnessRequest)
                    }

                    fitness.also {
                        sharedPreferences?.edit()?.apply {
                            putInt(ARG_FITNESS_VAL, it.fitness)
                            putInt(ARG_EXERCISE_DURATION, exerciseDuration)
                            apply()
                        }
                        findNavController().navigate(R.id.action_fitness_logs_to_hydro_logs)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "API ERROR", Toast.LENGTH_SHORT).show()
                    Log.e("ERROR", e.toString())
                }
            }
        }

        binding.btnRecord.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                bindCaptureUsecase()
                Toast.makeText(
                    requireContext(),
                    "Cover the camera with your Thumb completely for 1 minute",
                    Toast.LENGTH_SHORT
                ).show()
                startCountdown()
            }
        }
    }


    private val captureListener = Consumer<VideoRecordEvent> { event ->
        if (event is VideoRecordEvent.Finalize) {
            progress?.visibility = View.VISIBLE
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    calculateHeartRate(event.outputResults.outputUri)
                }.also {
                    progress?.visibility = View.GONE
                    heartRate?.text = rate.toString()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        camera.cameraControl.enableTorch(true)
        val name = "HydroFit" +
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        currentRecording = videoCapture.output
            .prepareRecording(requireActivity(), mediaStoreOutput)
            .start(mainThreadExecutor, captureListener)

        Log.i(TAG, "Recording started")
    }

    private fun stopRecording() {
        camera.cameraControl.enableTorch(false)
        if (currentRecording == null) {
            return
        }

        val recording = currentRecording
        if (recording != null) {
            recording.stop()
            currentRecording = null
        }
    }

    private suspend fun bindCaptureUsecase() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val preview = Preview.Builder()
            .build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.LOWEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                videoCapture,
                preview
            )
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun calculateHeartRate(params: Uri) {
        val retriever = MediaMetadataRetriever()
        val frameList = ArrayList<Bitmap>()
        var time: String? = null
        try {
            retriever.setDataSource(requireContext(), params)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
            val aduration = duration!!.toInt()
            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val a = aduration / 100
            var i = a
            while (i < aduration) {
                val bitmap = retriever.getFrameAtIndex(i)
                frameList.add(bitmap!!)
                i += a
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        } finally {
            retriever.release()
            var redBucket: Long
            var pixelCount: Long = 0
            val a = mutableListOf<Long>()
            for (i in frameList) {
                redBucket = 0
                val height = i.height
                val width = i.width
                for (y in height / 2 - 50 until height / 2 + 50) {
                    for (x in width / 2 - 50 until width / 2 + 50) {
                        if (x > width || y > height) break
                        val c: Int = i.getPixel(x, y)
                        pixelCount++
                        redBucket += Color.red(c) + Color.blue(c) + Color.green(c)
                    }
                }
                a.add(redBucket)
            }
            val b = mutableListOf<Long>()
            for (i in 0 until a.lastIndex - 5) {
                val temp = (a[i] + a[i + 1] + a[i + 2] + a[i + 3] + a[i + 4]) / 4
                b.add(temp)
            }
            if (b.isNotEmpty()) {
                var x = b.elementAt(0)
                var count = 0
                for (i in 1 until b.lastIndex) {
                    val p = b.elementAt(i)
                    if (abs(p - x) > 500) {
                        count++
                    }
                    x = b.elementAt(i)
                }
                val timeInSec = (time?.toLong() ?: 45000) / 1000
                rate = ((count.toFloat() / timeInSec) * 60).toInt()
            }
        }
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.countdownTextView.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                binding.countdownTextView.visibility = View.INVISIBLE
                lifecycleScope.launch {
                    startRecording()
                    delay(60000)
                    stopRecording()
                }

            }
        }

        binding.countdownTextView.visibility = View.VISIBLE
        countDownTimer.start()
        isCountdownRunning = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (isCountdownRunning) {
            countDownTimer.cancel()
        }
    }

    companion object {
        val TAG: String = FitnessLogsFragment::class.java.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}