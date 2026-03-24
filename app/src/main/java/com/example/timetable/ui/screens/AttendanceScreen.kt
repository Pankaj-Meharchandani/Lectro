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
                    AttendanceSubjectItem(subject, minAttendance)
                }
            }
        }
    }
}

@Composable
fun AttendanceSubjectItem(subject: Subject, goal: Int) {
    val total = subject.attended + subject.missed
    val percentage = if (total > 0) (subject.attended.toFloat() / total * 100).toInt() else 0
    val color = getAttendanceColor(percentage, goal)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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

fun getAttendanceColor(percentage: Int, goal: Int): Color {
    return when {
        percentage < goal -> Color(0xFFF44336) // Red
        percentage <= goal + 5 -> Color(0xFFFFC107) // Amber/Yellow
        else -> Color(0xFF4CAF50) // Green
    }
}
