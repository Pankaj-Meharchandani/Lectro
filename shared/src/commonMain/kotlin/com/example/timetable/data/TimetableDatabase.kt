package com.example.timetable.data

import com.example.timetable.model.*

interface TimetableDatabase {
    fun getAllWeeks(): List<Week>
    fun getWeek(fragment: String): List<Week>
    fun getWeeksBySubject(subject: String): List<Week>
    fun insertWeek(week: Week)
    fun updateWeek(week: Week)
    fun deleteWeekById(week: Week)
    
    fun getAllSubjects(): List<Subject>
    fun getSubjectsList(): List<String>
    fun getSubjectByName(name: String): Subject?
    fun getSubjectDetails(name: String): Week?
    fun insertSubject(name: String, color: Int, teacher: String, room: String)
    fun updateSubject(id: Int, name: String, color: Int, teacher: String, room: String)
    fun updateSubjectName(id: Int, name: String)
    fun deleteSubjectById(id: Int)
    fun updateSubjectSortOrder(id: Int, sortOrder: Int)
    fun updateSubjectAttendance(name: String, attended: Int, missed: Int, skipped: Int)
    
    fun getTeacher(): List<Teacher>
    fun getTeachersList(): List<String>
    fun insertTeacher(teacher: Teacher)
    fun updateTeacher(teacher: Teacher)
    fun deleteTeacherById(teacher: Teacher)
    fun updateTeacherSortOrder(id: Int, sortOrder: Int)
    
    fun getNote(): List<Note>
    fun getNotesBySubject(subjectId: Int): List<Note>
    fun insertNote(note: Note): Long
    fun updateNote(note: Note)
    fun deleteNoteById(id: Int)
    fun updateNoteSortOrder(id: Int, sortOrder: Int)
    
    fun getHomework(): List<Homework>
    fun insertHomework(homework: Homework)
    fun updateHomework(homework: Homework)
    fun deleteHomeworkById(homework: Homework)
    
    fun getExam(): List<Exam>
    fun insertExam(exam: Exam)
    fun updateExam(exam: Exam)
    fun deleteExamById(exam: Exam)
    
    fun getMaterialsBySubject(subjectId: Int): List<Material>
    fun insertMaterial(material: Material)
    fun updateMaterialName(id: Int, name: String)
    fun deleteMaterialById(id: Int)
    fun updateMaterialSortOrder(id: Int, sortOrder: Int)
    
    fun getUserDetail(): UserDetail
    fun saveUserDetail(userDetail: UserDetail)
    
    fun getAllUserFiles(): List<UserFile>
    fun insertUserFile(file: UserFile)
    fun deleteUserFile(id: Int)
    
    fun resetAllData()
    fun resetSemesterData()
    
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
