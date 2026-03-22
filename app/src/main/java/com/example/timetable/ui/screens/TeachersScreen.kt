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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
                    TeacherItem(teacher = teacher, onDelete = { viewModel.deleteTeacher(teacher) })
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

    if (showAddDialog) {
        AddTeacherDialog(
            onDismiss = { showAddDialog = false },
            onSave = { teacher -> viewModel.insertTeacher(teacher) }
        )
    }
}

@Composable
fun AddTeacherDialog(onDismiss: () -> Unit, onSave: (Teacher) -> Unit) {
    var name by remember { mutableStateOf("") }
    var post by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_teacher)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name)) })
                OutlinedTextField(value = post, onValueChange = { post = it }, label = { Text(stringResource(R.string.post)) })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.phone_number)) })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email)) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(Teacher().apply {
                        this.name = name
                        this.post = post
                        this.phonenumber = phone
                        this.email = email
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
fun TeacherItem(teacher: Teacher, onDelete: () -> Unit) {
    val teacherColor = if (teacher.color != 0) Color(teacher.color) else MaterialTheme.colorScheme.primary
    val containerColor = themedContainerColor(teacherColor)
    val contentColor = contentColorFor(containerColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = teacher.name, style = MaterialTheme.typography.titleLarge)
                Text(text = teacher.post, style = MaterialTheme.typography.bodyMedium)
                Text(text = teacher.phonenumber, style = MaterialTheme.typography.bodySmall)
                Text(text = teacher.email, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
