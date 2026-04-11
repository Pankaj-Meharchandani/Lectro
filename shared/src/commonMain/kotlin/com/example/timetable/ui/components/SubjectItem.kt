package com.example.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.timetable.model.Week
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.ui.viewmodel.MainViewModel
import com.example.timetable.utils.TimeUtils
import kotlinx.datetime.*

@Composable
fun SubjectItem(
    subject: Week, 
    attendanceEnabled: Boolean,
    minAttendance: Int,
    onClick: () -> Unit,
    onMarkAttendance: (Int, String, String) -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    showRoom: Boolean = true,
    showTeacher: Boolean = true,
    viewModel: MainViewModel
) {
    val subjectColor = remember(subject.color) { 
        if (subject.color != 0) Color(subject.color) else Color.Gray 
    }
    val containerColor = themedContainerColor(subjectColor)
    val contentColor = contentColorFor(containerColor)
    var showMenu by remember { mutableStateOf(false) }

    val subjectDetails by remember(subject.subject) {
        derivedStateOf { viewModel.allSubjects.find { it.name == subject.subject } }
    }
    val attendanceStatus = if (attendanceEnabled) viewModel.todayAttendance[subject.id] else null

    val formattedTime = remember(subject.fromTime, subject.toTime) {
        if (subject.fromTime.isEmpty()) "" 
        else "${TimeUtils.formatTo12Hour(subject.fromTime)} - ${TimeUtils.formatTo12Hour(subject.toTime)}"
    }

    val isAfterStartTime = remember(subject.fromTime) {
        if (subject.fromTime.isEmpty()) false
        else {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val timeParts = subject.fromTime.split(":")
            if (timeParts.size >= 2) {
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                now.hour > hour || (now.hour == hour && now.minute >= minute)
            } else false
        }
    }

    val isToday = remember(subject.fragment) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDay = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        subject.fragment == currentDay
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(subjectColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = subject.subject,
                            style = MaterialTheme.typography.titleLarge,
                            color = contentColor
                        )
                        if (showTeacher && subject.teacher.isNotEmpty()) {
                            Text(
                                text = subject.teacher,
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("View Details") },
                            onClick = {
                                showMenu = false
                                onClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                    }
                }
            }
            if (subject.fromTime.isNotEmpty()) {
                Spacer(modifier = Modifier.height(if (showTeacher && subject.teacher.isNotEmpty()) 4.dp else 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }
            if (showRoom && subject.room.isNotEmpty()) {
                if (subject.fromTime.isEmpty()) Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Room,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subject.room,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }

            val details = subjectDetails
            if (attendanceEnabled && details != null) {
                val total = details.attended + details.missed
                val progress = if (total > 0) details.attended.toFloat() / total else 0f
                val percentage = (progress * 100).toInt()
                val color = if (percentage >= minAttendance) Color(0xFF4CAF50) else Color(0xFFF44336)

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = color,
                        trackColor = contentColor.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor
                    )
                }

                if (isToday && isAfterStartTime && attendanceStatus == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalButton(
                            onClick = { onMarkAttendance(subject.id, subject.subject, "attended") },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Present", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = { onMarkAttendance(subject.id, subject.subject, "missed") },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Absent", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = { onMarkAttendance(subject.id, subject.subject, "skipped") },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Cancelled", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
