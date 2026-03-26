package com.example.timetable.ui.screens

import android.content.SharedPreferences
import android.text.TextUtils
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import coil.compose.AsyncImage
import com.example.timetable.R
import com.example.timetable.activities.SettingsActivity
import com.example.timetable.model.Week
import com.example.timetable.ui.components.AddSubjectDialog
import com.example.timetable.ui.components.SubjectItem
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.timetable.ui.viewmodel.MainViewModel
import com.example.timetable.utils.*
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
    onNavigateToPersonalDetails: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSubjectDetail: (Int) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    
    val sharedPref = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    var switchSevenDays by remember { 
        mutableStateOf(sharedPref.getBoolean(SettingsActivity.KEY_SEVEN_DAYS_SETTING, false)) 
    }
    var personalDetailsEnabled by remember {
        mutableStateOf(sharedPref.getBoolean(SettingsActivity.KEY_PERSONAL_DETAILS_SETTING, true))
    }
    var attendanceEnabled by remember {
        mutableStateOf(sharedPref.getBoolean(SettingsActivity.KEY_ATTENDANCE_SETTING, true))
    }
    var minAttendance by remember {
        mutableIntStateOf(sharedPref.getInt(SettingsActivity.KEY_MIN_ATTENDANCE_SETTING, 75))
    }

    DisposableEffect(sharedPref) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                SettingsActivity.KEY_SEVEN_DAYS_SETTING -> {
                    switchSevenDays = prefs.getBoolean(key, false)
                }
                SettingsActivity.KEY_PERSONAL_DETAILS_SETTING -> {
                    personalDetailsEnabled = prefs.getBoolean(key, true)
                }
                SettingsActivity.KEY_ATTENDANCE_SETTING -> {
                    attendanceEnabled = prefs.getBoolean(key, true)
                }
                SettingsActivity.KEY_MIN_ATTENDANCE_SETTING -> {
                    minAttendance = prefs.getInt(key, 75)
                }
            }
        }
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val days = remember(switchSevenDays) {
        if (switchSevenDays) dayNames else dayNames.take(5)
    }
    
    val pagerState = rememberPagerState(pageCount = { days.size })
    var showAddDialog by remember { mutableStateOf(false) }
    var weekToEdit by remember { mutableStateOf<Week?>(null) }
    var weekToDelete by remember { mutableStateOf<Week?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                days.forEach { viewModel.loadWeekData(it) }
                viewModel.loadSuggestions()
                viewModel.loadAttendance()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(days) {
        days.forEach { viewModel.loadWeekData(it) }
        viewModel.loadSuggestions()
    }
    
    LaunchedEffect(Unit) {
        NotificationHelper(context)
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val currentVersion = packageInfo.versionName ?: ""
        val info = UpdateManager.checkForUpdates(currentVersion)
        if (info != null && !UpdateManager.isVersionIgnored(context, info.latestVersion)) {
            updateInfo = info
        }
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
        val currentDay = if (weekToEdit != null) weekToEdit!!.getFragment() else days[pagerState.currentPage]
        AddSubjectDialog(
            onDismiss = { 
                showAddDialog = false
                weekToEdit = null
            },
            onSave = { week: Week ->
                if (weekToEdit != null) {
                    viewModel.updateWeek(week)
                } else {
                    week.setFragment(currentDay)
                    viewModel.insertWeek(week)
                }
                weekToEdit = null
            },
            onGetSubjectDetails = { viewModel.getSubjectDetails(it) },
            initialWeek = weekToEdit ?: Week(),
            subjectSuggestions = viewModel.subjects,
            teacherSuggestions = viewModel.teachers,
            existingSlots = viewModel.weekData[currentDay] ?: emptyList()
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                if (personalDetailsEnabled) {
                    val userDetail = viewModel.userDetail
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!userDetail.photoPath.isNullOrBlank()) {
                            AsyncImage(
                                model = userDetail.photoPath,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = userDetail.name?.ifBlank { "User Name" } ?: "User Name",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (!userDetail.email.isNullOrBlank()) {
                            Text(
                                text = userDetail.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!userDetail.rollNumber.isNullOrBlank()) {
                            Text(
                                text = "Roll: ${userDetail.rollNumber ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider()
                }
                NavigationDrawerContent(
                    onExamsClick = onNavigateToExams,
                    onTeachersClick = onNavigateToTeachers,
                    onAssignmentsClick = onNavigateToAssignments,
                    onNotesClick = onNavigateToNotes,
                    onAttendanceClick = onNavigateToAttendance,
                    onSettingsClick = onNavigateToSettings,
                    personalDetailsEnabled = personalDetailsEnabled,
                    onPersonalDetailsClick = onNavigateToPersonalDetails,
                    onSchoolWebsiteClick = {
                        val url = sharedPref.getString(SettingsActivity.KEY_SCHOOL_WEBSITE_SETTING, null)
                        if (!TextUtils.isEmpty(url)) {
                            BrowserUtil.openUrlInChromeCustomTab(context, url)
                        } else {
                            Toast.makeText(context, context.getString(R.string.school_website_snackbar), Toast.LENGTH_SHORT).show()
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
                        },
                        actions = {
                            IconButton(onClick = {
                                PdfExportUtil.exportScheduleToPdf(context, days, viewModel.weekData)
                            }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
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
                    onDeleteClick = { weekToDelete = it }
                )
            }
        }
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
    updateInfo?.let { info ->
        BasicAlertDialog(
            onDismissRequest = { updateInfo = null },
            modifier = Modifier.fillMaxWidth(0.92f),
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "New update available",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Update the app to version v${info.latestVersion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "What's new",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Box(modifier = Modifier
                        .heightIn(max = 250.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = parseMarkdown(info.releaseNotes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { updateInfo = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Later")
                        }
                        Button(
                            onClick = {
                                UpdateManager.openUpdateUrl(context, info.downloadUrl)
                                updateInfo = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Update")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            UpdateManager.ignoreVersion(context, info.latestVersion)
                            updateInfo = null
                        }
                    ) {
                        Text("Ignore this version", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun parseMarkdown(text: String): AnnotatedString {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    
    return buildAnnotatedString {
        val lines = text.split('\n')
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append(trimmed.substring(2))
                    }
                }
                trimmed.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryColor)) {
                        append(trimmed.substring(3))
                    }
                }
                trimmed.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) {
                        append(trimmed.substring(4))
                    }
                }
                trimmed.startsWith("- ") -> {
                    append(" • ")
                    append(parseInlineMarkdown(trimmed.substring(2)))
                }
                trimmed == "---" -> {
                    withStyle(SpanStyle(color = outlineColor)) {
                        append("────────────────────────")
                    }
                }
                else -> {
                    append(parseInlineMarkdown(line))
                }
            }
            if (index < lines.size - 1) append("\n")
        }
    }
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
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
    onDeleteClick: (Week) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(subjects) { subject ->
            SubjectItem(
                subject = subject, 
                attendanceEnabled = attendanceEnabled,
                minAttendance = minAttendance,
                onClick = { onSubjectClick(subject) },
                onMarkAttendance = onMarkAttendance,
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
    onAttendanceClick: () -> Unit,
    onSettingsClick: () -> Unit,
    personalDetailsEnabled: Boolean,
    onPersonalDetailsClick: () -> Unit,
    onSchoolWebsiteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    if (personalDetailsEnabled) {
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.personal_details_files)) },
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
