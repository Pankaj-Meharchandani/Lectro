package com.example.timetable.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.timetable.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToArchives: () -> Unit,
    onExportSchedule: () -> Unit,
    onImportSchedule: () -> Unit,
    onArchiveSemester: (String) -> Unit,
    viewModel: SettingsViewModel
) {
    var resetType by remember { mutableStateOf<ResetType?>(null) }
    var archiveName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    title = "7-Day Week",
                    control = { Switch(checked = viewModel.sevenDaysEnabled, onCheckedChange = { viewModel.updateSevenDays(it) }) }
                )
                SettingsItem(
                    title = "Personal Details",
                    control = { Switch(checked = viewModel.personalDetailsEnabled, onCheckedChange = { viewModel.updatePersonalDetails(it) }) }
                )
            }

            HorizontalDivider()

            SettingsSection(title = "Configuration") {
                OutlinedTextField(
                    value = viewModel.schoolWebsite,
                    onValueChange = { viewModel.updateSchoolWebsite(it) },
                    label = { Text("School Website") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://example.edu") }
                )
            }

            HorizontalDivider()

            SettingsSection(title = "Notifications") {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsItem(
                            title = "Enable Notifications",
                            control = { Switch(checked = viewModel.notificationsEnabled, onCheckedChange = { viewModel.updateNotificationsEnabled(it) }) }
                        )

                        AnimatedVisibility(visible = viewModel.notificationsEnabled, enter = expandVertically(), exit = shrinkVertically()) {
                            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                SettingsItem(title = "Schedule Reminder", control = { Switch(checked = viewModel.scheduleReminder, onCheckedChange = { viewModel.updateScheduleReminder(it) }) })
                                SettingsItem(title = "Assignment Reminder", control = { Switch(checked = viewModel.assignmentReminder, onCheckedChange = { viewModel.updateAssignmentReminder(it) }) })
                                SettingsItem(title = "Exam Reminder", control = { Switch(checked = viewModel.examReminder, onCheckedChange = { viewModel.updateExamReminder(it) }) })
                                SettingsItem(title = "Attendance Alert", control = { Switch(checked = viewModel.attendanceAlert, onCheckedChange = { viewModel.updateAttendanceAlert(it) }) })
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            SettingsSection(title = "Backup & Restore") {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Import or export your schedule as a .lec file to share with others or keep a backup.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onExportSchedule, modifier = Modifier.weight(1f)) {
                            Text("Export Schedule")
                        }
                        Button(onClick = onImportSchedule, modifier = Modifier.weight(1f)) {
                            Text("Import Schedule")
                        }
                    }
                }
            }

            HorizontalDivider()

            SettingsSection(title = "Danger Zone") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = onNavigateToArchives,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("View Archived Semesters")
                    }
                    DangerCard(
                        title = "Archive Current Semester",
                        description = "Saves everything to history and starts fresh.",
                        buttonText = "Archive Semester",
                        onClick = { resetType = ResetType.ARCHIVE }
                    )
                    DangerCard(
                        title = "Reset All Data",
                        description = "Wipes everything but keeps your personal details.",
                        buttonText = "Reset Data",
                        onClick = { resetType = ResetType.ALL }
                    )
                }
            }
        }
    }

    resetType?.let { type ->
        val title = if (type == ResetType.ARCHIVE) "Archive Semester" else "Reset Data"
        AlertDialog(
            onDismissRequest = { resetType = null },
            title = { Text(title) },
            text = {
                Column {
                    Text("Are you sure? This action will clear current data.")
                    if (type == ResetType.ARCHIVE) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = archiveName, onValueChange = { archiveName = it }, label = { Text("Archive Name") })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (type == ResetType.ARCHIVE) onArchiveSemester(archiveName)
                    else viewModel.resetData()
                    resetType = null 
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { resetType = null }) { Text("No") }
            }
        )
    }
}

enum class ResetType { ARCHIVE, ALL }

@Composable
fun DangerCard(title: String, description: String, buttonText: String, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth()) { Text(buttonText) }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
fun SettingsItem(title: String, control: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        control()
    }
}
