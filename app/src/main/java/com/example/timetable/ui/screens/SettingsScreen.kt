package com.example.timetable.ui.screens

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var schoolWebsite by mutableStateOf(sharedPref.getString(SettingsActivity.KEY_SCHOOL_WEBSITE_SETTING, "") ?: "")

    fun updateSevenDays(enabled: Boolean) {
        sharedPref.edit().putBoolean(SettingsActivity.KEY_SEVEN_DAYS_SETTING, enabled).apply()
        sevenDaysEnabled = enabled
    }

    fun updateSchoolWebsite(url: String) {
        sharedPref.edit().putString(SettingsActivity.KEY_SCHOOL_WEBSITE_SETTING, url).apply()
        schoolWebsite = url
    }

    fun resetData() {
        db.resetAllData()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    var showResetDialog by remember { mutableStateOf(false) }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.sevendays_setting))
                Switch(checked = viewModel.sevenDaysEnabled, onCheckedChange = { viewModel.updateSevenDays(it) })
            }
            
            OutlinedTextField(
                value = viewModel.schoolWebsite,
                onValueChange = { viewModel.updateSchoolWebsite(it) },
                label = { Text(stringResource(R.string.school_website_setting)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.reset_data))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_data)) },
            text = { Text(stringResource(R.string.reset_warning)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetData()
                    showResetDialog = false
                }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.no)) }
            }
        )
    }
}
