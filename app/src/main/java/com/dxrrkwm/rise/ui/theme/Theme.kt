package com.dxrrkwm.rise.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Sky = Color(0xFF9FD3F7)
private val Pink = Color(0xFFF6C1D6)
private val Night = Color(0xFF0E1222)
private val Text = Color(0xFFE8ECF8)

private val colors = darkColorScheme(
    primary = Sky,
    onPrimary = Color(0xFF0B2A3F),
    primaryContainer = Color(0xFF23415A),
    onPrimaryContainer = Sky,
    secondary = Pink,
    onSecondary = Color(0xFF3D1526),
    secondaryContainer = Color(0xFF4A2A3A),
    onSecondaryContainer = Pink,
    background = Night,
    onBackground = Text,
    surface = Night,
    onSurface = Text,
    surfaceVariant = Color(0xFF232945),
    onSurfaceVariant = Color(0xFFB4BCD6),
    surfaceContainerLowest = Color(0xFF0A0D19),
    surfaceContainerLow = Color(0xFF141829),
    surfaceContainer = Color(0xFF181D33),
    surfaceContainerHigh = Color(0xFF1F2540),
    surfaceContainerHighest = Color(0xFF283050),
    outline = Color(0xFF4A5378),
    outlineVariant = Color(0xFF2E3555),
    error = Color(0xFFFFB4AB),
)

@Composable
fun RiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}
