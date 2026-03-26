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
            subjectsArray.put(JSONObject().apply {
                put("name", s.name)
                put("color", s.color)
                put("teacher", s.teacher)
                put("room", s.room)
            })
        }
        root.put("subjects", subjectsArray)

        val scheduleArray = JSONArray()
        for (week in weeks) {
            scheduleArray.put(JSONObject().apply {
                put("subject", week.subject)
                put("fragment", week.fragment)
                put("teacher", week.teacher)
                put("room", week.room)
                put("fromtime", week.fromTime)
                put("totime", week.toTime)
                put("color", week.color)
            })
        }
        root.put("schedule", scheduleArray)

        outputStream.use { it.write(root.toString(2).toByteArray()) }
    }

    fun parseLecFile(inputStream: InputStream): List<Week> {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val root = try { JSONObject(jsonString) } catch (e: Exception) { null }
        
        val weeks = mutableListOf<Week>()
        
        if (root != null && root.has("schedule")) {
            // New optimized format
            val scheduleArray = root.getJSONArray("schedule")
            for (i in 0 until scheduleArray.length()) {
                val jsonObject = scheduleArray.getJSONObject(i)
                weeks.add(parseWeek(jsonObject))
            }
        } else {
            // Legacy/Simple format (just an array of weeks)
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                weeks.add(parseWeek(jsonArray.getJSONObject(i)))
            }
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
        
        // Group new weeks by fragment for easier lookup
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
