package com.example.timetable.shared

interface FileHandler {
    fun pickLecFile(onResult: (String?) -> Unit)
    fun saveLecFile(fileName: String, content: String)
    
    fun saveArchive(fileName: String, content: String)
    fun getArchives(): List<Pair<String, String>>
    fun deleteArchive(fileName: String)
}

expect fun getFileHandler(): FileHandler
