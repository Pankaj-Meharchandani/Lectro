package com.example.timetable.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.timetable.data.TimetableDatabase
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class SettingsViewModel(
    private val db: TimetableDatabase,
    private val settings: Settings = Settings()
) : ViewModel() {

    var sevenDaysEnabled by mutableStateOf(settings.get("seven_days_setting", false))
    var personalDetailsEnabled by mutableStateOf(settings.get("personal_details_setting", true))
    var schoolWebsite by mutableStateOf(settings.get("school_website_setting", ""))

    var notificationsEnabled by mutableStateOf(settings.get("notifications_enabled", false))
    var scheduleReminder by mutableStateOf(settings.get("schedule_reminder", true))
    var assignmentReminder by mutableStateOf(settings.get("assignment_reminder", true))
    var examReminder by mutableStateOf(settings.get("exam_reminder", true))
    var attendanceAlert by mutableStateOf(settings.get("attendance_alert", true))

    fun updateSevenDays(enabled: Boolean) {
        settings["seven_days_setting"] = enabled
        sevenDaysEnabled = enabled
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        settings["notifications_enabled"] = enabled
        notificationsEnabled = enabled
        // Platform specific logic for scheduling/cancelling alarms should be handled elsewhere
    }

    fun updateScheduleReminder(enabled: Boolean) {
        settings["schedule_reminder"] = enabled
        scheduleReminder = enabled
    }

    fun updateAssignmentReminder(enabled: Boolean) {
        settings["assignment_reminder"] = enabled
        assignmentReminder = enabled
    }

    fun updateExamReminder(enabled: Boolean) {
        settings["exam_reminder"] = enabled
        examReminder = enabled
    }

    fun updateAttendanceAlert(enabled: Boolean) {
        settings["attendance_alert"] = enabled
        attendanceAlert = enabled
    }

    fun updatePersonalDetails(enabled: Boolean) {
        settings["personal_details_setting"] = enabled
        personalDetailsEnabled = enabled
    }

    fun updateSchoolWebsite(url: String) {
        settings["school_website_setting"] = url
        schoolWebsite = url
    }

    fun resetData() {
        // db.resetAllData() 
    }
}
