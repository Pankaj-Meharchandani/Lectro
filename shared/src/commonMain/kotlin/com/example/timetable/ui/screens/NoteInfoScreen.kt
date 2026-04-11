package com.example.timetable.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.example.timetable.model.Note
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.theme.subtleThemedColor
import com.example.timetable.ui.viewmodel.NoteInfoViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInfoScreen(
    noteId: Int,
    onBack: () -> Unit,
    viewModel: NoteInfoViewModel
) {
    LaunchedEffect(noteId) { viewModel.loadNote(noteId) }
    val note = viewModel.note ?: return

    var title by remember(note) { mutableStateOf(note.title) }
    var textValue by remember(note) { mutableStateOf(TextFieldValue(note.text)) }
    var color by remember(note) { mutableIntStateOf(note.color) }
    var isSaved by remember { mutableStateOf(true) }
    var formattingBarExpanded by remember { mutableStateOf(true) }

    val bgColor = if (color != 0) Color(color) else MaterialTheme.colorScheme.surface
    val containerColor = subtleThemedColor(bgColor)
    val accentColor = if (color != 0) Color(color) else MaterialTheme.colorScheme.primary

    val triggerSave = {
        viewModel.saveNote(note.copy(title = title, text = textValue.text, color = color))
        isSaved = true
    }

    LaunchedEffect(textValue.text) { 
        viewModel.updateCounts(textValue.text)
        isSaved = false 
    }

    val onFormat: (String, String) -> Unit = { prefix, suffix ->
        val start = textValue.selection.min
        val end = textValue.selection.max
        val selectedText = textValue.text.substring(start, end)
        val newText = textValue.text.substring(0, start) + prefix + selectedText + suffix + textValue.text.substring(end)
        textValue = TextFieldValue(newText, TextRange(start + prefix.length, start + prefix.length + selectedText.length))
        isSaved = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        value = title, 
                        onValueChange = { title = it; isSaved = false },
                        textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        cursorBrush = SolidColor(accentColor)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { triggerSave(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { triggerSave() }) {
                        Icon(Icons.Default.Done, contentDescription = "Save")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = formattingBarExpanded) {
                    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { onFormat("**", "**") }) { Icon(Icons.Default.FormatBold, "Bold") }
                            IconButton(onClick = { onFormat("*", "*") }) { Icon(Icons.Default.FormatItalic, "Italic") }
                            IconButton(onClick = { onFormat("<u>", "</u>") }) { Icon(Icons.Default.FormatUnderlined, "Underline") }
                            IconButton(onClick = { onFormat("~~", "~~") }) { Icon(Icons.Default.FormatStrikethrough, "Strike") }
                            IconButton(onClick = { onFormat("# ", "") }) { Text("H1", fontWeight = FontWeight.Bold) }
                            IconButton(onClick = { onFormat("## ", "") }) { Text("H2", fontWeight = FontWeight.Bold) }
                            IconButton(onClick = { onFormat("* ", "") }) { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet") }
                        }
                    }
                }
                Surface(color = containerColor.copy(alpha = 0.9f)) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("${viewModel.wordCount} words", style = MaterialTheme.typography.labelSmall)
                        IconButton(onClick = { formattingBarExpanded = !formattingBarExpanded }) {
                            Icon(if (formattingBarExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, null)
                        }
                    }
                }
            }
        },
        containerColor = containerColor
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            BasicTextField(
                value = textValue,
                onValueChange = { textValue = it; isSaved = false },
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp),
                cursorBrush = SolidColor(accentColor),
                visualTransformation = NoteVisualTransformation()
            )
        }
    }
}

class NoteVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val result = buildAnnotatedString {
            val lines = text.text.split('\n')
            lines.forEachIndexed { idx, line ->
                renderLine(line)
                if (idx < lines.size - 1) append('\n')
            }
        }
        return TransformedText(result, OffsetMapping.Identity)
    }

    private fun AnnotatedString.Builder.renderLine(line: String) {
        when {
            line.startsWith("# ") -> {
                withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                    parseInlineSpans(line.substring(2))
                }
            }
            line.startsWith("## ") -> {
                withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                    parseInlineSpans(line.substring(3))
                }
            }
            line.startsWith("> ") -> {
                withStyle(SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic)) {
                    parseInlineSpans(line.substring(2))
                }
            }
            else -> parseInlineSpans(line)
        }
    }

    private fun AnnotatedString.Builder.parseInlineSpans(text: String) {
        var i = 0
        while (i < text.length) {
            val remaining = text.substring(i)
            when {
                remaining.startsWith("**") -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else { append(text[i]); i++ }
                }
                remaining.startsWith("*") -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else { append(text[i]); i++ }
                }
                else -> { append(text[i]); i++ }
            }
        }
    }
}
