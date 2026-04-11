package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.Material
import com.example.timetable.model.Note
import com.example.timetable.model.Subject
import com.example.timetable.model.Week
import kotlinx.datetime.*

class SubjectDetailViewModel(
    private val db: TimetableDatabase
) : ViewModel() {
    var subject by mutableStateOf<Subject?>(null)
    val notes = mutableStateListOf<Note>()
    val materials = mutableStateListOf<Material>()
    val slots = mutableStateListOf<Week>()

    fun loadSubjectData(id: Int) {
        val allSubjects = db.getAllSubjects()
        val currentSubject = allSubjects.find { it.id == id }
        subject = currentSubject
        currentSubject?.name?.let {
            slots.clear()
            slots.addAll(db.getWeeksBySubject(it))
        }
        loadNotes(id)
        loadMaterials(id)
    }

    fun updateAttendance(weekId: Int, type: String) {
        subject?.let {
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            db.updateAttendance(weekId, it.name, type, date)
            loadSubjectData(it.id)
        }
    }

    fun getAttendanceStatus(weekId: Int): String? {
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return db.getAttendanceStatus(weekId, date)
    }

    fun loadNotes(subjectId: Int) {
        notes.clear()
        notes.addAll(db.getNotesBySubject(subjectId))
    }

    fun loadMaterials(subjectId: Int) {
        materials.clear()
        materials.addAll(db.getMaterialsBySubject(subjectId))
    }

    fun addNote(title: String, content: String, color: Int) {
        subject?.let {
            val note = Note(
                subjectId = it.id,
                title = title,
                text = content,
                color = if (color != 0) color else it.color
            )
            db.insertNote(note)
            loadNotes(it.id)
        }
    }

    fun deleteNote(id: Int) {
        db.deleteNoteById(id)
        subject?.let { loadNotes(it.id) }
    }

    fun addMaterial(name: String, path: String, type: String) {
        subject?.let {
            val material = Material(
                subjectId = it.id,
                name = name,
                path = path,
                type = type
            )
            db.insertMaterial(material)
            loadMaterials(it.id)
        }
    }

    fun updateMaterialName(id: Int, newName: String) {
        db.updateMaterialName(id, newName)
        subject?.let { loadMaterials(it.id) }
    }

    fun deleteMaterial(id: Int) {
        db.deleteMaterialById(id)
        subject?.let { loadMaterials(it.id) }
    }

    fun moveNote(index: Int, up: Boolean) {
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in notes.indices) {
            val note1 = notes[index]
            val note2 = notes[targetIndex]
            db.updateNoteSortOrder(note1.id, targetIndex)
            db.updateNoteSortOrder(note2.id, index)
            subject?.let { loadNotes(it.id) }
        }
    }

    fun moveMaterial(index: Int, up: Boolean) {
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in materials.indices) {
            val mat1 = materials[index]
            val mat2 = materials[targetIndex]
            db.updateMaterialSortOrder(mat1.id, targetIndex)
            db.updateMaterialSortOrder(mat2.id, index)
            subject?.let { loadMaterials(it.id) }
        }
    }

    fun updateSubject(updated: Subject) {
        db.updateSubject(updated.id, updated.name, updated.color, updated.teacher, updated.room)
        loadSubjectData(updated.id)
    }
}
