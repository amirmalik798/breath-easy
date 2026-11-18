package com.amirmalik.breatheasy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFFE85D04),
    onPrimary = Color(0xFF1A0D06),
    primaryContainer = Color(0xFFFFE0CC),
    onPrimaryContainer = Color(0xFF4B1D08),
    secondary = Color(0xFFC64F0A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE7D8),
    onSecondaryContainer = Color(0xFF54220B),
    tertiary = Color(0xFFB87819),
    onTertiary = Color(0xFF241500),
    tertiaryContainer = Color(0xFFFFE7B8),
    onTertiaryContainer = Color(0xFF3E2600),
    background = Color(0xFFF6F2EF),
    onBackground = Color(0xFF211B18),
    surface = Color(0xFFFFFCFA),
    onSurface = Color(0xFF211B18),
    surfaceVariant = Color(0xFFECE3DE),
    onSurfaceVariant = Color(0xFF6D625B),
    outline = Color(0xFF9D8A7D),
    outlineVariant = Color(0xFFE2D6CF),
    error = Color(0xFFBA1A1A),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFFF7417),
    onPrimary = Color(0xFF201008),
    primaryContainer = Color(0xFF57270F),
    onPrimaryContainer = Color(0xFFFFE2D0),
    secondary = Color(0xFFFFA15C),
    onSecondary = Color(0xFF2B1207),
    secondaryContainer = Color(0xFF492514),
    onSecondaryContainer = Color(0xFFFFDCC5),
    tertiary = Color(0xFFFFC15E),
    onTertiary = Color(0xFF251700),
    tertiaryContainer = Color(0xFF4D3514),
    onTertiaryContainer = Color(0xFFFFE9BF),
    background = Color(0xFF0B0A0D),
    onBackground = Color(0xFFF6F0EC),
    surface = Color(0xFF17161B),
    onSurface = Color(0xFFF6F0EC),
    surfaceVariant = Color(0xFF28252B),
    onSurfaceVariant = Color(0xFFBEB3AC),
    outline = Color(0xFF85746B),
    outlineVariant = Color(0xFF393239),
    error = Color(0xFFFFB4AB),
)

@Composable
fun BreatheEasyTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkScheme else LightScheme
    MaterialTheme(colorScheme = colors, content = content)
}
