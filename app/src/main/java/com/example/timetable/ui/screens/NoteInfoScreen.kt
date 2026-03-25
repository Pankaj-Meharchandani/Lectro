package com.example.timetable.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.timetable.model.Note
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.theme.subtleThemedColor
import com.example.timetable.utils.DbHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ─────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────

class NoteInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var note      by mutableStateOf<Note?>(null)
    var wordCount by mutableIntStateOf(0)
    var charCount by mutableIntStateOf(0)

    fun loadNote(id: Int) { note = db.getNote().find { it.id == id } }
    fun saveNote(note: Note) { db.updateNote(note) }
    fun updateCounts(text: String) {
        charCount = text.length
        wordCount = if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size
    }
}

// ─────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────

private val ALIGN_OPEN  = listOf("<center>", "<right>")
private val ALIGN_CLOSE = listOf("</center>", "</right>")

// Inline tags that MUST be closed on the same line (Enter auto-closes them)
// Symmetric pairs: open == close; asymmetric: open != close
private val AUTO_CLOSE_TAGS = listOf(
    "**"  to "**",
    "*"   to "*",
    "~~"  to "~~",
    "<u>" to "</u>",
    "`"   to "`"
)

// ─────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────

private fun noteAccentColor(color: Int): Color =
    if (color != 0) {
        val b = Color(color)
        Color((b.red * .72f).coerceIn(0f,1f), (b.green * .72f).coerceIn(0f,1f), (b.blue * .72f).coerceIn(0f,1f))
    } else Color(0xFF5C6BC0)

data class FormattingState(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isCode: Boolean = false,
    val isBullet: Boolean = false,
    val isOrdered: Boolean = false,
    val alignment: TextAlign = TextAlign.Left
)

private fun getFormattingState(textValue: TextFieldValue): FormattingState {
    val text = textValue.text
    val selection = textValue.selection
    if (text.isEmpty()) return FormattingState()
    
    val cursor = selection.start
    val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)) + 1
    val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
    val line = text.substring(lineStart, lineEnd)
    
    val isBullet = line.trimStart().startsWith("* ") || line.trimStart().startsWith("- ")
    val isOrdered = line.trimStart().matches(Regex("^\\s*\\d+(\\.\\d+)*\\. .*"))
    
    val align = when {
        line.startsWith("<center>") -> TextAlign.Center
        line.startsWith("<right>")  -> TextAlign.Right
        else                        -> TextAlign.Left
    }

    val cursorInLine = cursor - lineStart
    fun isInside(open: String, close: String): Boolean {
        var lastIdx = 0
        while (true) {
            val start = line.indexOf(open, lastIdx)
            if (start == -1 || start >= cursorInLine) return false
            val end = line.indexOf(close, start + open.length)
            if (end == -1 || end >= cursorInLine) return true
            lastIdx = end + close.length
        }
    }

    return FormattingState(
        isBold = isInside("**", "**"),
        isItalic = isInside("*", "*") && !isInside("**", "**"),
        isUnderline = isInside("<u>", "</u>"),
        isStrikethrough = isInside("~~", "~~"),
        isCode = isInside("`", "`"),
        isBullet = isBullet,
        isOrdered = isOrdered,
        alignment = align
    )
}

// ─────────────────────────────────────────────
// Text input processor
// ─────────────────────────────────────────────

/**
 * Called on every keystroke. Handles two special cases when Enter is pressed:
 *
 * A) Auto-close unclosed inline tags on the current line.
 *    e.g.  **hello  →  **hello**\n
 *
 * B) List continuation / exit.
 *    "* item" + Enter  →  "* item\n* "
 *    "* "     + Enter  →  "\n"   (empty item exits list)
 */
