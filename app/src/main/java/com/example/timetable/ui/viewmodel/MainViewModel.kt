package com.example.timetable.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.model.Subject
import com.example.timetable.model.UserDetail
import com.example.timetable.model.Week
import com.example.timetable.utils.DbHelper
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    
    val weekData = mutableStateMapOf<String, List<Week>>()
    var subjects = mutableStateListOf<String>()
    var allSubjects = mutableStateListOf<Subject>()
    var teachers = mutableStateListOf<String>()
    var userDetail by mutableStateOf(UserDetail())

    fun loadWeekData(day: String) {
        weekData[day] = db.getWeek(day)
    }

    fun loadSuggestions() {
        userDetail = db.getUserDetail()
        subjects.clear()
        subjects.addAll(db.getSubjectsList())
        allSubjects.clear()
        allSubjects.addAll(db.allSubjects)
        teachers.clear()
        teachers.addAll(db.getTeachersList())
    }

    fun getSubjectIdByName(name: String): Int {
        return db.getAllSubjects().find { it.name == name }?.id ?: -1
    }

    fun getSubjectDetails(name: String): Week? {
        return db.getSubjectDetails(name)
    }

    fun deleteWeek(week: Week) {
        db.deleteWeekById(week)
        loadWeekData(week.fragment)
    }
    
    fun insertWeek(week: Week) {
        db.insertWeek(week)
        loadWeekData(week.fragment)
        loadSuggestions()
    }

    fun updateWeek(week: Week) {
        db.updateWeek(week)
        loadWeekData(week.fragment)
        loadSuggestions()
    }

    fun updateAttendance(weekId: Int, subjectName: String, type: String) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.updateAttendance(weekId, subjectName, type, date)
        weekData.keys.forEach { loadWeekData(it) }
        loadSuggestions()
    }

    fun getAttendanceStatus(weekId: Int): String? {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return db.getAttendanceStatus(weekId, date)
    }

    fun getSubjectByName(name: String) = db.getSubjectByName(name)

    fun getAllSubjects() = db.allSubjects
}
