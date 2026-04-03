package com.example.timetable.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.net.Uri
import org.json.JSONObject
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveDetailScreen(
    fileName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val archiveFile = File(context.filesDir, "semester_archives/$fileName")
    var archiveJson by remember { mutableStateOf<JSONObject?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(fileName) {
        if (archiveFile.exists()) {
            try {
                archiveJson = JSONObject(archiveFile.readText())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(archiveJson?.optString("archive_name") ?: "Archive Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (archiveJson == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                val tabs = listOf("Timetable", "Subjects", "Exams", "Assignments", "Notes", "Materials")
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> ArchiveTimetable(archiveJson!!)
                    1 -> ArchiveSubjects(archiveJson!!)
                    2 -> ArchiveExams(archiveJson!!)
                    3 -> ArchiveAssignments(archiveJson!!)
                    4 -> ArchiveNotes(archiveJson!!)
                    5 -> ArchiveMaterials(archiveJson!!)
                }
            }
        }
    }
}

@Composable
fun ArchiveTimetable(json: JSONObject) {
    val timetable = json.optJSONArray("timetable")
    if (timetable == null || timetable.length() == 0) {
        EmptyArchiveSection("No schedule archived")
    } else {
        val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val groupedByDay = (0 until timetable.length())
            .map { timetable.getJSONObject(it) }
            .groupBy { it.optString("f") }
            .toSortedMap(compareBy { dayOrder.indexOf(it) })

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            groupedByDay.forEach { (day, slots) ->
                item {
                    Text(day, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                items(slots) { slot ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slot.optString("s"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("Teacher: ${slot.optString("t")}", style = MaterialTheme.typography.bodySmall)
                                Text("Room: ${slot.optString("r")}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${slot.optString("ft")} - ${slot.optString("tt")}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveSubjects(json: JSONObject) {
    val subjects = json.optJSONArray("subjects")
    if (subjects == null || subjects.length() == 0) {
        EmptyArchiveSection("No subjects archived")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items((0 until subjects.length()).map { subjects.getJSONObject(it) }) { sub ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(sub.optString("n"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Teacher: ${sub.optString("t")}", style = MaterialTheme.typography.bodyMedium)
                        Text("Room: ${sub.optString("r")}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Text("Attended: ${sub.optInt("a")}, Missed: ${sub.optInt("m")}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveExams(json: JSONObject) {
    val exams = json.optJSONArray("exams")
    if (exams == null || exams.length() == 0) {
        EmptyArchiveSection("No exams archived")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items((0 until exams.length()).map { exams.getJSONObject(it) }) { e ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(e.optString("s"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Date: ${e.optString("d")} • Time: ${e.optString("tm")}", style = MaterialTheme.typography.bodyMedium)
                        Text("Room: ${e.optString("r")}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveAssignments(json: JSONObject) {
    val homeworks = json.optJSONArray("homeworks")
    if (homeworks == null || homeworks.length() == 0) {
        EmptyArchiveSection("No assignments archived")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items((0 until homeworks.length()).map { homeworks.getJSONObject(it) }) { h ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(h.optString("t"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Subject: ${h.optString("s")}", style = MaterialTheme.typography.bodyMedium)
                        Text("Due: ${h.optString("dt")}", style = MaterialTheme.typography.bodySmall)
                        if (h.optString("d").isNotEmpty()) Text(h.optString("d"), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveNotes(json: JSONObject) {
    val notes = json.optJSONArray("notes")
    if (notes == null || notes.length() == 0) {
        EmptyArchiveSection("No notes archived")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items((0 until notes.length()).map { notes.getJSONObject(it) }) { n ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { expanded = !expanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                n.optString("t"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "Collapse" else "Expand"
                            )
                        }
                        
                        if (expanded) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                n.optString("txt"),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                n.optString("txt"),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveMaterials(json: JSONObject) {
    val context = LocalContext.current
    val materials = json.optJSONArray("materials")
    if (materials == null || materials.length() == 0) {
        EmptyArchiveSection("No materials archived")
    } else {
        val existingMaterials = remember(materials) {
            (0 until materials.length()).map { materials.getJSONObject(it) }.filter { mat ->
                val path = mat.optString("p")
                if (path.isEmpty()) return@filter false
                try {
                    val uri = Uri.parse(path)
                    if (uri.scheme == "content") {
                        // For content URIs, we check if we can open it
                        try {
                            context.contentResolver.openInputStream(uri)?.use { true } ?: false
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        val file = File(uri.path ?: "")
                        file.exists()
                    }
                } catch (e: Exception) {
                    false
                }
            }
        }

        if (existingMaterials.isEmpty()) {
            EmptyArchiveSection("Archived files are no longer on this device")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(existingMaterials) { mat ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilePresent, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(mat.optString("n"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(mat.optString("t"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyArchiveSection(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
