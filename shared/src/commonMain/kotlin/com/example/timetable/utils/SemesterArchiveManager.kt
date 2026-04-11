package com.example.timetable.utils

import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SemesterArchive(
    val archiveName: String,
    val archiveDate: Long,
    val subjects: List<Subject>,
    val timetable: List<Week>,
    val notes: List<Note>,
    val homeworks: List<Homework>,
    val exams: List<Exam>,
    val attendance: List<ArchiveAttendanceRecord>,
    val materials: List<Material>
)

@Serializable
data class ArchiveAttendanceRecord(
    val subjectName: String,
    val date: String,
    val status: String,
    val weekId: Int
)

object SemesterArchiveManager {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun createArchive(database: TimetableDatabase, name: String, date: Long): String {
        val subjects = database.getAllSubjects()
        val timetable = database.getAllWeeks()
        val notes = database.getNote()
        val homeworks = database.getHomework()
        val exams = database.getExam()
        
        val attendance = mutableListOf<ArchiveAttendanceRecord>()
        subjects.forEach { s ->
            database.getAttendanceForSubject(s.name).forEach { r ->
                attendance.add(ArchiveAttendanceRecord(s.name, r.date, r.status, r.weekId))
            }
        }

        val materials = mutableListOf<Material>()
        subjects.forEach { s ->
            materials.addAll(database.getMaterialsBySubject(s.id))
        }

        val archive = SemesterArchive(name, date, subjects, timetable, notes, homeworks, exams, attendance, materials)
        return json.encodeToString(SemesterArchive.serializer(), archive)
    }

    fun restoreArchive(database: TimetableDatabase, archiveJson: String) {
        val archive = try { json.decodeFromString(SemesterArchive.serializer(), archiveJson) } catch (e: Exception) { return }
        
        // This is complex because we should probably clear current data or merge.
        // For a full restore, we might want to clear.
        
        archive.subjects.forEach { s ->
            // Insert or update subject
            database.insertSubject(s.name, s.color, s.teacher, s.room)
            database.updateSubjectAttendance(s.name, s.attended, s.missed, s.skipped)
        }

        archive.timetable.forEach { w -> database.insertWeek(w) }
        archive.notes.forEach { n -> database.insertNote(n) }
        archive.homeworks.forEach { h -> database.insertHomework(h) }
        archive.exams.forEach { e -> database.insertExam(e) }
        
        archive.attendance.forEach { r ->
            database.updateAttendance(r.weekId, r.subjectName, r.status, r.date)
        }
        
        archive.materials.forEach { m -> database.insertMaterial(m) }
    }
}
