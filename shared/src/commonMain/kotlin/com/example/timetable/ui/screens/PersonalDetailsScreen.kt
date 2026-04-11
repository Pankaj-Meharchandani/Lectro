package com.example.timetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.timetable.model.UserFile
import com.example.timetable.ui.viewmodel.PersonalDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    onBack: () -> Unit, 
    viewModel: PersonalDetailsViewModel,
    onPickImage: (callback: (String) -> Unit) -> Unit = {},
    onPickFile: (callback: (String, String, String) -> Unit) -> Unit = { _ -> },
    onOpenFile: (String, String) -> Unit = { _, _ -> }
) {
    LaunchedEffect(Unit) { viewModel.loadData() }
    var showAddFileDialog by remember { mutableStateOf(false) }
    var tempFilePath by remember { mutableStateOf("") }
    var tempFileName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.Done, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Photo
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onPickImage { viewModel.updateField(newPhoto = it) } },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.photoPath != null) {
                    AsyncImage(
                        model = viewModel.photoPath,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", modifier = Modifier.size(40.dp))
                }
            }

            // Details
            OutlinedTextField(value = viewModel.name, onValueChange = { viewModel.updateField(newName = it) }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.updateField(newEmail = it) }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.roll, onValueChange = { viewModel.updateField(newRoll = it) }, label = { Text("Roll Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.other, onValueChange = { viewModel.updateField(newOther = it) }, label = { Text("Other Info") }, modifier = Modifier.fillMaxWidth())

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Files Section
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("My Files", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { 
                    onPickFile { path, name, _ -> 
                        tempFilePath = path
                        tempFileName = name
                        showAddFileDialog = true
                    }
                }) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload File")
                }
            }

            if (viewModel.userFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))
                        Text("No files uploaded yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                viewModel.userFiles.forEach { file ->
                    FileItem(file = file, onDelete = { viewModel.deleteFile(file.id) }, onClick = { onOpenFile(file.path, "*/*") })
                }
            }
        }
    }

    if (showAddFileDialog) {
        var title by remember { mutableStateOf(tempFileName) }
        AlertDialog(
            onDismissRequest = { showAddFileDialog = false },
            title = { Text("File Title") },
            text = { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Enter title") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addFile(title, tempFilePath)
                        showAddFileDialog = false
                    }
                }) { Text("Add") }
            }
        )
    }
}

@Composable
fun FileItem(file: UserFile, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.InsertDriveFile, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Text(text = file.title, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}
