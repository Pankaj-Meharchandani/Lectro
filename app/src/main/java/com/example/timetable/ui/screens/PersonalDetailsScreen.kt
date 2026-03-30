package com.example.timetable.ui.screens

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.timetable.R
import com.example.timetable.model.UserDetail
import com.example.timetable.model.UserFile
import com.example.timetable.utils.DbHelper

class PersonalDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    
    var userDetail by mutableStateOf(UserDetail())
    var userFiles = mutableStateListOf<UserFile>()

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var roll by mutableStateOf("")
    var other by mutableStateOf("")
    var photoPath by mutableStateOf<String?>(null)

    init {
        loadData()
    }

    fun loadData() {
        userDetail = db.getUserDetail()
        name = userDetail.name ?: ""
        email = userDetail.email ?: ""
        roll = userDetail.rollNumber ?: ""
        other = userDetail.other ?: ""
        photoPath = userDetail.photoPath
        
        userFiles.clear()
        userFiles.addAll(db.getAllUserFiles())
    }

    fun updateField(
        newName: String = name,
        newEmail: String = email,
        newRoll: String = roll,
        newOther: String = other,
        newPhoto: String? = photoPath
    ) {
        name = newName
        email = newEmail
        roll = newRoll
        other = newOther
        photoPath = newPhoto
        saveDetails()
    }

    private fun saveDetails() {
        userDetail.apply {
            this.name = this@PersonalDetailsViewModel.name
            this.email = this@PersonalDetailsViewModel.email
            this.rollNumber = this@PersonalDetailsViewModel.roll
            this.other = this@PersonalDetailsViewModel.other
            this.photoPath = this@PersonalDetailsViewModel.photoPath
        }
        db.saveUserDetail(userDetail)
    }

    fun addFile(title: String, path: String) {
        db.insertUserFile(UserFile().apply {
            this.title = title
            this.path = path
        })
        loadData()
    }

    fun deleteFile(id: Int) {
        db.deleteUserFile(id)
        loadData()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(onBack: () -> Unit, viewModel: PersonalDetailsViewModel = viewModel()) {
    var tempFileUri by remember { mutableStateOf<Uri?>(null) }
    var showAddFileDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {}
                viewModel.updateField(newPhoto = it.toString())
            }
        }
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {}
                // We'll show a dialog to name the file
                tempFileUri = it
                showAddFileDialog = true
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.personal_details_files)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                        onBack()
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { photoPickerLauncher.launch("image/*") },
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
            OutlinedTextField(
                value = viewModel.name, 
                onValueChange = { viewModel.updateField(newName = it) }, 
                label = { Text(stringResource(R.string.name_label)) }, 
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.email, 
                onValueChange = { viewModel.updateField(newEmail = it) }, 
                label = { Text(stringResource(R.string.email_label)) }, 
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.roll, 
                onValueChange = { viewModel.updateField(newRoll = it) }, 
                label = { Text(stringResource(R.string.roll_number_label)) }, 
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.other, 
                onValueChange = { viewModel.updateField(newOther = it) }, 
                label = { Text(stringResource(R.string.other_label)) }, 
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Files Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.my_files), style = MaterialTheme.typography.titleLarge)
                Button(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.upload_file))
                }
            }

            // File List (Manual rendering since we are inside verticalScroll)
            if (viewModel.userFiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No files uploaded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                viewModel.userFiles.forEach { file ->
                    FileItem(
                        file = file,
                        onDelete = { viewModel.deleteFile(file.id) },
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(file.path)
                                    flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error opening file
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddFileDialog && tempFileUri != null) {
        var fileName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddFileDialog = false },
            title = { Text("File Title") },
            text = {
                OutlinedTextField(value = fileName, onValueChange = { fileName = it }, label = { Text("Enter title (e.g. Admit Card)") })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (fileName.isNotBlank()) {
                        viewModel.addFile(fileName, tempFileUri.toString())
                        showAddFileDialog = false
                        tempFileUri = null
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddFileDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FileItem(file: UserFile, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.InsertDriveFile, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Text(text = file.title, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
