package com.example.timetable.ui.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.example.timetable.R
import com.example.timetable.model.Material
import com.example.timetable.model.Note
import com.example.timetable.model.Subject
import com.example.timetable.model.Week
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.components.NoteItem
import com.example.timetable.ui.components.EditSubjectDialog
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.utils.AppConstants
import com.example.timetable.utils.DbHelper
import com.example.timetable.utils.PdfGenerator
import com.example.timetable.utils.ScheduleExporter
import java.text.SimpleDateFormat
import java.util.*
import com.example.timetable.ui.screens.getAttendanceColor

class SubjectDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var subject by mutableStateOf<Subject?>(null)
    var notes = mutableStateListOf<Note>()
    var materials = mutableStateListOf<Material>()
    var slots = mutableStateListOf<Week>()

    fun loadSubjectData(id: Int) {
        val allSubjects = db.getAllSubjects()
        val currentSubject = allSubjects.find { it.id == id }
        subject = currentSubject
        currentSubject?.name?.let {
            slots.clear()
            slots.addAll(db.getSlotsBySubject(it))
        }
        loadNotes(id)
        loadMaterials(id)
    }

    fun updateAttendance(weekId: Int, type: String) {
        subject?.let {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            db.updateAttendance(weekId, it.name, type, date)
            loadSubjectData(it.id)
        }
    }

    fun getAttendanceStatus(weekId: Int): String? {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return db.getAttendanceStatus(weekId, date)
    }

    fun loadNotes(subjectId: Int) {
        notes.clear()
        notes.addAll(db.getNotesBySubject(subjectId))
    }

    fun loadMaterials(subjectId: Int) {
        materials.clear()
        materials.addAll(db.getMaterialsBySubject(subjectId))
    }

    fun addNote(title: String, content: String, color: Int) {
        subject?.let {
            val note = Note().apply {
                this.subjectId = it.id
                this.title = title
                this.text = content
                this.color = if (color != 0) color else it.color
            }
            db.insertNote(note)
            loadNotes(it.id)
        }
    }

    fun deleteNote(id: Int) {
        db.deleteNoteById(id)
        subject?.let { loadNotes(it.id) }
    }

    fun addMaterial(name: String, path: String, type: String) {
        subject?.let {
            val material = Material().apply {
                this.subjectId = it.id
                this.name = name
                this.path = path
                this.type = type
            }
            db.insertMaterial(material)
            loadMaterials(it.id)
        }
    }

    fun updateMaterialName(id: Int, newName: String) {
        db.updateMaterialName(id, newName)
        subject?.let { loadMaterials(it.id) }
    }

    fun deleteMaterial(id: Int) {
        db.deleteMaterialById(id)
        subject?.let { loadMaterials(it.id) }
    }

    fun moveNote(index: Int, up: Boolean) {
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in notes.indices) {
            val note1 = notes[index]
            val note2 = notes[targetIndex]
            db.updateNoteSortOrder(note1.id, targetIndex)
            db.updateNoteSortOrder(note2.id, index)
            subject?.let { loadNotes(it.id) }
        }
    }

    fun moveMaterial(index: Int, up: Boolean) {
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in materials.indices) {
            val mat1 = materials[index]
            val mat2 = materials[targetIndex]
            db.updateMaterialSortOrder(mat1.id, targetIndex)
            db.updateMaterialSortOrder(mat2.id, index)
            subject?.let { loadMaterials(it.id) }
        }
    }

    fun updateSubject(updated: Subject) {
        db.updateSubject(updated.id, updated.name, updated.color, updated.teacher, updated.room)
        loadSubjectData(updated.id)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: Int, 
    onBack: () -> Unit, 
    onNoteClick: (Int) -> Unit,
    viewModel: SubjectDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var materialToEdit by remember { mutableStateOf<Material?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var materialToDelete by remember { mutableStateOf<Material?>(null) }
    var showEditSubjectDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    ScheduleExporter.exportSubject(context, viewModel.subject?.name ?: "", os)
                    android.widget.Toast.makeText(context, "Subject schedule exported", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    val openMaterial = { material: Material ->
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(material.path), material.type)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "No app found to open this file", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            uris.forEach { uri ->
                val contentResolver = context.contentResolver
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val fileName = uri.path?.substringAfterLast('/') ?: "file"
                val type = contentResolver.getType(uri) ?: "*/*"
                val subjectName = viewModel.subject?.name ?: ""
                viewModel.addMaterial("$subjectName: $fileName", uri.toString(), type)
            }
        }
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadSubjectData(subjectId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(subjectId) {
        viewModel.loadSubjectData(subjectId)
    }

    val subject = viewModel.subject
    val sharedPref = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    val minAttendance = remember { sharedPref.getInt(AppConstants.KEY_MIN_ATTENDANCE_SETTING, 75) }

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
                        IconButton(onClick = { exportLauncher.launch("${subject.name}.lec") }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Schedule")
                        }
                        IconButton(onClick = { 
                            PdfGenerator.generateAndShareSubjectNotes(context, subject.name, viewModel.notes)
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Share Notes as PDF")
                        }
                        IconButton(onClick = { showEditSubjectDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = headerColor
                    )
                )
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    SmallFloatingActionButton(
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { if (total > 0) subject.attended.toFloat() / total else 0f },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = color
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Present: ${subject.attended}, Absent: ${subject.missed}, Total: $total",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    if (subject.skipped > 0) {
                        Text(
                            text = "Lectures didn't happen: ${subject.skipped}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Today's Status", style = MaterialTheme.typography.titleMedium)
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
                    val todaySlots = viewModel.slots.filter { it.fragment == currentDay }
                    
                    if (todaySlots.isEmpty()) {
                        Text("No classes scheduled for today.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        todaySlots.forEach { slot ->
                            val status = viewModel.getAttendanceStatus(slot.id)
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${slot.fromTime} - ${slot.toTime}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (status != null) {
                                            Text(
                                                text = status.replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        AttendanceButton("Present", status == "attended", Modifier.weight(1f)) {
                                            viewModel.updateAttendance(slot.id, "attended")
                                        }
                                        AttendanceButton("Absent", status == "missed", Modifier.weight(1f)) {
                                            viewModel.updateAttendance(slot.id, "missed")
                                        }
                                        AttendanceButton("Cancelled", status == "skipped", Modifier.weight(1f)) {
                                            viewModel.updateAttendance(slot.id, "skipped")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (viewModel.notes.isNotEmpty()) {
                    item { Text(text = "Notes", style = MaterialTheme.typography.titleMedium) }
                    itemsIndexed(viewModel.notes) { index, note ->
                        Box {
                            var showMenu by remember { mutableStateOf(false) }
                            NoteItem(
                                note = note, 
                                onClick = { onNoteClick(note.id) },
                                onDelete = { noteToDelete = note },
                                onReorder = { showMenu = true }
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Move Up") },
                                    onClick = { viewModel.moveNote(index, true); showMenu = false },
                                    enabled = index > 0
                                )
                                DropdownMenuItem(
                                    text = { Text("Move Down") },
                                    onClick = { viewModel.moveNote(index, false); showMenu = false },
                                    enabled = index < viewModel.notes.size - 1
                                )
                            }
                        }
                    }
                }

                if (viewModel.materials.isNotEmpty()) {
                    item { 
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Materials", style = MaterialTheme.typography.titleMedium) 
                    }
                    itemsIndexed(viewModel.materials) { index, material ->
                        Box {
                            var showMenu by remember { mutableStateOf(false) }
                            MaterialItem(
                                material = material,
                                onClick = { openMaterial(material) },
                                onEditName = { materialToEdit = material },
                                onDelete = { materialToDelete = material },
                                onReorder = { showMenu = true }
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Move Up") },
                                    onClick = { viewModel.moveMaterial(index, true); showMenu = false },
                                    enabled = index > 0
                                )
                                DropdownMenuItem(
                                    text = { Text("Move Down") },
                                    onClick = { viewModel.moveMaterial(index, false); showMenu = false },
                                    enabled = index < viewModel.materials.size - 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        SimpleAddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onSave = { title, color -> 
                viewModel.addNote(title, "", color)
            }
        )
    }

    materialToEdit?.let { material ->
        var newName by remember { mutableStateOf(material.name) }
        AlertDialog(
            onDismissRequest = { materialToEdit = null },
            title = { Text("Edit File Name") },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("File Name") })
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateMaterialName(material.id, newName)
                    materialToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { materialToEdit = null }) { Text("Cancel") }
            }
        )
    }

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(note.id)
                    noteToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("Cancel") }
            }
        )
    }

    materialToDelete?.let { material ->
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Delete Material") },
            text = { Text("Are you sure you want to delete '${material.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMaterial(material.id)
                    materialToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showEditSubjectDialog) {
        EditSubjectDialog(
            subject = subject!!,
            onDismiss = { showEditSubjectDialog = false },
            onSave = { updated ->
                viewModel.updateSubject(updated)
                showEditSubjectDialog = false
            }
        )
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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Text("Note Color")
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(title, color)
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
fun MaterialItem(
    material: Material, 
    onClick: () -> Unit, 
    onEditName: () -> Unit, 
    onDelete: () -> Unit,
    onReorder: () -> Unit
) {
    val icon = when {
        material.type?.startsWith("image/") == true -> Icons.Default.Image
        material.type?.contains("code") == true || material.name?.endsWith(".kt") == true || material.name?.endsWith(".java") == true -> Icons.Default.Code
        else -> Icons.Default.FilePresent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = material.name ?: "", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = onReorder) {
                Icon(Icons.Default.SwapVert, contentDescription = "Reorder")
            }
            IconButton(onClick = onEditName) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Name")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun AttendanceButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (isSelected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}
