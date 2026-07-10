package com.example.timetable.model

data class SemesterResult(
    val id: Int = 0,
    val semesterName: String,
    val gpa: Double,
    val date: Long,
    val subjectGrades: List<SubjectGrade> = emptyList()
)

data class SubjectGrade(
    val id: Int = 0,
    val semesterId: Int = 0,
    val subjectName: String,
    val gradePoint: Double,
    val credits: Int
)
