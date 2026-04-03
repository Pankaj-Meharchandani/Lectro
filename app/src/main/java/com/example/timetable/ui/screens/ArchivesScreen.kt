package com.example.timetable.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timetable.utils.SemesterArchiveManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivesScreen(
    onBack: () -> Unit,
    onNavigateToArchiveDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val archives = remember { mutableStateListOf<SemesterArchiveManager.ArchiveInfo>() }
    var archiveToDelete by remember { mutableStateOf<SemesterArchiveManager.ArchiveInfo?>(null) }

    LaunchedEffect(Unit) {
        archives.addAll(SemesterArchiveManager.getArchives(context))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Semesters") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (archives.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("No archives found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(archives) { archive ->
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToArchiveDetail(archive.file.name) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(archive.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(sdf.format(Date(archive.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { archiveToDelete = archive }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Archive", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    archiveToDelete?.let { archive ->
        AlertDialog(
            onDismissRequest = { archiveToDelete = null },
            title = { Text("Delete Archive") },
            text = { Text("Are you sure you want to permanently delete '${archive.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        archive.file.delete()
                        archives.remove(archive)
                        archiveToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { archiveToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
