package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.TransactionEntity
import com.example.service.pdf.PdfReportService
import com.example.ui.MainViewModel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueCardBg
import com.example.ui.theme.SkyBlueCardBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Report types supported by the Financial Report Generation Service.
 */
enum class FinancialReportType(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    TRANSACTION_LEDGER(
        "Transaction Ledger",
        "Chronological statement with Dr/Cr running balances",
        Icons.Outlined.ReceiptLong
    ),
    BALANCE_SHEET(
        "Balance Sheet",
        "Statement of financial position (Assets, Liabilities & Equity)",
        Icons.Outlined.AccountBalance
    ),
    PROFIT_AND_LOSS(
        "Profit & Loss",
        "Income, procurement cost, operating expenses & net margin",
        Icons.Outlined.TrendingUp
    ),
    PARTY_LEDGER(
        "Party Balances",
        "Receivables from customers and payables to suppliers",
        Icons.Outlined.People
    )
}

/**
 * Modal Bottom Sheet for Generating Professional Branded PDF Financial Reports from the Transactions module.
 */
@Composable
fun GenerateFinancialReportSheet(
    viewModel: MainViewModel,
    filteredTransactions: List<TransactionEntity>? = null,
    currentDateFilter: String = "All Time",
    initialReportType: FinancialReportType = FinancialReportType.TRANSACTION_LEDGER,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = AppTheme.isDark

    val allTxs by viewModel.allTransactions.collectAsState()
    val allParties by viewModel.allParties.collectAsState()
    val currentBusiness by viewModel.selectedBusiness.collectAsState()

    // Financial totals
    val totalToReceive by viewModel.totalToReceive.collectAsState()
    val totalToGive by viewModel.totalToGive.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState()
    val totalPurchases by viewModel.totalPurchases.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val cashAndBank by viewModel.cashAndBankBalance.collectAsState()

    var selectedReportType by remember { mutableStateOf(initialReportType) }
    var useFilteredTransactionsOnly by remember { mutableStateOf(filteredTransactions != null && filteredTransactions.size != allTxs.size) }

    // Custom Header Info
    var businessNameInput by remember { mutableStateOf(if (currentBusiness.isNotBlank()) currentBusiness else "Atri Nova Tech Enterprises") }
    var panVatInput by remember { mutableStateOf("609823415") }
    var contactPhoneInput by remember { mutableStateOf("+977 9852020149") }

    // Generation state
    var isGenerating by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var generationStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag("generate_financial_report_sheet")
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SkyBlueCardBg)
                        .border(1.dp, SkyBlueCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PictureAsPdf,
                        contentDescription = "PDF Report",
                        tint = SkyBlueBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Financial Report Center",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Branded PDF Export & Printing Service",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_financial_report_dialog")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 1. SELECT REPORT TYPE
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SELECT FINANCIAL STATEMENT TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = if (isDark) SkyBlueBright else SkyBlue
                )
                Spacer(modifier = Modifier.height(10.dp))

                FinancialReportType.values().forEach { reportType ->
                    val isSelected = selectedReportType == reportType
                    val animatedBg by animateColorAsState(
                        targetValue = if (isSelected) SkyBlueCardBg else (if (isDark) SurfaceDark else Color(0xFFF1F5F9)),
                        label = "reportTypeBg"
                    )
                    val animatedBorder by animateColorAsState(
                        targetValue = if (isSelected) SkyBlue else (if (isDark) CardBorder else Color(0xFFCBD5E1)),
                        label = "reportTypeBorder"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(animatedBg)
                            .border(1.2.dp, animatedBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedReportType = reportType
                                generatedPdfFile = null // reset previously generated file
                            }
                            .padding(12.dp)
                            .testTag("report_type_${reportType.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) SkyBlue.copy(alpha = 0.2f) else (if (isDark) CardDark else Color(0xFFE2E8F0))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = reportType.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) SkyBlueBright else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reportType.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) (if (isDark) TextWhite else Color(0xFF0F172A)) else (if (isDark) TextWhite else Color(0xFF334155))
                                )
                                Text(
                                    text = reportType.subtitle,
                                    fontSize = 11.sp,
                                    color = if (isSelected) SkyBlueBright else TextSubtle
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = SkyBlueBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. SCOPE & TRANSACTION FILTER SELECTION
        if (selectedReportType == FinancialReportType.TRANSACTION_LEDGER && filteredTransactions != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TRANSACTION SCOPE & DATE RANGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = if (isDark) SkyBlueBright else SkyBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Option 1: Current View Filter
                        ScopeOptionButton(
                            title = "Filtered Transactions",
                            subtitle = "${filteredTransactions.size} txs ($currentDateFilter)",
                            isSelected = useFilteredTransactionsOnly,
                            onClick = {
                                useFilteredTransactionsOnly = true
                                generatedPdfFile = null
                            },
                            modifier = Modifier.weight(1f),
                            isDark = isDark
                        )

                        // Option 2: All Records
                        ScopeOptionButton(
                            title = "All Transactions",
                            subtitle = "${allTxs.size} total entries",
                            isSelected = !useFilteredTransactionsOnly,
                            onClick = {
                                useFilteredTransactionsOnly = false
                                generatedPdfFile = null
                            },
                            modifier = Modifier.weight(1f),
                            isDark = isDark
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. BRANDING & HEADER CUSTOMIZATION
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "REPORT BRANDING & COMPANY INFORMATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = if (isDark) SkyBlueBright else SkyBlue
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = businessNameInput,
                    onValueChange = { businessNameInput = it },
                    label = { Text("Business / Firm Name") },
                    colors = reportTextFieldColors(isDark),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("report_business_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = panVatInput,
                        onValueChange = { panVatInput = it },
                        label = { Text("PAN / VAT Registration") },
                        colors = reportTextFieldColors(isDark),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("report_pan_vat_input")
                    )

                    OutlinedTextField(
                        value = contactPhoneInput,
                        onValueChange = { contactPhoneInput = it },
                        label = { Text("Phone Number") },
                        colors = reportTextFieldColors(isDark),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("report_phone_input")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. GENERATED FILE STATUS CARD (IF GENERATED)
        AnimatedVisibility(visible = generatedPdfFile != null) {
            generatedPdfFile?.let { file ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else Color(0xFFECFDF5)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PDF Generated Successfully!",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) TextWhite else Color(0xFF065F46),
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                text = "${file.length() / 1024} KB",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = file.name,
                            fontSize = 12.sp,
                            color = TextSubtle
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions for Generated File: Print, Share, Open
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Print Action
                            Button(
                                onClick = {
                                    PdfReportService.printPdf(context, file, selectedReportType.title)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("print_generated_pdf_button")
                            ) {
                                Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print", fontSize = 13.sp)
                            }

                            // Share Action
                            Button(
                                onClick = {
                                    PdfReportService.sharePdf(context, file, selectedReportType.title)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("share_generated_pdf_button")
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 13.sp)
                            }

                            // Open / Preview Action
                            OutlinedButton(
                                onClick = {
                                    PdfReportService.openPdf(context, file)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("open_generated_pdf_button")
                            ) {
                                Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // PRIMARY GENERATE BUTTON
        Button(
            onClick = {
                scope.launch {
                    isGenerating = true
                    generationStatusMessage = "Formatting and rendering PDF..."

                    try {
                        val header = PdfReportService.ReportHeaderInfo(
                            businessName = businessNameInput.ifBlank { "Atri Nova Tech Enterprises" },
                            panVat = panVatInput.ifBlank { "609823415" },
                            phone = contactPhoneInput.ifBlank { "+977 9852020149" },
                            datePeriod = if (useFilteredTransactionsOnly) currentDateFilter else "All Time",
                            generatedAt = System.currentTimeMillis()
                        )

                        val file: File = withContext(Dispatchers.IO) {
                            when (selectedReportType) {
                                FinancialReportType.TRANSACTION_LEDGER -> {
                                    val txList = if (useFilteredTransactionsOnly && filteredTransactions != null) {
                                        filteredTransactions
                                    } else {
                                        allTxs
                                    }
                                    PdfReportService.generateTransactionLedgerPdf(
                                        context = context,
                                        header = header,
                                        transactions = txList,
                                        filterType = if (useFilteredTransactionsOnly) currentDateFilter else "All"
                                    )
                                }
                                FinancialReportType.BALANCE_SHEET -> {
                                    PdfReportService.generateBalanceSheetPdf(
                                        context = context,
                                        header = header,
                                        totalReceivables = totalToReceive,
                                        totalPayables = totalToGive,
                                        cashAndBank = cashAndBank,
                                        inventoryValue = 78500.00
                                    )
                                }
                                FinancialReportType.PROFIT_AND_LOSS -> {
                                    PdfReportService.generateProfitLossPdf(
                                        context = context,
                                        header = header,
                                        totalSales = totalSales,
                                        totalPurchases = totalPurchases,
                                        totalExpenses = totalExpenses
                                    )
                                }
                                FinancialReportType.PARTY_LEDGER -> {
                                    PdfReportService.generatePartyLedgerPdf(
                                        context = context,
                                        header = header,
                                        parties = allParties
                                    )
                                }
                            }
                        }

                        generatedPdfFile = file
                        Toast.makeText(context, "Report generated: ${file.name}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to generate report: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } finally {
                        isGenerating = false
                    }
                }
            },
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(
                containerColor = SkyBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("generate_pdf_report_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Rendering Vector PDF...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            } else {
                Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (generatedPdfFile != null) "Regenerate PDF Report" else "Generate Branded PDF Report",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Direct Print Button
        OutlinedButton(
            onClick = {
                scope.launch {
                    val header = PdfReportService.ReportHeaderInfo(
                        businessName = businessNameInput.ifBlank { "Atri Nova Tech Enterprises" },
                        panVat = panVatInput.ifBlank { "609823415" },
                        phone = contactPhoneInput.ifBlank { "+977 9852020149" },
                        datePeriod = if (useFilteredTransactionsOnly) currentDateFilter else "All Time",
                        generatedAt = System.currentTimeMillis()
                    )

                    val file: File = withContext(Dispatchers.IO) {
                        if (generatedPdfFile != null) {
                            generatedPdfFile!!
                        } else {
                            val txList = if (useFilteredTransactionsOnly && filteredTransactions != null) filteredTransactions else allTxs
                            PdfReportService.generateTransactionLedgerPdf(context, header, txList)
                        }
                    }
                    generatedPdfFile = file
                    PdfReportService.printPdf(context, file, selectedReportType.title)
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("direct_print_report_button")
        ) {
            Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Direct Print via Android Spooler", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ScopeOptionButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SkyBlueCardBg else (if (isDark) SurfaceDark else Color(0xFFF1F5F9)))
            .border(
                1.2.dp,
                if (isSelected) SkyBlue else (if (isDark) CardBorder else Color(0xFFCBD5E1)),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) (if (isDark) TextWhite else Color(0xFF0F172A)) else TextMuted
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) SkyBlueBright else TextSubtle
            )
        }
    }
}

@Composable
private fun reportTextFieldColors(isDark: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = if (isDark) TextWhite else Color(0xFF0F172A),
    unfocusedTextColor = if (isDark) TextWhite else Color(0xFF0F172A),
    focusedBorderColor = if (isDark) SkyBlueBright else SkyBlue,
    unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1),
    focusedLabelColor = if (isDark) SkyBlueBright else SkyBlue,
    unfocusedLabelColor = TextMuted,
    cursorColor = if (isDark) SkyBlueBright else SkyBlue,
    focusedContainerColor = if (isDark) SurfaceDark else Color.White,
    unfocusedContainerColor = if (isDark) SurfaceDark else Color.White
)
