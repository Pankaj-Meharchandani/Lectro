package com.example.timetable.ui.screens

import android.app.Application
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.R
import com.example.timetable.model.Homework
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.utils.DbHelper
import java.util.*

class HomeworkViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var homeworks = mutableStateListOf<Homework>()
        private set
    var subjects = mutableStateListOf<String>()
        private set

    init {
        loadHomeworks()
        loadSuggestions()
    }

    fun loadHomeworks() {
        homeworks.clear()
        homeworks.addAll(db.getHomework())
    }

    fun loadSuggestions() {
        subjects.clear()
        subjects.addAll(db.getSubjectsList())
    }

    fun getSubjectDetails(name: String): com.example.timetable.model.Week? {
        return db.getSubjectDetails(name)
    }

    fun deleteHomework(homework: Homework) {
        db.deleteHomeworkById(homework)
        loadHomeworks()
    }

    fun insertHomework(homework: Homework) {
        db.insertHomework(homework)
        loadHomeworks()
        loadSuggestions()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworksScreen(onBack: () -> Unit, viewModel: HomeworkViewModel = viewModel()) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.homeworks)) },
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
            items(viewModel.homeworks) { homework ->
                HomeworkItem(homework = homework, onDelete = { viewModel.deleteHomework(homework) })
            }
        }
    }

    if (showAddDialog) {
        AddHomeworkDialog(
            onDismiss = { showAddDialog = false },
            onSave = { homework -> viewModel.insertHomework(homework) },
            onGetSubjectDetails = { viewModel.getSubjectDetails(it) },
            subjectSuggestions = viewModel.subjects
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHomeworkDialog(
    onDismiss: () -> Unit, 
    onSave: (Homework) -> Unit,
    onGetSubjectDetails: (String) -> com.example.timetable.model.Week?,
    subjectSuggestions: List<String>
) {
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(0) }
    var subjectExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val updateFromSubjectDetails = { name: String ->
        onGetSubjectDetails(name)?.let { details ->
            color = details.color
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_homework)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) }
                    )
                    val filtered = subjectSuggestions.filter { it.contains(subject, ignoreCase = true) }
                    if (filtered.isNotEmpty()) {
                        ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                            filtered.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = {
                                    subject = s
                                    subjectExpanded = false
                                    updateFromSubjectDetails(s)
                                })
                            }
                        }
                    }
                }

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.desctiption)) })
                
                Button(onClick = {
                    val c = Calendar.getInstance()
                    DatePickerDialog(context, { _, y, m, d ->
                        date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    Text(if (date.isEmpty()) stringResource(R.string.select_date) else date)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (subject.isNotBlank()) {
                    onSave(Homework().apply {
                        this.subject = subject
                        this.description = description
                        this.date = date
                        this.color = color
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
fun HomeworkItem(homework: Homework, onDelete: () -> Unit) {
    val homeworkColor = if (homework.color != 0) Color(homework.color) else MaterialTheme.colorScheme.primary
    val containerColor = themedContainerColor(homeworkColor)
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
                Text(text = homework.subject, style = MaterialTheme.typography.titleLarge)
                Text(text = homework.description, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Deadline: ${homework.date}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
