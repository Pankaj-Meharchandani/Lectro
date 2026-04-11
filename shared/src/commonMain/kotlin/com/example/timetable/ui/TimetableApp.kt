package com.example.timetable.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.timetable.data.TimetableDatabase
import com.example.timetable.model.Note
import com.example.timetable.model.Week
import com.example.timetable.shared.Notifier
import com.example.timetable.shared.WidgetRefresher
import com.example.timetable.shared.getFileHandler
import com.example.timetable.shared.getPlatform
import com.example.timetable.ui.components.ImportConflictDialog
import com.example.timetable.ui.screens.*
import com.example.timetable.ui.viewmodel.*
import com.example.timetable.utils.ScheduleExporter
import com.example.timetable.utils.SemesterArchiveManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.datetime.Clock

@Composable
fun TimetableApp(
    database: TimetableDatabase,
    settings: Settings = Settings(),
    notifier: Notifier? = null,
    widgetRefresher: WidgetRefresher? = null,
    onExportSchedule: (String) -> Unit = {},
    onImportSchedule: ((String) -> Unit) -> Unit = {},
    onPickImage: (callback: (String) -> Unit) -> Unit = {},
    onPickFile: (callback: (String, String, String) -> Unit) -> Unit = { _ -> },
    onOpenFile: (String, String) -> Unit = { _, _ -> },
    onExportPdf: (List<String>, Map<String, List<Week>>) -> Unit = { _, _ -> },
    onShareText: (String) -> Unit = {},
    onReportIssue: () -> Unit = {}
) {
    val navController = rememberNavController()
    val onboardingCompleted: Boolean = settings.get("onboarding_completed", false)
    val fileHandler = remember { getFileHandler() }
    val platform = remember { getPlatform() }
    
    val mainViewModel = remember { MainViewModel(database, notifier, widgetRefresher) }

    var pendingImportWeeks by remember { mutableStateOf<List<Week>?>(null) }
    var importConflicts by remember { mutableStateOf<List<Pair<Week, Week>>?>(null) }

    if (importConflicts != null && pendingImportWeeks != null) {
        ImportConflictDialog(
            conflicts = importConflicts!!,
            onDismiss = { importConflicts = null; pendingImportWeeks = null },
            onKeepNew = {
                importConflicts!!.forEach { (_, existing) -> database.deleteWeekById(existing) }
                ScheduleExporter.importWeeks(database, pendingImportWeeks!!)
                importConflicts = null; pendingImportWeeks = null; mainViewModel.loadSuggestions()
                platform.showToast("Imported successfully (new kept)")
            },
            onKeepExisting = {
                val clashingNew = importConflicts!!.map { it.first }
                val nonClashing = pendingImportWeeks!!.filter { it !in clashingNew }
                ScheduleExporter.importWeeks(database, nonClashing)
                importConflicts = null; pendingImportWeeks = null; mainViewModel.loadSuggestions()
                platform.showToast("Imported non-clashing slots")
            }
        )
    }

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
                onNavigateToEditTeacher = { teacherId -> navController.navigate("teachers?editTeacherId=$teacherId") },
                onExportPdf = onExportPdf,
                onReportIssue = onReportIssue,
                viewModel = mainViewModel,
                settings = settings
            )
        }
        composable("attendance") {
            AttendanceScreen(onBack = { navController.popBackStack() }, viewModel = mainViewModel, settings = settings)
        }
        composable("personal_details") {
            val personalViewModel = remember { PersonalDetailsViewModel(database) }
            PersonalDetailsScreen(
                onBack = { navController.popBackStack() }, 
                viewModel = personalViewModel,
                onPickImage = onPickImage,
                onPickFile = onPickFile,
                onOpenFile = onOpenFile
            )
        }
        composable("exams") {
            val examViewModel = remember { ExamViewModel(database) }
            ExamsScreen(onBack = { navController.popBackStack() }, viewModel = examViewModel)
        }
        composable(
            route = "teachers?editTeacherId={teacherId}",
            arguments = listOf(navArgument("teacherId") { 
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: -1
            val teacherViewModel = remember { TeacherViewModel(database) }
            TeachersScreen(
                onBack = { navController.popBackStack() },
                editTeacherId = if (teacherId != -1) teacherId else null,
                viewModel = teacherViewModel
            )
        }
        composable("assignments") {
            val assignmentsViewModel = remember { AssignmentsViewModel(database, notifier, widgetRefresher) }
            AssignmentsScreen(onBack = { navController.popBackStack() }, viewModel = assignmentsViewModel)
        }
        composable("notes") {
            val noteViewModel = remember { NoteViewModel(database) }
            NotesScreen(
                onBack = { navController.popBackStack() },
                onSubjectClick = { subjectId -> navController.navigate("subject_detail/$subjectId") },
                viewModel = noteViewModel,
                mainViewModel = mainViewModel,
                settings = settings
            )
        }
        composable(
            route = "note_info/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            val noteInfoViewModel = remember { NoteInfoViewModel(database) }
            NoteInfoScreen(
                noteId = noteId, 
                onBack = { navController.popBackStack() }, 
                onShareText = onShareText,
                onSharePdf = { /* Android PDF callback needed */ },
                onPickImage = onPickImage,
                viewModel = noteInfoViewModel
            )
        }
        composable(
            route = "subject_detail/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            val subjectDetailViewModel = remember { SubjectDetailViewModel(database) }
            SubjectDetailScreen(
                subjectId = subjectId, 
                onBack = { navController.popBackStack() },
                onNoteClick = { noteId -> navController.navigate("note_info/$noteId") },
                viewModel = subjectDetailViewModel,
                settings = settings,
                onPickFile = onPickFile,
                onOpenFile = onOpenFile
            )
        }
        composable("settings") {
            val settingsViewModel = remember { SettingsViewModel(database, settings) }
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToArchives = { navController.navigate("archives") },
                onExportSchedule = {
                    val content = ScheduleExporter.exportSchedule(database)
                    onExportSchedule(content)
                },
                onImportSchedule = {
                    onImportSchedule { content ->
                        val weeks = ScheduleExporter.parseLecFile(content)
                        val conflicts = ScheduleExporter.findConflicts(database, weeks)
                        if (conflicts.isNotEmpty()) {
                            pendingImportWeeks = weeks
                            importConflicts = conflicts
                        } else {
                            ScheduleExporter.importWeeks(database, weeks)
                            mainViewModel.loadSuggestions()
                        }
                    }
                },
                onArchiveSemester = { name ->
                    val content = SemesterArchiveManager.createArchive(database, name, Clock.System.now().toEpochMilliseconds())
                    fileHandler.saveArchive("archive_${Clock.System.now().toEpochMilliseconds()}.json", content)
                    database.getAllWeeks().forEach { database.deleteWeekById(it) }
                    database.getAllSubjects().forEach { database.deleteSubjectById(it.id) }
                    mainViewModel.loadSuggestions()
                },
                viewModel = settingsViewModel
            )
        }
        composable("archives") {
            ArchivesScreen(onBack = { navController.popBackStack() }, onNavigateToArchiveDetail = { name -> navController.navigate("archive_detail/$name") })
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
