package com.example.gozizigo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.gozizigo.MainActivity
import com.example.gozizigo.model.Restaurant
import kotlinx.coroutines.delay

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch

val mockRestaurants = listOf(
    Restaurant(
        name = "Dönerci Ali Usta",
        distance = "250 m",
        imageUrl = "",
        lat = 37.048590269225514,   // Ortakent Center
        lng = 27.34792625047488,
        routePoints = emptyList()
    ),
    Restaurant(
        name = "Can Baba Döner",
        distance = "420 m",
        imageUrl = "",
        lat = 37.0345,   // Midtown AVM area
        lng = 27.3550,
        routePoints = emptyList()
    ),
    Restaurant(
        name = "Hatay Döner Sofrası",
        distance = "600 m",
        imageUrl = "",
        lat = 37.0195,   // Yahşi Beach area
        lng = 27.3580,
        routePoints = emptyList()
    ),
    Restaurant(
        name = "Mega Döner",
        distance = "750 m",
        imageUrl = "",
        lat = 37.0270,   // Marina side
        lng = 27.3660,
        routePoints = emptyList()
    ),
    Restaurant(
        name = "Döner Planet",
        distance = "900 m",
        imageUrl = "",
        lat = 37.0300,   // Random Ortakent street
        lng = 27.3620,
        routePoints = emptyList()
    )
)




@Composable
fun GoZizigoMainScreen(activity: MainActivity) {

    var isListening by remember { mutableStateOf(false) }
    var amplitude by remember { mutableStateOf(0f) }
    var partialText by remember { mutableStateOf("") }
    var finalText by remember { mutableStateOf("") }

    // LIVE USER LOCATION STATE
    var userLat by remember { mutableStateOf(0.0) }
    var userLng by remember { mutableStateOf(0.0) }
    var locationEnabled by remember { mutableStateOf(false) }

    val locationState = activity.locationFlow.collectAsState() // <-- NEW

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationEnabled = granted
    }

    LaunchedEffect(finalText) {
        if (finalText.isNotEmpty()) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(locationEnabled) {
        if (locationEnabled &&
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            activity.startLocationUpdates() // <-- FIXED
        }
    }

// Update UI when location changes
    LaunchedEffect(locationState.value) {
        val loc = locationState.value
        userLat = loc.lat
        userLng = loc.lng
    }







    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListening = true
            activity.startListening()
        }
    }

    // Attach callbacks from MainActivity
    LaunchedEffect(Unit) {

        activity.amplitudeCallback = { amp ->
            amplitude = amp
        }

        activity.partialResultCallback = { text ->
            partialText = text
        }

        activity.finalResultCallback = { text ->
            finalText = text
            isListening = false
        }

        activity.voiceStateCallback = { state ->
            when (state) {
                is com.example.gozizigo.VoiceState.Start -> isListening = true
                is com.example.gozizigo.VoiceState.Finished -> isListening = false
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (finalText.isEmpty()) {

            // 🎙️ MIC SCREEN
            AnimatedWaveform(amplitude = amplitude, active = isListening)

            Spacer(Modifier.height(20.dp))

            when {
                partialText.isNotEmpty() -> {
                    Text(
                        text = partialText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                else -> {
                    Text(
                        text = "Press and hold to speak…",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            PulsingGlow(isListening = isListening) {
                BigMicButton(
                    isListening = isListening,
                    onStart = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onStop = { activity.stopListening() }
                )
            }

        } else {

            // 🍽️ FULL-SCREEN RESTAURANTS
            Box(Modifier.fillMaxSize()) {

                RestaurantFullScreenPager(
                    restaurants = mockRestaurants,
                    userLat = userLat,
                    userLng = userLng
                )


                // ⬅️ BACK BUTTON (TOP-LEFT)
                IconButton(
                    onClick = {
                        finalText = ""
                        partialText = ""
                        amplitude = 0f
                        isListening = false
                    },
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopStart)
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

    }

}

@Composable
fun PulsingGlow(
    isListening: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Increase radius + fade out opacity
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "pulse-scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "pulse-alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }

        Box(content = content)
    }
}

@Composable
fun BigMicButton(
    isListening: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val activeColor = MaterialTheme.colorScheme.secondary
    val idleColor = MaterialTheme.colorScheme.primary
    val bgColor = if (isListening) activeColor else idleColor

    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(CircleShape)
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        val startTime = System.currentTimeMillis()

                        onStart()  // start recording on press down

                        val released = tryAwaitRelease()
                        pressed = false

                        val pressDuration =
                            System.currentTimeMillis() - startTime

                        if (released) {
                            if (pressDuration < 150) {
                                // SHORT TAP → 5-second auto recording
                                scope.launch {
                                    delay(5000)
                                    onStop()
                                }
                            } else {
                                // HOLD → stop when released
                                onStop()
                            }
                        } else {
                            // If gesture cancelled unexpectedly → stop
                            onStop()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(120.dp)
        )
    }
}
