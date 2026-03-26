package com.example.timetable.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val format24 = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val format12 = SimpleDateFormat("hh:mm a", Locale.getDefault())

    @JvmStatic
    fun formatTo12Hour(time24: String?): String {
        if (time24.isNullOrBlank()) return ""
        return try {
            val date = format24.parse(time24)
            if (date != null) format12.format(date) else time24
        } catch (e: Exception) {
            time24
        }
    }

    @JvmStatic
    fun get24HourString(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    @JvmStatic
    fun parse24Hour(time24: String?): Pair<Int, Int>? {
        if (time24.isNullOrBlank()) return null
        return try {
            val parts = time24.split(":")
            if (parts.size == 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
