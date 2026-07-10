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
import com.example.timetable.model.SemesterResult
import com.example.timetable.model.Subject
import com.example.timetable.model.UserDetail
import com.example.timetable.model.Week
import com.example.timetable.utils.DbHelper
import com.example.timetable.utils.NotificationHelper
import com.example.timetable.utils.WidgetUtils
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var latestGrade by mutableStateOf<SemesterResult?>(null)

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

    var searchResults = mutableStateListOf<SearchResult>()
        private set

    fun searchAcrossApp(query: String) {
        searchQuery = query
        if (query.isBlank()) {
            searchResults.clear()
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            val results = mutableListOf<SearchResult>()

            // 1. Search Subjects (Timetable slots)
            db.searchWeeks(query).forEach { week ->
                results.add(SearchResult(
                    SearchResultType.SUBJECT,
                    week.subject,
                    "Timetable • ${week.fragment} • ${week.fromTime}",
                    week,
                    week.id
                ))
            }

            // 2. Search Notes
            db.searchNotes(query).forEach { note ->
                results.add(SearchResult(
                    SearchResultType.NOTE,
                    note.title,
                    "Note • ${note.text.take(30)}...",
                    note,
                    note.id
                ))
            }

            // 3. Search Assignments (Homework)
            db.searchHomework(query).forEach { hw ->
                results.add(SearchResult(
                    SearchResultType.ASSIGNMENT,
                    hw.title ?: "Untitled",
                    "Assignment • ${hw.subject} • Due: ${hw.date}",
                    hw,
                    hw.id
                ))
            }

            // 4. Search Teachers
            db.searchTeachers(query).forEach { teacher ->
                results.add(SearchResult(
                    SearchResultType.TEACHER,
                    teacher.name,
                    "Teacher • ${teacher.post}",
                    teacher,
                    teacher.id
                ))
            }

            val distinctResults = results.distinctBy { it.type.name + it.id }
            withContext(Dispatchers.Main) {
                searchResults.clear()
                searchResults.addAll(distinctResults)
            }
        }
    }

    fun loadAllWeekData(days: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val allData = mutableMapOf<String, List<Week>>()
            val attendance = mutableMapOf<Int, String?>()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            days.forEach { day ->
                val data = db.getWeek(day)
                allData[day] = data
                data.forEach { slot ->
                    attendance[slot.id] = db.getAttendanceStatus(slot.id, date)
                }
            }
            
            val grades = db.getAllSemesterResults()
            
            withContext(Dispatchers.Main) {
                weekData.putAll(allData)
                todayAttendance.putAll(attendance)
                if (grades.isNotEmpty()) {
                    latestGrade = grades.first()
                } else {
                    latestGrade = null
                }
            }
        }
    }

    fun loadWeekData(day: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = db.getWeek(day)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val attendance = mutableMapOf<Int, String?>()
            data.forEach { slot ->
                attendance[slot.id] = db.getAttendanceStatus(slot.id, date)
            }
            withContext(Dispatchers.Main) {
                weekData[day] = data
                todayAttendance.putAll(attendance)
            }
        }
    }

    fun loadAttendance() {
        viewModelScope.launch(Dispatchers.IO) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val attendance = mutableMapOf<Int, String?>()
            weekData.values.flatten().forEach { slot ->
                attendance[slot.id] = db.getAttendanceStatus(slot.id, date)
            }
            withContext(Dispatchers.Main) {
                todayAttendance.putAll(attendance)
            }
        }
    }

    fun loadSuggestions() {
        viewModelScope.launch(Dispatchers.IO) {
            val details = db.getUserDetail()
            val subList = db.getSubjectsList()
            val allSub = db.getAllSubjects()
            val teacherList = db.getTeachersList()
            withContext(Dispatchers.Main) {
                userDetail = details
                subjects.clear()
                subjects.addAll(subList)
                allSubjects.clear()
                allSubjects.addAll(allSub)
                teachers.clear()
                teachers.addAll(teacherList)
            }
        }
    }

    fun getSubjectIdByName(name: String): Int {
        return db.getAllSubjects().find { it.name == name }?.id ?: -1
    }

    fun getSubjectDetails(name: String): Week? {
        return db.getSubjectDetails(name)
    }

    fun deleteWeek(week: Week) {
        viewModelScope.launch(Dispatchers.IO) {
            db.deleteWeekById(week)
            loadWeekData(week.fragment)
            notificationHelper.scheduleEventsForToday()
            WidgetUtils.refreshAllWidgets(getApplication<Application>())
        }
    }
    
    fun insertWeek(week: Week) {
        viewModelScope.launch(Dispatchers.IO) {
            db.insertWeek(week)
            loadWeekData(week.fragment)
            loadSuggestions()
            notificationHelper.scheduleEventsForToday()
            WidgetUtils.refreshAllWidgets(getApplication<Application>())
        }
    }

    fun updateWeek(week: Week) {
        viewModelScope.launch(Dispatchers.IO) {
            db.updateWeek(week)
            loadWeekData(week.fragment)
            loadSuggestions()
            notificationHelper.scheduleEventsForToday()
            WidgetUtils.refreshAllWidgets(getApplication<Application>())
        }
    }

    fun updateAttendance(weekId: Int, subjectName: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            db.updateAttendance(weekId, subjectName, type, date)
            withContext(Dispatchers.Main) {
                todayAttendance[weekId] = type
            }
            loadSuggestions()
            WidgetUtils.refreshAllWidgets(getApplication<Application>())
        }
    }

    fun getAttendanceStatus(weekId: Int): String? {
        // This is still blocking but called in derivedStateOf or remember in some places.
        // For now, let's leave it as is to avoid breaking more things, 
        // but loadAttendance handles the main screen.
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return db.getAttendanceStatus(weekId, date)
    }

    fun getAttendanceForSubject(subjectName: String) = db.getAttendanceForSubject(subjectName)

    fun updateAttendanceByDate(weekId: Int, subjectName: String, type: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.updateAttendanceByDate(weekId, subjectName, type, date)
            loadSuggestions()
            loadAttendance()
        }
    }

    fun deleteAttendanceRecord(weekId: Int, subjectName: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.deleteAttendanceRecord(weekId, subjectName, date)
            loadSuggestions()
            loadAttendance()
        }
    }

    fun getSubjectByName(name: String) = db.getSubjectByName(name)

    fun getAllSubjects() = db.getAllSubjects()

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
