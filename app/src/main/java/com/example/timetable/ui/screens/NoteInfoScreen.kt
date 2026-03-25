package com.example.timetable.ui.screens

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
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
// ViewModel (unchanged interface, extended)
// ─────────────────────────────────────────────

class NoteInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DbHelper(application)
    var note by mutableStateOf<Note?>(null)
    var wordCount by mutableIntStateOf(0)
    var charCount by mutableIntStateOf(0)

    fun loadNote(id: Int) {
        note = db.getNote().find { it.id == id }
    }

    fun saveNote(note: Note) {
        db.updateNote(note)
    }

    fun updateCounts(text: String) {
        charCount = text.length
        wordCount = if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size
    }
}

// ─────────────────────────────────────────────
// Utility: Accent color extraction
// ─────────────────────────────────────────────

private fun noteAccentColor(color: Int): Color {
    return if (color != 0) {
        val base = Color(color)
        // Slightly darken/saturate for text/accent use
        Color(
            red = (base.red * 0.75f).coerceIn(0f, 1f),
            green = (base.green * 0.75f).coerceIn(0f, 1f),
            blue = (base.blue * 0.75f).coerceIn(0f, 1f),
            alpha = 1f
        )
    } else Color(0xFF5C6BC0) // indigo-ish default accent
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

    // ── State ──────────────────────────────────
    var title by remember(note) { mutableStateOf(note.title) }
    var textValue by remember(note) { mutableStateOf(TextFieldValue(note.text)) }
    var color by remember(note) { mutableIntStateOf(note.color) }

    var showColorPicker by remember { mutableStateOf(false) }
    var showFindReplace by remember { mutableStateOf(false) }
    var showOutline by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var formattingBarExpanded by remember { mutableStateOf(true) }
    var isSaved by remember { mutableStateOf(true) }
    var lastSavedTime by remember { mutableStateOf(System.currentTimeMillis()) }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    val bodyFocusRequester = remember { FocusRequester() }
    val clipboardManager = LocalClipboardManager.current

    // ── Derived ───────────────────────────────
    val bgColor = if (color != 0) Color(color) else MaterialTheme.colorScheme.surface
    val containerColor = subtleThemedColor(bgColor)
    val accentColor = noteAccentColor(color)
    val isDarkBg = remember(color) {
        if (color == 0) false else {
            val c = Color(color)
            (c.red * 0.299 + c.green * 0.587 + c.blue * 0.114) < 0.5f
        }
    }

    // ── Save logic ────────────────────────────
    val triggerSave = {
        viewModel.saveNote(note.apply {
            this.title = title
            this.text = textValue.text
            this.color = color
        })
        isSaved = true
        lastSavedTime = System.currentTimeMillis()
    }

    // Auto-save every 30 seconds when unsaved
    LaunchedEffect(isSaved) {
        if (!isSaved) {
            delay(30_000)
            triggerSave()
        }
    }

    // Word/char counts
    LaunchedEffect(textValue.text) {
        viewModel.updateCounts(textValue.text)
        isSaved = false
    }

    BackHandler {
        triggerSave()
        onBack()
    }

    DisposableEffect(Unit) {
        onDispose { triggerSave() }
    }

    // ── Image picker ──────────────────────────
    val currentTextValue by rememberUpdatedState(textValue)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val insertion = "\n[img:$it]\n"
                val start = currentTextValue.selection.start.coerceAtLeast(0)
                val end = currentTextValue.selection.end.coerceAtLeast(start)
                val newText = currentTextValue.text.substring(0, start) +
                        insertion + currentTextValue.text.substring(end)
                textValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(start + insertion.length)
                )
            }
        }
    )

    // ── UI ────────────────────────────────────
    Scaffold(
        topBar = {
            NoteTopBar(
                title = title,
                onTitleChange = { title = it; isSaved = false },
                isSaved = isSaved,
                accentColor = accentColor,
                onBack = { triggerSave(); onBack() },
                onSave = { triggerSave(); Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show() },
                onColorPicker = { showColorPicker = true },
                onFindReplace = { showFindReplace = !showFindReplace; showOutline = false },
                onOutline = { showOutline = !showOutline; showFindReplace = false },
                onStats = { showStats = true },
                onShare = {
                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        putExtra(android.content.Intent.EXTRA_TEXT, "**$title**\n\n${textValue.text}")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Note"))
                },
                titleFocusRequester = titleFocusRequester,
                containerColor = containerColor
            )
        },
        containerColor = containerColor,
        bottomBar = {
            Column {
                // Formatting toolbar
                AnimatedVisibility(
                    visible = formattingBarExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    EnhancedFormattingToolbar(
                        accentColor = accentColor,
                        onFormat = { prefix, suffix ->
                            isSaved = false
                            val selection = textValue.selection
                            val start = selection.min
                            val end = selection.max
                            val selectedText = textValue.text.substring(start, end)

                            // Strip conflicting alignment wrappers if inserting a new alignment tag
                            val alignmentPrefixes = listOf("<center>", "<right>")
                            val alignmentSuffixes = listOf("</center>", "</right>")
                            val isAlignmentOp = prefix in alignmentPrefixes

                            val cleanedSelected = if (isAlignmentOp) {
                                var s = selectedText
                                alignmentPrefixes.forEachIndexed { i, ap ->
                                    if (s.startsWith(ap) && s.endsWith(alignmentSuffixes[i])) {
                                        s = s.substring(ap.length, s.length - alignmentSuffixes[i].length)
                                    }
                                }
                                s
                            } else selectedText

                            val newText = textValue.text.substring(0, start) +
                                    prefix + cleanedSelected + suffix +
                                    textValue.text.substring(end)
                            textValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(start + prefix.length, start + prefix.length + cleanedSelected.length)
                            )
                        },
                        onAddImage = { imagePickerLauncher.launch(arrayOf("image/*")) }
                    )
                }

                // Status bar
                NoteStatusBar(
                    wordCount = viewModel.wordCount,
                    charCount = viewModel.charCount,
                    isSaved = isSaved,
                    formattingBarExpanded = formattingBarExpanded,
                    onToggleToolbar = { formattingBarExpanded = !formattingBarExpanded },
                    accentColor = accentColor,
                    containerColor = containerColor
                )

                Spacer(modifier = Modifier.imePadding())
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main editor column
            Column(modifier = Modifier.fillMaxSize()) {
                // Find & Replace panel
                AnimatedVisibility(
                    visible = showFindReplace,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    FindReplacePanel(
                        text = textValue.text,
                        accentColor = accentColor,
                        onReplace = { oldText, newText ->
                            val replaced = textValue.text.replace(oldText, newText)
                            textValue = TextFieldValue(replaced, TextRange(replaced.length))
                            isSaved = false
                        },
                        onClose = { showFindReplace = false }
                    )
                }

                // Outline panel
                AnimatedVisibility(
                    visible = showOutline,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinePanel(
                        text = textValue.text,
                        accentColor = accentColor,
                        onHeadingClick = { charPos ->
                            // Scroll to heading in body
                            scope.launch {
                                textValue = textValue.copy(selection = TextRange(charPos))
                                textLayoutResult?.let { layout ->
                                    if (charPos < layout.layoutInput.text.length) {
                                        val rect = layout.getBoundingBox(charPos)
                                        scrollState.animateScrollTo(rect.top.toInt())
                                    }
                                }
                            }
                            showOutline = false
                        },
                        onClose = { showOutline = false }
                    )
                }

                // Paper editor
                PaperEditor(
                    textValue = textValue,
                    onTextChange = { newValue ->
                        val old = textValue // capture stable snapshot
                        handleAutoList(newValue, old) { result ->
                            textValue = result
                        }
                        isSaved = false
                    },
                    scrollState = scrollState,
                    textLayoutResult = textLayoutResult,
                    onTextLayout = { textLayoutResult = it },
                    bodyFocusRequester = bodyFocusRequester,
                    accentColor = accentColor
                )
            }
        }
    }

    // ── Dialogs ───────────────────────────────
    if (showColorPicker) {
        NoteColorPickerDialog(
            selectedColor = color,
            onColorSelected = { color = it; isSaved = false },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showStats) {
        NoteStatsDialog(
            title = title,
            wordCount = viewModel.wordCount,
            charCount = viewModel.charCount,
            text = textValue.text,
            onDismiss = { showStats = false }
        )
    }
}

