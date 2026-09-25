package com.turkuaz.beta.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF1F6F5C)

private val LightColors = lightColorScheme(primary = Accent, secondary = Accent)
private val DarkColors = darkColorScheme(primary = Accent, secondary = Accent)

@Composable
fun TurkuazTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
