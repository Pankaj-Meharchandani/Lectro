package com.example.timetable.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.timetable.model.Note
import com.example.timetable.model.Subject
import com.example.timetable.model.Week
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.components.EditSubjectDialog
import com.example.timetable.ui.components.SubjectItem
import com.example.timetable.ui.viewmodel.MainViewModel
import com.example.timetable.ui.viewmodel.NoteViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onBack: () -> Unit, 
    onSubjectClick: (Int) -> Unit,
    viewModel: NoteViewModel,
    mainViewModel: MainViewModel,
    settings: Settings = Settings()
) {
    val minAttendance: Int = settings.get("min_attendance_setting", 75)
    var showAddDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSubjects()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (viewModel.allSubjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Text("No subjects added yet.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                itemsIndexed(viewModel.allSubjects, key = { _, s -> s.id }) { index, subject ->
                    Box {
                        var showReorderMenu by remember { mutableStateOf(false) }
                        SubjectItem(
                            subject = Week(
                                id = subject.id,
                                subject = subject.name,
                                color = subject.color,
                                teacher = subject.teacher,
                                room = subject.room
                            ),
                            attendanceEnabled = false,
                            minAttendance = minAttendance,
                            onClick = { onSubjectClick(subject.id) },
                            onMarkAttendance = { _, _, _ -> },
                            onEdit = { subjectToEdit = subject },
                            onDelete = { subjectToDelete = subject },
                            showRoom = false,
                            viewModel = mainViewModel
                        )
                        IconButton(
                            onClick = { showReorderMenu = true },
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 60.dp)
                        ) { Icon(Icons.Default.SwapVert, contentDescription = "Reorder") }
                        DropdownMenu(expanded = showReorderMenu, onDismissRequest = { showReorderMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Move Up") },
                                onClick = { viewModel.moveSubject(index, true); showReorderMenu = false },
                                enabled = index > 0
                            )
                            DropdownMenuItem(
                                text = { Text("Move Down") },
                                onClick = { viewModel.moveSubject(index, false); showReorderMenu = false },
                                enabled = index < viewModel.allSubjects.size - 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onSave = { note, subjectName -> viewModel.insertNote(note, subjectName) },
            subjectSuggestions = viewModel.subjectNames
        )
    }

    subjectToEdit?.let { subject ->
        EditSubjectDialog(
            subject = subject,
            onDismiss = { subjectToEdit = null },
            onSave = { updated ->
                viewModel.updateSubject(updated)
                subjectToEdit = null
            }
        )
    }

    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text("Delete Subject") },
            text = { Text("Are you sure you want to delete '${subject.name}'? This will also remove all its schedule slots, notes and materials.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSubject(subject.id)
                    subjectToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onSave: (Note, String) -> Unit, subjectSuggestions: List<String>) {
    var title by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(0) }
    var subjectExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = it }) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it; subjectExpanded = it.isNotEmpty() },
                        label = { Text("Subject (creates if new)") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) }
                    )
                    val filtered = subjectSuggestions.filter { it.contains(subjectName, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                            filtered.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = {
                                    subjectName = s
                                    subjectExpanded = false
                                })
                            }
                        }
                    }
                }

                Text("Note Color")
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(Note(title = title, color = color), subjectName)
                    onDismiss()
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
