package com.example.timetable.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.timetable.data.DatabaseDriverFactory
import com.example.timetable.data.SqlDelightTimetableDatabase
import com.example.timetable.shared.Notifier
import com.example.timetable.shared.WidgetRefresher
import com.example.timetable.shared.initPlatform
import com.example.timetable.shared.initUriHandler
import com.example.timetable.ui.theme.TimeTableTheme
import com.example.timetable.ui.TimetableApp
import com.example.timetable.utils.NotificationHelper
import com.example.timetable.utils.WidgetUtils
import com.russhwolf.settings.SharedPreferencesSettings
import androidx.preference.PreferenceManager
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initPlatform(this)
        initUriHandler(this)
        
        val database = SqlDelightTimetableDatabase(DatabaseDriverFactory(this).createDriver())
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val settings = SharedPreferencesSettings(sharedPref)
        
        val notifier = object : Notifier {
            override fun scheduleEventsForToday() {
                NotificationHelper(this@MainActivity).scheduleEventsForToday()
            }
        }
        
        val widgetRefresher = object : WidgetRefresher {
            override fun refreshAllWidgets() {
                lifecycleScope.launch {
                    WidgetUtils.refreshAllWidgets(this@MainActivity)
                }
            }
        }

        setContent {
            var pendingImportCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
            
            val exportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/octet-stream")
            ) { uri ->
                uri?.let {
                    val content = intent.getStringExtra("export_content") ?: ""
                    contentResolver.openOutputStream(it)?.use { os ->
                        os.write(content.toByteArray())
                        Toast.makeText(this, "Schedule exported", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val importLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let {
                    contentResolver.openInputStream(it)?.use { isStream ->
                        val content = isStream.bufferedReader().readText()
                        pendingImportCallback?.invoke(content)
                        Toast.makeText(this, "Schedule imported", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            TimeTableTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimetableApp(
                        database = database,
                        settings = settings,
                        notifier = notifier,
                        widgetRefresher = widgetRefresher,
                        onExportSchedule = { content ->
                            intent.putExtra("export_content", content)
                            exportLauncher.launch("timetable.lec")
                        },
                        onImportSchedule = { callback ->
                            pendingImportCallback = callback
                            importLauncher.launch(arrayOf("*/*"))
                        }
                    )
                }
            }
        }
    }
}
