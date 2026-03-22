package com.example.timetable.ui.screens

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timetable.R
import com.example.timetable.model.Note
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.theme.themedContainerColor
import com.example.timetable.utils.DbHelper

class NoteInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var note by mutableStateOf<Note?>(null)

    fun loadNote(id: Int) {
        val allNotes = db.getNote()
        note = allNotes.find { it.id == id }
    }

    fun saveNote(note: Note) {
        db.updateNote(note)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInfoScreen(noteId: Int, onBack: () -> Unit, viewModel: NoteInfoViewModel = viewModel()) {
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    val note = viewModel.note

    if (note != null) {
        var title by remember(note) { mutableStateOf(note.title) }
        var textValue by remember(note) { mutableStateOf(TextFieldValue(note.text)) }
        var color by remember(note) { mutableIntStateOf(note.color) }
        var showColorPicker by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }

        val triggerSave = {
            viewModel.saveNote(note.apply {
                this.title = title
                this.text = textValue.text
                this.color = color
            })
        }

        BackHandler {
            triggerSave()
            onBack()
        }

        DisposableEffect(Unit) {
            onDispose {
                triggerSave()
            }
        }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                uri?.let {
                    val markdownImage = "\n![image]($it)\n"
                    val selection = textValue.selection
                    val newText = textValue.text.substring(0, selection.start) + 
                                markdownImage + 
                                textValue.text.substring(selection.end)
                    textValue = TextFieldValue(newText)
                }
            }
        )

        val containerColor = themedContainerColor(if (color != 0) Color(color) else MaterialTheme.colorScheme.surface)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) "Edit Note" else "View Note") },
                    navigationIcon = {
                        IconButton(onClick = {
                            triggerSave()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showColorPicker = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Change Color")
                        }
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(if (isEditing) Icons.Default.Visibility else Icons.Default.Edit, contentDescription = "Toggle Preview")
                        }
                        if (isEditing) {
                            IconButton(onClick = {
                                triggerSave()
                                isEditing = false
                            }) {
                                Icon(Icons.Default.Save, contentDescription = "Save")
                            }
                        }
                    }
                )
            },
            containerColor = containerColor
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (isEditing) {
                    FormattingToolbar(
                        onFormat = { prefix, suffix ->
                            val selection = textValue.selection
                            val newText = textValue.text.substring(0, selection.start) + 
                                        prefix + textValue.text.substring(selection.start, selection.end) + suffix +
                                        textValue.text.substring(selection.end)
                            textValue = TextFieldValue(newText)
                        },
                        onAddImage = { imagePickerLauncher.launch(arrayOf("image/*")) }
                    )
                    
                    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(R.string.title)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                if (newValue.text.length > textValue.text.length && 
                                    newValue.text.getOrNull(newValue.selection.start - 1) == '\n') {
                                    val lines = textValue.text.substring(0, textValue.selection.start).split('\n')
                                    val lastLine = lines.lastOrNull() ?: ""
                                    
                                    val prefix = when {
                                        lastLine.startsWith("* ") -> "* "
                                        lastLine.startsWith("- ") -> "- "
                                        lastLine.contains(Regex("^\\d+\\. ")) -> {
                                            val num = lastLine.substringBefore('.').toIntOrNull() ?: 0
                                            "${num + 1}. "
                                        }
                                        else -> ""
                                    }
                                    
                                    if (prefix.isNotEmpty()) {
                                        val newText = newValue.text.substring(0, newValue.selection.start) + 
                                                    prefix + newValue.text.substring(newValue.selection.start)
                                        textValue = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(newValue.selection.start + prefix.length))
                                        return@OutlinedTextField
                                    }
                                }
                                textValue = newValue
                            },
                            label = { Text("Content (Markdown supported)") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp)
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                        Text(text = title, style = MaterialTheme.typography.headlineMedium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        MarkdownRenderer(text = textValue.text)
                    }
                }
            }
        }

        if (showColorPicker) {
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                title = { Text("Pick Note Color") },
                text = {
                    ColorPickerRow(selectedColor = color, onColorSelected = { 
                        color = it
                        showColorPicker = false
                    })
                },
                confirmButton = {
                    TextButton(onClick = { showColorPicker = false }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
fun FormattingToolbar(onFormat: (String, String) -> Unit, onAddImage: () -> Unit) {
    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = { onFormat("# ", "") }) { Icon(Icons.Default.Title, contentDescription = "H1") }
            IconButton(onClick = { onFormat("## ", "") }) { Icon(Icons.Default.Title, contentDescription = "H2", modifier = Modifier.size(18.dp)) }
            IconButton(onClick = { onFormat("**", "**") }) { Icon(Icons.Default.FormatBold, contentDescription = "Bold") }
            IconButton(onClick = { onFormat("* ", "") }) { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Bullet") }
            IconButton(onClick = { onFormat("1. ", "") }) { Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered List") }
            IconButton(onClick = { onAddImage() }) { Icon(Icons.Default.Image, contentDescription = "Add Image") }
        }
    }
}

@Composable
fun MarkdownRenderer(text: String) {
    val lines = text.split('\n')
    Column {
        lines.forEach { line ->
            when {
                line.startsWith("# ") -> Text(line.substring(2), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                line.startsWith("## ") -> Text(line.substring(3), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                line.startsWith("### ") -> Text(line.substring(4), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                line.startsWith("* ") || line.startsWith("- ") -> {
                    Row {
                        Text("• ", style = MaterialTheme.typography.bodyLarge)
                        Text(line.substring(2), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                line.contains(Regex("^\\d+\\. ")) -> {
                    Text(line, style = MaterialTheme.typography.bodyLarge)
                }
                line.startsWith("![image](") && line.endsWith(")") -> {
                    val uri = line.substring(9, line.length - 1)
                    Text("[Image: $uri]", color = MaterialTheme.colorScheme.primary, fontStyle = FontStyle.Italic)
                }
                else -> {
                    // Simple bold parsing **text**
                    val annotatedString = buildAnnotatedString {
                        var current = line
                        while (current.contains("**")) {
                            val start = current.indexOf("**")
                            val next = current.indexOf("**", start + 2)
                            if (next == -1) break
                            
                            append(current.substring(0, start))
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(current.substring(start + 2, next))
                            }
                            current = current.substring(next + 2)
                        }
                        append(current)
                    }
                    Text(text = annotatedString, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
