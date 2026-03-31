package com.example.timetable.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import coil.compose.AsyncImage
import com.example.timetable.R
import com.example.timetable.model.Week
import com.example.timetable.ui.components.AddSubjectDialog
import com.example.timetable.ui.components.SubjectItem
import com.example.timetable.utils.AppConstants
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.timetable.ui.viewmodel.MainViewModel
import com.example.timetable.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
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
    onNavigateToAbout: () -> Unit,
    onNavigateToSubjectDetail: (Int) -> Unit,
    onNavigateToNoteInfo: (Int) -> Unit,
    onNavigateToEditTeacher: (Int) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    
    val sharedPref = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    var switchSevenDays by remember { 
        mutableStateOf(sharedPref.getBoolean(AppConstants.KEY_SEVEN_DAYS_SETTING, false)) 
    }
    var personalDetailsEnabled by remember {
        mutableStateOf(sharedPref.getBoolean(AppConstants.KEY_PERSONAL_DETAILS_SETTING, true))
    }
    var attendanceEnabled by remember {
        mutableStateOf(sharedPref.getBoolean(AppConstants.KEY_ATTENDANCE_SETTING, true))
    }
    var minAttendance by remember {
        mutableIntStateOf(sharedPref.getInt(AppConstants.KEY_MIN_ATTENDANCE_SETTING, 75))
    }

    DisposableEffect(sharedPref) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                AppConstants.KEY_SEVEN_DAYS_SETTING -> {
                    switchSevenDays = prefs.getBoolean(key, false)
                }
                AppConstants.KEY_PERSONAL_DETAILS_SETTING -> {
                    personalDetailsEnabled = prefs.getBoolean(key, true)
                }
                AppConstants.KEY_ATTENDANCE_SETTING -> {
                    attendanceEnabled = prefs.getBoolean(key, true)
                }
                AppConstants.KEY_MIN_ATTENDANCE_SETTING -> {
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
    
    val initialDayIndex = remember(days) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2, ...
        // Convert to 0-indexed where Monday = 0
        val mondayIndexed = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
        
        if (mondayIndexed < days.size) mondayIndexed else 0
    }
    
    val pagerState = rememberPagerState(initialPage = initialDayIndex) { days.size }
    var showAddDialog by remember { mutableStateOf(false) }
    var weekToEdit by remember { mutableStateOf<Week?>(null) }
    var weekToDelete by remember { mutableStateOf<Week?>(null) }
    
    var showReportIssueDialog by remember { mutableStateOf(false) }
    var showLogDurationDialog by remember { mutableStateOf(false) }
    
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery) { viewModel.searchAcrossApp(searchQuery) }

    // Reactive time for Ongoing Class FAB
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000) // Update every 30 seconds
            currentTimeMillis = System.currentTimeMillis()
        }
    }
    val ongoingClass by remember(currentTimeMillis) { 
        derivedStateOf { viewModel.getOngoingClass() } 
    }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

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

    val userDetail = viewModel.userDetail

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                userDetail.let { user ->
                    Row(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = user.photoPath ?: R.drawable.ic_launcher_foreground,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = user.name ?: "User Name",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Roll: ${user.rollNumber ?: ""}",
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
                    onAboutClick = onNavigateToAbout,
                    personalDetailsEnabled = personalDetailsEnabled,
                    onPersonalDetailsClick = onNavigateToPersonalDetails,
                    onSchoolWebsiteClick = {
                        val url = sharedPref.getString(AppConstants.KEY_SCHOOL_WEBSITE_SETTING, null)
                        if (!TextUtils.isEmpty(url)) {
                            BrowserUtil.openUrlInChromeCustomTab(context, url)
                        } else {
                            Toast.makeText(context, context.getString(R.string.school_website_snackbar), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onItemClick = {
                        scope.launch { drawerState.close() }
                    },
                    onReportIssueClick = { showReportIssueDialog = true }
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
                                Text(stringResource(id = R.string.app_name))
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
                                IconButton(onClick = {
                                    scope.launch { drawerState.open() }
                                }) {
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
                                    PdfExportUtil.exportScheduleToPdf(context, days, viewModel.weekData)
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
                                    val noteId = viewModel.createQuickNote(ongoing.subject ?: "")
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
            initialWeek = Week().apply { fragment = days[pagerState.currentPage] },
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

    if (showReportIssueDialog) {
        AlertDialog(
            onDismissRequest = { showReportIssueDialog = false },
            title = { Text("Report Issue") },
            text = { Text("To help us fix the problem faster, you can include app logs from the past few minutes. Device information will also be included.") },
            confirmButton = {
                Button(onClick = {
                    showReportIssueDialog = false
                    showLogDurationDialog = true
                }) {
                    Text("Attach Logs")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showReportIssueDialog = false
                    val intent = Intent(Intent.ACTION_VIEW, "https://github.com/Pankaj-Meharchandani/Lectro/issues/new".toUri())
                    context.startActivity(intent)
                }) {
                    Text("Just Report Issue")
                }
            }
        )
    }

    if (showLogDurationDialog) {
        AlertDialog(
            onDismissRequest = { showLogDurationDialog = false },
            title = { Text("Log Duration") },
            text = {
                Column {
                    Text("Select how far back to collect logs:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    listOf(1, 5, 10, 15).forEach { mins ->
                        val durationText = if (mins == 1) "1 Minute" else "$mins Minutes"
                        TextButton(
                            onClick = {
                                showLogDurationDialog = false
                                scope.launch(Dispatchers.IO) {
                                    val deviceInfo = getDeviceInfo(context)
                                    val logs = getAppLogs(mins)
                                    withContext(Dispatchers.Main) {
                                        val fullBody = "\n\n$deviceInfo\n\n--- APP LOGS (Past $durationText) ---\n$logs"
                                        
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("App Logs", fullBody)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Issue details copied to clipboard", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(Intent.ACTION_VIEW, 
                                            "https://github.com/Pankaj-Meharchandani/Lectro/issues/new?body=${Uri.encode(fullBody)}".toUri()
                                        )
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback if the body is too long for the URL
                                            val shortIntent = Intent(Intent.ACTION_VIEW, "https://github.com/Pankaj-Meharchandani/Lectro/issues/new".toUri())
                                            context.startActivity(shortIntent)
                                            Toast.makeText(context, "Body too large, please paste from clipboard", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(durationText)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLogDurationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                    Icons.AutoMirrored.Filled.EventNote,
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
                Text(
                    "Relax or add some slots.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
    onItemClick: () -> Unit,
    onReportIssueClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.weight(1f)) {
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
                icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) }
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
                icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) }
            )
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.notes)) },
                selected = false,
                onClick = { onNotesClick(); onItemClick() },
                icon = { Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                label = { Text(stringResource(id = R.string.action_settings)) },
                selected = false,
                onClick = { onSettingsClick(); onItemClick() },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) }
            )
        }

        // Bottom Section
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                label = { Text("Report Issue") },
                selected = false,
                onClick = { 
                    onReportIssueClick()
                    onItemClick() 
                },
                icon = { Icon(Icons.Default.BugReport, contentDescription = null) }
            )
            NavigationDrawerItem(
                label = { Text("About") },
                selected = false,
                onClick = { onAboutClick(); onItemClick() },
                icon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }
    }
}

private fun getDeviceInfo(context: Context): String {
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        null
    }
    val appVersion = packageInfo?.versionName ?: "Unknown"
    return """
        --- DEVICE INFO ---
        App Version: $appVersion
        Android Version: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})
        Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
        -------------------
    """.trimIndent()
}

