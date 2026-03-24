package com.example.timetable.ui.screens

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.timetable.R
import com.example.timetable.model.Note
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.theme.subtleThemedColor
import com.example.timetable.utils.DbHelper
import kotlin.math.roundToInt

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
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

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
            onDispose { triggerSave() }
        }

        val currentTextValue by rememberUpdatedState(textValue)
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                uri?.let {
                    val insertion = "\n[img:$it]\n"
                    val start = currentTextValue.selection.start
                    val end = currentTextValue.selection.end
                    val newText = currentTextValue.text.substring(0, start) + insertion + currentTextValue.text.substring(end)
                    textValue = TextFieldValue(
                        text = newText,
                        selection = TextRange(start + insertion.length)
                    )
                }
            }
        )

        val bgColor = if (color != 0) Color(color) else MaterialTheme.colorScheme.surface
        val containerColor = subtleThemedColor(bgColor)

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (title.isEmpty()) Text("Title", style = MaterialTheme.typography.titleLarge, color = Color.Gray.copy(alpha = 0.5f))
                                    innerTextField()
                                }
                            )
                        },
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
                                Icon(Icons.Default.Palette, contentDescription = "Color")
                            }
                            IconButton(onClick = { triggerSave() }) {
                                Icon(Icons.Default.Done, contentDescription = "Save")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    FormattingToolbar(
                        onFormat = { prefix, suffix ->
                            val selection = textValue.selection
                            val start = selection.min
                            val end = selection.max
                            val newText = textValue.text.substring(0, start) +
                                        prefix + textValue.text.substring(start, end) + suffix +
                                        textValue.text.substring(end)
                            textValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(start + prefix.length, end + prefix.length)
                            )
                        },
                        onAddImage = { imagePickerLauncher.launch(arrayOf("image/*")) }
                    )
                }
            },
            containerColor = containerColor,
            bottomBar = {
                Spacer(modifier = Modifier.imePadding())
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        BasicTextField(
                            value = textValue,
                            onValueChange = label@ { newValue: TextFieldValue ->
                                // Auto-list logic
                                if (newValue.text.length > textValue.text.length &&
                                    newValue.text.getOrNull(newValue.selection.start - 1) == '\n') {
                                    val lines = textValue.text.substring(0, textValue.selection.start).split('\n')
                                    val lastLine = lines.lastOrNull() ?: ""

                                    val prefix = when {
                                        lastLine.trim().startsWith("* ") -> lastLine.substring(0, lastLine.indexOf("* ") + 2)
                                        lastLine.trim().startsWith("- ") -> lastLine.substring(0, lastLine.indexOf("- ") + 2)
                                        lastLine.trim().contains(Regex("^\\d+(\\.\\d+)*\\. ")) -> {
                                            val match = Regex("^(\\s*)(\\d+(\\.\\d+)*)\\. ").find(lastLine)
                                            if (match != null) {
                                                val indent = match.groupValues[1]
                                                val numbers = match.groupValues[2].split('.').map { it.toIntOrNull() ?: 0 }.toMutableList()
                                                numbers[numbers.lastIndex]++
                                                indent + numbers.joinToString(".") + ". "
                                            } else ""
                                        }
                                        else -> ""
                                    }

                                    if (prefix.isNotEmpty()) {
                                        if (lastLine.trim() == prefix.trim()) {
                                            val newText = textValue.text.substring(0, textValue.selection.start - lastLine.length) +
                                                        "\n" + newValue.text.substring(newValue.selection.start)
                                            textValue = TextFieldValue(newText, selection = TextRange(textValue.selection.start - lastLine.length + 1))
                                        } else {
                                            val newText = newValue.text.substring(0, newValue.selection.start) +
                                                        prefix + newValue.text.substring(newValue.selection.start)
                                            textValue = TextFieldValue(newText, selection = TextRange(newValue.selection.start + prefix.length))
                                        }
                                        return@label
                                    }
                                }
                                textValue = newValue
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .heightIn(min = 1000.dp),
                            textStyle = TextStyle(
                                fontSize = 17.sp,
                                color = Color.DarkGray,
                                lineHeight = 24.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            onTextLayout = { textLayoutResult = it },
                            visualTransformation = WYSIWYGTransformation()
                        )

                        // Better image rendering (larger, centered)
                        textLayoutResult?.let { layout ->
                            val text = textValue.text
                            val regex = Regex("\\[img:(.*?)\\]")
                            regex.findAll(text).forEach { match ->
                                val uri = match.groupValues[1]
                                val start = match.range.first
                                if (start < layout.layoutInput.text.length) {
                                    val rect = layout.getBoundingBox(start)
                                    val density = LocalDensity.current
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .offset {
                                                IntOffset(
                                                    x = with(density) { (24.dp.toPx()).roundToInt() },
                                                    y = with(density) { (rect.top + 20.dp.toPx()).roundToInt() }
                                                )
                                            }
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .height(350.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF5F5F5))
                                            .clickable {
                                                textValue = textValue.copy(selection = TextRange(start))
                                            },
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showColorPicker) {
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                title = { Text("Note Color") },
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
    Column {
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ToolbarButton(Icons.Default.Title, "H1") { onFormat("# ", "") }
            ToolbarButton(Icons.Default.Title, "H2", iconSize = 18.dp) { onFormat("## ", "") }
            ToolbarButton(Icons.Default.Title, "H3", iconSize = 14.dp) { onFormat("### ", "") }
            ToolbarDivider()
            ToolbarButton(Icons.Default.FormatBold, "Bold") { onFormat("**", "**") }
            ToolbarButton(Icons.Default.FormatItalic, "Italic") { onFormat("*", "*") }
            ToolbarButton(Icons.Default.FormatUnderlined, "Underline") { onFormat("<u>", "</u>") }
            ToolbarButton(Icons.Default.FormatStrikethrough, "Strike") { onFormat("~~", "~~") }
            ToolbarDivider()
            ToolbarButton(Icons.Default.FormatAlignCenter, "Center") { onFormat("<center>", "</center>") }
            ToolbarButton(Icons.Default.FormatAlignRight, "Right") { onFormat("<right>", "</right>") }
            ToolbarDivider()
            ToolbarButton(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet") { onFormat("* ", "") }
            ToolbarButton(Icons.Default.FormatListNumbered, "Numbered") { onFormat("1. ", "") }
            ToolbarButton(Icons.Default.HorizontalRule, "Separator") { onFormat("\n---\n", "") }
            ToolbarButton(Icons.Default.Image, "Image") { onAddImage() }
        }
    }
}

@Composable
fun ToolbarButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, iconSize: androidx.compose.ui.unit.Dp = 24.dp, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(icon, contentDescription = desc, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun ToolbarDivider() {
    VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp), color = Color.Gray.copy(alpha = 0.2f))
}

class WYSIWYGTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styledText = buildAnnotatedString {
            val lines = text.text.split('\n')
            lines.forEachIndexed { index, line ->
                val trimmed = line.trim()
                when {
                    line.startsWith("# ") -> {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("# ") }
                        withStyle(SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)) { append(line.substring(2)) }
                    }
                    line.startsWith("## ") -> {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("## ") }
                        withStyle(SpanStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)) { append(line.substring(3)) }
                    }
                    line.startsWith("### ") -> {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("### ") }
                        withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)) { append(line.substring(4)) }
                    }
                    trimmed == "---" -> {
                        val padding = line.indexOf("---")
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append(line.substring(0, padding)) }
                        withStyle(SpanStyle(background = Color.Gray.copy(alpha = 0.2f), color = Color.Transparent, fontSize = 1.sp)) {
                            append("---")
                        }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append(line.substring(padding + 3)) }
                    }
                    line.contains(Regex("\\[img:(.*?)\\]")) -> {
                        // Massive reserved space for the image to be readable
                        withStyle(SpanStyle(fontSize = 350.sp, color = Color.Transparent)) {
                            append(line)
                        }
                    }
                    else -> {
                        val alignment = when {
                            line.startsWith("<center>") && line.endsWith("</center>") -> TextAlign.Center
                            line.startsWith("<right>") && line.endsWith("</right>") -> TextAlign.Right
                            else -> TextAlign.Left
                        }
                        val match = Regex("^(\\s*)(\\d+(\\.\\d+)*\\. )").find(line)
                        val indent = if (match != null) match.groupValues[1].length * 12 else 0
                        withStyle(ParagraphStyle(
                            textAlign = alignment,
                            textIndent = if (indent > 0) TextIndent(firstLine = (indent + 12).sp, restLine = (indent + 24).sp) else null
                        )) {
                            renderRichLine(line)
                        }
                    }
                }
                if (index < lines.size - 1) append("\n")
            }
        }
        return TransformedText(styledText, OffsetMapping.Identity)
    }

    private fun AnnotatedString.Builder.renderRichLine(line: String) {
        var current = line
        var startTag = ""
        var endTag = ""

        if (current.startsWith("<center>") && current.endsWith("</center>")) {
            startTag = "<center>"; endTag = "</center>"
        } else if (current.startsWith("<right>") && current.endsWith("</right>")) {
            startTag = "<right>"; endTag = "</right>"
        }

        if (startTag.isNotEmpty()) {
            withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append(startTag) }
            current = current.substring(startTag.length, current.length - endTag.length)
        }

        var i = 0
        while (i < current.length) {
            when {
                current.startsWith("**", i) -> {
                    val end = current.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("**") }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(current.substring(i + 2, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("**") }
                        i = end + 2
                    } else { append(current[i]); i++ }
                }
                current.startsWith("*", i) -> {
                    val end = current.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("*") }
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(current.substring(i + 1, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("*") }
                        i = end + 1
                    } else { append(current[i]); i++ }
                }
                current.startsWith("<u>", i) -> {
                    val end = current.indexOf("</u>", i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("<u>") }
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append(current.substring(i + 3, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("</u>") }
                        i = end + 4
                    } else { append(current[i]); i++ }
                }
                current.startsWith("~~", i) -> {
                    val end = current.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("~~") }
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(current.substring(i + 2, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append("~~") }
                        i = end + 2
                    } else { append(current[i]); i++ }
                }
                else -> { append(current[i]); i++ }
            }
        }

        if (endTag.isNotEmpty()) {
            withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.01.sp)) { append(endTag) }
        }
    }
}
