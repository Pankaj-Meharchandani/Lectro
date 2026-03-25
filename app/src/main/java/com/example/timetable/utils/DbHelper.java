package com.example.timetable.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.timetable.model.Exam;
import com.example.timetable.model.Homework;
import com.example.timetable.model.Material;
import com.example.timetable.model.Note;
import com.example.timetable.model.Subject;
import com.example.timetable.model.Teacher;
import com.example.timetable.model.UserDetail;
import com.example.timetable.model.UserFile;
import com.example.timetable.model.Week;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 16;
    private static final String DB_NAME = "timetabledb";

    private static final String TIMETABLE = "timetable";
    private static final String WEEK_ID = "id";
    private static final String WEEK_SUBJECT = "subject";
    private static final String WEEK_FRAGMENT = "fragment";
    private static final String WEEK_TEACHER = "teacher";
    private static final String WEEK_ROOM = "room";
    private static final String WEEK_FROM_TIME = "fromtime";
    private static final String WEEK_TO_TIME = "totime";
    private static final String WEEK_COLOR = "color";

    public static final String SUBJECTS = "subjects";
    public static final String SUBJECTS_ID = "id";
    public static final String SUBJECTS_NAME = "name";
    public static final String SUBJECTS_COLOR = "color";
    public static final String SUBJECTS_TEACHER = "teacher";
    public static final String SUBJECTS_ROOM = "room";
    public static final String SUBJECTS_SORT_ORDER = "sort_order";
    public static final String SUBJECTS_ATTENDED = "attended";
    public static final String SUBJECTS_MISSED = "missed";
    public static final String SUBJECTS_SKIPPED = "skipped";

    private static final String HOMEWORKS = "homeworks";
    private static final String HOMEWORKS_ID = "id";
    private static final String HOMEWORKS_SUBJECT = "subject";
    private static final String HOMEWORKS_TITLE = "title";
    private static final String HOMEWORKS_DESCRIPTION = "description";
    private static final String HOMEWORKS_DATE = "date";
    private static final String HOMEWORKS_COLOR = "color";
    private static final String HOMEWORKS_COMPLETED = "completed";

    private static final String NOTES = "notes";
    private static final String NOTES_ID = "id";
    private static final String NOTES_TITLE = "title";
    private static final String NOTES_TEXT = "text";
    private static final String NOTES_COLOR = "color";
    private static final String NOTES_SUBJECT_ID = "subject_id";
    private static final String NOTES_SORT_ORDER = "sort_order";

    private static final String TEACHERS = "teachers";
    private static final String TEACHERS_ID = "id";
    private static final String TEACHERS_NAME = "name";
    private static final String TEACHERS_POST = "post";
    private static final String TEACHERS_PHONE_NUMBER = "phonenumber";
    private static final String TEACHERS_EMAIL = "email";
    private static final String TEACHERS_CABIN_NUMBER = "cabinnumber";
    private static final String TEACHERS_COLOR = "color";
    private static final String TEACHERS_SORT_ORDER = "sort_order";

    private static final String EXAMS = "exams";
    private static final String EXAMS_ID = "id";
    private static final String EXAMS_SUBJECT = "subject";
    private static final String EXAMS_TEACHER = "teacher";
    private static final String EXAMS_ROOM = "room";
    private static final String EXAMS_DATE = "date";
    private static final String EXAMS_TIME = "time";
    private static final String EXAMS_COLOR = "color";

    private static final String MATERIALS = "materials";
    private static final String MATERIALS_ID = "id";
    private static final String MATERIALS_SUBJECT_ID = "subject_id";
    private static final String MATERIALS_PATH = "path";
    private static final String MATERIALS_TYPE = "type";
    private static final String MATERIALS_NAME = "name";
    private static final String MATERIALS_SORT_ORDER = "sort_order";

    private static final String USER_DETAILS = "user_details";
    private static final String USER_DETAILS_ID = "id";
    private static final String USER_DETAILS_NAME = "name";
    private static final String USER_DETAILS_EMAIL = "email";
    private static final String USER_DETAILS_ROLL = "roll_number";
    private static final String USER_DETAILS_PHOTO = "photo_path";
    private static final String USER_DETAILS_OTHER = "other";

    private static final String USER_FILES = "user_files";
    private static final String USER_FILES_ID = "id";
    private static final String USER_FILES_TITLE = "title";
    private static final String USER_FILES_PATH = "path";

    private static final String ATTENDANCE = "attendance_records";
    private static final String ATTENDANCE_ID = "id";
    private static final String ATTENDANCE_DATE = "date";
    private static final String ATTENDANCE_WEEK_ID = "week_id";
    private static final String ATTENDANCE_SUBJECT_NAME = "subject_name";
    private static final String ATTENDANCE_STATUS = "status";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TIMETABLE = "CREATE TABLE " + TIMETABLE + "("
                + WEEK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WEEK_SUBJECT + " TEXT,"
                + WEEK_FRAGMENT + " TEXT,"
                + WEEK_TEACHER + " TEXT,"
                + WEEK_ROOM + " TEXT,"
                + WEEK_FROM_TIME + " TEXT,"
                + WEEK_TO_TIME + " TEXT,"
                + WEEK_COLOR + " INTEGER" + ")";

        String CREATE_HOMEWORKS = "CREATE TABLE " + HOMEWORKS + "("
                + HOMEWORKS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HOMEWORKS_SUBJECT + " TEXT,"
                + HOMEWORKS_TITLE + " TEXT,"
                + HOMEWORKS_DESCRIPTION + " TEXT,"
                + HOMEWORKS_DATE + " TEXT,"
                + HOMEWORKS_COLOR + " INTEGER,"
                + HOMEWORKS_COMPLETED + " INTEGER DEFAULT 0" + ")";

        String CREATE_NOTES = "CREATE TABLE " + NOTES + "("
                + NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NOTES_TITLE + " TEXT,"
                + NOTES_TEXT + " TEXT,"
                + NOTES_COLOR + " INTEGER,"
                + NOTES_SUBJECT_ID + " INTEGER DEFAULT -1,"
                + NOTES_SORT_ORDER + " INTEGER DEFAULT 0" + ")";

        String CREATE_TEACHERS = "CREATE TABLE " + TEACHERS + "("
                + TEACHERS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TEACHERS_NAME + " TEXT,"
                + TEACHERS_POST + " TEXT,"
                + TEACHERS_PHONE_NUMBER + " TEXT,"
                + TEACHERS_EMAIL + " TEXT,"
                + TEACHERS_CABIN_NUMBER + " TEXT,"
                + TEACHERS_COLOR + " INTEGER,"
                + TEACHERS_SORT_ORDER + " INTEGER DEFAULT 0" + ")";

        String CREATE_EXAMS = "CREATE TABLE " + EXAMS + "("
                + EXAMS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + EXAMS_SUBJECT + " TEXT,"
                + EXAMS_TEACHER + " TEXT,"
                + EXAMS_ROOM + " TEXT,"
                + EXAMS_DATE + " TEXT,"
                + EXAMS_TIME + " TEXT,"
                + EXAMS_COLOR + " INTEGER" + ")";

        String CREATE_SUBJECTS = "CREATE TABLE " + SUBJECTS + "("
                + SUBJECTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SUBJECTS_NAME + " TEXT,"
                + SUBJECTS_COLOR + " INTEGER,"
                + SUBJECTS_TEACHER + " TEXT,"
                + SUBJECTS_ROOM + " TEXT,"
                + SUBJECTS_SORT_ORDER + " INTEGER DEFAULT 0,"
                + SUBJECTS_ATTENDED + " INTEGER DEFAULT 0,"
                + SUBJECTS_MISSED + " INTEGER DEFAULT 0,"
                + SUBJECTS_SKIPPED + " INTEGER DEFAULT 0" + ")";

        String CREATE_MATERIALS = "CREATE TABLE " + MATERIALS + "("
                + MATERIALS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MATERIALS_SUBJECT_ID + " INTEGER,"
                + MATERIALS_PATH + " TEXT,"
                + MATERIALS_TYPE + " TEXT,"
                + MATERIALS_NAME + " TEXT,"
                + MATERIALS_SORT_ORDER + " INTEGER DEFAULT 0" + ")";

        String CREATE_USER_DETAILS = "CREATE TABLE " + USER_DETAILS + "("
                + USER_DETAILS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USER_DETAILS_NAME + " TEXT,"
                + USER_DETAILS_EMAIL + " TEXT,"
                + USER_DETAILS_ROLL + " TEXT,"
                + USER_DETAILS_PHOTO + " TEXT,"
                + USER_DETAILS_OTHER + " TEXT" + ")";

        String CREATE_USER_FILES = "CREATE TABLE " + USER_FILES + "("
                + USER_FILES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USER_FILES_TITLE + " TEXT,"
                + USER_FILES_PATH + " TEXT" + ")";

        String CREATE_ATTENDANCE = "CREATE TABLE " + ATTENDANCE + "("
                + ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ATTENDANCE_DATE + " TEXT,"
                + ATTENDANCE_WEEK_ID + " INTEGER,"
                + ATTENDANCE_SUBJECT_NAME + " TEXT,"
                + ATTENDANCE_STATUS + " TEXT" + ")";

        db.execSQL(CREATE_TIMETABLE);
        db.execSQL(CREATE_HOMEWORKS);
        db.execSQL(CREATE_NOTES);
        db.execSQL(CREATE_TEACHERS);
        db.execSQL(CREATE_EXAMS);
        db.execSQL(CREATE_SUBJECTS);
        db.execSQL(CREATE_MATERIALS);
        db.execSQL(CREATE_USER_DETAILS);
        db.execSQL(CREATE_USER_FILES);
        db.execSQL(CREATE_ATTENDANCE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS " + TIMETABLE);
            db.execSQL("DROP TABLE IF EXISTS " + HOMEWORKS);
            db.execSQL("DROP TABLE IF EXISTS " + NOTES);
            db.execSQL("DROP TABLE IF EXISTS " + TEACHERS);
            db.execSQL("DROP TABLE IF EXISTS " + EXAMS);
            onCreate(db);
        } else if (oldVersion == 6) {
            db.execSQL("CREATE TABLE " + SUBJECTS + "("
                    + SUBJECTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SUBJECTS_NAME + " TEXT,"
                    + SUBJECTS_COLOR + " INTEGER,"
                    + SUBJECTS_TEACHER + " TEXT,"
                    + SUBJECTS_ROOM + " TEXT)");
            onUpgrade(db, 7, newVersion);
        } else if (oldVersion == 7) {
            db.execSQL("ALTER TABLE " + NOTES + " ADD COLUMN " + NOTES_SUBJECT_ID + " INTEGER DEFAULT -1");
            db.execSQL("CREATE TABLE " + MATERIALS + "("
                    + MATERIALS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + MATERIALS_SUBJECT_ID + " INTEGER,"
                    + MATERIALS_PATH + " TEXT,"
                    + MATERIALS_TYPE + " TEXT,"
                    + MATERIALS_NAME + " TEXT)");
            onUpgrade(db, 8, newVersion);
        } else if (oldVersion == 8) {
            db.execSQL("ALTER TABLE " + SUBJECTS + " ADD COLUMN " + SUBJECTS_SORT_ORDER + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + NOTES + " ADD COLUMN " + NOTES_SORT_ORDER + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + MATERIALS + " ADD COLUMN " + MATERIALS_SORT_ORDER + " INTEGER DEFAULT 0");
            onUpgrade(db, 9, newVersion);
        } else if (oldVersion == 9) {
            db.execSQL("ALTER TABLE " + TEACHERS + " ADD COLUMN " + TEACHERS_SORT_ORDER + " INTEGER DEFAULT 0");
            onUpgrade(db, 10, newVersion);
        } else if (oldVersion == 10) {
            db.execSQL("ALTER TABLE " + HOMEWORKS + " ADD COLUMN " + HOMEWORKS_TITLE + " TEXT");
            db.execSQL("ALTER TABLE " + HOMEWORKS + " ADD COLUMN " + HOMEWORKS_COMPLETED + " INTEGER DEFAULT 0");
            onUpgrade(db, 11, newVersion);
        } else if (oldVersion == 11) {
            db.execSQL("ALTER TABLE " + TEACHERS + " ADD COLUMN " + TEACHERS_CABIN_NUMBER + " TEXT");
            onUpgrade(db, 12, newVersion);
        } else if (oldVersion == 12) {
            db.execSQL("CREATE TABLE " + USER_DETAILS + "("
                    + USER_DETAILS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + USER_DETAILS_NAME + " TEXT,"
                    + USER_DETAILS_EMAIL + " TEXT,"
                    + USER_DETAILS_ROLL + " TEXT,"
                    + USER_DETAILS_PHOTO + " TEXT,"
                    + USER_DETAILS_OTHER + " TEXT)");
            db.execSQL("CREATE TABLE " + USER_FILES + "("
                    + USER_FILES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + USER_FILES_TITLE + " TEXT,"
                    + USER_FILES_PATH + " TEXT)");
            onUpgrade(db, 13, newVersion);
        } else if (oldVersion == 13) {
            db.execSQL("ALTER TABLE " + SUBJECTS + " ADD COLUMN " + SUBJECTS_ATTENDED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + SUBJECTS + " ADD COLUMN " + SUBJECTS_MISSED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + SUBJECTS + " ADD COLUMN " + SUBJECTS_SKIPPED + " INTEGER DEFAULT 0");
            onUpgrade(db, 14, newVersion);
        } else if (oldVersion == 14) {
            db.execSQL("CREATE TABLE " + ATTENDANCE + "("
                    + ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ATTENDANCE_DATE + " TEXT,"
                    + ATTENDANCE_WEEK_ID + " INTEGER,"
                    + ATTENDANCE_STATUS + " TEXT)");
            onUpgrade(db, 15, newVersion);
        } else if (oldVersion == 15) {
            db.execSQL("ALTER TABLE " + ATTENDANCE + " ADD COLUMN " + ATTENDANCE_SUBJECT_NAME + " TEXT");
            // Populate subject_name for existing records if any
            db.execSQL("UPDATE " + ATTENDANCE + " SET " + ATTENDANCE_SUBJECT_NAME + " = (SELECT " + WEEK_SUBJECT + " FROM " + TIMETABLE + " WHERE " + TIMETABLE + "." + WEEK_ID + " = " + ATTENDANCE + "." + ATTENDANCE_WEEK_ID + ") WHERE " + ATTENDANCE_WEEK_ID + " > 0");
        }
    }

    private String getStringChecked(Cursor cursor, String columnName) {
        int idx = cursor.getColumnIndex(columnName);
        return (idx != -1) ? cursor.getString(idx) : "";
    }

    private int getIntChecked(Cursor cursor, String columnName) {
        int idx = cursor.getColumnIndex(columnName);
        return (idx != -1) ? cursor.getInt(idx) : 0;
    }

    public void resetAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TIMETABLE);
        db.execSQL("DELETE FROM " + HOMEWORKS);
        db.execSQL("DELETE FROM " + NOTES);
        db.execSQL("DELETE FROM " + TEACHERS);
        db.execSQL("DELETE FROM " + EXAMS);
        db.execSQL("DELETE FROM " + SUBJECTS);
        db.execSQL("DELETE FROM " + MATERIALS);
        db.execSQL("DELETE FROM " + ATTENDANCE);
        db.close();
    }

    public void removeFullSchedule() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TIMETABLE);
        db.close();
    }

    public void removeAllSubjects() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + SUBJECTS);
        db.execSQL("DELETE FROM " + TIMETABLE);
        db.execSQL("DELETE FROM " + MATERIALS);
        db.execSQL("DELETE FROM " + ATTENDANCE);
        db.close();
    }

    /**
     * Methods for Week fragments
     **/
    public void insertWeek(Week week) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(WEEK_SUBJECT, week.getSubject());
        contentValues.put(WEEK_FRAGMENT, week.getFragment());
        contentValues.put(WEEK_TEACHER, week.getTeacher());
        contentValues.put(WEEK_ROOM, week.getRoom());
        contentValues.put(WEEK_FROM_TIME, week.getFromTime());
        contentValues.put(WEEK_TO_TIME, week.getToTime());
        contentValues.put(WEEK_COLOR, week.getColor());
        db.insert(TIMETABLE, null, contentValues);
        db.close();

        addTeacherIfNew(week.getTeacher(), week.getColor());
        insertSubject(week.getSubject(), week.getColor(), week.getTeacher(), week.getRoom());
    }

    public void deleteWeekById(Week week) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TIMETABLE, WEEK_ID + " = ? ", new String[]{String.valueOf(week.getId())});
        db.close();
    }

    public void updateWeek(Week week) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(WEEK_SUBJECT, week.getSubject());
        contentValues.put(WEEK_TEACHER, week.getTeacher());
        contentValues.put(WEEK_ROOM, week.getRoom());
        contentValues.put(WEEK_FROM_TIME, week.getFromTime());
        contentValues.put(WEEK_TO_TIME, week.getToTime());
        contentValues.put(WEEK_COLOR, week.getColor());
        db.update(TIMETABLE, contentValues, WEEK_ID + " = ?", new String[]{String.valueOf(week.getId())});
        db.close();

        addTeacherIfNew(week.getTeacher(), week.getColor());
        insertSubject(week.getSubject(), week.getColor(), week.getTeacher(), week.getRoom());
    }

    public ArrayList<Week> getWeek(String fragment) {
        ArrayList<Week> weeklist = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TIMETABLE + " WHERE " + WEEK_FRAGMENT + " = ? ORDER BY " + WEEK_FROM_TIME + " ASC", new String[]{fragment});
        if (cursor.moveToFirst()) {
            do {
                Week week = new Week();
                week.setId(getIntChecked(cursor, WEEK_ID));
                week.setSubject(getStringChecked(cursor, WEEK_SUBJECT));
                week.setFragment(getStringChecked(cursor, WEEK_FRAGMENT));
                week.setTeacher(getStringChecked(cursor, WEEK_TEACHER));
                week.setRoom(getStringChecked(cursor, WEEK_ROOM));
                week.setFromTime(getStringChecked(cursor, WEEK_FROM_TIME));
                week.setToTime(getStringChecked(cursor, WEEK_TO_TIME));
                week.setColor(getIntChecked(cursor, WEEK_COLOR));
                weeklist.add(week);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return weeklist;
    }

    /**
     * Methods for Assignments activity
     **/
    public void insertHomework(Homework homework) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOMEWORKS_SUBJECT, homework.getSubject());
        contentValues.put(HOMEWORKS_TITLE, homework.getTitle());
        contentValues.put(HOMEWORKS_DESCRIPTION, homework.getDescription());
        contentValues.put(HOMEWORKS_DATE, homework.getDate());
        contentValues.put(HOMEWORKS_COLOR, homework.getColor());
        contentValues.put(HOMEWORKS_COMPLETED, homework.getCompleted());
        db.insert(HOMEWORKS, null, contentValues);
        db.close();

        insertSubject(homework.getSubject(), homework.getColor(), "", null);
    }

    public void updateHomework(Homework homework) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOMEWORKS_SUBJECT, homework.getSubject());
        contentValues.put(HOMEWORKS_TITLE, homework.getTitle());
        contentValues.put(HOMEWORKS_DESCRIPTION, homework.getDescription());
        contentValues.put(HOMEWORKS_DATE, homework.getDate());
        contentValues.put(HOMEWORKS_COLOR, homework.getColor());
        contentValues.put(HOMEWORKS_COMPLETED, homework.getCompleted());
        db.update(HOMEWORKS, contentValues, HOMEWORKS_ID + " = ?", new String[]{String.valueOf(homework.getId())});
        db.close();

        insertSubject(homework.getSubject(), homework.getColor(), "", null);
    }

    public void deleteHomeworkById(Homework homework) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(HOMEWORKS, HOMEWORKS_ID + " =? ", new String[]{String.valueOf(homework.getId())});
        db.close();
    }


    public ArrayList<Homework> getHomework() {
        ArrayList<Homework> homeworklist = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + HOMEWORKS + " ORDER BY " + HOMEWORKS_DATE + " ASC", null);
        while (cursor.moveToNext()) {
            Homework homework = new Homework();
            homework.setId(getIntChecked(cursor, HOMEWORKS_ID));
            homework.setSubject(getStringChecked(cursor, HOMEWORKS_SUBJECT));
            homework.setTitle(getStringChecked(cursor, HOMEWORKS_TITLE));
            homework.setDescription(getStringChecked(cursor, HOMEWORKS_DESCRIPTION));
            homework.setDate(getStringChecked(cursor, HOMEWORKS_DATE));
            homework.setColor(getIntChecked(cursor, HOMEWORKS_COLOR));
            homework.setCompleted(getIntChecked(cursor, HOMEWORKS_COMPLETED));
            homeworklist.add(homework);
        }
        cursor.close();
        db.close();
        return homeworklist;
    }

    /**
     * Methods for Notes activity
     **/
    public long insertNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NOTES_TITLE, note.getTitle());
        contentValues.put(NOTES_TEXT, note.getText());
        contentValues.put(NOTES_COLOR, note.getColor());
        contentValues.put(NOTES_SUBJECT_ID, note.getSubjectId());
        long id = db.insert(NOTES, null, contentValues);
        db.close();
        return id;
    }

    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NOTES_TITLE, note.getTitle());
        contentValues.put(NOTES_TEXT, note.getText());
        contentValues.put(NOTES_COLOR, note.getColor());
        db.update(NOTES, contentValues, NOTES_ID + " = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public void deleteNoteById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NOTES, NOTES_ID + " =? ", new String[]{String.valueOf(id)});
        db.close();
    }

    public ArrayList<Note> getNote() {
        ArrayList<Note> notelist = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + NOTES + " ORDER BY " + NOTES_SORT_ORDER + " ASC, " + NOTES_ID + " DESC", null);
        while (cursor.moveToNext()) {
            Note note = new Note();
            note.setId(getIntChecked(cursor, NOTES_ID));
            note.setTitle(getStringChecked(cursor, NOTES_TITLE));
            note.setText(getStringChecked(cursor, NOTES_TEXT));
            note.setColor(getIntChecked(cursor, NOTES_COLOR));
            note.setSubjectId(getIntChecked(cursor, NOTES_SUBJECT_ID));
            notelist.add(note);
        }
        cursor.close();
        db.close();
        return notelist;
    }

    public ArrayList<Note> getNotesBySubject(int subjectId) {
        ArrayList<Note> notelist = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(NOTES, null, NOTES_SUBJECT_ID + "=?", new String[]{String.valueOf(subjectId)}, null, null, NOTES_SORT_ORDER + " ASC");
        while (cursor.moveToNext()) {
            Note note = new Note();
            note.setId(getIntChecked(cursor, NOTES_ID));
            note.setTitle(getStringChecked(cursor, NOTES_TITLE));
            note.setText(getStringChecked(cursor, NOTES_TEXT));
            note.setColor(getIntChecked(cursor, NOTES_COLOR));
            note.setSubjectId(getIntChecked(cursor, NOTES_SUBJECT_ID));
            notelist.add(note);
        }
        cursor.close();
        db.close();
        return notelist;
    }

    public void updateNoteSortOrder(int noteId, int newOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NOTES_SORT_ORDER, newOrder);
        db.update(NOTES, values, NOTES_ID + "=?", new String[]{String.valueOf(noteId)});
        db.close();
    }

    /**
     * Methods for Teachers activity
     **/
    public void insertTeacher(Teacher teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TEACHERS_NAME, teacher.getName());
        contentValues.put(TEACHERS_POST, teacher.getPost());
        contentValues.put(TEACHERS_PHONE_NUMBER, teacher.getPhonenumber());
        contentValues.put(TEACHERS_EMAIL, teacher.getEmail());
        contentValues.put(TEACHERS_CABIN_NUMBER, teacher.getCabinNumber());
        contentValues.put(TEACHERS_COLOR, teacher.getColor());
        db.insert(TEACHERS, null, contentValues);
        db.close();
    }

    public void updateTeacher(Teacher teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TEACHERS_NAME, teacher.getName());
        contentValues.put(TEACHERS_POST, teacher.getPost());
        contentValues.put(TEACHERS_PHONE_NUMBER, teacher.getPhonenumber());
        contentValues.put(TEACHERS_EMAIL, teacher.getEmail());
        contentValues.put(TEACHERS_CABIN_NUMBER, teacher.getCabinNumber());
        contentValues.put(TEACHERS_COLOR, teacher.getColor());
        db.update(TEACHERS, contentValues, TEACHERS_ID + " = ?", new String[]{String.valueOf(teacher.getId())});
        db.close();
    }

    public void deleteTeacherById(Teacher teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TEACHERS, TEACHERS_ID + " =? ", new String[]{String.valueOf(teacher.getId())});
        db.close();
    }

    public ArrayList<Teacher> getTeacher() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Teacher> teacherlist = new ArrayList<>();
        Teacher teacher;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TEACHERS + " ORDER BY " + TEACHERS_SORT_ORDER + " ASC, " + TEACHERS_NAME + " ASC", null);
        while (cursor.moveToNext()) {
            teacher = new Teacher();
            teacher.setId(getIntChecked(cursor, TEACHERS_ID));
            teacher.setName(getStringChecked(cursor, TEACHERS_NAME));
            teacher.setPost(getStringChecked(cursor, TEACHERS_POST));
            teacher.setPhonenumber(getStringChecked(cursor, TEACHERS_PHONE_NUMBER));
            teacher.setEmail(getStringChecked(cursor, TEACHERS_EMAIL));
            teacher.setCabinNumber(getStringChecked(cursor, TEACHERS_CABIN_NUMBER));
            teacher.setColor(getIntChecked(cursor, TEACHERS_COLOR));
            teacherlist.add(teacher);
        }
        cursor.close();
        db.close();
        return teacherlist;
    }

    public void updateTeacherSortOrder(int teacherId, int newOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEACHERS_SORT_ORDER, newOrder);
        db.update(TEACHERS, values, TEACHERS_ID + "=?", new String[]{String.valueOf(teacherId)});
        db.close();
    }

    public void addTeacherIfNew(String name, int color) {
        if (name == null || name.length() == 0 || isTeacherInDb(name)) {
            return;
        }
        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setPost("");
        teacher.setPhonenumber("");
        teacher.setEmail("");
        teacher.setCabinNumber("");
        teacher.setColor(color);
        insertTeacher(teacher);
    }

    public boolean isTeacherInDb(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TEACHERS, new String[]{TEACHERS_NAME}, TEACHERS_NAME + "=?", new String[]{name}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void insertSubject(String name, int color, String teacher, String room) {
        if (name == null || name.length() == 0) {
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBJECTS_NAME, name);
        contentValues.put(SUBJECTS_COLOR, color);
        contentValues.put(SUBJECTS_TEACHER, teacher);

        if (isSubjectInDb(name)) {
            if (room != null) {
                contentValues.put(SUBJECTS_ROOM, room);
            }
            db.update(SUBJECTS, contentValues, SUBJECTS_NAME + "=?", new String[]{name});
        } else {
            contentValues.put(SUBJECTS_ROOM, room != null ? room : "");
            db.insert(SUBJECTS, null, contentValues);
        }
        db.close();
    }

    public boolean isSubjectInDb(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SUBJECTS, new String[]{SUBJECTS_NAME}, SUBJECTS_NAME + "=?", new String[]{name}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public ArrayList<String> getSubjectsList() {
        ArrayList<String> subjects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SUBJECTS, new String[]{SUBJECTS_NAME}, null, null, null, null, SUBJECTS_NAME + " ASC");
        while (cursor.moveToNext()) {
            subjects.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return subjects;
    }

    public ArrayList<Subject> getAllSubjects() {
        ArrayList<Subject> subjects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SUBJECTS, null, null, null, null, null, SUBJECTS_SORT_ORDER + " ASC, " + SUBJECTS_NAME + " ASC");
        while (cursor.moveToNext()) {
            Subject subject = new Subject();
            subject.setId(getIntChecked(cursor, SUBJECTS_ID));
            subject.setName(getStringChecked(cursor, SUBJECTS_NAME));
            subject.setColor(getIntChecked(cursor, SUBJECTS_COLOR));
            subject.setTeacher(getStringChecked(cursor, SUBJECTS_TEACHER));
            subject.setRoom(getStringChecked(cursor, SUBJECTS_ROOM));
            subject.setAttended(getIntChecked(cursor, SUBJECTS_ATTENDED));
            subject.setMissed(getIntChecked(cursor, SUBJECTS_MISSED));
            subject.setSkipped(getIntChecked(cursor, SUBJECTS_SKIPPED));
            subjects.add(subject);
        }
        cursor.close();
        db.close();
        return subjects;
    }

    public void updateSubjectName(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SUBJECTS_NAME, newName);
        db.update(SUBJECTS, values, SUBJECTS_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateSubjectSortOrder(int subjectId, int newOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SUBJECTS_SORT_ORDER, newOrder);
        db.update(SUBJECTS, values, SUBJECTS_ID + "=?", new String[]{String.valueOf(subjectId)});
        db.close();
    }

    public void deleteSubjectById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SUBJECTS, SUBJECTS_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Week getSubjectDetails(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SUBJECTS, new String[]{SUBJECTS_COLOR, SUBJECTS_TEACHER, SUBJECTS_ROOM}, SUBJECTS_NAME + "=?", new String[]{name}, null, null, null);
        Week week = null;
        if (cursor.moveToFirst()) {
            week = new Week();
            week.setSubject(name);
            week.setColor(getIntChecked(cursor, SUBJECTS_COLOR));
            week.setTeacher(getStringChecked(cursor, SUBJECTS_TEACHER));
            week.setRoom(getStringChecked(cursor, SUBJECTS_ROOM));
        }
        cursor.close();
        db.close();
        return week;
    }

    public ArrayList<String> getTeachersList() {
        ArrayList<String> teachers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TEACHERS, new String[]{TEACHERS_NAME}, null, null, null, null, TEACHERS_NAME + " ASC");
        while (cursor.moveToNext()) {
            teachers.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return teachers;
    }

    /**
     * Methods for Exams activity
     **/
    public void insertExam(Exam exam) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXAMS_SUBJECT, exam.getSubject());
        contentValues.put(EXAMS_TEACHER, exam.getTeacher());
        contentValues.put(EXAMS_ROOM, exam.getRoom());
        contentValues.put(EXAMS_DATE, exam.getDate());
        contentValues.put(EXAMS_TIME, exam.getTime());
        contentValues.put(EXAMS_COLOR, exam.getColor());
        db.insert(EXAMS, null, contentValues);
        db.close();

        addTeacherIfNew(exam.getTeacher(), exam.getColor());
        insertSubject(exam.getSubject(), exam.getColor(), exam.getTeacher(), null);
    }

    public void updateExam(Exam exam) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXAMS_SUBJECT, exam.getSubject());
        contentValues.put(EXAMS_TEACHER, exam.getTeacher());
        contentValues.put(EXAMS_ROOM, exam.getRoom());
        contentValues.put(EXAMS_DATE, exam.getDate());
        contentValues.put(EXAMS_TIME, exam.getTime());
        contentValues.put(EXAMS_COLOR, exam.getColor());
        db.update(EXAMS, contentValues, EXAMS_ID + " = ?", new String[]{String.valueOf(exam.getId())});
        db.close();

        addTeacherIfNew(exam.getTeacher(), exam.getColor());
        insertSubject(exam.getSubject(), exam.getColor(), exam.getTeacher(), null);
    }

    public void deleteExamById(Exam exam) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXAMS, EXAMS_ID + " =? ", new String[]{String.valueOf(exam.getId())});
        db.close();
    }

    public ArrayList<Exam> getExam() {
        ArrayList<Exam> examlist = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + EXAMS + " ORDER BY " + EXAMS_DATE + " ASC, " + EXAMS_TIME + " ASC", null);
        while (cursor.moveToNext()) {
            Exam exam = new Exam();
            exam.setId(getIntChecked(cursor, EXAMS_ID));
            exam.setSubject(getStringChecked(cursor, EXAMS_SUBJECT));
            exam.setTeacher(getStringChecked(cursor, EXAMS_TEACHER));
            exam.setRoom(getStringChecked(cursor, EXAMS_ROOM));
            exam.setDate(getStringChecked(cursor, EXAMS_DATE));
            exam.setTime(getStringChecked(cursor, EXAMS_TIME));
            exam.setColor(getIntChecked(cursor, EXAMS_COLOR));
            examlist.add(exam);
        }
        cursor.close();
        db.close();
        return examlist;
    }

    /**
     * Methods for Materials
     **/
    public void insertMaterial(Material material) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MATERIALS_SUBJECT_ID, material.getSubjectId());
        contentValues.put(MATERIALS_PATH, material.getPath());
        contentValues.put(MATERIALS_TYPE, material.getType());
        contentValues.put(MATERIALS_NAME, material.getName());
        db.insert(MATERIALS, null, contentValues);
        db.close();
    }

    public void updateMaterialName(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MATERIALS_NAME, newName);
        db.update(MATERIALS, values, MATERIALS_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteMaterialById(int materialId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MATERIALS, MATERIALS_ID + " = ?", new String[]{String.valueOf(materialId)});
        db.close();
    }

    public ArrayList<Material> getMaterialsBySubject(int subjectId) {
        ArrayList<Material> materials = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(MATERIALS, null, MATERIALS_SUBJECT_ID + "=?", new String[]{String.valueOf(subjectId)}, null, null, MATERIALS_SORT_ORDER + " ASC");
        while (cursor.moveToNext()) {
            Material material = new Material();
            material.setId(getIntChecked(cursor, MATERIALS_ID));
            material.setSubjectId(getIntChecked(cursor, MATERIALS_SUBJECT_ID));
            material.setPath(getStringChecked(cursor, MATERIALS_PATH));
            material.setType(getStringChecked(cursor, MATERIALS_TYPE));
            material.setName(getStringChecked(cursor, MATERIALS_NAME));
            materials.add(material);
        }
        cursor.close();
        db.close();
        return materials;
    }

    public void updateMaterialSortOrder(int materialId, int newOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MATERIALS_SORT_ORDER, newOrder);
        db.update(MATERIALS, values, MATERIALS_ID + "=?", new String[]{String.valueOf(materialId)});
        db.close();
    }

    /**
     * Methods for User Personal Details
     **/
    public UserDetail getUserDetail() {
        UserDetail userDetail = new UserDetail();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_DETAILS + " LIMIT 1", null);
        if (cursor.moveToFirst()) {
            userDetail.setId(getIntChecked(cursor, USER_DETAILS_ID));
            userDetail.setName(getStringChecked(cursor, USER_DETAILS_NAME));
            userDetail.setEmail(getStringChecked(cursor, USER_DETAILS_EMAIL));
            userDetail.setRollNumber(getStringChecked(cursor, USER_DETAILS_ROLL));
            userDetail.setPhotoPath(getStringChecked(cursor, USER_DETAILS_PHOTO));
            userDetail.setOther(getStringChecked(cursor, USER_DETAILS_OTHER));
        }
        cursor.close();
        db.close();
        return userDetail;
    }

    public void saveUserDetail(UserDetail detail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_DETAILS_NAME, detail.getName());
        values.put(USER_DETAILS_EMAIL, detail.getEmail());
        values.put(USER_DETAILS_ROLL, detail.getRollNumber());
        values.put(USER_DETAILS_PHOTO, detail.getPhotoPath());
        values.put(USER_DETAILS_OTHER, detail.getOther());

        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_DETAILS, null);
        if (cursor.getCount() > 0) {
            db.update(USER_DETAILS, values, null, null);
        } else {
            db.insert(USER_DETAILS, null, values);
        }
        cursor.close();
        db.close();
    }

    /**
     * Methods for User Files
     **/
    public void insertUserFile(UserFile file) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_FILES_TITLE, file.getTitle());
        values.put(USER_FILES_PATH, file.getPath());
        db.insert(USER_FILES, null, values);
        db.close();
    }

    public ArrayList<UserFile> getAllUserFiles() {
        ArrayList<UserFile> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_FILES, null);
        while (cursor.moveToNext()) {
            UserFile file = new UserFile();
            file.setId(getIntChecked(cursor, USER_FILES_ID));
            file.setTitle(getStringChecked(cursor, USER_FILES_TITLE));
            file.setPath(getStringChecked(cursor, USER_FILES_PATH));
            list.add(file);
        }
        cursor.close();
        db.close();
        return list;
    }

    public void deleteUserFile(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USER_FILES, USER_FILES_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateAttendance(int weekId, String subjectName, String type, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(ATTENDANCE, new String[]{ATTENDANCE_STATUS},
                ATTENDANCE_SUBJECT_NAME + " = ? AND " + ATTENDANCE_DATE + " = ? AND " + ATTENDANCE_WEEK_ID + " = ?",
                new String[]{subjectName, date, String.valueOf(weekId)}, null, null, null);

        String oldType = null;
        if (cursor.moveToFirst()) {
            oldType = cursor.getString(0);
        }
        cursor.close();

        if (oldType != null && oldType.equals(type)) {
            db.close();
            return;
        }

        if (oldType != null) {
            String oldColumn = getColumnNameForType(oldType);
            if (oldColumn != null) {
                db.execSQL("UPDATE " + SUBJECTS + " SET " + oldColumn + " = " + oldColumn + " - 1 WHERE " + SUBJECTS_NAME + " = ?", new String[]{subjectName});
            }
        }

        String newColumn = getColumnNameForType(type);
        if (newColumn != null) {
            db.execSQL("UPDATE " + SUBJECTS + " SET " + newColumn + " = " + newColumn + " + 1 WHERE " + SUBJECTS_NAME + " = ?", new String[]{subjectName});
        }

        ContentValues values = new ContentValues();
        values.put(ATTENDANCE_DATE, date);
        values.put(ATTENDANCE_WEEK_ID, weekId);
        values.put(ATTENDANCE_SUBJECT_NAME, subjectName);
        values.put(ATTENDANCE_STATUS, type);

        if (oldType != null) {
            db.update(ATTENDANCE, values, ATTENDANCE_SUBJECT_NAME + " = ? AND " + ATTENDANCE_DATE + " = ? AND " + ATTENDANCE_WEEK_ID + " = ?", new String[]{subjectName, date, String.valueOf(weekId)});
        } else {
            db.insert(ATTENDANCE, null, values);
        }
        db.close();
    }

    private String getColumnNameForType(String type) {
        switch (type) {
            case "attended": return SUBJECTS_ATTENDED;
            case "missed": return SUBJECTS_MISSED;
            case "skipped": return SUBJECTS_SKIPPED;
            default: return null;
        }
    }

    public String getAttendanceStatus(int weekId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ATTENDANCE, new String[]{ATTENDANCE_STATUS},
                ATTENDANCE_WEEK_ID + " = ? AND " + ATTENDANCE_DATE + " = ?",
                new String[]{String.valueOf(weekId), date}, null, null, null);
        String status = null;
        if (cursor.moveToFirst()) {
            status = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return status;
    }

    public ArrayList<Week> getSlotsBySubject(String name) {
        ArrayList<Week> weeklist = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TIMETABLE + " WHERE " + WEEK_SUBJECT + " = ? ORDER BY " + WEEK_FROM_TIME + " ASC", new String[]{name});
        if (cursor.moveToFirst()) {
            do {
                Week week = new Week();
                week.setId(getIntChecked(cursor, WEEK_ID));
                week.setSubject(getStringChecked(cursor, WEEK_SUBJECT));
                week.setFragment(getStringChecked(cursor, WEEK_FRAGMENT));
                week.setTeacher(getStringChecked(cursor, WEEK_TEACHER));
                week.setRoom(getStringChecked(cursor, WEEK_ROOM));
                week.setFromTime(getStringChecked(cursor, WEEK_FROM_TIME));
                week.setToTime(getStringChecked(cursor, WEEK_TO_TIME));
                week.setColor(getIntChecked(cursor, WEEK_COLOR));
                weeklist.add(week);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return weeklist;
    }

    public void updateAttendanceByDate(int weekId, String subjectName, String type, String date) {
        updateAttendance(weekId, subjectName, type, date);
    }

    public ArrayList<AttendanceRecord> getAttendanceForSubject(String subjectName) {
        ArrayList<AttendanceRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ATTENDANCE, new String[]{ATTENDANCE_DATE, ATTENDANCE_STATUS, ATTENDANCE_WEEK_ID},
                ATTENDANCE_SUBJECT_NAME + " = ?", new String[]{subjectName}, null, null, null);
        while (cursor.moveToNext()) {
            AttendanceRecord record = new AttendanceRecord();
            record.date = cursor.getString(0);
            record.status = cursor.getString(1);
            record.weekId = cursor.getInt(2);
            records.add(record);
        }
        cursor.close();
        db.close();
        return records;
    }

    public static class AttendanceRecord {
        public String date;
        public String status;
        public int weekId;
    }

    public Subject getSubjectByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SUBJECTS, null, SUBJECTS_NAME + " = ?", new String[]{name}, null, null, null);
        Subject subject = null;
        if (cursor.moveToFirst()) {
            subject = new Subject();
            subject.setId(getIntChecked(cursor, SUBJECTS_ID));
            subject.setName(getStringChecked(cursor, SUBJECTS_NAME));
            subject.setColor(getIntChecked(cursor, SUBJECTS_COLOR));
            subject.setTeacher(getStringChecked(cursor, SUBJECTS_TEACHER));
            subject.setRoom(getStringChecked(cursor, SUBJECTS_ROOM));
            subject.setAttended(getIntChecked(cursor, SUBJECTS_ATTENDED));
            subject.setMissed(getIntChecked(cursor, SUBJECTS_MISSED));
            subject.setSkipped(getIntChecked(cursor, SUBJECTS_SKIPPED));
        }
        cursor.close();
        db.close();
        return subject;
    }
}
