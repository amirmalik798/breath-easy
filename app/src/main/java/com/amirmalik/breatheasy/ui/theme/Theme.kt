package com.amirmalik.breatheasy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Forest = Color(0xFF174A35)
private val ForestDeep = Color(0xFF0F3526)
private val Sage = Color(0xFF557665)
private val Canvas = Color(0xFFF3FAF5)
private val Card = Color(0xFFFFFFFF)
private val Ink = Color(0xFF17231D)
private val Muted = Color(0xFF68776E)
private val Border = Color(0xFFDCE8E0)

private val LightScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEFE3),
    onPrimaryContainer = ForestDeep,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4F1E8),
    onSecondaryContainer = ForestDeep,
    tertiary = Color(0xFF8B7356),
    background = Canvas,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEAF2EC),
    onSurfaceVariant = Muted,
    outline = Color(0xFFAAB9AF),
    outlineVariant = Border,
    error = Color(0xFFB64B45),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFA8D5BA),
    onPrimary = Color(0xFF153D2C),
    primaryContainer = Color(0xFF285B43),
    onPrimaryContainer = Color(0xFFD9F1E1),
    secondary = Color(0xFFB3CCBD),
    onSecondary = Color(0xFF263D31),
    secondaryContainer = Color(0xFF334F40),
    onSecondaryContainer = Color(0xFFD9EDE0),
    tertiary = Color(0xFFD5B992),
    background = Color(0xFF111A15),
    onBackground = Color(0xFFE4EEE7),
    surface = Color(0xFF18231D),
    onSurface = Color(0xFFE4EEE7),
    surfaceVariant = Color(0xFF26352C),
    onSurfaceVariant = Color(0xFFB8C9BE),
    outline = Color(0xFF82968A),
    outlineVariant = Color(0xFF34473A),
    error = Color(0xFFFFB4AB),
)

@Composable
fun BreatheEasyTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkScheme else LightScheme
    MaterialTheme(colorScheme = colors, content = content)
}