// ─────────────────────────────────────────────
// Top App Bar
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteTopBar(
    title: String,
    onTitleChange: (String) -> Unit,
    isSaved: Boolean,
    accentColor: Color,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onColorPicker: () -> Unit,
    onFindReplace: () -> Unit,
    onOutline: () -> Unit,
    onStats: () -> Unit,
    onShare: () -> Unit,
    titleFocusRequester: FocusRequester,
    containerColor: Color
) {
    var showOverflow by remember { mutableStateOf(false) }

    val saveIndicatorAlpha by animateFloatAsState(
        targetValue = if (!isSaved) 1f else 0f,
        animationSpec = tween(300),
        label = "saveIndicator"
    )

    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Unsaved dot indicator
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = saveIndicatorAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        cursorBrush = SolidColor(accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(titleFocusRequester),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (title.isEmpty()) {
                                Text(
                                    "Untitled Note",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.Gray.copy(alpha = 0.4f)
                                )
                            }
                            inner()
                        }
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Outline
                IconButton(onClick = onOutline) {
                    Icon(Icons.Default.List, contentDescription = "Outline", modifier = Modifier.size(22.dp))
                }
                // Find
                IconButton(onClick = onFindReplace) {
                    Icon(Icons.Default.Search, contentDescription = "Find & Replace", modifier = Modifier.size(22.dp))
                }
                // Overflow
                Box {
                    IconButton(onClick = { showOverflow = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Color") },
                            leadingIcon = { Icon(Icons.Default.Palette, null, modifier = Modifier.size(18.dp)) },
                            onClick = { showOverflow = false; onColorPicker() }
                        )
                        DropdownMenuItem(
                            text = { Text("Save") },
                            leadingIcon = { Icon(Icons.Default.Done, null, modifier = Modifier.size(18.dp)) },
                            onClick = { showOverflow = false; onSave() }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp)) },
                            onClick = { showOverflow = false; onShare() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Statistics") },
                            leadingIcon = { Icon(Icons.Outlined.Analytics, null, modifier = Modifier.size(18.dp)) },
                            onClick = { showOverflow = false; onStats() }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        HorizontalDivider(color = accentColor.copy(alpha = 0.15f), thickness = 1.dp)
    }
}

