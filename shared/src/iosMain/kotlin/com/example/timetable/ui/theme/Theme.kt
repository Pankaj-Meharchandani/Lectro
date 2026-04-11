package com.example.timetable.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
actual fun TimeTableTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
