package com.example.timetable.shared

class IOSFileHandler : FileHandler {
    override fun pickLecFile(onResult: (String?) -> Unit) {
    }
    override fun saveLecFile(fileName: String, content: String) {
    }
}

actual fun getFileHandler(): FileHandler = IOSFileHandler()
