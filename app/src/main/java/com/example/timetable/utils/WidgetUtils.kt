package com.example.timetable.utils

import android.content.Context
import com.example.timetable.model.Exam
import com.example.timetable.model.Homework
import com.example.timetable.model.Week
import androidx.glance.appwidget.updateAll
import java.text.SimpleDateFormat
import java.util.*

object WidgetUtils {
    fun getTodaySchedule(context: Context): List<Week> {
        val db = DbHelper(context)
        val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val todayName = dayNames[dayOfWeek - 1]
        
        return db.getWeek(todayName).sortedBy { it.fromTime }
    }

    fun getUnmarkedClasses(context: Context): List<Week> {
        val db = DbHelper(context)
        val todayClasses = getTodaySchedule(context)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nowMinutes = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }

        return todayClasses.filter { slot ->
            val startMinutes = TimeUtils.timeToMinutes(slot.fromTime)
            // Show if class has started AND hasn't been marked yet
            nowMinutes >= startMinutes && db.getAttendanceStatus(slot.id, todayDate) == null
        }
    }

    fun getUpcomingDeadlines(context: Context): List<DeadlineItem> {
        val db = DbHelper(context)
        val deadlines = mutableListOf<DeadlineItem>()
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
        
        // Fetch Homework
        db.homework.forEach { hw ->
            if (hw.getCompleted() == 0) {
                try {
                    val date = sdf.parse(hw.date)
                    if (date != null && date.after(now.time) && date.before(nextWeek.time)) {
                        deadlines.add(DeadlineItem(hw.title ?: "Assignment", hw.subject, hw.date, true, hw.color))
                    }
                } catch (e: Exception) {}
            }
        }
        
        // Fetch Exams
        db.exam.forEach { exam ->
            try {
                val date = sdf.parse(exam.date)
                if (date != null && date.after(now.time) && date.before(nextWeek.time)) {
                    deadlines.add(DeadlineItem("Exam: ${exam.subject}", exam.room, exam.date, false, exam.color))
                }
            } catch (e: Exception) {}
        }
        
        return deadlines.sortedBy { it.date }
    }

    suspend fun refreshAllWidgets(context: Context) {
        com.example.timetable.widget.ScheduleWidget().updateAll(context)
        com.example.timetable.widget.DeadlinesWidget().updateAll(context)
        com.example.timetable.widget.AttendanceWidget().updateAll(context)
    }

    data class DeadlineItem(
        val title: String,
        val subtitle: String?,
        val date: String,
        val isAssignment: Boolean,
        val color: Int
    )
}
