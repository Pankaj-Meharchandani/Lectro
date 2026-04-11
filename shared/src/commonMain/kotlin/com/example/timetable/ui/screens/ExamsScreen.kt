package com.example.timetable.ui.screens

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
import androidx.compose.ui.unit.dp
import com.example.timetable.model.Exam
import com.example.timetable.model.Week
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.ui.viewmodel.ExamViewModel
import com.example.timetable.utils.TimeUtils
import com.example.timetable.shared.getPlatform
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(onBack: () -> Unit, viewModel: ExamViewModel) {
    val platform = remember { getPlatform() }
    var showAddDialog by remember { mutableStateOf(false) }
    var examToDelete by remember { mutableStateOf<Exam?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Completed")

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    LaunchedEffect(Unit) {
        viewModel.loadExams()
        viewModel.loadSuggestions()
    }

    val filteredExams by remember(selectedTab, viewModel.exams) {
        derivedStateOf {
            viewModel.exams.filter { exam ->
                val isPast = try {
                    val examDate = LocalDate.parse(exam.date)
                    val examTime = if (exam.time.isNotEmpty()) LocalTime.parse(exam.time) else LocalTime(0, 0)
                    val examDateTime = LocalDateTime(examDate, examTime)
                    examDateTime < now
                } catch (e: Exception) { false }
                
                if (selectedTab == 0) !isPast else isPast
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Exams") },
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
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.AutoMirrored.Filled.Assignment else Icons.Default.EventAvailable,
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
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(filteredExams) { exam ->
                    ExamItem(exam = exam, onDelete = { examToDelete = exam })
                }
            }
        }
    }

    if (showAddDialog) {
        AddExamDialog(
            onDismiss = { showAddDialog = false },
            onSave = { exam -> 
                viewModel.insertExam(exam)
                platform.showToast("Exam added")
            },
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
                    platform.showToast("Exam deleted")
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
    onGetSubjectDetails: (String) -> Week?,
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val updateFromSubjectDetails = { name: String ->
        onGetSubjectDetails(name)?.let { details ->
            teacher = details.teacher
            room = details.room
            color = details.color
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val localDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                        date = localDate.toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                    showTimePicker = false
                }) { Text("OK") }
            },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exam") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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
                        label = { Text("Subject") },
                        modifier = Modifier.menuAnchor(),
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
                        label = { Text("Teacher") },
                        modifier = Modifier.menuAnchor(),
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

                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") })
                
                Button(onClick = { showDatePicker = true }) {
                    Text(if (date.isEmpty()) "Select Exam Date" else date)
                }

                Button(onClick = { showTimePicker = true }) {
                    Text(if (time.isEmpty()) "Select Time" else TimeUtils.formatTo12Hour(time))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (subject.isNotBlank()) {
                    onSave(Exam(
                        subject = subject,
                        teacher = teacher,
                        room = room,
                        date = date,
                        time = time,
                        color = color
                    ))
                    onDismiss()
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exam.subject, style = MaterialTheme.typography.titleLarge)
                val dateTimeText = if (exam.time.isEmpty()) exam.date else "${exam.date} at ${TimeUtils.formatTo12Hour(exam.time)}"
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
