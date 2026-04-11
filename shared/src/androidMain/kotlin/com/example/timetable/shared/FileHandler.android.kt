package com.example.timetable.shared

import android.content.Context
import java.io.File

class AndroidFileHandler(private val context: Context) : FileHandler {
    override fun pickLecFile(onResult: (String?) -> Unit) {}
    override fun saveLecFile(fileName: String, content: String) {}

    private val archiveDir by lazy {
        File(context.filesDir, "semester_archives").apply { if (!exists()) mkdirs() }
    }

    override fun saveArchive(fileName: String, content: String) {
        File(archiveDir, fileName).writeText(content)
    }

    override fun getArchives(): List<Pair<String, String>> {
        return archiveDir.listFiles()?.map { it.name to it.readText() } ?: emptyList()
    }

    override fun deleteArchive(fileName: String) {
        File(archiveDir, fileName).delete()
    }
}

private var fileHandler: FileHandler? = null

fun initFileHandler(context: Context) {
    fileHandler = AndroidFileHandler(context)
}

actual fun getFileHandler(): FileHandler = fileHandler ?: throw Exception("FileHandler not initialized")
