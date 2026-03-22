package com.example.timetable.ui.screens

import android.text.TextUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.example.timetable.R
import com.example.timetable.activities.SettingsActivity
import com.example.timetable.model.Week
import com.example.timetable.ui.components.AddSubjectDialog
import com.example.timetable.ui.components.SubjectItem
import com.example.timetable.ui.viewmodel.MainViewModel
import com.example.timetable.utils.BrowserUtil
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToExams: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSubjectDetail: (Int) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val sharedPref = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    val switchSevenDays = remember { 
        sharedPref.getBoolean(SettingsActivity.KEY_SEVEN_DAYS_SETTING, false) 
    }
    
    val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val days = remember(switchSevenDays) {
        if (switchSevenDays) dayNames else dayNames.take(5)
    }
    
    val pagerState = rememberPagerState(pageCount = { days.size })
    var showAddDialog by remember { mutableStateOf(false) }
    var weekToEdit by remember { mutableStateOf<Week?>(null) }

    LaunchedEffect(days) {
        days.forEach { viewModel.loadWeekData(it) }
        viewModel.loadSuggestions()
    }
    
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val targetPage = when(day) {
            Calendar.SUNDAY -> if (switchSevenDays) 6 else 0
            else -> (day - 2).coerceIn(0, 4)
        }.coerceIn(0, days.size - 1)
        pagerState.scrollToPage(targetPage)
    }

    if (showAddDialog || weekToEdit != null) {
        AddSubjectDialog(
            onDismiss = { 
                showAddDialog = false
                weekToEdit = null
            },
            onSave = { week: Week ->
                if (weekToEdit != null) {
                    viewModel.updateWeek(week)
                } else {
                    week.setFragment(days[pagerState.currentPage])
                    viewModel.insertWeek(week)
                }
                weekToEdit = null
            },
            onGetSubjectDetails = { viewModel.getSubjectDetails(it) },
            initialWeek = weekToEdit ?: Week(),
            subjectSuggestions = viewModel.subjects,
            teacherSuggestions = viewModel.teachers
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    onExamsClick = onNavigateToExams,
                    onTeachersClick = onNavigateToTeachers,
                    onAssignmentsClick = onNavigateToAssignments,
                    onNotesClick = onNavigateToNotes,
                    onSettingsClick = onNavigateToSettings,
                    onSchoolWebsiteClick = {
                        val url = sharedPref.getString(SettingsActivity.KEY_SCHOOL_WEBSITE_SETTING, null)
                        if (!TextUtils.isEmpty(url)) {
                            BrowserUtil.openUrlInChromeCustomTab(context, url)
                        }
                    },
                    onItemClick = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.app_name)) },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
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
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { padding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) { page ->
                val dayData = viewModel.weekData[days[page]] ?: emptyList()
                DayList(
                    subjects = dayData, 
                    onSubjectClick = { week ->
                        val subjectId = viewModel.getSubjectIdByName(week.subject)
                        if (subjectId != -1) {
                            onNavigateToSubjectDetail(subjectId)
                        }
                    },
                    onEditClick = { weekToEdit = it },
                    onDeleteClick = { viewModel.deleteWeek(it) }
                )
            }
        }
    }
}

@Composable
fun DayList(
    subjects: List<Week>, 
    onSubjectClick: (Week) -> Unit,
    onEditClick: (Week) -> Unit,
    onDeleteClick: (Week) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(subjects) { subject ->
            SubjectItem(
                subject = subject, 
                onClick = { onSubjectClick(subject) },
                onEdit = { onEditClick(subject) },
                onDelete = { onDeleteClick(subject) }
            )
        }
    }
}

@Composable
fun NavigationDrawerContent(
    onExamsClick: () -> Unit,
    onTeachersClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    onNotesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSchoolWebsiteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(stringResource(id = R.string.school_website)) },
        selected = false,
        onClick = { onSchoolWebsiteClick(); onItemClick() },
        icon = { Icon(Icons.Default.Language, contentDescription = null) }
    )
    NavigationDrawerItem(
        label = { Text(stringResource(id = R.string.exams)) },
        selected = false,
        onClick = { onExamsClick(); onItemClick() },
        icon = { Icon(Icons.Default.Assignment, contentDescription = null) }
    )
    NavigationDrawerItem(
        label = { Text(stringResource(id = R.string.teachers)) },
        selected = false,
        onClick = { onTeachersClick(); onItemClick() },
        icon = { Icon(Icons.Default.Person, contentDescription = null) }
    )
    NavigationDrawerItem(
        label = { Text(stringResource(id = R.string.homeworks)) },
        selected = false,
        onClick = { onAssignmentsClick(); onItemClick() },
        icon = { Icon(Icons.Default.Assignment, contentDescription = null) }
    )
    NavigationDrawerItem(
        label = { Text(stringResource(id = R.string.notes)) },
        selected = false,
        onClick = { onNotesClick(); onItemClick() },
        icon = { Icon(Icons.Default.Note, contentDescription = null) }
    )
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    NavigationDrawerItem(
        label = { Text(stringResource(id = R.string.action_settings)) },
        selected = false,
        onClick = { onSettingsClick(); onItemClick() },
        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
    )
}
