package com.example.timetable.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.example.timetable.model.Subject
import com.example.timetable.ui.viewmodel.MainViewModel
import com.example.timetable.utils.AppConstants
import com.example.timetable.utils.DbHelper
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPref = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    var attendanceEnabled by remember {
        mutableStateOf(sharedPref.getBoolean(AppConstants.KEY_ATTENDANCE_SETTING, true))
    }
    var minAttendance by remember {
        mutableIntStateOf(sharedPref.getInt(AppConstants.KEY_MIN_ATTENDANCE_SETTING, 75))
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
                            sharedPref.edit().putBoolean(AppConstants.KEY_ATTENDANCE_SETTING, it).apply()
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
                        sharedPref.edit().putInt(AppConstants.KEY_MIN_ATTENDANCE_SETTING, minAttendance).apply()
                    },
                    valueRange = 0f..100f,
                    steps = 19
                )
            }

            HorizontalDivider()

            if (subjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.FactCheck,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No subjects found.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Add subjects in the Timetable or Notes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(subjects) { subject ->
                        AttendanceSubjectItem(subject, minAttendance) {
                            selectedSubject = it
                        }
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

    // Advanced Attendance Logic
    val statusText = remember(subject.attended, subject.missed, goal) {
        val p = subject.attended.toDouble()
        val t = (subject.attended + subject.missed).toDouble()
        val g = goal.toDouble()

        if (t == 0.0) {
            "Attend your first class to see stats!"
        } else {
            val currentPercent = (p / t) * 100.0
            if (currentPercent >= g) {
                // How many can they skip?
                // (Present) / (Total + X) >= Goal / 100
                // 100 * Present / Goal >= Total + X
                // X <= (100 * Present / Goal) - Total
                val canSkip = kotlin.math.floor((100.0 * p / g) - t).toInt()
                if (canSkip > 0) {
                    "Safe! You can skip $canSkip more ${if (canSkip == 1) "class" else "classes"}."
                } else {
                    "On the edge! Don't miss the next class."
                }
            } else {
                // How many must they attend?
                // (Present + Y) / (Total + Y) >= Goal / 100
                // 100 * (Present + Y) >= Goal * (Total + Y)
                // 100P + 100Y >= Goal*Total + Goal*Y
                // Y * (100 - Goal) >= Goal*Total - 100P
                // Y >= (Goal*Total - 100P) / (100 - Goal)
                if (g < 100.0) {
                    val mustAttend = kotlin.math.ceil((g * t - 100.0 * p) / (100.0 - g)).toInt()
                    "Attend $mustAttend more classes consecutively to reach $goal%."
                } else {
                    "Goal is 100%! Attend all remaining classes."
                }
            }
        }
    }

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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Present: ${subject.attended}", style = MaterialTheme.typography.bodySmall)
                Text("Absent: ${subject.missed}", style = MaterialTheme.typography.bodySmall)
                Text("Cancelled: ${subject.skipped}", style = MaterialTheme.typography.bodySmall)
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
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val records = remember { mutableStateListOf<DbHelper.AttendanceRecord>() }
    
    LaunchedEffect(subject.name, refreshTrigger) {
        records.clear()
        records.addAll(viewModel.getAttendanceForSubject(subject.name ?: ""))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var showTypePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attendance History: ${subject.name}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                AttendanceCalendar(records)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Recent Activity", style = MaterialTheme.typography.titleSmall)
                
                if (records.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No records found.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(records.sortedByDescending { it.date }, key = { it.date + it.weekId }) { record ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(record.date, style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusIcon(record.status)
                                    IconButton(onClick = {
                                        viewModel.deleteAttendanceRecord(record.weekId, subject.name ?: "", record.date)
                                        refreshTrigger++
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        selectedDate = sdf.format(Date(millis))
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
            title = { Text("Status for $selectedDate") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HistoricalTypeButton("Present", Color(0xFF4CAF50)) {
                        markAttendance(viewModel, subject, selectedDate, "attended")
                        refreshTrigger++
                        showTypePicker = false
                    }
                    HistoricalTypeButton("Absent", Color(0xFFF44336)) {
                        markAttendance(viewModel, subject, selectedDate, "missed")
                        refreshTrigger++
                        showTypePicker = false
                    }
                    HistoricalTypeButton("Cancelled", Color.Gray) {
                        markAttendance(viewModel, subject, selectedDate, "skipped")
                        refreshTrigger++
                        showTypePicker = false
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun AttendanceCalendar(records: List<DbHelper.AttendanceRecord>) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    val monthStartDay = remember(currentMonth, currentYear) {
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth, 1)
        cal.get(Calendar.DAY_OF_WEEK)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newCal = calendar.clone() as Calendar
                newCal.add(Calendar.MONTH, -1)
                calendar = newCal
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }
            
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = {
                val newCal = calendar.clone() as Calendar
                newCal.add(Calendar.MONTH, 1)
                calendar = newCal
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        val totalCells = ((daysInMonth + monthStartDay - 1 + 6) / 7) * 7
        for (row in 0 until totalCells / 7) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 1..7) {
                    val dayNum = row * 7 + col - monthStartDay + 1
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth + 1, dayNum)
                            val record = records.find { it.date == dateStr }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = dayNum.toString(), style = MaterialTheme.typography.bodySmall)
                                if (record != null) {
                                    StatusDot(record.status)
                                }
                            }
                        }
                    }
                }
            }
        }
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
fun StatusDot(status: String) {
    val (icon, color) = when (status) {
        "attended" -> Icons.Default.Check to Color(0xFF4CAF50)
        "missed" -> Icons.Default.Close to Color(0xFFF44336)
        else -> Icons.Default.Block to Color.Gray
    }
    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
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
    viewModel.updateAttendanceByDate(weekId, subject.name ?: "", type, date)
}

fun getAttendanceColor(percentage: Int, goal: Int): Color {
    return when {
        percentage < goal -> Color(0xFFF44336) // Red
        percentage <= goal + 5 -> Color(0xFFFFC107) // Amber/Yellow
        else -> Color(0xFF4CAF50) // Green
    }
}