private fun processTextInput(newValue: TextFieldValue, oldValue: TextFieldValue): TextFieldValue {
    if (newValue.text.length <= oldValue.text.length) return newValue
    val cursorAfter = newValue.selection.start
    if (newValue.text.getOrNull(cursorAfter - 1) != '\n') return newValue

    val oldCursor   = oldValue.selection.start.coerceAtMost(oldValue.text.length)
    val lineStart   = oldValue.text.lastIndexOf('\n', oldCursor - 1) + 1
    val currentLine = oldValue.text.substring(lineStart, oldCursor)

    // ── B: list & table prefix ─────────────────────────────────────────────
    val listPrefix: String = when {
        currentLine.matches(Regex("^\\s*\\* .+"))            ->
            currentLine.substring(0, currentLine.indexOf("* ") + 2)
        currentLine.matches(Regex("^\\s*- \\[[ xX]\\] .+")) ->
            currentLine.substring(0, currentLine.indexOf("- [") + 6)
        currentLine.matches(Regex("^\\s*- .+"))              ->
            currentLine.substring(0, currentLine.indexOf("- ") + 2)
        currentLine.matches(Regex("^\\s*\\d+(\\.\\d+)*\\. .+")) -> {
            val m = Regex("^(\\s*)(\\d+(\\.\\d+)*)\\. ").find(currentLine)
            if (m != null) {
                val indent  = m.groupValues[1]
                val nums    = m.groupValues[2].split('.').map { it.toIntOrNull() ?: 0 }.toMutableList()
                nums[nums.lastIndex]++
                "$indent${nums.joinToString(".")}. "
            } else ""
        }
        currentLine.trim().startsWith("|") && currentLine.indexOf('|', 1) != -1 -> {
            val pipeCount = currentLine.count { it == '|' }
            if (pipeCount >= 2) "| " + " | ".repeat(pipeCount - 2) + " |" else ""
        }
        else -> ""
    }

    // Empty list/table item → exit (remove marker, plain newline)
    val isTable = currentLine.trim().startsWith("|")
    val isEmptyMarker = if (isTable) currentLine.replace("|", "").replace(" ", "").isEmpty()
                        else currentLine.trim() == listPrefix.trim()

    if (listPrefix.isNotEmpty() && isEmptyMarker) {
        val newText = oldValue.text.substring(0, lineStart) + "\n" + newValue.text.substring(cursorAfter)
        return TextFieldValue(newText, TextRange(lineStart + 1))
    }

    // ── A: auto-close inline tags & persistence ────────────────────────────
    val closeSuffix = StringBuilder()
    val openPrefix  = StringBuilder()
    AUTO_CLOSE_TAGS.forEach { (open, close) ->
        val openCount  = countOccurrences(currentLine, open)
        val closeCount = if (open == close) 0 else countOccurrences(currentLine, close)
        val unclosed = if (open == close) (openCount % 2 != 0) else (openCount > closeCount)
        if (unclosed) {
            closeSuffix.append(close)
            openPrefix.append(open)
        }
    }
    // Alignment tags
    ALIGN_OPEN.forEachIndexed { i, op ->
        if (currentLine.trimStart().startsWith(op) && !currentLine.trimEnd().endsWith(ALIGN_CLOSE[i])) {
            closeSuffix.append(ALIGN_CLOSE[i])
            openPrefix.insert(0, op)
        }
    }

    // Clean up "redundant" tags (if user hits enter on an empty formatted line)
    if (currentLine.trim().isNotEmpty()) {
        val isJustTags = AUTO_CLOSE_TAGS.any { (op, cl) ->
            val t = currentLine.trim()
            t == op || t == "$op$cl"
        } || ALIGN_OPEN.any { currentLine.trim() == it } || ALIGN_OPEN.any { op -> ALIGN_CLOSE.any { cl -> currentLine.trim() == "$op$cl" } }
        
        if (isJustTags) {
            val newText = oldValue.text.substring(0, lineStart) + "\n" + newValue.text.substring(cursorAfter)
            return TextFieldValue(newText, TextRange(lineStart + 1))
        }
    }

    // Compose final text
    val beforeNewline = newValue.text.substring(0, cursorAfter - 1)
    val afterNewline  = newValue.text.substring(cursorAfter)
    val newText       = beforeNewline + closeSuffix.toString() + "\n" + listPrefix + openPrefix.toString() + afterNewline
    val newCursor     = cursorAfter + closeSuffix.length + listPrefix.length + openPrefix.length
    return TextFieldValue(newText, TextRange(newCursor))
}

private fun countOccurrences(text: String, sub: String): Int {
    var count = 0; var idx = 0
    while (true) { idx = text.indexOf(sub, idx); if (idx == -1) break; count++; idx += sub.length }
    return count
}

