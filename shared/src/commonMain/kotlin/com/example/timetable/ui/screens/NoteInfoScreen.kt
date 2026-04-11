package com.example.timetable.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.example.timetable.model.Note
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.theme.subtleThemedColor
import com.example.timetable.ui.viewmodel.NoteInfoViewModel

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

    var showColorPicker by remember { mutableStateOf(false) }
    var showFindReplace by remember { mutableStateOf(false) }
    var showOutline by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var formattingBarExpanded by remember { mutableStateOf(true) }
    var isSaved by remember { mutableStateOf(true) }

    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }

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
            NoteTopBar(
                title = title, onTitleChange = { title = it; isSaved = false },
                accentColor = accentColor,
                onBack = { triggerSave(); onBack() },
                onSave = { triggerSave() },
                onColorPicker = { showColorPicker = true },
                onFindReplace = { showFindReplace = !showFindReplace; showOutline = false },
                onOutline = { showOutline = !showOutline; showFindReplace = false },
                onStats = { showStats = true },
                titleFocusRequester = titleFocus
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = formattingBarExpanded) {
                    EnhancedFormattingToolbar(onFormat = onFormat)
                }
                NoteStatusBar(
                    wordCount = viewModel.wordCount, charCount = viewModel.charCount,
                    isSaved = isSaved, formattingBarExpanded = formattingBarExpanded,
                    onToggleToolbar = { formattingBarExpanded = !formattingBarExpanded },
                    accentColor = accentColor, containerColor = containerColor
                )
            }
        },
        containerColor = containerColor
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = showFindReplace) {
                FindReplacePanel(accentColor = accentColor,
                    onReplace = { o, n -> 
                        val r = textValue.text.replace(o, n)
                        textValue = TextFieldValue(r, TextRange(r.length))
                        isSaved = false 
                    },
                    onClose = { showFindReplace = false })
            }
            AnimatedVisibility(visible = showOutline) {
                OutlinePanel(text = textValue.text,
                    onHeadingClick = { pos ->
                        textValue = textValue.copy(selection = TextRange(pos))
                        showOutline = false
                    }, onClose = { showOutline = false })
            }
            PaperEditor(
                textValue = textValue,
                onTextChange = { textValue = it; isSaved = false },
                bodyFocusRequester = bodyFocus,
                accentColor = accentColor
            )
        }
    }

    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Note Color") },
            text = { ColorPickerRow(selectedColor = color, onColorSelected = { color = it; isSaved = false; showColorPicker = false }) },
            confirmButton = { TextButton(onClick = { showColorPicker = false }) { Text("Close") } }
        )
    }
    if (showStats) {
        AlertDialog(
            onDismissRequest = { showStats = false },
            title = { Text("Statistics") },
            text = {
                Column {
                    Text("Words: ${viewModel.wordCount}")
                    Text("Characters: ${viewModel.charCount}")
                }
            },
            confirmButton = { TextButton(onClick = { showStats = false }) { Text("Close") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteTopBar(
    title: String, onTitleChange: (String) -> Unit,
    accentColor: Color,
    onBack: () -> Unit, onSave: () -> Unit,
    onColorPicker: () -> Unit, onFindReplace: () -> Unit,
    onOutline: () -> Unit, onStats: () -> Unit,
    titleFocusRequester: FocusRequester
) {
    var showOverflow by remember { mutableStateOf(false) }
    Column {
        TopAppBar(
            title = {
                BasicTextField(
                    value = title, onValueChange = onTitleChange,
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                    singleLine = true,
                    cursorBrush = SolidColor(accentColor)
                )
            },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            actions = {
                IconButton(onClick = onOutline) { Icon(Icons.AutoMirrored.Filled.List, "Outline") }
                IconButton(onClick = onFindReplace) { Icon(Icons.Default.Search, "Find") }
                Box {
                    IconButton(onClick = { showOverflow = true }) { Icon(Icons.Default.MoreVert, "More") }
                    DropdownMenu(expanded = showOverflow, onDismissRequest = { showOverflow = false }) {
                        DropdownMenuItem(text = { Text("Color") }, onClick = { showOverflow = false; onColorPicker() }, leadingIcon = { Icon(Icons.Default.Palette, null) })
                        DropdownMenuItem(text = { Text("Save") }, onClick = { showOverflow = false; onSave() }, leadingIcon = { Icon(Icons.Default.Done, null) })
                        DropdownMenuItem(text = { Text("Statistics") }, onClick = { showOverflow = false; onStats() }, leadingIcon = { Icon(Icons.Default.Analytics, null) })
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        HorizontalDivider(color = accentColor.copy(alpha = 0.15f))
    }
}

@Composable
private fun PaperEditor(
    textValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    bodyFocusRequester: FocusRequester,
    accentColor: Color
) {
    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize().shadow(4.dp, RoundedCornerShape(12.dp)),
            color = Color.White, shape = RoundedCornerShape(12.dp)
        ) {
            Box(Modifier.fillMaxSize().drawBehind { 
                val step = 24.dp.toPx()
                var y = 16.dp.toPx() + step
                while (y < size.height) {
                    drawLine(Color(0xFFEEEEEE), Offset(0f, y), Offset(size.width, y), 1f)
                    y += step
                }
            }) {
                Box(Modifier.fillMaxHeight().width(2.dp).offset(x = 48.dp).background(accentColor.copy(alpha = 0.1f)))
                BasicTextField(
                    value = textValue,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxSize().padding(start = 56.dp, end = 16.dp, top = 16.dp).focusRequester(bodyFocusRequester),
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black, lineHeight = 24.sp),
                    cursorBrush = SolidColor(accentColor),
                    visualTransformation = NoteVisualTransformation()
                )
            }
        }
    }
}

@Composable
private fun EnhancedFormattingToolbar(
    onFormat: (String, String) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = { onFormat("**", "**") }) { Icon(Icons.Default.FormatBold, "Bold") }
            IconButton(onClick = { onFormat("*", "*") }) { Icon(Icons.Default.FormatItalic, "Italic") }
            IconButton(onClick = { onFormat("<u>", "</u>") }) { Icon(Icons.Default.FormatUnderlined, "Underline") }
            IconButton(onClick = { onFormat("~~", "~~") }) { Icon(Icons.Default.FormatStrikethrough, "Strike") }
            VerticalDivider(modifier = Modifier.height(24.dp))
            IconButton(onClick = { onFormat("# ", "") }) { Text("H1", fontWeight = FontWeight.Bold) }
            IconButton(onClick = { onFormat("## ", "") }) { Text("H2", fontWeight = FontWeight.Bold) }
            IconButton(onClick = { onFormat("* ", "") }) { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet") }
            IconButton(onClick = { onFormat("> ", "") }) { Icon(Icons.Default.FormatQuote, "Quote") }
        }
    }
}

@Composable
private fun NoteStatusBar(
    wordCount: Int, charCount: Int, isSaved: Boolean,
    formattingBarExpanded: Boolean, onToggleToolbar: () -> Unit,
    accentColor: Color, containerColor: Color
) {
    Surface(color = containerColor.copy(alpha = 0.9f)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$wordCount words", style = MaterialTheme.typography.labelSmall)
                Text("$charCount chars", style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isSaved) "Saved" else "Editing...", style = MaterialTheme.typography.labelSmall, color = if (isSaved) Color(0xFF4CAF50) else accentColor)
                IconButton(onClick = onToggleToolbar, modifier = Modifier.size(28.dp)) {
                    Icon(if (formattingBarExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, null)
                }
            }
        }
    }
}

@Composable
private fun FindReplacePanel(accentColor: Color, onReplace: (String, String) -> Unit, onClose: () -> Unit) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Find & Replace", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = onClose, Modifier.size(24.dp)) { Icon(Icons.Default.Close, null) }
            }
            OutlinedTextField(value = findText, onValueChange = { findText = it }, label = { Text("Find") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = replaceText, onValueChange = { replaceText = it }, label = { Text("Replace") }, modifier = Modifier.weight(1f), singleLine = true)
                Button(onClick = { onReplace(findText, replaceText) }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) { Text("Replace All") }
            }
        }
    }
}

