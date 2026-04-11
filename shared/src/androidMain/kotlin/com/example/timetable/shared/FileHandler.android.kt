package com.example.timetable.shared

class AndroidFileHandler : FileHandler {
    override fun pickLecFile(onResult: (String?) -> Unit) {
        // Not easily implementable without Activity context in a singleton.
        // We'll handle file picking in MainActivity and pass it down.
    }
    override fun saveLecFile(fileName: String, content: String) {
        // Same here.
    }
}

actual fun getFileHandler(): FileHandler = AndroidFileHandler()
