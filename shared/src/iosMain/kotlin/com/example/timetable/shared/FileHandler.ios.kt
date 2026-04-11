package com.example.timetable.shared

class IOSFileHandler : FileHandler {
    override fun pickLecFile(onResult: (String?) -> Unit) {}
    override fun saveLecFile(fileName: String, content: String) {}
    override fun saveArchive(fileName: String, content: String) {}
    override fun getArchives(): List<Pair<String, String>> = emptyList()
    override fun deleteArchive(fileName: String) {}
}

actual fun getFileHandler(): FileHandler = IOSFileHandler()
