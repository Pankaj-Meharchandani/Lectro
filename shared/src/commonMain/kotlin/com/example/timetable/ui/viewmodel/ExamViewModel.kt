package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.Exam
import com.example.timetable.model.Week

class ExamViewModel(
    private val db: TimetableDatabase
) : ViewModel() {
    
    val exams = mutableStateListOf<Exam>()
    val subjects = mutableStateListOf<String>()
    val teachers = mutableStateListOf<String>()

    fun loadExams() {
        exams.clear()
        exams.addAll(db.getExam())
    }

    fun loadSuggestions() {
        subjects.clear()
        subjects.addAll(db.getSubjectsList())
        teachers.clear()
        teachers.addAll(db.getTeachersList())
    }

    fun getSubjectDetails(name: String): Week? {
        return db.getSubjectDetails(name)
    }

    fun deleteExam(exam: Exam) {
        db.deleteExamById(exam)
        loadExams()
    }

    fun insertExam(exam: Exam) {
        db.insertExam(exam)
        loadExams()
        loadSuggestions()
    }
}
