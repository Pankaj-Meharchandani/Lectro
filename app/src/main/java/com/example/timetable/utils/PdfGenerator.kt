package com.example.timetable.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.timetable.model.Note
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateAndShareNote(context: Context, note: Note) {
        val file = generateNotePdf(context, note)
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

    private fun generateNotePdf(context: Context, note: Note): File? {
        val pdfDocument = PdfDocument()
        drawNoteToPdf(pdfDocument, note, 1)
        
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
            startPage = drawNoteToPdf(pdfDocument, note, startPage)
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

    private fun drawNoteToPdf(pdfDocument: PdfDocument, note: Note, startPageNum: Int): Int {
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
        }, contentWidth.toInt())
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

        // Draw Content
        textPaint.textSize = 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val cleanText = note.text.replace(Regex("<[^>]*>"), "")
        
        val contentLayout = StaticLayout.Builder.obtain(cleanText, 0, cleanText.length, textPaint, contentWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1.2f)
            .build()

        val lines = contentLayout.lineCount
        var startLine = 0
        
        while (startLine < lines) {
            val availableHeight = pageHeight - margin - currentY - 30f
            var linesOnThisPage = 0
            var heightOnThisPage = 0f
            
            while (startLine + linesOnThisPage < lines) {
                val lineHeight = contentLayout.getLineBottom(startLine + linesOnThisPage) - contentLayout.getLineTop(startLine + linesOnThisPage)
                if (heightOnThisPage + lineHeight > availableHeight) break
                heightOnThisPage += lineHeight
                linesOnThisPage++
            }

            if (linesOnThisPage == 0) {
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
            canvas.clipRect(0f, 0f, contentWidth, heightOnThisPage)
            canvas.translate(0f, -contentLayout.getLineTop(startLine).toFloat())
            contentLayout.draw(canvas)
            canvas.restore()

            startLine += linesOnThisPage
            currentY += heightOnThisPage

            if (startLine < lines) {
                drawFooter(canvas, pageWidth, pageHeight, pageNumber, margin)
                pdfDocument.finishPage(myPage)
                pageNumber++
                myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                myPage = pdfDocument.startPage(myPageInfo)
                canvas = myPage.canvas
                currentY = margin
            }
        }

        drawFooter(canvas, pageWidth, pageHeight, pageNumber, margin)
        pdfDocument.finishPage(myPage)
        return pageNumber + 1
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
