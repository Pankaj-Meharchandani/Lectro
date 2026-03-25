package com.example.timetable.ui.screens

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.example.timetable.R
import com.example.timetable.activities.SettingsActivity
import com.example.timetable.utils.DbHelper

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(application)

    var sevenDaysEnabled by mutableStateOf(sharedPref.getBoolean(SettingsActivity.KEY_SEVEN_DAYS_SETTING, false))
    var personalDetailsEnabled by mutableStateOf(sharedPref.getBoolean(SettingsActivity.KEY_PERSONAL_DETAILS_SETTING, true))
    var schoolWebsite by mutableStateOf(sharedPref.getString(SettingsActivity.KEY_SCHOOL_WEBSITE_SETTING, "") ?: "")

    fun updateSevenDays(enabled: Boolean) {
        sharedPref.edit().putBoolean(SettingsActivity.KEY_SEVEN_DAYS_SETTING, enabled).apply()
        sevenDaysEnabled = enabled
    }

    fun updatePersonalDetails(enabled: Boolean) {
        sharedPref.edit().putBoolean(SettingsActivity.KEY_PERSONAL_DETAILS_SETTING, enabled).apply()
        personalDetailsEnabled = enabled
    }

    fun updateSchoolWebsite(url: String) {
        sharedPref.edit().putString(SettingsActivity.KEY_SCHOOL_WEBSITE_SETTING, url).apply()
        schoolWebsite = url
    }

    fun resetData() {
        db.resetAllData()
    }

    fun removeFullSchedule() {
        db.removeFullSchedule()
    }

    fun removeAllSubjects() {
        db.removeAllSubjects()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    var resetType by remember { mutableStateOf<ResetType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.action_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    title = stringResource(R.string.sevendays_setting),
                    control = {
                        Switch(
                            checked = viewModel.sevenDaysEnabled,
                            onCheckedChange = { viewModel.updateSevenDays(it) }
                        )
                    }
                )
                SettingsItem(
                    title = stringResource(R.string.enable_personal_details),
                    control = {
                        Switch(
                            checked = viewModel.personalDetailsEnabled,
                            onCheckedChange = { viewModel.updatePersonalDetails(it) }
                        )
                    }
                )
            }

            HorizontalDivider()

            SettingsSection(title = "Configuration") {
                OutlinedTextField(
                    value = viewModel.schoolWebsite,
                    onValueChange = { viewModel.updateSchoolWebsite(it) },
                    label = { Text(stringResource(R.string.school_website_setting)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://example.edu") }
                )
                Text(
                    text = stringResource(R.string.school_website_setting_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            HorizontalDivider()

            SettingsSection(title = "Danger Zone") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Remove Full Schedule
                    DangerCard(
                        title = "Remove Full Schedule",
                        description = "Wipes all timetable slots but keeps subjects, homeworks, and notes.",
                        buttonText = "Remove Schedule",
                        onClick = { resetType = ResetType.SCHEDULE }
                    )

                    // Remove All Subjects
                    DangerCard(
                        title = "Remove All Subjects",
                        description = "Deletes all subjects, which also clears the schedule and associated materials.",
                        buttonText = "Remove Subjects",
                        onClick = { resetType = ResetType.SUBJECTS }
                    )

                    // Reset All Data
                    DangerCard(
                        title = stringResource(R.string.reset_data),
                        description = "Wipes everything (schedule, subjects, homeworks, notes, attendance) but keeps your personal details.",
                        buttonText = stringResource(R.string.reset_data),
                        onClick = { resetType = ResetType.ALL }
                    )
                }
            }
        }
    }

    resetType?.let { type ->
        val title = when (type) {
            ResetType.SCHEDULE -> "Remove Schedule?"
            ResetType.SUBJECTS -> "Remove All Subjects?"
            ResetType.ALL -> stringResource(R.string.reset_data)
        }
        val message = when (type) {
            ResetType.SCHEDULE -> "Are you sure you want to clear your entire timetable? This cannot be undone."
            ResetType.SUBJECTS -> "This will delete all subjects and clear your schedule. Are you sure?"
            ResetType.ALL -> stringResource(R.string.reset_warning)
        }

        AlertDialog(
            onDismissRequest = { resetType = null },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (type) {
                            ResetType.SCHEDULE -> viewModel.removeFullSchedule()
                            ResetType.SUBJECTS -> viewModel.removeAllSubjects()
                            ResetType.ALL -> viewModel.resetData()
                        }
                        resetType = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { resetType = null }) { Text(stringResource(R.string.no)) }
            }
        )
    }
}

enum class ResetType { SCHEDULE, SUBJECTS, ALL }

@Composable
fun DangerCard(title: String, description: String, buttonText: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(title: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        control()
    }
}
