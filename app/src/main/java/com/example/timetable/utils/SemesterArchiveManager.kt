package com.example.timetable.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object SemesterArchiveManager {

    private const val ARCHIVE_DIR = "semester_archives"

    fun archiveCurrentSemester(context: Context, semesterName: String): Boolean {
        try {
            val db = DbHelper(context)
            val root = JSONObject()
            root.put("archive_name", semesterName)
            root.put("archive_date", System.currentTimeMillis())

            // 1. Subjects
            val subjectsArray = JSONArray()
            db.getAllSubjects().forEach { s ->
                subjectsArray.put(JSONObject().apply {
                    put("n", s.name)
                    put("c", s.color)
                    put("t", s.teacher)
                    put("r", s.room)
                    put("a", s.attended)
                    put("m", s.missed)
                    put("s", s.skipped)
                })
            }
            root.put("subjects", subjectsArray)

            // 2. Timetable
            val weeksArray = JSONArray()
            db.getAllWeeks().forEach { w ->
                weeksArray.put(JSONObject().apply {
                    put("s", w.subject)
                    put("f", w.fragment)
                    put("t", w.teacher)
                    put("r", w.room)
                    put("ft", w.fromTime)
                    put("tt", w.toTime)
                    put("c", w.color)
                })
            }
            root.put("timetable", weeksArray)

            // 3. Notes
            val notesArray = JSONArray()
            db.getNote().forEach { n ->
                notesArray.put(JSONObject().apply {
                    put("t", n.title)
                    put("txt", n.text)
                    put("c", n.color)
                    put("sid", n.subjectId)
                })
            }
            root.put("notes", notesArray)

            // 4. Assignments
            val homeworksArray = JSONArray()
            db.getHomework().forEach { h ->
                homeworksArray.put(JSONObject().apply {
                    put("s", h.subject)
                    put("t", h.title)
                    put("d", h.description)
                    put("dt", h.date)
                    put("c", h.color)
                    put("cmp", h.completed)
                })
            }
            root.put("homeworks", homeworksArray)

            // 5. Exams
            val examsArray = JSONArray()
            db.getExam().forEach { e ->
                examsArray.put(JSONObject().apply {
                    put("s", e.subject)
                    put("t", e.teacher)
                    put("r", e.room)
                    put("d", e.date)
                    put("tm", e.time)
                    put("c", e.color)
                })
            }
            root.put("exams", examsArray)

            // 6. Attendance
            val attendanceArray = JSONArray()
            db.getAllSubjects().forEach { s ->
                db.getAttendanceForSubject(s.name).forEach { r ->
                    attendanceArray.put(JSONObject().apply {
                        put("sn", s.name)
                        put("d", r.date)
                        put("st", r.status)
                        put("wid", r.weekId)
                    })
                }
            }
            root.put("attendance", attendanceArray)

            // 8. Materials
            val materialsArray = JSONArray()
            db.getAllSubjects().forEach { s ->
                db.getMaterialsBySubject(s.id).forEach { m ->
                    materialsArray.put(JSONObject().apply {
                        put("sid", m.subjectId)
                        put("p", m.path)
                        put("t", m.type)
                        put("n", m.name)
                    })
                }
            }
            root.put("materials", materialsArray)

            val fileName = "archive_${System.currentTimeMillis()}.json"
            val dir = File(context.filesDir, ARCHIVE_DIR)
            if (!dir.exists()) dir.mkdirs()
            
            File(dir, fileName).writeText(root.toString())
            
            // Clear current data
            db.resetSemesterData()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getArchives(context: Context): List<ArchiveInfo> {
        val dir = File(context.filesDir, ARCHIVE_DIR)
        if (!dir.exists()) return emptyList()
        
        return dir.listFiles()?.mapNotNull { file ->
            try {
                val json = JSONObject(file.readText())
                ArchiveInfo(
                    name = json.optString("archive_name", "Unnamed Archive"),
                    date = json.optLong("archive_date", file.lastModified()),
                    file = file
                )
            } catch (e: Exception) {
                null
            }
        }?.sortedByDescending { it.date } ?: emptyList()
    }

    data class ArchiveInfo(val name: String, val date: Long, val file: File)
}
