package com.example.timetable.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.timetable.model.Week
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

object PdfExportUtil {

    fun exportScheduleToPdf(context: Context, days: List<String>, scheduleData: Map<String, List<Week>>) {
        val pdfDocument = PdfDocument()
        
        // A4 size in points (72 dpi)
        val pageWidth = 595
        val pageHeight = 842
        
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint()
        
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Margin
        val margin = 40f
        var currentY = margin
        
        // Title
        canvas.drawText("Weekly Schedule", margin, currentY, titlePaint)
        currentY += 30f

        // Calculate time range
        var minMinutes = 24 * 60
        var maxMinutes = 0
        
        val allWeeks = scheduleData.values.flatten()
        if (allWeeks.isEmpty()) {
            Toast.makeText(context, "No schedule data to export", Toast.LENGTH_SHORT).show()
            pdfDocument.close()
            return
        }

        for (week in allWeeks) {
            val from = timeToMinutes(week.fromTime)
            val to = timeToMinutes(week.toTime)
            if (from < minMinutes) minMinutes = from
            if (to > maxMinutes) maxMinutes = to
        }

        // Round minMinutes down to hour, maxMinutes up to hour
        minMinutes = (minMinutes / 60) * 60
        maxMinutes = ((maxMinutes + 59) / 60) * 60
        
        val totalMinutes = maxMinutes - minMinutes
        if (totalMinutes <= 0) {
            Toast.makeText(context, "Invalid time range", Toast.LENGTH_SHORT).show()
            pdfDocument.close()
            return
        }

        val tableWidth = pageWidth - 2 * margin
        val tableHeight = pageHeight - currentY - margin
        
        val timeColumnWidth = 50f
        val dayColumnWidth = (tableWidth - timeColumnWidth) / days.size
        
        // Draw Table Headers (Days)
        var currentX = margin + timeColumnWidth
        for (day in days) {
            canvas.drawText(day.take(3), currentX + 5, currentY + 15, headerPaint)
            currentX += dayColumnWidth
        }
        
        currentY += 20f
        val tableTop = currentY
        
        // Draw Time labels and horizontal lines
        val totalHours = totalMinutes / 60
        val hourHeight = tableHeight / totalHours
        
        for (i in 0..totalHours) {
            val m = minMinutes + i * 60
            val y = tableTop + i * hourHeight
            canvas.drawText(minutesToTime(m), margin, y + 5, headerPaint)
            
            paint.color = Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(margin + timeColumnWidth, y, margin + tableWidth, y, paint)
        }

        // Draw Vertical lines
        paint.color = Color.BLACK
        canvas.drawLine(margin + timeColumnWidth, tableTop, margin + timeColumnWidth, tableTop + tableHeight, paint)
        for (i in 0..days.size) {
            val x = margin + timeColumnWidth + i * dayColumnWidth
            canvas.drawLine(x, tableTop, x, tableTop + tableHeight, paint)
        }
        canvas.drawLine(margin, tableTop, margin + tableWidth, tableTop, paint)
        canvas.drawLine(margin, tableTop + tableHeight, margin + tableWidth, tableTop + tableHeight, paint)

        // Draw Lectures
        val lecturePaint = Paint().apply {
            style = Paint.Style.FILL
        }
        val lectureTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            typeface = Typeface.DEFAULT
        }

        for (i in days.indices) {
            val day = days[i]
            val dayWeeks = scheduleData[day] ?: continue
            val x = margin + timeColumnWidth + i * dayColumnWidth
            
            for (week in dayWeeks) {
                val from = timeToMinutes(week.fromTime)
                val to = timeToMinutes(week.toTime)
                
                val top = tableTop + ((from - minMinutes) / 60f) * hourHeight
                val bottom = tableTop + ((to - minMinutes) / 60f) * hourHeight
                
                lecturePaint.color = if (week.color != 0) week.color else Color.BLUE
                canvas.drawRect(x + 2, top + 2, x + dayColumnWidth - 2, bottom - 2, lecturePaint)
                
                // Subject Name
                canvas.drawText(week.subject.take(10), x + 5, top + 12, lectureTextPaint)
                if (week.room.isNotEmpty()) {
                    canvas.drawText(week.room.take(10), x + 5, top + 22, lectureTextPaint)
                }
            }
        }

        pdfDocument.finishPage(page)

        savePdfToDownloads(context, pdfDocument)
    }

    private fun savePdfToDownloads(context: Context, pdfDocument: PdfDocument) {
        val fileName = "Schedule_${System.currentTimeMillis()}.pdf"
        
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                outputStream?.use { os ->
                    pdfDocument.writeTo(os)
                }
                Toast.makeText(context, "PDF saved to Downloads folder", Toast.LENGTH_LONG).show()
            } ?: throw Exception("Failed to create MediaStore entry")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun timeToMinutes(time: String?): Int {
        if (time == null || !time.contains(":")) return 0
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun minutesToTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }
}
