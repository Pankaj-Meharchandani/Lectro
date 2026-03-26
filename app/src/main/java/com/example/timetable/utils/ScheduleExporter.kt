package com.example.timetable.utils

import android.content.Context
import com.example.timetable.model.Week
import com.example.timetable.model.Subject
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

object ScheduleExporter {

    fun exportSchedule(context: Context, outputStream: OutputStream) {
        val dbHelper = DbHelper(context)
        val allWeeks = dbHelper.getAllWeeks()
        val allSubjects = dbHelper.getAllSubjects()
        exportData(allSubjects, allWeeks, outputStream)
    }

    fun exportSubject(context: Context, subjectName: String, outputStream: OutputStream) {
        val dbHelper = DbHelper(context)
        val subjectWeeks = dbHelper.getWeeksBySubject(subjectName)
        val subjectDef = dbHelper.getAllSubjects().find { it.name == subjectName }
        exportData(if (subjectDef != null) listOf(subjectDef) else emptyList(), subjectWeeks, outputStream)
    }

    private fun exportData(subjects: List<Subject>, weeks: List<Week>, outputStream: OutputStream) {
        val root = JSONObject()
        val subjectsArray = JSONArray()
        val subjectIndexMap = mutableMapOf<String, Int>()

        // 1. Declare Subjects first
        subjects.forEachIndexed { index, s ->
            val subjectObj = JSONObject()
            subjectObj.put("n", s.name)
            subjectObj.put("c", s.color)
            
            // Map unique teachers for this subject to variables (a, b, c...)
            val subjectWeeks = weeks.filter { it.subject == s.name }
            val uniqueTeachers = subjectWeeks.mapNotNull { it.teacher }.filter { it.isNotBlank() }.distinct()
            
            val teacherMap = JSONObject()
            uniqueTeachers.forEachIndexed { tIdx, tName ->
                teacherMap.put(getVarName(tIdx), tName)
            }
            subjectObj.put("t", teacherMap)
            
            subjectsArray.put(subjectObj)
            subjectIndexMap[s.name] = index
        }
        root.put("subjects", subjectsArray)

        // 2. Group Schedule by Days
        val daysObject = JSONObject()
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        for (day in dayNames) {
            val dayWeeks = weeks.filter { it.fragment == day }
            if (dayWeeks.isNotEmpty()) {
                val dayArray = JSONArray()
                for (week in dayWeeks) {
                    val sIdx = subjectIndexMap[week.subject] ?: continue
                    val sDef = subjects[sIdx]
                    
                    // Find teacher variable
                    val subjectWeeks = weeks.filter { it.subject == week.subject }
                    val uniqueTeachers = subjectWeeks.mapNotNull { it.teacher }.filter { it.isNotBlank() }.distinct()
                    val tVar = getVarName(uniqueTeachers.indexOf(week.teacher))

                    dayArray.put(JSONObject().apply {
                        put("s", sIdx)
                        put("f", week.fromTime)
                        put("t", week.toTime)
                        put("r", week.room)
                        put("v", if (tVar.isNotEmpty()) tVar else "")
                    })
                }
                daysObject.put(day, dayArray)
            }
        }
        root.put("days", daysObject)

        outputStream.use { it.write(root.toString(2).toByteArray()) }
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

    fun parseLecFile(inputStream: InputStream): List<Week> {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val root = try { JSONObject(jsonString) } catch (e: Exception) { return emptyList() }
        val weeks = mutableListOf<Week>()
        
        if (root.has("subjects") && root.has("days")) {
            // New structured format
            val subjectsArray = root.getJSONArray("subjects")
            val daysObject = root.getJSONObject("days")
            
            val dayNames = daysObject.keys()
            while (dayNames.hasNext()) {
                val day = dayNames.next()
                val dayArray = daysObject.getJSONArray(day)
                
                for (i in 0 until dayArray.length()) {
                    val slot = dayArray.getJSONObject(i)
                    val sIdx = slot.getInt("s")
                    if (sIdx < subjectsArray.length()) {
                        val sObj = subjectsArray.getJSONObject(sIdx)
                        val tMap = sObj.optJSONObject("t") ?: JSONObject()
                        val teacherVar = slot.optString("v")
                        val teacherName = tMap.optString(teacherVar, "")
                        
                        weeks.add(Week().apply {
                            subject = sObj.getString("n")
                            color = sObj.getInt("c")
                            fragment = day
                            teacher = teacherName
                            room = slot.optString("r")
                            setFromTime(slot.optString("f"))
                            setToTime(slot.optString("t"))
                        })
                    }
                }
            }
        }
        return weeks
    }

    fun findConflicts(context: Context, newWeeks: List<Week>): List<Pair<Week, Week>> {
        val dbHelper = DbHelper(context)
        val conflicts = mutableListOf<Pair<Week, Week>>()
        val newByDay = newWeeks.groupBy { it.fragment }
        
        for ((day, weeks) in newByDay) {
            val existing = dbHelper.getWeek(day)
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

    fun importWeeks(context: Context, weeks: List<Week>) {
        val dbHelper = DbHelper(context)
        for (week in weeks) {
            dbHelper.insertWeek(week)
        }
        NotificationHelper(context).scheduleEventsForToday()
    }
}
