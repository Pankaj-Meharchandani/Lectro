package com.example.timetable.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.timetable.model.Homework
import com.example.timetable.model.Note
import com.example.timetable.model.Subject
import com.example.timetable.model.UserDetail
import com.example.timetable.model.Week
import com.example.timetable.utils.DbHelper
import com.example.timetable.utils.NotificationHelper
import com.example.timetable.utils.WidgetUtils
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    private val notificationHelper = NotificationHelper(application)
    
    val weekData = mutableStateMapOf<String, List<Week>>()
    var subjects = mutableStateListOf<String>()
    var allSubjects = mutableStateListOf<Subject>()
    var teachers = mutableStateListOf<String>()
    var userDetail by mutableStateOf(UserDetail())
    val todayAttendance = mutableStateMapOf<Int, String?>()

    // Search related
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

        // 1. Search Subjects (Timetable slots)
        db.getAllWeeks().forEach { week ->
            if (week.subject.lowercase().contains(lowercaseQuery) || 
                (week.teacher?.lowercase()?.contains(lowercaseQuery) == true)) {
                results.add(SearchResult(
                    SearchResultType.SUBJECT,
                    week.subject,
                    "Timetable • ${week.fragment} • ${week.fromTime}",
                    week,
                    week.id
                ))
            }
        }

        // 2. Search Notes
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

        // 3. Search Assignments (Homework)
        db.getHomework().forEach { hw ->
            if (hw.title.lowercase().contains(lowercaseQuery) || 
                hw.subject.lowercase().contains(lowercaseQuery) || 
                hw.description.lowercase().contains(lowercaseQuery)) {
                results.add(SearchResult(
                    SearchResultType.ASSIGNMENT,
                    hw.title ?: "Untitled",
                    "Assignment • ${hw.subject} • Due: ${hw.date}",
                    hw,
                    hw.id
                ))
            }
        }

        // 4. Search Teachers
        db.getTeacher().forEach { teacher ->
            if (teacher.name.lowercase().contains(lowercaseQuery) ||
                teacher.post.lowercase().contains(lowercaseQuery) ||
                (teacher.email?.lowercase()?.contains(lowercaseQuery) == true)) {
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
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        weekData.values.flatten().forEach { slot ->
            todayAttendance[slot.id] = db.getAttendanceStatus(slot.id, date)
        }
    }

    fun loadSuggestions() {
        userDetail = db.getUserDetail()
        subjects.clear()
        subjects.addAll(db.getSubjectsList())
        allSubjects.clear()
        allSubjects.addAll(db.allSubjects)
        teachers.clear()
        teachers.addAll(db.getTeachersList())
    }

    fun getSubjectIdByName(name: String): Int {
        return db.getAllSubjects().find { it.name == name }?.id ?: -1
    }

    fun getSubjectDetails(name: String): Week? {
        return db.getSubjectDetails(name)
    }

    fun deleteWeek(week: Week) {
        db.deleteWeekById(week)
        loadWeekData(week.fragment)
        notificationHelper.scheduleEventsForToday()
        viewModelScope.launch { WidgetUtils.refreshAllWidgets(getApplication()) }
    }
    
    fun insertWeek(week: Week) {
        db.insertWeek(week)
        loadWeekData(week.fragment)
        loadSuggestions()
        notificationHelper.scheduleEventsForToday()
        viewModelScope.launch { WidgetUtils.refreshAllWidgets(getApplication()) }
    }

    fun updateWeek(week: Week) {
        db.updateWeek(week)
        loadWeekData(week.fragment)
        loadSuggestions()
        notificationHelper.scheduleEventsForToday()
        viewModelScope.launch { WidgetUtils.refreshAllWidgets(getApplication()) }
    }

    fun updateAttendance(weekId: Int, subjectName: String, type: String) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.updateAttendance(weekId, subjectName, type, date)
        todayAttendance[weekId] = type
        loadSuggestions()
        
        viewModelScope.launch { WidgetUtils.refreshAllWidgets(getApplication()) }
    }

    fun getAttendanceStatus(weekId: Int): String? {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return db.getAttendanceStatus(weekId, date)
    }

    fun getAttendanceForSubject(subjectName: String) = db.getAttendanceForSubject(subjectName)

    fun updateAttendanceByDate(weekId: Int, subjectName: String, type: String, date: String) {
        db.updateAttendanceByDate(weekId, subjectName, type, date)
        loadSuggestions()
        loadAttendance()
    }

    fun deleteAttendanceRecord(weekId: Int, subjectName: String, date: String) {
        db.deleteAttendanceRecord(weekId, subjectName, date)
        loadSuggestions()
        loadAttendance()
    }

    fun getSubjectByName(name: String) = db.getSubjectByName(name)

    fun getAllSubjects() = db.allSubjects

    fun getOngoingClass(): Week? {
        val now = Calendar.getInstance()
        val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val today = dayNames[now.get(Calendar.DAY_OF_WEEK) - 1]
        
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        
        return db.getWeek(today).find { slot ->
            val partsFrom = slot.fromTime?.split(":")
            val partsTo = slot.toTime?.split(":")
            if (partsFrom?.size == 2 && partsTo?.size == 2) {
                val start = partsFrom[0].toInt() * 60 + partsFrom[1].toInt()
                var end = partsTo[0].toInt() * 60 + partsTo[1].toInt()
                
                // Handle midnight (e.g., 11:30 PM to 12:00 AM)
                if (end <= start) {
                    end += 24 * 60
                }

                nowMinutes in start until end
            } else false
        }
    }

    fun createQuickNote(subjectName: String): Int {
        val subject = db.getSubjectByName(subjectName)
        val subjectId = if (subject != null) {
            subject.id
        } else {
            // Subject doesn't exist, we could create it but for now just default to -1 or similar
            // Actually, let's just use the subject name to find it in the subjects table
            db.getAllSubjects().find { it.name == subjectName }?.id ?: -1
        }
        
        val note = Note().apply {
            this.title = "Quick Note: $subjectName"
            this.text = ""
            this.subjectId = subjectId
            this.color = subject?.color ?: 0
        }
        return db.insertNote(note).toInt()
    }
}
