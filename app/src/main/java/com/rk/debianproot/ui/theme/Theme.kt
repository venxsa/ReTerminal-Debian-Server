package com.rk.debianproot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val darkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF8CE0FF),
    secondary = androidx.compose.ui.graphics.Color(0xFFA6B4FF),
    background = androidx.compose.ui.graphics.Color(0xFF0F111E),
    surface = androidx.compose.ui.graphics.Color(0xFF171A2C),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE6E9FF)
)

private val lightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF0D47A1),
    secondary = androidx.compose.ui.graphics.Color(0xFF0288D1),
    background = androidx.compose.ui.graphics.Color(0xFFF2F4FF),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFF0A0A0A)
)

@Composable
fun DebianTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColors else lightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
}
