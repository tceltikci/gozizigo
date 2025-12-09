package com.example.gozizigo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography

private val LightColors = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = White,
    background = White,
    onBackground = MidnightNavy,
    secondary = AquaMint
)

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = White,
    background = MidnightNavy,
    onBackground = White,
    secondary = AquaMint
)

@Composable
fun GoZizigoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
