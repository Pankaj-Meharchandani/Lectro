package com.example.timetable.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.preference.PreferenceManager
import com.example.timetable.utils.AppConstants
import com.example.timetable.ui.screens.*

@Composable
fun TimetableApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPref = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    val onboardingCompleted = sharedPref.getBoolean(AppConstants.KEY_ONBOARDING_COMPLETED, false)
    
    val navigateBack: () -> Unit = {
        if (navController.previousBackStackEntry != null &&
            navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            navController.popBackStack()
        }
    }

    val navigateTo: (String) -> Unit = { route ->
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            navController.navigate(route)
        }
    }
    
    NavHost(
        navController = navController, 
        startDestination = if (onboardingCompleted) "main" else "onboarding",
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = EaseIn)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = EaseIn)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable("onboarding") {
            OnboardingScreen(onFinished = {
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    sharedPref.edit().putBoolean(AppConstants.KEY_ONBOARDING_COMPLETED, true).apply()
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            })
        }
        composable("main") {
            MainScreen(
                onNavigateToExams = { navigateTo("exams") },
                onNavigateToTeachers = { navigateTo("teachers") },
                onNavigateToAssignments = { navigateTo("assignments") },
                onNavigateToNotes = { navigateTo("notes") },
                onNavigateToSettings = { navigateTo("settings") },
                onNavigateToPersonalDetails = { navigateTo("personal_details") },
                onNavigateToAttendance = { navigateTo("attendance") },
                onNavigateToGradeHistory = { navigateTo("grade_history") },
                onNavigateToAbout = { navigateTo("about") },
                onNavigateToSubjectDetail = { subjectId -> navigateTo("subject_detail/$subjectId") },
                onNavigateToNoteInfo = { noteId -> navigateTo("note_info/$noteId") },
                onNavigateToEditTeacher = { teacherId -> navigateTo("teachers?editTeacherId=$teacherId") }
            )
        }
        composable("attendance") {
            AttendanceScreen(onBack = navigateBack)
        }
        composable("personal_details") {
            PersonalDetailsScreen(onBack = navigateBack)
        }
        composable("grade_history") {
            GradeHistoryScreen(onBack = navigateBack)
        }
        composable("exams") {
            ExamsScreen(onBack = navigateBack)
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
                onBack = navigateBack,
                editTeacherId = if (teacherId != -1) teacherId else null
            )
        }
        composable("assignments") {
            AssignmentsScreen(onBack = navigateBack)
        }
        composable("notes") {
            NotesScreen(
                onBack = navigateBack,
                onSubjectClick = { subjectId -> navigateTo("subject_detail/$subjectId") }
            )
        }
        composable(
            route = "note_info/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            NoteInfoScreen(noteId = noteId, onBack = navigateBack)
        }
        composable(
            route = "subject_detail/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            SubjectDetailScreen(
                subjectId = subjectId, 
                onBack = navigateBack,
                onNoteClick = { noteId -> navigateTo("note_info/$noteId") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = navigateBack,
                onNavigateToArchives = { navigateTo("archives") }
            )
        }
        composable("archives") {
            ArchivesScreen(
                onBack = navigateBack,
                onNavigateToArchiveDetail = { fileName -> navigateTo("archive_detail/$fileName") }
            )
        }
        composable(
            route = "archive_detail/{fileName}",
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            ArchiveDetailScreen(fileName = fileName, onBack = navigateBack)
        }
        composable("about") {
            AboutScreen(onBack = navigateBack)
        }
    }
}
