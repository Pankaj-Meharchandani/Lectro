package com.example.timetable.utils

object TimeUtils {
    fun formatTo12Hour(time24: String?): String {
        if (time24.isNullOrBlank()) return ""
        return try {
            val parts = time24.split(":")
            if (parts.size >= 2) {
                var hour = parts[0].toInt()
                val minute = parts[1].toInt()
                val ampm = if (hour >= 12) "PM" else "AM"
                hour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $ampm"
            } else time24
        } catch (e: Exception) {
            time24 ?: ""
        }
    }

    fun get24HourString(hour: Int, minute: Int): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    fun parse24Hour(time24: String?): Pair<Int, Int>? {
        if (time24.isNullOrBlank()) return null
        return try {
            val parts = time24.split(":")
            if (parts.size >= 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun timeToMinutes(time: String?): Int {
        if (time.isNullOrBlank()) return 0
        return try {
            val parts = time.split(":")
            if (parts.size >= 2) {
                parts[0].toInt() * 60 + parts[1].toInt()
            } else 0
        } catch (e: Exception) {
            0
        }
    }
}
