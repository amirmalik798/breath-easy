package com.amirmalik.breatheasy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A brighter botanical palette: deep evergreen foundations, fresh mint actions,
// an aqua secondary accent, and a warm citrus highlight for meaningful emphasis.
private val Evergreen = Color(0xFF174B3B)
private val EvergreenDeep = Color(0xFF0D3027)
private val Mint = Color(0xFF2E8B68)
private val Aqua = Color(0xFF287F7A)
private val Citrus = Color(0xFF9A7A20)
private val Canvas = Color(0xFFF3F8F3)
private val Card = Color(0xFFFFFFFF)
private val Ink = Color(0xFF14231D)
private val Muted = Color(0xFF5E7066)
private val Border = Color(0xFFD7E5DA)

private val LightScheme = lightColorScheme(
    primary = Evergreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F2E3),
    onPrimaryContainer = EvergreenDeep,
    secondary = Aqua,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9F1EC),
    onSecondaryContainer = Color(0xFF123D39),
    tertiary = Citrus,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8EDC7),
    onTertiaryContainer = Color(0xFF49380A),
    background = Canvas,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8F1E9),
    onSurfaceVariant = Muted,
    outline = Color(0xFF91A79A),
    outlineVariant = Border,
    error = Color(0xFFB8494C),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF9DE7C1),
    onPrimary = Color(0xFF073A2A),
    primaryContainer = Color(0xFF236247),
    onPrimaryContainer = Color(0xFFD1F8E1),
    secondary = Color(0xFF83D9CD),
    onSecondary = Color(0xFF073B37),
    secondaryContainer = Color(0xFF20544E),
    onSecondaryContainer = Color(0xFFCAFAF1),
    tertiary = Color(0xFFE3D17A),
    onTertiary = Color(0xFF403500),
    tertiaryContainer = Color(0xFF5C4C16),
    onTertiaryContainer = Color(0xFFFFF1B8),
    background = Color(0xFF0B1512),
    onBackground = Color(0xFFE6F2EA),
    surface = Color(0xFF14231D),
    onSurface = Color(0xFFE6F2EA),
    surfaceVariant = Color(0xFF20352B),
    onSurfaceVariant = Color(0xFFB7CFC0),
    outline = Color(0xFF789889),
    outlineVariant = Color(0xFF304A3C),
    error = Color(0xFFFFB4AB),
)

@Composable
fun BreatheEasyTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkScheme else LightScheme
    MaterialTheme(colorScheme = colors, content = content)
}
