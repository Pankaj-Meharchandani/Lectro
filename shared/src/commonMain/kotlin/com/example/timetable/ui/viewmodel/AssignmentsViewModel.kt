package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.Homework
import com.example.timetable.model.Week
import com.example.timetable.shared.Notifier
import com.example.timetable.shared.WidgetRefresher

class AssignmentsViewModel(
    private val db: TimetableDatabase,
    private val notifier: Notifier? = null,
    private val widgetRefresher: WidgetRefresher? = null
) : ViewModel() {
    
    val assignments = mutableStateListOf<Homework>()
    val subjects = mutableStateListOf<String>()

    fun loadAssignments() {
        assignments.clear()
        assignments.addAll(db.getHomework())
    }

    fun loadSuggestions() {
        subjects.clear()
        subjects.addAll(db.getSubjectsList())
    }

    fun getSubjectDetails(name: String): Week? {
        return db.getSubjectDetails(name)
    }

    fun deleteAssignment(assignment: Homework) {
        db.deleteHomeworkById(assignment)
        loadAssignments()
        notifier?.scheduleEventsForToday()
        widgetRefresher?.refreshAllWidgets()
    }

    fun insertAssignment(assignment: Homework) {
        db.insertHomework(assignment)
        loadAssignments()
        loadSuggestions()
        notifier?.scheduleEventsForToday()
        widgetRefresher?.refreshAllWidgets()
    }

    fun updateAssignment(assignment: Homework) {
        db.updateHomework(assignment)
        loadAssignments()
        notifier?.scheduleEventsForToday()
        widgetRefresher?.refreshAllWidgets()
    }

    fun toggleComplete(assignment: Homework) {
        assignment.completed = if (assignment.completed == 1) 0 else 1
        db.updateHomework(assignment)
        loadAssignments()
        notifier?.scheduleEventsForToday()
        widgetRefresher?.refreshAllWidgets()
    }
}
