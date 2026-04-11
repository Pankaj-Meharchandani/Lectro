package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.Teacher

class TeacherViewModel(
    private val db: TimetableDatabase
) : ViewModel() {
    
    val teachers = mutableStateListOf<Teacher>()

    fun loadTeachers() {
        teachers.clear()
        teachers.addAll(db.getTeacher())
    }

    fun deleteTeacher(teacher: Teacher) {
        db.deleteTeacherById(teacher)
        loadTeachers()
    }

    fun insertTeacher(teacher: Teacher) {
        db.insertTeacher(teacher)
        loadTeachers()
    }

    fun updateTeacher(teacher: Teacher) {
        db.updateTeacher(teacher)
        loadTeachers()
    }

    fun moveTeacher(index: Int, up: Boolean) {
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in teachers.indices) {
            val t1 = teachers[index]
            val t2 = teachers[targetIndex]
            db.updateTeacherSortOrder(t1.id, targetIndex)
            db.updateTeacherSortOrder(t2.id, index)
            loadTeachers()
        }
    }
}