// ─────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInfoScreen(
    noteId: Int,
    onBack: () -> Unit,
    viewModel: NoteInfoViewModel = viewModel()
) {
    LaunchedEffect(noteId) { viewModel.loadNote(noteId) }
    val note = viewModel.note ?: return

    var title     by remember(note) { mutableStateOf(note.title) }
    var textValue by remember(note) { mutableStateOf(TextFieldValue(note.text)) }
    var color     by remember(note) { mutableIntStateOf(note.color) }

    var showColorPicker       by remember { mutableStateOf(false) }
    var showFindReplace       by remember { mutableStateOf(false) }
    var showOutline           by remember { mutableStateOf(false) }
    var showStats             by remember { mutableStateOf(false) }
    var formattingBarExpanded by remember { mutableStateOf(true) }
    var isSaved               by remember { mutableStateOf(true) }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState   = rememberScrollState()
    val scope         = rememberCoroutineScope()
    val context       = LocalContext.current
    val titleFocus    = remember { FocusRequester() }
    val bodyFocus     = remember { FocusRequester() }

    val bgColor        = if (color != 0) Color(color) else MaterialTheme.colorScheme.surface
    val containerColor = subtleThemedColor(bgColor)
    val accentColor    = noteAccentColor(color)

    val triggerSave = {
        viewModel.saveNote(note.apply { this.title = title; this.text = textValue.text; this.color = color })
        isSaved = true
    }

    LaunchedEffect(isSaved) { if (!isSaved) { delay(30_000); triggerSave() } }
    LaunchedEffect(textValue.text) { viewModel.updateCounts(textValue.text); isSaved = false }

    BackHandler { triggerSave(); onBack() }
    DisposableEffect(Unit) { onDispose { triggerSave() } }

    val currentTV by rememberUpdatedState(textValue)
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val ins   = "\n[img:$it]\n"
            val start = currentTV.selection.start.coerceAtLeast(0)
            val end   = currentTV.selection.end.coerceAtLeast(start)
            textValue = TextFieldValue(currentTV.text.substring(0, start) + ins + currentTV.text.substring(end), TextRange(start + ins.length))
        }
    }

    // ── Smart format inserter ──────────────────────────────────────────────
    val onFormat: (String, String) -> Unit = { prefix, suffix ->
        isSaved = false
        val full  = textValue.text
        val sel   = textValue.selection
        val start = sel.min
        val end   = sel.max

        val isAlignment = prefix in ALIGN_OPEN || (prefix == "" && suffix == "")

        if (isAlignment) {
            // Alignment: operate on the whole current line
            val lineStart   = full.lastIndexOf('\n', start - 1) + 1
            val lineEnd     = full.indexOf('\n', end).let { if (it == -1) full.length else it }
            var lineContent = full.substring(lineStart, lineEnd)

            // Strip any existing alignment wrapper
            val alreadyIdx = ALIGN_OPEN.indexOfFirst { lineContent.startsWith(it) }
            if (alreadyIdx != -1) {
                val op = ALIGN_OPEN[alreadyIdx]; val cl = ALIGN_CLOSE[alreadyIdx]
                lineContent = if (lineContent.endsWith(cl))
                    lineContent.substring(op.length, lineContent.length - cl.length)
                else lineContent.substring(op.length)
            }
            val newLine = if (prefix == "" || (alreadyIdx != -1 && ALIGN_OPEN[alreadyIdx] == prefix)) {
                lineContent // toggled off or switched to left
            } else {
                "$prefix$lineContent$suffix"
            }
            val newText   = full.substring(0, lineStart) + newLine + full.substring(lineEnd)
            val newCursor = (lineStart + newLine.length).coerceAtMost(newText.length)
            textValue = TextFieldValue(newText, TextRange(newCursor))
        } else {
            // All other formats: wrap selection or insert at cursor
            val selected = full.substring(start, end)
            val newText  = full.substring(0, start) + prefix + selected + suffix + full.substring(end)
            textValue = TextFieldValue(newText, TextRange(start + prefix.length, start + prefix.length + selected.length))
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            NoteTopBar(
                title = title, onTitleChange = { title = it; isSaved = false },
                isSaved = isSaved, accentColor = accentColor,
                onBack        = { triggerSave(); onBack() },
                onSave        = { triggerSave(); Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show() },
                onColorPicker = { showColorPicker = true },
                onFindReplace = { showFindReplace = !showFindReplace; showOutline = false },
                onOutline     = { showOutline = !showOutline; showFindReplace = false },
                onStats       = { showStats = true },
                onShare       = {
                    context.startActivity(android.content.Intent.createChooser(
                        android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            putExtra(android.content.Intent.EXTRA_TEXT, "$title\n\n${textValue.text}"); type = "text/plain"
                        }, "Share Note"))
                },
                titleFocusRequester = titleFocus, containerColor = containerColor
            )
        },
        containerColor = containerColor,
        bottomBar = {
            val fmtState = getFormattingState(textValue)
            Column {
                AnimatedVisibility(visible = formattingBarExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    EnhancedFormattingToolbar(accentColor = accentColor, fmtState = fmtState, onFormat = onFormat, onAddImage = { imageLauncher.launch(arrayOf("image/*")) })
                }
                NoteStatusBar(
                    wordCount = viewModel.wordCount, charCount = viewModel.charCount,
                    isSaved = isSaved, formattingBarExpanded = formattingBarExpanded,
                    onToggleToolbar = { formattingBarExpanded = !formattingBarExpanded },
                    accentColor = accentColor, containerColor = containerColor
                )
                Spacer(Modifier.imePadding())
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = showFindReplace, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                FindReplacePanel(text = textValue.text, accentColor = accentColor,
                    onReplace = { o, n -> val r = textValue.text.replace(o, n); textValue = TextFieldValue(r, TextRange(r.length)); isSaved = false },
                    onClose = { showFindReplace = false })
            }
            AnimatedVisibility(visible = showOutline, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                OutlinePanel(text = textValue.text, accentColor = accentColor,
                    onHeadingClick = { pos ->
                        scope.launch {
                            textValue = textValue.copy(selection = TextRange(pos))
                            textLayoutResult?.let { l -> if (pos < l.layoutInput.text.length) scrollState.animateScrollTo(l.getBoundingBox(pos).top.toInt()) }
                        }
                        showOutline = false
                    }, onClose = { showOutline = false })
            }
            PaperEditor(
                textValue    = textValue,
                onTextChange = { new -> textValue = processTextInput(new, textValue); isSaved = false },
                scrollState  = scrollState,
                textLayoutResult = textLayoutResult,
                onTextLayout = { textLayoutResult = it },
                bodyFocusRequester = bodyFocus,
                accentColor  = accentColor
            )
        }
    }

    if (showColorPicker) NoteColorPickerDialog(selectedColor = color, onColorSelected = { color = it; isSaved = false }, onDismiss = { showColorPicker = false })
    if (showStats) NoteStatsDialog(title = title, wordCount = viewModel.wordCount, charCount = viewModel.charCount, text = textValue.text, onDismiss = { showStats = false })
}

