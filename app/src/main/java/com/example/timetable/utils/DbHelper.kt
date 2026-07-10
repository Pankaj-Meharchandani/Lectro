package com.example.timetable.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.example.timetable.model.Exam
import com.example.timetable.model.Homework
import com.example.timetable.model.Material
import com.example.timetable.model.Note
import com.example.timetable.model.SemesterResult
import com.example.timetable.model.Subject
import com.example.timetable.model.SubjectGrade
import com.example.timetable.model.Teacher
import com.example.timetable.model.UserDetail
import com.example.timetable.model.UserFile
import com.example.timetable.model.Week
import java.util.*

class DbHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTimetable = ("CREATE TABLE " + TIMETABLE + "("
                + WEEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WEEK_SUBJECT + " TEXT,"
                + WEEK_FRAGMENT + " TEXT,"
                + WEEK_TEACHER + " TEXT,"
                + WEEK_ROOM + " TEXT,"
                + WEEK_FROM_TIME + " TEXT,"
                + WEEK_TO_TIME + " TEXT,"
                + WEEK_COLOR + " INTEGER" + ")")

        val createHomeworks = ("CREATE TABLE " + HOMEWORKS + "("
                + HOMEWORKS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HOMEWORKS_SUBJECT + " TEXT,"
                + HOMEWORKS_TITLE + " TEXT,"
                + HOMEWORKS_DESCRIPTION + " TEXT,"
                + HOMEWORKS_DATE + " TEXT,"
                + HOMEWORKS_COLOR + " INTEGER,"
                + HOMEWORKS_COMPLETED + " INTEGER DEFAULT 0" + ")")

        val createNotes = ("CREATE TABLE " + NOTES + "("
                + NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NOTES_TITLE + " TEXT,"
                + NOTES_TEXT + " TEXT,"
                + NOTES_COLOR + " INTEGER,"
                + NOTES_SUBJECT_ID + " INTEGER DEFAULT -1,"
                + NOTES_SORT_ORDER + " INTEGER DEFAULT 0" + ")")

        val createTeachers = ("CREATE TABLE " + TEACHERS + "("
                + TEACHERS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TEACHERS_NAME + " TEXT,"
                + TEACHERS_POST + " TEXT,"
                + TEACHERS_PHONE_NUMBER + " TEXT,"
                + TEACHERS_EMAIL + " TEXT,"
                + TEACHERS_CABIN_NUMBER + " TEXT,"
                + TEACHERS_COLOR + " INTEGER,"
                + TEACHERS_SORT_ORDER + " INTEGER DEFAULT 0" + ")")

        val createExams = ("CREATE TABLE " + EXAMS + "("
                + EXAMS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + EXAMS_SUBJECT + " TEXT,"
                + EXAMS_TEACHER + " TEXT,"
                + EXAMS_ROOM + " TEXT,"
                + EXAMS_DATE + " TEXT,"
                + EXAMS_TIME + " TEXT,"
                + EXAMS_COLOR + " INTEGER" + ")")

        val createSubjects = ("CREATE TABLE " + SUBJECTS + "("
                + SUBJECTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SUBJECTS_NAME + " TEXT,"
                + SUBJECTS_COLOR + " INTEGER,"
                + SUBJECTS_TEACHER + " TEXT,"
                + SUBJECTS_ROOM + " TEXT,"
                + SUBJECTS_SORT_ORDER + " INTEGER DEFAULT 0,"
                + SUBJECTS_ATTENDED + " INTEGER DEFAULT 0,"
                + SUBJECTS_MISSED + " INTEGER DEFAULT 0,"
                + SUBJECTS_SKIPPED + " INTEGER DEFAULT 0,"
                + SUBJECTS_CREDITS + " INTEGER DEFAULT 0,"
                + SUBJECTS_GRADE_POINT + " REAL DEFAULT 0.0" + ")")

        val createMaterials = ("CREATE TABLE " + MATERIALS + "("
                + MATERIALS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MATERIALS_SUBJECT_ID + " INTEGER,"
                + MATERIALS_PATH + " TEXT,"
                + MATERIALS_TYPE + " TEXT,"
                + MATERIALS_NAME + " TEXT,"
                + MATERIALS_SORT_ORDER + " INTEGER DEFAULT 0" + ")")

        val createUserDetails = ("CREATE TABLE " + USER_DETAILS + "("
                + USER_DETAILS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USER_DETAILS_NAME + " TEXT,"
                + USER_DETAILS_EMAIL + " TEXT,"
                + USER_DETAILS_ROLL + " TEXT,"
                + USER_DETAILS_PHOTO + " TEXT,"
                + USER_DETAILS_OTHER + " TEXT" + ")")

        val createUserFiles = ("CREATE TABLE " + USER_FILES + "("
                + USER_FILES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USER_FILES_TITLE + " TEXT,"
                + USER_FILES_PATH + " TEXT" + ")")

        val createSemesterResults = ("CREATE TABLE " + SEMESTER_RESULTS + "("
                + SEM_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SEM_RES_NAME + " TEXT,"
                + SEM_RES_GPA + " REAL,"
                + SEM_RES_DATE + " INTEGER" + ")")

        val createSubjectGrades = ("CREATE TABLE " + SUBJECT_GRADES + "("
                + SUB_GRADES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SUB_GRADES_SEM_ID + " INTEGER,"
                + SUB_GRADES_NAME + " TEXT,"
                + SUB_GRADES_GP + " REAL,"
                + SUB_GRADES_CREDITS + " INTEGER,"
                + "FOREIGN KEY(" + SUB_GRADES_SEM_ID + ") REFERENCES " + SEMESTER_RESULTS + "(" + SEM_RES_ID + ") ON DELETE CASCADE" + ")")

        val createAttendance = ("CREATE TABLE " + ATTENDANCE + "("
                + ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ATTENDANCE_DATE + " TEXT,"
                + ATTENDANCE_WEEK_ID + " INTEGER,"
                + ATTENDANCE_SUBJECT_NAME + " TEXT,"
                + ATTENDANCE_STATUS + " TEXT" + ")")

        db.execSQL(createTimetable)
        db.execSQL(createHomeworks)
        db.execSQL(createNotes)
        db.execSQL(createTeachers)
        db.execSQL(createExams)
        db.execSQL(createSubjects)
        db.execSQL(createMaterials)
        db.execSQL(createUserDetails)
        db.execSQL(createUserFiles)
        db.execSQL(createAttendance)
        db.execSQL(createSemesterResults)
        db.execSQL(createSubjectGrades)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS $TIMETABLE")
            db.execSQL("DROP TABLE IF EXISTS $HOMEWORKS")
            db.execSQL("DROP TABLE IF EXISTS $NOTES")
            db.execSQL("DROP TABLE IF EXISTS $TEACHERS")
            db.execSQL("DROP TABLE IF EXISTS $EXAMS")
            onCreate(db)
        } else if (oldVersion == 6) {
            db.execSQL(
                "CREATE TABLE " + SUBJECTS + "("
                        + SUBJECTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + SUBJECTS_NAME + " TEXT,"
                        + SUBJECTS_COLOR + " INTEGER,"
                        + SUBJECTS_TEACHER + " TEXT,"
                        + SUBJECTS_ROOM + " TEXT)"
            )
            onUpgrade(db, 7, newVersion)
        } else if (oldVersion == 7) {
            db.execSQL("ALTER TABLE $NOTES ADD COLUMN $NOTES_SUBJECT_ID INTEGER DEFAULT -1")
            db.execSQL(
                "CREATE TABLE " + MATERIALS + "("
                        + MATERIALS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + MATERIALS_SUBJECT_ID + " INTEGER,"
                        + MATERIALS_PATH + " TEXT,"
                        + MATERIALS_TYPE + " TEXT,"
                        + MATERIALS_NAME + " TEXT)"
            )
            onUpgrade(db, 8, newVersion)
        } else if (oldVersion == 8) {
            db.execSQL("ALTER TABLE $SUBJECTS ADD COLUMN $SUBJECTS_SORT_ORDER INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $NOTES ADD COLUMN $NOTES_SORT_ORDER INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $MATERIALS ADD COLUMN $MATERIALS_SORT_ORDER INTEGER DEFAULT 0")
            onUpgrade(db, 9, newVersion)
        } else if (oldVersion == 9) {
            db.execSQL("ALTER TABLE $TEACHERS ADD COLUMN $TEACHERS_SORT_ORDER INTEGER DEFAULT 0")
            onUpgrade(db, 10, newVersion)
        } else if (oldVersion == 10) {
            db.execSQL("ALTER TABLE $HOMEWORKS ADD COLUMN $HOMEWORKS_TITLE TEXT")
            db.execSQL("ALTER TABLE $HOMEWORKS ADD COLUMN $HOMEWORKS_COMPLETED INTEGER DEFAULT 0")
            onUpgrade(db, 11, newVersion)
        } else if (oldVersion == 11) {
            db.execSQL("ALTER TABLE $TEACHERS ADD COLUMN $TEACHERS_CABIN_NUMBER TEXT")
            onUpgrade(db, 12, newVersion)
        } else if (oldVersion == 12) {
            db.execSQL(
                "CREATE TABLE " + USER_DETAILS + "("
                        + USER_DETAILS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + USER_DETAILS_NAME + " TEXT,"
                        + USER_DETAILS_EMAIL + " TEXT,"
                        + USER_DETAILS_ROLL + " TEXT,"
                        + USER_DETAILS_PHOTO + " TEXT,"
                        + USER_DETAILS_OTHER + " TEXT)"
            )
            db.execSQL(
                "CREATE TABLE " + USER_FILES + "("
                        + USER_FILES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + USER_FILES_TITLE + " TEXT,"
                        + USER_FILES_PATH + " TEXT)"
            )
            onUpgrade(db, 13, newVersion)
        } else if (oldVersion == 13) {
            db.execSQL("ALTER TABLE $SUBJECTS ADD COLUMN $SUBJECTS_ATTENDED INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $SUBJECTS ADD COLUMN $SUBJECTS_MISSED INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $SUBJECTS ADD COLUMN $SUBJECTS_SKIPPED INTEGER DEFAULT 0")
            onUpgrade(db, 14, newVersion)
        } else if (oldVersion == 14) {
            db.execSQL(
                "CREATE TABLE " + ATTENDANCE + "("
                        + ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + ATTENDANCE_DATE + " TEXT,"
                        + ATTENDANCE_WEEK_ID + " INTEGER,"
                        + ATTENDANCE_STATUS + " TEXT)"
            )
            onUpgrade(db, 15, newVersion)
        } else if (oldVersion == 15) {
            db.execSQL("ALTER TABLE $ATTENDANCE ADD COLUMN $ATTENDANCE_SUBJECT_NAME TEXT")
            // Populate subject_name for existing records if any
            db.execSQL("UPDATE $ATTENDANCE SET $ATTENDANCE_SUBJECT_NAME = (SELECT $WEEK_SUBJECT FROM $TIMETABLE WHERE $TIMETABLE.$WEEK_ID = $ATTENDANCE.$ATTENDANCE_WEEK_ID) WHERE $ATTENDANCE_WEEK_ID > 0")
            onUpgrade(db, 16, newVersion)
        } else if (oldVersion == 16) {
            db.execSQL("ALTER TABLE $SUBJECTS ADD COLUMN $SUBJECTS_CREDITS INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $SUBJECTS ADD COLUMN $SUBJECTS_GRADE_POINT REAL DEFAULT 0.0")

            db.execSQL(
                "CREATE TABLE " + SEMESTER_RESULTS + "("
                        + SEM_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + SEM_RES_NAME + " TEXT,"
                        + SEM_RES_GPA + " REAL,"
                        + SEM_RES_DATE + " INTEGER" + ")"
            )

            db.execSQL(
                "CREATE TABLE " + SUBJECT_GRADES + "("
                        + SUB_GRADES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + SUB_GRADES_SEM_ID + " INTEGER,"
                        + SUB_GRADES_NAME + " TEXT,"
                        + SUB_GRADES_GP + " REAL,"
                        + SUB_GRADES_CREDITS + " INTEGER,"
                        + "FOREIGN KEY(" + SUB_GRADES_SEM_ID + ") REFERENCES " + SEMESTER_RESULTS + "(" + SEM_RES_ID + ") ON DELETE CASCADE" + ")"
            )
        }
    }

    private fun getStringChecked(cursor: Cursor, columnName: String): String {
        val idx = cursor.getColumnIndex(columnName)
        return if (idx != -1) cursor.getString(idx) else ""
    }

    private fun getIntChecked(cursor: Cursor, columnName: String): Int {
        val idx = cursor.getColumnIndex(columnName)
        return if (idx != -1) cursor.getInt(idx) else 0
    }

    private fun getLongChecked(cursor: Cursor, columnName: String): Long {
        val idx = cursor.getColumnIndex(columnName)
        return if (idx != -1) cursor.getLong(idx) else 0L
    }

    private fun getDoubleChecked(cursor: Cursor, columnName: String): Double {
        val idx = cursor.getColumnIndex(columnName)
        return if (idx != -1) cursor.getDouble(idx) else 0.0
    }

    fun resetAllData() {
        writableDatabase.apply {
            execSQL("DELETE FROM $TIMETABLE")
            execSQL("DELETE FROM $HOMEWORKS")
            execSQL("DELETE FROM $NOTES")
            execSQL("DELETE FROM $TEACHERS")
            execSQL("DELETE FROM $EXAMS")
            execSQL("DELETE FROM $SUBJECTS")
            execSQL("DELETE FROM $MATERIALS")
            execSQL("DELETE FROM $ATTENDANCE")
        }
    }

    fun resetSemesterData() {
        writableDatabase.apply {
            execSQL("DELETE FROM $TIMETABLE")
            execSQL("DELETE FROM $HOMEWORKS")
            execSQL("DELETE FROM $NOTES")
            execSQL("DELETE FROM $EXAMS")
            execSQL("DELETE FROM $SUBJECTS")
            execSQL("DELETE FROM $MATERIALS")
            execSQL("DELETE FROM $ATTENDANCE")
        }
    }

    fun removeFullSchedule() {
        writableDatabase.execSQL("DELETE FROM $TIMETABLE")
    }

    fun removeAllSubjects() {
        writableDatabase.apply {
            execSQL("DELETE FROM $SUBJECTS")
            execSQL("DELETE FROM $TIMETABLE")
            execSQL("DELETE FROM $MATERIALS")
            execSQL("DELETE FROM $ATTENDANCE")
        }
    }

    /**
     * Methods for Week fragments
     */
    fun insertWeek(week: Week) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(WEEK_SUBJECT, week.subject)
            put(WEEK_FRAGMENT, week.fragment)
            put(WEEK_TEACHER, week.teacher)
            put(WEEK_ROOM, week.room)
            put(WEEK_FROM_TIME, week.fromTime)
            put(WEEK_TO_TIME, week.toTime)
            put(WEEK_COLOR, week.color)
        }
        db.insert(TIMETABLE, null, contentValues)

        addTeacherIfNew(week.teacher, week.color)
        insertSubject(week.subject, week.color, week.teacher, week.room)
    }

    fun deleteWeekById(week: Week) {
        writableDatabase.delete(TIMETABLE, "$WEEK_ID = ? ", arrayOf(week.id.toString()))
    }

    fun updateWeek(week: Week) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(WEEK_SUBJECT, week.subject)
            put(WEEK_TEACHER, week.teacher)
            put(WEEK_ROOM, week.room)
            put(WEEK_FROM_TIME, week.fromTime)
            put(WEEK_TO_TIME, week.toTime)
            put(WEEK_COLOR, week.color)
        }
        db.update(TIMETABLE, contentValues, "$WEEK_ID = ?", arrayOf(week.id.toString()))

        addTeacherIfNew(week.teacher, week.color)
        insertSubject(week.subject, week.color, week.teacher, week.room)
    }

    fun getWeek(fragment: String): ArrayList<Week> {
        val weeklist = ArrayList<Week>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TIMETABLE WHERE $WEEK_FRAGMENT = ? ORDER BY $WEEK_FROM_TIME ASC",
            arrayOf(fragment)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val w = Week()
                    w.id = getIntChecked(cursor, WEEK_ID)
                    w.subject = getStringChecked(cursor, WEEK_SUBJECT)
                    w.fragment = getStringChecked(cursor, WEEK_FRAGMENT)
                    w.teacher = getStringChecked(cursor, WEEK_TEACHER)
                    w.room = getStringChecked(cursor, WEEK_ROOM)
                    w.fromTime = getStringChecked(cursor, WEEK_FROM_TIME)
                    w.toTime = getStringChecked(cursor, WEEK_TO_TIME)
                    w.color = getIntChecked(cursor, WEEK_COLOR)
                    weeklist.add(w)
                } while (cursor.moveToNext())
            }
        }
        return weeklist
    }

    fun getAllWeeks(): ArrayList<Week> {
        val weeklist = ArrayList<Week>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TIMETABLE ORDER BY $WEEK_FRAGMENT, $WEEK_FROM_TIME ASC",
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val w = Week()
                    w.id = getIntChecked(cursor, WEEK_ID)
                    w.subject = getStringChecked(cursor, WEEK_SUBJECT)
                    w.fragment = getStringChecked(cursor, WEEK_FRAGMENT)
                    w.teacher = getStringChecked(cursor, WEEK_TEACHER)
                    w.room = getStringChecked(cursor, WEEK_ROOM)
                    w.fromTime = getStringChecked(cursor, WEEK_FROM_TIME)
                    w.toTime = getStringChecked(cursor, WEEK_TO_TIME)
                    w.color = getIntChecked(cursor, WEEK_COLOR)
                    weeklist.add(w)
                } while (cursor.moveToNext())
            }
        }
        return weeklist
    }

    fun getWeeksBySubject(subject: String): ArrayList<Week> {
        val weeklist = ArrayList<Week>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TIMETABLE WHERE $WEEK_SUBJECT = ? ORDER BY $WEEK_FRAGMENT, $WEEK_FROM_TIME ASC",
            arrayOf(subject)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val w = Week()
                    w.id = getIntChecked(cursor, WEEK_ID)
                    w.subject = getStringChecked(cursor, WEEK_SUBJECT)
                    w.fragment = getStringChecked(cursor, WEEK_FRAGMENT)
                    w.teacher = getStringChecked(cursor, WEEK_TEACHER)
                    w.room = getStringChecked(cursor, WEEK_ROOM)
                    w.fromTime = getStringChecked(cursor, WEEK_FROM_TIME)
                    w.toTime = getStringChecked(cursor, WEEK_TO_TIME)
                    w.color = getIntChecked(cursor, WEEK_COLOR)
                    weeklist.add(w)
                } while (cursor.moveToNext())
            }
        }
        return weeklist
    }

    /**
     * Methods for Assignments activity
     */
    fun insertHomework(homework: Homework) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(HOMEWORKS_SUBJECT, homework.subject)
            put(HOMEWORKS_TITLE, homework.title)
            put(HOMEWORKS_DESCRIPTION, homework.description)
            put(HOMEWORKS_DATE, homework.date)
            put(HOMEWORKS_COLOR, homework.color)
            put(HOMEWORKS_COMPLETED, homework.completed)
        }
        db.insert(HOMEWORKS, null, contentValues)

        insertSubject(homework.subject, homework.color, "", null)
    }

    fun updateHomework(homework: Homework) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(HOMEWORKS_SUBJECT, homework.subject)
            put(HOMEWORKS_TITLE, homework.title)
            put(HOMEWORKS_DESCRIPTION, homework.description)
            put(HOMEWORKS_DATE, homework.date)
            put(HOMEWORKS_COLOR, homework.color)
            put(HOMEWORKS_COMPLETED, homework.completed)
        }
        db.update(HOMEWORKS, contentValues, "$HOMEWORKS_ID = ?", arrayOf(homework.id.toString()))

        insertSubject(homework.subject, homework.color, "", null)
    }

    fun deleteHomeworkById(homework: Homework) {
        writableDatabase.delete(HOMEWORKS, "$HOMEWORKS_ID =? ", arrayOf(homework.id.toString()))
    }

    fun getHomework(): ArrayList<Homework> {
        val homeworklist = ArrayList<Homework>()
        readableDatabase.rawQuery("SELECT * FROM $HOMEWORKS ORDER BY $HOMEWORKS_DATE ASC", null).use { cursor ->
            while (cursor.moveToNext()) {
                val h = Homework()
                h.id = getIntChecked(cursor, HOMEWORKS_ID)
                h.subject = getStringChecked(cursor, HOMEWORKS_SUBJECT)
                h.title = getStringChecked(cursor, HOMEWORKS_TITLE)
                h.description = getStringChecked(cursor, HOMEWORKS_DESCRIPTION)
                h.date = getStringChecked(cursor, HOMEWORKS_DATE)
                h.color = getIntChecked(cursor, HOMEWORKS_COLOR)
                h.completed = getIntChecked(cursor, HOMEWORKS_COMPLETED)
                homeworklist.add(h)
            }
        }
        return homeworklist
    }

    /**
     * Methods for Notes activity
     */
    fun insertNote(note: Note): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(NOTES_TITLE, note.title)
            put(NOTES_TEXT, note.text)
            put(NOTES_COLOR, note.color)
            put(NOTES_SUBJECT_ID, note.subjectId)
        }
        return db.insert(NOTES, null, contentValues)
    }

    fun updateNote(note: Note) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(NOTES_TITLE, note.title)
            put(NOTES_TEXT, note.text)
            put(NOTES_COLOR, note.color)
        }
        db.update(NOTES, contentValues, "$NOTES_ID = ?", arrayOf(note.id.toString()))
    }

    fun deleteNoteById(id: Int) {
        writableDatabase.delete(NOTES, "$NOTES_ID =? ", arrayOf(id.toString()))
    }

    fun getNote(): ArrayList<Note> {
        val notelist = ArrayList<Note>()
        readableDatabase.rawQuery(
            "SELECT * FROM $NOTES ORDER BY $NOTES_SORT_ORDER ASC, $NOTES_ID DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val n = Note()
                n.id = getIntChecked(cursor, NOTES_ID)
                n.title = getStringChecked(cursor, NOTES_TITLE)
                n.text = getStringChecked(cursor, NOTES_TEXT)
                n.color = getIntChecked(cursor, NOTES_COLOR)
                n.subjectId = getIntChecked(cursor, NOTES_SUBJECT_ID)
                notelist.add(n)
            }
        }
        return notelist
    }

    fun getNotesBySubject(subjectId: Int): ArrayList<Note> {
        val notelist = ArrayList<Note>()
        readableDatabase.query(
            NOTES,
            null,
            "$NOTES_SUBJECT_ID=?",
            arrayOf(subjectId.toString()),
            null,
            null,
            "$NOTES_SORT_ORDER ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val n = Note()
                n.id = getIntChecked(cursor, NOTES_ID)
                n.title = getStringChecked(cursor, NOTES_TITLE)
                n.text = getStringChecked(cursor, NOTES_TEXT)
                n.color = getIntChecked(cursor, NOTES_COLOR)
                n.subjectId = getIntChecked(cursor, NOTES_SUBJECT_ID)
                notelist.add(n)
            }
        }
        return notelist
    }

    fun updateNoteSortOrder(noteId: Int, newOrder: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(NOTES_SORT_ORDER, newOrder)
        }
        db.update(NOTES, values, "$NOTES_ID=?", arrayOf(noteId.toString()))
    }

    /**
     * Methods for Teachers activity
     */
    fun insertTeacher(teacher: Teacher) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(TEACHERS_NAME, teacher.name)
            put(TEACHERS_POST, teacher.post)
            put(TEACHERS_PHONE_NUMBER, teacher.phonenumber)
            put(TEACHERS_EMAIL, teacher.email)
            put(TEACHERS_CABIN_NUMBER, teacher.cabinNumber)
            put(TEACHERS_COLOR, teacher.color)
        }
        db.insert(TEACHERS, null, contentValues)
    }

    fun updateTeacher(teacher: Teacher) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(TEACHERS_NAME, teacher.name)
            put(TEACHERS_POST, teacher.post)
            put(TEACHERS_PHONE_NUMBER, teacher.phonenumber)
            put(TEACHERS_EMAIL, teacher.email)
            put(TEACHERS_CABIN_NUMBER, teacher.cabinNumber)
            put(TEACHERS_COLOR, teacher.color)
        }
        db.update(TEACHERS, contentValues, "$TEACHERS_ID = ?", arrayOf(teacher.id.toString()))
    }

    fun deleteTeacherById(teacher: Teacher) {
        writableDatabase.delete(TEACHERS, "$TEACHERS_ID =? ", arrayOf(teacher.id.toString()))
    }

    fun getTeacher(): ArrayList<Teacher> {
        val teacherlist = ArrayList<Teacher>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TEACHERS ORDER BY $TEACHERS_SORT_ORDER ASC, $TEACHERS_NAME ASC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val t = Teacher()
                t.id = getIntChecked(cursor, TEACHERS_ID)
                t.name = getStringChecked(cursor, TEACHERS_NAME)
                t.post = getStringChecked(cursor, TEACHERS_POST)
                t.phonenumber = getStringChecked(cursor, TEACHERS_PHONE_NUMBER)
                t.email = getStringChecked(cursor, TEACHERS_EMAIL)
                t.cabinNumber = getStringChecked(cursor, TEACHERS_CABIN_NUMBER)
                t.color = getIntChecked(cursor, TEACHERS_COLOR)
                teacherlist.add(t)
            }
        }
        return teacherlist
    }

    fun updateTeacherSortOrder(teacherId: Int, newOrder: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TEACHERS_SORT_ORDER, newOrder)
        }
        db.update(TEACHERS, values, "$TEACHERS_ID=?", arrayOf(teacherId.toString()))
    }

    fun addTeacherIfNew(name: String?, color: Int) {
        if (name.isNullOrEmpty() || isTeacherInDb(name)) {
            return
        }
        val teacher = Teacher()
        teacher.name = name
        teacher.post = ""
        teacher.phonenumber = ""
        teacher.email = ""
        teacher.cabinNumber = ""
        teacher.color = color
        insertTeacher(teacher)
    }

    fun isTeacherInDb(name: String): Boolean {
        readableDatabase.query(
            TEACHERS,
            arrayOf(TEACHERS_NAME),
            "$TEACHERS_NAME=?",
            arrayOf(name),
            null,
            null,
            null
        ).use { cursor ->
            return cursor.count > 0
        }
    }

    fun insertSubject(name: String?, color: Int, teacher: String?, room: String?) {
        if (name.isNullOrEmpty()) {
            return
        }
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(SUBJECTS_NAME, name)
            put(SUBJECTS_COLOR, color)
            put(SUBJECTS_TEACHER, teacher)
        }

        if (isSubjectInDb(name)) {
            if (room != null) {
                contentValues.put(SUBJECTS_ROOM, room)
            }
            db.update(SUBJECTS, contentValues, "$SUBJECTS_NAME=?", arrayOf(name))
        } else {
            contentValues.put(SUBJECTS_ROOM, room ?: "")
            db.insert(SUBJECTS, null, contentValues)
        }
    }

    fun isSubjectInDb(name: String): Boolean {
        readableDatabase.query(
            SUBJECTS,
            arrayOf(SUBJECTS_NAME),
            "$SUBJECTS_NAME=?",
            arrayOf(name),
            null,
            null,
            null
        ).use { cursor ->
            return cursor.count > 0
        }
    }

    fun getSubjectsList(): ArrayList<String> {
        val subjects = ArrayList<String>()
        readableDatabase.query(
            SUBJECTS,
            arrayOf(SUBJECTS_NAME),
            null,
            null,
            null,
            null,
            "$SUBJECTS_NAME ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                subjects.add(cursor.getString(0))
            }
        }
        return subjects
    }

    fun getAllSubjects(): ArrayList<Subject> {
        val subjects = ArrayList<Subject>()
        readableDatabase.query(
            SUBJECTS,
            null,
            null,
            null,
            null,
            null,
            "$SUBJECTS_SORT_ORDER ASC, $SUBJECTS_NAME ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val s = Subject()
                s.id = getIntChecked(cursor, SUBJECTS_ID)
                s.name = getStringChecked(cursor, SUBJECTS_NAME)
                s.color = getIntChecked(cursor, SUBJECTS_COLOR)
                s.teacher = getStringChecked(cursor, SUBJECTS_TEACHER)
                s.room = getStringChecked(cursor, SUBJECTS_ROOM)
                s.attended = getIntChecked(cursor, SUBJECTS_ATTENDED)
                s.missed = getIntChecked(cursor, SUBJECTS_MISSED)
                s.skipped = getIntChecked(cursor, SUBJECTS_SKIPPED)
                s.credits = getIntChecked(cursor, SUBJECTS_CREDITS)
                s.gradePoint = getDoubleChecked(cursor, SUBJECTS_GRADE_POINT)
                subjects.add(s)
            }
        }
        return subjects
    }

    fun getTeachersForSubject(subjectName: String): String? {
        readableDatabase.query(
            true,
            TIMETABLE,
            arrayOf(WEEK_TEACHER),
            "$WEEK_SUBJECT=?",
            arrayOf(subjectName),
            null,
            null,
            null,
            null
        ).use { cursor ->
            val teachersList = ArrayList<String>()
            while (cursor.moveToNext()) {
                val t = cursor.getString(0)
                if (!t.isNullOrEmpty()) teachersList.add(t)
            }
            if (teachersList.isEmpty()) return null
            return TextUtils.join(", ", teachersList)
        }
    }

    fun updateSubjectName(id: Int, newName: String) {
        val db = writableDatabase
        var oldName: String? = null
        db.query(
            SUBJECTS,
            arrayOf(SUBJECTS_NAME),
            "$SUBJECTS_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                oldName = getStringChecked(cursor, SUBJECTS_NAME)
            }
        }

        val values = ContentValues().apply {
            put(SUBJECTS_NAME, newName)
        }
        db.update(SUBJECTS, values, "$SUBJECTS_ID=?", arrayOf(id.toString()))

        if (oldName != null && oldName != newName) {
            val cascade = ContentValues().apply {
                put(WEEK_SUBJECT, newName)
            }
            db.update(TIMETABLE, cascade, "$WEEK_SUBJECT=?", arrayOf(oldName))

            cascade.clear()
            cascade.put(HOMEWORKS_SUBJECT, newName)
            db.update(HOMEWORKS, cascade, "$HOMEWORKS_SUBJECT=?", arrayOf(oldName))

            cascade.clear()
            cascade.put(EXAMS_SUBJECT, newName)
            db.update(EXAMS, cascade, "$EXAMS_SUBJECT=?", arrayOf(oldName))
        }
    }

    fun updateSubject(subject: Subject?) {
        if (subject == null) return
        val id = subject.id
        val name = subject.name
        val color = subject.color
        val teacher = subject.teacher
        val room = subject.room

        val db = writableDatabase
        var oldName: String? = null
        db.query(
            SUBJECTS,
            arrayOf(SUBJECTS_NAME),
            "$SUBJECTS_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                oldName = getStringChecked(cursor, SUBJECTS_NAME)
            }
        }

        val values = ContentValues().apply {
            put(SUBJECTS_NAME, name)
            put(SUBJECTS_COLOR, color)
            put(SUBJECTS_TEACHER, teacher)
            put(SUBJECTS_ROOM, room)
            put(SUBJECTS_CREDITS, subject.credits)
            put(SUBJECTS_GRADE_POINT, subject.gradePoint)
        }

        db.update(SUBJECTS, values, "$SUBJECTS_ID=?", arrayOf(id.toString()))

        val targetName = oldName ?: name

        // 1. Force update colors in all related tables
        val colorUpdate = ContentValues().apply {
            put(WEEK_COLOR, color)
        }
        db.update(TIMETABLE, colorUpdate, "$WEEK_SUBJECT=?", arrayOf(targetName))

        colorUpdate.clear()
        colorUpdate.put(HOMEWORKS_COLOR, color)
        db.update(HOMEWORKS, colorUpdate, "$HOMEWORKS_SUBJECT=?", arrayOf(targetName))

        colorUpdate.clear()
        colorUpdate.put(EXAMS_COLOR, color)
        db.update(EXAMS, colorUpdate, "$EXAMS_SUBJECT=?", arrayOf(targetName))

        // 2. Cascade name change if name was modified
        if (oldName != null && oldName != name) {
            val nameUpdate = ContentValues().apply {
                put(WEEK_SUBJECT, name)
            }
            db.update(TIMETABLE, nameUpdate, "$WEEK_SUBJECT=?", arrayOf(oldName))

            nameUpdate.clear()
            nameUpdate.put(HOMEWORKS_SUBJECT, name)
            db.update(HOMEWORKS, nameUpdate, "$HOMEWORKS_SUBJECT=?", arrayOf(oldName))

            nameUpdate.clear()
            nameUpdate.put(EXAMS_SUBJECT, name)
            db.update(EXAMS, nameUpdate, "$EXAMS_SUBJECT=?", arrayOf(oldName))
        }
    }

    fun updateSubjectSortOrder(subjectId: Int, newOrder: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(SUBJECTS_SORT_ORDER, newOrder)
        }
        db.update(SUBJECTS, values, "$SUBJECTS_ID=?", arrayOf(subjectId.toString()))
    }

    fun deleteSubjectById(id: Int) {
        val db = writableDatabase
        var name: String? = null
        db.query(
            SUBJECTS,
            arrayOf(SUBJECTS_NAME),
            "$SUBJECTS_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                name = getStringChecked(cursor, SUBJECTS_NAME)
            }
        }

        db.delete(SUBJECTS, "$SUBJECTS_ID = ?", arrayOf(id.toString()))
        db.delete(NOTES, "$NOTES_SUBJECT_ID = ?", arrayOf(id.toString()))
        db.delete(MATERIALS, "$MATERIALS_SUBJECT_ID = ?", arrayOf(id.toString()))
        if (name != null) {
            db.delete(TIMETABLE, "$WEEK_SUBJECT = ?", arrayOf(name))
            db.delete(HOMEWORKS, "$HOMEWORKS_SUBJECT = ?", arrayOf(name))
            db.delete(EXAMS, "$EXAMS_SUBJECT = ?", arrayOf(name))
        }
    }

    fun getSubjectName(id: Int): String? {
        readableDatabase.query(
            SUBJECTS,
            arrayOf(SUBJECTS_NAME),
            "$SUBJECTS_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return getStringChecked(cursor, SUBJECTS_NAME)
            }
        }
        return null
    }

    fun getSubjectDetails(name: String): Week? {
        readableDatabase.query(
            SUBJECTS,
            arrayOf(SUBJECTS_COLOR, SUBJECTS_TEACHER, SUBJECTS_ROOM),
            "$SUBJECTS_NAME=?",
            arrayOf(name),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val w = Week()
                w.subject = name
                w.color = getIntChecked(cursor, SUBJECTS_COLOR)
                w.teacher = getStringChecked(cursor, SUBJECTS_TEACHER)
                w.room = getStringChecked(cursor, SUBJECTS_ROOM)
                return w
            }
        }
        return null
    }

    fun getTeachersList(): ArrayList<String> {
        val teachersList = ArrayList<String>()
        readableDatabase.query(
            TEACHERS,
            arrayOf(TEACHERS_NAME),
            null,
            null,
            null,
            null,
            "$TEACHERS_NAME ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                teachersList.add(cursor.getString(0))
            }
        }
        return teachersList
    }

    /**
     * Methods for Exams activity
     */
    fun insertExam(exam: Exam) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(EXAMS_SUBJECT, exam.subject)
            put(EXAMS_TEACHER, exam.teacher)
            put(EXAMS_ROOM, exam.room)
            put(EXAMS_DATE, exam.date)
            put(EXAMS_TIME, exam.time)
            put(EXAMS_COLOR, exam.color)
        }
        db.insert(EXAMS, null, contentValues)

        addTeacherIfNew(exam.teacher, exam.color)
        insertSubject(exam.subject, exam.color, exam.teacher, null)
    }

    fun updateExam(exam: Exam) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(EXAMS_SUBJECT, exam.subject)
            put(EXAMS_TEACHER, exam.teacher)
            put(EXAMS_ROOM, exam.room)
            put(EXAMS_DATE, exam.date)
            put(EXAMS_TIME, exam.time)
            put(EXAMS_COLOR, exam.color)
        }
        db.update(EXAMS, contentValues, "$EXAMS_ID = ?", arrayOf(exam.id.toString()))

        addTeacherIfNew(exam.teacher, exam.color)
        insertSubject(exam.subject, exam.color, exam.teacher, null)
    }

    fun deleteExamById(exam: Exam) {
        writableDatabase.delete(EXAMS, "$EXAMS_ID =? ", arrayOf(exam.id.toString()))
    }

    fun getExam(): ArrayList<Exam> {
        val examlist = ArrayList<Exam>()
        readableDatabase.rawQuery(
            "SELECT * FROM $EXAMS ORDER BY $EXAMS_DATE ASC, $EXAMS_TIME ASC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val e = Exam()
                e.id = getIntChecked(cursor, EXAMS_ID)
                e.subject = getStringChecked(cursor, EXAMS_SUBJECT)
                e.teacher = getStringChecked(cursor, EXAMS_TEACHER)
                e.room = getStringChecked(cursor, EXAMS_ROOM)
                e.date = getStringChecked(cursor, EXAMS_DATE)
                e.time = getStringChecked(cursor, EXAMS_TIME)
                e.color = getIntChecked(cursor, EXAMS_COLOR)
                examlist.add(e)
            }
        }
        return examlist
    }

    /**
     * Methods for Materials
     */
    fun insertMaterial(material: Material) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(MATERIALS_SUBJECT_ID, material.subjectId)
            put(MATERIALS_PATH, material.path)
            put(MATERIALS_TYPE, material.type)
            put(MATERIALS_NAME, material.name)
        }
        db.insert(MATERIALS, null, contentValues)
    }

    fun updateMaterialName(id: Int, newName: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(MATERIALS_NAME, newName)
        }
        db.update(MATERIALS, values, "$MATERIALS_ID=?", arrayOf(id.toString()))
    }

    fun deleteMaterialById(materialId: Int) {
        writableDatabase.delete(MATERIALS, "$MATERIALS_ID = ?", arrayOf(materialId.toString()))
    }

    fun getMaterialsBySubject(subjectId: Int): ArrayList<Material> {
        val materialsList = ArrayList<Material>()
        readableDatabase.query(
            MATERIALS,
            null,
            "$MATERIALS_SUBJECT_ID=?",
            arrayOf(subjectId.toString()),
            null,
            null,
            "$MATERIALS_SORT_ORDER ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val m = Material()
                m.id = getIntChecked(cursor, MATERIALS_ID)
                m.subjectId = getIntChecked(cursor, MATERIALS_SUBJECT_ID)
                m.path = getStringChecked(cursor, MATERIALS_PATH)
                m.type = getStringChecked(cursor, MATERIALS_TYPE)
                m.name = getStringChecked(cursor, MATERIALS_NAME)
                materialsList.add(m)
            }
        }
        return materialsList
    }

    fun updateMaterialSortOrder(materialId: Int, newOrder: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(MATERIALS_SORT_ORDER, newOrder)
        }
        db.update(MATERIALS, values, "$MATERIALS_ID=?", arrayOf(materialId.toString()))
    }

    /**
     * Methods for User Personal Details
     */
    fun getUserDetail(): UserDetail {
        val userDetail = UserDetail()
        readableDatabase.rawQuery("SELECT * FROM $USER_DETAILS LIMIT 1", null).use { cursor ->
            if (cursor.moveToFirst()) {
                userDetail.id = getIntChecked(cursor, USER_DETAILS_ID)
                userDetail.name = getStringChecked(cursor, USER_DETAILS_NAME)
                userDetail.email = getStringChecked(cursor, USER_DETAILS_EMAIL)
                userDetail.rollNumber = getStringChecked(cursor, USER_DETAILS_ROLL)
                userDetail.photoPath = getStringChecked(cursor, USER_DETAILS_PHOTO)
                userDetail.other = getStringChecked(cursor, USER_DETAILS_OTHER)
            }
        }
        return userDetail
    }

    fun saveUserDetail(detail: UserDetail) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(USER_DETAILS_NAME, detail.name)
            put(USER_DETAILS_EMAIL, detail.email)
            put(USER_DETAILS_ROLL, detail.rollNumber)
            put(USER_DETAILS_PHOTO, detail.photoPath)
            put(USER_DETAILS_OTHER, detail.other)
        }

        readableDatabase.rawQuery("SELECT * FROM $USER_DETAILS", null).use { cursor ->
            if (cursor.count > 0) {
                db.update(USER_DETAILS, values, null, null)
            } else {
                db.insert(USER_DETAILS, null, values)
            }
        }
    }

    /**
     * Methods for User Files
     */
    fun insertUserFile(file: UserFile) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(USER_FILES_TITLE, file.title)
            put(USER_FILES_PATH, file.path)
        }
        db.insert(USER_FILES, null, values)
    }

    fun getAllUserFiles(): ArrayList<UserFile> {
        val list = ArrayList<UserFile>()
        readableDatabase.rawQuery("SELECT * FROM $USER_FILES", null).use { cursor ->
            while (cursor.moveToNext()) {
                val f = UserFile()
                f.id = getIntChecked(cursor, USER_FILES_ID)
                f.title = getStringChecked(cursor, USER_FILES_TITLE)
                f.path = getStringChecked(cursor, USER_FILES_PATH)
                list.add(f)
            }
        }
        return list
    }

    fun deleteUserFile(id: Int) {
        writableDatabase.delete(USER_FILES, "$USER_FILES_ID=?", arrayOf(id.toString()))
    }

    fun updateAttendance(weekId: Int, subjectName: String, type: String, date: String) {
        val db = writableDatabase

        var oldType: String? = null
        db.query(
            ATTENDANCE, arrayOf(ATTENDANCE_STATUS),
            "$ATTENDANCE_SUBJECT_NAME = ? AND $ATTENDANCE_DATE = ? AND $ATTENDANCE_WEEK_ID = ?",
            arrayOf(subjectName, date, weekId.toString()), null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                oldType = cursor.getString(0)
            }
        }

        if (oldType == type) {
            return
        }

        if (oldType != null) {
            val oldColumn = getColumnNameForType(oldType!!)
            if (oldColumn != null) {
                db.execSQL("UPDATE $SUBJECTS SET $oldColumn = $oldColumn - 1 WHERE $SUBJECTS_NAME = ?", arrayOf(subjectName))
            }
        }

        val newColumn = getColumnNameForType(type)
        if (newColumn != null) {
            db.execSQL("UPDATE $SUBJECTS SET $newColumn = $newColumn + 1 WHERE $SUBJECTS_NAME = ?", arrayOf(subjectName))
        }

        val values = ContentValues().apply {
            put(ATTENDANCE_DATE, date)
            put(ATTENDANCE_WEEK_ID, weekId)
            put(ATTENDANCE_SUBJECT_NAME, subjectName)
            put(ATTENDANCE_STATUS, type)
        }

        if (oldType != null) {
            db.update(
                ATTENDANCE, values,
                "$ATTENDANCE_SUBJECT_NAME = ? AND $ATTENDANCE_DATE = ? AND $ATTENDANCE_WEEK_ID = ?",
                arrayOf(subjectName, date, weekId.toString())
            )
        } else {
            db.insert(ATTENDANCE, null, values)
        }
    }

    private fun getColumnNameForType(type: String): String? {
        return when (type) {
            "attended" -> SUBJECTS_ATTENDED
            "missed" -> SUBJECTS_MISSED
            "skipped" -> SUBJECTS_SKIPPED
            else -> null
        }
    }

    fun getAttendanceStatus(weekId: Int, date: String): String? {
        readableDatabase.query(
            ATTENDANCE, arrayOf(ATTENDANCE_STATUS),
            "$ATTENDANCE_WEEK_ID = ? AND $ATTENDANCE_DATE = ?",
            arrayOf(weekId.toString(), date), null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    fun getSlotsBySubject(name: String): ArrayList<Week> {
        val weeklist = ArrayList<Week>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TIMETABLE WHERE $WEEK_SUBJECT = ? ORDER BY $WEEK_FROM_TIME ASC",
            arrayOf(name)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val w = Week()
                    w.id = getIntChecked(cursor, WEEK_ID)
                    w.subject = getStringChecked(cursor, WEEK_SUBJECT)
                    w.fragment = getStringChecked(cursor, WEEK_FRAGMENT)
                    w.teacher = getStringChecked(cursor, WEEK_TEACHER)
                    w.room = getStringChecked(cursor, WEEK_ROOM)
                    w.fromTime = getStringChecked(cursor, WEEK_FROM_TIME)
                    w.toTime = getStringChecked(cursor, WEEK_TO_TIME)
                    w.color = getIntChecked(cursor, WEEK_COLOR)
                    weeklist.add(w)
                } while (cursor.moveToNext())
            }
        }
        return weeklist
    }

    fun updateAttendanceByDate(weekId: Int, subjectName: String, type: String, date: String) {
        updateAttendance(weekId, subjectName, type, date)
    }

    fun getAttendanceForSubject(subjectName: String): ArrayList<AttendanceRecord> {
        val records = ArrayList<AttendanceRecord>()
        readableDatabase.query(
            ATTENDANCE, arrayOf(ATTENDANCE_DATE, ATTENDANCE_STATUS, ATTENDANCE_WEEK_ID),
            "$ATTENDANCE_SUBJECT_NAME = ?", arrayOf(subjectName), null, null, null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val record = AttendanceRecord().apply {
                    date = cursor.getString(0)
                    status = cursor.getString(1)
                    weekId = cursor.getInt(2)
                }
                records.add(record)
            }
        }
        return records
    }

    fun deleteAttendanceRecord(weekId: Int, subjectName: String, date: String) {
        val db = writableDatabase

        var status: String? = null
        db.query(
            ATTENDANCE, arrayOf(ATTENDANCE_STATUS),
            "$ATTENDANCE_SUBJECT_NAME = ? AND $ATTENDANCE_DATE = ? AND $ATTENDANCE_WEEK_ID = ?",
            arrayOf(subjectName, date, weekId.toString()), null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                status = cursor.getString(0)
            }
        }

        if (status != null) {
            val column = getColumnNameForType(status!!)
            if (column != null) {
                db.execSQL("UPDATE $SUBJECTS SET $column = $column - 1 WHERE $SUBJECTS_NAME = ?", arrayOf(subjectName))
            }
        }

        db.delete(
            ATTENDANCE,
            "$ATTENDANCE_SUBJECT_NAME = ? AND $ATTENDANCE_DATE = ? AND $ATTENDANCE_WEEK_ID = ?",
            arrayOf(subjectName, date, weekId.toString())
        )
    }

    class AttendanceRecord {
        var date: String? = null
        var status: String? = null
        var weekId: Int = 0
    }

    fun getSubjectByName(name: String): Subject? {
        readableDatabase.query(
            SUBJECTS,
            null,
            "$SUBJECTS_NAME = ?",
            arrayOf(name),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val s = Subject()
                s.id = getIntChecked(cursor, SUBJECTS_ID)
                s.name = getStringChecked(cursor, SUBJECTS_NAME)
                s.color = getIntChecked(cursor, SUBJECTS_COLOR)
                s.teacher = getStringChecked(cursor, SUBJECTS_TEACHER)
                s.room = getStringChecked(cursor, SUBJECTS_ROOM)
                s.attended = getIntChecked(cursor, SUBJECTS_ATTENDED)
                s.missed = getIntChecked(cursor, SUBJECTS_MISSED)
                s.skipped = getIntChecked(cursor, SUBJECTS_SKIPPED)
                s.credits = getIntChecked(cursor, SUBJECTS_CREDITS)
                s.gradePoint = getDoubleChecked(cursor, SUBJECTS_GRADE_POINT)
                return s
            }
        }
        return null
    }

    fun searchWeeks(query: String): ArrayList<Week> {
        val list = ArrayList<Week>()
        val db = readableDatabase
        val q = "%$query%"
        db.rawQuery(
            "SELECT * FROM $TIMETABLE WHERE $WEEK_SUBJECT LIKE ? OR $WEEK_TEACHER LIKE ? ORDER BY $WEEK_FROM_TIME ASC",
            arrayOf(q, q)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val w = Week()
                    w.id = getIntChecked(cursor, WEEK_ID)
                    w.subject = getStringChecked(cursor, WEEK_SUBJECT)
                    w.fragment = getStringChecked(cursor, WEEK_FRAGMENT)
                    w.teacher = getStringChecked(cursor, WEEK_TEACHER)
                    w.room = getStringChecked(cursor, WEEK_ROOM)
                    w.fromTime = getStringChecked(cursor, WEEK_FROM_TIME)
                    w.toTime = getStringChecked(cursor, WEEK_TO_TIME)
                    w.color = getIntChecked(cursor, WEEK_COLOR)
                    list.add(w)
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun searchNotes(query: String): ArrayList<Note> {
        val list = ArrayList<Note>()
        val db = readableDatabase
        val q = "%$query%"
        db.rawQuery(
            "SELECT * FROM $NOTES WHERE $NOTES_TITLE LIKE ? OR $NOTES_TEXT LIKE ? ORDER BY $NOTES_ID DESC",
            arrayOf(q, q)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val n = Note()
                    n.id = getIntChecked(cursor, NOTES_ID)
                    n.title = getStringChecked(cursor, NOTES_TITLE)
                    n.text = getStringChecked(cursor, NOTES_TEXT)
                    n.color = getIntChecked(cursor, NOTES_COLOR)
                    n.subjectId = getIntChecked(cursor, NOTES_SUBJECT_ID)
                    list.add(n)
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun searchHomework(query: String): ArrayList<Homework> {
        val list = ArrayList<Homework>()
        val db = readableDatabase
        val q = "%$query%"
        db.rawQuery(
            "SELECT * FROM $HOMEWORKS WHERE $HOMEWORKS_TITLE LIKE ? OR $HOMEWORKS_SUBJECT LIKE ? OR $HOMEWORKS_DESCRIPTION LIKE ? ORDER BY $HOMEWORKS_DATE ASC",
            arrayOf(q, q, q)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val h = Homework()
                    h.id = getIntChecked(cursor, HOMEWORKS_ID)
                    h.subject = getStringChecked(cursor, HOMEWORKS_SUBJECT)
                    h.title = getStringChecked(cursor, HOMEWORKS_TITLE)
                    h.description = getStringChecked(cursor, HOMEWORKS_DESCRIPTION)
                    h.date = getStringChecked(cursor, HOMEWORKS_DATE)
                    h.color = getIntChecked(cursor, HOMEWORKS_COLOR)
                    h.completed = getIntChecked(cursor, HOMEWORKS_COMPLETED)
                    list.add(h)
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun searchTeachers(query: String): ArrayList<Teacher> {
        val list = ArrayList<Teacher>()
        val db = readableDatabase
        val q = "%$query%"
        db.rawQuery(
            "SELECT * FROM $TEACHERS WHERE $TEACHERS_NAME LIKE ? OR $TEACHERS_POST LIKE ? OR $TEACHERS_EMAIL LIKE ? ORDER BY $TEACHERS_NAME ASC",
            arrayOf(q, q, q)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val t = Teacher()
                    t.id = getIntChecked(cursor, TEACHERS_ID)
                    t.name = getStringChecked(cursor, TEACHERS_NAME)
                    t.post = getStringChecked(cursor, TEACHERS_POST)
                    t.phonenumber = getStringChecked(cursor, TEACHERS_PHONE_NUMBER)
                    t.email = getStringChecked(cursor, TEACHERS_EMAIL)
                    t.cabinNumber = getStringChecked(cursor, TEACHERS_CABIN_NUMBER)
                    t.color = getIntChecked(cursor, TEACHERS_COLOR)
                    list.add(t)
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    /**
     * Methods for Grade History
     */
    fun insertSemesterResult(result: SemesterResult): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(SEM_RES_NAME, result.semesterName)
            put(SEM_RES_GPA, result.gpa)
            put(SEM_RES_DATE, result.date)
        }
        val semId = db.insert(SEMESTER_RESULTS, null, values)

        for (grade in result.subjectGrades) {
            val gValues = ContentValues().apply {
                put(SUB_GRADES_SEM_ID, semId)
                put(SUB_GRADES_NAME, grade.subjectName)
                put(SUB_GRADES_GP, grade.gradePoint)
                put(SUB_GRADES_CREDITS, grade.credits)
            }
            db.insert(SUBJECT_GRADES, null, gValues)
        }
        return semId
    }

    fun getAllSemesterResults(): ArrayList<SemesterResult> {
        val results = ArrayList<SemesterResult>()
        readableDatabase.query(SEMESTER_RESULTS, null, null, null, null, null, "$SEM_RES_DATE DESC").use { cursor ->
            while (cursor.moveToNext()) {
                val id = getIntChecked(cursor, SEM_RES_ID)
                val name = getStringChecked(cursor, SEM_RES_NAME)
                val gpa = getDoubleChecked(cursor, SEM_RES_GPA)
                val date = getLongChecked(cursor, SEM_RES_DATE)

                val grades = getSubjectGradesBySemId(id)
                results.add(SemesterResult(id, name, gpa, date, grades))
            }
        }
        return results
    }

    private fun getSubjectGradesBySemId(semId: Int): ArrayList<SubjectGrade> {
        val grades = ArrayList<SubjectGrade>()
        readableDatabase.query(
            SUBJECT_GRADES,
            null,
            "$SUB_GRADES_SEM_ID=?",
            arrayOf(semId.toString()),
            null,
            null,
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = getIntChecked(cursor, SUB_GRADES_ID)
                val name = getStringChecked(cursor, SUB_GRADES_NAME)
                val gp = getDoubleChecked(cursor, SUB_GRADES_GP)
                val credits = getIntChecked(cursor, SUB_GRADES_CREDITS)
                grades.add(SubjectGrade(id, semId, name, gp, credits))
            }
        }
        return grades
    }

    fun deleteSemesterResult(id: Int) {
        writableDatabase.delete(SEMESTER_RESULTS, "$SEM_RES_ID=?", arrayOf(id.toString()))
        // Subject grades will be deleted by cascade
    }

    companion object {
        private const val DB_VERSION = 17
        private const val DB_NAME = "timetabledb"

        private const val TIMETABLE = "timetable"
        private const val WEEK_ID = "id"
        private const val WEEK_SUBJECT = "subject"
        private const val WEEK_FRAGMENT = "fragment"
        private const val WEEK_TEACHER = "teacher"
        private const val WEEK_ROOM = "room"
        private const val WEEK_FROM_TIME = "fromtime"
        private const val WEEK_TO_TIME = "totime"
        private const val WEEK_COLOR = "color"

        const val SUBJECTS = "subjects"
        const val SUBJECTS_ID = "id"
        const val SUBJECTS_NAME = "name"
        const val SUBJECTS_COLOR = "color"
        const val SUBJECTS_TEACHER = "teacher"
        const val SUBJECTS_ROOM = "room"
        const val SUBJECTS_SORT_ORDER = "sort_order"
        const val SUBJECTS_ATTENDED = "attended"
        const val SUBJECTS_MISSED = "missed"
        const val SUBJECTS_SKIPPED = "skipped"
        const val SUBJECTS_CREDITS = "credits"
        const val SUBJECTS_GRADE_POINT = "grade_point"

        private const val SEMESTER_RESULTS = "semester_results"
        private const val SEM_RES_ID = "id"
        private const val SEM_RES_NAME = "semester_name"
        private const val SEM_RES_GPA = "gpa"
        private const val SEM_RES_DATE = "date"

        private const val SUBJECT_GRADES = "subject_grades"
        private const val SUB_GRADES_ID = "id"
        private const val SUB_GRADES_SEM_ID = "semester_id"
        private const val SUB_GRADES_NAME = "subject_name"
        private const val SUB_GRADES_GP = "grade_point"
        private const val SUB_GRADES_CREDITS = "credits"

        private const val HOMEWORKS = "homeworks"
        private const val HOMEWORKS_ID = "id"
        private const val HOMEWORKS_SUBJECT = "subject"
        private const val HOMEWORKS_TITLE = "title"
        private const val HOMEWORKS_DESCRIPTION = "description"
        private const val HOMEWORKS_DATE = "date"
        private const val HOMEWORKS_COLOR = "color"
        private const val HOMEWORKS_COMPLETED = "completed"

        private const val NOTES = "notes"
        private const val NOTES_ID = "id"
        private const val NOTES_TITLE = "title"
        private const val NOTES_TEXT = "text"
        private const val NOTES_COLOR = "color"
        private const val NOTES_SUBJECT_ID = "subject_id"
        private const val NOTES_SORT_ORDER = "sort_order"

        private const val TEACHERS = "teachers"
        private const val TEACHERS_ID = "id"
        private const val TEACHERS_NAME = "name"
        private const val TEACHERS_POST = "post"
        private const val TEACHERS_PHONE_NUMBER = "phonenumber"
        private const val TEACHERS_EMAIL = "email"
        private const val TEACHERS_CABIN_NUMBER = "cabinnumber"
        private const val TEACHERS_COLOR = "color"
        private const val TEACHERS_SORT_ORDER = "sort_order"

        private const val EXAMS = "exams"
        private const val EXAMS_ID = "id"
        private const val EXAMS_SUBJECT = "subject"
        private const val EXAMS_TEACHER = "teacher"
        private const val EXAMS_ROOM = "room"
        private const val EXAMS_DATE = "date"
        private const val EXAMS_TIME = "time"
        private const val EXAMS_COLOR = "color"

        private const val MATERIALS = "materials"
        private const val MATERIALS_ID = "id"
        private const val MATERIALS_SUBJECT_ID = "subject_id"
        private const val MATERIALS_PATH = "path"
        private const val MATERIALS_TYPE = "type"
        private const val MATERIALS_NAME = "name"
        private const val MATERIALS_SORT_ORDER = "sort_order"

        private const val USER_DETAILS = "user_details"
        private const val USER_DETAILS_ID = "id"
        private const val USER_DETAILS_NAME = "name"
        private const val USER_DETAILS_EMAIL = "email"
        private const val USER_DETAILS_ROLL = "roll_number"
        private const val USER_DETAILS_PHOTO = "photo_path"
        private const val USER_DETAILS_OTHER = "other"

        private const val USER_FILES = "user_files"
        private const val USER_FILES_ID = "id"
        private const val USER_FILES_TITLE = "title"
        private const val USER_FILES_PATH = "path"

        private const val ATTENDANCE = "attendance_records"
        private const val ATTENDANCE_ID = "id"
        private const val ATTENDANCE_DATE = "date"
        private const val ATTENDANCE_WEEK_ID = "week_id"
        private const val ATTENDANCE_SUBJECT_NAME = "subject_name"
        private const val ATTENDANCE_STATUS = "status"
    }
}
