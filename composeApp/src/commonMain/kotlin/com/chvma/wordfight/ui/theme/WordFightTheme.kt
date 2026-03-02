package com.chvma.wordfight.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7FC8FF),
    secondary = androidx.compose.ui.graphics.Color(0xFF7CFFB2),
    tertiary = androidx.compose.ui.graphics.Color(0xFFFF9AEF),
    background = androidx.compose.ui.graphics.Color(0xFF101426),
    surface = androidx.compose.ui.graphics.Color(0xFF1A1F34),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE9ECFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE9ECFF),
)

private val LightScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1F6FEB),
    secondary = androidx.compose.ui.graphics.Color(0xFF1E8E5A),
    tertiary = androidx.compose.ui.graphics.Color(0xFF7A3FE0),
    background = androidx.compose.ui.graphics.Color(0xFFF3F7FF),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onBackground = androidx.compose.ui.graphics.Color(0xFF101426),
    onSurface = androidx.compose.ui.graphics.Color(0xFF101426),
)

@Composable
fun WordFightTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (isDark) DarkScheme else LightScheme,
        content = content,
    )
}

