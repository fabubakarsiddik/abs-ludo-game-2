package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VibrantIndigo600,
    secondary = VibrantYellow400,
    tertiary = VibrantGreen500,
    background = VibrantSlate900,
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantIndigo600,
    secondary = VibrantYellow400,
    tertiary = VibrantGreen500,
    background = VibrantCanvasBg,
    surface = VibrantSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = VibrantSlate900,
    onSurface = VibrantSlate900,
    surfaceVariant = VibrantSlate100
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to the requested Vibrant Palette theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
