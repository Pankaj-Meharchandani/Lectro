package com.example.timetable.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.timetable.ui.screens.*

@Composable
fun TimetableApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onNavigateToExams = { navController.navigate("exams") },
                onNavigateToTeachers = { navController.navigate("teachers") },
                onNavigateToHomeworks = { navController.navigate("homeworks") },
                onNavigateToNotes = { navController.navigate("notes") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToSubjectDetail = { subjectId -> navController.navigate("subject_detail/$subjectId") }
            )
        }
        composable("exams") {
            ExamsScreen(onBack = { navController.popBackStack() })
        }
        composable("teachers") {
            TeachersScreen(onBack = { navController.popBackStack() })
        }
        composable("homeworks") {
            HomeworksScreen(onBack = { navController.popBackStack() })
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
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
