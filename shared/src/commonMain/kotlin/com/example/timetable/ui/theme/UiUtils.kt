package com.example.timetable.ui.theme

import androidx.compose.ui.graphics.Color

fun getAttendanceColor(percentage: Int, goal: Int): Color {
    return when {
        percentage < goal -> Color(0xFFF44336) // Red
        percentage <= goal + 5 -> Color(0xFFFFC107) // Amber/Yellow
        else -> Color(0xFF4CAF50) // Green
    }
}
