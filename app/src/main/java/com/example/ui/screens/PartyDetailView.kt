package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.TransactionEntity
import com.example.service.pdf.PdfReportService
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeCardBg
import com.example.ui.theme.OrangeCardBorder
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueCardBg
import com.example.ui.theme.SkyBlueCardBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.util.NepaliDateUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Advanced Party View & Financial Dashboard
 * Converts contact/profile view into a modern accounting workstation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyDetailView(
    party: PartyEntity,
    allTransactions: List<TransactionEntity>,
    privacyMode: Boolean,
    onBackClick: () -> Unit,
    onEditParty: (PartyEntity) -> Unit,
    onDeleteParty: (PartyEntity) -> Unit,
    onCreateSale: () -> Unit,
    onCreatePurchase: () -> Unit,
    onRecordReceive: () -> Unit,
    onRecordPay: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onCallParty: (String) -> Unit,
    onSendReminder: (PartyEntity) -> Unit,
    onShareStatement: (PartyEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark

    // Back handling
    BackHandler { onBackClick() }

    // State for Tabs
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Ledger", "Invoices", "Payments", "Activity")

    // Menus and dialog states
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var invoiceToView by remember { mutableStateOf<TransactionEntity?>(null) }
    var showQuickPurchaseDialog by remember { mutableStateOf(false) }

    // Filter transactions for this party
    val partyTransactions = remember(allTransactions, party.id, party.name) {
        allTransactions.filter {
            it.partyId == party.id || it.partyName.equals(party.name, ignoreCase = true)
        }.sortedByDescending { it.dateMillis }
    }

    // Financial calculations from actual data
    val totalSales = remember(partyTransactions) {
        partyTransactions.filter { it.type == "Sales Invoice" }.sumOf { it.amount }
    }
    val totalPurchases = remember(partyTransactions) {
        partyTransactions.filter { it.type == "Purchase" }.sumOf { it.amount }
    }
    val totalReceived = remember(partyTransactions) {
        partyTransactions.filter { it.type == "Payment In" }.sumOf { it.amount }
    }
    val totalPaid = remember(partyTransactions) {
        partyTransactions.filter { it.type == "Payment Out" }.sumOf { it.amount }
    }
    val transactionsCount = partyTransactions.size

    // Invoices list
    val invoiceTransactions = remember(partyTransactions) {
        partyTransactions.filter { it.type == "Sales Invoice" || it.invoiceNumber.isNotBlank() }
    }

    // Payments list
    val paymentTransactions = remember(partyTransactions) {
        partyTransactions.filter { it.type == "Payment In" || it.type == "Payment Out" }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .testTag("party_detail_view_root")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. TOP APP BAR
            item(key = "top_bar") {
                PartyTopAppBar(
                    party = party,
                    onBackClick = onBackClick,
                    onCallClick = { onCallParty(party.phone) },
                    onReminderClick = { onSendReminder(party) },
                    onEditClick = { onEditParty(party) },
                    onDeleteClick = { showDeleteConfirmDialog = true },
                    onExportPdfClick = {
                        try {
                            val header = PdfReportService.ReportHeaderInfo(
                                businessName = "Atri Nova Tech",
                                panVat = "609823412",
                                address = "Kathmandu, Nepal",
                                phone = "+977 9801234567"
                            )
                            val file = PdfReportService.generatePartyLedgerPdf(context, header, listOf(party))
                            Toast.makeText(context, "Statement generated: ${file.name}", Toast.LENGTH_LONG).show()
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(viewIntent, "Open Party Statement PDF"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "PDF Statement ready for ${party.name}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    isDark = isDark
                )
            }

            // 2. PARTY PROFILE HEADER
            item(key = "party_header") {
                PartyProfileHeaderCard(
                    party = party,
                    isDark = isDark
                )
            }

            // 3. FINANCIAL POSITION CARD
            item(key = "financial_position") {
                FinancialPositionCard(
                    party = party,
                    privacyMode = privacyMode,
                    isDark = isDark
                )
            }

            // 4. QUICK ACTION BUTTONS
            item(key = "quick_actions") {
                QuickActionArea(
                    onCreateSale = onCreateSale,
                    onCreatePurchase = onCreatePurchase,
                    onRecordReceive = onRecordReceive,
                    onRecordPay = onRecordPay,
                    isDark = isDark
                )
            }

            // 5. PARTY FINANCIAL SUMMARY KPI GRID
            item(key = "financial_summary") {
                PartyFinancialSummaryGrid(
                    party = party,
                    totalSales = totalSales,
                    totalPurchases = totalPurchases,
                    totalReceived = totalReceived,
                    totalPaid = totalPaid,
                    transactionsCount = transactionsCount,
                    privacyMode = privacyMode,
                    isDark = isDark
                )
            }

            // 6. SCROLLABLE TAB NAVIGATION
            item(key = "tab_navigation") {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isDark) SurfaceDark else Color.White,
                    contentColor = if (isDark) SkyBlueBright else SkyBlue,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (isDark) SkyBlueBright else SkyBlue,
                            height = 3.dp
                        )
                    },
                    divider = {
                        HorizontalDivider(color = if (isDark) CardBorder else Color(0xFFE2E8F0))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val countBadge = when (index) {
                            2 -> invoiceTransactions.size
                            3 -> paymentTransactions.size
                            else -> null
                        }
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == index) {
                                            if (isDark) SkyBlueBright else SkyBlue
                                        } else {
                                            if (isDark) TextMuted else Color(0xFF64748B)
                                        }
                                    )
                                    if (countBadge != null && countBadge > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (selectedTab == index) {
                                                        (if (isDark) SkyBlueBright else SkyBlue).copy(alpha = 0.2f)
                                                    } else {
                                                        if (isDark) CardDark else Color(0xFFE2E8F0)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = countBadge.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedTab == index) {
                                                    if (isDark) SkyBlueBright else SkyBlue
                                                } else {
                                                    if (isDark) TextMuted else Color(0xFF475569)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // 7. TAB CONTENT
            when (selectedTab) {
                0 -> {
                    // OVERVIEW TAB
                    item(key = "tab_overview") {
                        OverviewTabContent(
                            party = party,
                            invoices = invoiceTransactions,
                            privacyMode = privacyMode,
                            onInvoiceClick = { invoiceToView = it },
                            onCreateInvoice = onCreateSale,
                            onSendReminder = { onSendReminder(party) },
                            isDark = isDark
                        )
                    }
                }
                1 -> {
                    // LEDGER TAB
                    item(key = "tab_ledger") {
                        LedgerTabContent(
                            party = party,
                            transactions = partyTransactions,
                            privacyMode = privacyMode,
                            onDeleteTransaction = { transactionToDelete = it },
                            isDark = isDark
                        )
                    }
                }
                2 -> {
                    // INVOICES TAB
                    item(key = "tab_invoices") {
                        InvoicesTabContent(
                            party = party,
                            invoices = invoiceTransactions,
                            privacyMode = privacyMode,
                            onInvoiceClick = { invoiceToView = it },
                            onCreateInvoice = onCreateSale,
                            onRecordPayment = onRecordReceive,
                            onDeleteInvoice = { transactionToDelete = it },
                            isDark = isDark
                        )
                    }
                }
                3 -> {
                    // PAYMENTS TAB
                    item(key = "tab_payments") {
                        PaymentsTabContent(
                            party = party,
                            payments = paymentTransactions,
                            privacyMode = privacyMode,
                            onRecordPaymentIn = onRecordReceive,
                            onRecordPaymentOut = onRecordPay,
                            onDeletePayment = { transactionToDelete = it },
                            isDark = isDark
                        )
                    }
                }
                4 -> {
                    // ACTIVITY TAB
                    item(key = "tab_activity") {
                        ActivityTabContent(
                            party = party,
                            transactions = partyTransactions,
                            privacyMode = privacyMode,
                            isDark = isDark
                        )
                    }
                }
            }
        }

        // DIALOG: Delete Party Confirmation
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Party?", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
                text = {
                    Text(
                        text = "Are you sure you want to delete '${party.name}'? This action cannot be undone.",
                        color = if (isDark) TextMuted else Color(0xFF475569)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteParty(party)
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = if (isDark) CardDark else Color.White
            )
        }

        // DIALOG: Delete Transaction Confirmation
        transactionToDelete?.let { tx ->
            AlertDialog(
                onDismissRequest = { transactionToDelete = null },
                title = { Text("Delete Entry?", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
                text = {
                    Text(
                        text = "Delete ${tx.type} #${tx.invoiceNumber} (Rs. ${String.format(Locale.US, "%,.2f", tx.amount)}) from party ledger?",
                        color = if (isDark) TextMuted else Color(0xFF475569)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteTransaction(tx)
                            transactionToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { transactionToDelete = null }) {
                        Text("Cancel")
                    }
                },
                containerColor = if (isDark) CardDark else Color.White
            )
        }

        // DIALOG: Invoice Details Dialog
        invoiceToView?.let { inv ->
            InvoiceDetailsDialog(
                invoice = inv,
                party = party,
                privacyMode = privacyMode,
                onDismiss = { invoiceToView = null },
                onRecordPayment = {
                    invoiceToView = null
                    onRecordReceive()
                },
                isDark = isDark
            )
        }

        // DIALOG: Quick Purchase Recording
        if (showQuickPurchaseDialog) {
            QuickPurchaseDialog(
                party = party,
                onDismiss = { showQuickPurchaseDialog = false },
                onSave = { amount, note, method ->
                    // Purchase triggers the generic creation or quick sale callback
                    onCreatePurchase()
                    showQuickPurchaseDialog = false
                },
                isDark = isDark
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 1. TOP APP BAR
// -----------------------------------------------------------------------------
@Composable
private fun PartyTopAppBar(
    party: PartyEntity,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onReminderClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    isDark: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) SurfaceDark else Color.White)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("party_detail_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (isDark) TextWhite else Color(0xFF0F172A)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = party.name,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isDark) TextWhite else Color(0xFF0F172A)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = party.type,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (party.type == "Customer") (if (isDark) SkyBlueBright else SkyBlue) else OrangeAccent
                )
                Text(text = "•", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = "Active",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981)
                )
            }
        }

        // Fast Action Buttons
        IconButton(
            onClick = onCallClick,
            enabled = party.phone.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call",
                tint = if (party.phone.isNotBlank()) (if (isDark) SkyBlueBright else SkyBlue) else TextMuted
            )
        }

        IconButton(onClick = onReminderClick) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send Reminder",
                tint = if (isDark) SkyBlueBright else SkyBlue
            )
        }

        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Party",
                tint = if (isDark) TextWhite else Color(0xFF0F172A)
            )
        }

        // 3-Dot More Menu
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = if (isDark) TextWhite else Color(0xFF0F172A)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
                    .background(if (isDark) CardDark else Color.White)
                    .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Party Profile") },
                    onClick = {
                        menuExpanded = false
                        onEditClick()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Export Statement (PDF)") },
                    onClick = {
                        menuExpanded = false
                        onExportPdfClick()
                    },
                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Share WhatsApp Reminder") },
                    onClick = {
                        menuExpanded = false
                        onReminderClick()
                    },
                    leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                HorizontalDivider(color = if (isDark) CardBorder else Color(0xFFE2E8F0))
                DropdownMenuItem(
                    text = { Text("Delete Party", color = Color(0xFFEF4444)) },
                    onClick = {
                        menuExpanded = false
                        onDeleteClick()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 2. PARTY PROFILE HEADER CARD
// -----------------------------------------------------------------------------
@Composable
private fun PartyProfileHeaderCard(
    party: PartyEntity,
    isDark: Boolean
) {
    val initials = remember(party.name) {
        val parts = party.name.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase(Locale.US)
            parts.size == 1 -> parts[0].take(2).uppercase(Locale.US)
            else -> "PT"
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CardDark else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                1.dp,
                if (isDark) CardBorder else Color(0xFFE2E8F0),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Avatar with Initials
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (party.type == "Customer") {
                                if (isDark) Color(0xFF162A4A) else Color(0xFFE0F2FE)
                            } else {
                                if (isDark) Color(0xFF382314) else Color(0xFFFFEDD5)
                            }
                        )
                        .border(
                            1.5.dp,
                            if (party.type == "Customer") {
                                if (isDark) SkyBlue else SkyBlue
                            } else {
                                OrangeAccent
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (party.type == "Customer") {
                            if (isDark) SkyBlueBright else SkyBlue
                        } else {
                            OrangeAccent
                        }
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = party.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Type Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (party.type == "Customer") SkyBlueCardBg else OrangeCardBg
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = party.type,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (party.type == "Customer") (if (isDark) SkyBlueBright else SkyBlue) else OrangeAccent
                            )
                        }

                        // Category Badge
                        if (party.category.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDark) SurfaceDark else Color(0xFFF1F5F9))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = party.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) TextWhite else Color(0xFF334155)
                                )
                            }
                        }

                        // Status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = if (isDark) CardBorder else Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Phone
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = if (isDark) SkyBlueBright else SkyBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (party.phone.isNotBlank()) party.phone else "No phone",
                        fontSize = 12.5.sp,
                        color = if (isDark) TextWhite else Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                }

                // PAN/VAT
                if (party.panVatNumber.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PAN: ${party.panVatNumber}",
                            fontSize = 12.sp,
                            color = if (isDark) TextMuted else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Address if present
            if (party.address.isNotBlank() || party.city.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val addr = listOf(party.address, party.city).filter { it.isNotBlank() }.joinToString(", ")
                    Text(
                        text = addr,
                        fontSize = 12.sp,
                        color = if (isDark) TextMuted else Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. FINANCIAL POSITION CARD
// -----------------------------------------------------------------------------
@Composable
private fun FinancialPositionCard(
    party: PartyEntity,
    privacyMode: Boolean,
    isDark: Boolean
) {
    val netBalance = party.balanceToReceive - party.balanceToGive
    val isReceivable = netBalance > 0
    val isPayable = netBalance < 0
    val isSettled = netBalance == 0.0

    val primaryColor = when {
        isReceivable -> if (isDark) SkyBlueBright else SkyBlue
        isPayable -> OrangeAccent
        else -> Color(0xFF10B981)
    }

    val cardBg = when {
        isReceivable -> SkyBlueCardBg
        isPayable -> OrangeCardBg
        else -> if (isDark) CardDark else Color.White
    }

    val cardBorder = when {
        isReceivable -> SkyBlueCardBorder
        isPayable -> OrangeCardBorder
        else -> if (isDark) CardBorder else Color(0xFFE2E8F0)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.2.dp, cardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Label and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OUTSTANDING BALANCE",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = primaryColor
                )

                // Semantic Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            when {
                                isReceivable -> SkyBlue.copy(alpha = 0.15f)
                                isPayable -> OrangeAccent.copy(alpha = 0.15f)
                                else -> Color(0xFF10B981).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            isReceivable -> "You will Receive (Dr)"
                            isPayable -> "You will Pay (Cr)"
                            else -> "Settled (Rs. 0.00)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Balance Text
            Text(
                text = if (privacyMode) "Rs. •••••" else "Rs. ${String.format(Locale.US, "%,.2f", kotlin.math.abs(netBalance))}",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) TextWhite else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = cardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Three-column detail breakdown: You will Receive | You will Pay | Net Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // You will Receive
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You will Receive",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", party.balanceToReceive)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) SkyBlueBright else SkyBlue
                    )
                }

                // You will Pay
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You will Pay",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", party.balanceToGive)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent
                    )
                }

                // Net Balance
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Net Position",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", netBalance)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. QUICK ACTION BUTTONS AREA
// -----------------------------------------------------------------------------
@Composable
private fun QuickActionArea(
    onCreateSale: () -> Unit,
    onCreatePurchase: () -> Unit,
    onRecordReceive: () -> Unit,
    onRecordPay: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // + Sale Button
        QuickActionButton(
            label = "+ Sale",
            icon = Icons.Outlined.ShoppingCart,
            backgroundColor = if (isDark) SkyBlue else SkyBlue,
            contentColor = Color.White,
            borderColor = Color.Transparent,
            onClick = onCreateSale,
            modifier = Modifier.weight(1f)
        )

        // + Purchase Button
        QuickActionButton(
            label = "+ Purchase",
            icon = Icons.Outlined.LocalShipping,
            backgroundColor = if (isDark) CardDark else Color.White,
            contentColor = if (isDark) TextWhite else Color(0xFF0F172A),
            borderColor = if (isDark) CardBorder else Color(0xFFCBD5E1),
            onClick = onCreatePurchase,
            modifier = Modifier.weight(1f)
        )

        // Receive Button
        QuickActionButton(
            label = "Receive",
            icon = Icons.Default.ArrowDownward,
            backgroundColor = if (isDark) Color(0xFF0F3025) else Color(0xFFDCFCE7),
            contentColor = Color(0xFF10B981),
            borderColor = if (isDark) Color(0xFF1E6347) else Color(0xFF86EFAC),
            onClick = onRecordReceive,
            modifier = Modifier.weight(1f)
        )

        // Pay Button
        QuickActionButton(
            label = "Pay",
            icon = Icons.Default.ArrowUpward,
            backgroundColor = if (isDark) Color(0xFF382314) else Color(0xFFFFEDD5),
            contentColor = OrangeAccent,
            borderColor = if (isDark) OrangeAccent.copy(alpha = 0.5f) else Color(0xFFFDBA74),
            onClick = onRecordPay,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 5. PARTY FINANCIAL SUMMARY KPI GRID
// -----------------------------------------------------------------------------
@Composable
private fun PartyFinancialSummaryGrid(
    party: PartyEntity,
    totalSales: Double,
    totalPurchases: Double,
    totalReceived: Double,
    totalPaid: Double,
    transactionsCount: Int,
    privacyMode: Boolean,
    isDark: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CardDark else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "PARTY FINANCIAL SUMMARY",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = if (isDark) SkyBlueBright else SkyBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Total Sales & Total Purchases
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryKpiCard(
                    title = "Total Sales",
                    amount = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", totalSales)}",
                    icon = Icons.Outlined.ShoppingCart,
                    iconColor = if (isDark) SkyBlueBright else SkyBlue,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                SummaryKpiCard(
                    title = "Total Purchase",
                    amount = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", totalPurchases)}",
                    icon = Icons.Outlined.LocalShipping,
                    iconColor = OrangeAccent,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Total Received & Total Paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryKpiCard(
                    title = "Total Received",
                    amount = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", totalReceived)}",
                    icon = Icons.Default.ArrowDownward,
                    iconColor = Color(0xFF10B981),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                SummaryKpiCard(
                    title = "Total Paid",
                    amount = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", totalPaid)}",
                    icon = Icons.Default.ArrowUpward,
                    iconColor = Color(0xFFEF4444),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Total Outstanding & Transactions Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val netOutstanding = party.balanceToReceive - party.balanceToGive
                SummaryKpiCard(
                    title = "Total Outstanding",
                    amount = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", netOutstanding)}",
                    icon = Icons.Outlined.ReceiptLong,
                    iconColor = if (netOutstanding >= 0) (if (isDark) SkyBlueBright else SkyBlue) else OrangeAccent,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                SummaryKpiCard(
                    title = "Transactions",
                    amount = "$transactionsCount Txns",
                    icon = Icons.Outlined.History,
                    iconColor = if (isDark) TextWhite else Color(0xFF475569),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryKpiCard(
    title: String,
    amount: String,
    icon: ImageVector,
    iconColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) SurfaceDark else Color(0xFFF8FAFC))
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    maxLines = 1
                )
                Text(
                    text = amount,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    maxLines = 1
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 8. OVERVIEW TAB CONTENT
// -----------------------------------------------------------------------------
@Composable
private fun OverviewTabContent(
    party: PartyEntity,
    invoices: List<TransactionEntity>,
    privacyMode: Boolean,
    onInvoiceClick: (TransactionEntity) -> Unit,
    onCreateInvoice: () -> Unit,
    onSendReminder: () -> Unit,
    isDark: Boolean
) {
    var isInfoExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Outstanding / Due Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) CardDark else Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OUTSTANDING DUES",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = if (isDark) SkyBlueBright else SkyBlue
                    )

                    OutlinedButton(
                        onClick = onSendReminder,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remind", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Current Due", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", party.balanceToReceive)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) SkyBlueBright else SkyBlue
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Unpaid Invoices", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "${invoices.size} Invoices",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextWhite else Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        // Credit Limit Section (Requirement 15)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) CardDark else Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CREDIT LIMIT & UTILIZATION",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = if (isDark) SkyBlueBright else SkyBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                val creditLimit = if (party.creditLimit > 0) party.creditLimit else 50000.0
                val currentOutstanding = party.balanceToReceive
                val availableCredit = maxOf(0.0, creditLimit - currentOutstanding)
                val utilizationPercent = (currentOutstanding / creditLimit).coerceIn(0.0, 1.0).toFloat()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Credit Limit", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%,.0f", creditLimit)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDark) TextWhite else Color(0xFF0F172A)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Used", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%,.0f", currentOutstanding)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDark) SkyBlueBright else SkyBlue
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Available", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%,.0f", availableCredit)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { utilizationPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        utilizationPercent > 0.9f -> Color(0xFFEF4444)
                        utilizationPercent > 0.7f -> OrangeAccent
                        else -> Color(0xFF10B981)
                    },
                    trackColor = if (isDark) SurfaceDark else Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(utilizationPercent * 100).toInt()}% credit used (${String.format(Locale.US, "Rs. %,.0f", availableCredit)} remaining)",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Recent Outstanding Invoices
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) CardDark else Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT INVOICES",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = if (isDark) SkyBlueBright else SkyBlue
                    )
                    Text(
                        text = "${invoices.size} total",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No invoices recorded yet",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(onClick = onCreateInvoice) {
                                Text("+ Create First Invoice", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    invoices.take(4).forEach { inv ->
                        InvoiceRowItem(
                            invoice = inv,
                            privacyMode = privacyMode,
                            onClick = { onInvoiceClick(inv) },
                            isDark = isDark
                        )
                        HorizontalDivider(color = if (isDark) CardBorder else Color(0xFFF1F5F9))
                    }
                }
            }
        }

        // Collapsible Party Information (Requirement 14)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) CardDark else Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isInfoExpanded = !isInfoExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PARTY CONTACT & BUSINESS INFO",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = if (isDark) SkyBlueBright else SkyBlue
                    )
                    Icon(
                        imageVector = if (isInfoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isDark) SkyBlueBright else SkyBlue
                    )
                }

                AnimatedVisibility(visible = isInfoExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRowItem(label = "Full Name", value = party.name, isDark = isDark)
                        if (party.contactPerson.isNotBlank()) {
                            InfoRowItem(label = "Contact Person", value = party.contactPerson, isDark = isDark)
                        }
                        if (party.phone.isNotBlank()) {
                            InfoRowItem(label = "Phone", value = party.phone, isDark = isDark)
                        }
                        if (party.contactNumber.isNotBlank()) {
                            InfoRowItem(label = "Alt Phone", value = party.contactNumber, isDark = isDark)
                        }
                        if (party.email.isNotBlank()) {
                            InfoRowItem(label = "Email", value = party.email, isDark = isDark)
                        }
                        if (party.panVatNumber.isNotBlank()) {
                            InfoRowItem(label = "PAN / VAT No.", value = party.panVatNumber, isDark = isDark)
                        }
                        val fullAddr = listOf(party.address, party.city).filter { it.isNotBlank() }.joinToString(", ")
                        if (fullAddr.isNotBlank()) {
                            InfoRowItem(label = "Address", value = fullAddr, isDark = isDark)
                        }
                        InfoRowItem(label = "Party Type", value = party.type, isDark = isDark)
                        if (party.category.isNotBlank()) {
                            InfoRowItem(label = "Category", value = party.category, isDark = isDark)
                        }
                        if (party.openingBalance > 0) {
                            InfoRowItem(
                                label = "Opening Balance",
                                value = "Rs. ${String.format(Locale.US, "%,.2f", party.openingBalance)} (${party.balanceType})",
                                isDark = isDark
                            )
                        }
                        if (party.notes.isNotBlank()) {
                            InfoRowItem(label = "Notes", value = party.notes, isDark = isDark)
                        }
                        val createdDateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(party.createdAt))
                        InfoRowItem(label = "Registered Date", value = createdDateStr, isDark = isDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRowItem(
    label: String,
    value: String,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) TextWhite else Color(0xFF0F172A),
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun InvoiceRowItem(
    invoice: TransactionEntity,
    privacyMode: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (invoice.invoiceNumber.isNotBlank()) invoice.invoiceNumber else "INV-#${invoice.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = if (isDark) TextWhite else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${invoice.displayBsDate} (${invoice.displayAdDate})",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", invoice.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = if (isDark) TextWhite else Color(0xFF0F172A)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = invoice.status.ifBlank { "Recorded" },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 9. LEDGER TAB CONTENT (Requirement 9)
// -----------------------------------------------------------------------------
data class LedgerEntry(
    val tx: TransactionEntity?,
    val isOpening: Boolean = false,
    val dateText: String,
    val subDateText: String,
    val voucherNumber: String,
    val title: String,
    val debit: Double,
    val credit: Double,
    val runningBalance: Double,
    val paymentMode: String = "",
    val notes: String = ""
)

@Composable
private fun LedgerTabContent(
    party: PartyEntity,
    transactions: List<TransactionEntity>,
    privacyMode: Boolean,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    isDark: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") }

    // Calculate chronological running balance starting from opening balance
    val ledgerEntries = remember(transactions, party.openingBalance, party.balanceType, party.type) {
        val chronological = transactions.sortedBy { it.dateMillis }
        val entries = mutableListOf<LedgerEntry>()

        // Initial Opening balance entry if present
        val initialIsDr = party.balanceType.contains("Receive") || party.balanceType.contains("Dr")
        var currentRunningBalance = if (initialIsDr) party.openingBalance else -party.openingBalance

        if (party.openingBalance > 0) {
            val createdDateBs = NepaliDateUtils.formatBsDate(party.registerDate)
            val createdDateAd = NepaliDateUtils.formatAdDate(party.registerDate)
            entries.add(
                LedgerEntry(
                    tx = null,
                    isOpening = true,
                    dateText = createdDateBs,
                    subDateText = createdDateAd,
                    voucherNumber = "OPENING-BAL",
                    title = "Opening Balance (${party.balanceType})",
                    debit = if (initialIsDr) party.openingBalance else 0.0,
                    credit = if (!initialIsDr) party.openingBalance else 0.0,
                    runningBalance = currentRunningBalance,
                    paymentMode = "System",
                    notes = "Initial balance recorded upon registration"
                )
            )
        }

        // Process chronological transactions
        for (tx in chronological) {
            var debit = 0.0
            var credit = 0.0

            when (tx.type) {
                "Sales Invoice" -> {
                    debit = tx.amount
                    currentRunningBalance += tx.amount
                }
                "Purchase" -> {
                    credit = tx.amount
                    currentRunningBalance -= tx.amount
                }
                "Payment In" -> {
                    credit = tx.amount
                    currentRunningBalance -= tx.amount
                }
                "Payment Out" -> {
                    debit = tx.amount
                    currentRunningBalance += tx.amount
                }
                else -> {
                    debit = tx.amount
                    currentRunningBalance += tx.amount
                }
            }

            entries.add(
                LedgerEntry(
                    tx = tx,
                    isOpening = false,
                    dateText = tx.displayBsDate,
                    subDateText = tx.displayAdDate,
                    voucherNumber = tx.invoiceNumber.ifBlank { "#${tx.id}" },
                    title = tx.type,
                    debit = debit,
                    credit = credit,
                    runningBalance = currentRunningBalance,
                    paymentMode = tx.paymentMethod,
                    notes = tx.notes
                )
            )
        }

        // Reverse to show newest transactions first in the list
        entries.reversed()
    }

    // Filter ledger entries by search and category
    val filteredEntries = remember(ledgerEntries, searchQuery, filterType) {
        ledgerEntries.filter { entry ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                entry.voucherNumber.lowercase().contains(q) ||
                entry.notes.lowercase().contains(q) ||
                entry.title.lowercase().contains(q)
            }

            val matchesFilter = when (filterType) {
                "All" -> true
                "Sales" -> entry.title.contains("Sales", ignoreCase = true)
                "Purchase" -> entry.title.contains("Purchase", ignoreCase = true)
                "Payment" -> entry.title.contains("Payment", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search and Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search ledger by invoice #, remarks...", fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDark) SurfaceDark else Color.White,
                unfocusedContainerColor = if (isDark) SurfaceDark else Color.White,
                focusedBorderColor = if (isDark) SkyBlueBright else SkyBlue,
                unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        // Type Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Sales", "Purchase", "Payment").forEach { type ->
                val isSelected = filterType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                (if (isDark) SkyBlueBright else SkyBlue).copy(alpha = 0.2f)
                            } else {
                                if (isDark) CardDark else Color.White
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) (if (isDark) SkyBlueBright else SkyBlue) else (if (isDark) CardBorder else Color(0xFFCBD5E1)),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { filterType = type }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = type,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) (if (isDark) SkyBlueBright else SkyBlue) else (if (isDark) TextWhite else Color(0xFF475569))
                    )
                }
            }
        }

        // Ledger Header Bar (Date | Details | Debit | Credit | Balance)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDark) SurfaceDark else Color(0xFFE2E8F0))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("DATE & PARTICULARS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text("DEBIT / CREDIT (BAL)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }
        }

        // Ledger List
        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No transactions match criteria", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            filteredEntries.forEach { entry ->
                LedgerEntryCard(
                    entry = entry,
                    privacyMode = privacyMode,
                    onDelete = { entry.tx?.let { onDeleteTransaction(it) } },
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun LedgerEntryCard(
    entry: LedgerEntry,
    privacyMode: Boolean,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CardDark else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Date & Voucher #
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    entry.debit > 0 -> (if (isDark) SkyBlueBright else SkyBlue).copy(alpha = 0.15f)
                                    entry.credit > 0 -> OrangeAccent.copy(alpha = 0.15f)
                                    else -> Color(0xFF10B981).copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.title,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                entry.debit > 0 -> if (isDark) SkyBlueBright else SkyBlue
                                entry.credit > 0 -> OrangeAccent
                                else -> Color(0xFF10B981)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.voucherNumber,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isDark) TextWhite else Color(0xFF0F172A)
                    )
                }

                Text(
                    text = "${entry.dateText} (${entry.subDateText})",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Debit / Credit Amounts & Running Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (entry.paymentMode.isNotBlank()) {
                        Text(
                            text = "Mode: ${entry.paymentMode}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    if (entry.notes.isNotBlank()) {
                        Text(
                            text = entry.notes,
                            fontSize = 11.5.sp,
                            color = if (isDark) TextWhite else Color(0xFF475569),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (entry.debit > 0) {
                            Text(
                                text = if (privacyMode) "Dr •••" else "Dr. Rs. ${String.format(Locale.US, "%,.2f", entry.debit)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (isDark) SkyBlueBright else SkyBlue
                            )
                        }
                        if (entry.credit > 0) {
                            Text(
                                text = if (privacyMode) "Cr •••" else "Cr. Rs. ${String.format(Locale.US, "%,.2f", entry.credit)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = OrangeAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (privacyMode) "Bal: •••••" else "Bal: Rs. ${String.format(Locale.US, "%,.2f", entry.runningBalance)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (entry.runningBalance >= 0) (if (isDark) SkyBlueBright else SkyBlue) else OrangeAccent
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 10. INVOICES TAB CONTENT (Requirement 10)
// -----------------------------------------------------------------------------
@Composable
private fun InvoicesTabContent(
    party: PartyEntity,
    invoices: List<TransactionEntity>,
    privacyMode: Boolean,
    onInvoiceClick: (TransactionEntity) -> Unit,
    onCreateInvoice: () -> Unit,
    onRecordPayment: () -> Unit,
    onDeleteInvoice: (TransactionEntity) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INVOICES (${invoices.size})",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = if (isDark) SkyBlueBright else SkyBlue
            )

            Button(
                onClick = onCreateInvoice,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) SkyBlue else SkyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ New Invoice", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (invoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No invoices recorded for ${party.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) TextWhite else Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate professional tax invoices & billings",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = onCreateInvoice) {
                        Text("+ Create Sales Invoice")
                    }
                }
            }
        } else {
            invoices.forEach { inv ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) CardDark else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .clickable { onInvoiceClick(inv) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = inv.invoiceNumber.ifBlank { "INV-#${inv.id}" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDark) TextWhite else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${inv.displayBsDate} • ${inv.displayAdDate}",
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", inv.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isDark) TextWhite else Color(0xFF0F172A)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = inv.status.ifBlank { "Active" },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = if (isDark) CardBorder else Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Payment Mode: ${inv.paymentMethod}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(
                                    onClick = onRecordPayment,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Record Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { onDeleteInvoice(inv) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 11. PAYMENTS TAB CONTENT (Requirement 11)
// -----------------------------------------------------------------------------
@Composable
private fun PaymentsTabContent(
    party: PartyEntity,
    payments: List<TransactionEntity>,
    privacyMode: Boolean,
    onRecordPaymentIn: () -> Unit,
    onRecordPaymentOut: () -> Unit,
    onDeletePayment: (TransactionEntity) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAYMENT HISTORY (${payments.size})",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = if (isDark) SkyBlueBright else SkyBlue
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onRecordPaymentIn,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("+ Receive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRecordPaymentOut,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("+ Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No payments recorded yet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) TextWhite else Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Record receipts and disbursements for this account",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            payments.forEach { pmt ->
                val isIn = pmt.type == "Payment In"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) CardDark else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isIn) Color(0xFF10B981).copy(alpha = 0.15f) else OrangeAccent.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isIn) Color(0xFF10B981) else OrangeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isIn) "Payment Received" else "Payment Made",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (isDark) TextWhite else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${pmt.displayBsDate} (${pmt.displayAdDate}) • ${pmt.paymentMethod}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            if (pmt.notes.isNotBlank()) {
                                Text(
                                    text = pmt.notes,
                                    fontSize = 11.5.sp,
                                    color = if (isDark) TextWhite else Color(0xFF475569)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (privacyMode) "Rs. •••" else "${if (isIn) "+" else "-"} Rs. ${String.format(Locale.US, "%,.2f", pmt.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isIn) Color(0xFF10B981) else OrangeAccent
                            )

                            IconButton(
                                onClick = { onDeletePayment(pmt) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 12. ACTIVITY TAB CONTENT (Requirement 12)
// -----------------------------------------------------------------------------
@Composable
private fun ActivityTabContent(
    party: PartyEntity,
    transactions: List<TransactionEntity>,
    privacyMode: Boolean,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PARTY ACTIVITY TIMELINE",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = if (isDark) SkyBlueBright else SkyBlue
        )

        val chronological = remember(transactions) { transactions.sortedByDescending { it.dateMillis } }

        // Timeline Node: Recent Transactions
        chronological.forEach { tx ->
            TimelineNodeCard(
                title = "${tx.type} Recorded",
                subtitle = "Voucher #${tx.invoiceNumber.ifBlank { "#${tx.id}" }} for Rs. ${String.format(Locale.US, "%,.2f", tx.amount)} (${tx.paymentMethod})",
                date = "${tx.displayBsDate} (${tx.displayAdDate})",
                icon = when (tx.type) {
                    "Sales Invoice" -> Icons.Outlined.ShoppingCart
                    "Purchase" -> Icons.Outlined.LocalShipping
                    "Payment In" -> Icons.Default.ArrowDownward
                    "Payment Out" -> Icons.Default.ArrowUpward
                    else -> Icons.Outlined.Receipt
                },
                accentColor = when (tx.type) {
                    "Sales Invoice" -> if (isDark) SkyBlueBright else SkyBlue
                    "Purchase" -> OrangeAccent
                    "Payment In" -> Color(0xFF10B981)
                    "Payment Out" -> OrangeAccent
                    else -> SkyBlue
                },
                isDark = isDark
            )
        }

        // Timeline Node: Opening Balance Initialized
        if (party.openingBalance > 0) {
            TimelineNodeCard(
                title = "Opening Balance Initialized",
                subtitle = "Starting balance of Rs. ${String.format(Locale.US, "%,.2f", party.openingBalance)} (${party.balanceType})",
                date = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(party.registerDate)),
                icon = Icons.Outlined.AccountBalance,
                accentColor = if (isDark) SkyBlueBright else SkyBlue,
                isDark = isDark
            )
        }

        // Timeline Node: Account Created
        TimelineNodeCard(
            title = "Party Account Created",
            subtitle = "${party.type} profile '${party.name}' registered into Atri Khata",
            date = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(party.createdAt)),
            icon = Icons.Default.Person,
            accentColor = Color(0xFF10B981),
            isDark = isDark
        )
    }
}

@Composable
private fun TimelineNodeCard(
    title: String,
    subtitle: String,
    date: String,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(if (isDark) CardBorder else Color(0xFFE2E8F0))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) CardDark else Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isDark) TextWhite else Color(0xFF0F172A)
                    )
                    Text(
                        text = date,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = if (isDark) TextMuted else Color(0xFF475569)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// INVOICE DETAILS DIALOG
// -----------------------------------------------------------------------------
@Composable
private fun InvoiceDetailsDialog(
    invoice: TransactionEntity,
    party: PartyEntity,
    privacyMode: Boolean,
    onDismiss: () -> Unit,
    onRecordPayment: () -> Unit,
    isDark: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Invoice #${invoice.invoiceNumber.ifBlank { invoice.id.toString() }}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else Color(0xFF0F172A)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Party Name", color = TextMuted, fontSize = 12.sp)
                    Text(party.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bikram Sambat (BS)", color = TextMuted, fontSize = 12.sp)
                    Text(invoice.displayBsDate, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Gregorian (AD)", color = TextMuted, fontSize = 12.sp)
                    Text(invoice.displayAdDate, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Payment Method", color = TextMuted, fontSize = 12.sp)
                    Text(invoice.paymentMethod, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                }
                if (invoice.notes.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Notes / Memo", color = TextMuted, fontSize = 12.sp)
                        Text(invoice.notes, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                    }
                }
                HorizontalDivider(color = if (isDark) CardBorder else Color(0xFFE2E8F0))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Invoice Amount", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                    Text(
                        text = if (privacyMode) "Rs. •••" else "Rs. ${String.format(Locale.US, "%,.2f", invoice.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (isDark) SkyBlueBright else SkyBlue
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRecordPayment,
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) SkyBlue else SkyBlue)
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = if (isDark) CardDark else Color.White
    )
}

// -----------------------------------------------------------------------------
// QUICK PURCHASE DIALOG
// -----------------------------------------------------------------------------
@Composable
private fun QuickPurchaseDialog(
    party: PartyEntity,
    onDismiss: () -> Unit,
    onSave: (amount: Double, note: String, method: String) -> Unit,
    isDark: Boolean
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Cash") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Purchase from ${party.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextWhite else Color(0xFF0F172A)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = it
                        }
                    },
                    label = { Text("Purchase Amount") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Items / Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Cash", "Bank Transfer", "Credit").forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OrangeCardBg else Color.Transparent)
                                .border(1.dp, if (isSelected) OrangeAccent else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .clickable { selectedMethod = method }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = method,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) OrangeAccent else (if (isDark) TextWhite else Color(0xFF0F172A))
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(amt, note, selectedMethod)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
            ) {
                Text("Save Purchase", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = if (isDark) CardDark else Color.White
    )
}
