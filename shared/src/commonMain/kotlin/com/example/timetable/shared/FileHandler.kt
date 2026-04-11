package com.example.timetable.shared

interface FileHandler {
    fun pickLecFile(onResult: (String?) -> Unit)
    fun saveLecFile(fileName: String, content: String)
}

expect fun getFileHandler(): FileHandler
