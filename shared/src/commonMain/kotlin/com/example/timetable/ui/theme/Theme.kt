@file:JvmName("SharedTheme")
package com.example.timetable.ui.theme

import kotlin.jvm.JvmName
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
expect fun TimeTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
