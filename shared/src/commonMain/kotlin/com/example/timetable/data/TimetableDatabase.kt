package com.example.timetable.data

import com.example.timetable.model.*

interface TimetableDatabase {
    fun getAllWeeks(): List<Week>
    fun getWeek(fragment: String): List<Week>
    fun insertWeek(week: Week)
    fun updateWeek(week: Week)
    fun deleteWeekById(week: Week)
    
    fun getAllSubjects(): List<Subject>
    fun getSubjectsList(): List<String>
    fun getSubjectByName(name: String): Subject?
    fun getSubjectDetails(name: String): Week?
    
    fun getTeacher(): List<Teacher>
    fun getTeachersList(): List<String>
    
    fun getNote(): List<Note>
    fun insertNote(note: Note): Long
    
    fun getHomework(): List<Homework>
    
    fun getUserDetail(): UserDetail
    
    fun getAttendanceStatus(weekId: Int, date: String): String?
    fun updateAttendance(weekId: Int, subjectName: String, type: String, date: String)
    fun getAttendanceForSubject(subjectName: String): List<AttendanceRecord>
    fun updateAttendanceByDate(weekId: Int, subjectName: String, type: String, date: String)
    fun deleteAttendanceRecord(weekId: Int, subjectName: String, date: String)
}

data class AttendanceRecord(
    val date: String,
    val status: String,
    val weekId: Int
)
