package com.example.timetable.utils

import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.Subject
import com.example.timetable.model.Week
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class LecSubject(
    val n: String,
    val c: Int,
    val t: Map<String, String>
)

@Serializable
data class LecSlot(
    val s: Int,
    val f: String,
    val t: String,
    val r: String,
    val v: String
)

@Serializable
data class LecRoot(
    val subjects: List<LecSubject>,
    val days: Map<String, List<LecSlot>>
)

object ScheduleExporter {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun exportSchedule(database: TimetableDatabase): String {
        val allWeeks = database.getAllWeeks()
        val allSubjects = database.getAllSubjects()
        return exportData(allSubjects, allWeeks)
    }

    private fun exportData(subjects: List<Subject>, weeks: List<Week>): String {
        val subjectIndexMap = mutableMapOf<String, Int>()
        val lecSubjects = subjects.mapIndexed { index, s ->
            val subjectWeeks = weeks.filter { it.subject == s.name }
            val uniqueTeachers = subjectWeeks.map { it.teacher }.filter { it.isNotBlank() }.distinct()
            
            val teacherMap = uniqueTeachers.mapIndexed { tIdx, tName ->
                getVarName(tIdx) to tName
            }.toMap()
            
            subjectIndexMap[s.name] = index
            LecSubject(s.name, s.color, teacherMap)
        }

        val daysMap = mutableMapOf<String, List<LecSlot>>()
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        for (day in dayNames) {
            val dayWeeks = weeks.filter { it.fragment == day }
            if (dayWeeks.isNotEmpty()) {
                val slots = dayWeeks.mapNotNull { week ->
                    val sIdx = subjectIndexMap[week.subject] ?: return@mapNotNull null
                    
                    val subjectWeeks = weeks.filter { it.subject == week.subject }
                    val uniqueTeachers = subjectWeeks.map { it.teacher }.filter { it.isNotBlank() }.distinct()
                    val tVar = getVarName(uniqueTeachers.indexOf(week.teacher))

                    LecSlot(sIdx, week.fromTime, week.toTime, week.room, tVar)
                }
                daysMap[day] = slots
            }
        }

        val root = LecRoot(lecSubjects, daysMap)
        return json.encodeToString(LecRoot.serializer(), root)
    }

    private fun getVarName(index: Int): String {
        if (index < 0) return ""
        var n = index
        var name = ""
        do {
            name = ('a' + (n % 26)).toString() + name
            n = n / 26 - 1
        } while (n >= 0)
        return name
    }

    fun parseLecFile(jsonString: String): List<Week> {
        val root = try { json.decodeFromString(LecRoot.serializer(), jsonString) } catch (e: Exception) { return emptyList() }
        val weeks = mutableListOf<Week>()
        
        root.days.forEach { (day, slots) ->
            slots.forEach { slot ->
                if (slot.s < root.subjects.size) {
                    val sObj = root.subjects[slot.s]
                    val teacherName = sObj.t[slot.v] ?: ""
                    
                    weeks.add(Week(
                        subject = sObj.n,
                        color = sObj.c,
                        fragment = day,
                        teacher = teacherName,
                        room = slot.r,
                        fromTime = slot.f,
                        toTime = slot.t
                    ))
                }
            }
        }
        return weeks
    }

    fun findConflicts(database: TimetableDatabase, newWeeks: List<Week>): List<Pair<Week, Week>> {
        val conflicts = mutableListOf<Pair<Week, Week>>()
        val newByDay = newWeeks.groupBy { it.fragment }
        
        for ((day, weeks) in newByDay) {
            val existing = database.getWeek(day)
            for (newW in weeks) {
                val clash = existing.find { ex ->
                    val nFrom = TimeUtils.timeToMinutes(newW.fromTime)
                    var nTo = TimeUtils.timeToMinutes(newW.toTime)
                    if (nTo <= nFrom) nTo += 1440
                    
                    val eFrom = TimeUtils.timeToMinutes(ex.fromTime)
                    var eTo = TimeUtils.timeToMinutes(ex.toTime)
                    if (eTo <= eFrom) eTo += 1440

                    nFrom < eTo && nTo > eFrom
                }
                if (clash != null) {
                    conflicts.add(newW to clash)
                }
            }
        }
        return conflicts
    }

    fun importWeeks(database: TimetableDatabase, weeks: List<Week>) {
        for (week in weeks) {
            database.insertWeek(week)
        }
    }
}
