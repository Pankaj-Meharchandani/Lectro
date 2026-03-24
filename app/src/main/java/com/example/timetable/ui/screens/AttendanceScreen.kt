package com.example.timetable.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.example.timetable.activities.SettingsActivity
import com.example.timetable.model.Subject
import com.example.timetable.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPref = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    var attendanceEnabled by remember {
        mutableStateOf(sharedPref.getBoolean(SettingsActivity.KEY_ATTENDANCE_SETTING, true))
    }
    var minAttendance by remember {
        mutableIntStateOf(sharedPref.getInt(SettingsActivity.KEY_MIN_ATTENDANCE_SETTING, 75))
    }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSuggestions()
    }

    val subjects = viewModel.allSubjects

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Attendance Tracking", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = attendanceEnabled,
                        onCheckedChange = {
                            attendanceEnabled = it
                            sharedPref.edit().putBoolean(SettingsActivity.KEY_ATTENDANCE_SETTING, it).apply()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Minimum Attendance Goal: $minAttendance%", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = minAttendance.toFloat(),
                    onValueChange = {
                        minAttendance = it.toInt()
                        sharedPref.edit().putInt(SettingsActivity.KEY_MIN_ATTENDANCE_SETTING, minAttendance).apply()
                    },
                    valueRange = 0f..100f,
                    steps = 19
                )
            }

            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(subjects) { subject ->
                    AttendanceSubjectItem(subject, minAttendance) {
                        selectedSubject = it
                    }
                }
            }
        }
    }

    selectedSubject?.let { subject ->
        HistoricalAttendanceDialog(
            subject = subject,
            viewModel = viewModel,
            onDismiss = { selectedSubject = null }
        )
    }
}

@Composable
fun AttendanceSubjectItem(subject: Subject, goal: Int, onClick: (Subject) -> Unit) {
    val total = subject.attended + subject.missed
    val percentage = if (total > 0) (subject.attended.toFloat() / total * 100).toInt() else 0
    val color = getAttendanceColor(percentage, goal)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { onClick(subject) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = subject.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleLarge,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { if (total > 0) subject.attended.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Present: ${subject.attended}")
                Text("Absent: ${subject.missed}")
                Text("No Class: ${subject.skipped}")
            }
            Text(
                text = "Total classes: ${total + subject.skipped}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricalAttendanceDialog(
    subject: Subject,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val records = remember(subject.name) { viewModel.getAttendanceForSubject(subject.name ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var showTypePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Historical Attendance: ${subject.name}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (records.isEmpty()) {
                    Text("No past records found.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(records.sortedByDescending { it.date }) { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(record.date, style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (icon, color, label) = when (record.status) {
                                        "attended" -> Triple(Icons.Default.Check, Color(0xFF4CAF50), "Present")
                                        "missed" -> Triple(Icons.Default.Close, Color(0xFFF44336), "Absent")
                                        else -> Triple(Icons.Default.Block, Color.Gray, "No Class")
                                    }
                                    Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(label, style = MaterialTheme.typography.bodySmall, color = color)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Older Record")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                        selectedDate = date
                        showTypePicker = true
                    }
                    showDatePicker = false
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTypePicker) {
        AlertDialog(
            onDismissRequest = { showTypePicker = false },
            title = { Text("Select Status for $selectedDate") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        HistoricalTypeButton("Present", Color(0xFF4CAF50)) {
                            markAttendance(viewModel, subject, selectedDate, "attended")
                            showTypePicker = false
                            onDismiss() // Refresh
                        }
                        HistoricalTypeButton("Absent", Color(0xFFF44336)) {
                            markAttendance(viewModel, subject, selectedDate, "missed")
                            showTypePicker = false
                            onDismiss()
                        }
                        HistoricalTypeButton("No Class", Color.Gray) {
                            markAttendance(viewModel, subject, selectedDate, "skipped")
                            showTypePicker = false
                            onDismiss()
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun HistoricalTypeButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

private fun markAttendance(viewModel: MainViewModel, subject: Subject, date: String, type: String) {
    val slots = viewModel.getSubjectDetails(subject.name ?: "") 
    val weekId = slots?.id ?: -1
    if (weekId != -1) {
        viewModel.updateAttendanceByDate(weekId, subject.name ?: "", type, date)
    }
}

fun getAttendanceColor(percentage: Int, goal: Int): Color {
    return when {
        percentage < goal -> Color(0xFFF44336) // Red
        percentage <= goal + 5 -> Color(0xFFFFC107) // Amber/Yellow
        else -> Color(0xFF4CAF50) // Green
    }
}
