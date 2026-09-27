package com.example.service.pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.relation.SalesInvoiceWithDetails
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise PDF Report Generation Service for Financial Reports & Statements.
 * Generates vector-sharp, publication-quality A4 PDF documents for:
 * 1. Transaction Ledger (Statement of Accounts with running balances, Dr/Cr)
 * 2. Balance Sheet (Assets, Liabilities & Equity overview)
 * 3. Profit & Loss Statement (Operating Revenue, Expenses, Net Margin)
 * 4. Party Ledger Summary (Receivables & Payables breakdown)
 *
 * Includes built-in support for:
 * - Direct Print spooling via Android PrintManager
 * - Share via WhatsApp, Email, Drive via Android FileProvider
 * - Open & Preview in system PDF viewer
 */
object PdfReportService {

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

    data class ReportHeaderInfo(
        val businessName: String = "Atri Nova Tech Enterprises",
        val subtitle: String = "Official Financial Ledger & Statement",
        val panVat: String = "609823415",
        val phone: String = "+977 9852020149",
        val email: String = "accounts@atrinova.com",
        val address: String = "Main Road, Biratnagar-6, Nepal",
        val datePeriod: String = "All Time",
        val generatedAt: Long = System.currentTimeMillis()
    )

    private val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    private fun formatAmount(amount: Double): String {
        return "Rs. ${currencyFormat.format(amount)}"
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(millis))
    }

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // -------------------------------------------------------------------------
    // 1. TRANSACTION LEDGER REPORT
    // -------------------------------------------------------------------------
    fun generateTransactionLedgerPdf(
        context: Context,
        header: ReportHeaderInfo,
        transactions: List<TransactionEntity>,
        filterType: String = "All"
    ): File {
        val document = PdfDocument()
        val sortedList = transactions.sortedBy { it.dateMillis }

        // Calculate Totals
        var totalInflow = 0.0
        var totalOutflow = 0.0
        sortedList.forEach { tx ->
            val isCredit = tx.type in listOf("Sales Invoice", "Payment In")
            if (isCredit) totalInflow += tx.amount else totalOutflow += tx.amount
        }
        val netCashFlow = totalInflow - totalOutflow

        val rowsPerPage = 18
        val totalPages = maxOf(1, ((sortedList.size + (rowsPerPage - 1)) / rowsPerPage))
        var currentTxIndex = 0
        var runningBalance = 0.0

        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var cursorY = MARGIN_Y

            // Draw Branded Header
            cursorY = drawHeader(
                canvas = canvas,
                header = header,
                reportTitle = "TRANSACTION LEDGER STATEMENT",
                reportTypeBadge = if (filterType == "All") "ALL TRANSACTIONS" else filterType.uppercase(),
                startY = cursorY,
                isContinuation = pageIndex > 1
            )

            // Draw Summary Cards on Page 1
            if (pageIndex == 1) {
                cursorY = drawLedgerSummaryCards(
                    canvas = canvas,
                    startY = cursorY,
                    totalInflow = totalInflow,
                    totalOutflow = totalOutflow,
                    netBalance = netCashFlow,
                    count = sortedList.size
                )
            }

            // Draw Ledger Table
            cursorY = drawLedgerTableHeader(canvas, cursorY)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 8.5f
                color = COLOR_NAVY_DARK
            }

            val tableTop = cursorY
            var rowCountOnThisPage = 0

            while (currentTxIndex < sortedList.size && rowCountOnThisPage < rowsPerPage) {
                val tx = sortedList[currentTxIndex]
                val isCredit = tx.type in listOf("Sales Invoice", "Payment In")
                if (isCredit) runningBalance += tx.amount else runningBalance -= tx.amount

                val rowHeight = 22f
                val rowRect = RectF(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + rowHeight)

                // Row background alternating
                if (rowCountOnThisPage % 2 == 1) {
                    paint.color = COLOR_ROW_ALT
                    canvas.drawRect(rowRect, paint)
                }

                // Divider line
                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.6f
                canvas.drawLine(MARGIN_X, cursorY + rowHeight, MARGIN_X + USABLE_WIDTH, cursorY + rowHeight, paint)

                // Values
                val colDate = formatDate(tx.dateMillis)
                val colRef = if (tx.invoiceNumber.isNotBlank()) tx.invoiceNumber else "TX#${tx.id}"
                val colParty = truncate(tx.partyName, 20)
                val colType = tx.type
                val colMethod = tx.paymentMethod
                val colDebit = if (!isCredit) currencyFormat.format(tx.amount) else "-"
                val colCredit = if (isCredit) currencyFormat.format(tx.amount) else "-"
                val colBal = currencyFormat.format(runningBalance)

                val textY = cursorY + 14f

                // Draw columns
                // 1. Date (36 to 90)
                textPaint.color = COLOR_TEXT_MUTED
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(colDate, MARGIN_X + 4f, textY, textPaint)

                // 2. Ref (90 to 145)
                textPaint.color = COLOR_NAVY_DARK
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(colRef, MARGIN_X + 60f, textY, textPaint)

                // 3. Particulars / Party (145 to 265)
                textPaint.typeface = Typeface.DEFAULT
                canvas.drawText(colParty, MARGIN_X + 120f, textY, textPaint)

                // 4. Type & Method (265 to 355)
                textPaint.color = COLOR_TEXT_MUTED
                canvas.drawText("$colType ($colMethod)", MARGIN_X + 235f, textY, textPaint)

                // 5. Debit (355 to 415) - Right aligned
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.color = if (!isCredit) COLOR_RED else COLOR_TEXT_MUTED
                canvas.drawText(colDebit, MARGIN_X + 375f, textY, textPaint)

                // 6. Credit (415 to 475) - Right aligned
                textPaint.color = if (isCredit) COLOR_GREEN else COLOR_TEXT_MUTED
                canvas.drawText(colCredit, MARGIN_X + 445f, textY, textPaint)

                // 7. Balance (475 to 523) - Right aligned
                textPaint.color = COLOR_NAVY_DARK
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(colBal, MARGIN_X + USABLE_WIDTH - 4f, textY, textPaint)

                cursorY += rowHeight
                rowCountOnThisPage++
                currentTxIndex++
            }

            // Draw outer border for table
            paint.style = Paint.Style.STROKE
            paint.color = COLOR_BORDER
            paint.strokeWidth = 1f
            canvas.drawRect(MARGIN_X, tableTop - 20f, MARGIN_X + USABLE_WIDTH, cursorY, paint)
            paint.style = Paint.Style.FILL

            // On the last page, draw grand totals and signature section
            if (pageIndex == totalPages) {
                cursorY = drawGrandTotalsRow(canvas, cursorY, totalOutflow, totalInflow, netCashFlow)
                drawSignatorySection(canvas, cursorY + 20f)
            }

            // Draw Footer on every page
            drawFooter(canvas, pageIndex, totalPages, header.generatedAt)

            document.finishPage(page)
        }

        val outputFile = File(getReportsDir(context), "Ledger_Report_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // 2. BALANCE SHEET REPORT
    // -------------------------------------------------------------------------
    fun generateBalanceSheetPdf(
        context: Context,
        header: ReportHeaderInfo,
        totalReceivables: Double,
        totalPayables: Double,
        cashAndBank: Double,
        inventoryValue: Double
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var cursorY = MARGIN_Y

        // Header
        cursorY = drawHeader(
            canvas = canvas,
            header = header,
            reportTitle = "BALANCE SHEET STATEMENT",
            reportTypeBadge = "STATEMENT OF FINANCIAL POSITION",
            startY = cursorY,
            isContinuation = false
        )

        // Asset & Liability Calculations
        val totalCurrentAssets = cashAndBank + totalReceivables + inventoryValue
        val totalAssets = totalCurrentAssets
        val totalLiabilities = totalPayables
        val ownerEquity = totalAssets - totalLiabilities
        val totalLiabilitiesAndEquity = totalLiabilities + ownerEquity

        cursorY += 6f

        // Two-column or structured block: Assets & Liabilities
        val boxWidth = (USABLE_WIDTH - 16f) / 2f
        val leftX = MARGIN_X
        val rightX = MARGIN_X + boxWidth + 16f

        // 1. ASSETS COLUMN (LEFT)
        drawFinancialCategoryBox(
            canvas = canvas,
            x = leftX,
            y = cursorY,
            width = boxWidth,
            title = "ASSETS",
            accentColor = COLOR_SKY_BLUE,
            items = listOf(
                "Cash & Cash Equivalents" to cashAndBank * 0.45,
                "Bank Account Balances" to cashAndBank * 0.55,
                "Accounts Receivable (Debtors)" to totalReceivables,
                "Inventory / Stock Valuation" to inventoryValue
            ),
            totalLabel = "TOTAL ASSETS",
            totalAmount = totalAssets
        )

        // 2. LIABILITIES & EQUITY COLUMN (RIGHT)
        drawFinancialCategoryBox(
            canvas = canvas,
            x = rightX,
            y = cursorY,
            width = boxWidth,
            title = "LIABILITIES & EQUITY",
            accentColor = COLOR_ACCENT_ORANGE,
            items = listOf(
                "Accounts Payable (Creditors)" to totalPayables,
                "Current Accrued Liabilities" to (totalPayables * 0.08),
                "Owner's Capital Fund" to (ownerEquity * 0.70),
                "Retained Earnings / Surplus" to (ownerEquity * 0.30)
            ),
            totalLabel = "TOTAL LIABILITIES & EQUITY",
            totalAmount = totalLiabilitiesAndEquity
        )

        cursorY += 260f

        // FINANCIAL KEY RATIOS & HEALTH SUMMARY
        cursorY = drawBalanceSheetRatios(
            canvas = canvas,
            startY = cursorY,
            currentAssets = totalAssets,
            currentLiabilities = totalLiabilities,
            netWorth = ownerEquity
        )

        // Signatory & Stamp
        drawSignatorySection(canvas, cursorY + 20f)

        // Footer
        drawFooter(canvas, 1, 1, header.generatedAt)

        document.finishPage(page)

        val outputFile = File(getReportsDir(context), "Balance_Sheet_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // 3. PROFIT & LOSS STATEMENT
    // -------------------------------------------------------------------------
    fun generateProfitLossPdf(
        context: Context,
        header: ReportHeaderInfo,
        totalSales: Double,
        totalPurchases: Double,
        totalExpenses: Double
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var cursorY = MARGIN_Y

        cursorY = drawHeader(
            canvas = canvas,
            header = header,
            reportTitle = "PROFIT & LOSS STATEMENT",
            reportTypeBadge = "INCOME & EXPENDITURE STATEMENT",
            startY = cursorY,
            isContinuation = false
        )

        val grossProfit = totalSales - totalPurchases
        val netProfit = grossProfit - totalExpenses
        val profitMargin = if (totalSales > 0) (netProfit / totalSales) * 100.0 else 0.0

        cursorY += 10f

        // Table for Revenue & Expenses
        cursorY = drawIncomeStatementTable(
            canvas = canvas,
            startY = cursorY,
            sales = totalSales,
            purchases = totalPurchases,
            grossProfit = grossProfit,
            expenses = totalExpenses,
            netProfit = netProfit,
            profitMargin = profitMargin
        )

        drawSignatorySection(canvas, cursorY + 40f)
        drawFooter(canvas, 1, 1, header.generatedAt)

        document.finishPage(page)

        val outputFile = File(getReportsDir(context), "Profit_Loss_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // 4. PARTY BALANCES LEDGER REPORT
    // -------------------------------------------------------------------------
    fun generatePartyLedgerPdf(
        context: Context,
        header: ReportHeaderInfo,
        parties: List<PartyEntity>
    ): File {
        val document = PdfDocument()
        val rowsPerPage = 22
        val totalPages = maxOf(1, ((parties.size + (rowsPerPage - 1)) / rowsPerPage))
        var currentPartyIndex = 0

        var totalRec = 0.0
        var totalGive = 0.0
        parties.forEach {
            totalRec += it.balanceToReceive
            totalGive += it.balanceToGive
        }

        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var cursorY = MARGIN_Y

            cursorY = drawHeader(
                canvas = canvas,
                header = header,
                reportTitle = "PARTY BALANCE LEDGER SUMMARY",
                reportTypeBadge = "ACCOUNTS RECEIVABLE & PAYABLE",
                startY = cursorY,
                isContinuation = pageIndex > 1
            )

            if (pageIndex == 1) {
                cursorY = drawPartySummaryCards(canvas, cursorY, totalRec, totalGive, parties.size)
            }

            // Draw Table Header
            cursorY = drawPartyTableHeader(canvas, cursorY)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 8.5f
                color = COLOR_NAVY_DARK
            }

            val tableTop = cursorY
            var rowCount = 0

            while (currentPartyIndex < parties.size && rowCount < rowsPerPage) {
                val party = parties[currentPartyIndex]
                val rowHeight = 22f
                val rowRect = RectF(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + rowHeight)

                if (rowCount % 2 == 1) {
                    paint.color = COLOR_ROW_ALT
                    canvas.drawRect(rowRect, paint)
                }

                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.6f
                canvas.drawLine(MARGIN_X, cursorY + rowHeight, MARGIN_X + USABLE_WIDTH, cursorY + rowHeight, paint)

                val textY = cursorY + 14f

                // 1. Party Name & Category
                textPaint.textAlign = Paint.Align.LEFT
                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.color = COLOR_NAVY_DARK
                canvas.drawText(truncate(party.name, 22), MARGIN_X + 4f, textY, textPaint)

                // 2. Type & Category
                textPaint.typeface = Typeface.DEFAULT
                textPaint.color = COLOR_TEXT_MUTED
                canvas.drawText("${party.type} / ${party.category}", MARGIN_X + 140f, textY, textPaint)

                // 3. Phone / PAN
                val contactInfo = if (party.phone.isNotBlank()) party.phone else (if (party.panVatNumber.isNotBlank()) "PAN: ${party.panVatNumber}" else "-")
                canvas.drawText(contactInfo, MARGIN_X + 260f, textY, textPaint)

                // 4. To Receive (Dr)
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.color = if (party.balanceToReceive > 0) COLOR_SKY_BLUE else COLOR_TEXT_MUTED
                textPaint.typeface = if (party.balanceToReceive > 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                canvas.drawText(if (party.balanceToReceive > 0) currencyFormat.format(party.balanceToReceive) else "-", MARGIN_X + 420f, textY, textPaint)

                // 5. To Give (Cr)
                textPaint.color = if (party.balanceToGive > 0) COLOR_ACCENT_ORANGE else COLOR_TEXT_MUTED
                textPaint.typeface = if (party.balanceToGive > 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                canvas.drawText(if (party.balanceToGive > 0) currencyFormat.format(party.balanceToGive) else "-", MARGIN_X + USABLE_WIDTH - 4f, textY, textPaint)

                cursorY += rowHeight
                rowCount++
                currentPartyIndex++
            }

            paint.style = Paint.Style.STROKE
            paint.color = COLOR_BORDER
            paint.strokeWidth = 1f
            canvas.drawRect(MARGIN_X, tableTop - 20f, MARGIN_X + USABLE_WIDTH, cursorY, paint)
            paint.style = Paint.Style.FILL

            if (pageIndex == totalPages) {
                // Party Totals
                cursorY = drawPartyGrandTotals(canvas, cursorY, totalRec, totalGive)
                drawSignatorySection(canvas, cursorY + 20f)
            }

            drawFooter(canvas, pageIndex, totalPages, header.generatedAt)
            document.finishPage(page)
        }

        val outputFile = File(getReportsDir(context), "Party_Ledger_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // DRAWING HELPER FUNCTIONS (VECTOR ACCURACY)
    // -------------------------------------------------------------------------

    private fun drawHeader(
        canvas: Canvas,
        header: ReportHeaderInfo,
        reportTitle: String,
        reportTypeBadge: String,
        startY: Float,
        isContinuation: Boolean
    ): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        var y = startY

        // Top Accent Strip
        paint.color = COLOR_NAVY_DARK
        canvas.drawRect(MARGIN_X, y, MARGIN_X + USABLE_WIDTH, y + 4f, paint)
        paint.color = COLOR_SKY_BLUE
        canvas.drawRect(MARGIN_X, y + 4f, MARGIN_X + 100f, y + 6.5f, paint)

        y += 18f

        // Business Name & Report Title
        textPaint.textSize = 15f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.color = COLOR_NAVY_DARK
        canvas.drawText(header.businessName.uppercase(), MARGIN_X, y, textPaint)

        // Right side badge: Document Type
        val badgeText = if (isContinuation) "$reportTypeBadge (CONT.)" else reportTypeBadge
        textPaint.textSize = 8.5f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        val badgeWidth = textPaint.measureText(badgeText) + 14f
        val badgeRect = RectF(MARGIN_X + USABLE_WIDTH - badgeWidth, y - 11f, MARGIN_X + USABLE_WIDTH, y + 4f)
        paint.color = COLOR_ROW_ALT
        canvas.drawRoundRect(badgeRect, 4f, 4f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(badgeRect, 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        textPaint.color = COLOR_SKY_BLUE
        canvas.drawText(badgeText, MARGIN_X + USABLE_WIDTH - badgeWidth + 7f, y, textPaint)

        y += 13f

        // Report Subtitle
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.color = COLOR_SKY_BLUE
        canvas.drawText(reportTitle, MARGIN_X, y, textPaint)

        y += 12f

        // Business Metadata Line: PAN/VAT, Contact, Address
        textPaint.textSize = 8f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = COLOR_TEXT_MUTED
        val metaLine1 = "PAN / VAT: ${header.panVat}  •  Phone: ${header.phone}  •  Email: ${header.email}"
        canvas.drawText(metaLine1, MARGIN_X, y, textPaint)

        // Right aligned Date Filter Period
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Period: ${header.datePeriod}", MARGIN_X + USABLE_WIDTH, y, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        y += 11f
        val metaLine2 = "Address: ${header.address}"
        canvas.drawText(metaLine2, MARGIN_X, y, textPaint)

        y += 8f

        // Bottom separator line
        paint.color = COLOR_BORDER
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_X, y, MARGIN_X + USABLE_WIDTH, y, paint)

        return y + 10f
    }

    private fun drawLedgerSummaryCards(
        canvas: Canvas,
        startY: Float,
        totalInflow: Double,
        totalOutflow: Double,
        netBalance: Double,
        count: Int
    ): Float {
        val cardHeight = 44f
        val cardSpacing = 8f
        val cardWidth = (USABLE_WIDTH - (cardSpacing * 3)) / 4f

        val cards = listOf(
            SummaryCardData("TOTAL INFLOW", formatAmount(totalInflow), COLOR_GREEN),
            SummaryCardData("TOTAL OUTFLOW", formatAmount(totalOutflow), COLOR_RED),
            SummaryCardData("NET CASH FLOW", formatAmount(netBalance), if (netBalance >= 0) COLOR_SKY_BLUE else COLOR_ACCENT_ORANGE),
            SummaryCardData("RECORD COUNT", "$count Entries", COLOR_NAVY_DARK)
        )

        cards.forEachIndexed { i, card ->
            val left = MARGIN_X + (i * (cardWidth + cardSpacing))
            val rect = RectF(left, startY, left + cardWidth, startY + cardHeight)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = COLOR_ROW_ALT
            canvas.drawRoundRect(rect, 6f, 6f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = COLOR_BORDER
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(rect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            // Top decorative bar
            paint.color = card.accentColor
            canvas.drawRoundRect(RectF(left, startY, left + cardWidth, startY + 2.5f), 1f, 1f, paint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            textPaint.textSize = 7.5f
            textPaint.color = COLOR_TEXT_MUTED
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(card.label, left + 8f, startY + 16f, textPaint)

            textPaint.textSize = 10f
            textPaint.color = card.accentColor
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(card.value, left + 8f, startY + 34f, textPaint)
        }

        return startY + cardHeight + 12f
    }

    private data class SummaryCardData(val label: String, val value: String, val accentColor: Int)

    private fun drawLedgerTableHeader(canvas: Canvas, startY: Float): Float {
        val height = 20f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_NAVY_HEADER
        }
        val headerRect = RectF(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY + height)
        canvas.drawRect(headerRect, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
        }

        val textY = startY + 13.5f

        canvas.drawText("DATE", MARGIN_X + 4f, textY, textPaint)
        canvas.drawText("REF / VCH", MARGIN_X + 60f, textY, textPaint)
        canvas.drawText("PARTICULARS", MARGIN_X + 120f, textY, textPaint)
        canvas.drawText("TYPE & METHOD", MARGIN_X + 235f, textY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("DEBIT (OUT)", MARGIN_X + 375f, textY, textPaint)
        canvas.drawText("CREDIT (IN)", MARGIN_X + 445f, textY, textPaint)
        canvas.drawText("BALANCE", MARGIN_X + USABLE_WIDTH - 4f, textY, textPaint)

        return startY + height
    }

    private fun drawGrandTotalsRow(
        canvas: Canvas,
        startY: Float,
        totalDebit: Double,
        totalCredit: Double,
        closingBalance: Double
    ): Float {
        val height = 24f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_ROW_ALT
        }
        canvas.drawRect(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY + height, paint)

        paint.color = COLOR_NAVY_DARK
        paint.strokeWidth = 1.2f
        canvas.drawLine(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY, paint)
        canvas.drawLine(MARGIN_X, startY + height - 2f, MARGIN_X + USABLE_WIDTH, startY + height - 2f, paint)
        canvas.drawLine(MARGIN_X, startY + height, MARGIN_X + USABLE_WIDTH, startY + height, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            color = COLOR_NAVY_DARK
        }

        val textY = startY + 15f
        canvas.drawText("GRAND TOTALS:", MARGIN_X + 120f, textY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = COLOR_RED
        canvas.drawText(formatAmount(totalDebit), MARGIN_X + 375f, textY, textPaint)

        textPaint.color = COLOR_GREEN
        canvas.drawText(formatAmount(totalCredit), MARGIN_X + 445f, textY, textPaint)

        textPaint.color = COLOR_NAVY_DARK
        canvas.drawText(formatAmount(closingBalance), MARGIN_X + USABLE_WIDTH - 4f, textY, textPaint)

        return startY + height + 6f
    }

    private fun drawPartySummaryCards(
        canvas: Canvas,
        startY: Float,
        totalRec: Double,
        totalGive: Double,
        partyCount: Int
    ): Float {
        val cardHeight = 44f
        val cardSpacing = 10f
        val cardWidth = (USABLE_WIDTH - (cardSpacing * 2)) / 3f

        val cards = listOf(
            SummaryCardData("TOTAL RECEIVABLES (DR)", formatAmount(totalRec), COLOR_SKY_BLUE),
            SummaryCardData("TOTAL PAYABLES (CR)", formatAmount(totalGive), COLOR_ACCENT_ORANGE),
            SummaryCardData("TOTAL ACTIVE PARTIES", "$partyCount Accounts", COLOR_NAVY_DARK)
        )

        cards.forEachIndexed { i, card ->
            val left = MARGIN_X + (i * (cardWidth + cardSpacing))
            val rect = RectF(left, startY, left + cardWidth, startY + cardHeight)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = COLOR_ROW_ALT
            canvas.drawRoundRect(rect, 6f, 6f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = COLOR_BORDER
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(rect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.color = card.accentColor
            canvas.drawRoundRect(RectF(left, startY, left + cardWidth, startY + 2.5f), 1f, 1f, paint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            textPaint.textSize = 7.5f
            textPaint.color = COLOR_TEXT_MUTED
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(card.label, left + 8f, startY + 16f, textPaint)

            textPaint.textSize = 10.5f
            textPaint.color = card.accentColor
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(card.value, left + 8f, startY + 34f, textPaint)
        }

        return startY + cardHeight + 12f
    }

    private fun drawPartyTableHeader(canvas: Canvas, startY: Float): Float {
        val height = 20f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_NAVY_HEADER }
        canvas.drawRect(RectF(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY + height), paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
        }
        val textY = startY + 13.5f

        canvas.drawText("PARTY / BUSINESS NAME", MARGIN_X + 4f, textY, textPaint)
        canvas.drawText("TYPE / CATEGORY", MARGIN_X + 140f, textY, textPaint)
        canvas.drawText("PHONE / PAN", MARGIN_X + 260f, textY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TO RECEIVE (DR)", MARGIN_X + 420f, textY, textPaint)
        canvas.drawText("TO GIVE (CR)", MARGIN_X + USABLE_WIDTH - 4f, textY, textPaint)

        return startY + height
    }

    private fun drawPartyGrandTotals(
        canvas: Canvas,
        startY: Float,
        totalRec: Double,
        totalGive: Double
    ): Float {
        val height = 24f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_ROW_ALT }
        canvas.drawRect(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY + height, paint)

        paint.color = COLOR_NAVY_DARK
        paint.strokeWidth = 1.2f
        canvas.drawLine(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY, paint)
        canvas.drawLine(MARGIN_X, startY + height - 2f, MARGIN_X + USABLE_WIDTH, startY + height - 2f, paint)
        canvas.drawLine(MARGIN_X, startY + height, MARGIN_X + USABLE_WIDTH, startY + height, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            color = COLOR_NAVY_DARK
        }

        val textY = startY + 15f
        canvas.drawText("TOTAL LEDGER BALANCES:", MARGIN_X + 140f, textY, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = COLOR_SKY_BLUE
        canvas.drawText(formatAmount(totalRec), MARGIN_X + 420f, textY, textPaint)

        textPaint.color = COLOR_ACCENT_ORANGE
        canvas.drawText(formatAmount(totalGive), MARGIN_X + USABLE_WIDTH - 4f, textY, textPaint)

        return startY + height + 6f
    }

    private fun drawFinancialCategoryBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        title: String,
        accentColor: Int,
        items: List<Pair<String, Double>>,
        totalLabel: String,
        totalAmount: Double
    ) {
        val boxHeight = 220f
        val rect = RectF(x, y, x + width, y + boxHeight)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = COLOR_ROW_ALT
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = COLOR_BORDER
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Header Banner for this box
        val headerRect = RectF(x, y, x + width, y + 26f)
        paint.color = COLOR_NAVY_HEADER
        canvas.drawRoundRect(headerRect, 8f, 8f, paint)
        // square bottom corners
        canvas.drawRect(x, y + 15f, x + width, y + 26f, paint)

        // Accent top line
        paint.color = accentColor
        canvas.drawRoundRect(RectF(x, y, x + width, y + 3f), 1f, 1f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(title, x + 10f, y + 17f, textPaint)

        // Draw items
        var itemY = y + 46f
        textPaint.textSize = 8.5f
        textPaint.color = COLOR_NAVY_DARK

        items.forEachIndexed { index, (label, amount) ->
            textPaint.typeface = Typeface.DEFAULT
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(label, x + 10f, itemY, textPaint)

            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(formatAmount(amount), x + width - 10f, itemY, textPaint)

            paint.color = COLOR_BORDER
            paint.strokeWidth = 0.5f
            canvas.drawLine(x + 10f, itemY + 8f, x + width - 10f, itemY + 8f, paint)

            itemY += 28f
        }

        // Total Section at bottom of box
        val totalY = y + boxHeight - 34f
        paint.color = Color.WHITE
        canvas.drawRect(x + 2f, totalY, x + width - 2f, y + boxHeight - 2f, paint)

        paint.color = COLOR_NAVY_DARK
        paint.strokeWidth = 1f
        canvas.drawLine(x + 8f, totalY, x + width - 8f, totalY, paint)

        textPaint.textSize = 9f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.color = COLOR_NAVY_DARK
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(totalLabel, x + 10f, totalY + 18f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = accentColor
        canvas.drawText(formatAmount(totalAmount), x + width - 10f, totalY + 18f, textPaint)
    }

    private fun drawBalanceSheetRatios(
        canvas: Canvas,
        startY: Float,
        currentAssets: Double,
        currentLiabilities: Double,
        netWorth: Double
    ): Float {
        val rect = RectF(MARGIN_X, startY, MARGIN_X + USABLE_WIDTH, startY + 54f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_ROW_ALT }
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = COLOR_BORDER
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
            color = COLOR_NAVY_DARK
        }

        canvas.drawText("FINANCIAL HEALTH INDICATORS & LIQUIDITY OVERVIEW", MARGIN_X + 12f, startY + 16f, textPaint)

        val workingCapital = currentAssets - currentLiabilities
        val currentRatio = if (currentLiabilities > 0) String.format(Locale.US, "%.2f : 1", currentAssets / currentLiabilities) else "Healthy (No Debt)"

        textPaint.textSize = 8f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = COLOR_TEXT_MUTED
        canvas.drawText("• Net Working Capital: ${formatAmount(workingCapital)}", MARGIN_X + 12f, startY + 32f, textPaint)
        canvas.drawText("• Current Liquidity Ratio: $currentRatio", MARGIN_X + 200f, startY + 32f, textPaint)
        canvas.drawText("• Net Business Worth: ${formatAmount(netWorth)}", MARGIN_X + 370f, startY + 32f, textPaint)

        return startY + 54f
    }

    private fun drawIncomeStatementTable(
        canvas: Canvas,
        startY: Float,
        sales: Double,
        purchases: Double,
        grossProfit: Double,
        expenses: Double,
        netProfit: Double,
        profitMargin: Double
    ): Float {
        var y = startY
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        val items = listOf(
            IncomeItem("1. REVENUE FROM OPERATIONS", "Gross Sales & Receipts", sales, COLOR_NAVY_DARK, false),
            IncomeItem("2. COST OF GOODS SOLD", "Inventory Procurement & Purchases", purchases, COLOR_RED, false),
            IncomeItem("   GROSS OPERATING PROFIT", "(Revenue minus Cost of Goods)", grossProfit, COLOR_GREEN, true),
            IncomeItem("3. OPERATING & ADMIN EXPENSES", "Rent, Utilities, Staff Salaries & Petty Cash", expenses, COLOR_RED, false),
            IncomeItem("   NET OPERATING PROFIT / (LOSS)", "Net Bottom Line Earnings", netProfit, if (netProfit >= 0) COLOR_GREEN else COLOR_RED, true)
        )

        // Header
        paint.color = COLOR_NAVY_HEADER
        canvas.drawRect(MARGIN_X, y, MARGIN_X + USABLE_WIDTH, y + 20f, paint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 8.5f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("ACCOUNT HEAD & CLASSIFICATION", MARGIN_X + 8f, y + 14f, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (RS.)", MARGIN_X + USABLE_WIDTH - 8f, y + 14f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        y += 20f

        items.forEach { item ->
            val rowHeight = if (item.isHighlight) 28f else 24f
            val rowRect = RectF(MARGIN_X, y, MARGIN_X + USABLE_WIDTH, y + rowHeight)

            paint.color = if (item.isHighlight) COLOR_ROW_ALT else Color.WHITE
            canvas.drawRect(rowRect, paint)

            paint.color = if (item.isHighlight) COLOR_NAVY_DARK else COLOR_BORDER
            paint.strokeWidth = if (item.isHighlight) 1f else 0.5f
            canvas.drawLine(MARGIN_X, y + rowHeight, MARGIN_X + USABLE_WIDTH, y + rowHeight, paint)

            val textY = y + (if (item.isHighlight) 18f else 15f)

            textPaint.color = if (item.isHighlight) COLOR_NAVY_DARK else COLOR_NAVY_DARK
            textPaint.typeface = if (item.isHighlight) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textPaint.textSize = if (item.isHighlight) 9.5f else 8.5f
            canvas.drawText(item.title, MARGIN_X + 8f, textY, textPaint)

            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 7.5f
            textPaint.typeface = Typeface.DEFAULT
            canvas.drawText(" - ${item.subtitle}", MARGIN_X + 170f, textY, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = item.color
            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textSize = if (item.isHighlight) 10f else 8.5f
            canvas.drawText(formatAmount(item.amount), MARGIN_X + USABLE_WIDTH - 8f, textY, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            y += rowHeight
        }

        // Margin banner
        y += 12f
        val marginRect = RectF(MARGIN_X, y, MARGIN_X + USABLE_WIDTH, y + 36f)
        paint.color = COLOR_ROW_ALT
        canvas.drawRoundRect(marginRect, 6f, 6f, paint)

        paint.color = COLOR_SKY_BLUE
        canvas.drawRoundRect(RectF(MARGIN_X, y, MARGIN_X + 4f, y + 36f), 1f, 1f, paint)

        textPaint.textSize = 9f
        textPaint.color = COLOR_NAVY_DARK
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("NET PROFIT MARGIN: ${String.format(Locale.US, "%.1f", profitMargin)}%", MARGIN_X + 14f, y + 16f, textPaint)

        textPaint.textSize = 8f
        textPaint.color = COLOR_TEXT_MUTED
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("Profit margin calculated as (Net Profit / Total Operating Revenue) * 100", MARGIN_X + 14f, y + 28f, textPaint)

        return y + 36f
    }

    private data class IncomeItem(
        val title: String,
        val subtitle: String,
        val amount: Double,
        val color: Int,
        val isHighlight: Boolean
    )

    private fun drawSignatorySection(canvas: Canvas, startY: Float) {
        val boxWidth = 140f
        val rightX = MARGIN_X + USABLE_WIDTH - boxWidth

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(rightX, startY + 36f, rightX + boxWidth, startY + 36f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f
            color = COLOR_NAVY_DARK
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("AUTHORIZED SIGNATORY", rightX + (boxWidth / 2f), startY + 48f, textPaint)

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 7f
        canvas.drawText("For & on behalf of the Management", rightX + (boxWidth / 2f), startY + 58f, textPaint)
    }

    private fun drawFooter(
        canvas: Canvas,
        pageNumber: Int,
        totalPages: Int,
        generatedAt: Long
    ) {
        val footerY = PAGE_HEIGHT - MARGIN_Y + 12f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BORDER
            strokeWidth = 0.6f
        }
        canvas.drawLine(MARGIN_X, footerY - 14f, MARGIN_X + USABLE_WIDTH, footerY - 14f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f
            color = COLOR_TEXT_MUTED
            typeface = Typeface.DEFAULT
        }

        // Left branding
        canvas.drawText("Frappe Books Pro Financial Suite  •  Confidential & Privileged", MARGIN_X, footerY, textPaint)

        // Center Page Number
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page $pageNumber of $totalPages", MARGIN_X + (USABLE_WIDTH / 2f), footerY, textPaint)

        // Right Generated timestamp
        textPaint.textAlign = Paint.Align.RIGHT
        val timeStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date(generatedAt))
        canvas.drawText("Generated: $timeStr", MARGIN_X + USABLE_WIDTH, footerY, textPaint)
    }

    private fun truncate(str: String, maxLength: Int): String {
        return if (str.length > maxLength) str.substring(0, maxLength - 2) + ".." else str
    }

    // -------------------------------------------------------------------------
    // 5. INDIVIDUAL TRANSACTION VOUCHER / RECEIPT PDF
    // -------------------------------------------------------------------------
    fun generateReceiptVoucherPdf(
        context: Context,
        header: ReportHeaderInfo,
        transaction: TransactionEntity
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var cursorY = MARGIN_Y

        val isCredit = transaction.type in listOf("Sales Invoice", "Payment In")
        val voucherTitle = if (transaction.type == "Sales Invoice") "TAX INVOICE / CASH BILL" else "${transaction.type.uppercase()} VOUCHER"

        cursorY = drawHeader(
            canvas = canvas,
            header = header,
            reportTitle = voucherTitle,
            reportTypeBadge = transaction.type.uppercase(),
            startY = cursorY,
            isContinuation = false
        )

        cursorY += 10f

        // Voucher metadata box
        val rect = RectF(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + 70f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_ROW_ALT }
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_BORDER
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            color = COLOR_NAVY_DARK
        }

        val voucherNo = if (transaction.invoiceNumber.isNotBlank()) transaction.invoiceNumber else "VCH-${transaction.id}"
        canvas.drawText("VOUCHER NO: $voucherNo", MARGIN_X + 14f, cursorY + 22f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("DATE (BS): ${transaction.displayBsDate}  |  (AD): ${transaction.displayAdDate}", MARGIN_X + USABLE_WIDTH - 14f, cursorY + 22f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        textPaint.textSize = 8.5f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = COLOR_TEXT_MUTED
        canvas.drawText("PAYMENT METHOD: ${transaction.paymentMethod}", MARGIN_X + 14f, cursorY + 42f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("STATUS: ${transaction.status.uppercase()}", MARGIN_X + USABLE_WIDTH - 14f, cursorY + 42f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        cursorY += 85f

        // Party Details Box
        val partyRect = RectF(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + 54f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(partyRect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_BORDER
        paint.strokeWidth = 1f
        canvas.drawRoundRect(partyRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        textPaint.textSize = 8f
        textPaint.color = COLOR_SKY_BLUE
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(if (isCredit) "CUSTOMER / PARTY DETAILS:" else "SUPPLIER / PAYEE DETAILS:", MARGIN_X + 12f, cursorY + 18f, textPaint)

        textPaint.textSize = 12f
        textPaint.color = COLOR_NAVY_DARK
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(transaction.partyName, MARGIN_X + 12f, cursorY + 38f, textPaint)

        cursorY += 70f

        // Particulars Table
        val tableHeaderY = cursorY
        paint.color = COLOR_NAVY_HEADER
        canvas.drawRect(MARGIN_X, tableHeaderY, MARGIN_X + USABLE_WIDTH, tableHeaderY + 22f, paint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("DESCRIPTION / PARTICULARS", MARGIN_X + 10f, tableHeaderY + 15f, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (RS.)", MARGIN_X + USABLE_WIDTH - 10f, tableHeaderY + 15f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        cursorY += 22f

        // Row
        val descRowHeight = 44f
        paint.color = COLOR_ROW_ALT
        canvas.drawRect(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + descRowHeight, paint)

        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(MARGIN_X, cursorY + descRowHeight, MARGIN_X + USABLE_WIDTH, cursorY + descRowHeight, paint)

        textPaint.color = COLOR_NAVY_DARK
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("${transaction.type} - Account Settlement", MARGIN_X + 10f, cursorY + 20f, textPaint)

        textPaint.textSize = 8f
        textPaint.color = COLOR_TEXT_MUTED
        textPaint.typeface = Typeface.DEFAULT
        val noteText = if (transaction.notes.isNotBlank()) "Note: ${transaction.notes}" else "Payment settled via ${transaction.paymentMethod}"
        canvas.drawText(noteText, MARGIN_X + 10f, cursorY + 34f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 11f
        textPaint.color = if (isCredit) COLOR_GREEN else COLOR_RED
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(formatAmount(transaction.amount), MARGIN_X + USABLE_WIDTH - 10f, cursorY + 24f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        cursorY += descRowHeight + 10f

        // Total Amount Highlight Card
        val totalBox = RectF(MARGIN_X + USABLE_WIDTH - 240f, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + 40f)
        paint.color = COLOR_ROW_ALT
        canvas.drawRoundRect(totalBox, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = COLOR_NAVY_DARK
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(totalBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        textPaint.textSize = 10f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.color = COLOR_NAVY_DARK
        canvas.drawText("TOTAL AMOUNT PAID:", MARGIN_X + USABLE_WIDTH - 230f, cursorY + 24f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 12f
        textPaint.color = if (isCredit) COLOR_GREEN else COLOR_RED
        canvas.drawText(formatAmount(transaction.amount), MARGIN_X + USABLE_WIDTH - 12f, cursorY + 25f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        cursorY += 70f

        // Terms & Conditions note
        val termsRect = RectF(MARGIN_X, cursorY, MARGIN_X + 260f, cursorY + 50f)
        paint.color = COLOR_ROW_ALT
        canvas.drawRoundRect(termsRect, 4f, 4f, paint)
        textPaint.textSize = 7.5f
        textPaint.color = COLOR_TEXT_MUTED
        canvas.drawText("TERMS & ACKNOWLEDGEMENT:", MARGIN_X + 8f, cursorY + 14f, textPaint)
        canvas.drawText("1. Goods once sold are subject to standard warranty.", MARGIN_X + 8f, cursorY + 26f, textPaint)
        canvas.drawText("2. Computer generated receipt. Valid without physical seal.", MARGIN_X + 8f, cursorY + 38f, textPaint)

        drawSignatorySection(canvas, cursorY)
        drawFooter(canvas, 1, 1, header.generatedAt)

        document.finishPage(page)

        val outputFile = File(getReportsDir(context), "Voucher_${voucherNo}_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // 6. ENTERPRISE SALES TAX INVOICE / BILL OF SUPPLY PDF (A4 PRINTABLE)
    // -------------------------------------------------------------------------
    fun generateSalesInvoicePdf(
        context: Context,
        invoiceWithDetails: SalesInvoiceWithDetails,
        settings: com.example.data.model.InvoiceSettings = com.example.data.model.InvoiceSettings()
    ): File {
        val invoice = invoiceWithDetails.invoice
        val party = invoiceWithDetails.party
        val profile = invoiceWithDetails.businessProfile
        val items = invoiceWithDetails.items
        val payments = invoiceWithDetails.payments

        val document = PdfDocument()

        // Real Business Profile information merged with settings
        val businessName = profile?.businessName?.ifBlank { settings.businessName } ?: settings.businessName
        val panVat = profile?.panVatNumber?.ifBlank { settings.panVatNumber } ?: settings.panVatNumber
        val phone = profile?.phone?.ifBlank { settings.phone } ?: settings.phone
        val email = profile?.email?.ifBlank { settings.email } ?: settings.email
        val address = profile?.address?.ifBlank { settings.address } ?: settings.address
        val terms = profile?.invoiceTerms?.ifBlank { settings.defaultTerms } ?: settings.defaultTerms

        val partyName = invoice.partyNameSnapshot.ifBlank { party?.name ?: "Walk-in Cash Customer" }
        val partyPhone = if (settings.showPartyPhone) {
            invoice.partyPhoneSnapshot?.ifBlank { null } ?: party?.phone ?: "Phone: Over-the-counter"
        } else ""
        val partyAddress = if (settings.showPartyAddress) {
            invoice.partyAddressSnapshot?.ifBlank { null } ?: party?.address ?: "Address: Local Walk-in Consumer"
        } else ""
        val partyPan = if (settings.showPartyPanVat) {
            invoice.partyPanSnapshot?.ifBlank { null } ?: party?.panVatNumber?.ifBlank { null }
        } else null
        val panText = if (partyPan != null) "PAN/VAT: $partyPan" else if (settings.showPartyPanVat) "PAN/VAT: Unregistered Consumer" else ""

        // Dynamic Multi-page Chunking
        val pageItemChunks = mutableListOf<List<SalesInvoiceItemEntity>>()
        if (items.isEmpty()) {
            pageItemChunks.add(emptyList())
        } else {
            var remaining = items
            val p1CapacityWithTotals = 16
            if (remaining.size <= p1CapacityWithTotals) {
                pageItemChunks.add(remaining)
                remaining = emptyList()
            } else {
                val p1ChunkSize = minOf(22, remaining.size)
                pageItemChunks.add(remaining.take(p1ChunkSize))
                remaining = remaining.drop(p1ChunkSize)

                while (remaining.isNotEmpty()) {
                    val pSubCapacityWithTotals = 26
                    if (remaining.size <= pSubCapacityWithTotals) {
                        pageItemChunks.add(remaining)
                        remaining = emptyList()
                    } else {
                        val pSubChunkSize = minOf(34, remaining.size)
                        pageItemChunks.add(remaining.take(pSubChunkSize))
                        remaining = remaining.drop(pSubChunkSize)
                    }
                }
            }
        }

        val totalPages = pageItemChunks.size
        var globalItemIndex = 0

        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            var cursorY = MARGIN_Y

            // -------------------------------------------------------------
            // Page Header
            // -------------------------------------------------------------
            if (pageIndex == 1) {
                // Top Accent Strip
                paint.color = COLOR_NAVY_DARK
                canvas.drawRect(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + 4f, paint)
                paint.color = COLOR_SKY_BLUE
                canvas.drawRect(MARGIN_X, cursorY + 4f, MARGIN_X + 110f, cursorY + 6.5f, paint)

                cursorY += 14f

                // Business Logo / Vector Crest Monogram
                val showLogo = settings.showBusinessLogo
                val logoSize = if (showLogo) 42f else 0f
                val logoRect = RectF(MARGIN_X, cursorY, MARGIN_X + logoSize, cursorY + logoSize)
                var drewCustomLogo = false
                if (showLogo) {
                    if (!profile?.logoUri.isNullOrBlank()) {
                        try {
                            val uri = Uri.parse(profile!!.logoUri)
                            val bitmap = if (uri.scheme == "content" || uri.scheme == "android.resource") {
                                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                            } else {
                                BitmapFactory.decodeFile(profile.logoUri)
                            }
                            if (bitmap != null) {
                                canvas.drawBitmap(bitmap, null, logoRect, null)
                                drewCustomLogo = true
                            }
                        } catch (_: Exception) {
                            drewCustomLogo = false
                        }
                    }

                    if (!drewCustomLogo) {
                        // Corporate Emblem Badge with Business Initials
                        paint.color = COLOR_NAVY_DARK
                        paint.style = Paint.Style.FILL
                        canvas.drawRoundRect(logoRect, 8f, 8f, paint)

                        paint.style = Paint.Style.STROKE
                        paint.color = COLOR_SKY_BLUE
                        paint.strokeWidth = 1.5f
                        canvas.drawRoundRect(RectF(logoRect.left + 2f, logoRect.top + 2f, logoRect.right - 2f, logoRect.bottom - 2f), 6f, 6f, paint)
                        paint.style = Paint.Style.FILL

                        val initialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.WHITE
                            textSize = 17f
                            typeface = Typeface.DEFAULT_BOLD
                            textAlign = Paint.Align.CENTER
                        }
                        val initials = businessName.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .map { it.first().uppercaseChar() }
                            .joinToString("")
                            .ifBlank { "AK" }
                        val fm = initialPaint.fontMetrics
                        val textCenterY = logoRect.centerY() - (fm.ascent + fm.descent) / 2f
                        canvas.drawText(initials, logoRect.centerX(), textCenterY, initialPaint)
                    }
                }

                // Business Details (Beside Logo)
                val textStartX = if (showLogo) MARGIN_X + logoSize + 12f else MARGIN_X
                textPaint.textSize = 14f
                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.color = COLOR_NAVY_DARK
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(businessName.uppercase(), textStartX, cursorY + 14f, textPaint)

                textPaint.textSize = 8f
                textPaint.typeface = Typeface.DEFAULT
                textPaint.color = COLOR_TEXT_MUTED
                val metaLine1 = "PAN / VAT: $panVat  •  Phone: $phone  •  Email: $email"
                canvas.drawText(metaLine1, textStartX, cursorY + 27f, textPaint)
                val metaLine2 = "Address: $address"
                canvas.drawText(metaLine2, textStartX, cursorY + 39f, textPaint)

                // Right Classification Badge
                val invoiceTitle = if (invoice.isTaxInvoice) "TAX INVOICE" else "BILL OF SUPPLY / SALES MEMO"
                val badgeText = if (invoice.isTaxInvoice) "VAT REGISTERED (13%)" else "EXEMPT / NON-VAT"

                textPaint.textSize = 8.5f
                textPaint.typeface = Typeface.DEFAULT_BOLD
                val badgeWidth = textPaint.measureText(badgeText) + 14f
                val badgeRect = RectF(MARGIN_X + USABLE_WIDTH - badgeWidth, cursorY + 2f, MARGIN_X + USABLE_WIDTH, cursorY + 18f)
                paint.color = COLOR_ROW_ALT
                canvas.drawRoundRect(badgeRect, 4f, 4f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.8f
                canvas.drawRoundRect(badgeRect, 4f, 4f, paint)
                paint.style = Paint.Style.FILL

                textPaint.color = COLOR_SKY_BLUE
                canvas.drawText(badgeText, MARGIN_X + USABLE_WIDTH - badgeWidth + 7f, cursorY + 13.5f, textPaint)

                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.textSize = 12f
                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.color = COLOR_NAVY_DARK
                canvas.drawText(invoiceTitle, MARGIN_X + USABLE_WIDTH, cursorY + 36f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                cursorY += logoSize + 10f

                // Divider line
                paint.color = COLOR_BORDER
                paint.strokeWidth = 1f
                canvas.drawLine(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY, paint)
                cursorY += 8f

                // Two Column Meta Block: Customer Info (Left) vs Invoice Details (Right)
                val metaBoxHeight = 84f
                val halfWidth = (USABLE_WIDTH - 12f) / 2f

                // Left Box: Billed To
                val leftRect = RectF(MARGIN_X, cursorY, MARGIN_X + halfWidth, cursorY + metaBoxHeight)
                paint.color = COLOR_ROW_ALT
                canvas.drawRoundRect(leftRect, 6f, 6f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.8f
                canvas.drawRoundRect(leftRect, 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                textPaint.color = COLOR_SKY_BLUE
                textPaint.textSize = 8.5f
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("BILLED TO / CUSTOMER DETAILS", MARGIN_X + 10f, cursorY + 16f, textPaint)

                textPaint.color = COLOR_NAVY_DARK
                textPaint.textSize = 10f
                canvas.drawText(partyName, MARGIN_X + 10f, cursorY + 32f, textPaint)

                textPaint.textSize = 8f
                textPaint.typeface = Typeface.DEFAULT
                textPaint.color = COLOR_TEXT_MUTED
                canvas.drawText(partyPhone, MARGIN_X + 10f, cursorY + 46f, textPaint)
                canvas.drawText(partyAddress, MARGIN_X + 10f, cursorY + 58f, textPaint)
                canvas.drawText(panText, MARGIN_X + 10f, cursorY + 70f, textPaint)

                // Right Box: Invoice Meta
                val rightX = MARGIN_X + halfWidth + 12f
                val rightRect = RectF(rightX, cursorY, rightX + halfWidth, cursorY + metaBoxHeight)
                paint.color = COLOR_ROW_ALT
                canvas.drawRoundRect(rightRect, 6f, 6f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.8f
                canvas.drawRoundRect(rightRect, 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                textPaint.color = COLOR_SKY_BLUE
                textPaint.textSize = 8.5f
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("INVOICE PARTICULARS", rightX + 10f, cursorY + 16f, textPaint)

                textPaint.color = COLOR_NAVY_DARK
                textPaint.textSize = 9.5f
                canvas.drawText("INVOICE NO: ${invoice.invoiceNumber}", rightX + 10f, cursorY + 32f, textPaint)

                textPaint.textSize = 8f
                textPaint.typeface = Typeface.DEFAULT
                textPaint.color = COLOR_TEXT_MUTED
                canvas.drawText("Date (BS): ${invoice.displayBsDate}   |   (AD): ${invoice.displayAdDate}", rightX + 10f, cursorY + 46f, textPaint)

                val dueDateStr = if (!invoice.dueDateBS.isNullOrBlank()) "${invoice.dueDateBS} (BS)" else "On Presentation"
                canvas.drawText("Payment Terms / Due: $dueDateStr", rightX + 10f, cursorY + 58f, textPaint)

                val statusStr = "Status: ${invoice.invoiceStatus.uppercase()}  |  Payment: ${invoice.paymentStatus.uppercase()}"
                canvas.drawText(statusStr, rightX + 10f, cursorY + 70f, textPaint)

                cursorY += metaBoxHeight + 10f
            } else {
                // Continuation Header for Pages 2..N
                paint.color = COLOR_NAVY_DARK
                canvas.drawRect(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + 3f, paint)
                cursorY += 12f

                textPaint.textSize = 10.5f
                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.color = COLOR_NAVY_DARK
                canvas.drawText("${businessName.uppercase()} — TAX INVOICE (CONTINUATION)", MARGIN_X, cursorY, textPaint)

                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.textSize = 8.5f
                textPaint.color = COLOR_TEXT_MUTED
                canvas.drawText("Invoice: ${invoice.invoiceNumber}  |  Customer: $partyName  |  Page $pageIndex of $totalPages", MARGIN_X + USABLE_WIDTH, cursorY, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                cursorY += 8f
                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.8f
                canvas.drawLine(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY, paint)
                cursorY += 8f
            }

            // -------------------------------------------------------------
            // Items Table Header
            // -------------------------------------------------------------
            val tableTop = cursorY
            paint.color = COLOR_NAVY_HEADER
            canvas.drawRect(MARGIN_X, tableTop, MARGIN_X + USABLE_WIDTH, tableTop + 20f, paint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 8f
            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textAlign = Paint.Align.LEFT

            val colSnX = MARGIN_X + 6f
            val colDescX = MARGIN_X + 28f
            val colCodeX = MARGIN_X + 195f
            val colQtyX = MARGIN_X + 285f
            val colRateX = MARGIN_X + 355f
            val colDiscX = MARGIN_X + 430f
            val colTotalX = MARGIN_X + USABLE_WIDTH - 6f

            canvas.drawText("S.N.", colSnX, tableTop + 13f, textPaint)
            canvas.drawText("ITEM DESCRIPTION", colDescX, tableTop + 13f, textPaint)
            canvas.drawText("SKU / CODE", colCodeX, tableTop + 13f, textPaint)
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("QTY & UNIT", colQtyX, tableTop + 13f, textPaint)
            canvas.drawText("RATE", colRateX, tableTop + 13f, textPaint)
            canvas.drawText("DISC", colDiscX, tableTop + 13f, textPaint)
            canvas.drawText("AMOUNT (RS.)", colTotalX, tableTop + 13f, textPaint)

            cursorY += 20f

            // -------------------------------------------------------------
            // Table Rows for this page
            // -------------------------------------------------------------
            val pageItems = pageItemChunks[pageIndex - 1]
            if (pageItems.isEmpty() && items.isEmpty()) {
                paint.color = COLOR_ROW_ALT
                canvas.drawRect(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + 28f, paint)
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.color = COLOR_TEXT_MUTED
                textPaint.textSize = 8.5f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                canvas.drawText("Direct Counter Sale / General Invoice without individual item breakdown", MARGIN_X + (USABLE_WIDTH / 2f), cursorY + 18f, textPaint)
                cursorY += 28f
            } else {
                pageItems.forEach { item ->
                    val isAlt = globalItemIndex % 2 == 1
                    if (isAlt) {
                        paint.color = COLOR_ROW_ALT
                        canvas.drawRect(MARGIN_X, cursorY, MARGIN_X + USABLE_WIDTH, cursorY + 18f, paint)
                    }

                    paint.color = COLOR_BORDER
                    paint.strokeWidth = 0.5f
                    canvas.drawLine(MARGIN_X, cursorY + 18f, MARGIN_X + USABLE_WIDTH, cursorY + 18f, paint)

                    val rowY = cursorY + 12.5f

                    textPaint.textAlign = Paint.Align.LEFT
                    textPaint.color = COLOR_TEXT_MUTED
                    textPaint.textSize = 8f
                    textPaint.typeface = Typeface.DEFAULT
                    canvas.drawText("${globalItemIndex + 1}", colSnX, rowY, textPaint)

                    textPaint.color = COLOR_NAVY_DARK
                    textPaint.typeface = Typeface.DEFAULT_BOLD
                    val itemName = item.productNameSnapshot.take(28)
                    canvas.drawText(itemName, colDescX, rowY, textPaint)

                    textPaint.color = COLOR_TEXT_MUTED
                    textPaint.typeface = Typeface.DEFAULT
                    val itemCode = (item.productCodeSnapshot ?: "-").take(14)
                    canvas.drawText(itemCode, colCodeX, rowY, textPaint)

                    textPaint.textAlign = Paint.Align.RIGHT
                    val qtyStr = "${String.format(Locale.US, "%.1f", item.quantity)} ${item.unit}"
                    canvas.drawText(qtyStr, colQtyX, rowY, textPaint)

                    val rateStr = currencyFormat.format(item.rate)
                    canvas.drawText(rateStr, colRateX, rowY, textPaint)

                    val discStr = if (item.discount > 0) currencyFormat.format(item.discount) else "-"
                    canvas.drawText(discStr, colDiscX, rowY, textPaint)

                    textPaint.color = COLOR_NAVY_DARK
                    textPaint.typeface = Typeface.DEFAULT_BOLD
                    val lineTotalStr = currencyFormat.format(item.lineTotal)
                    canvas.drawText(lineTotalStr, colTotalX, rowY, textPaint)

                    cursorY += 18f
                    globalItemIndex++
                }
            }

            // Outer border around items table
            paint.style = Paint.Style.STROKE
            paint.color = COLOR_BORDER
            paint.strokeWidth = 1f
            canvas.drawRect(MARGIN_X, tableTop, MARGIN_X + USABLE_WIDTH, cursorY, paint)
            paint.style = Paint.Style.FILL

            cursorY += 8f

            // -------------------------------------------------------------
            // Bottom Totals & Signatures (Drawn on the Final Page)
            // -------------------------------------------------------------
            if (pageIndex == totalPages) {
                val totalsBoxWidth = 220f
                val totalsLeftX = MARGIN_X + USABLE_WIDTH - totalsBoxWidth

                // Left Box: Payment Summary, Remarks, Terms & Created By
                val notesRect = RectF(MARGIN_X, cursorY, totalsLeftX - 12f, cursorY + 125f)
                paint.color = COLOR_ROW_ALT
                canvas.drawRoundRect(notesRect, 6f, 6f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.8f
                canvas.drawRoundRect(notesRect, 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                textPaint.textAlign = Paint.Align.LEFT
                textPaint.color = COLOR_SKY_BLUE
                textPaint.textSize = 8f
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("PAYMENT METHOD & REMARKS", MARGIN_X + 8f, cursorY + 14f, textPaint)

                textPaint.color = COLOR_NAVY_DARK
                textPaint.textSize = 8.5f
                textPaint.typeface = Typeface.DEFAULT
                val payMethodStr = "Mode: ${invoice.paymentMethod}" + if (!invoice.paymentAccountId.isNullOrBlank()) " (${invoice.paymentAccountId})" else ""
                canvas.drawText(payMethodStr, MARGIN_X + 8f, cursorY + 28f, textPaint)

                val remarksStr = if (!invoice.remarks.isNullOrBlank()) "Note: ${invoice.remarks}" else "Note: General trade invoice."
                canvas.drawText(remarksStr.take(52), MARGIN_X + 8f, cursorY + 42f, textPaint)

                val createdByStr = "Prepared by: ${invoice.createdBy ?: "System Administrator"}"
                textPaint.color = COLOR_TEXT_MUTED
                textPaint.textSize = 7.5f
                canvas.drawText(createdByStr, MARGIN_X + 8f, cursorY + 56f, textPaint)

                val termsSingleLine = terms.replace("\n", "  •  ").take(60)
                canvas.drawText("Terms: $termsSingleLine", MARGIN_X + 8f, cursorY + 70f, textPaint)

                // Show payment allocation if present
                if (payments.isNotEmpty()) {
                    val p = payments.first()
                    textPaint.color = COLOR_GREEN
                    textPaint.typeface = Typeface.DEFAULT_BOLD
                    canvas.drawText("Allocated: Rs. ${currencyFormat.format(p.paymentAmount)} on ${p.paymentDateBS}", MARGIN_X + 8f, cursorY + 86f, textPaint)
                    if (!p.transactionReference.isNullOrBlank()) {
                        textPaint.color = COLOR_TEXT_MUTED
                        textPaint.typeface = Typeface.DEFAULT
                        canvas.drawText("Ref: ${p.transactionReference}", MARGIN_X + 8f, cursorY + 98f, textPaint)
                    }
                }

                // Right Box: Calculation Breakdown
                var calcY = cursorY + 10f

                fun drawTotalLine(label: String, amount: Double, isBold: Boolean = false, isAccent: Boolean = false, isNegative: Boolean = false) {
                    textPaint.textAlign = Paint.Align.LEFT
                    textPaint.textSize = if (isBold) 9.5f else 8.5f
                    textPaint.typeface = if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    textPaint.color = if (isBold) COLOR_NAVY_DARK else COLOR_TEXT_MUTED
                    canvas.drawText(label, totalsLeftX + 6f, calcY, textPaint)

                    textPaint.textAlign = Paint.Align.RIGHT
                    textPaint.color = when {
                        isAccent -> COLOR_SKY_BLUE
                        isNegative -> COLOR_RED
                        isBold -> COLOR_NAVY_DARK
                        else -> COLOR_NAVY_DARK
                    }
                    val prefix = if (isNegative && amount > 0) "-Rs. " else "Rs. "
                    canvas.drawText("$prefix${currencyFormat.format(amount)}", MARGIN_X + USABLE_WIDTH - 6f, calcY, textPaint)
                    calcY += 14f
                }

                drawTotalLine("Subtotal", invoice.subtotal)
                if (invoice.discountAmount > 0) {
                    val discLabel = if (invoice.discountPercent > 0) "Discount (${invoice.discountPercent.toInt()}%)" else "Discount"
                    drawTotalLine(discLabel, invoice.discountAmount, isNegative = true)
                }
                if (invoice.vatAmount > 0) {
                    drawTotalLine("Taxable Amount", invoice.taxableAmount)
                    val vatLabel = if (invoice.vatRate > 0) "VAT (${invoice.vatRate.toInt()}%)" else "VAT"
                    drawTotalLine(vatLabel, invoice.vatAmount)
                }
                if (invoice.extraChargeAmount > 0) {
                    val chargeLabel = invoice.extraChargeDescription?.ifBlank { "Extra Charge" } ?: "Extra Charge"
                    drawTotalLine(chargeLabel, invoice.extraChargeAmount)
                }
                if (invoice.roundOff != 0.0) {
                    drawTotalLine("Round Off", invoice.roundOff)
                }

                // Divider
                paint.color = COLOR_BORDER
                paint.strokeWidth = 1f
                canvas.drawLine(totalsLeftX, calcY - 2f, MARGIN_X + USABLE_WIDTH, calcY - 2f, paint)
                calcY += 6f

                drawTotalLine("Grand Total", invoice.grandTotal, isBold = true, isAccent = true)
                drawTotalLine("Amount Paid", invoice.paidAmount)

                val dueColor = if (invoice.dueAmount > 0) COLOR_RED else COLOR_GREEN
                textPaint.textAlign = Paint.Align.LEFT
                textPaint.textSize = 9.5f
                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.color = dueColor
                canvas.drawText("Balance Due", totalsLeftX + 6f, calcY, textPaint)

                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Rs. ${currencyFormat.format(invoice.dueAmount)}", MARGIN_X + USABLE_WIDTH - 6f, calcY, textPaint)

                // Signatory Section
                cursorY += 135f
                if (settings.showAuthorizedSignatureBox) {
                    drawSignatorySection(canvas, cursorY)
                }
            }

            // Watermark if requested
            if (settings.pdfWatermark != "None") {
                val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(20, 100, 116, 139)
                    textSize = 52f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
                canvas.save()
                canvas.rotate(-30f, PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f)
                canvas.drawText(settings.pdfWatermark, PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f, watermarkPaint)
                canvas.restore()
            }

            // Draw Footer on every page
            if (settings.showFooterText) {
                drawFooter(canvas, pageIndex, totalPages, System.currentTimeMillis())
            }

            document.finishPage(page)
        }

        val outputFile = File(getReportsDir(context), "Invoice_${invoice.invoiceNumber.replace("#", "").replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // 7. COMPACT POS THERMAL RECEIPT (80mm RECEIPT PRINTER LAYOUT)
    // -------------------------------------------------------------------------
    fun generateSalesReceiptPdf(
        context: Context,
        invoiceWithDetails: SalesInvoiceWithDetails
    ): File {
        val invoice = invoiceWithDetails.invoice
        val party = invoiceWithDetails.party
        val profile = invoiceWithDetails.businessProfile
        val items = invoiceWithDetails.items

        val businessName = profile?.businessName?.ifBlank { "Atri Nova Tech Enterprises" } ?: "Atri Nova Tech Enterprises"
        val panVat = profile?.panVatNumber?.ifBlank { "609823415" } ?: "609823415"
        val phone = profile?.phone?.ifBlank { "+977 9852020149" } ?: "+977 9852020149"
        val address = profile?.address?.ifBlank { "Main Road, Biratnagar-6, Nepal" } ?: "Main Road, Biratnagar-6, Nepal"
        val partyName = invoice.partyNameSnapshot.ifBlank { party?.name ?: "Walk-in Cash Customer" }

        // Standard 80mm thermal receipt width in points (approx 226 pt)
        val receiptWidth = 226
        val marginX = 8f
        val usableWidth = receiptWidth - (marginX * 2)

        // Dynamic continuous roll height calculation
        val rowCount = maxOf(1, items.size)
        val calculatedHeight = 280 + (rowCount * 22) + (if (invoice.vatAmount > 0) 30 else 0) + (if (invoice.discountAmount > 0) 15 else 0)
        val receiptHeight = maxOf(380, calculatedHeight)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(receiptWidth, receiptHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        var y = 14f

        fun drawDottedDivider() {
            paint.color = COLOR_NAVY_DARK
            paint.strokeWidth = 0.8f
            var x = marginX
            while (x < marginX + usableWidth) {
                canvas.drawLine(x, y, x + 3f, y, paint)
                x += 6f
            }
            y += 8f
        }

        // 1. Business Header (Centered)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textSize = 10.5f
        textPaint.color = COLOR_NAVY_DARK
        canvas.drawText(businessName, receiptWidth / 2f, y, textPaint)
        y += 11f

        textPaint.textSize = 7f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = COLOR_NAVY_DARK
        canvas.drawText(address.take(40), receiptWidth / 2f, y, textPaint)
        y += 9f

        val phonePanText = "Tel: $phone  •  PAN: $panVat"
        canvas.drawText(phonePanText, receiptWidth / 2f, y, textPaint)
        y += 10f

        drawDottedDivider()

        // 2. Receipt Title & Metadata
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textSize = 8.5f
        val receiptTitle = if (invoice.isTaxInvoice) "TAX INVOICE / POS RECEIPT" else "SALES RECEIPT / CASH MEMO"
        canvas.drawText(receiptTitle, receiptWidth / 2f, y, textPaint)
        y += 11f

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 7f
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("INV NO: ${invoice.invoiceNumber}", marginX, y, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("DATE: ${invoice.displayBsDate} BS", marginX + usableWidth, y, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        y += 9f

        canvas.drawText("CUSTOMER: ${partyName.take(24)}", marginX, y, textPaint)
        y += 9f

        val partyPan = invoice.partyPanSnapshot?.ifBlank { null } ?: party?.panVatNumber?.ifBlank { null }
        if (partyPan != null) {
            canvas.drawText("CUST PAN: $partyPan", marginX, y, textPaint)
            y += 9f
        }

        drawDottedDivider()

        // 3. Items Table Header
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textSize = 7f
        canvas.drawText("ITEM", marginX, y, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("QTY", marginX + 115f, y, textPaint)
        canvas.drawText("RATE", marginX + 155f, y, textPaint)
        canvas.drawText("TOTAL", marginX + usableWidth, y, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        y += 10f

        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.5f
        canvas.drawLine(marginX, y, marginX + usableWidth, y, paint)
        y += 6f

        // Items List
        textPaint.typeface = Typeface.DEFAULT
        if (items.isEmpty()) {
            canvas.drawText("Direct Counter Sale", marginX, y, textPaint)
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Rs. ${currencyFormat.format(invoice.grandTotal)}", marginX + usableWidth, y, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            y += 12f
        } else {
            items.forEachIndexed { index, item ->
                // Line 1: Item Name
                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.textSize = 7f
                canvas.drawText("${index + 1}. ${item.productNameSnapshot.take(28)}", marginX, y, textPaint)
                y += 9f

                // Line 2: Qty x Rate = Amount
                textPaint.typeface = Typeface.DEFAULT
                val qtyStr = "${String.format(Locale.US, "%.1f", item.quantity)} ${item.unit}"
                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(qtyStr, marginX + 115f, y, textPaint)
                canvas.drawText(currencyFormat.format(item.rate), marginX + 155f, y, textPaint)
                canvas.drawText(currencyFormat.format(item.lineTotal), marginX + usableWidth, y, textPaint)
                textPaint.textAlign = Paint.Align.LEFT
                y += 11f
            }
        }

        drawDottedDivider()

        // 4. Totals Breakdown
        fun drawReceiptTotalRow(label: String, amount: Double, isBold: Boolean = false, isNegative: Boolean = false) {
            textPaint.typeface = if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textPaint.textSize = if (isBold) 8.5f else 7f
            canvas.drawText(label, marginX, y, textPaint)
            textPaint.textAlign = Paint.Align.RIGHT
            val prefix = if (isNegative && amount > 0) "-Rs. " else "Rs. "
            canvas.drawText("$prefix${currencyFormat.format(amount)}", marginX + usableWidth, y, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            y += if (isBold) 12f else 9.5f
        }

        drawReceiptTotalRow("Subtotal", invoice.subtotal)
        if (invoice.discountAmount > 0) {
            val discLabel = if (invoice.discountPercent > 0) "Discount (${invoice.discountPercent.toInt()}%)" else "Discount"
            drawReceiptTotalRow(discLabel, invoice.discountAmount, isNegative = true)
        }
        if (invoice.vatAmount > 0) {
            drawReceiptTotalRow("Taxable Amount", invoice.taxableAmount)
            val vatLabel = if (invoice.vatRate > 0) "VAT (${invoice.vatRate.toInt()}%)" else "VAT"
            drawReceiptTotalRow(vatLabel, invoice.vatAmount)
        }
        if (invoice.extraChargeAmount > 0) {
            val chargeLabel = invoice.extraChargeDescription?.ifBlank { "Extra Charge" } ?: "Extra Charge"
            drawReceiptTotalRow(chargeLabel, invoice.extraChargeAmount)
        }
        if (invoice.roundOff != 0.0) {
            drawReceiptTotalRow("Round Off", invoice.roundOff)
        }

        paint.color = COLOR_NAVY_DARK
        paint.strokeWidth = 0.8f
        canvas.drawLine(marginX, y, marginX + usableWidth, y, paint)
        y += 6f

        drawReceiptTotalRow("GRAND TOTAL", invoice.grandTotal, isBold = true)
        drawReceiptTotalRow("Paid Amount", invoice.paidAmount)
        drawReceiptTotalRow("Balance Due", invoice.dueAmount, isBold = true)

        drawDottedDivider()

        // 5. Payment Details & Footer
        textPaint.textSize = 7f
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("PAY METHOD: ${invoice.paymentMethod.uppercase()}", marginX, y, textPaint)
        y += 9f

        val createdByStr = "PREPARED BY: ${invoice.createdBy ?: "CASHIER"}"
        canvas.drawText(createdByStr, marginX, y, textPaint)
        y += 9f

        if (!invoice.remarks.isNullOrBlank()) {
            canvas.drawText("NOTE: ${invoice.remarks.take(35)}", marginX, y, textPaint)
            y += 9f
        }

        y += 4f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textSize = 7.5f
        val footerMsg = profile?.invoiceFooter?.ifBlank { "THANK YOU! VISIT AGAIN" } ?: "THANK YOU! VISIT AGAIN"
        canvas.drawText(footerMsg.uppercase(), receiptWidth / 2f, y, textPaint)
        y += 9f

        textPaint.textSize = 6.5f
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("ATRI KHATA ERP POS SYSTEM", receiptWidth / 2f, y, textPaint)

        document.finishPage(page)

        val outputFile = File(getReportsDir(context), "Receipt_${invoice.invoiceNumber.replace("#", "").replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return outputFile
    }

    // -------------------------------------------------------------------------
    // 8. DOWNLOAD & SAVE PDF (MEDIASTORE / PUBLIC STORAGE COMPLIANT)
    // -------------------------------------------------------------------------
    fun downloadPdf(context: Context, sourceFile: File, displayName: String): Uri? {
        val cleanName = if (displayName.endsWith(".pdf", ignoreCase = true)) displayName else "$displayName.pdf"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        sourceFile.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    uri
                } else {
                    null
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val destFile = File(downloadsDir, cleanName)
                sourceFile.copyTo(destFile, overwrite = true)
                Uri.fromFile(destFile)
            }
        } catch (_: Exception) {
            // Graceful fallback to app external documents
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

    // -------------------------------------------------------------------------
    // SYSTEM ACTIONS: PRINT, SHARE & PREVIEW
    // -------------------------------------------------------------------------

    fun printPdf(context: Context, file: File, title: String = "Financial Report") {
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

    fun sharePdf(context: Context, file: File, subject: String = "Financial Report") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "Please find attached the official $subject generated from Atri Khata.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Document via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPdf(context: Context, file: File) {
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
}
