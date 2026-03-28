package com.example.timetable.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.R
import com.example.timetable.model.Note
import com.example.timetable.model.Subject
import com.example.timetable.model.Week
import com.example.timetable.ui.viewmodel.MainViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.preference.PreferenceManager
import com.example.timetable.activities.SettingsActivity
import com.example.timetable.utils.DbHelper

import com.example.timetable.ui.components.NoteItem
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.components.SubjectItem
import com.example.timetable.ui.components.EditSubjectDialog
import androidx.compose.ui.graphics.toArgb

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var allSubjects = mutableStateListOf<Subject>()
        private set
    var subjectNames = mutableStateListOf<String>()
        private set

    init {
        loadSubjects()
    }

    fun loadSubjects() {
        allSubjects.clear()
        val subs = db.allSubjects
        for (sub in subs) {
            val combinedTeachers = db.getTeachersForSubject(sub.name)
            if (!combinedTeachers.isNullOrBlank()) {
                sub.teacher = combinedTeachers
            }
        }
        allSubjects.addAll(subs)
        subjectNames.clear()
        subjectNames.addAll(db.getSubjectsList())
    }

    fun insertNote(note: Note, subjectName: String? = null) {
        if (!subjectName.isNullOrBlank()) {
            val subject = db.getSubjectDetails(subjectName)
            if (subject == null) {
                db.insertSubject(subjectName, note.color, "", "")
            }
            note.subjectId = db.getAllSubjects().find { it.name == subjectName }?.id ?: -1
        }
        db.insertNote(note)
        loadSubjects()
    }

    fun moveSubject(index: Int, up: Boolean) {
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in allSubjects.indices) {
            val sub1 = allSubjects[index]
            val sub2 = allSubjects[targetIndex]
            db.updateSubjectSortOrder(sub1.id, targetIndex)
            db.updateSubjectSortOrder(sub2.id, index)
            loadSubjects()
        }
    }

    fun updateSubject(subject: Subject) {
        db.updateSubject(subject.id, subject.name, subject.color, subject.teacher, subject.room)
        loadSubjects()
    }

    fun deleteSubject(id: Int) {
        db.deleteSubjectById(id)
        loadSubjects()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onBack: () -> Unit, 
    onSubjectClick: (Int) -> Unit,
    viewModel: NoteViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPref = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    val minAttendance = remember { sharedPref.getInt(SettingsActivity.KEY_MIN_ATTENDANCE_SETTING, 75) }
    var showAddDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadSubjects()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.notes)) },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No subjects added yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap + to add your first subject.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                itemsIndexed(viewModel.allSubjects) { index, subject ->
                    Box {
                        var showReorderMenu by remember { mutableStateOf(false) }
                        SubjectItem(
                            subject = Week().apply {
                                this.subject = subject.name
                                this.color = subject.color
                                this.id = subject.id
                                this.teacher = subject.teacher
                                this.room = subject.room
                            },
                            attendanceEnabled = false,
                            minAttendance = minAttendance,
                            onClick = { onSubjectClick(subject.id) },
                            onMarkAttendance = { _: Int, _: String, _: String -> },
                            onEdit = { subjectToEdit = subject },
                            onDelete = { subjectToDelete = subject },
                            showRoom = false,
                            viewModel = mainViewModel
                        )
                        IconButton(
                            onClick = { showReorderMenu = true },
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 60.dp)
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Reorder")
                        }
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
            onSave = { note, subjectName -> 
                viewModel.insertNote(note, subjectName)
            },
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
                TextButton(
                    onClick = {
                        viewModel.deleteSubject(subject.id)
                        subjectToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
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
        title = { Text(stringResource(R.string.add_note)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title)) }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it }
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { 
                            subjectName = it
                            subjectExpanded = it.isNotEmpty()
                        },
                        label = { Text("Subject (creates if new)") },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
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
                    onSave(Note().apply {
                        this.title = title
                        this.text = ""
                        this.color = color
                    }, subjectName)
                    onDismiss()
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
