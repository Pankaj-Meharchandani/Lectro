package com.example.timetable.data

import com.example.timetable.db.TimetableDatabase as Db
import com.example.timetable.model.*
import app.cash.sqldelight.db.SqlDriver

class SqlDelightTimetableDatabase(driver: SqlDriver) : TimetableDatabase {
    private val database = Db(driver)
    private val dbQueries = database.timetableQueries

    override fun getAllWeeks(): List<Week> = dbQueries.getAllWeeks { id, subject, fragment, teacher, room, fromtime, totime, color ->
        Week(id.toInt(), subject, fragment, teacher ?: "", room ?: "", fromtime ?: "", totime ?: "", "", color.toInt())
    }.executeAsList()

    override fun getWeek(fragment: String): List<Week> = dbQueries.getWeek(fragment) { id, subject, fragment, teacher, room, fromtime, totime, color ->
        Week(id.toInt(), subject, fragment, teacher ?: "", room ?: "", fromtime ?: "", totime ?: "", "", color.toInt())
    }.executeAsList()

    override fun getWeeksBySubject(subject: String): List<Week> = dbQueries.getWeeksBySubject(subject) { id, sub, fragment, teacher, room, fromtime, totime, color ->
        Week(id.toInt(), sub, fragment, teacher ?: "", room ?: "", fromtime ?: "", totime ?: "", "", color.toInt())
    }.executeAsList()

    override fun insertWeek(week: Week) {
        dbQueries.insertWeek(week.subject, week.fragment, week.teacher, week.room, week.fromTime, week.toTime, week.color.toLong())
        // Auto create teacher
        if (week.teacher.isNotBlank()) {
            val teachers = dbQueries.getTeachersList().executeAsList()
            if (!teachers.contains(week.teacher)) {
                dbQueries.insertTeacher(week.teacher, "", "", "", "", week.color.toLong())
            }
        }
        // Auto create or update subject
        val existing = dbQueries.getSubjectByName(week.subject) { id, _, _, _, _, _, _, _, _ -> id }.executeAsOneOrNull()
        if (existing != null) {
            dbQueries.updateSubject(week.subject, week.color.toLong(), week.teacher, week.room, existing)
        } else {
            dbQueries.insertSubject(week.subject, week.color.toLong(), week.teacher, week.room)
        }
    }

    override fun updateWeek(week: Week) {
        dbQueries.updateWeek(week.subject, week.teacher, week.room, week.fromTime, week.toTime, week.color.toLong(), week.id.toLong())
        // Same auto logic
        if (week.teacher.isNotBlank()) {
            val teachers = dbQueries.getTeachersList().executeAsList()
            if (!teachers.contains(week.teacher)) {
                dbQueries.insertTeacher(week.teacher, "", "", "", "", week.color.toLong())
            }
        }
        val existing = dbQueries.getSubjectByName(week.subject) { id, _, _, _, _, _, _, _, _ -> id }.executeAsOneOrNull()
        if (existing != null) {
            dbQueries.updateSubject(week.subject, week.color.toLong(), week.teacher, week.room, existing)
        } else {
            dbQueries.insertSubject(week.subject, week.color.toLong(), week.teacher, week.room)
        }
    }

    override fun deleteWeekById(week: Week) {
        dbQueries.deleteWeekById(week.id.toLong())
    }

    override fun getAllSubjects(): List<Subject> = dbQueries.getAllSubjects { id, name, color, teacher, room, sort_order, attended, missed, skipped ->
        Subject(id.toInt(), name, color.toInt(), teacher ?: "", room ?: "", attended.toInt(), missed.toInt(), skipped.toInt())
    }.executeAsList()

    override fun getSubjectsList(): List<String> = dbQueries.getSubjectsList().executeAsList()

    override fun getSubjectByName(name: String): Subject? = dbQueries.getSubjectByName(name) { id, n, color, teacher, room, sort_order, attended, missed, skipped ->
        Subject(id.toInt(), n, color.toInt(), teacher ?: "", room ?: "", attended.toInt(), missed.toInt(), skipped.toInt())
    }.executeAsOneOrNull()

    override fun getSubjectDetails(name: String): Week? = dbQueries.getSubjectDetails(name) { id, n, color, teacher, room, sort_order, attended, missed, skipped ->
        Week(subject = n, color = color.toInt(), teacher = teacher ?: "", room = room ?: "")
    }.executeAsOneOrNull()

    override fun insertSubject(name: String, color: Int, teacher: String, room: String) {
        dbQueries.insertSubject(name, color.toLong(), teacher, room)
    }

