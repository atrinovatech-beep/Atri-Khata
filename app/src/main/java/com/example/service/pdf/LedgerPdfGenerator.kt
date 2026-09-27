package com.example.service.pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.dao.AccountBalanceRow
import com.example.data.local.entity.LedgerEntry
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise PDF Generator Service for Frappe-style General Ledger and Trial Balance reports.
 * Uses Android's native PdfDocument, Canvas, and PrintManager to render publication-ready,
 * vector-sharp A4 financial statements.
 */
object LedgerPdfGenerator {

    // Standard A4 dimensions in typographic points (72 points = 1 inch)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_X = 36f
    private const val MARGIN_Y = 36f
    private const val USABLE_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)

    // Palette (Corporate Navy & Sky Blue)
    private val COLOR_NAVY_DARK = Color.rgb(15, 23, 42)      // #0F172A
    private val COLOR_NAVY_HEADER = Color.rgb(30, 41, 59)    // #1E293B
    private val COLOR_SKY_BLUE = Color.rgb(0, 163, 255)       // #00A3FF
    private val COLOR_ACCENT_ORANGE = Color.rgb(249, 115, 22) // #F97316
    private val COLOR_TEXT_MUTED = Color.rgb(100, 116, 139)  // #64748B
    private val COLOR_ROW_ALT = Color.rgb(248, 250, 252)     // #F8FAFC
    private val COLOR_BORDER = Color.rgb(226, 232, 240)       // #E2E8F0
    private val COLOR_GREEN = Color.rgb(22, 163, 74)          // #16A34A
    private val COLOR_RED = Color.rgb(220, 38, 38)            // #DC2626

    data class LedgerReportHeader(
        val businessName: String = "Atri Khata Business",
        val subtitle: String = "General Ledger & Double-Entry Statement",
        val panVat: String = "",
        val phone: String = "",
        val email: String = "",
        val address: String = "",
        val fiscalYear: String = "FY 2081/82",
        val datePeriod: String = "All Time",
        val accountFilter: String? = null,
        val partyFilter: String? = null,
        val generatedAt: Long = System.currentTimeMillis()
    )

    private val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    private fun formatAmount(amount: Double): String = "Rs. ${currencyFormat.format(amount)}"

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "ledger_reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // -------------------------------------------------------------------------
    // 1. GENERAL LEDGER STATEMENT PDF GENERATION
    // -------------------------------------------------------------------------
    fun generateGeneralLedgerPdf(
        context: Context,
        header: LedgerReportHeader,
        entries: List<LedgerEntry>
    ): File {
        val document = PdfDocument()
        val sortedList = entries.sortedBy { it.postingDateMillis }

        val totalDebits = sortedList.sumOf { it.debit }
        val totalCredits = sortedList.sumOf { it.credit }
        val netBalance = totalDebits - totalCredits

        val rowsPerPage = 20
        val totalPages = if (sortedList.isEmpty()) 1 else ((sortedList.size - 1) / rowsPerPage) + 1

        var currentEntryIndex = 0
        var runningBalance = 0.0

        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var cursorY = MARGIN_Y

            // Draw Header
            cursorY = drawHeader(
                canvas = canvas,
                header = header,
                reportTitle = "GENERAL LEDGER STATEMENT",
                badgeText = if (header.accountFilter != null) header.accountFilter.uppercase() else "ALL ACCOUNTS",
                startY = cursorY,
                isContinuation = pageIndex > 1
            )

            // Draw KPI Summary on First Page
            if (pageIndex == 1) {
                cursorY = drawLedgerSummaryCards(
                    canvas = canvas,
                    startY = cursorY,
                    totalDebits = totalDebits,
                    totalCredits = totalCredits,
                    netBalance = netBalance,
                    count = sortedList.size
                )
            }

            // Draw Table Header
            cursorY = drawLedgerTableHeader(canvas, cursorY)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 8.5f
                color = COLOR_NAVY_DARK
            }

            var rowCountOnThisPage = 0

            while (currentEntryIndex < sortedList.size && rowCountOnThisPage < rowsPerPage) {
                val entry = sortedList[currentEntryIndex]
                runningBalance += (entry.debit - entry.credit)

                val rowHeight = 22f
                val rowRect = RectF(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + rowHeight)

                if (rowCountOnThisPage % 2 == 1) {
                    paint.color = COLOR_ROW_ALT
                    canvas.drawRect(rowRect, paint)
                }

                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.6f
                canvas.drawLine(MARGIN_X, cursorY + rowHeight, MARGIN_X + USABLE_WIDTH, cursorY + rowHeight, paint)

                val colDate = if (entry.postingDateBS.isNotBlank()) entry.postingDateBS else entry.postingDateAD
                val colVoucher = truncate("${entry.voucherNo} (${entry.voucherType})", 22)
                val colAccount = truncate("${entry.account} [${entry.againstAccount}]", 24)
                val colParty = truncate(entry.partyName ?: "-", 16)
                val colDebit = if (entry.debit > 0) currencyFormat.format(entry.debit) else "-"
                val colCredit = if (entry.credit > 0) currencyFormat.format(entry.credit) else "-"
                val colBal = "${currencyFormat.format(Math.abs(runningBalance))} ${if (runningBalance >= 0) "Dr" else "Cr"}"

                val textY = cursorY + 14f

                // 1. Date
                textPaint.color = COLOR_TEXT_MUTED
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(colDate, MARGIN_X + 4f, textY, textPaint)

                // 2. Voucher
                textPaint.color = COLOR_NAVY_DARK
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(colVoucher, MARGIN_X + 75f, textY, textPaint)

                // 3. Account [Contra]
                textPaint.typeface = Typeface.DEFAULT
                textPaint.color = COLOR_NAVY_DARK
                canvas.drawText(colAccount, MARGIN_X + 185f, textY, textPaint)

                // 4. Party Name
                textPaint.color = COLOR_TEXT_MUTED
                canvas.drawText(colParty, MARGIN_X + 300f, textY, textPaint)

                // 5. Debit (Dr) - Right aligned
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.color = if (entry.debit > 0) COLOR_GREEN else COLOR_TEXT_MUTED
                canvas.drawText(colDebit, MARGIN_X + 400f, textY, textPaint)

                // 6. Credit (Cr) - Right aligned
                textPaint.color = if (entry.credit > 0) COLOR_RED else COLOR_TEXT_MUTED
                canvas.drawText(colCredit, MARGIN_X + 460f, textY, textPaint)

                // 7. Running Balance - Right aligned
                textPaint.color = COLOR_NAVY_DARK
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(colBal, MARGIN_X + USABLE_WIDTH - 4f, textY, textPaint)

                cursorY += rowHeight
                rowCountOnThisPage++
                currentEntryIndex++
            }

            // Draw Footer with Pagination
            drawFooter(canvas, pageIndex, totalPages)

            document.finishPage(page)
        }

        val outputFile = File(getReportsDir(context), "General_Ledger_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // 2. TRIAL BALANCE PDF GENERATION
    // -------------------------------------------------------------------------
    fun generateTrialBalancePdf(
        context: Context,
        header: LedgerReportHeader,
        rows: List<AccountBalanceRow>
    ): File {
        val document = PdfDocument()

        val totalDebits = rows.sumOf { it.totalDebit }
        val totalCredits = rows.sumOf { it.totalCredit }
        val isBalanced = Math.abs(totalDebits - totalCredits) < 0.01

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var cursorY = MARGIN_Y

        cursorY = drawHeader(
            canvas = canvas,
            header = header,
            reportTitle = "TRIAL BALANCE STATEMENT",
            badgeText = if (isBalanced) "BALANCED (Dr = Cr)" else "UNBALANCED",
            startY = cursorY,
            isContinuation = false
        )

        // Trial Balance Summary Cards
        cursorY = drawLedgerSummaryCards(
            canvas = canvas,
            startY = cursorY,
            totalDebits = totalDebits,
            totalCredits = totalCredits,
            netBalance = totalDebits - totalCredits,
            count = rows.size
        )

        // Table Header
        cursorY = drawTrialBalanceTableHeader(canvas, cursorY)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            color = COLOR_NAVY_DARK
        }

        rows.forEachIndexed { index, row ->
            val rowHeight = 22f
            val rowRect = RectF(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + rowHeight)

            if (index % 2 == 1) {
                paint.color = COLOR_ROW_ALT
                canvas.drawRect(rowRect, paint)
            }

            paint.color = COLOR_BORDER
            paint.strokeWidth = 0.6f
            canvas.drawLine(MARGIN_X, cursorY + rowHeight, MARGIN_X + USABLE_WIDTH, cursorY + rowHeight, paint)

            val textY = cursorY + 14f

            // Account Name
            textPaint.color = COLOR_NAVY_DARK
            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(row.account, MARGIN_X + 6f, textY, textPaint)

            // Account Type
            textPaint.color = COLOR_TEXT_MUTED
            textPaint.typeface = Typeface.DEFAULT
            canvas.drawText(row.accountType, MARGIN_X + 200f, textY, textPaint)

            // Total Debit
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = COLOR_GREEN
            canvas.drawText(currencyFormat.format(row.totalDebit), MARGIN_X + 330f, textY, textPaint)

            // Total Credit
            textPaint.color = COLOR_RED
            canvas.drawText(currencyFormat.format(row.totalCredit), MARGIN_X + 430f, textY, textPaint)

            // Net Balance
            textPaint.color = COLOR_NAVY_DARK
            textPaint.typeface = Typeface.DEFAULT_BOLD
            val balText = "${currencyFormat.format(Math.abs(row.balance))} ${if (row.balance >= 0) "Dr" else "Cr"}"
            canvas.drawText(balText, MARGIN_X + USABLE_WIDTH - 6f, textY, textPaint)

            cursorY += rowHeight
        }

        // Totals Row
        val totalRowHeight = 26f
        paint.color = COLOR_ROW_ALT
        canvas.drawRect(RectF(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + totalRowHeight), paint)

        paint.color = COLOR_NAVY_DARK
        paint.strokeWidth = 1.2f
        canvas.drawLine(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY, paint)
        canvas.drawLine(MARGIN_X, cursorY + totalRowHeight, MARGIN_X + USABLE_WIDTH, cursorY + totalRowHeight, paint)
        canvas.drawLine(MARGIN_X, cursorY + totalRowHeight - 3f, MARGIN_X + USABLE_WIDTH, cursorY + totalRowHeight - 3f, paint)

        val totalY = cursorY + 17f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.color = COLOR_NAVY_DARK
        canvas.drawText("TOTAL TRIAL BALANCE:", MARGIN_X + 6f, totalY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = COLOR_GREEN
        canvas.drawText(currencyFormat.format(totalDebits), MARGIN_X + 330f, totalY, textPaint)

        textPaint.color = COLOR_RED
        canvas.drawText(currencyFormat.format(totalCredits), MARGIN_X + 430f, totalY, textPaint)

        textPaint.color = if (isBalanced) COLOR_GREEN else COLOR_RED
        canvas.drawText(if (isBalanced) "BALANCED" else "DIFF: ${currencyFormat.format(Math.abs(totalDebits - totalCredits))}", MARGIN_X + USABLE_WIDTH - 6f, totalY, textPaint)

        drawFooter(canvas, 1, 1)

        document.finishPage(page)

        val outputFile = File(getReportsDir(context), "Trial_Balance_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // CANVAS DRAWING HELPERS
    // -------------------------------------------------------------------------

    private fun drawHeader(
        canvas: Canvas,
        header: LedgerReportHeader,
        reportTitle: String,
        badgeText: String,
        startY: Float,
        isContinuation: Boolean
    ): Float {
        var y = startY
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (!isContinuation) {
            // Brand Logo Box
            shapePaint.color = COLOR_NAVY_DARK
            canvas.drawRoundRect(RectF(MARGIN_X, y, MARGIN_X + 42f, y + 42f), 8f, 8f, shapePaint)

            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textSize = 14f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Atri", MARGIN_X + 21f, y + 22f, textPaint)

            textPaint.textSize = 8f
            textPaint.color = COLOR_SKY_BLUE
            canvas.drawText("ERP", MARGIN_X + 21f, y + 33f, textPaint)

            // Business Info
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textSize = 15f
            textPaint.color = COLOR_NAVY_DARK
            canvas.drawText(header.businessName, MARGIN_X + 50f, y + 15f, textPaint)

            textPaint.typeface = Typeface.DEFAULT
            textPaint.textSize = 8.5f
            textPaint.color = COLOR_TEXT_MUTED
            val addressText = listOfNotNull(header.address.ifBlank { null }, header.phone.ifBlank { null }).joinToString(" • ")
            if (addressText.isNotBlank()) {
                canvas.drawText(addressText, MARGIN_X + 50f, y + 27f, textPaint)
            }
            if (header.panVat.isNotBlank()) {
                canvas.drawText("PAN/VAT: ${header.panVat}  •  Fiscal Year: ${header.fiscalYear}", MARGIN_X + 50f, y + 39f, textPaint)
            }

            y += 54f
        }

        // Title Bar & Badge
        shapePaint.color = COLOR_NAVY_HEADER
        canvas.drawRoundRect(RectF(MARGIN_X, y, MARGIN_X + USABLE_WIDTH, y + 28f), 6f, 6f, shapePaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textSize = 11f
        canvas.drawText(reportTitle, MARGIN_X + 12f, y + 18f, textPaint)

        // Badge
        shapePaint.color = COLOR_SKY_BLUE
        val badgeWidth = textPaint.measureText(badgeText) + 16f
        canvas.drawRoundRect(
            RectF(MARGIN_X + USABLE_WIDTH - badgeWidth - 8f, y + 5f, MARGIN_X + USABLE_WIDTH - 8f, y + 23f),
            4f,
            4f,
            shapePaint
        )

        textPaint.color = Color.WHITE
        textPaint.textSize = 8.5f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(badgeText, MARGIN_X + USABLE_WIDTH - (badgeWidth / 2f) - 8f, y + 17f, textPaint)

        return y + 36f
    }

    private fun drawLedgerSummaryCards(
        canvas: Canvas,
        startY: Float,
        totalDebits: Double,
        totalCredits: Double,
        netBalance: Double,
        count: Int
    ): Float {
        val cardHeight = 44f
        val cardSpacing = 8f
        val cardWidth = (USABLE_WIDTH - (cardSpacing * 3)) / 4f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        data class CardItem(val title: String, val value: String, val color: Int)
        val cards = listOf(
            CardItem("TOTAL DEBITS (DR)", formatAmount(totalDebits), COLOR_GREEN),
            CardItem("TOTAL CREDITS (CR)", formatAmount(totalCredits), COLOR_RED),
            CardItem("NET POSITION", "${formatAmount(Math.abs(netBalance))} ${if (netBalance >= 0) "Dr" else "Cr"}", COLOR_NAVY_DARK),
            CardItem("TOTAL ENTRIES", "$count Postings", COLOR_SKY_BLUE)
        )

        cards.forEachIndexed { i, card ->
            val left = MARGIN_X + (i * (cardWidth + cardSpacing))
            val rect = RectF(left, startY, left + cardWidth, startY + cardHeight)

            paint.color = COLOR_ROW_ALT
            canvas.drawRoundRect(rect, 6f, 6f, paint)

            paint.color = COLOR_BORDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(rect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            // Title
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 7.5f
            textPaint.color = COLOR_TEXT_MUTED
            textPaint.typeface = Typeface.DEFAULT
            canvas.drawText(card.title, left + 8f, startY + 14f, textPaint)

            // Value
            textPaint.textSize = 10f
            textPaint.color = card.color
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(card.value, left + 8f, startY + 30f, textPaint)
        }

        return startY + cardHeight + 14f
    }

    private fun drawLedgerTableHeader(canvas: Canvas, startY: Float): Float {
        val hHeight = 20f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_ROW_ALT }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f
            typeface = Typeface.DEFAULT_BOLD
            color = COLOR_NAVY_HEADER
        }

        canvas.drawRect(RectF(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY + hHeight), paint)

        val borderPaint = Paint().apply {
            color = COLOR_BORDER
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY, borderPaint)
        canvas.drawLine(MARGIN_X, startY + hHeight, MARGIN_X + USABLE_WIDTH, startY + hHeight, borderPaint)

        val textY = startY + 13f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("DATE", MARGIN_X + 4f, textY, textPaint)
        canvas.drawText("VOUCHER", MARGIN_X + 75f, textY, textPaint)
        canvas.drawText("ACCOUNT [CONTRA]", MARGIN_X + 185f, textY, textPaint)
        canvas.drawText("PARTY", MARGIN_X + 300f, textY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("DEBIT (DR)", MARGIN_X + 400f, textY, textPaint)
        canvas.drawText("CREDIT (CR)", MARGIN_X + 460f, textY, textPaint)
        canvas.drawText("BALANCE", MARGIN_X + USABLE_WIDTH - 4f, textY, textPaint)

        return startY + hHeight
    }

    private fun drawTrialBalanceTableHeader(canvas: Canvas, startY: Float): Float {
        val hHeight = 20f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_ROW_ALT }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
            color = COLOR_NAVY_HEADER
        }

        canvas.drawRect(RectF(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY + hHeight), paint)

        val borderPaint = Paint().apply {
            color = COLOR_BORDER
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY, borderPaint)
        canvas.drawLine(MARGIN_X, startY + hHeight, MARGIN_X + USABLE_WIDTH, startY + hHeight, borderPaint)

        val textY = startY + 13f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("ACCOUNT NAME", MARGIN_X + 6f, textY, textPaint)
        canvas.drawText("TYPE", MARGIN_X + 200f, textY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL DEBIT (DR)", MARGIN_X + 330f, textY, textPaint)
        canvas.drawText("TOTAL CREDIT (CR)", MARGIN_X + 430f, textY, textPaint)
        canvas.drawText("NET BALANCE", MARGIN_X + USABLE_WIDTH - 6f, textY, textPaint)

        return startY + hHeight
    }

    private fun drawFooter(canvas: Canvas, currentPage: Int, totalPages: Int) {
        val footerY = PAGE_HEIGHT - MARGIN_Y + 12f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f
            color = COLOR_TEXT_MUTED
        }

        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.6f
        canvas.drawLine(MARGIN_X, footerY - 14f, MARGIN_X + USABLE_WIDTH, footerY - 14f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Frappe-Style General Ledger  •  Atri Khata ERP  •  Confidential Financial Record", MARGIN_X, footerY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $currentPage of $totalPages", MARGIN_X + USABLE_WIDTH, footerY, textPaint)
    }

    private fun truncate(text: String, maxLen: Int): String {
        return if (text.length > maxLen) text.take(maxLen - 2) + ".." else text
    }

    // -------------------------------------------------------------------------
    // SYSTEM ACTIONS: PRINT, SHARE & PREVIEW VIA ANDROID PRINTMANAGER
    // -------------------------------------------------------------------------

    fun printLedgerReport(context: Context, file: File, title: String = "General Ledger Report") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val printAdapter = PdfPrintDocumentAdapter(file, title.replace(" ", "_"))
                printManager.print(title, printAdapter, PrintAttributes.Builder().build())
            } else {
                Toast.makeText(context, "Printing service unavailable on this device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Print error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareLedgerReport(context: Context, file: File, title: String = "General Ledger Report") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Please find attached the official $title generated from Atri Khata.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Ledger Report via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openLedgerReport(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(viewIntent, "Open PDF with"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app installed to open document directly", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveToDownloads(context: Context, sourceFile: File, suggestedName: String): Uri? {
        val cleanName = if (suggestedName.endsWith(".pdf", ignoreCase = true)) suggestedName else "$suggestedName.pdf"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AtriKhata")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourceFile.inputStream().use { input -> input.copyTo(out) }
                    }
                    uri
                } else null
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val destFile = File(downloadsDir, cleanName)
                sourceFile.copyTo(destFile, overwrite = true)
                Uri.fromFile(destFile)
            }
        } catch (_: Exception) {
            try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val destFile = File(fallbackDir, cleanName)
                sourceFile.copyTo(destFile, overwrite = true)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destFile)
            } catch (_: Exception) {
                null
            }
        }
    }
}
