package com.dxrrkwm.rise.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

private val Sky = Color(0xFFA8D8F8)
private val Pink = Color(0xFFF5C4D8)
private val Night = Color(0xFF0B0E19)
private val Ink = Color(0xFFE9ECF5)

private val colors = darkColorScheme(
    primary = Sky,
    onPrimary = Color(0xFF0A2233),
    primaryContainer = Color(0xFF1C3347),
    onPrimaryContainer = Sky,
    secondary = Pink,
    onSecondary = Color(0xFF3A1626),
    secondaryContainer = Color(0xFF3B2431),
    onSecondaryContainer = Pink,
    background = Night,
    onBackground = Ink,
    surface = Night,
    onSurface = Ink,
    surfaceVariant = Color(0xFF1E2333),
    onSurfaceVariant = Color(0xFF8E95AB),
    surfaceContainerLowest = Color(0xFF080A12),
    surfaceContainerLow = Color(0xFF111521),
    surfaceContainer = Color(0xFF151A28),
    surfaceContainerHigh = Color(0xFF1B2131),
    surfaceContainerHighest = Color(0xFF232A3D),
    outline = Color(0xFF3A4157),
    outlineVariant = Color(0xFF232838),
    error = Color(0xFFFFB4AB),
)

// thin numerals with tabular figures, so times don't jump while they change
private val numbers = TextStyle(fontWeight = FontWeight.Thin, fontFeatureSettings = "tnum", letterSpacing = (-0.02).em)

private val typography = Typography().run {
    copy(
        displayLarge = displayLarge.merge(numbers),
        displayMedium = displayMedium.merge(numbers),
        displaySmall = displaySmall.merge(numbers).copy(fontWeight = FontWeight.ExtraLight),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Normal, letterSpacing = (-0.01).em),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Normal),
        titleSmall = titleSmall.copy(letterSpacing = 0.08.em, fontWeight = FontWeight.Medium),
    )
}

private val shapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun RiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, shapes = shapes, content = content)
}