// ─────────────────────────────────────────────
// Paper-like Editor
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
    val scope = rememberCoroutineScope()

    // Auto-scroll: keep cursor visible as text grows
    LaunchedEffect(textValue.selection, textLayoutResult) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        val cursorPos = textValue.selection.start.coerceIn(0, layout.layoutInput.text.length)
        if (cursorPos < layout.layoutInput.text.length) {
            val cursorRect = layout.getCursorRect(cursorPos)
            // top/bottom of cursor in the full scrollable content (add top padding offset)
            val topPaddingPx = with(density) { 20.dp.toPx() }
            val cursorTop = (cursorRect.top + topPaddingPx).toInt()
            val cursorBottom = (cursorRect.bottom + topPaddingPx).toInt()
            // Visible window
            val visibleTop = scrollState.value
            val visibleBottom = visibleTop + scrollState.viewportSize
            when {
                cursorBottom > visibleBottom -> scope.launch {
                    scrollState.animateScrollTo(cursorBottom - scrollState.viewportSize + 80)
                }
                cursorTop < visibleTop -> scope.launch {
                    scrollState.animateScrollTo((cursorTop - 80).coerceAtLeast(0))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Paper card with subtle ruled lines
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                ),
            color = Color.White,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    // Subtle ruled-paper lines drawn behind text
                    .drawBehind { drawRuledLines(this) }
            ) {
                // Left margin accent bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .padding(start = 56.dp)
                        .background(accentColor.copy(alpha = 0.18f))
                )

                BasicTextField(
                    value = textValue,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 72.dp, end = 24.dp, top = 20.dp, bottom = 200.dp)
                        .heightIn(min = 700.dp)
                        .focusRequester(bodyFocusRequester),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A2E),
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.1.sp
                    ),
                    cursorBrush = SolidColor(accentColor),
                    onTextLayout = onTextLayout,
                    visualTransformation = EnhancedWYSIWYGTransformation(),
                    decorationBox = { inner ->
                        if (textValue.text.isEmpty()) {
                            Text(
                                "Start writing…",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Gray.copy(alpha = 0.35f),
                                    lineHeight = 28.sp
                                )
                            )
                        }
                        inner()
                    }
                )

                // Overlaid images
                textLayoutResult?.let { layout ->
                    val regex = Regex("\\[img:(.*?)\\]")
                    regex.findAll(textValue.text).forEach { match ->
                        val uri = match.groupValues[1]
                        val start = match.range.first
                        val safeStart = start.coerceIn(0, layout.layoutInput.text.length - 1)
                        if (start < layout.layoutInput.text.length && safeStart >= 0) {
                            val rect = layout.getBoundingBox(safeStart)
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = with(density) { (rect.left + 72.dp.toPx()).roundToInt() },
                                            y = with(density) { (rect.top + 20.dp.toPx()).roundToInt() }
                                        )
                                    }
                                    .widthIn(max = 280.dp)
                                    .heightIn(max = 200.dp)
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
    val lineHeight = with(scope) { 24.dp.toPx() }
    val startY = with(scope) { 20.dp.toPx() }
    var y = startY + lineHeight
    while (y < scope.size.height) {
        scope.drawLine(
            color = Color(0xFFE8EAF0),
            start = Offset(0f, y),
            end = Offset(scope.size.width, y),
            strokeWidth = 1f
        )
        y += lineHeight
    }
}

