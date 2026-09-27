package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VoiceHubDarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF321A6D),
    onPrimaryContainer = NeonViolet,
    secondary = NeonEmerald,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D28),
    onSecondaryContainer = Color(0xFFB9F6CA),
    tertiary = SoftCyan,
    onTertiary = Color.Black,
    background = DeepMidnight,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = CoralRose
)

private val VoiceHubLightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7F6),
    onPrimaryContainer = Color(0xFF4A148C),
    secondary = Color(0xFF00A859),
    onSecondary = Color.White,
    background = Color(0xFFF6F5FC),
    onBackground = Color(0xFF1B1B2F),
    surface = Color.White,
    onSurface = Color(0xFF1B1B2F),
    surfaceVariant = Color(0xFFECEBFA),
    onSurfaceVariant = Color(0xFF535075),
    outline = Color(0xFFD0CDED)
)

@Composable
fun VoiceHubTheme(
    darkTheme: Boolean = true, // Default to stunning dark theme for audio lounge
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) VoiceHubDarkColorScheme else VoiceHubLightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
