package com.yourpackage.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// -------------------------------
// BRAND COLORS
// -------------------------------
val ElectricBlue = Color(0xFF4A90E2)
val MidnightNavy = Color(0xFF1A1F36)
val AquaMint = Color(0xFF2DE3C6)
val SoftGray = Color(0xFFF5F7FA)
val PureWhite = Color(0xFFFFFFFF)

// -------------------------------
// LIGHT COLOR SCHEME
// -------------------------------
private val LightColors = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = PureWhite,

    primaryContainer = ElectricBlue,
    onPrimaryContainer = PureWhite,

    secondary = AquaMint,
    onSecondary = MidnightNavy,

    background = PureWhite,
    onBackground = MidnightNavy,

    surface = PureWhite,
    onSurface = MidnightNavy,

    tertiary = AquaMint,
)

// -------------------------------
// DARK COLOR SCHEME
// -------------------------------
private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = PureWhite,

    secondary = AquaMint,
    onSecondary = PureWhite,

    background = MidnightNavy,
    onBackground = PureWhite,

    surface = MidnightNavy,
    onSurface = PureWhite,

    tertiary = AquaMint,
)

// -------------------------------
// THEME WRAPPER
// -------------------------------
@Composable
fun GoZizigoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme =
        if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
