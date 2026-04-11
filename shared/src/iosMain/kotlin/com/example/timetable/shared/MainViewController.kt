package com.example.timetable.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.example.timetable.data.DatabaseDriverFactory
import com.example.timetable.data.SqlDelightTimetableDatabase
import com.example.timetable.ui.TimetableApp
import com.example.timetable.ui.theme.TimeTableTheme
import com.russhwolf.settings.Settings
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val database = SqlDelightTimetableDatabase(DatabaseDriverFactory().createDriver())
    val settings = Settings()
    
    TimeTableTheme {
        TimetableApp(database = database, settings = settings)
    }
}
