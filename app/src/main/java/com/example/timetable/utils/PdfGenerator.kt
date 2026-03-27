package com.example.timetable.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.*
import androidx.core.content.FileProvider
import com.example.timetable.model.Note
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateAndShareNote(context: Context, note: Note) {
        val subjectName = if (note.subjectId != -1) DbHelper(context).getSubjectName(note.subjectId) else null
        val file = generateNotePdf(context, note, subjectName)
        if (file != null) {
            shareFile(context, file, "Share Note PDF")
        }
    }

    fun generateAndShareSubjectNotes(context: Context, subjectName: String, notes: List<Note>) {
        if (notes.isEmpty()) return
        val file = generateSubjectNotesPdf(context, subjectName, notes)
        if (file != null) {
            shareFile(context, file, "Share $subjectName Notes PDF")
        }
    }

    private fun generateNotePdf(context: Context, note: Note, subjectName: String?): File? {
        val pdfDocument = PdfDocument()
        drawNoteToPdf(context, pdfDocument, note, 1, subjectName)
        
        val file = File(context.cacheDir, "${note.title.replace(" ", "_")}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    private fun generateSubjectNotesPdf(context: Context, subjectName: String, notes: List<Note>): File? {
        val pdfDocument = PdfDocument()
        var startPage = 1
        for (note in notes) {
            startPage = drawNoteToPdf(context, pdfDocument, note, startPage, subjectName)
        }

        val file = File(context.cacheDir, "${subjectName.replace(" ", "_")}_Notes.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    private fun drawNoteToPdf(context: Context, pdfDocument: PdfDocument, note: Note, startPageNum: Int, subjectName: String?): Int {
        val paint = Paint()
        val textPaint = TextPaint().apply {
            textSize = 12f
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val pageWidth = 595
        val pageHeight = 842
        val margin = 50f
        val contentWidth = pageWidth - 2 * margin

        var pageNumber = startPageNum
        var myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var myPage = pdfDocument.startPage(myPageInfo)
        var canvas = myPage.canvas

        var currentY = margin

        // Draw Header
        paint.color = Color.DKGRAY
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Lectro Notes", margin, currentY, paint)

        // Draw Subject Name in Top Right (1st page of each note)
        if (!subjectName.isNullOrBlank()) {
            val subPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(subjectName, pageWidth - margin, currentY, subPaint)
        }

        currentY += 20f

        // Draw Note Color indicator
        if (note.color != 0) {
            paint.color = note.color
            canvas.drawRect(margin, currentY, margin + 10f, currentY + 30f, paint)
        }

        // Draw Title
        val titleLayout = StaticLayout.Builder.obtain(note.title, 0, note.title.length, textPaint.apply { 
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }, (contentWidth - 20f).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        
        canvas.save()
        canvas.translate(margin + 20f, currentY)
        titleLayout.draw(canvas)
        canvas.restore()
        currentY += titleLayout.height + 30f

        // Draw Divider
        paint.color = Color.LTGRAY
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 20f

        // Process content (Text and Images)
        textPaint.textSize = 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        val contentLines = note.text.split('\n')
        for (line in contentLines) {
            if (line.contains("[img:")) {
                val regex = Regex("\\[img:(.*?)]")
                val match = regex.find(line)
                if (match != null) {
                    val uriString = match.groupValues[1]
                    val bitmap = loadBitmapFromUri(context, uriString)
                    if (bitmap != null) {
                        // Calculate width-preserving scale
                        val scale = contentWidth / bitmap.width.toFloat()
                        val scaledHeight = bitmap.height * scale
                        
                        // New page if image doesn't fit
                        if (currentY + scaledHeight > pageHeight - margin - 30f) {
                            drawFooter(canvas, pageWidth, pageHeight, pageNumber, margin)
                            pdfDocument.finishPage(myPage)
                            pageNumber++
                            myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            myPage = pdfDocument.startPage(myPageInfo)
                            canvas = myPage.canvas
                            currentY = margin
                        }
                        
                        val destRect = RectF(margin, currentY, margin + contentWidth, currentY + scaledHeight)
                        canvas.drawBitmap(bitmap, null, destRect, null)
                        currentY += scaledHeight + 15f
                        bitmap.recycle()
                    }
                }
            } else if (line.trim() == "---") {
                // Horizontal Rule
                paint.color = Color.LTGRAY
                canvas.drawLine(margin, currentY + 10f, pageWidth - margin, currentY + 10f, paint)
                currentY += 20f
            } else {
                val spannable = parseMarkdownLine(line)
                if (spannable.isEmpty()) {
                    currentY += 12f // Spacing for empty lines (Enter)
                    continue
                }

                val lineLayout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, textPaint, contentWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(6f, 1.2f)
                    .build()

                var startL = 0
                while (startL < lineLayout.lineCount) {
                    val availH = pageHeight - margin - currentY - 30f
                    var linesInPage = 0
                    var heightInPage = 0f
                    
                    while (startL + linesInPage < lineLayout.lineCount) {
                        val h = lineLayout.getLineBottom(startL + linesInPage) - lineLayout.getLineTop(startL + linesInPage)
                        if (availH < heightInPage + h) break
                        heightInPage += h
                        linesInPage++
                    }

                    if (linesInPage == 0) {
                        drawFooter(canvas, pageWidth, pageHeight, pageNumber, margin)
                        pdfDocument.finishPage(myPage)
                        pageNumber++
                        myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        myPage = pdfDocument.startPage(myPageInfo)
                        canvas = myPage.canvas
                        currentY = margin
                        continue
                    }

                    canvas.save()
                    canvas.translate(margin, currentY)
                    canvas.clipRect(0f, 0f, contentWidth, heightInPage)
                    canvas.translate(0f, -lineLayout.getLineTop(startL).toFloat())
                    lineLayout.draw(canvas)
                    canvas.restore()

                    startL += linesInPage
                    currentY += heightInPage

                    if (startL < lineLayout.lineCount) {
                        drawFooter(canvas, pageWidth, pageHeight, pageNumber, margin)
                        pdfDocument.finishPage(myPage)
                        pageNumber++
                        myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        myPage = pdfDocument.startPage(myPageInfo)
                        canvas = myPage.canvas
                        currentY = margin
                    }
                }
                currentY += 2f // Extra gap after each newline processed
            }
        }

        drawFooter(canvas, pageWidth, pageHeight, pageNumber, margin)
        pdfDocument.finishPage(myPage)
        return pageNumber + 1
    }

    private fun parseMarkdownLine(line: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val current = line

        when {
            current.startsWith("# ") -> {
                appendStyled(builder, current.substring(2), AbsoluteSizeSpan(20, true), StyleSpan(Typeface.BOLD))
            }
            current.startsWith("## ") -> {
                appendStyled(builder, current.substring(3), AbsoluteSizeSpan(18, true), StyleSpan(Typeface.BOLD))
            }
            current.startsWith("### ") -> {
                appendStyled(builder, current.substring(4), AbsoluteSizeSpan(16, true), StyleSpan(Typeface.BOLD))
            }
            current.startsWith("> ") -> {
                appendStyled(builder, "│ ", ForegroundColorSpan(Color.parseColor("#9575CD")))
                appendStyled(builder, current.substring(2), StyleSpan(Typeface.ITALIC), ForegroundColorSpan(Color.DKGRAY))
            }
            current.startsWith("- [ ] ") -> {
                builder.append("☐ ")
                parseInlineMarkdown(builder, current.substring(6))
            }
            current.startsWith("- [x] ") || current.startsWith("- [X] ") -> {
                builder.append("☑ ")
                val start = builder.length
                parseInlineMarkdown(builder, current.substring(6))
                builder.setSpan(StrikethroughSpan(), start, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.setSpan(ForegroundColorSpan(Color.GRAY), start, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                parseInlineMarkdown(builder, current)
            }
        }
        return builder
    }

    private fun appendStyled(builder: SpannableStringBuilder, text: String, vararg spans: Any) {
        val start = builder.length
        builder.append(text)
        for (span in spans) {
            builder.setSpan(span, start, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun parseInlineMarkdown(builder: SpannableStringBuilder, text: String) {
        var i = 0
        while (i < text.length) {
            val remaining = text.substring(i)
            when {
                remaining.startsWith("**") -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        appendStyled(builder, text.substring(i + 2, end), StyleSpan(Typeface.BOLD))
                        i = end + 2
                    } else { builder.append(text[i]); i++ }
                }
                remaining.startsWith("*") -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        appendStyled(builder, text.substring(i + 1, end), StyleSpan(Typeface.ITALIC))
                        i = end + 1
                    } else { builder.append(text[i]); i++ }
                }
                remaining.startsWith("<u>") -> {
                    val end = text.indexOf("</u>", i + 3)
                    if (end != -1) {
                        appendStyled(builder, text.substring(i + 3, end), UnderlineSpan())
                        i = end + 4
                    } else { builder.append(text[i]); i++ }
                }
                remaining.startsWith("~~") -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        appendStyled(builder, text.substring(i + 2, end), StrikethroughSpan())
                        i = end + 2
                    } else { builder.append(text[i]); i++ }
                }
                remaining.startsWith("`") -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        appendStyled(builder, text.substring(i + 1, end), TypefaceSpan("monospace"), BackgroundColorSpan(Color.parseColor("#F0F0F5")))
                        i = end + 1
                    } else { builder.append(text[i]); i++ }
                }
                remaining.startsWith("[") -> {
                    val cb = text.indexOf("]", i + 1)
                    val op = if (cb != -1 && cb + 1 < text.length && text[cb + 1] == '(') cb + 1 else -1
                    val cp = if (op != -1) text.indexOf(")", op + 1) else -1
                    if (cb != -1 && op != -1 && cp != -1) {
                        val linkText = text.substring(i + 1, cb)
                        appendStyled(builder, linkText, ForegroundColorSpan(Color.parseColor("#1565C0")), UnderlineSpan())
                        i = cp + 1
                    } else { builder.append(text[i]); i++ }
                }
                else -> {
                    builder.append(text[i])
                    i++
                }
            }
        }
    }

    private fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun drawFooter(canvas: Canvas, width: Int, height: Int, pageNum: Int, margin: Float) {
        val paint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        canvas.drawText("Page $pageNum", width / 2f - 20f, height - margin / 2f, paint)
        canvas.drawText("Generated by Lectro", margin, height - margin / 2f, paint)
    }

    private fun shareFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
