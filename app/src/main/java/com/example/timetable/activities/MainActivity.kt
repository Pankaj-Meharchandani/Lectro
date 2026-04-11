package com.example.timetable.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.example.timetable.data.DatabaseDriverFactory
import com.example.timetable.data.SqlDelightTimetableDatabase
import com.example.timetable.shared.Notifier
import com.example.timetable.shared.WidgetRefresher
import com.example.timetable.shared.initPlatform
import com.example.timetable.shared.initUriHandler
import com.example.timetable.shared.initFileHandler
import com.example.timetable.ui.theme.TimeTableTheme
import com.example.timetable.ui.TimetableApp
import com.example.timetable.utils.NotificationHelper
import com.example.timetable.utils.WidgetUtils
import com.example.timetable.utils.PdfExportUtil
import com.russhwolf.settings.SharedPreferencesSettings
import androidx.preference.PreferenceManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initPlatform(this)
        initUriHandler(this)
        initFileHandler(this)
        
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
            var pendingImageCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
            var pendingFileCallback by remember { mutableStateOf<((String, String, String) -> Unit)?>(null) }
            
            val exportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/octet-stream")
            ) { uri ->
                uri?.let {
                    val content = intent.getStringExtra("export_content") ?: ""
                    contentResolver.openOutputStream(it)?.use { os ->
                        os.write(content.toByteArray())
                        Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show()
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
                    }
                }
            }

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                uri?.let {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    pendingImageCallback?.invoke(it.toString())
                }
            }

            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    // We don't have the path easily here, we use the URI string
                    pendingFileCallback?.invoke(it.toString(), "Uploaded File", "*/*")
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
                        },
                        onPickImage = { callback ->
                            pendingImageCallback = callback
                            imagePickerLauncher.launch("image/*")
                        },
                        onPickFile = { callback ->
                            pendingFileCallback = callback
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        onOpenFile = { path, type ->
                            try {
                                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(path.toUri(), type)
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                startActivity(Intent.createChooser(openIntent, "Open with"))
                            } catch (_: Exception) {
                                Toast.makeText(this, "No app to open this file", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onExportPdf = { days, data ->
                            PdfExportUtil.exportScheduleToPdf(this, days, data)
                        },
                        onShareText = { text ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            startActivity(Intent.createChooser(shareIntent, "Share"))
                        },
                        onReportIssue = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://github.com/Pankaj-Meharchandani/Lectro/issues/new".toUri())
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
