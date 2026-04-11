package com.example.timetable.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timetable.model.Material
import com.example.timetable.model.Note
import com.example.timetable.model.Subject
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.components.EditSubjectDialog
import com.example.timetable.ui.components.NoteItem
import com.example.timetable.ui.theme.getAttendanceColor
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.ui.viewmodel.SubjectDetailViewModel
import com.example.timetable.shared.getPlatform
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: Int, 
    onBack: () -> Unit, 
    onNoteClick: (Int) -> Unit,
    viewModel: SubjectDetailViewModel,
    settings: Settings = Settings(),
    onPickFile: (callback: (String, String, String) -> Unit) -> Unit = {},
    onOpenFile: (String, String) -> Unit = { _, _ -> }
) {
    val platform = remember { getPlatform() }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var materialToEdit by remember { mutableStateOf<Material?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var materialToDelete by remember { mutableStateOf<Material?>(null) }
    var showEditSubjectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(subjectId) {
        viewModel.loadSubjectData(subjectId)
    }

    val subject = viewModel.subject
    val minAttendance: Int = settings.get("min_attendance_setting", 75)

    if (subject != null) {
        val headerColor = themedContainerColor(if (subject.color != 0) Color(subject.color) else MaterialTheme.colorScheme.primary)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(subject.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditSubjectDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = headerColor)
                )
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    SmallFloatingActionButton(
                        onClick = { 
                            onPickFile { path, name, type -> 
                                viewModel.addMaterial(name, path, type)
                                platform.showToast("Material added")
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Upload Materials")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FloatingActionButton(onClick = { showAddNoteDialog = true }) {
                        Icon(Icons.Default.NoteAdd, contentDescription = "Add Note")
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(text = "Details", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Teacher: ${subject.teacher}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Room: ${subject.room}", style = MaterialTheme.typography.bodyLarge)
                    
                    val total = subject.attended + subject.missed
                    val percentage = if (total > 0) (subject.attended.toFloat() / total * 100).toInt() else 0
                    val color = getAttendanceColor(percentage, minAttendance)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Attendance", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(progress = { if (total > 0) subject.attended.toFloat() / total else 0f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = color)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Present: ${subject.attended}, Absent: ${subject.missed}, Total: $total", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "$percentage%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Today's Status", style = MaterialTheme.typography.titleMedium)
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val currentDay = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                    val todaySlots = viewModel.slots.filter { it.fragment == currentDay }
                    
                    if (todaySlots.isEmpty()) {
                        Text("No classes scheduled for today.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        todaySlots.forEach { slot ->
                            val status = viewModel.getAttendanceStatus(slot.id)
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "${slot.fromTime} - ${slot.toTime}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        if (status != null) Text(text = status.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        AttendanceButton("Present", status == "attended", Modifier.weight(1f)) { viewModel.updateAttendance(slot.id, "attended") }
                                        AttendanceButton("Absent", status == "missed", Modifier.weight(1f)) { viewModel.updateAttendance(slot.id, "missed") }
                                        AttendanceButton("Cancelled", status == "skipped", Modifier.weight(1f)) { viewModel.updateAttendance(slot.id, "skipped") }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (viewModel.notes.isNotEmpty()) {
                    item { Text(text = "Notes", style = MaterialTheme.typography.titleMedium) }
                    itemsIndexed(viewModel.notes, key = { _, n -> n.id }) { index, note ->
                        Box {
                            var showMenu by remember { mutableStateOf(false) }
                            NoteItem(note = note, onClick = { onNoteClick(note.id) }, onDelete = { noteToDelete = note }, onReorder = { showMenu = true })
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Move Up") }, onClick = { viewModel.moveNote(index, true); showMenu = false }, enabled = index > 0)
                                DropdownMenuItem(text = { Text("Move Down") }, onClick = { viewModel.moveNote(index, false); showMenu = false }, enabled = index < viewModel.notes.size - 1)
                            }
                        }
                    }
                }

                if (viewModel.materials.isNotEmpty()) {
                    item { 
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Materials", style = MaterialTheme.typography.titleMedium) 
                    }
                    itemsIndexed(viewModel.materials, key = { _, m -> m.id }) { index, material ->
                        Box {
                            var showMenu by remember { mutableStateOf(false) }
                            MaterialItem(material = material, onClick = { onOpenFile(material.path, material.type) }, onEditName = { materialToEdit = material }, onDelete = { materialToDelete = material }, onReorder = { showMenu = true })
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Move Up") }, onClick = { viewModel.moveMaterial(index, true); showMenu = false }, enabled = index > 0)
                                DropdownMenuItem(text = { Text("Move Down") }, onClick = { viewModel.moveMaterial(index, false); showMenu = false }, enabled = index < viewModel.materials.size - 1)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        SimpleAddNoteDialog(onDismiss = { showAddNoteDialog = false }, onSave = { title, color -> 
            viewModel.addNote(title, "", color)
            platform.showToast("Note added")
        })
    }

    materialToEdit?.let { material ->
        var newName by remember { mutableStateOf(material.name) }
        AlertDialog(
            onDismissRequest = { materialToEdit = null },
            title = { Text("Edit File Name") },
            text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("File Name") }) },
            confirmButton = { TextButton(onClick = { 
                viewModel.updateMaterialName(material.id, newName)
                platform.showToast("Renamed")
                materialToEdit = null 
            }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { materialToEdit = null }) { Text("Cancel") } }
        )
    }

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = { TextButton(onClick = { 
                viewModel.deleteNote(note.id)
                platform.showToast("Note deleted")
                noteToDelete = null 
            }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { noteToDelete = null }) { Text("Cancel") } }
        )
    }

    materialToDelete?.let { material ->
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Delete Material") },
            text = { Text("Are you sure you want to delete '${material.name}'?") },
            confirmButton = { TextButton(onClick = { 
                viewModel.deleteMaterial(material.id)
                platform.showToast("Material deleted")
                materialToDelete = null 
            }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { materialToDelete = null }) { Text("Cancel") } }
        )
    }

    if (showEditSubjectDialog) {
        EditSubjectDialog(subject = subject!!, onDismiss = { showEditSubjectDialog = false }, onSave = { updated -> viewModel.updateSubject(updated); showEditSubjectDialog = false })
    }
}

@Composable
fun SimpleAddNoteDialog(onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Text("Note Color")
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) { onSave(title, color); onDismiss() } }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MaterialItem(material: Material, onClick: () -> Unit, onEditName: () -> Unit, onDelete: () -> Unit, onReorder: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FilePresent, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = material.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = onReorder) { Icon(Icons.Default.SwapVert, contentDescription = "Reorder") }
            IconButton(onClick = onEditName) { Icon(Icons.Default.Edit, contentDescription = "Edit Name") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

@Composable
fun AttendanceButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (isSelected) {
        Button(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(0.dp)) { Text(text, style = MaterialTheme.typography.labelSmall) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(0.dp)) { Text(text, style = MaterialTheme.typography.labelSmall) }
    }
}
