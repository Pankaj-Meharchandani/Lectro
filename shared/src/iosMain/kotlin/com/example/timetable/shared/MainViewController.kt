package com.example.timetable.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.example.timetable.ui.TimetableApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    TimetableApp()
}
