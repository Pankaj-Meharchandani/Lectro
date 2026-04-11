package com.example.timetable.data

import com.example.timetable.db.TimetableDatabase as Db
import com.example.timetable.model.*
import app.cash.sqldelight.db.SqlDriver

class SqlDelightTimetableDatabase(driver: SqlDriver) : TimetableDatabase {
    private val database = Db(driver)
    private val dbQueries = database.timetableQueries

    override fun getAllWeeks(): List<Week> = dbQueries.getAllWeeks { id, subject, fragment, teacher, room, fromtime, totime, color ->
        Week(id.toInt(), subject, fragment, teacher ?: "", room ?: "", fromtime ?: "", totime ?: "", "", color)
    }.executeAsList()

    override fun getWeek(fragment: String): List<Week> = dbQueries.getWeek(fragment) { id, subject, fragment, teacher, room, fromtime, totime, color ->
        Week(id.toInt(), subject, fragment, teacher ?: "", room ?: "", fromtime ?: "", totime ?: "", "", color)
    }.executeAsList()

    override fun insertWeek(week: Week) {
        dbQueries.insertWeek(week.subject, week.fragment, week.teacher, week.room, week.fromTime, week.toTime, week.color)
    }

    override fun updateWeek(week: Week) {
        dbQueries.updateWeek(week.subject, week.teacher, week.room, week.fromTime, week.toTime, week.color, week.id.toLong())
    }

    override fun deleteWeekById(week: Week) {
        dbQueries.deleteWeekById(week.id.toLong())
    }

    override fun getAllSubjects(): List<Subject> = dbQueries.getAllSubjects { id, name, color, teacher, room, sort_order, attended, missed, skipped ->
        Subject(id.toInt(), name, color, teacher ?: "", room ?: "", attended, missed, skipped)
    }.executeAsList()

    override fun getSubjectsList(): List<String> = dbQueries.getSubjectsList().executeAsList()

    override fun getSubjectByName(name: String): Subject? = dbQueries.getSubjectByName(name) { id, name, color, teacher, room, sort_order, attended, missed, skipped ->
        Subject(id.toInt(), name, color, teacher ?: "", room ?: "", attended, missed, skipped)
    }.executeAsOneOrNull()

    override fun getSubjectDetails(name: String): Week? = dbQueries.getSubjectDetails(name) { id, name, color, teacher, room, sort_order, attended, missed, skipped ->
        Week(subject = name, color = color, teacher = teacher ?: "", room = room ?: "")
    }.executeAsOneOrNull()

    override fun getTeacher(): List<Teacher> = dbQueries.getTeachers { id, name, post, phonenumber, email, cabinnumber, color, sort_order ->
        Teacher(id.toInt(), name, post ?: "", phonenumber ?: "", email ?: "", cabinnumber ?: "", color)
    }.executeAsList()

    override fun insertTeacher(teacher: Teacher) {
        dbQueries.insertTeacher(teacher.name, teacher.post, teacher.phonenumber, teacher.email, teacher.cabinNumber, teacher.color)
    }

    override fun updateTeacher(teacher: Teacher) {
        dbQueries.updateTeacher(teacher.name, teacher.post, teacher.phonenumber, teacher.email, teacher.cabinNumber, teacher.color, teacher.id.toLong())
    }

    override fun deleteTeacherById(teacher: Teacher) {
        dbQueries.deleteTeacherById(teacher.id.toLong())
    }

    override fun updateTeacherSortOrder(id: Int, sortOrder: Int) {
        dbQueries.updateTeacherSortOrder(sortOrder.toLong(), id.toLong())
    }

    override fun getTeachersList(): List<String> = dbQueries.getTeachersList().executeAsList()

    override fun getNote(): List<Note> = dbQueries.getNotes { id, title, text, color, subject_id, sort_order ->
        Note(id.toInt(), title ?: "", text ?: "", color, subject_id)
    }.executeAsList()

    override fun insertNote(note: Note): Long {
        dbQueries.insertNote(note.title, note.text, note.color, note.subjectId)
        // SQLDelight doesn't easily return last inserted ID without another query or specific wrapper.
        // For now, let's just return 0 or do a custom query if needed.
        return 0 
    }

    override fun getHomework(): List<Homework> = dbQueries.getHomeworks { id, subject, title, description, date, color, completed ->
        Homework(id.toInt(), subject, title ?: "", description ?: "", date ?: "", color, completed)
    }.executeAsList()

    override fun getExam(): List<Exam> = dbQueries.getExams { id, subject, teacher, room, date, time, color ->
        Exam(id.toInt(), subject, teacher ?: "", time ?: "", date ?: "", room ?: "", color)
    }.executeAsList()

    override fun insertExam(exam: Exam) {
        dbQueries.insertExam(exam.subject, exam.teacher, exam.room, exam.date, exam.time, exam.color)
    }

    override fun updateExam(exam: Exam) {
        // ... (update query in .sq)
    }

    override fun deleteExamById(exam: Exam) {
        dbQueries.deleteExamById(exam.id.toLong())
    }

    override fun getUserDetail(): UserDetail = dbQueries.getUserDetail { id, name, email, roll_number, photo_path, other ->
        UserDetail(id.toInt(), name ?: "", email ?: "", roll_number ?: "", photo_path ?: "", other ?: "")
    }.executeAsOneOrNull() ?: UserDetail()

    override fun getAttendanceStatus(weekId: Int, date: String): String? = dbQueries.getAttendanceStatus(weekId, date).executeAsOneOrNull()

    override fun updateAttendance(weekId: Int, subjectName: String, type: String, date: String) {
        dbQueries.updateAttendance(date, weekId, subjectName, type)
    }

    override fun getAttendanceForSubject(subjectName: String): List<AttendanceRecord> = dbQueries.getAttendanceForSubject(subjectName) { id, date, week_id, subject_name, status ->
        AttendanceRecord(date, status, week_id)
    }.executeAsList()

    override fun updateAttendanceByDate(weekId: Int, subjectName: String, type: String, date: String) {
        dbQueries.updateAttendance(date, weekId, subjectName, type)
    }

    override fun deleteAttendanceRecord(weekId: Int, subjectName: String, date: String) {
        dbQueries.deleteAttendanceRecord(subjectName, date, weekId)
    }
}
