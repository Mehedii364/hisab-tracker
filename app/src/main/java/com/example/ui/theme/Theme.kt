package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NeonDarkColorScheme = darkColorScheme(
    primary = NeonWhiteSolid,
    onPrimary = NeonBlackBg,
    primaryContainer = NeonSurfaceHover,
    onPrimaryContainer = NeonTextPrimary,
    secondary = NeonTextSecondary,
    onSecondary = NeonBlackBg,
    secondaryContainer = NeonSurfaceCard,
    onSecondaryContainer = NeonTextPrimary,
    tertiary = Red500,
    background = NeonBlackBg,
    surface = NeonSurface,
    surfaceVariant = NeonSurfaceCard,
    onBackground = NeonTextPrimary,
    onSurface = NeonTextPrimary,
    onSurfaceVariant = NeonTextMuted,
    outline = NeonBorder
)

private val NeonLightColorScheme = lightColorScheme(
    primary = NeonBlackBg,
    onPrimary = NeonWhiteSolid,
    primaryContainer = NeonSurfaceHover,
    onPrimaryContainer = NeonTextPrimary,
    secondary = NeonSurfaceCard,
    onSecondary = NeonTextPrimary,
    secondaryContainer = NeonSurface,
    onSecondaryContainer = NeonTextPrimary,
    tertiary = Red500,
    background = NeonBlackBg,
    surface = NeonSurface,
    surfaceVariant = NeonSurfaceCard,
    onBackground = NeonTextPrimary,
    onSurface = NeonTextPrimary,
    onSurfaceVariant = NeonTextMuted,
    outline = NeonBorder
)

@Composable
fun HisabTrackerTheme(
    darkTheme: Boolean = true, // Default to Futuristic Neon Black
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NeonDarkColorScheme else NeonLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
