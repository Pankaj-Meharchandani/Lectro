package com.example.timetable.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.R
import com.example.timetable.model.Teacher
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.utils.DbHelper

class TeacherViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var teachers = mutableStateListOf<Teacher>()
        private set

    init {
        loadTeachers()
    }

    fun loadTeachers() {
        teachers.clear()
        teachers.addAll(db.getTeacher())
    }

    fun deleteTeacher(teacher: Teacher) {
        db.deleteTeacherById(teacher)
        loadTeachers()
    }

    fun insertTeacher(teacher: Teacher) {
        db.insertTeacher(teacher)
        loadTeachers()
    }

    fun updateTeacher(teacher: Teacher) {
        db.updateTeacher(teacher)
        loadTeachers()
    }

    fun moveTeacher(index: Int, up: Boolean) {
        val targetIndex = if (up) index - 1 else index + 1
        if (targetIndex in teachers.indices) {
            val t1 = teachers[index]
            val t2 = teachers[targetIndex]
            db.updateTeacherSortOrder(t1.id, targetIndex)
            db.updateTeacherSortOrder(t2.id, index)
            loadTeachers()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachersScreen(onBack: () -> Unit, viewModel: TeacherViewModel = viewModel()) {
    var showAddDialog by remember { mutableStateOf(false) }
    var teacherToEdit by remember { mutableStateOf<Teacher?>(null) }
    var teacherToDelete by remember { mutableStateOf<Teacher?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.teachers)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            itemsIndexed(viewModel.teachers) { index, teacher ->
                Box {
                    var showMenu by remember { mutableStateOf(false) }
                    TeacherItem(
                        teacher = teacher, 
                        onDelete = { teacherToDelete = teacher },
                        onEdit = { teacherToEdit = it }
                    )
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 60.dp)
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Reorder")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Move Up") },
                            onClick = { viewModel.moveTeacher(index, true); showMenu = false },
                            enabled = index > 0
                        )
                        DropdownMenuItem(
                            text = { Text("Move Down") },
                            onClick = { viewModel.moveTeacher(index, false); showMenu = false },
                            enabled = index < viewModel.teachers.size - 1
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || teacherToEdit != null) {
        AddTeacherDialog(
            onDismiss = { 
                showAddDialog = false
                teacherToEdit = null
            },
            onSave = { teacher -> 
                if (teacherToEdit != null) {
                    viewModel.updateTeacher(teacher)
                } else {
                    viewModel.insertTeacher(teacher)
                }
                teacherToEdit = null
            },
            initialTeacher = teacherToEdit ?: Teacher()
        )
    }

    teacherToDelete?.let { teacher ->
        AlertDialog(
            onDismissRequest = { teacherToDelete = null },
            title = { Text("Delete Teacher") },
            text = { Text("Are you sure you want to delete '${teacher.name}' from your contacts?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTeacher(teacher)
                    teacherToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { teacherToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddTeacherDialog(
    onDismiss: () -> Unit, 
    onSave: (Teacher) -> Unit,
    initialTeacher: Teacher = Teacher()
) {
    var name by remember { mutableStateOf(initialTeacher.name ?: "") }
    var post by remember { mutableStateOf(initialTeacher.post ?: "") }
    var phone by remember { mutableStateOf(initialTeacher.phonenumber ?: "") }
    var email by remember { mutableStateOf(initialTeacher.email ?: "") }
    var cabinNumber by remember { mutableStateOf(initialTeacher.getCabinNumber() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTeacher.id == 0) stringResource(R.string.add_teacher) else stringResource(R.string.edit_teacher)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name)) })
                OutlinedTextField(value = post, onValueChange = { post = it }, label = { Text(stringResource(R.string.post)) })
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == ' ' || it == '+' || it == '-' }) {
                            phone = input
                        }
                    }, 
                    label = { Text(stringResource(R.string.phone_number)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email)) })
                OutlinedTextField(value = cabinNumber, onValueChange = { cabinNumber = it }, label = { Text(stringResource(R.string.cabin_number)) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(initialTeacher.apply {
                        this.name = name
                        this.post = post
                        this.phonenumber = phone
                        this.email = email
                        this.setCabinNumber(cabinNumber)
                    })
                    onDismiss()
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun TeacherItem(
    teacher: Teacher, 
    onDelete: () -> Unit,
    onEdit: (Teacher) -> Unit
) {
    val teacherColor = if (teacher.color != 0) Color(teacher.color) else MaterialTheme.colorScheme.primary
    val containerColor = themedContainerColor(teacherColor)
    val contentColor = contentColorFor(containerColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { onEdit(teacher) },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = teacher.name, style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = teacher.post, style = MaterialTheme.typography.bodyMedium)
                }
                if (teacher.phonenumber.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = teacher.phonenumber, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (teacher.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = teacher.email, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (teacher.getCabinNumber()?.isNotBlank() == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Room, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Cabin: ${teacher.getCabinNumber()}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
