package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.ChatMessageEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportUtils {

    fun exportChatToPdf(
        context: Context,
        chatTitle: String,
        messages: List<ChatMessageEntity>,
        analystName: String? = null
    ): File? {
        if (messages.isEmpty()) {
            Toast.makeText(context, "No messages to export", Toast.LENGTH_SHORT).show()
            return null
        }

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36
        val contentWidth = pageWidth - (margin * 2)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val paintHeaderTitle = Paint().apply {
            color = Color.parseColor("#00363A")
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val paintHeaderSub = Paint().apply {
            color = Color.parseColor("#00838F")
            textSize = 10f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val paintDivider = Paint().apply {
            color = Color.parseColor("#B2EBF2")
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        var y = margin.toFloat()

        fun drawPageHeader(c: Canvas, isFirstPage: Boolean) {
            y = margin.toFloat()
            // Header box accent
            val bgPaint = Paint().apply { color = Color.parseColor("#E0F7FA") }
            c.drawRect(margin.toFloat(), y, (pageWidth - margin).toFloat(), y + 44f, bgPaint)

            c.drawText("CYBERGUARD AI • TRANSCRIPT EXPORT", margin + 10f, y + 20f, paintHeaderTitle)
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val operator = analystName ?: "Lead SOC Analyst"
            c.drawText("Session: $chatTitle | Analyst: $operator | Generated: $timeStr", margin + 10f, y + 36f, paintHeaderSub)

            y += 54f
            c.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, paintDivider)
            y += 16f
        }

        fun drawPageFooter(c: Canvas, pageNum: Int) {
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 9f
                isAntiAlias = true
            }
            val footerText = "CyberGuard AI Security Terminal • Confidential Record • Page $pageNum"
            c.drawText(footerText, margin.toFloat(), (pageHeight - 20).toFloat(), footerPaint)
        }

        drawPageHeader(canvas, true)

        for (msg in messages) {
            val isUser = msg.role == "user"
            val senderLabel = if (isUser) "OPERATOR (${msg.toolType})" else "CYBERGUARD AI AGENT"
            val badgeColor = if (isUser) Color.parseColor("#0288D1") else Color.parseColor("#00897B")

            // Wrap text lines
            val lines = wrapText(msg.content, paintText, (contentWidth - 20).toFloat())
            val blockHeight = 24 + (lines.size * 14) + 12

            if (y + blockHeight > pageHeight - 40) {
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawPageHeader(canvas, false)
            }

            // Draw sender badge box
            val badgePaint = Paint().apply { color = badgeColor }
            canvas.drawRoundRect(
                margin.toFloat(),
                y,
                (margin + 160).toFloat(),
                y + 18f,
                4f,
                4f,
                badgePaint
            )

            val badgeTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            canvas.drawText(senderLabel, margin + 6f, y + 12f, badgeTextPaint)

            val timePaint = Paint().apply {
                color = Color.GRAY
                textSize = 8f
                isAntiAlias = true
            }
            val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(msg.timestamp))
            canvas.drawText(timeString, (margin + 170).toFloat(), y + 12f, timePaint)

            y += 22f

            // Message content box background
            val msgBgPaint = Paint().apply {
                color = if (isUser) Color.parseColor("#F1F8E9") else Color.parseColor("#F4F6F9")
            }
            val boxHeight = (lines.size * 14) + 10
            canvas.drawRoundRect(
                margin.toFloat(),
                y,
                (pageWidth - margin).toFloat(),
                y + boxHeight,
                6f,
                6f,
                msgBgPaint
            )

            var lineY = y + 14f
            for (line in lines) {
                canvas.drawText(line, margin + 10f, lineY, paintText)
                lineY += 14f
            }

            y += boxHeight + 14f
        }

        drawPageFooter(canvas, pageNumber)
        pdfDocument.finishPage(page)

        return saveAndSharePdf(context, pdfDocument, "CyberGuard_Chat_Export_${System.currentTimeMillis()}.pdf")
    }

    fun exportIncidentReportToPdf(
        context: Context,
        toolTitle: String,
        promptInput: String,
        codeInput: String?,
        analysisResult: String,
        analystName: String? = null
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36
        val contentWidth = pageWidth - (margin * 2)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val paintHeaderTitle = Paint().apply {
            color = Color.parseColor("#00363A")
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val paintSectionHeader = Paint().apply {
            color = Color.parseColor("#006064")
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val paintCode = Paint().apply {
            color = Color.parseColor("#1B5E20")
            textSize = 9.5f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val paintDivider = Paint().apply {
            color = Color.parseColor("#00838F")
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        var y = margin.toFloat()

        fun drawPageHeader(c: Canvas) {
            y = margin.toFloat()

            // Header Banner
            val bgPaint = Paint().apply { color = Color.parseColor("#E0F7FA") }
            c.drawRect(margin.toFloat(), y, (pageWidth - margin).toFloat(), y + 48f, bgPaint)

            c.drawText("CYBERGUARD AI • INCIDENT & THREAT REPORT", margin + 10f, y + 20f, paintHeaderTitle)

            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val operator = analystName ?: "Lead SOC Analyst"
            val metaStr = "Module: $toolTitle | Analyst: $operator | Date: $timeStr"
            val metaPaint = Paint().apply { color = Color.parseColor("#00838F"); textSize = 9f; typeface = Typeface.MONOSPACE }
            c.drawText(metaStr, margin + 10f, y + 38f, metaPaint)

            y += 58f
            c.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, paintDivider)
            y += 16f
        }

        fun drawPageFooter(c: Canvas, pageNum: Int) {
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 9f
                isAntiAlias = true
            }
            c.drawText("RESTRICTED - FOR INTERNAL SECURITY USE ONLY • Page $pageNum", margin.toFloat(), (pageHeight - 20).toFloat(), footerPaint)
        }

        fun checkPageOverflow(requiredHeight: Float) {
            if (y + requiredHeight > pageHeight - 40) {
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawPageHeader(canvas)
            }
        }

        drawPageHeader(canvas)

        // Section 1: Scope & Input
        checkPageOverflow(40f)
        canvas.drawText("1. THREAT SCOPE & ANALYSIS INPUT", margin.toFloat(), y, paintSectionHeader)
        y += 16f

        val promptLines = wrapText("Prompt / Threat Query: $promptInput", paintText, contentWidth.toFloat())
        for (line in promptLines) {
            checkPageOverflow(14f)
            canvas.drawText(line, margin.toFloat(), y, paintText)
            y += 14f
        }

        if (!codeInput.isNullOrBlank()) {
            y += 6f
            val codeLines = wrapText(codeInput, paintCode, (contentWidth - 20).toFloat())
            val codeBoxHeight = (codeLines.size * 13) + 12
            checkPageOverflow((codeBoxHeight + 10).toFloat())

            val codeBgPaint = Paint().apply { color = Color.parseColor("#F1F8E9") }
            canvas.drawRoundRect(margin.toFloat(), y, (pageWidth - margin).toFloat(), y + codeBoxHeight, 4f, 4f, codeBgPaint)

            var cy = y + 14f
            for (line in codeLines) {
                canvas.drawText(line, margin + 10f, cy, paintCode)
                cy += 13f
            }
            y += codeBoxHeight + 12f
        } else {
            y += 12f
        }

        // Section 2: AI Analysis & Findings
        checkPageOverflow(30f)
        canvas.drawLine(margin.toFloat(), y, (pageWidth - margin).toFloat(), y, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        y += 16f
        canvas.drawText("2. AI THREAT ASSESSMENT & FINDINGS", margin.toFloat(), y, paintSectionHeader)
        y += 18f

        // Clean markdown text for simple PDF layout
        val cleanResult = cleanMarkdownForPdf(analysisResult)
        val resultParagraphs = cleanResult.split("\n")

        for (para in resultParagraphs) {
            val trimmed = para.trim()
            if (trimmed.isEmpty()) {
                y += 6f
                continue
            }

            val isHeading = trimmed.startsWith("#") || trimmed.endsWith(":") || (trimmed.length < 50 && trimmed == trimmed.uppercase())
            val currentPaint = if (isHeading) paintBold else paintText
            val wrappedLines = wrapText(trimmed.replace("#", "").trim(), currentPaint, contentWidth.toFloat())

            for (wLine in wrappedLines) {
                checkPageOverflow(14f)
                canvas.drawText(wLine, margin.toFloat(), y, currentPaint)
                y += 14f
            }
            y += 4f
        }

        drawPageFooter(canvas, pageNumber)
        pdfDocument.finishPage(page)

        val cleanToolName = toolTitle.replace(" ", "_").lowercase()
        return saveAndSharePdf(context, pdfDocument, "CyberGuard_${cleanToolName}_Report_${System.currentTimeMillis()}.pdf")
    }

    private fun saveAndSharePdf(context: Context, pdfDocument: PdfDocument, fileName: String): File? {
        return try {
            val pdfDir = File(context.cacheDir, "pdf_exports")
            if (!pdfDir.exists()) pdfDir.mkdirs()

            val pdfFile = File(pdfDir, fileName)
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            Toast.makeText(context, "PDF Report Exported: ${pdfFile.name}", Toast.LENGTH_LONG).show()

            // Trigger Share Intent
            sharePdfFile(context, pdfFile)

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            pdfDocument.close()
            null
        }
    }

    fun sharePdfFile(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "CyberGuard AI Incident Report")
                putExtra(Intent.EXTRA_TEXT, "Attached is the security analysis report exported from CyberGuard AI Terminal.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Security PDF Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open share menu: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (para in paragraphs) {
            if (para.isEmpty()) {
                lines.add("")
                continue
            }
            val words = para.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = StringBuilder(testLine)
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine.toString())
                    }
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
            }
        }
        return lines
    }

    private fun cleanMarkdownForPdf(md: String): String {
        return md
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // bold
            .replace(Regex("\\*(.*?)\\*"), "$1") // italic
            .replace(Regex("`{3}[a-zA-Z]*\\n?"), "") // code fence
            .replace("`", "")
    }
}
