package com.example.gozizigo.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.launch

@Composable
fun AnimatedWaveform(amplitude: Float, active: Boolean) {

    val bars = 20
    val animBars = remember { List(bars) { Animatable(10f) } }

    LaunchedEffect(amplitude, active) {
        if (active) {
            animBars.forEach { anim ->
                launch {
                    anim.animateTo(
                        targetValue = 10f + amplitude * 8f,
                        animationSpec = tween(80)
                    )
                }
            }
        } else {
            animBars.forEach { anim ->
                launch {
                    anim.animateTo(10f, tween(200))
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        animBars.forEach { bar ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(6.dp)
                    .height(bar.value.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
