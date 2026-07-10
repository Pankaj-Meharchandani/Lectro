package com.example.timetable.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.model.SemesterResult
import com.example.timetable.model.SubjectGrade
import com.example.timetable.utils.DbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GradeHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var results = mutableStateListOf<SemesterResult>()

    fun loadResults() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = db.getAllSemesterResults()
            withContext(Dispatchers.Main) {
                results.clear()
                results.addAll(data)
            }
        }
    }

    fun deleteResult(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.deleteSemesterResult(id)
            loadResults()
        }
    }

    fun addManualResult(name: String, gpa: Double, grades: List<SubjectGrade>) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = SemesterResult(
                semesterName = name,
                gpa = gpa,
                date = System.currentTimeMillis(),
                subjectGrades = grades
            )
            db.insertSemesterResult(result)
            loadResults()
        }
    }

    fun getSubjects() : List<String> {
         return db.getSubjectsList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeHistoryScreen(onBack: () -> Unit, viewModel: GradeHistoryViewModel = viewModel()) {
    var selectedResult by remember { mutableStateOf<SemesterResult?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadResults()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grade History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Grade Record")
            }
        }
    ) { padding ->
        if (viewModel.results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Grade, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Text("No grades recorded yet.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(viewModel.results) { result ->
                    GradeRecordItem(
                        result = result,
                        onClick = { selectedResult = result },
                        onDelete = { viewModel.deleteResult(result.id) }
                    )
                }
            }
        }
    }

    selectedResult?.let { result ->
        GradeDetailDialog(result = result, onDismiss = { selectedResult = null })
    }

    if (showAddDialog) {
        AddManualGradeDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, gpa, grades ->
                viewModel.addManualResult(name, gpa, grades)
                showAddDialog = false
            },
            subjects = viewModel.getSubjects()
        )
    }
}

@Composable
fun GradeRecordItem(result: SemesterResult, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = result.semesterName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(result.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "GPA: ${"%.2f".format(result.gpa)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailDialog(result: SemesterResult, onDismiss: () -> Unit) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.92f),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = result.semesterName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Overall GPA: ${"%.2f".format(result.gpa)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Box(modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.subjectGrades.forEach { grade ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = grade.subjectName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(text = "${grade.credits} Credits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = grade.gradePoint.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun AddManualGradeDialog(onDismiss: () -> Unit, onSave: (String, Double, List<SubjectGrade>) -> Unit, subjects: List<String>) {
    var semName by remember { mutableStateOf("") }
    val gradeList = remember { mutableStateListOf<SubjectGrade>() }
    
    // Auto-fill subjects if available
    LaunchedEffect(subjects) {
        if (gradeList.isEmpty()) {
            subjects.forEach { name ->
                gradeList.add(SubjectGrade(subjectName = name, gradePoint = 0.0, credits = 0))
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Semester Result") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = semName, onValueChange = { semName = it }, label = { Text("Semester Name (e.g. Sem 1)") }, modifier = Modifier.fillMaxWidth())
                
                Text("Subject Grades", style = MaterialTheme.typography.titleSmall)
                
                gradeList.forEachIndexed { index, grade ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = grade.subjectName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (grade.credits > 0) grade.credits.toString() else "",
                                    onValueChange = { if (it.all { char -> char.isDigit() }) gradeList[index] = grade.copy(credits = it.toIntOrNull() ?: 0) },
                                    label = { Text("Cr") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = if (grade.gradePoint > 0) grade.gradePoint.toString() else "",
                                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) gradeList[index] = grade.copy(gradePoint = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text("GP") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                                )
                            }
                        }
                    }
                }
                
                Button(onClick = { gradeList.add(SubjectGrade(subjectName = "New Subject", gradePoint = 0.0, credits = 0)) }) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Subject")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (semName.isNotBlank() && gradeList.isNotEmpty()) {
                    val totalCredits = gradeList.sumOf { it.credits }
                    val totalGP = gradeList.sumOf { it.gradePoint * it.credits }
                    val gpa = if (totalCredits > 0) totalGP / totalCredits else 0.0
                    onSave(semName, gpa, gradeList.toList())
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
