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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.R
import com.example.timetable.model.Material
import com.example.timetable.model.Note
import com.example.timetable.model.Subject
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.components.NoteItem
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.utils.DbHelper

class SubjectDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var subject by mutableStateOf<Subject?>(null)
    var notes = mutableStateListOf<Note>()
    var materials = mutableStateListOf<Material>()

    fun loadSubjectData(id: Int) {
        val allSubjects = db.getAllSubjects()
        subject = allSubjects.find { it.id == id }
        loadNotes(id)
        loadMaterials(id)
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

    LaunchedEffect(subjectId) {
        viewModel.loadSubjectData(subjectId)
    }

    val subject = viewModel.subject

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
                                onDelete = { viewModel.deleteNote(note.id) },
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
                                onDelete = { viewModel.deleteMaterial(material.id) },
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
            onSave = { title, content, color -> 
                viewModel.addNote(title, content, color)
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
}

@Composable
fun SimpleAddNoteDialog(onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") })
                Text("Note Color")
                ColorPickerRow(selectedColor = color, onColorSelected = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(title, content, color)
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