// ─────────────────────────────────────────────
// Enhanced Formatting Toolbar
// ─────────────────────────────────────────────

@Composable
private fun EnhancedFormattingToolbar(
    accentColor: Color,
    onFormat: (String, String) -> Unit,
    onAddImage: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            HorizontalDivider(color = accentColor.copy(alpha = 0.12f))

            // Tab row for toolbar sections
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("Text", "Structure", "Insert")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, label ->
                    val isSelected = index == selectedTab
                    val underlineWidth by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        label = "tab_underline"
                    )
                    TextButton(
                        onClick = { selectedTab = index },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) accentColor else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .fillMaxWidth(underlineWidth)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(accentColor)
                            )
                        }
                    }
                }
            }

            // Toolbar content per tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Text formatting
                        FmtBtn(Icons.Default.FormatBold, "Bold") { onFormat("**", "**") }
                        FmtBtn(Icons.Default.FormatItalic, "Italic") { onFormat("*", "*") }
                        FmtBtn(Icons.Default.FormatUnderlined, "Underline") { onFormat("<u>", "</u>") }
                        FmtBtn(Icons.Default.FormatStrikethrough, "Strike") { onFormat("~~", "~~") }
                        ToolbarDivider()
                        FmtBtn(Icons.Default.Title, "H1", labelText = "H1") { onFormat("# ", "") }
                        FmtBtn(Icons.Default.Title, "H2", labelText = "H2", iconSize = 18.dp) { onFormat("## ", "") }
                        FmtBtn(Icons.Default.Title, "H3", labelText = "H3", iconSize = 14.dp) { onFormat("### ", "") }
                    }
                    1 -> {
                        // Structure / alignment
                        FmtBtn(Icons.Default.FormatAlignLeft, "Left") { onFormat("", "") }
                        FmtBtn(Icons.Default.FormatAlignCenter, "Center") { onFormat("<center>", "</center>") }
                        FmtBtn(Icons.Default.FormatAlignRight, "Right") { onFormat("<right>", "</right>") }
                        ToolbarDivider()
                        FmtBtn(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet") { onFormat("* ", "") }
                        FmtBtn(Icons.Default.FormatListNumbered, "Numbered") { onFormat("1. ", "") }
                        FmtBtn(Icons.Default.HorizontalRule, "Divider") { onFormat("\n---\n", "") }
                        ToolbarDivider()
                        FmtBtn(Icons.Default.FormatQuote, "Quote") { onFormat("> ", "") }
                        FmtBtn(Icons.Default.Code, "Code") { onFormat("`", "`") }
                    }
                    2 -> {
                        // Insert
                        FmtBtn(Icons.Default.Image, "Image") { onAddImage() }
                        FmtBtn(Icons.Default.Link, "Link") { onFormat("[", "](url)") }
                        FmtBtn(Icons.Default.CheckBox, "Checkbox") { onFormat("- [ ] ", "") }
                        FmtBtn(Icons.Default.TableChart, "Table") {
                            onFormat("\n| Col 1 | Col 2 |\n|-------|-------|\n| Cell  | Cell  |\n", "")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FmtBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    labelText: String? = null,
    iconSize: Dp = 22.dp,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
        if (labelText != null) {
            Text(
                labelText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontSize = when (iconSize) {
                    14.dp -> 10.sp
                    18.dp -> 12.sp
                    else -> 14.sp
                }
            )
        } else {
            Icon(icon, contentDescription = desc, modifier = Modifier.size(iconSize))
        }
    }
}

// ─────────────────────────────────────────────
// Status Bar (bottom)
// ─────────────────────────────────────────────

@Composable
private fun NoteStatusBar(
    wordCount: Int,
    charCount: Int,
    isSaved: Boolean,
    formattingBarExpanded: Boolean,
    onToggleToolbar: () -> Unit,
    accentColor: Color,
    containerColor: Color
) {
    Surface(color = containerColor.copy(alpha = 0.95f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusChip("$wordCount w")
                StatusChip("$charCount ch")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save indicator
                AnimatedContent(
                    targetState = isSaved,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "saveStatus"
                ) { saved ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (saved) Icons.Default.CheckCircle else Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (saved) Color(0xFF4CAF50) else accentColor.copy(alpha = 0.7f)
                        )
                        Text(
                            if (saved) "Saved" else "Editing",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (saved) Color(0xFF4CAF50) else accentColor.copy(alpha = 0.7f)
                        )
                    }
                }

                // Toggle formatting bar
                IconButton(
                    onClick = onToggleToolbar,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        if (formattingBarExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = "Toggle toolbar",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray.copy(alpha = 0.75f),
        fontSize = 11.sp
    )
}

// ─────────────────────────────────────────────
// Find & Replace Panel
// ─────────────────────────────────────────────

@Composable
private fun FindReplacePanel(
    text: String,
    accentColor: Color,
    onReplace: (String, String) -> Unit,
    onClose: () -> Unit
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    val matchCount = remember(findText, text) {
        if (findText.isBlank()) 0
        else Regex(Regex.escape(findText)).findAll(text).count()
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find & Replace", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text("Find") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = {
                        if (findText.isNotEmpty()) {
                            Text(
                                "$matchCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (matchCount > 0) accentColor else Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("Replace with") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = {
                        if (findText.isNotBlank()) {
                            onReplace(findText, replaceText)
                        }
                    },
                    enabled = findText.isNotBlank() && matchCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
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
private fun OutlinePanel(
    text: String,
    accentColor: Color,
    onHeadingClick: (Int) -> Unit,
    onClose: () -> Unit
) {
    data class HeadingEntry(val level: Int, val text: String, val charPos: Int)

    val headings = remember(text) {
        val result = mutableListOf<HeadingEntry>()
        var pos = 0
        text.split('\n').forEach { line ->
            when {
                line.startsWith("### ") -> result.add(HeadingEntry(3, line.substring(4), pos))
                line.startsWith("## ") -> result.add(HeadingEntry(2, line.substring(3), pos))
                line.startsWith("# ") -> result.add(HeadingEntry(1, line.substring(2), pos))
            }
            pos += line.length + 1
        }
        result
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Outline", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }

            if (headings.isEmpty()) {
                Text(
                    "No headings found. Use # H1, ## H2, ### H3.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                headings.forEach { heading ->
                    val indent = (heading.level - 1) * 16
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHeadingClick(heading.charPos) }
                            .padding(
                                start = indent.dp + 4.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(if (heading.level == 1) 14.dp else if (heading.level == 2) 11.dp else 9.dp)
                                .background(
                                    accentColor.copy(
                                        alpha = when (heading.level) { 1 -> 1f; 2 -> 0.7f; else -> 0.45f }
                                    ),
                                    RoundedCornerShape(1.dp)
                                )
                        )
                        Text(
                            heading.text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = when (heading.level) { 1 -> 14.sp; 2 -> 13.sp; else -> 12.sp },
                                fontWeight = if (heading.level == 1) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (heading.level == 1) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Stats Dialog
// ─────────────────────────────────────────────

@Composable
private fun NoteStatsDialog(
    title: String,
    wordCount: Int,
    charCount: Int,
    text: String,
    onDismiss: () -> Unit
) {
    val lineCount = text.lines().size
    val paragraphCount = text.split(Regex("\n{2,}")).count { it.isNotBlank() }
    val avgWordLength = if (wordCount > 0) {
        (text.filter { it.isLetterOrDigit() }.length.toFloat() / wordCount).let {
            "%.1f".format(it)
        }
    } else "0"
    // Estimate reading time at 200 words/min
    val readMinutes = if (wordCount < 200) "<1 min" else "${wordCount / 200} min"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Note Statistics", fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatRow("Words", "$wordCount")
                StatRow("Characters", "$charCount")
                StatRow("Lines", "$lineCount")
                StatRow("Paragraphs", "$paragraphCount")
                StatRow("Avg. word length", "$avgWordLength letters")
                StatRow("Est. reading time", readMinutes)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────────────────────────────────
// Color Picker Dialog (enhanced)
// ─────────────────────────────────────────────

@Composable
private fun NoteColorPickerDialog(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note Color", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Choose a background color for this note",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                ColorPickerRow(selectedColor = selectedColor, onColorSelected = {
                    onColorSelected(it)
                    onDismiss()
                })
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ─────────────────────────────────────────────
// Auto-list helper (extracted)
// ─────────────────────────────────────────────

private fun handleAutoList(
    newValue: TextFieldValue,
    oldValue: TextFieldValue,
    onResult: (TextFieldValue) -> Unit
) {
    // Only intercept when a newline was just inserted
    if (newValue.text.length <= oldValue.text.length) { onResult(newValue); return }
    val insertedChar = newValue.text.getOrNull(newValue.selection.start - 1)
    if (insertedChar != '\n') { onResult(newValue); return }

    // Find the line that was just completed (before the new newline)
    val textBeforeCursor = oldValue.text.substring(0, oldValue.selection.start.coerceAtMost(oldValue.text.length))
    val lines = textBeforeCursor.split('\n')
    val lastLine = lines.lastOrNull() ?: ""

    // Determine list prefix — ONLY if the line genuinely starts with a list marker
    val prefix: String = when {
        // Unordered bullet: "* text" or "- text" — must have content after marker to continue
        lastLine.matches(Regex("^\\s*\\* .+")) ->
            lastLine.substring(0, lastLine.indexOf("* ") + 2)

        lastLine.matches(Regex("^\\s*- \\[[ xX]\\] .+")) ->
            lastLine.substring(0, lastLine.indexOf("- [") + 6) // "- [ ] "

        lastLine.matches(Regex("^\\s*- .+")) ->
            lastLine.substring(0, lastLine.indexOf("- ") + 2)

        // Numbered list: "1. text" or "1.2. text"
        lastLine.matches(Regex("^\\s*\\d+(\\.\\d+)*\\. .+")) -> {
            val match = Regex("^(\\s*)(\\d+(\\.\\d+)*)\\. ").find(lastLine)
            if (match != null) {
                val indent = match.groupValues[1]
                val numbers = match.groupValues[2].split('.').map { it.toIntOrNull() ?: 0 }.toMutableList()
                numbers[numbers.lastIndex]++
                "$indent${numbers.joinToString(".")}. "
            } else ""
        }

        else -> ""
    }

    if (prefix.isEmpty()) { onResult(newValue); return }

    // If the previous line was ONLY the prefix (empty list item) → exit the list
    if (lastLine.trim() == prefix.trim()) {
        val removeFrom = textBeforeCursor.length - lastLine.length
        val newText = oldValue.text.substring(0, removeFrom) +
                "\n" + newValue.text.substring(newValue.selection.start)
        onResult(TextFieldValue(newText, selection = TextRange(removeFrom + 1)))
        return
    }

    // Continue the list: insert prefix after the newline
    val insertAt = newValue.selection.start
    val newText = newValue.text.substring(0, insertAt) + prefix + newValue.text.substring(insertAt)
    onResult(TextFieldValue(newText, selection = TextRange(insertAt + prefix.length)))
}

// ─────────────────────────────────────────────
// Shared small composables
// ─────────────────────────────────────────────

@Composable
private fun ToolbarDivider() {
    VerticalDivider(
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 2.dp),
        color = Color.Gray.copy(alpha = 0.25f)
    )
}

// ─────────────────────────────────────────────
// Enhanced WYSIWYG Transformation
// ─────────────────────────────────────────────

class EnhancedWYSIWYGTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            val lines = text.text.split('\n')
            lines.forEachIndexed { index, line ->
                appendLine(line)
                if (index < lines.size - 1) append("\n")
            }
        }
        // Rebuild with styles
        val result = buildAnnotatedString {
            val lines = text.text.split('\n')
            lines.forEachIndexed { index, line ->
                when {
                    // H1
                    line.startsWith("# ") -> {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("# ") }
                        withStyle(SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))) {
                            append(line.substring(2))
                        }
                    }
                    // H2
                    line.startsWith("## ") -> {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("## ") }
                        withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))) {
                            append(line.substring(3))
                        }
                    }
                    // H3
                    line.startsWith("### ") -> {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("### ") }
                        withStyle(SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D2D44))) {
                            append(line.substring(4))
                        }
                    }
                    // Blockquote
                    line.startsWith("> ") -> {
                        withStyle(SpanStyle(color = Color(0xFF9575CD), background = Color(0xFFF3E5F5))) {
                            append("│ ")
                        }
                        withStyle(SpanStyle(color = Color(0xFF7E57C2), fontStyle = FontStyle.Italic)) {
                            renderRichInline(line.substring(2))
                        }
                    }
                    // Horizontal rule
                    line.trim() == "---" -> {
                        withStyle(SpanStyle(background = Color(0xFFE0E0E0), color = Color.Transparent, fontSize = 2.sp)) {
                            append(line)
                        }
                    }
                    // Checkbox unchecked
                    line.startsWith("- [ ] ") -> {
                        withStyle(SpanStyle(color = Color(0xFF90A4AE))) { append("☐ ") }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("- [ ] ") }
                        renderRichInline(line.substring(6))
                    }
                    // Checkbox checked
                    line.startsWith("- [x] ") || line.startsWith("- [X] ") -> {
                        withStyle(SpanStyle(color = Color(0xFF4CAF50))) { append("☑ ") }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("- [x] ") }
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray)) {
                            append(line.substring(6))
                        }
                    }
                    // Image placeholder
                    line.contains(Regex("\\[img:(.*?)\\]")) -> {
                        withStyle(SpanStyle(fontSize = 160.sp, color = Color.Transparent)) { append(line) }
                    }
                    // Table row
                    line.trimStart().startsWith("|") -> {
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFFF5F5F5),
                            fontSize = 14.sp
                        )) {
                            append(line)
                        }
                    }
                    // Normal line with inline formatting
                    else -> {
                        // Determine alignment: check for wrapping tags (only outermost matters)
                        val alignment = when {
                            line.startsWith("<center>") -> TextAlign.Center
                            line.startsWith("<right>") -> TextAlign.Right
                            else -> TextAlign.Left
                        }
                        // Numbered list indent
                        val numMatch = Regex("^(\\s*)(\\d+(\\.\\d+)*\\. )").find(line)
                        val indent = if (numMatch != null) numMatch.groupValues[1].length * 12 else 0
                        withStyle(ParagraphStyle(
                            textAlign = alignment,
                            textIndent = if (indent > 0) TextIndent(firstLine = (indent + 12).sp, restLine = (indent + 24).sp) else null
                        )) {
                            renderRichInline(line)
                        }
                    }
                }
                if (index < lines.size - 1) append("\n")
            }
        }
        return TransformedText(result, OffsetMapping.Identity)
    }

    private fun AnnotatedString.Builder.renderRichInline(line: String) {
        // Note: alignment tags (<center>, <right>) are already handled at the line level
        // in filter(). By the time renderRichInline is called, `line` may still contain
        // them (we pass the full line), but we must hide the tags and render the inner content.
        var current = line
        var startTag = ""; var endTag = ""

        // Strip outermost alignment wrapper (only exact full-line wrappers)
        when {
            current.startsWith("<center>") && current.endsWith("</center>") -> {
                startTag = "<center>"; endTag = "</center>"
            }
            current.startsWith("<right>") && current.endsWith("</right>") -> {
                startTag = "<right>"; endTag = "</right>"
            }
        }

        if (startTag.isNotEmpty()) {
            withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append(startTag) }
            current = current.substring(startTag.length, current.length - endTag.length)
        }

        var i = 0
        while (i < current.length) {
            when {
                // Bold **
                current.startsWith("**", i) -> {
                    val end = current.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("**") }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(current.substring(i + 2, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("**") }
                        i = end + 2
                    } else { append(current[i]); i++ }
                }
                // Italic *
                current.startsWith("*", i) && !current.startsWith("* ", i) -> {
                    val end = current.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("*") }
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(current.substring(i + 1, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("*") }
                        i = end + 1
                    } else { append(current[i]); i++ }
                }
                // Bullet list item
                current.startsWith("* ", i) && i == 0 -> {
                    withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("* ") }
                    withStyle(SpanStyle()) { append("• ") }
                    i += 2
                }
                current.startsWith("- ", i) && i == 0 -> {
                    withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("- ") }
                    withStyle(SpanStyle()) { append("• ") }
                    i += 2
                }
                // Underline
                current.startsWith("<u>", i) -> {
                    val end = current.indexOf("</u>", i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("<u>") }
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append(current.substring(i + 3, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("</u>") }
                        i = end + 4
                    } else { append(current[i]); i++ }
                }
                // Strikethrough
                current.startsWith("~~", i) -> {
                    val end = current.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("~~") }
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(current.substring(i + 2, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("~~") }
                        i = end + 2
                    } else { append(current[i]); i++ }
                }
                // Inline code
                current.startsWith("`", i) -> {
                    val end = current.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("`") }
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFFF0F0F5),
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )) { append(current.substring(i + 1, end)) }
                        withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append("`") }
                        i = end + 1
                    } else { append(current[i]); i++ }
                }
                else -> { append(current[i]); i++ }
            }
        }

        if (endTag.isNotEmpty()) {
            withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) { append(endTag) }
        }
    }
}