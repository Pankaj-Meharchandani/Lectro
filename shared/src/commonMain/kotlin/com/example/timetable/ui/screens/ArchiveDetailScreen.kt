package com.example.timetable.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.timetable.shared.getFileHandler
import com.example.timetable.utils.SemesterArchive
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveDetailScreen(
    fileName: String,
    onBack: () -> Unit
) {
    val fileHandler = remember { getFileHandler() }
    val archiveJson = remember(fileName) { 
        fileHandler.getArchives().find { it.first == fileName }?.second ?: "" 
    }
    
    val archive = remember(archiveJson) {
        try {
            Json { ignoreUnknownKeys = true }.decodeFromString(SemesterArchive.serializer(), archiveJson)
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(archive?.archiveName ?: fileName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (archive == null) {
                Text("Error loading archive content.")
            } else {
                Text("Archive Date: ${archive.archiveDate}", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(16.dp))
                
                Text("Subjects", style = MaterialTheme.typography.titleLarge)
                archive.subjects.forEach { s ->
                    ListItem(headlineContent = { Text(s.name) }, supportingContent = { Text("Teacher: ${s.teacher}") })
                }
                
                Spacer(Modifier.height(16.dp))
                Text("Timetable", style = MaterialTheme.typography.titleLarge)
                archive.timetable.forEach { w ->
                    ListItem(headlineContent = { Text("${w.subject} (${w.fragment})") }, supportingContent = { Text("${w.fromTime} - ${w.toTime}") })
                }

                Spacer(Modifier.height(16.dp))
                Text("Notes", style = MaterialTheme.typography.titleLarge)
                archive.notes.forEach { n ->
                    ListItem(headlineContent = { Text(n.title) }, supportingContent = { Text(n.text.take(50) + "...") })
                }
            }
        }
    }
}
