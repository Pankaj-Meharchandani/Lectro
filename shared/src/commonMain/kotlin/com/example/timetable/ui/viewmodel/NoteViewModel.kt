package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.Note
import com.example.timetable.model.Subject

class NoteViewModel(
    private val db: TimetableDatabase
) : ViewModel() {
    
    val allSubjects = mutableStateListOf<Subject>()
    val subjectNames = mutableStateListOf<String>()

    fun loadSubjects() {
        allSubjects.clear()
        val subs = db.getAllSubjects()
        // Here we could add more logic if needed, like fetching teachers for each subject
        allSubjects.addAll(subs)
        subjectNames.clear()
        subjectNames.addAll(db.getSubjectsList())
    }

    fun insertNote(note: Note, subjectName: String? = null) {
        if (!subjectName.isNullOrBlank()) {
            val subject = db.getSubjectDetails(subjectName)
            if (subject == null) {
                db.insertSubject(subjectName, note.color, "", "")
            }
            note.subjectId = db.getAllSubjects().find { it.name == subjectName }?.id ?: -1
        }
        db.insertNote(note)
        loadSubjects()
    }

    fun moveSubject(index: Int, up: Boolean) {
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in allSubjects.indices) {
            val sub1 = allSubjects[index]
            val sub2 = allSubjects[targetIndex]
            db.updateSubjectSortOrder(sub1.id, targetIndex)
            db.updateSubjectSortOrder(sub2.id, index)
            loadSubjects()
        }
    }

    fun updateSubject(subject: Subject) {
        db.updateSubject(subject.id, subject.name, subject.color, subject.teacher, subject.room)
        loadSubjects()
    }

    fun deleteSubject(id: Int) {
        db.deleteSubjectById(id)
        loadSubjects()
    }
}