private fun getAppLogs(minutes: Int): String {
    val log = StringBuilder()
    try {
        val pid = android.os.Process.myPid().toString()
        val process = Runtime.getRuntime().exec("logcat -d -v threadtime")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val startTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, -minutes)
        }
        
        val logTimeFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val currentLine = line ?: continue
            // Basic logcat time format check: MM-dd HH:mm:ss.SSS
            if (currentLine.length > 18 && currentLine[2] == '-' && currentLine[5] == ' ' && currentLine[8] == ':') {
                try {
                    val dateStr = currentLine.substring(0, 18)
                    val logTime = Calendar.getInstance()
                    logTime.time = logTimeFormat.parse(dateStr) ?: continue
                    logTime.set(Calendar.YEAR, currentYear)
                    
                    if (logTime.after(startTime)) {
                        // Filter by the current PID (often appears after the timestamp in threadtime)
                        if (currentLine.contains(pid) || currentLine.contains("com.example.timetable")) {
                            log.append(currentLine).append("\n")
                        }
                    }
                } catch (_: Exception) {
                    // ignore
                }
            }
        }
    } catch (e: Exception) {
        log.append("Error collecting logs: ${e.message}")
    }
    
    // Limit log size to stay within safe URL limits (GitHub + Intent limits)
    val logStr = log.toString()
    return if (logStr.length > 3000) {
        "(Older logs truncated for length...)\n" + logStr.takeLast(3000)
    } else {
        logStr
    }
}
