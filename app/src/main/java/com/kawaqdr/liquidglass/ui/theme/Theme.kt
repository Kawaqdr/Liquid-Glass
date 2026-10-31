package com.kawaqdr.liquidglass.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GlassColors = darkColorScheme(
    primary = Color(0xFFEAF5FF),
    secondary = Color(0xFFB8D9F2),
    background = Color(0xFF101522),
    surface = Color(0xB3222D40),
    onPrimary = Color(0xFF142033),
    onSecondary = Color(0xFF172337),
    onBackground = Color(0xFFF6F8FC),
    onSurface = Color(0xFFF6F8FC)
)

@Composable
fun LiquidGlassTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GlassColors, content = content)
}