    override fun updateSubject(id: Int, name: String, color: Int, teacher: String, room: String) {
        dbQueries.updateSubject(name, color.toLong(), teacher, room, id.toLong())
        // Update related tables colors
        dbQueries.updateTimetableColorBySubject(color.toLong(), name)
        dbQueries.updateHomeworkColorBySubject(color.toLong(), name)
        dbQueries.updateExamColorBySubject(color.toLong(), name)
    }

    override fun updateSubjectName(id: Int, name: String) {
        dbQueries.updateSubjectName(name, id.toLong())
    }

    override fun deleteSubjectById(id: Int) {
        val sub = dbQueries.getAllSubjects().executeAsList().find { it.id.toInt() == id }
        dbQueries.deleteSubjectById(id.toLong())
        dbQueries.deleteNotesBySubjectId(id.toLong())
        dbQueries.deleteMaterialsBySubjectId(id.toLong())
        sub?.name?.let { name ->
            dbQueries.deleteTimetableBySubjectName(name)
            dbQueries.deleteHomeworkBySubjectName(name)
            dbQueries.deleteExamBySubjectName(name)
        }
    }

    override fun updateSubjectSortOrder(id: Int, sortOrder: Int) {
        dbQueries.updateSubjectSortOrder(sortOrder.toLong(), id.toLong())
    }

    override fun updateSubjectAttendance(name: String, attended: Int, missed: Int, skipped: Int) {
        dbQueries.updateSubjectAttendance(attended.toLong(), missed.toLong(), skipped.toLong(), name)
    }

    override fun getTeacher(): List<Teacher> = dbQueries.getTeachers { id, name, post, phonenumber, email, cabinnumber, color, sort_order ->
        Teacher(id.toInt(), name, post ?: "", phonenumber ?: "", email ?: "", cabinnumber ?: "", color.toInt())
    }.executeAsList()

    override fun getTeachersList(): List<String> = dbQueries.getTeachersList().executeAsList()

    override fun insertTeacher(teacher: Teacher) {
        dbQueries.insertTeacher(teacher.name, teacher.post, teacher.phonenumber, teacher.email, teacher.cabinNumber, teacher.color.toLong())
    }

    override fun updateTeacher(teacher: Teacher) {
        dbQueries.updateTeacher(teacher.name, teacher.post, teacher.phonenumber, teacher.email, teacher.cabinNumber, teacher.color.toLong(), teacher.id.toLong())
    }

    override fun deleteTeacherById(teacher: Teacher) {
        dbQueries.deleteTeacherById(teacher.id.toLong())
    }

    override fun updateTeacherSortOrder(id: Int, sortOrder: Int) {
        dbQueries.updateTeacherSortOrder(sortOrder.toLong(), id.toLong())
    }

    override fun getNote(): List<Note> = dbQueries.getNotes { id, title, text, color, subject_id, sort_order ->
        Note(id.toInt(), title ?: "", text ?: "", color.toInt(), subject_id.toInt())
    }.executeAsList()

    override fun getNotesBySubject(subjectId: Int): List<Note> = dbQueries.getNotesBySubject(subjectId.toLong()) { id, title, text, color, sub_id, sort_order ->
        Note(id.toInt(), title ?: "", text ?: "", color.toInt(), sub_id.toInt())
    }.executeAsList()

    override fun insertNote(note: Note): Long {
        dbQueries.insertNote(note.title, note.text, note.color.toLong(), note.subjectId.toLong())
        return 0 
    }

    override fun updateNote(note: Note) {
        dbQueries.updateNote(note.title, note.text, note.color.toLong(), note.id.toLong())
    }

    override fun deleteNoteById(id: Int) {
        dbQueries.deleteNoteById(id.toLong())
    }

    override fun updateNoteSortOrder(id: Int, sortOrder: Int) {
        dbQueries.updateNoteSortOrder(sortOrder.toLong(), id.toLong())
    }

    override fun getHomework(): List<Homework> = dbQueries.getHomeworks { id, subject, title, description, date, color, completed ->
        Homework(id.toInt(), subject, title ?: "", description ?: "", date ?: "", color.toInt(), completed.toInt())
    }.executeAsList()

    override fun insertHomework(homework: Homework) {
        dbQueries.insertHomework(homework.subject, homework.title, homework.description, homework.date, homework.color.toLong(), homework.completed.toLong())
        // Auto create subject
        val existing = dbQueries.getSubjectByName(homework.subject) { id, _, _, _, _, _, _, _, _ -> id }.executeAsOneOrNull()
        if (existing == null) {
            dbQueries.insertSubject(homework.subject, homework.color.toLong(), "", "")
        }
    }

    override fun updateHomework(homework: Homework) {
        dbQueries.updateHomework(homework.subject, homework.title, homework.description, homework.date, homework.color.toLong(), homework.completed.toLong(), homework.id.toLong())
    }