@Composable
private fun OutlinePanel(text: String, onHeadingClick: (Int) -> Unit, onClose: () -> Unit) {
    val headings = remember(text) {
        val list = mutableListOf<Pair<String, Int>>()
        var pos = 0
        text.split('\n').forEach { line ->
            if (line.startsWith("#")) list.add(line to pos)
            pos += line.length + 1
        }
        list
    }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Outline", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = onClose, Modifier.size(24.dp)) { Icon(Icons.Default.Close, null) }
            }
            headings.forEach { (h, p) ->
                Text(h, modifier = Modifier.fillMaxWidth().clickable { onHeadingClick(p) }.padding(vertical = 4.dp), style = MaterialTheme.typography.bodySmall)
            }
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
                withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) { parseInlineSpans(line.substring(2)) }
            }
            line.startsWith("## ") -> {
                withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) { parseInlineSpans(line.substring(3)) }
            }
            line.startsWith("> ") -> {
                withStyle(SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic)) { parseInlineSpans(line.substring(2)) }
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
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                        i = end + 2
                    } else { append(text[i]); i++ }
                }
                remaining.startsWith("*") -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(text.substring(i + 1, end)) }
                        i = end + 1
                    } else { append(text[i]); i++ }
                }
                remaining.startsWith("<u>") -> {
                    val end = text.indexOf("</u>", i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append(text.substring(i + 3, end)) }
                        i = end + 4
                    } else { append(text[i]); i++ }
                }
                remaining.startsWith("~~") -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(text.substring(i + 2, end)) }
                        i = end + 2
                    } else { append(text[i]); i++ }
                }
                else -> { append(text[i]); i++ }
            }
        }
    }
}
