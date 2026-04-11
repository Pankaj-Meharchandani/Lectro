package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.*
import com.example.timetable.shared.Notifier
import com.example.timetable.shared.WidgetRefresher
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class MainViewModel(
    private val db: TimetableDatabase,
    private val notifier: Notifier? = null,
    private val widgetRefresher: WidgetRefresher? = null
) : ViewModel() {
    
    val weekData = mutableStateMapOf<String, List<Week>>()
    var subjects = mutableStateListOf<String>()
    var allSubjects = mutableStateListOf<Subject>()
    var teachers = mutableStateListOf<String>()
    var userDetail by mutableStateOf(UserDetail())
    val todayAttendance = mutableStateMapOf<Int, String?>()

    var searchQuery by mutableStateOf("")
    
    data class SearchResult(
        val type: SearchResultType,
        val title: String,
        val subtitle: String? = null,
        val originalObject: Any,
        val id: Int
    )
    
    enum class SearchResultType { SUBJECT, NOTE, ASSIGNMENT, TEACHER }

    fun searchAcrossApp(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        val results = mutableListOf<SearchResult>()
        val lowercaseQuery = query.lowercase()

        db.getAllWeeks().forEach { week ->
            if (week.subject.lowercase().contains(lowercaseQuery) || 
                (week.teacher.lowercase().contains(lowercaseQuery))) {
                results.add(SearchResult(
                    SearchResultType.SUBJECT,
                    week.subject,
                    "Timetable • ${week.fragment} • ${week.fromTime}",
                    week,
                    week.id
                ))
            }
        }

        db.getNote().forEach { note ->
            if (note.title.lowercase().contains(lowercaseQuery) || 
                note.text.lowercase().contains(lowercaseQuery)) {
                results.add(SearchResult(
                    SearchResultType.NOTE,
                    note.title,
                    "Note • ${note.text.take(30)}...",
                    note,
                    note.id
                ))
            }
        }

        db.getHomework().forEach { hw ->
            if (hw.title.lowercase().contains(lowercaseQuery) || 
                hw.subject.lowercase().contains(lowercaseQuery) || 
                hw.description.lowercase().contains(lowercaseQuery)) {
                results.add(SearchResult(
                    SearchResultType.ASSIGNMENT,
                    hw.title,
                    "Assignment • ${hw.subject} • Due: ${hw.date}",
                    hw,
                    hw.id
                ))
            }
        }

        db.getTeacher().forEach { teacher ->
            if (teacher.name.lowercase().contains(lowercaseQuery) ||
                teacher.post.lowercase().contains(lowercaseQuery) ||
                (teacher.email.lowercase().contains(lowercaseQuery))) {
                results.add(SearchResult(
                    SearchResultType.TEACHER,
                    teacher.name,
                    "Teacher • ${teacher.post}",
                    teacher,
                    teacher.id
                ))
            }
        }

        return results.distinctBy { it.type.name + it.id }
    }

    fun loadWeekData(day: String) {
        weekData[day] = db.getWeek(day)
        loadAttendance()
    }

    fun loadAttendance() {
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        weekData.values.flatten().forEach { slot ->
            todayAttendance[slot.id] = db.getAttendanceStatus(slot.id, date)
        }
    }

    fun loadSuggestions() {
        userDetail = db.getUserDetail()
        subjects.clear()
        subjects.addAll(db.getSubjectsList())
        allSubjects.clear()
        allSubjects.addAll(db.getAllSubjects())
        teachers.clear()
        teachers.addAll(db.getTeachersList())
    }

    fun deleteWeek(week: Week) {
        db.deleteWeekById(week)
        loadWeekData(week.fragment)
        notifier?.scheduleEventsForToday()
        widgetRefresher?.refreshAllWidgets()
    }
    
    fun insertWeek(week: Week) {
        db.insertWeek(week)
        loadWeekData(week.fragment)
        loadSuggestions()
        notifier?.scheduleEventsForToday()
        widgetRefresher?.refreshAllWidgets()
    }

    fun updateWeek(week: Week) {
        db.updateWeek(week)
        loadWeekData(week.fragment)
        loadSuggestions()
        notifier?.scheduleEventsForToday()
        widgetRefresher?.refreshAllWidgets()
    }

    fun updateAttendance(weekId: Int, subjectName: String, type: String) {
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        db.updateAttendance(weekId, subjectName, type, date)
        todayAttendance[weekId] = type
        loadSuggestions()
        widgetRefresher?.refreshAllWidgets()
    }

    fun getOngoingClass(): Week? {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val today = dayNames[now.dayOfWeek.ordinal] // ordinal is 0 for Monday, 6 for Sunday?
        // Wait, Sunday is often 0 in many systems. In kotlinx.datetime, DayOfWeek is an enum.
        // DayOfWeek.MONDAY.ordinal is 0, ... SUNDAY is 6.
        // Let's use now.dayOfWeek.name and capitalize it.
        val todayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        
        val nowMinutes = now.hour * 60 + now.minute
        
        return db.getWeek(todayName).find { slot ->
            val partsFrom = slot.fromTime.split(":")
            val partsTo = slot.toTime.split(":")
            if (partsFrom.size >= 2 && partsTo.size >= 2) {
                val start = partsFrom[0].toInt() * 60 + partsFrom[1].toInt()
                var end = partsTo[0].toInt() * 60 + partsTo[1].toInt()
                
                if (end <= start) end += 24 * 60
                nowMinutes in start until end
            } else false
        }
    }

    fun createQuickNote(subjectName: String): Int {
        val subject = db.getSubjectByName(subjectName)
        val subjectId = subject?.id ?: -1
        
        val note = Note().apply {
            this.title = "Quick Note: $subjectName"
            this.text = ""
            this.subjectId = subjectId
            this.color = subject?.color ?: 0
        }
        return db.insertNote(note).toInt()
    }
}