// ─────────────────────────────────────────────
// Top App Bar
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteTopBar(
    title: String, onTitleChange: (String) -> Unit,
    isSaved: Boolean, accentColor: Color,
    onBack: () -> Unit, onSave: () -> Unit,
    onColorPicker: () -> Unit, onFindReplace: () -> Unit,
    onOutline: () -> Unit, onStats: () -> Unit, onShare: () -> Unit,
    titleFocusRequester: FocusRequester, containerColor: Color
) {
    var showOverflow by remember { mutableStateOf(false) }
    val dotAlpha by animateFloatAsState(if (!isSaved) 1f else 0f, tween(300), label = "dot")

    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(accentColor.copy(alpha = dotAlpha)))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = title, onValueChange = onTitleChange,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold,
                            fontSize = 20.sp, letterSpacing = (-0.5).sp),
                        cursorBrush = SolidColor(accentColor),
                        modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (title.isEmpty()) Text("Untitled Note",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                color = Color.Gray.copy(alpha = 0.4f))
                            inner()
                        }
                    )
                }
            },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            actions = {
                IconButton(onClick = onOutline)     { Icon(Icons.Default.List,   "Outline", Modifier.size(22.dp)) }
                IconButton(onClick = onFindReplace) { Icon(Icons.Default.Search, "Find",    Modifier.size(22.dp)) }
                Box {
                    IconButton(onClick = { showOverflow = true }) { Icon(Icons.Default.MoreVert, "More") }
                    DropdownMenu(expanded = showOverflow, onDismissRequest = { showOverflow = false }) {
                        DropdownMenuItem(text = { Text("Color") },      leadingIcon = { Icon(Icons.Default.Palette,       null, Modifier.size(18.dp)) }, onClick = { showOverflow = false; onColorPicker() })
                        DropdownMenuItem(text = { Text("Save") },       leadingIcon = { Icon(Icons.Default.Done,          null, Modifier.size(18.dp)) }, onClick = { showOverflow = false; onSave() })
                        DropdownMenuItem(text = { Text("Share") },      leadingIcon = { Icon(Icons.Default.Share,         null, Modifier.size(18.dp)) }, onClick = { showOverflow = false; onShare() })
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Statistics") }, leadingIcon = { Icon(Icons.Outlined.Analytics,    null, Modifier.size(18.dp)) }, onClick = { showOverflow = false; onStats() })
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        HorizontalDivider(color = accentColor.copy(alpha = 0.15f), thickness = 1.dp)
    }
}

// ─────────────────────────────────────────────
// Paper Editor
// ─────────────────────────────────────────────

@Composable
private fun PaperEditor(
    textValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    scrollState: ScrollState,
    textLayoutResult: TextLayoutResult?,
    onTextLayout: (TextLayoutResult) -> Unit,
    bodyFocusRequester: FocusRequester,
    accentColor: Color
) {
    val density = LocalDensity.current
    val scope   = rememberCoroutineScope()

    // Auto-scroll to keep cursor visible
    LaunchedEffect(textValue.selection, textLayoutResult) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        if (layout.layoutInput.text.isEmpty()) return@LaunchedEffect
        val pos       = textValue.selection.start.coerceIn(0, layout.layoutInput.text.length - 1)
        val topPx     = with(density) { 20.dp.toPx() }
        val rect      = layout.getCursorRect(pos)
        val curTop    = (rect.top    + topPx).toInt()
        val curBottom = (rect.bottom + topPx).toInt()
        val visTop    = scrollState.value
        val visBottom = visTop + scrollState.viewportSize
        when {
            curBottom > visBottom -> scope.launch { scrollState.animateScrollTo(curBottom - scrollState.viewportSize + 80) }
            curTop    < visTop    -> scope.launch { scrollState.animateScrollTo((curTop - 80).coerceAtLeast(0)) }
        }
    }

    Box(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize().shadow(6.dp, RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(.08f), spotColor = Color.Black.copy(.12f)),
            color = Color.White, shape = RoundedCornerShape(12.dp)
        ) {
            Box(Modifier.fillMaxSize().verticalScroll(scrollState).drawBehind { drawRuledLines(this) }) {

                // Left margin accent bar
                Box(Modifier.fillMaxHeight().width(3.dp).offset(x = 56.dp).background(accentColor.copy(alpha = 0.18f)))

                BasicTextField(
                    value         = textValue,
                    onValueChange = onTextChange,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(start = 72.dp, end = 24.dp, top = 20.dp, bottom = 200.dp)
                        .heightIn(min = 700.dp)
                        .focusRequester(bodyFocusRequester)
                        .pointerInput(textValue) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val up = event.changes.find { it.changedToUp() }
                                    if (up != null) {
                                        val offset = up.position
                                        textLayoutResult?.let { layout ->
                                            val position = layout.getOffsetForPosition(offset)
                                            val text = textValue.text
                                            val lineStart = text.lastIndexOf('\n', (position - 1).coerceAtLeast(0)) + 1
                                            val lineEnd = text.indexOf('\n', position).let { if (it == -1) text.length else it }
                                            if (lineStart < text.length && lineEnd >= lineStart) {
                                                val line = text.substring(lineStart, lineEnd)
                                                if (line.startsWith("- [ ] ") || line.startsWith("- [x] ") || line.startsWith("- [X] ")) {
                                                    if (position >= lineStart && position < lineStart + 6) {
                                                        val isChecked = line.startsWith("- [x] ") || line.startsWith("- [X] ")
                                                        val newMarker = if (isChecked) "- [ ] " else "- [x] "
                                                        val newText = text.substring(0, lineStart) + newMarker + text.substring(lineStart + 6)
                                                        onTextChange(textValue.copy(text = newText))
                                                        up.consume()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF1A1A2E), lineHeight = 24.sp, letterSpacing = 0.1.sp),
                    cursorBrush  = SolidColor(accentColor),
                    onTextLayout = onTextLayout,
                    visualTransformation = remember { NoteVisualTransformation() },
                    decorationBox = { inner ->
                        if (textValue.text.isEmpty()) Text("Start writing…",
                            style = TextStyle(fontSize = 16.sp, color = Color.Gray.copy(.35f), lineHeight = 24.sp))
                        inner()
                    }
                )

                // Overlaid images — unchanged from original
                textLayoutResult?.let { layout ->
                    Regex("\\[img:(.*?)\\]").findAll(textValue.text).forEach { match ->
                        val uri     = match.groupValues[1]
                        val mStart  = match.range.first
                        val safePos = mStart.coerceIn(0, (layout.layoutInput.text.length - 1).coerceAtLeast(0))
                        if (layout.layoutInput.text.isNotEmpty() && mStart < layout.layoutInput.text.length) {
                            val rect = layout.getBoundingBox(safePos)
                            AsyncImage(
                                model = uri, contentDescription = null,
                                modifier = Modifier
                                    .offset { IntOffset(
                                        x = with(density) { (rect.left + 72.dp.toPx()).roundToInt() },
                                        y = with(density) { (rect.top  + 20.dp.toPx()).roundToInt() }
                                    )}
                                    .widthIn(max = 280.dp).heightIn(max = 200.dp)
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .shadow(3.dp, RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun drawRuledLines(scope: DrawScope) {
    val step   = with(scope) { 24.dp.toPx() }
    val startY = with(scope) { 20.dp.toPx() }
    var y = startY + step
    while (y < scope.size.height) {
        scope.drawLine(Color(0xFFECEEF5), Offset(0f, y), Offset(scope.size.width, y), 1f)
        y += step
    }
}

// ─────────────────────────────────────────────
// Formatting Toolbar
// ─────────────────────────────────────────────

@Composable
private fun EnhancedFormattingToolbar(
    accentColor: Color,
    fmtState: FormattingState,
    onFormat: (String, String) -> Unit,
    onAddImage: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Text", "Structure", "Insert")

    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Column {
            HorizontalDivider(color = accentColor.copy(alpha = 0.12f))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEachIndexed { idx, label ->
                    val isSel = idx == selectedTab
                    val uw by animateFloatAsState(if (isSel) 1f else 0f, label = "uw$idx")
                    TextButton(onClick = { selectedTab = idx }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, style = MaterialTheme.typography.labelMedium,
                                color = if (isSel) accentColor else Color.Gray,
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal)
                            Box(Modifier.height(2.dp).fillMaxWidth(uw).clip(RoundedCornerShape(1.dp)).background(accentColor))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        FmtBtn(Icons.Default.FormatBold,         "Bold",      isActive = fmtState.isBold)      { onFormat("**",   "**")   }
                        FmtBtn(Icons.Default.FormatItalic,       "Italic",    isActive = fmtState.isItalic)    { onFormat("*",    "*")    }
                        FmtBtn(Icons.Default.FormatUnderlined,   "Underline", isActive = fmtState.isUnderline) { onFormat("<u>",  "</u>") }
                        FmtBtn(Icons.Default.FormatStrikethrough,"Strike",    isActive = fmtState.isStrikethrough) { onFormat("~~",   "~~")   }
                        ToolbarDivider()
                        FmtBtn(null, "H1", labelText = "H1", labelSize = 14.sp) { onFormat("# ",   "") }
                        FmtBtn(null, "H2", labelText = "H2", labelSize = 12.sp) { onFormat("## ",  "") }
                        FmtBtn(null, "H3", labelText = "H3", labelSize = 10.sp) { onFormat("### ", "") }
                    }
                    1 -> {
                        val isLeft = fmtState.alignment == TextAlign.Left
                        val isCenter = fmtState.alignment == TextAlign.Center
                        val isRight = fmtState.alignment == TextAlign.Right
                        FmtBtn(Icons.Default.FormatAlignLeft,   "Left", isActive = isLeft)    { onFormat("",          "")           }
                        FmtBtn(Icons.Default.FormatAlignCenter, "Center", isActive = isCenter)  { onFormat("<center>",   "</center>")  }
                        FmtBtn(Icons.Default.FormatAlignRight,  "Right", isActive = isRight)   { onFormat("<right>",    "</right>")   }
                        ToolbarDivider()
                        FmtBtn(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet", isActive = fmtState.isBullet)   { onFormat("* ",  "") }
                        FmtBtn(Icons.Default.FormatListNumbered,             "Numbered", isActive = fmtState.isOrdered) { onFormat("1. ", "") }
                        FmtBtn(Icons.Default.HorizontalRule,                 "Divider")  { onFormat("\n---\n", "") }
                        ToolbarDivider()
                        FmtBtn(Icons.Default.FormatQuote, "Quote") { onFormat("> ", "") }
                        FmtBtn(Icons.Default.Code,        "Code", isActive = fmtState.isCode)  { onFormat("`",  "`") }
                    }
                    2 -> {
                        FmtBtn(Icons.Default.Image,     "Image")    { onAddImage() }
                        FmtBtn(Icons.Default.Link,      "Link")     { onFormat("[",       "](url)") }
                        FmtBtn(Icons.Default.CheckBox,  "Checkbox") { onFormat("- [ ] ", "")        }
                        FmtBtn(Icons.Default.TableChart,"Table") {
                            onFormat("\n| Header 1 | Header 2 | Header 3 |\n| :------- | :------: | -------: |\n| Cell     |   Cell   |     Cell |\n| Cell     |   Cell   |     Cell |\n", "")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FmtBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    desc: String,
    labelText: String? = null,
    labelSize: TextUnit = 14.sp,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick, 
        modifier = Modifier
            .size(42.dp)
            .background(if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
            .then(if (isActive) Modifier.shadow(1.dp, RoundedCornerShape(8.dp)) else Modifier)
    ) {
        when {
            labelText != null -> Text(labelText, fontWeight = FontWeight.Bold, fontSize = labelSize, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
            icon != null      -> Icon(icon, contentDescription = desc, modifier = Modifier.size(22.dp), tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
        }
    }
}

@Composable
private fun ToolbarDivider() =
    VerticalDivider(Modifier.height(20.dp).padding(horizontal = 2.dp), color = Color.Gray.copy(.25f))

// ─────────────────────────────────────────────
// Status Bar
// ─────────────────────────────────────────────

@Composable
private fun NoteStatusBar(
    wordCount: Int, charCount: Int, isSaved: Boolean,
    formattingBarExpanded: Boolean, onToggleToolbar: () -> Unit,
    accentColor: Color, containerColor: Color
) {
    Surface(color = containerColor.copy(.95f)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$wordCount w",  style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(.75f), fontSize = 11.sp)
                Text("$charCount ch", style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(.75f), fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedContent(targetState = isSaved, transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }, label = "save") { saved ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(if (saved) Icons.Default.CheckCircle else Icons.Outlined.Edit, null,
                            Modifier.size(12.dp), tint = if (saved) Color(0xFF4CAF50) else accentColor.copy(.7f))
                        Text(if (saved) "Saved" else "Editing", style = MaterialTheme.typography.labelSmall,
                            color = if (saved) Color(0xFF4CAF50) else accentColor.copy(.7f))
                    }
                }
                IconButton(onClick = onToggleToolbar, modifier = Modifier.size(28.dp)) {
                    Icon(if (formattingBarExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        "Toggle", Modifier.size(16.dp), tint = Color.Gray)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Find & Replace
// ─────────────────────────────────────────────

@Composable
private fun FindReplacePanel(text: String, accentColor: Color, onReplace: (String, String) -> Unit, onClose: () -> Unit) {
    var findText    by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    val matchCount  = remember(findText, text) { if (findText.isBlank()) 0 else Regex(Regex.escape(findText)).findAll(text).count() }

    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shadowElevation = 2.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Find & Replace", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onClose, Modifier.size(28.dp)) { Icon(Icons.Default.Close, "Close", Modifier.size(16.dp)) }
            }
            OutlinedTextField(value = findText, onValueChange = { findText = it }, label = { Text("Find") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                trailingIcon = { if (findText.isNotEmpty()) Text("$matchCount", style = MaterialTheme.typography.labelSmall,
                    color = if (matchCount > 0) accentColor else Color.Gray, modifier = Modifier.padding(end = 8.dp)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor), textStyle = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = replaceText, onValueChange = { replaceText = it }, label = { Text("Replace with") },
                    modifier = Modifier.weight(1f), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor), textStyle = MaterialTheme.typography.bodySmall)
                Button(onClick = { if (findText.isNotBlank()) onReplace(findText, replaceText) },
                    enabled = findText.isNotBlank() && matchCount > 0,
                    colors  = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Replace All", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Outline Panel
// ─────────────────────────────────────────────

@Composable
private fun OutlinePanel(text: String, accentColor: Color, onHeadingClick: (Int) -> Unit, onClose: () -> Unit) {
    data class H(val level: Int, val label: String, val pos: Int)
    val headings = remember(text) {
        val list = mutableListOf<H>(); var pos = 0
        text.split('\n').forEach { line ->
            when {
                line.startsWith("### ") -> list.add(H(3, line.substring(4), pos))
                line.startsWith("## ")  -> list.add(H(2, line.substring(3), pos))
                line.startsWith("# ")   -> list.add(H(1, line.substring(2), pos))
            }
            pos += line.length + 1
        }
        list
    }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shadowElevation = 2.dp) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Outline", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onClose, Modifier.size(28.dp)) { Icon(Icons.Default.Close, "Close", Modifier.size(16.dp)) }
            }
            if (headings.isEmpty()) {
                Text("No headings. Use # H1, ## H2, ### H3.", style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                headings.forEach { h ->
                    Row(Modifier.fillMaxWidth().clickable { onHeadingClick(h.pos) }
                        .padding(start = ((h.level - 1) * 16).dp + 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.width(3.dp)
                            .height(if (h.level == 1) 14.dp else if (h.level == 2) 11.dp else 9.dp)
                            .background(accentColor.copy(alpha = when(h.level) { 1 -> 1f; 2 -> 0.7f; else -> 0.45f }), RoundedCornerShape(1.dp)))
                        Text(h.label, style = MaterialTheme.typography.bodySmall.copy(
                            fontSize   = when(h.level) { 1 -> 14.sp; 2 -> 13.sp; else -> 12.sp },
                            fontWeight = if (h.level == 1) FontWeight.SemiBold else FontWeight.Normal),
                            color = MaterialTheme.colorScheme.onSurface.copy(if (h.level == 1) 1f else 0.75f))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Stats + Color Picker Dialogs
// ─────────────────────────────────────────────

@Composable
private fun NoteStatsDialog(title: String, wordCount: Int, charCount: Int, text: String, onDismiss: () -> Unit) {
    val lineCount  = text.lines().size
    val paraCount  = text.split(Regex("\n{2,}")).count { it.isNotBlank() }
    val avgWordLen = if (wordCount > 0) "%.1f".format(text.filter { it.isLetterOrDigit() }.length.toFloat() / wordCount) else "0"
    val readTime   = if (wordCount < 200) "<1 min" else "${wordCount / 200} min"
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Note Statistics", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Words" to "$wordCount", "Characters" to "$charCount", "Lines" to "$lineCount",
                    "Paragraphs" to "$paraCount", "Avg. word length" to "$avgWordLen ch", "Est. reading" to readTime
                ).forEach { (l, v) ->
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text(l, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(v, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun NoteColorPickerDialog(selectedColor: Int, onColorSelected: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Note Color", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose a background color for this note", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                ColorPickerRow(selectedColor = selectedColor, onColorSelected = { onColorSelected(it); onDismiss() })
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ─────────────────────────────────────────────
// WYSIWYG Visual Transformation
// ─────────────────────────────────────────────

class NoteVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val result = buildAnnotatedString {
            val lines = text.text.split('\n')
            lines.forEachIndexed { idx, line ->
                val nextLine = lines.getOrNull(idx + 1)
                renderLine(line, nextLine)
                if (idx < lines.size - 1) append('\n')
            }
        }
        return TransformedText(result, OffsetMapping.Identity)
    }

    private fun AnnotatedString.Builder.renderLine(line: String, nextLine: String? = null) {
        when {
            // ── Headings ──────────────────────────────────────────────────
            line.startsWith("### ") -> {
                withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("### ") }
                withStyle(SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D2D44))) {
                    parseInlineSpans(line.substring(4))
                }
            }
            line.startsWith("## ") -> {
                withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("## ") }
                withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E), letterSpacing = (-0.3).sp)) {
                    parseInlineSpans(line.substring(3))
                }
            }
            line.startsWith("# ") -> {
                withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("# ") }
                withStyle(SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), letterSpacing = (-0.5).sp)) {
                    parseInlineSpans(line.substring(2))
                }
            }

            // ── Blockquote ────────────────────────────────────────────────
            line.startsWith("> ") -> {
                withStyle(SpanStyle(color = Color(0xFF9575CD), background = Color(0xFFF3E5F5).copy(alpha = 0.4f))) { append("│") }
                append(" ")
                withStyle(SpanStyle(color = Color(0xFF7E57C2), fontStyle = FontStyle.Italic)) {
                    parseInlineSpans(line.substring(2))
                }
            }

            // ── Horizontal rule ───────────────────────────────────────────
            line.trim() == "---" -> {
                withStyle(SpanStyle(background = Color(0xFFE0E0E0), color = Color.Transparent, fontSize = 2.sp)) { append(line) }
            }

            // ── Checkbox ──────────────────────────────────────────────────
            line.startsWith("- [ ] ") -> {
                withStyle(SpanStyle(color = Color(0xFF90A4AE), fontSize = 16.sp)) { append("☐") }
                withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append(line.substring(1, 6)) }
                parseInlineSpans(line.substring(6))
            }
            line.length >= 6 && (line.substring(0, 6) == "- [x] " || line.substring(0, 6) == "- [X] ") -> {
                withStyle(SpanStyle(color = Color(0xFF4CAF50), fontSize = 16.sp)) { append("☑") }
                withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append(line.substring(1, 6)) }
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray)) {
                    parseInlineSpans(line.substring(6))
                }
            }

            // ── Image placeholder ─────────────────────────────────────────
            line.contains("[img:") -> {
                var last = 0
                val regex = Regex("\\[img:(.*?)\\]")
                regex.findAll(line).forEach { m ->
                    parseInlineSpans(line.substring(last, m.range.first))
                    // Using a small font size for the placeholder allows the user to control spacing with newlines
                    withStyle(SpanStyle(fontSize = 20.sp, color = Color.Transparent, letterSpacing = 0.sp)) { append(m.value) }
                    last = m.range.last + 1
                }
                parseInlineSpans(line.substring(last))
            }

            // ── Table ─────────────────────────────────────────────────────
            line.trimStart().startsWith("|") -> {
                val isHeader = nextLine?.let {
                    it.trim().startsWith("|") && it.replace("|", "").replace("-", "").replace(":", "").replace(" ", "").isEmpty()
                } ?: false
                renderTableRow(line, isHeader)
            }

            // ── Normal line ───────────────────────────────────────────────
            else -> {
                val alignment = when {
                    line.startsWith("<center>") -> TextAlign.Center
                    line.startsWith("<right>")  -> TextAlign.Right
                    else                        -> TextAlign.Left
                }
                withStyle(ParagraphStyle(textAlign = alignment)) {
                    renderInlineWithAlignStrip(line)
                }
            }
        }
    }

    private fun AnnotatedString.Builder.renderTableRow(line: String, isHeader: Boolean = false) {
        val isSep = line.replace("|", "").replace("-", "").replace(":", "").replace(" ", "").isEmpty()
        if (isSep) {
            withStyle(SpanStyle(color = Color.Transparent, fontSize = 2.sp, background = Color(0xFFDDE0E8))) { append(line) }
            return
        }
        line.forEach { char ->
            if (char == '|') {
                withStyle(SpanStyle(color = Color(0xFFCDD0DA), fontSize = 14.sp)) { append('|') }
            } else {
                withStyle(SpanStyle(fontSize = 14.sp, color = Color(0xFF1A1A2E), fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal)) { append(char) }
            }
        }
    }

    private fun AnnotatedString.Builder.renderInlineWithAlignStrip(line: String) {
        var current = line
        var startTag = ""
        var endTag  = ""

        ALIGN_OPEN.forEachIndexed { i, open ->
            if (current.startsWith(open)) {
                startTag = open
                val close = ALIGN_CLOSE[i]
                current = if (current.endsWith(close)) {
                    endTag = close
                    current.substring(open.length, current.length - close.length)
                } else {
                    current.substring(open.length)
                }
                return@forEachIndexed
            }
        }

        if (startTag.isNotEmpty()) withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append(startTag) }
        parseInlineSpans(current)
        if (endTag.isNotEmpty()) withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append(endTag) }
    }

    private fun AnnotatedString.Builder.parseInlineSpans(text: String) {
        var i = 0
        while (i < text.length) {
            val remaining = text.substring(i)
            when {
                // Bold
                remaining.startsWith("**") -> {
                    val end = text.indexOf("**", i + 2)
                    withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("**") }
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("**") }
                        i = end + 2
                    } else {
                        // Unclosed, but keep marker tiny to avoid layout jumps
                        i += 2
                    }
                }
                // Bullet
                remaining.startsWith("* ") && i == 0 -> {
                    append("•"); i++
                }
                remaining.startsWith("- ") && i == 0 -> {
                    append("•"); i++
                }
                // Italic
                remaining.startsWith("*") -> {
                    val end = text.indexOf("*", i + 1)
                    withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("*") }
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(text.substring(i + 1, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("*") }
                        i = end + 1
                    } else {
                        i++
                    }
                }
                // Underline
                remaining.startsWith("<u>") -> {
                    val end = text.indexOf("</u>", i + 3)
                    withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("<u>") }
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append(text.substring(i + 3, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("</u>") }
                        i = end + 4
                    } else {
                        i += 3
                    }
                }
                // Strikethrough
                remaining.startsWith("~~") -> {
                    val end = text.indexOf("~~", i + 2)
                    withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("~~") }
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(text.substring(i + 2, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("~~") }
                        i = end + 2
                    } else {
                        i += 2
                    }
                }
                // Inline code
                remaining.startsWith("`") -> {
                    val end = text.indexOf("`", i + 1)
                    withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("`") }
                    if (end != -1) {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFF0F0F5),
                            color = Color(0xFFD32F2F), fontSize = 14.sp)) { append(text.substring(i + 1, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("`") }
                        i = end + 1
                    } else {
                        i++
                    }
                }
                // Link
                remaining.startsWith("[") && !remaining.startsWith("[img:") -> {
                    val cb = text.indexOf("]", i + 1)
                    val op = if (cb != -1 && cb + 1 < text.length && text[cb + 1] == '(') cb + 1 else -1
                    val cp = if (op != -1) text.indexOf(")", op + 1) else -1
                    if (cb != -1 && op != -1 && cp != -1) {
                        val linkText = text.substring(i + 1, cb)
                        val url      = text.substring(op + 1, cp)
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("[") }
                        withStyle(SpanStyle(color = Color(0xFF1565C0), textDecoration = TextDecoration.Underline)) { append(linkText) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append("]($url)") }
                        i = cp + 1
                    } else { append(text[i]); i++ }
                }
                // Alignment and Heading tags in middle of text
                ALIGN_OPEN.any { remaining.startsWith(it) } || ALIGN_CLOSE.any { remaining.startsWith(it) } || 
                remaining.startsWith("### ") || remaining.startsWith("## ") || remaining.startsWith("# ") -> {
                    val tag = (ALIGN_OPEN + ALIGN_CLOSE + listOf("### ", "## ", "# ")).find { remaining.startsWith(it) }!!
                    withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.1.sp)) { append(tag) }
                    i += tag.length
                }
                else -> { append(text[i]); i++ }
            }
        }
    }
}