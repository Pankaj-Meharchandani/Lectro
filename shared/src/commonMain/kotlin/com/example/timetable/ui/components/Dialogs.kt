package com.example.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.timetable.model.Subject
import com.example.timetable.model.Week
import com.example.timetable.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onSave: (Week) -> Unit,
    onGetSubjectDetails: (String) -> Week?,
    initialWeek: Week = Week(),
    subjectSuggestions: List<String> = emptyList(),
    teacherSuggestions: List<String> = emptyList(),
    existingSlots: List<Week> = emptyList()
) {
    var subject by remember { mutableStateOf(initialWeek.subject) }
    var teacher by remember { mutableStateOf(initialWeek.teacher) }
    var room by remember { mutableStateOf(initialWeek.room) }
    var fromTime by remember { mutableStateOf(initialWeek.fromTime) }
    var toTime by remember { mutableStateOf(initialWeek.toTime) }
    var color by remember { mutableIntStateOf(if (initialWeek.color != 0) initialWeek.color else -7829368) } // Gray

    var subjectExpanded by remember { mutableStateOf(false) }
    var teacherExpanded by remember { mutableStateOf(false) }

    var showFromTimePicker by remember { mutableStateOf(false) }
    var showToTimePicker by remember { mutableStateOf(false) }

    val updateFromSubjectDetails = { name: String ->
        onGetSubjectDetails(name)?.let { details ->
            teacher = if (details.teacher.isNotEmpty()) details.teacher else teacher
            room = if (details.room.isNotEmpty()) details.room else room
            if (details.color != 0) color = details.color
        }
    }

    if (showFromTimePicker) {
        val state = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showFromTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fromTime = "${state.hour.toString().padStart(2, '0')}:${state.minute.toString().padStart(2, '0')}"
                    showFromTimePicker = false
                }) { Text("OK") }
            },
            title = { Text("Select Start Time") },
            text = { TimePicker(state = state) }
        )
    }

    if (showToTimePicker) {
        val state = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showToTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    toTime = "${state.hour.toString().padStart(2, '0')}:${state.minute.toString().padStart(2, '0')}"
                    showToTimePicker = false
                }) { Text("OK") }
            },
            title = { Text("Select End Time") },
            text = { TimePicker(state = state) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (initialWeek.id == 0) "Add Subject" else "Edit Subject") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = it }) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it; subjectExpanded = it.isNotEmpty() },
                        label = { Text("Subject") },
                        modifier = Modifier.menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        singleLine = true
                    )
                    val filtered = subjectSuggestions.filter { it.contains(subject, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                            filtered.forEach { selection ->
                                DropdownMenuItem(text = { Text(selection) }, onClick = {
                                    subject = selection
                                    subjectExpanded = false
                                    updateFromSubjectDetails(selection)
                                })
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = teacherExpanded, onExpandedChange = { teacherExpanded = it }) {
                    OutlinedTextField(
                        value = teacher,
                        onValueChange = { teacher = it; teacherExpanded = it.isNotEmpty() },
                        label = { Text("Teacher") },
                        modifier = Modifier.menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                        singleLine = true
                    )
                    val filtered = teacherSuggestions.filter { it.contains(teacher, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        ExposedDropdownMenu(expanded = teacherExpanded, onDismissRequest = { teacherExpanded = false }) {
                            filtered.forEach { selection ->
                                DropdownMenuItem(text = { Text(selection) }, onClick = {
                                    teacher = selection
                                    teacherExpanded = false
                                })
                            }
                        }
                    }
                }

                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showFromTimePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(if (fromTime.isEmpty()) "Start Time" else TimeUtils.formatTo12Hour(fromTime))
                    }
                    Button(onClick = { showToTimePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(if (toTime.isEmpty()) "End Time" else TimeUtils.formatTo12Hour(toTime))
                    }
                }

                Text("Select Color")
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (subject.isNotBlank() && fromTime.isNotBlank() && toTime.isNotBlank()) {
                    onSave(initialWeek.copy(
                        subject = subject,
                        teacher = teacher,
                        room = room,
                        fromTime = fromTime,
                        toTime = toTime,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubjectDialog(subject: Subject, onDismiss: () -> Unit, onSave: (Subject) -> Unit) {
    var name by remember { mutableStateOf(subject.name) }
    var teacher by remember { mutableStateOf(subject.teacher) }
    var room by remember { mutableStateOf(subject.room) }
    var color by remember { mutableIntStateOf(subject.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Subject") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Subject Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Teacher") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") }, modifier = Modifier.fillMaxWidth())
                Text("Color")
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(subject.copy(name = name, teacher = teacher, room = room, color = color))
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
fun ColorPickerRow(selectedColor: Int, onColorSelected: (Int) -> Unit) {
    val colors = listOf(
        0xFFEF5350.toInt(), 0xFFEC407A.toInt(), 0xFFAB47BC.toInt(), 0xFF7E57C2.toInt(),
        0xFF5C6BC0.toInt(), 0xFF42A5F5.toInt(), 0xFF26A69A.toInt(), 0xFF66BB6A.toInt(),
        0xFFD4E157.toInt(), 0xFFFFEE58.toInt(), 0xFFFFCA28.toInt(), 0xFFFFA726.toInt()
    )
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            colors.take(6).forEach { c ->
                ColorCircle(color = Color(c), isSelected = selectedColor == c) { onColorSelected(c) }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            colors.drop(6).forEach { c ->
                ColorCircle(color = Color(c), isSelected = selectedColor == c) { onColorSelected(c) }
            }
        }
    }
}

@Composable
fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).background(color).clickable(onClick = onClick)
            .border(width = if (isSelected) 2.dp else 0.dp, color = if (isSelected) Color.White else Color.Transparent, shape = CircleShape)
    )
}
