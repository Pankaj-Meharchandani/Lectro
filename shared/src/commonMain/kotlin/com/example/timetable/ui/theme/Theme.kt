package com.example.timetable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

internal val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

internal val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun themedContainerColor(baseColor: Color): Color {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        baseColor.copy(alpha = 0.25f).compositeOver(MaterialTheme.colorScheme.surface)
    } else {
        baseColor.copy(alpha = 0.15f).compositeOver(MaterialTheme.colorScheme.surface)
    }
}

@Composable
fun subtleThemedColor(baseColor: Color): Color {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        baseColor.copy(alpha = 0.1f).compositeOver(MaterialTheme.colorScheme.surface)
    } else {
        baseColor.copy(alpha = 0.05f).compositeOver(MaterialTheme.colorScheme.surface)
    }
}

@Composable
expect fun TimeTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
