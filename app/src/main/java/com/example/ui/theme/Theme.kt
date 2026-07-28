package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363A),
    onPrimaryContainer = CyberCyan,
    secondary = CyberEmerald,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003816),
    onSecondaryContainer = CyberEmerald,
    tertiary = CyberPurple,
    onTertiary = Color.White,
    background = CyberBgDark,
    onBackground = CyberTextPrimary,
    surface = CyberSurfaceDark,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariantDark,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    error = CyberCrimson,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006875),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF006C35),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8CF8AD),
    onSecondaryContainer = Color(0xFF00210C),
    tertiary = CyberPurple,
    background = CyberBgLight,
    onBackground = Color(0xFF0F172A),
    surface = CyberSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = CyberSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = CyberCrimson
)

@Composable
fun CyberGuardTheme(
    darkTheme: Boolean = true, // CyberGuard AI defaults to dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

