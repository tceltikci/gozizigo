package com.example.gozizigo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.gozizigo.ui.screens.GoZizigoMainScreen
import com.example.gozizigo.ui.screens.DirectionsScreen
import com.example.gozizigo.ui.theme.GoZizigoTheme
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow

// Voice recognition states
sealed class VoiceState {
    object Ready : VoiceState()
    object Start : VoiceState()
    object Finished : VoiceState()
    data class Error(val code: Int) : VoiceState()
}

// Location model for live navigation
data class LocationModel(val lat: Double, val lng: Double)

class MainActivity : ComponentActivity() {

    // ===== Voice Recognition =====
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent

    var amplitudeCallback: ((Float) -> Unit)? = null
    var partialResultCallback: ((String) -> Unit)? = null
    var finalResultCallback: ((String) -> Unit)? = null
    var voiceStateCallback: ((VoiceState) -> Unit)? = null

    // ===== Navigation =====
    lateinit var navController: NavHostController

    // ===== Live Location Flow =====
    val locationFlow = MutableStateFlow(LocationModel(0.0, 0.0))
    private lateinit var locationRequest: LocationRequest

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            locationFlow.value = LocationModel(loc.latitude, loc.longitude)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== Initialize Speech Recognizer =====
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                voiceStateCallback?.invoke(VoiceState.Ready)
            }

            override fun onBeginningOfSpeech() {
                voiceStateCallback?.invoke(VoiceState.Start)
            }

            override fun onRmsChanged(rmsdB: Float) {
                amplitudeCallback?.invoke(rmsdB.coerceIn(0f, 12f))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResultCallback?.invoke(
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
                )
            }

            override fun onResults(results: Bundle?) {
                finalResultCallback?.invoke(
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
                )
                voiceStateCallback?.invoke(VoiceState.Finished)
            }

            override fun onError(error: Int) {
                voiceStateCallback?.invoke(VoiceState.Error(error))
            }

            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // ===== COMPOSE UI WITH NAVIGATION =====
        setContent {
            GoZizigoTheme {

                navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {

                    // Home screen
                    composable("main") {
                        GoZizigoMainScreen(this@MainActivity)
                    }

                    // Directions screen
                    composable(
                        "directions/{lat}/{lng}",
                        arguments = listOf(
                            navArgument("lat") { type = NavType.FloatType },
                            navArgument("lng") { type = NavType.FloatType }
                        )
                    ) { entry ->
                        val lat = entry.arguments!!.getFloat("lat").toDouble()
                        val lng = entry.arguments!!.getFloat("lng").toDouble()

                        DirectionsScreen(
                            restaurantLat = entry.arguments!!.getDouble("lat"),
                            restaurantLng = entry.arguments!!.getDouble("lng"),
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    // ===== Voice control =====
    fun startListening() = speechRecognizer.startListening(recognizerIntent)
    fun stopListening() = speechRecognizer.stopListening()

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    // ===== Location updates =====
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {

        val fused = LocationServices.getFusedLocationProviderClient(this)

        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                locationFlow.value = LocationModel(loc.latitude, loc.longitude)
            }
        }

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).build()

        fused.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }
}
