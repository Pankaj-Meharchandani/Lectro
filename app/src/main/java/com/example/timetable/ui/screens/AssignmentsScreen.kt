package com.example.timetable.ui.screens

import android.app.Application
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.R
import com.example.timetable.model.Homework
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.utils.DbHelper
import com.example.timetable.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

class AssignmentsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    private val notificationHelper = NotificationHelper(application)
    var assignments = mutableStateListOf<Homework>()
        private set
    var subjects = mutableStateListOf<String>()
        private set

    init {
        loadAssignments()
        loadSuggestions()
    }

    fun loadAssignments() {
        assignments.clear()
        assignments.addAll(db.getHomework())
    }

    fun loadSuggestions() {
        subjects.clear()
        subjects.addAll(db.getSubjectsList())
    }

    fun getSubjectDetails(name: String): com.example.timetable.model.Week? {
        return db.getSubjectDetails(name)
    }

    fun deleteAssignment(assignment: Homework) {
        db.deleteHomeworkById(assignment)
        loadAssignments()
        notificationHelper.scheduleEventsForToday()
    }

    fun insertAssignment(assignment: Homework) {
        db.insertHomework(assignment)
        loadAssignments()
        loadSuggestions()
        notificationHelper.scheduleEventsForToday()
    }

    fun updateAssignment(assignment: Homework) {
        db.updateHomework(assignment)
        loadAssignments()
        notificationHelper.scheduleEventsForToday()
    }

    fun toggleComplete(assignment: Homework) {
        assignment.setCompleted(if (assignment.getCompleted() == 1) 0 else 1)
        db.updateHomework(assignment)
        loadAssignments()
        notificationHelper.scheduleEventsForToday()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(onBack: () -> Unit, viewModel: AssignmentsViewModel = viewModel()) {
    var showAddDialog by remember { mutableStateOf(false) }
    var assignmentToEdit by remember { mutableStateOf<Homework?>(null) }
    var assignmentToDelete by remember { mutableStateOf<Homework?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Overdue", "Completed")

    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadAssignments()
                viewModel.loadSuggestions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val filteredAssignments by remember(selectedTab) {
        derivedStateOf {
            viewModel.assignments.filter { assignment ->
                when (selectedTab) {
                    0 -> assignment.getCompleted() == 0 && (assignment.date ?: "") >= currentDate
                    1 -> assignment.getCompleted() == 0 && (assignment.date ?: "") < currentDate
                    2 -> assignment.getCompleted() == 1
                    else -> true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.homeworks)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (filteredAssignments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        when (selectedTab) {
                            0 -> Icons.AutoMirrored.Filled.Assignment
                            1 -> Icons.Default.EventBusy
                            else -> Icons.Default.AssignmentTurnedIn
                        },
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        when (selectedTab) {
                            0 -> "No pending assignments!"
                            1 -> "No overdue assignments!"
                            else -> "No completed assignments yet."
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedTab == 0) {
                        Text(
                            "Tap + to stay on top of your work.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(filteredAssignments) { assignment ->
                    AssignmentItem(
                        assignment = assignment, 
                        onDelete = { assignmentToDelete = assignment },
                        onToggleComplete = { viewModel.toggleComplete(assignment) },
                        onEdit = { assignmentToEdit = it }
                    )
                }
            }
        }
    }

    if (showAddDialog || assignmentToEdit != null) {
        AddAssignmentDialog(
            onDismiss = { 
                showAddDialog = false
                assignmentToEdit = null
            },
            onSave = { assignment -> 
                if (assignmentToEdit != null) {
                    viewModel.updateAssignment(assignment)
                } else {
                    viewModel.insertAssignment(assignment)
                }
                assignmentToEdit = null
            },
            onGetSubjectDetails = { viewModel.getSubjectDetails(it) },
            subjectSuggestions = viewModel.subjects,
            initialAssignment = assignmentToEdit ?: Homework()
        )
    }

    assignmentToDelete?.let { assignment ->
        AlertDialog(
            onDismissRequest = { assignmentToDelete = null },
            title = { Text("Delete Assignment") },
            text = { Text("Are you sure you want to delete '${assignment.title}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAssignment(assignment)
                    assignmentToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { assignmentToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentDialog(
    onDismiss: () -> Unit, 
    onSave: (Homework) -> Unit,
    onGetSubjectDetails: (String) -> com.example.timetable.model.Week?,
    subjectSuggestions: List<String>,
    initialAssignment: Homework = Homework()
) {
    var subject by remember { mutableStateOf(initialAssignment.subject ?: "") }
    var title by remember { mutableStateOf(initialAssignment.title ?: "") }
    var description by remember { mutableStateOf(initialAssignment.description ?: "") }
    var date by remember { mutableStateOf(initialAssignment.date ?: "") }
    var color by remember { mutableIntStateOf(initialAssignment.color) }
    var subjectExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val updateFromSubjectDetails = { name: String ->
        onGetSubjectDetails(name)?.let { details ->
            color = details.color
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAssignment.id == 0) stringResource(R.string.add_homework) else stringResource(R.string.edit_homework)) },
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

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title)) })
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
                if (subject.isNotBlank() && title.isNotBlank()) {
                    onSave(initialAssignment.apply {
                        this.subject = subject
                        this.title = title
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
fun AssignmentItem(
    assignment: Homework, 
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    onEdit: (Homework) -> Unit
) {
    val assignmentColor = if (assignment.color != 0) Color(assignment.color) else MaterialTheme.colorScheme.primary
    val containerColor = themedContainerColor(assignmentColor)
    val contentColor = contentColorFor(containerColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { onEdit(assignment) },
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
                Text(text = assignment.subject, style = MaterialTheme.typography.labelSmall)
                Text(text = assignment.title ?: "", style = MaterialTheme.typography.titleLarge)
                if (!assignment.description.isNullOrBlank()) {
                    Text(text = assignment.description, style = MaterialTheme.typography.bodyMedium)
                }
                Text(text = "Deadline: ${assignment.date}", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onToggleComplete) {
                    Icon(
                        imageVector = Icons.Default.Check, 
                        contentDescription = "Mark Complete",
                        tint = if (assignment.getCompleted() == 1) Color.Green else contentColor
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
