package com.example.timetable.shared

interface Platform {
    val name: String
    val versionName: String
    fun showToast(message: String)
}

expect fun getPlatform(): Platform
