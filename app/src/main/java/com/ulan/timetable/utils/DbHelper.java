package com.ulan.timetable.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ulan.timetable.model.Exam;
import com.ulan.timetable.model.Homework;
import com.ulan.timetable.model.Material;
import com.ulan.timetable.model.Note;
import com.ulan.timetable.model.Subject;
import com.ulan.timetable.model.Teacher;
import com.ulan.timetable.model.Week;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 8;
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

    private static final String HOMEWORKS = "homeworks";
    private static final String HOMEWORKS_ID = "id";
    private static final String HOMEWORKS_SUBJECT = "subject";
    private static final String HOMEWORKS_DESCRIPTION = "description";
    private static final String HOMEWORKS_DATE = "date";
    private static final String HOMEWORKS_COLOR = "color";

    private static final String NOTES = "notes";
    private static final String NOTES_ID = "id";
    private static final String NOTES_TITLE = "title";
    private static final String NOTES_TEXT = "text";
    private static final String NOTES_COLOR = "color";
    private static final String NOTES_SUBJECT_ID = "subject_id";

    private static final String TEACHERS = "teachers";
    private static final String TEACHERS_ID = "id";
    private static final String TEACHERS_NAME = "name";
    private static final String TEACHERS_POST = "post";
    private static final String TEACHERS_PHONE_NUMBER = "phonenumber";
    private static final String TEACHERS_EMAIL = "email";
    private static final String TEACHERS_COLOR = "color";

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
                + HOMEWORKS_DESCRIPTION + " TEXT,"
                + HOMEWORKS_DATE + " TEXT,"
                + HOMEWORKS_COLOR + " INTEGER" + ")";

        String CREATE_NOTES = "CREATE TABLE " + NOTES + "("
                + NOTES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NOTES_TITLE + " TEXT,"
                + NOTES_TEXT + " TEXT,"
                + NOTES_COLOR + " INTEGER,"
                + NOTES_SUBJECT_ID + " INTEGER DEFAULT -1" + ")";

        String CREATE_TEACHERS = "CREATE TABLE " + TEACHERS + "("
                + TEACHERS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TEACHERS_NAME + " TEXT,"
                + TEACHERS_POST + " TEXT,"
                + TEACHERS_PHONE_NUMBER + " TEXT,"
                + TEACHERS_EMAIL + " TEXT,"
                + TEACHERS_COLOR + " INTEGER" + ")";

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
                + SUBJECTS_ROOM + " TEXT" + ")";

        String CREATE_MATERIALS = "CREATE TABLE " + MATERIALS + "("
                + MATERIALS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MATERIALS_SUBJECT_ID + " INTEGER,"
                + MATERIALS_PATH + " TEXT,"
                + MATERIALS_TYPE + " TEXT,"
                + MATERIALS_NAME + " TEXT" + ")";

        db.execSQL(CREATE_TIMETABLE);
        db.execSQL(CREATE_HOMEWORKS);
        db.execSQL(CREATE_NOTES);
        db.execSQL(CREATE_TEACHERS);
        db.execSQL(CREATE_EXAMS);
        db.execSQL(CREATE_SUBJECTS);
        db.execSQL(CREATE_MATERIALS);
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
            String CREATE_SUBJECTS = "CREATE TABLE " + SUBJECTS + "("
                    + SUBJECTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SUBJECTS_NAME + " TEXT,"
                    + SUBJECTS_COLOR + " INTEGER,"
                    + SUBJECTS_TEACHER + " TEXT,"
                    + SUBJECTS_ROOM + " TEXT" + ")";
            db.execSQL(CREATE_SUBJECTS);
            onUpgrade(db, 7, newVersion);
        } else if (oldVersion == 7) {
            db.execSQL("ALTER TABLE " + NOTES + " ADD COLUMN " + NOTES_SUBJECT_ID + " INTEGER DEFAULT -1");
            String CREATE_MATERIALS = "CREATE TABLE " + MATERIALS + "("
                    + MATERIALS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + MATERIALS_SUBJECT_ID + " INTEGER,"
                    + MATERIALS_PATH + " TEXT,"
                    + MATERIALS_TYPE + " TEXT,"
                    + MATERIALS_NAME + " TEXT" + ")";
            db.execSQL(CREATE_MATERIALS);
        }
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
                week.setId(cursor.getInt(cursor.getColumnIndex(WEEK_ID)));
                week.setSubject(cursor.getString(cursor.getColumnIndex(WEEK_SUBJECT)));
                week.setFragment(cursor.getString(cursor.getColumnIndex(WEEK_FRAGMENT)));
                week.setTeacher(cursor.getString(cursor.getColumnIndex(WEEK_TEACHER)));
                week.setRoom(cursor.getString(cursor.getColumnIndex(WEEK_ROOM)));
                week.setFromTime(cursor.getString(cursor.getColumnIndex(WEEK_FROM_TIME)));
                week.setToTime(cursor.getString(cursor.getColumnIndex(WEEK_TO_TIME)));
                week.setColor(cursor.getInt(cursor.getColumnIndex(WEEK_COLOR)));
                weeklist.add(week);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return weeklist;
    }

    /**
     * Methods for Homeworks activity
     **/
    public void insertHomework(Homework homework) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOMEWORKS_SUBJECT, homework.getSubject());
        contentValues.put(HOMEWORKS_DESCRIPTION, homework.getDescription());
        contentValues.put(HOMEWORKS_DATE, homework.getDate());
        contentValues.put(HOMEWORKS_COLOR, homework.getColor());
        db.insert(HOMEWORKS, null, contentValues);
        db.close();

        insertSubject(homework.getSubject(), homework.getColor(), "", "");
    }

    public void updateHomework(Homework homework) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HOMEWORKS_SUBJECT, homework.getSubject());
        contentValues.put(HOMEWORKS_DESCRIPTION, homework.getDescription());
        contentValues.put(HOMEWORKS_DATE, homework.getDate());
        contentValues.put(HOMEWORKS_COLOR, homework.getColor());
        db.update(HOMEWORKS, contentValues, HOMEWORKS_ID + " = ?", new String[]{String.valueOf(homework.getId())});
        db.close();

        insertSubject(homework.getSubject(), homework.getColor(), "", "");
    }

    public void deleteHomeworkById(Homework homework) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(HOMEWORKS, HOMEWORKS_ID + " =? ", new String[]{String.valueOf(homework.getId())});
        db.close();
    }


    public ArrayList<Homework> getHomework() {
        ArrayList<Homework> homeworklist = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + HOMEWORKS, null);
        while (cursor.moveToNext()) {
            Homework homework = new Homework();
            homework.setId(cursor.getInt(cursor.getColumnIndex(HOMEWORKS_ID)));
            homework.setSubject(cursor.getString(cursor.getColumnIndex(HOMEWORKS_SUBJECT)));
            homework.setDescription(cursor.getString(cursor.getColumnIndex(HOMEWORKS_DESCRIPTION)));
            homework.setDate(cursor.getString(cursor.getColumnIndex(HOMEWORKS_DATE)));
            homework.setColor(cursor.getInt(cursor.getColumnIndex(HOMEWORKS_COLOR)));
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

    public void deleteNoteById(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NOTES, NOTES_ID + " =? ", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public ArrayList<Note> getNote() {
        ArrayList<Note> notelist = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + NOTES, null);
        while (cursor.moveToNext()) {
            Note note = new Note();
            note.setId(cursor.getInt(cursor.getColumnIndex(NOTES_ID)));
            note.setTitle(cursor.getString(cursor.getColumnIndex(NOTES_TITLE)));
            note.setText(cursor.getString(cursor.getColumnIndex(NOTES_TEXT)));
            note.setColor(cursor.getInt(cursor.getColumnIndex(NOTES_COLOR)));
            notelist.add(note);
        }
        cursor.close();
        db.close();
        return notelist;
    }

    public ArrayList<Note> getNotesBySubject(int subjectId) {
        ArrayList<Note> notelist = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(NOTES, null, NOTES_SUBJECT_ID + "=?", new String[]{String.valueOf(subjectId)}, null, null, null);
        while (cursor.moveToNext()) {
            Note note = new Note();
            note.setId(cursor.getInt(cursor.getColumnIndex(NOTES_ID)));
            note.setTitle(cursor.getString(cursor.getColumnIndex(NOTES_TITLE)));
            note.setText(cursor.getString(cursor.getColumnIndex(NOTES_TEXT)));
            note.setColor(cursor.getInt(cursor.getColumnIndex(NOTES_COLOR)));
            notelist.add(note);
        }
        cursor.close();
        db.close();
        return notelist;
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + TEACHERS, null);
        while (cursor.moveToNext()) {
            teacher = new Teacher();
            teacher.setId(cursor.getInt(cursor.getColumnIndex(TEACHERS_ID)));
            teacher.setName(cursor.getString(cursor.getColumnIndex(TEACHERS_NAME)));
            teacher.setPost(cursor.getString(cursor.getColumnIndex(TEACHERS_POST)));
            teacher.setPhonenumber(cursor.getString(cursor.getColumnIndex(TEACHERS_PHONE_NUMBER)));
            teacher.setEmail(cursor.getString(cursor.getColumnIndex(TEACHERS_EMAIL)));
            teacher.setColor(cursor.getInt(cursor.getColumnIndex(TEACHERS_COLOR)));
            teacherlist.add(teacher);
        }
        cursor.close();
        db.close();
        return teacherlist;
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
        contentValues.put(SUBJECTS_ROOM, room);

        if (isSubjectInDb(name)) {
            db.update(SUBJECTS, contentValues, SUBJECTS_NAME + "=?", new String[]{name});
        } else {
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

    public void deleteSubjectById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SUBJECTS, SUBJECTS_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
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
        Cursor cursor = db.query(SUBJECTS, null, null, null, null, null, SUBJECTS_NAME + " ASC");
        while (cursor.moveToNext()) {
            Subject subject = new Subject();
            subject.setId(cursor.getInt(cursor.getColumnIndex(SUBJECTS_ID)));
            subject.setName(cursor.getString(cursor.getColumnIndex(SUBJECTS_NAME)));
            subject.setColor(cursor.getInt(cursor.getColumnIndex(SUBJECTS_COLOR)));
            subject.setTeacher(cursor.getString(cursor.getColumnIndex(SUBJECTS_TEACHER)));
            subject.setRoom(cursor.getString(cursor.getColumnIndex(SUBJECTS_ROOM)));
            subjects.add(subject);
        }
        cursor.close();
        db.close();
        return subjects;
    }

    public Week getSubjectDetails(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SUBJECTS, new String[]{SUBJECTS_COLOR, SUBJECTS_TEACHER, SUBJECTS_ROOM}, SUBJECTS_NAME + "=?", new String[]{name}, null, null, null);
        Week week = null;
        if (cursor.moveToFirst()) {
            week = new Week();
            week.setSubject(name);
            week.setColor(cursor.getInt(0));
            week.setTeacher(cursor.getString(1));
            week.setRoom(cursor.getString(2));
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
        insertSubject(exam.getSubject(), exam.getColor(), exam.getTeacher(), exam.getRoom());
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
        insertSubject(exam.getSubject(), exam.getColor(), exam.getTeacher(), exam.getRoom());
    }

    public void deleteExamById(Exam exam) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(EXAMS, EXAMS_ID + " =? ", new String[]{String.valueOf(exam.getId())});
        db.close();
    }

    public ArrayList<Exam> getExam() {
        ArrayList<Exam> examlist = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + EXAMS, null);
        while (cursor.moveToNext()) {
            Exam exam = new Exam();
            exam.setId(cursor.getInt(cursor.getColumnIndex(EXAMS_ID)));
            exam.setSubject(cursor.getString(cursor.getColumnIndex(EXAMS_SUBJECT)));
            exam.setTeacher(cursor.getString(cursor.getColumnIndex(EXAMS_TEACHER)));
            exam.setRoom(cursor.getString(cursor.getColumnIndex(EXAMS_ROOM)));
            exam.setDate(cursor.getString(cursor.getColumnIndex(EXAMS_DATE)));
            exam.setTime(cursor.getString(cursor.getColumnIndex(EXAMS_TIME)));
            exam.setColor(cursor.getInt(cursor.getColumnIndex(EXAMS_COLOR)));
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

    public void deleteMaterialById(int materialId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MATERIALS, MATERIALS_ID + " = ?", new String[]{String.valueOf(materialId)});
        db.close();
    }

    public ArrayList<Material> getMaterialsBySubject(int subjectId) {
        ArrayList<Material> materials = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(MATERIALS, null, MATERIALS_SUBJECT_ID + "=?", new String[]{String.valueOf(subjectId)}, null, null, null);
        while (cursor.moveToNext()) {
            Material material = new Material();
            material.setId(cursor.getInt(cursor.getColumnIndex(MATERIALS_ID)));
            material.setSubjectId(cursor.getInt(cursor.getColumnIndex(MATERIALS_SUBJECT_ID)));
            material.setPath(cursor.getString(cursor.getColumnIndex(MATERIALS_PATH)));
            material.setType(cursor.getString(cursor.getColumnIndex(MATERIALS_TYPE)));
            material.setName(cursor.getString(cursor.getColumnIndex(MATERIALS_NAME)));
            materials.add(material);
        }
        cursor.close();
        db.close();
        return materials;
    }
}
