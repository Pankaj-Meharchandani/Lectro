package com.example.timetable.ui.screens

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.R
import com.example.timetable.model.Exam
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.utils.DbHelper
import com.example.timetable.utils.NotificationHelper
import com.example.timetable.utils.TimeUtils
import com.example.timetable.utils.WidgetUtils
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExamViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    private val notificationHelper = NotificationHelper(application)
    var exams = mutableStateListOf<Exam>()
        private set
    var subjects = mutableStateListOf<String>()
        private set
    var teachers = mutableStateListOf<String>()
        private set

    init {
        loadExams()
        loadSuggestions()
    }

    fun loadExams() {
        exams.clear()
        exams.addAll(db.getExam())
    }

    fun loadSuggestions() {
        subjects.clear()
        subjects.addAll(db.getSubjectsList())
        teachers.clear()
        teachers.addAll(db.getTeachersList())
    }

    fun getSubjectDetails(name: String): com.example.timetable.model.Week? {
        return db.getSubjectDetails(name)
    }

    fun deleteExam(exam: Exam) {
        db.deleteExamById(exam)
        loadExams()
        notificationHelper.scheduleEventsForToday()
        viewModelScope.launch { WidgetUtils.refreshAllWidgets(getApplication()) }
    }

    fun insertExam(exam: Exam) {
        db.insertExam(exam)
        loadExams()
        loadSuggestions()
        notificationHelper.scheduleEventsForToday()
        viewModelScope.launch { WidgetUtils.refreshAllWidgets(getApplication()) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(onBack: () -> Unit, viewModel: ExamViewModel = viewModel()) {
    var showAddDialog by remember { mutableStateOf(false) }
    var examToDelete by remember { mutableStateOf<Exam?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Completed")

    val currentDateTime = remember { Date() }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadExams()
                viewModel.loadSuggestions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val filteredExams by remember(selectedTab) {
        derivedStateOf {
            viewModel.exams.filter { exam ->
                val examDateTimeStr = "${exam.date} ${exam.time}"
                val examDate = try { sdf.parse(examDateTimeStr) } catch (e: Exception) { null }
                val isPast = examDate?.before(currentDateTime) ?: false
                if (selectedTab == 0) !isPast else isPast
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.exams)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
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
        if (filteredExams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (selectedTab == 0) Icons.AutoMirrored.Filled.Assignment else Icons.Default.EventAvailable,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (selectedTab == 0) "No upcoming exams!" else "No completed exams yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedTab == 0) {
                        Text(
                            "Tap + to add an exam.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(filteredExams) { exam ->
                    ExamItem(exam = exam, onDelete = { examToDelete = exam })
                }
            }
        }
    }

    if (showAddDialog) {
        AddExamDialog(
            onDismiss = { showAddDialog = false },
            onSave = { exam -> viewModel.insertExam(exam) },
            onGetSubjectDetails = { viewModel.getSubjectDetails(it) },
            subjectSuggestions = viewModel.subjects,
            teacherSuggestions = viewModel.teachers
        )
    }

    examToDelete?.let { exam ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("Delete Exam") },
            text = { Text("Are you sure you want to delete the '${exam.subject}' exam?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExam(exam)
                    examToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { examToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamDialog(
    onDismiss: () -> Unit, 
    onSave: (Exam) -> Unit,
    onGetSubjectDetails: (String) -> com.example.timetable.model.Week?,
    subjectSuggestions: List<String>,
    teacherSuggestions: List<String>
) {
    var subject by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(0) }

    var subjectExpanded by remember { mutableStateOf(false) }
    var teacherExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val updateFromSubjectDetails = { name: String ->
        onGetSubjectDetails(name)?.let { details ->
            teacher = details.teacher ?: teacher
            room = details.room ?: room
            color = details.color
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_exam)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it }
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { 
                            subject = it
                            subjectExpanded = it.isNotEmpty()
                        },
                        label = { Text(stringResource(R.string.subject)) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) }
                    )
                    val filtered = subjectSuggestions.filter { it.contains(subject, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                            filtered.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = {
                                    subject = s
                                    subjectExpanded = false
                                    updateFromSubjectDetails(s)
                                })
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = it }
                ) {
                    OutlinedTextField(
                        value = teacher,
                        onValueChange = { 
                            teacher = it
                            teacherExpanded = it.isNotEmpty()
                        },
                        label = { Text(stringResource(R.string.teacher)) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) }
                    )
                    val filtered = teacherSuggestions.filter { it.contains(teacher, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        ExposedDropdownMenu(expanded = teacherExpanded, onDismissRequest = { teacherExpanded = false }) {
                            filtered.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = {
                                    teacher = s
                                    teacherExpanded = false
                                })
                            }
                        }
                    }
                }

                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text(stringResource(R.string.room)) })
                
                Button(onClick = {
                    val c = Calendar.getInstance()
                    DatePickerDialog(context, { _, y, m, d ->
                        date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    Text(if (date.isEmpty()) stringResource(R.string.select_exam_date) else date)
                }

                Button(onClick = {
                    val parsedTime = TimeUtils.parse24Hour(time)
                    val hour = parsedTime?.first ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val minute = parsedTime?.second ?: Calendar.getInstance().get(Calendar.MINUTE)
                    TimePickerDialog(context, { _, h, m ->
                        time = TimeUtils.get24HourString(h, m)
                    }, hour, minute, false).show()
                }) {
                    Text(if (time.isEmpty()) stringResource(R.string.select_time) else TimeUtils.formatTo12Hour(time))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (subject.isNotBlank()) {
                    onSave(Exam().apply {
                        this.subject = subject
                        this.teacher = teacher
                        this.room = room
                        this.date = date
                        this.time = time
                        this.color = color
                    })
                    onDismiss()
                }
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun ExamItem(exam: Exam, onDelete: () -> Unit) {
    val examColor = if (exam.color != 0) Color(exam.color) else MaterialTheme.colorScheme.primary
    val containerColor = themedContainerColor(examColor)
    val contentColor = contentColorFor(containerColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exam.subject, style = MaterialTheme.typography.titleLarge)
                val dateTimeText = if (exam.time.isNullOrBlank()) exam.date else "${exam.date} at ${TimeUtils.formatTo12Hour(exam.time)}"
                Text(text = dateTimeText, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Room: ${exam.room}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Teacher: ${exam.teacher}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
