package com.example.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.model.Week
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.ui.viewmodel.MainViewModel
import com.example.timetable.utils.TimeUtils
import java.util.Calendar
import com.example.timetable.ui.screens.getAttendanceColor

@Composable
fun SubjectItem(
    subject: Week, 
    attendanceEnabled: Boolean,
    minAttendance: Int,
    onClick: () -> Unit,
    onMarkAttendance: (Int, String, String) -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val subjectColor = if (subject.color != 0) Color(subject.color) else MaterialTheme.colorScheme.primary
    val containerColor = themedContainerColor(subjectColor)
    val contentColor = contentColorFor(containerColor)
    var showMenu by remember { mutableStateOf(false) }

    val subjectDetails = viewModel.allSubjects.find { it.name == subject.subject }
    val attendanceStatus = if (attendanceEnabled) viewModel.todayAttendance[subject.id] else null

    val isAfterStartTime = remember(subject.fromTime) {
        if (subject.fromTime.isNullOrBlank()) false
        else {
            val now = Calendar.getInstance()
            val timeParts = subject.fromTime.split(":")
            if (timeParts.size == 2) {
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    set(Calendar.MINUTE, timeParts[1].toInt())
                    set(Calendar.SECOND, 0)
                }
                now.after(target)
            } else false
        }
    }

    val isToday = remember(subject.fragment) {
        val currentDay = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> ""
        }
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
                    Text(
                        text = subject.subject ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor
                    )
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
                            text = { Text("Edit Slot") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Slot") },
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
            if (!subject.fromTime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${TimeUtils.formatTo12Hour(subject.fromTime)} - ${TimeUtils.formatTo12Hour(subject.toTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }
            if (!subject.room.isNullOrBlank()) {
                if (subject.fromTime.isNullOrBlank()) Spacer(modifier = Modifier.height(8.dp))
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

            if (attendanceEnabled && subjectDetails != null) {
                val total = subjectDetails.attended + subjectDetails.missed
                val progress = if (total > 0) subjectDetails.attended.toFloat() / total else 0f
                val percentage = (progress * 100).toInt()
                val color = getAttendanceColor(percentage, minAttendance)

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
                            onClick = { onMarkAttendance(subject.id, subject.subject ?: "", "attended") },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Present", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = { onMarkAttendance(subject.id, subject.subject ?: "", "missed") },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Absent", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = { onMarkAttendance(subject.id, subject.subject ?: "", "skipped") },
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
