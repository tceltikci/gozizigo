package com.example.gozizigo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch

@Composable
fun GoZizigoMainScreen(
    startListening: ((String) -> Unit) -> Unit
) {
    var isListening by remember { mutableStateOf(false) }
    var textResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔥 Animated waveform
        AnimatedWaveform(isListening)

        Spacer(Modifier.height(32.dp))

        Text(
            text = if (isListening) "Listening…" else "Say something for GoZizigo",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(32.dp))

        MicButton(
            isActive = isListening,
            onClick = {
                isListening = true
                startListening { spokenText ->
                    textResult = spokenText
                    isListening = false
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        if (textResult.isNotEmpty()) {
            Text(
                text = "You said: $textResult",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun WaveformLogo(isListening: Boolean) {

    val height = if (isListening) 60f else 40f
    val color = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.size(200.dp)) {
        val points = listOf(
            Offset(20f, 100f),
            Offset(60f, 100f - height),
            Offset(100f, 100f),
            Offset(140f, 100f - height),
            Offset(180f, 100f),
            Offset(220f, 100f - height),
            Offset(260f, 100f)
        )

        for (i in 0 until points.size - 1) {
            drawLine(
                color = color,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 14f
            )
        }
    }
}

@Composable
fun MicButton(isActive: Boolean, onClick: () -> Unit) {

    val color = if (isActive)
        MaterialTheme.colorScheme.secondary
    else
        MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = "Mic",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun AnimatedWaveform(isListening: Boolean) {
    val barCount = 20
    val animatedValues = remember { List(barCount) { Animatable(0f) } }

    // When listening state changes, animate bars
    LaunchedEffect(isListening) {
        if (isListening) {
            animatedValues.forEach { bar ->
                launch {
                    while (true) {
                        bar.animateTo(
                            targetValue = (20..100).random().toFloat(),
                            animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                        )
                        bar.animateTo(
                            targetValue = 10f,
                            animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                        )
                    }
                }
            }
        } else {
            // Reset animation when not listening
            animatedValues.forEach { bar ->
                launch {
                    bar.animateTo(10f, tween(300))
                }
            }
        }
    }

    // Draw waveform
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        animatedValues.forEach { anim ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(6.dp)
                    .height(anim.value.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
