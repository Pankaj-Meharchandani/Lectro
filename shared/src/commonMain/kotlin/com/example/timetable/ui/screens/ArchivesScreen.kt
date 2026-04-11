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
import androidx.compose.ui.unit.dp
import com.example.timetable.shared.getFileHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivesScreen(
    onBack: () -> Unit,
    onNavigateToArchiveDetail: (String) -> Unit
) {
    val fileHandler = remember { getFileHandler() }
    var archives by remember { mutableStateOf(fileHandler.getArchives()) }

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
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Text("No archives yet.", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(archives) { (name, _) ->
                    ListItem(
                        headlineContent = { Text(name) },
                        trailingContent = {
                            IconButton(onClick = { 
                                fileHandler.deleteArchive(name)
                                archives = fileHandler.getArchives()
                            }) {
                                Icon(Icons.Default.Delete, null)
                            }
                        },
                        modifier = Modifier.clickable { onNavigateToArchiveDetail(name) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