    override fun deleteHomeworkById(homework: Homework) {
        dbQueries.deleteHomeworkById(homework.id.toLong())
    }

    override fun getExam(): List<Exam> = dbQueries.getExams { id, subject, teacher, room, date, time, color ->
        Exam(id.toInt(), subject, teacher ?: "", time ?: "", date ?: "", room ?: "", color.toInt())
    }.executeAsList()

    override fun insertExam(exam: Exam) {
        dbQueries.insertExam(exam.subject, exam.teacher, exam.room, exam.date, exam.time, exam.color.toLong())
        // Auto create teacher and subject
        if (exam.teacher.isNotBlank()) {
            val teachers = dbQueries.getTeachersList().executeAsList()
            if (!teachers.contains(exam.teacher)) {
                dbQueries.insertTeacher(exam.teacher, "", "", "", "", exam.color.toLong())
            }
        }
        val existing = dbQueries.getSubjectByName(exam.subject) { id, _, _, _, _, _, _, _, _ -> id }.executeAsOneOrNull()
        if (existing == null) {
            dbQueries.insertSubject(exam.subject, exam.color.toLong(), exam.teacher, "")
        }
    }

    override fun updateExam(exam: Exam) {
        dbQueries.updateExam(exam.subject, exam.teacher, exam.room, exam.date, exam.time, exam.color.toLong(), exam.id.toLong())
    }

    override fun deleteExamById(exam: Exam) {
        dbQueries.deleteExamById(exam.id.toLong())
    }

    override fun getMaterialsBySubject(subjectId: Int): List<Material> = dbQueries.getMaterialsBySubject(subjectId.toLong()) { id, sub_id, path, type, name, sort_order ->
        Material(id.toInt(), sub_id.toInt(), path, type ?: "", name ?: "")
    }.executeAsList()

    override fun insertMaterial(material: Material) {
        dbQueries.insertMaterial(material.subjectId.toLong(), material.path, material.type, material.name)
    }

    override fun updateMaterialName(id: Int, name: String) {
        dbQueries.updateMaterialName(name, id.toLong())
    }

    override fun deleteMaterialById(id: Int) {
        dbQueries.deleteMaterialById(id.toLong())
    }

    override fun updateMaterialSortOrder(id: Int, sortOrder: Int) {
        dbQueries.updateMaterialSortOrder(sortOrder.toLong(), id.toLong())
    }

    override fun getUserDetail(): UserDetail = dbQueries.getUserDetail { id, name, email, roll_number, photo_path, other ->
        UserDetail(id.toInt(), name ?: "", email ?: "", roll_number ?: "", photo_path ?: "", other ?: "")
    }.executeAsOneOrNull() ?: UserDetail()

    override fun saveUserDetail(userDetail: UserDetail) {
        val existing = dbQueries.getUserDetail { id, name, email, roll_number, photo_path, other -> id }.executeAsOneOrNull()
        if (existing == null) {
            dbQueries.insertUserDetail(userDetail.name, userDetail.email, userDetail.rollNumber, userDetail.photoPath, userDetail.other)
        } else {
            dbQueries.updateUserDetail(userDetail.name, userDetail.email, userDetail.rollNumber, userDetail.photoPath, userDetail.other)
        }
    }

    override fun getAllUserFiles(): List<UserFile> = dbQueries.getAllUserFiles { id, title, path ->
        UserFile(id.toInt(), title ?: "", path)
    }.executeAsList()

    override fun insertUserFile(file: UserFile) {
        dbQueries.insertUserFile(file.title, file.path)
    }

    override fun deleteUserFile(id: Int) {
        dbQueries.deleteUserFile(id.toLong())
    }

    override fun getAttendanceStatus(weekId: Int, date: String): String? = dbQueries.getAttendanceStatus(weekId.toLong(), date).executeAsOneOrNull()

    override fun updateAttendance(weekId: Int, subjectName: String, type: String, date: String) {
        dbQueries.updateAttendance(date, weekId.toLong(), subjectName, type)
    }

    override fun getAttendanceForSubject(subjectName: String): List<AttendanceRecord> = dbQueries.getAttendanceForSubject(subjectName) { id, date, week_id, subject_name, status ->
        AttendanceRecord(date, status, week_id.toInt())
    }.executeAsList()

    override fun updateAttendanceByDate(weekId: Int, subjectName: String, type: String, date: String) {
        dbQueries.updateAttendance(date, weekId.toLong(), subjectName, type)
    }

    override fun deleteAttendanceRecord(weekId: Int, subjectName: String, date: String) {
        dbQueries.deleteAttendanceRecord(subjectName, date, weekId.toLong())
    }
}
