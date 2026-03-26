package com.example.timetable.utils

import android.content.Context
import com.example.timetable.model.Week
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

    private fun exportData(subjects: List<com.example.timetable.model.Subject>, weeks: List<Week>, outputStream: OutputStream) {
        val root = JSONObject()
        val subjectsArray = JSONArray()
        
        for (s in subjects) {
            val subjectObj = JSONObject()
            subjectObj.put("n", s.name)
            subjectObj.put("c", s.color)
            
            // Find unique teachers for this subject in the exported weeks
            val subjectWeeks = weeks.filter { it.subject == s.name }
            val uniqueTeachers = subjectWeeks.mapNotNull { it.teacher }.filter { it.isNotBlank() }.distinct()
            
            val teacherMap = JSONObject()
            val teacherVarMap = mutableMapOf<String, String>()
            uniqueTeachers.forEachIndexed { index, name ->
                val varName = getVarName(index)
                teacherMap.put(varName, name)
                teacherVarMap[name] = varName
            }
            subjectObj.put("t", teacherMap)
            
            val slotsArray = JSONArray()
            for (week in subjectWeeks) {
                slotsArray.put(JSONObject().apply {
                    put("d", week.fragment)
                    put("f", week.fromTime)
                    put("t", week.toTime)
                    put("r", week.room)
                    put("v", teacherVarMap[week.teacher] ?: "")
                })
            }
            subjectObj.put("s", slotsArray)
            subjectsArray.put(subjectObj)
        }
        root.put("subjects", subjectsArray)
        outputStream.use { it.write(root.toString(2).toByteArray()) }
    }

    private fun getVarName(index: Int): String {
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
        val root = try { JSONObject(jsonString) } catch (e: Exception) { null }
        val weeks = mutableListOf<Week>()
        
        if (root != null && root.has("subjects")) {
            // Optimized format
            val subjectsArray = root.getJSONArray("subjects")
            for (i in 0 until subjectsArray.length()) {
                val sObj = subjectsArray.getJSONObject(i)
                val name = sObj.optString("n")
                val color = sObj.optInt("c")
                val tMap = sObj.optJSONObject("t") ?: JSONObject()
                val slots = sObj.optJSONArray("s") ?: JSONArray()
                
                for (j in 0 until slots.length()) {
                    val slot = slots.getJSONObject(j)
                    val teacherVar = slot.optString("v")
                    val teacherName = tMap.optString(teacherVar, "")
                    
                    weeks.add(Week().apply {
                        subject = name
                        fragment = slot.optString("d")
                        teacher = teacherName
                        room = slot.optString("r")
                        setFromTime(slot.optString("f"))
                        setToTime(slot.optString("t"))
                        this.color = color
                    })
                }
            }
        } else if (root != null && root.has("schedule")) {
            // Middle format
            val scheduleArray = root.getJSONArray("schedule")
            for (i in 0 until scheduleArray.length()) {
                weeks.add(parseWeek(scheduleArray.getJSONObject(i)))
            }
        } else {
            // Legacy/Simple format (array of weeks)
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    weeks.add(parseWeek(jsonArray.getJSONObject(i)))
                }
            } catch (e: Exception) {}
        }
        return weeks
    }

    private fun parseWeek(jsonObject: JSONObject): Week {
        return Week().apply {
            subject = jsonObject.optString("subject")
            fragment = jsonObject.optString("fragment")
            teacher = jsonObject.optString("teacher")
            room = jsonObject.optString("room")
            setFromTime(jsonObject.optString("fromtime"))
            setToTime(jsonObject.optString("totime"))
            color = jsonObject.optInt("color")
        }
    }

    fun findConflicts(context: Context, newWeeks: List<Week>): List<Pair<Week, Week>> {
        val dbHelper = DbHelper(context)
        val conflicts = mutableListOf<Pair<Week, Week>>()
        val newByDay = newWeeks.groupBy { it.fragment }
        
        for ((day, weeks) in newByDay) {
            val existing = dbHelper.getWeek(day)
            for (newW in weeks) {
                val clash = existing.find { ex ->
                    (newW.fromTime ?: "") < (ex.toTime ?: "") &&
                    (newW.toTime ?: "") > (ex.fromTime ?: "")
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
