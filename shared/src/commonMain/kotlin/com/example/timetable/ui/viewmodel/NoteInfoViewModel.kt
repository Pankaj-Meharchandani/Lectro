package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.Note

class NoteInfoViewModel(
    private val db: TimetableDatabase
) : ViewModel() {
    var note by mutableStateOf<Note?>(null)
    var wordCount by mutableIntStateOf(0)
    var charCount by mutableIntStateOf(0)

    fun loadNote(id: Int) {
        note = db.getNote().find { it.id == id }
    }

    fun saveNote(note: Note) {
        db.updateNote(note)
    }

    fun updateCounts(text: String) {
        charCount = text.length
        wordCount = if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size
    }
}
