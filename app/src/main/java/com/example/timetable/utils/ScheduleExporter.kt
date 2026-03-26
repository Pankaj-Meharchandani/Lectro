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
        exportWeeks(allWeeks, outputStream)
    }

    fun exportSubject(context: Context, subjectName: String, outputStream: OutputStream) {
        val dbHelper = DbHelper(context)
        val subjectWeeks = dbHelper.getWeeksBySubject(subjectName)
        exportWeeks(subjectWeeks, outputStream)
    }

    private fun exportWeeks(weeks: List<Week>, outputStream: OutputStream) {
        val jsonArray = JSONArray()
        for (week in weeks) {
            val jsonObject = JSONObject().apply {
                put("subject", week.subject)
                put("fragment", week.fragment)
                put("teacher", week.teacher)
                put("room", week.room)
                put("fromtime", week.fromTime)
                put("totime", week.toTime)
                put("color", week.color)
            }
            jsonArray.put(jsonObject)
        }
        outputStream.use { it.write(jsonArray.toString(2).toByteArray()) }
    }

    fun parseLecFile(inputStream: InputStream): List<Week> {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val weeks = mutableListOf<Week>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            weeks.add(Week().apply {
                subject = jsonObject.optString("subject")
                fragment = jsonObject.optString("fragment")
                teacher = jsonObject.optString("teacher")
                room = jsonObject.optString("room")
                setFromTime(jsonObject.optString("fromtime"))
                setToTime(jsonObject.optString("totime"))
                color = jsonObject.optInt("color")
            })
        }
        return weeks
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
