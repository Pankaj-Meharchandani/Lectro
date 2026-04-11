package com.example.timetable.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.timetable.ui.screens.*
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get

@Composable
fun TimetableApp(settings: Settings = Settings()) {
    val navController = rememberNavController()
    val onboardingCompleted: Boolean = settings.get("onboarding_completed", false)
    
    NavHost(
        navController = navController, 
        startDestination = if (onboardingCompleted) "main" else "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(onFinished = {
                settings.putBoolean("onboarding_completed", true)
                navController.navigate("main") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("main") {
            MainScreen(
                onNavigateToExams = { navController.navigate("exams") },
                onNavigateToTeachers = { navController.navigate("teachers") },
                onNavigateToAssignments = { navController.navigate("assignments") },
                onNavigateToNotes = { navController.navigate("notes") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToPersonalDetails = { navController.navigate("personal_details") },
                onNavigateToAttendance = { navController.navigate("attendance") },
                onNavigateToAbout = { navController.navigate("about") },
                onNavigateToSubjectDetail = { subjectId -> navController.navigate("subject_detail/$subjectId") },
                onNavigateToNoteInfo = { noteId -> navController.navigate("note_info/$noteId") },
                onNavigateToEditTeacher = { teacherId -> navController.navigate("teachers?editTeacherId=$teacherId") }
            )
        }
        composable("attendance") {
            AttendanceScreen(onBack = { navController.popBackStack() })
        }
        composable("personal_details") {
            PersonalDetailsScreen(onBack = { navController.popBackStack() })
        }
        composable("exams") {
            ExamsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "teachers?editTeacherId={teacherId}",
            arguments = listOf(navArgument("teacherId") { 
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: -1
            TeachersScreen(
                onBack = { navController.popBackStack() },
                editTeacherId = if (teacherId != -1) teacherId else null
            )
        }
        composable("assignments") {
            AssignmentsScreen(onBack = { navController.popBackStack() })
        }
        composable("notes") {
            NotesScreen(
                onBack = { navController.popBackStack() },
                onSubjectClick = { subjectId -> navController.navigate("subject_detail/$subjectId") }
            )
        }
        composable(
            route = "note_info/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            NoteInfoScreen(noteId = noteId, onBack = { navController.popBackStack() })
        }
        composable(
            route = "subject_detail/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            SubjectDetailScreen(
                subjectId = subjectId, 
                onBack = { navController.popBackStack() },
                onNoteClick = { noteId -> navController.navigate("note_info/$noteId") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToArchives = { navController.navigate("archives") }
            )
        }
        composable("archives") {
            ArchivesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToArchiveDetail = { fileName -> navController.navigate("archive_detail/$fileName") }
            )
        }
        composable(
            route = "archive_detail/{fileName}",
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            ArchiveDetailScreen(fileName = fileName, onBack = { navController.popBackStack() })
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
