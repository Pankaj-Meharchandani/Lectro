package com.example.timetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timetable.model.Week
import com.example.timetable.ui.components.AddSubjectDialog
import com.example.timetable.ui.components.SubjectItem
import com.example.timetable.ui.viewmodel.MainViewModel
import com.example.timetable.shared.getUriHandler
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToExams: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPersonalDetails: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSubjectDetail: (Int) -> Unit,
    onNavigateToNoteInfo: (Int) -> Unit,
    onNavigateToEditTeacher: (Int) -> Unit,
    onExportPdf: (List<String>, Map<String, List<Week>>) -> Unit = { _, _ -> },
    viewModel: MainViewModel,
    settings: Settings = Settings()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val uriHandler = remember { getUriHandler() }
    
    val switchSevenDays: Boolean = settings.get("seven_days_setting", false)
    val personalDetailsEnabled: Boolean = settings.get("personal_details_setting", true)
    val attendanceEnabled: Boolean = settings.get("attendance_setting", true)
    val minAttendance: Int = settings.get("min_attendance_setting", 75)

    val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val days = remember(switchSevenDays) {
        if (switchSevenDays) dayNames else dayNames.take(5)
    }
    
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val initialDayIndex = remember(days) {
        val dayOfWeek = now.dayOfWeek.isoDayNumber 
        val mondayIndexed = dayOfWeek - 1
        if (mondayIndexed < days.size) mondayIndexed else 0
    }
    
    val pagerState = rememberPagerState(initialPage = initialDayIndex) { days.size }
    var showAddDialog by remember { mutableStateOf(false) }
    var weekToEdit by remember { mutableStateOf<Week?>(null) }
    var weekToDelete by remember { mutableStateOf<Week?>(null) }
    
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery) { viewModel.searchAcrossApp(searchQuery) }

    val ongoingClass = viewModel.getOngoingClass()

    LaunchedEffect(Unit) {
        days.forEach { viewModel.loadWeekData(it) }
        viewModel.loadSuggestions()
        viewModel.loadAttendance()
    }

    val userDetail = viewModel.userDetail

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                if (personalDetailsEnabled) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (userDetail.name.isEmpty()) "Student Name" else userDetail.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (userDetail.rollNumber.isNotEmpty()) {
                                Text(
                                    text = "Roll: ${userDetail.rollNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Lectro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = "Schedule Manager", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                HorizontalDivider()
                NavigationDrawerContent(
                    onExamsClick = onNavigateToExams,
                    onTeachersClick = onNavigateToTeachers,
                    onAssignmentsClick = onNavigateToAssignments,
                    onNotesClick = onNavigateToNotes,
                    onAttendanceClick = onNavigateToAttendance,
                    onSettingsClick = onNavigateToSettings,
                    onAboutClick = onNavigateToAbout,
                    personalDetailsEnabled = personalDetailsEnabled,
                    onPersonalDetailsClick = onNavigateToPersonalDetails,
                    onSchoolWebsiteClick = {
                        val url: String = settings.get("school_website_setting", "")
                        if (url.isNotEmpty()) uriHandler.openUri(url)
                    },
                    onItemClick = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { 
                            if (isSearchActive) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                            } else {
                                Text("Lectro")
                            }
                        },
                        navigationIcon = {
                            if (isSearchActive) {
                                IconButton(onClick = { 
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },
                        actions = {
                            if (isSearchActive) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            } else {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                                IconButton(onClick = { 
                                    onExportPdf(days, viewModel.weekData)
                                }) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                                }
                            }
                        }
                    )
                    if (!isSearchActive) {
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            edgePadding = 0.dp,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            indicator = { tabPositions ->
                                if (pagerState.currentPage < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                        height = 4.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        ) {
                            days.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                    text = { 
                                        Text(
                                            text = title,
                                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (!isSearchActive) {
                    Column(horizontalAlignment = Alignment.End) {
                        ongoingClass?.let { ongoing ->
                            ExtendedFloatingActionButton(
                                onClick = {
                                    val noteId = viewModel.createQuickNote(ongoing.subject)
                                    onNavigateToNoteInfo(noteId)
                                },
                                icon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null) },
                                text = { Text("Note: ${ongoing.subject}") },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        FloatingActionButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isSearchActive) {
                    if (searchQuery.isBlank()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                Text("Search Subjects, Notes, Assignments", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else if (searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No results found for \"$searchQuery\"", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(searchResults) { result ->
                                ListItem(
                                    headlineContent = { Text(result.title) },
                                    overlineContent = { 
                                        Text(
                                            when (result.type) {
                                                MainViewModel.SearchResultType.SUBJECT -> "TIMETABLE"
                                                MainViewModel.SearchResultType.NOTE -> "NOTE"
                                                MainViewModel.SearchResultType.ASSIGNMENT -> "ASSIGNMENT"
                                                MainViewModel.SearchResultType.TEACHER -> "TEACHER"
                                            }
                                        )
                                    },
                                    supportingContent = { result.subtitle?.let { Text(it) } },
                                    leadingContent = {
                                        Icon(
                                            when (result.type) {
                                                MainViewModel.SearchResultType.SUBJECT -> Icons.Default.Schedule
                                                MainViewModel.SearchResultType.NOTE -> Icons.AutoMirrored.Filled.Note
                                                MainViewModel.SearchResultType.ASSIGNMENT -> Icons.AutoMirrored.Filled.Assignment
                                                MainViewModel.SearchResultType.TEACHER -> Icons.Default.Person
                                            },
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        val original = result.originalObject
                                        isSearchActive = false
                                        searchQuery = ""
                                        when (result.type) {
                                            MainViewModel.SearchResultType.SUBJECT -> {
                                                if (original is Week) {
                                                    val dayIndex = days.indexOf(original.fragment)
                                                    if (dayIndex != -1) {
                                                        scope.launch { pagerState.scrollToPage(dayIndex) }
                                                    }
                                                }
                                            }
                                            MainViewModel.SearchResultType.NOTE -> onNavigateToNoteInfo(result.id)
                                            MainViewModel.SearchResultType.ASSIGNMENT -> onNavigateToAssignments()
                                            MainViewModel.SearchResultType.TEACHER -> onNavigateToEditTeacher(result.id)
                                        }
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val dayData = viewModel.weekData[days[page]] ?: emptyList()
                        DayList(
                            subjects = dayData, 
                            attendanceEnabled = attendanceEnabled,
                            minAttendance = minAttendance,
                            onSubjectClick = { week ->
                                val subjectId = viewModel.getSubjectIdByName(week.subject)
                                if (subjectId != -1) {
                                    onNavigateToSubjectDetail(subjectId)
                                }
                            },
                            onMarkAttendance = { weekId, subjectName, type ->
                                viewModel.updateAttendance(weekId, subjectName, type)
                            },
                            onEditClick = { weekToEdit = it },
                            onDeleteClick = { weekToDelete = it },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubjectDialog(
            onDismiss = { showAddDialog = false },
            onSave = { week ->
                viewModel.insertWeek(week)
                showAddDialog = false
            },
            onGetSubjectDetails = { viewModel.getSubjectDetails(it) },
            initialWeek = Week(fragment = days[pagerState.currentPage]),
            subjectSuggestions = viewModel.subjects,
            teacherSuggestions = viewModel.teachers,
            existingSlots = viewModel.weekData[days[pagerState.currentPage]] ?: emptyList()
        )
    }

    weekToEdit?.let { week ->
        AddSubjectDialog(
            onDismiss = { weekToEdit = null },
            onSave = { updatedWeek ->
                viewModel.updateWeek(updatedWeek)
                weekToEdit = null
            },
            onGetSubjectDetails = { viewModel.getSubjectDetails(it) },
            initialWeek = week,
            subjectSuggestions = viewModel.subjects,
            teacherSuggestions = viewModel.teachers,
            existingSlots = viewModel.weekData[week.fragment] ?: emptyList()
        )
    }

    weekToDelete?.let { week ->
        AlertDialog(
            onDismissRequest = { weekToDelete = null },
            title = { Text("Delete Slot") },
            text = { Text("Are you sure you want to delete this ${week.subject} slot?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWeek(week)
                    weekToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { weekToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DayList(
    subjects: List<Week>, 
    attendanceEnabled: Boolean,
    minAttendance: Int,
    onSubjectClick: (Week) -> Unit,
    onMarkAttendance: (Int, String, String) -> Unit,
    onEditClick: (Week) -> Unit,
    onDeleteClick: (Week) -> Unit,
    viewModel: MainViewModel
) {
    if (subjects.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No classes for this day!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(subjects, key = { it.id }) { subject ->
                SubjectItem(
                    subject = subject, 
                    attendanceEnabled = attendanceEnabled,
                    minAttendance = minAttendance,
                    onClick = { onSubjectClick(subject) },
                    onMarkAttendance = onMarkAttendance,
                    onEdit = { onEditClick(subject) },
                    onDelete = { onDeleteClick(subject) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun NavigationDrawerContent(
    onExamsClick: () -> Unit,
    onTeachersClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    onNotesClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    personalDetailsEnabled: Boolean,
    onPersonalDetailsClick: () -> Unit,
    onSchoolWebsiteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (personalDetailsEnabled) {
                NavigationDrawerItem(
                    label = { Text("Personal Details & Files") },
                    selected = false,
                    onClick = { onPersonalDetailsClick(); onItemClick() },
                    icon = { Icon(Icons.Default.Badge, contentDescription = null) }
                )
            }
            NavigationDrawerItem(
                label = { Text("Attendance") },
                selected = false,
                onClick = { onAttendanceClick(); onItemClick() },
                icon = { Icon(Icons.Default.DoneAll, contentDescription = null) }
            )
            NavigationDrawerItem(
                label = { Text("School Website") },
                selected = false,
                onClick = { onSchoolWebsiteClick(); onItemClick() },
                icon = { Icon(Icons.Default.Language, contentDescription = null) }
            )
            NavigationDrawerItem(
                label = { Text("Exams") },
                selected = false,
                onClick = { onExamsClick(); onItemClick() },
                icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) }
            )
            NavigationDrawerItem(
                label = { Text("Teachers") },
                selected = false,
                onClick = { onTeachersClick(); onItemClick() },
                icon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            NavigationDrawerItem(
                label = { Text("Homeworks") },
                selected = false,
                onClick = { onAssignmentsClick(); onItemClick() },
                icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) }
            )
            NavigationDrawerItem(
                label = { Text("Notes") },
                selected = false,
                onClick = { onNotesClick(); onItemClick() },
                icon = { Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                label = { Text("Settings") },
                selected = false,
                onClick = { onSettingsClick(); onItemClick() },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) }
            )
        }

        // Bottom Section
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                label = { Text("About") },
                selected = false,
                onClick = { onAboutClick(); onItemClick() },
                icon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }
    }
}
