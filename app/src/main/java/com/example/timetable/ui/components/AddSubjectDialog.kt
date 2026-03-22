package com.example.timetable.ui.components

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.timetable.R
import com.example.timetable.model.Week
import com.example.timetable.utils.TimeUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onSave: (Week) -> Unit,
    onGetSubjectDetails: (String) -> Week?,
    initialWeek: Week = Week(),
    subjectSuggestions: List<String> = emptyList(),
    teacherSuggestions: List<String> = emptyList()
) {
    var subject by remember { mutableStateOf(initialWeek.subject ?: "") }
    var teacher by remember { mutableStateOf(initialWeek.teacher ?: "") }
    var room by remember { mutableStateOf(initialWeek.room ?: "") }
    var fromTime by remember { mutableStateOf(initialWeek.fromTime ?: "") }
    var toTime by remember { mutableStateOf(initialWeek.toTime ?: "") }
    var color by remember { mutableIntStateOf(if (initialWeek.color != 0) initialWeek.color else Color.Gray.toArgb()) }

    var subjectExpanded by remember { mutableStateOf(false) }
    var teacherExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val updateFromSubjectDetails = { name: String ->
        onGetSubjectDetails(name)?.let { details ->
            teacher = details.teacher ?: teacher
            room = details.room ?: room
            if (details.color != 0) color = details.color
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (initialWeek.id == 0) stringResource(R.string.add_subject) else stringResource(R.string.edit_subject)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subject Autocomplete
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        singleLine = true
                    )
                    val filteredSubjects = subjectSuggestions.filter { it.contains(subject, ignoreCase = true) }
                    if (filteredSubjects.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            filteredSubjects.forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection) },
                                    onClick = {
                                        subject = selection
                                        subjectExpanded = false
                                        updateFromSubjectDetails(selection)
                                    }
                                )
                            }
                        }
                    }
                }

                // Teacher Autocomplete
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        singleLine = true
                    )
                    val filteredTeachers = teacherSuggestions.filter { it.contains(teacher, ignoreCase = true) }
                    if (filteredTeachers.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = teacherExpanded,
                            onDismissRequest = { teacherExpanded = false }
                        ) {
                            filteredTeachers.forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection) },
                                    onClick = {
                                        teacher = selection
                                        teacherExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text(stringResource(R.string.room)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val c = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, m ->
                                fromTime = TimeUtils.get24HourString(h, m)
                            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(if (fromTime.isEmpty()) "Start Time" else TimeUtils.formatTo12Hour(fromTime))
                    }
                    Button(
                        onClick = {
                            val c = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, m ->
                                toTime = TimeUtils.get24HourString(h, m)
                            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(if (toTime.isEmpty()) "End Time" else TimeUtils.formatTo12Hour(toTime))
                    }
                }

                Text(stringResource(R.string.select_color))
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val errorMessage = when {
                    subject.isBlank() -> "Please enter Subject"
                    fromTime.isBlank() -> "Please select Start Time"
                    toTime.isBlank() -> "Please select End Time"
                    else -> null
                }

                if (errorMessage == null) {
                    onSave(Week().apply {
                        this.id = initialWeek.id
                        this.subject = subject
                        this.teacher = teacher
                        this.room = room
                        this.setFromTime(fromTime)
                        this.setToTime(toTime)
                        this.color = color
                        this.setFragment(initialWeek.getFragment())
                    })
                    onDismiss()
                } else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ColorPickerRow(selectedColor: Int, onColorSelected: (Int) -> Unit) {
    val colors = listOf(
        Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
        Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF26A69A), Color(0xFF66BB6A),
        Color(0xFFD4E157), Color(0xFFFFEE58), Color(0xFFFFCA28), Color(0xFFFFA726)
    )
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            colors.take(6).forEach { c ->
                ColorCircle(color = c, isSelected = selectedColor == c.toArgb()) {
                    onColorSelected(c.toArgb())
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            colors.drop(6).forEach { c ->
                ColorCircle(color = c, isSelected = selectedColor == c.toArgb()) {
                    onColorSelected(c.toArgb())
                }
            }
        }
    }
}

@Composable
fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
            )
    )
}
