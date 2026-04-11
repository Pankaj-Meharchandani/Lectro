package com.example.timetable.shared

interface UriHandler {
    fun openUri(uri: String)
}

expect fun getUriHandler(): UriHandler
