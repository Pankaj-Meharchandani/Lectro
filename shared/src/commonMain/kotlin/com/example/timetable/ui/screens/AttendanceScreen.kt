package com.example.timetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.timetable.data.AttendanceRecord
import com.example.timetable.model.Subject
import com.example.timetable.ui.theme.getAttendanceColor
import com.example.timetable.ui.viewmodel.MainViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.datetime.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel,
    settings: Settings = Settings()
) {
    var attendanceEnabled: Boolean by remember { mutableStateOf(settings.get("attendance_setting", true)) }
    var minAttendance: Int by remember { mutableIntStateOf(settings.get("min_attendance_setting", 75)) }
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Enable Attendance Tracking", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = attendanceEnabled,
                        onCheckedChange = {
                            attendanceEnabled = it
                            settings["attendance_setting"] = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Minimum Attendance Goal: $minAttendance%", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = minAttendance.toFloat(),
                    onValueChange = {
                        val newValue = (it / 5f).roundToInt() * 5
                        minAttendance = newValue
                        settings["min_attendance_setting"] = minAttendance
                    },
                    valueRange = 0f..100f,
                    steps = 19
                )
            }

            HorizontalDivider()

            if (subjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("No subjects found.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(subjects) { subject ->
                        AttendanceSubjectItem(subject, minAttendance) { selectedSubject = it }
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

    val statusText = remember(subject.attended, subject.missed, goal) {
        val p = subject.attended.toDouble()
        val t = (subject.attended + subject.missed).toDouble()
        val g = goal.toDouble()

        if (t == 0.0) {
            "Attend your first class to see stats!"
        } else {
            val currentPercent = (p / t) * 100.0
            if (currentPercent >= g) {
                val canSkip = kotlin.math.floor((100.0 * p / g) - t).toInt()
                if (canSkip > 0) "Safe! You can skip $canSkip more ${if (canSkip == 1) "class" else "classes"}."
                else "On the edge! Don't miss the next class."
            } else {
                if (g < 100.0) {
                    val mustAttend = kotlin.math.ceil((g * t - 100.0 * p) / (100.0 - g)).toInt()
                    "Attend $mustAttend more classes consecutively to reach $goal%."
                } else "Goal is 100%! Attend all remaining classes."
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { onClick(subject) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = subject.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "$percentage%", style = MaterialTheme.typography.titleLarge, color = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total > 0) subject.attended.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = statusText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Present: ${subject.attended}", style = MaterialTheme.typography.bodySmall)
                Text("Absent: ${subject.missed}", style = MaterialTheme.typography.bodySmall)
                Text("Cancelled: ${subject.skipped}", style = MaterialTheme.typography.bodySmall)
            }
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
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val records = remember { mutableStateListOf<AttendanceRecord>() }
    
    LaunchedEffect(subject.name, refreshTrigger) {
        records.clear()
        records.addAll(viewModel.getAttendanceForSubject(subject.name))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var showTypePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attendance History: ${subject.name}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                Text("Records List", style = MaterialTheme.typography.titleSmall)
                
                if (records.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No records found.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(records.sortedByDescending { it.date }, key = { it.date + it.weekId }) { record ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(record.date, style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusIcon(record.status)
                                    IconButton(onClick = {
                                        viewModel.deleteAttendanceRecord(record.weekId, subject.name, record.date)
                                        refreshTrigger++
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Older Record")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        selectedDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date.toString()
                        showTypePicker = true
                    }
                    showDatePicker = false
                }) { Text("Next") }
            }
        ) { DatePicker(state = state) }
    }

    if (showTypePicker) {
        AlertDialog(
            onDismissRequest = { showTypePicker = false },
            title = { Text("Status for $selectedDate") },
            text = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    HistoricalTypeButton("Present", Color(0xFF4CAF50)) {
                        val slots = viewModel.getSubjectDetails(subject.name)
                        viewModel.updateAttendanceByDate(slots?.id ?: -1, subject.name, "attended", selectedDate)
                        refreshTrigger++; showTypePicker = false
                    }
                    HistoricalTypeButton("Absent", Color(0xFFF44336)) {
                        val slots = viewModel.getSubjectDetails(subject.name)
                        viewModel.updateAttendanceByDate(slots?.id ?: -1, subject.name, "missed", selectedDate)
                        refreshTrigger++; showTypePicker = false
                    }
                    HistoricalTypeButton("Cancelled", Color.Gray) {
                        val slots = viewModel.getSubjectDetails(subject.name)
                        viewModel.updateAttendanceByDate(slots?.id ?: -1, subject.name, "skipped", selectedDate)
                        refreshTrigger++; showTypePicker = false
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun StatusIcon(status: String) {
    val (icon, color, label) = when (status) {
        "attended" -> Triple(Icons.Default.Check, Color(0xFF4CAF50), "Present")
        "missed" -> Triple(Icons.Default.Close, Color(0xFFF44336), "Absent")
        else -> Triple(Icons.Default.Block, Color.Gray, "Cancelled")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Composable
fun HistoricalTypeButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.padding(2.dp)
    ) { Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White) }
}
