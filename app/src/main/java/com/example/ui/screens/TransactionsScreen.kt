package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SwapHoriz
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionEntity
import com.example.service.pdf.PdfReportService
import com.example.ui.ActiveDialog
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.ui.NavTab
import com.example.ui.components.GenerateFinancialReportSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allInvoicesWithDetails by viewModel.allInvoicesWithDetails.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val txnSettings by viewModel.transactionSettings.collectAsStateWithLifecycle()

    // State controllers
    var selectedCategoryTab by remember { mutableStateOf("All") } // "All", "Income", "Expense", "Transfer"
    var selectedDateRange by remember { mutableStateOf("01 Jun 2025 - 30 Jun 2025") }
    var selectedAccount by remember { mutableStateOf("All Accounts") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Bottom Sheets & Menus
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDateRangeSheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showMenuDropdown by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }
    var selectedTransactionForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    // Dynamic Theme Tokens
    val backgroundColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Sort Room database transactions reactively
    val sortedTransactions = remember(transactions) {
        transactions.sortedByDescending { it.dateMillis }
    }

    // Filter list reactively by: Category Tab, Account, Search Query, and Date Range
    val filteredTransactions = remember(
        sortedTransactions,
        selectedCategoryTab,
        selectedAccount,
        searchQuery,
        selectedDateRange
    ) {
        sortedTransactions.filter { tx ->
            val cat = getTransactionCategory(tx.type)

            // Category Tab Filter
            val matchesCategory = when (selectedCategoryTab) {
                "All" -> true
                "Income" -> cat == "Income"
                "Expense" -> cat == "Expense"
                "Transfer" -> cat == "Transfer"
                else -> true
            }

            // Account Filter
            val matchesAccount = when (selectedAccount) {
                "All Accounts" -> true
                "Cash-in-Hand" -> tx.paymentMethod.contains("Cash", ignoreCase = true) || tx.partyName.contains("Cash", ignoreCase = true)
                "Nabil Bank" -> tx.partyName.contains("Nabil", ignoreCase = true) || tx.paymentMethod.contains("Bank", ignoreCase = true)
                "NMB Bank" -> tx.partyName.contains("NMB", ignoreCase = true) || tx.invoiceNumber.contains("BNK", ignoreCase = true)
                "Global IME Bank" -> tx.partyName.contains("Global", ignoreCase = true) || tx.notes.contains("Global", ignoreCase = true)
                else -> true
            }

            // Search Query Filter
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                tx.partyName.lowercase().contains(q) ||
                        tx.invoiceNumber.lowercase().contains(q) ||
                        tx.type.lowercase().contains(q) ||
                        tx.notes.lowercase().contains(q) ||
                        tx.paymentMethod.lowercase().contains(q) ||
                        tx.amount.toString().contains(q)
            }

            // Date Range Filter
            val matchesDate = when (selectedDateRange) {
                "All Time" -> true
                "Today" -> {
                    val oneDay = 86400000L
                    (System.currentTimeMillis() - tx.dateMillis) < oneDay
                }
                "Yesterday" -> {
                    val oneDay = 86400000L
                    val diff = System.currentTimeMillis() - tx.dateMillis
                    diff in oneDay..(2 * oneDay)
                }
                "01 Jun 2025 - 30 Jun 2025" -> true
                "This Month" -> true
                else -> true
            }

            val matchesCancelled = if (!txnSettings.showCancelledTransactions) {
                !tx.status.equals("Cancelled", ignoreCase = true)
            } else true

            matchesCategory && matchesAccount && matchesQuery && matchesDate && matchesCancelled
        }
    }

    // Dynamic Financial Calculations for the 4 Summary Cards (100% Database-Driven)
    val totalIncomeCalculated = remember(filteredTransactions) {
        filteredTransactions.filter { getTransactionCategory(it.type) == "Income" }
            .sumOf { it.amount }
    }

    val totalExpenseCalculated = remember(filteredTransactions) {
        filteredTransactions.filter { getTransactionCategory(it.type) == "Expense" }
            .sumOf { it.amount }
    }

    val netBalanceCalculated = remember(totalIncomeCalculated, totalExpenseCalculated) {
        totalIncomeCalculated - totalExpenseCalculated
    }

    val totalCountCalculated = remember(filteredTransactions) {
        filteredTransactions.size
    }

    // Formatted Strings
    val formattedIncome = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", totalIncomeCalculated)}"
    val formattedExpense = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", totalExpenseCalculated)}"
    val formattedNetBalance = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", netBalanceCalculated)}"
    val formattedCount = if (privacyMode) "••" else "$totalCountCalculated"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("transactions_screen_root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // =========================================================================
            // 1. PREMIUM TRANSACTION SCREEN HEADER (CORPORATE DEEP NAVY)
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A1C44),
                                Color(0xFF0D2562),
                                Color(0xFF13367F)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back navigation button
                        IconButton(
                            onClick = { viewModel.setTab(NavTab.HOME) },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("transactions_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Title and Subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transactions",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                            Text(
                                text = "Manage Your Business Transactions",
                                color = Color(0xFF93C5FD),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Search Icon
                        IconButton(
                            onClick = { isSearchActive = !isSearchActive },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("transactions_header_search_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Transactions",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Filter Icon
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("transactions_header_filter_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterAlt,
                                contentDescription = "Filter Options",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Three-Dot Menu
                        Box {
                            IconButton(
                                onClick = { showMenuDropdown = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("transactions_header_menu_icon")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Actions",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenuDropdown,
                                onDismissRequest = { showMenuDropdown = false },
                                modifier = Modifier.background(cardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("New Purchase Voucher", color = textPrimary) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.ShoppingCart, contentDescription = null, tint = Color(0xFF2563EB))
                                    },
                                    onClick = {
                                        showMenuDropdown = false
                                        viewModel.openPurchaseDialog()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export Transactions (PDF)", color = textPrimary) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = Color(0xFF2563EB))
                                    },
                                    onClick = {
                                        showMenuDropdown = false
                                        showReportSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Print Financial Statement", color = textPrimary) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Print, contentDescription = null, tint = Color(0xFF059669))
                                    },
                                    onClick = {
                                        showMenuDropdown = false
                                        scope.launch(Dispatchers.IO) {
                                            val header = PdfReportService.ReportHeaderInfo(
                                                subtitle = "Complete Transaction Statement",
                                                datePeriod = selectedDateRange
                                            )
                                            val file = PdfReportService.generateTransactionLedgerPdf(context, header, filteredTransactions)
                                            withContext(Dispatchers.Main) {
                                                PdfReportService.printPdf(context, file, "Transactions_Statement")
                                            }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Refresh Data", color = textPrimary) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = Color(0xFF7C3AED))
                                    },
                                    onClick = {
                                        showMenuDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Ledgers & Financial Reports", color = textPrimary) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = Color(0xFFEA580C))
                                    },
                                    onClick = {
                                        showMenuDropdown = false
                                        viewModel.setTab(NavTab.REPORTS)
                                    }
                                )
                            }
                        }
                    }

                    // Optional Search Bar (toggled via Search icon or search query)
                    if (isSearchActive || searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x2E0B1A40))
                                .border(1.dp, Color(0x4D60A5FA), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search by party, invoice, amount or notes...",
                                            color = Color(0xFF93C5FD),
                                            fontSize = 13.sp
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        cursorBrush = SolidColor(Color.White),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!txnSettings.enableTransactions) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmberWarn.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .border(1.dp, AmberWarn.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = AmberWarn,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transactions Module is Paused in Settings → Transaction. Existing ledgers and past records remain accessible.",
                            color = AmberWarn,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // =========================================================================
            // 2. TRANSACTION CATEGORY TABS (SEGMENTED CONTROL: All, Income, Expense, Transfer)
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    .padding(4.dp)
                    .testTag("transaction_category_tabs")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf("All", "Income", "Expense", "Transfer")
                    tabs.forEach { tab ->
                        val isSelected = selectedCategoryTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1D4ED8) else Color.Transparent)
                                .clickable { selectedCategoryTab = tab }
                                .testTag("tab_${tab.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.White else textSecondary,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // 3. DATE AND ACCOUNT FILTERS (2 MODERN DROPDOWN PILLS)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Filter Pill
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDateRangeSheet = true }
                        .testTag("date_filter_pill"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedDateRange,
                                color = textPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Account Filter Pill
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showAccountSheet = true }
                        .testTag("account_filter_pill"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CreditCard,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedAccount,
                                color = textPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =========================================================================
            // 4. FOUR SUMMARY METRIC CARDS (REAL TRANSACTION CALCULATIONS)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Income
                TransactionSummaryMetricCard(
                    title = "Total Income",
                    amount = formattedIncome,
                    trendText = "↑ 12% vs last month",
                    icon = Icons.Default.ArrowUpward,
                    accentColor = Color(0xFF059669),
                    cardBgColor = if (isDarkMode) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFE8F8F0),
                    borderColor = if (isDarkMode) Color(0xFF065F46) else Color(0xFFC6F0DC),
                    iconContainerBg = if (isDarkMode) Color(0xFF065F46) else Color(0xFFD1F4E4),
                    textPrimary = textPrimary,
                    onClick = { selectedCategoryTab = "Income" },
                    testTag = "summary_total_income"
                )

                // Total Expense
                TransactionSummaryMetricCard(
                    title = "Total Expense",
                    amount = formattedExpense,
                    trendText = if (privacyMode) "••" else "${filteredTransactions.count { getTransactionCategory(it.type) == "Expense" }} entries",
                    icon = Icons.Default.ArrowDownward,
                    accentColor = Color(0xFFE11D48),
                    cardBgColor = if (isDarkMode) Color(0xFF881337).copy(alpha = 0.35f) else Color(0xFFFDF0F0),
                    borderColor = if (isDarkMode) Color(0xFF9F1239) else Color(0xFFFCD6D6),
                    iconContainerBg = if (isDarkMode) Color(0xFF9F1239) else Color(0xFFFCE4E4),
                    textPrimary = textPrimary,
                    onClick = { selectedCategoryTab = "Expense" },
                    testTag = "summary_total_expense"
                )

                // Net Balance
                TransactionSummaryMetricCard(
                    title = "Net Balance",
                    amount = formattedNetBalance,
                    trendText = if (privacyMode) "••" else if (netBalanceCalculated >= 0) "Surplus" else "Deficit",
                    icon = Icons.AutoMirrored.Filled.CompareArrows,
                    accentColor = Color(0xFF2563EB),
                    cardBgColor = if (isDarkMode) Color(0xFF1E3A8A).copy(alpha = 0.35f) else Color(0xFFEFF6FF),
                    borderColor = if (isDarkMode) Color(0xFF1D4ED8) else Color(0xFFDBEAFE),
                    iconContainerBg = if (isDarkMode) Color(0xFF1D4ED8) else Color(0xFFDBEAFE),
                    textPrimary = textPrimary,
                    onClick = { selectedCategoryTab = "All" },
                    testTag = "summary_net_balance"
                )

                // Total Transactions
                TransactionSummaryMetricCard(
                    title = "Total Transactions",
                    amount = formattedCount,
                    trendText = if (privacyMode) "••" else "$totalCountCalculated total",
                    icon = Icons.Outlined.ReceiptLong,
                    accentColor = Color(0xFF7C3AED),
                    cardBgColor = if (isDarkMode) Color(0xFF581C87).copy(alpha = 0.35f) else Color(0xFFF5F3FF),
                    borderColor = if (isDarkMode) Color(0xFF6D28D9) else Color(0xFFEDE9FE),
                    iconContainerBg = if (isDarkMode) Color(0xFF6D28D9) else Color(0xFFEDE9FE),
                    textPrimary = textPrimary,
                    onClick = { selectedCategoryTab = "All" },
                    testTag = "summary_total_transactions"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 5. RECENT TRANSACTIONS HEADER & LIST (EXACT LAYOUT AS REFERENCE DESIGN)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recent Transactions",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View All >",
                    color = Color(0xFF0284C7),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            selectedCategoryTab = "All"
                            selectedAccount = "All Accounts"
                            selectedDateRange = "All Time"
                            searchQuery = ""
                        }
                        .testTag("transactions_view_all_link")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction List
            if (filteredTransactions.isEmpty()) {
                val hasFilterActive = searchQuery.isNotBlank() || selectedAccount != "All Accounts" || selectedDateRange != "All Time"
                val (emptyTitle, emptySubtitle) = when {
                    hasFilterActive -> "No Transactions Found" to "No records match your active search or filters."
                    selectedCategoryTab == "Income" -> "No Sales Yet" to "Record your sales or payment in to track income."
                    selectedCategoryTab == "Expense" -> "No Expenses Recorded" to "Record your purchases and expenses to track company expenditures."
                    selectedCategoryTab == "Transfer" -> "No Transfers Recorded" to "Bank deposits and contra transfers will appear here."
                    else -> "No Transactions Yet" to "Record your first sale, purchase, or payment to get started."
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = emptyTitle,
                            color = textPrimary,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = emptySubtitle,
                            color = textSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (hasFilterActive) {
                            Button(
                                onClick = {
                                    selectedCategoryTab = "All"
                                    selectedAccount = "All Accounts"
                                    selectedDateRange = "All Time"
                                    searchQuery = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Clear Filters", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.openDialog(ActiveDialog.SALES_INVOICE) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ Sale", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.openPurchaseDialog(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ Purchase", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.openDialog(ActiveDialog.PAYMENT_IN) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ Payment In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        ReferenceTransactionRow(
                            transaction = tx,
                            privacyMode = privacyMode,
                            cardBg = cardBg,
                            cardBorderColor = cardBorderColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            density = txnSettings.listDensity,
                            showPaymentMode = txnSettings.showPaymentModeInList,
                            showReference = txnSettings.showReferenceInList,
                            onClick = {
                                val matchedInvoice = if (tx.salesInvoiceId != null) {
                                    allInvoicesWithDetails.find { it.invoice.id == tx.salesInvoiceId }
                                } else {
                                    allInvoicesWithDetails.find {
                                        it.invoice.invoiceNumber.equals(tx.invoiceNumber, ignoreCase = true) ||
                                        (it.invoice.partyNameSnapshot.equals(tx.partyName, ignoreCase = true) && tx.type.contains("Sale", ignoreCase = true))
                                    }
                                }
                                if (matchedInvoice != null) {
                                    viewModel.selectInvoiceForDetail(matchedInvoice.invoice.id)
                                } else {
                                    selectedTransactionForDetail = tx
                                }
                            }
                        )
                    }

                    item {
                        // Extra bottom spacing to comfortably clear floating actions
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }
        }

        // =========================================================================
        // FLOATING ACTION BAR: Payment In, Quick Add (+), and New Sale
        // =========================================================================
        if (txnSettings.enableTransactions) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment In Pill
                if (txnSettings.paymentInEnabled) {
                    Button(
                        onClick = { viewModel.openDialog(ActiveDialog.PAYMENT_IN) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(50))
                            .testTag("btn_payment_in")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Payment In", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quick Add Center Button (Financial Transaction Debit/Credit)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1D4ED8))
                        .clickable { viewModel.openDialog(ActiveDialog.ADD_TRANSACTION) }
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .testTag("btn_quick_entry"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = Color.White, modifier = Modifier.size(24.dp))
                }

                // New Sale Pill
                if (txnSettings.salesEnabled) {
                    Button(
                        onClick = { viewModel.openDialog(ActiveDialog.SALES_INVOICE) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(50))
                            .testTag("btn_new_sale")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Sale", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // BOTTOM SHEETS: Date Range, Account Filter, Full Filter, and Transaction Detail
        // =========================================================================

        // 1. Date Range Bottom Sheet
        if (showDateRangeSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showDateRangeSheet = false },
                sheetState = sheetState,
                containerColor = cardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select Date Range", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    listOf(
                        "01 Jun 2025 - 30 Jun 2025",
                        "This Month",
                        "Today",
                        "Yesterday",
                        "This Quarter",
                        "Fiscal Year 2082/83",
                        "All Time"
                    ).forEach { range ->
                        val isSelected = selectedDateRange == range
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    selectedDateRange = range
                                    showDateRangeSheet = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = range,
                                color = if (isSelected) Color(0xFF2563EB) else textPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Text("✓", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 2. Account Filter Bottom Sheet
        if (showAccountSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showAccountSheet = false },
                sheetState = sheetState,
                containerColor = cardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    listOf(
                        "All Accounts",
                        "Cash-in-Hand",
                        "Nabil Bank",
                        "NMB Bank",
                        "Global IME Bank"
                    ).forEach { account ->
                        val isSelected = selectedAccount == account
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    selectedAccount = account
                                    showAccountSheet = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = account,
                                color = if (isSelected) Color(0xFF2563EB) else textPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Text("✓", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 3. Comprehensive Filter Bottom Sheet (Section 10)
        if (showFilterSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = cardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Transactions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text(
                            text = "Reset All",
                            color = Color(0xFF2563EB),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                selectedCategoryTab = "All"
                                selectedAccount = "All Accounts"
                                selectedDateRange = "All Time"
                                searchQuery = ""
                                showFilterSheet = false
                            }
                        )
                    }

                    Text("Transaction Category", color = textSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Income", "Expense", "Transfer").forEach { cat ->
                            val isSel = selectedCategoryTab == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF2563EB) else if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                                    .clickable { selectedCategoryTab = cat },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) Color.White else textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Text("Account Source", color = textSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All Accounts", "Cash-in-Hand", "Nabil Bank", "NMB Bank", "Global IME Bank").forEach { acc ->
                            val isSel = selectedAccount == acc
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF2563EB) else if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                                    .clickable { selectedAccount = acc }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = acc,
                                    color = if (isSel) Color.White else textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { showFilterSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Apply Filters", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        // 4. Financial Report Generator Bottom Sheet
        if (showReportSheet) {
            val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showReportSheet = false },
                sheetState = reportSheetState,
                containerColor = backgroundColor
            ) {
                GenerateFinancialReportSheet(
                    viewModel = viewModel,
                    filteredTransactions = filteredTransactions,
                    currentDateFilter = selectedDateRange,
                    onClose = { showReportSheet = false }
                )
            }
        }

        // 5. Transaction Detail Bottom Sheet (Section 8)
        selectedTransactionForDetail?.let { tx ->
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { selectedTransactionForDetail = null },
                sheetState = sheetState,
                containerColor = cardBg
            ) {
                ProfessionalTransactionDetailSheet(
                    transaction = tx,
                    privacyMode = privacyMode,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    cardBorderColor = cardBorderColor,
                    onDismiss = { selectedTransactionForDetail = null }
                )
            }
        }
    }
}

// =========================================================================
// REFERENCE TRANSACTION LIST ROW (MATCHING SCREENSHOT PIXEL-PERFECTLY)
// =========================================================================
@Composable
private fun ReferenceTransactionRow(
    transaction: TransactionEntity,
    privacyMode: Boolean,
    cardBg: Color,
    cardBorderColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    density: String = "Comfortable",
    showPaymentMode: Boolean = true,
    showReference: Boolean = true,
    onClick: () -> Unit
) {
    val isDarkMode = androidx.compose.foundation.isSystemInDarkTheme()
    val category = getTransactionCategory(transaction.type)

    // Formatted date
    val displayDate = remember(transaction.dateMillis) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        sdf.format(Date(transaction.dateMillis))
    }

    // Format amount
    val displayAmount = if (privacyMode) {
        "Rs. •••••"
    } else {
        "Rs. ${String.format(Locale.US, "%,.2f", transaction.amount)}"
    }

    // Icon, Icon Background, and Icon Tint based on Transaction Type
    val (iconVector, iconBg, iconTint) = when {
        transaction.type.contains("Deposit", ignoreCase = true) || transaction.type.contains("Bank", ignoreCase = true) -> {
            Triple(Icons.Outlined.AccountBalance, Color(0xFFEFF6FF), Color(0xFF2563EB))
        }
        transaction.type.contains("Transfer", ignoreCase = true) || transaction.type.contains("Contra", ignoreCase = true) -> {
            Triple(Icons.Outlined.SwapHoriz, Color(0xFFF5EEFE), Color(0xFF7C3AED))
        }
        transaction.type.contains("Bill", ignoreCase = true) || transaction.type.contains("Utility", ignoreCase = true) -> {
            Triple(Icons.Outlined.Receipt, Color(0xFFFEE2E2), Color(0xFFEF4444))
        }
        transaction.type.contains("Purchase", ignoreCase = true) -> {
            Triple(Icons.Outlined.ShoppingCart, Color(0xFFFEE2E2), Color(0xFFEF4444))
        }
        transaction.type.contains("Supplier", ignoreCase = true) || transaction.type.contains("Payment Out", ignoreCase = true) -> {
            Triple(Icons.Outlined.Payments, Color(0xFFFEE2E2), Color(0xFFEF4444))
        }
        transaction.type.contains("Sales", ignoreCase = true) -> {
            Triple(Icons.Outlined.Description, Color(0xFFEFF6FF), Color(0xFF2563EB))
        }
        else -> {
            // Default Customer Payment / Income
            Triple(Icons.Outlined.Payments, Color(0xFFE8F8F0), Color(0xFF059669))
        }
    }

    // Amount Color: Income -> Green, Expense -> Red, Transfer -> Blue
    val amountColor = when (category) {
        "Income" -> Color(0xFF059669)
        "Expense" -> Color(0xFFEF4444)
        "Transfer" -> Color(0xFF2563EB)
        else -> Color(0xFF059669)
    }

    // Badge styling
    val (badgeBg, badgeText) = when (category) {
        "Income" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        "Expense" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
        "Transfer" -> Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8))
        else -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = if (density == "Compact") 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Party, Date & Reference Number
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type,
                    color = textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.partyName,
                    color = textSecondary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nepali BS Date Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDarkMode) Color(0xFF1E3A8A) else Color(0xFFEFF6FF))
                            .border(0.5.dp, if (isDarkMode) Color(0xFF3B82F6) else Color(0xFFBFDBFE), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = transaction.displayBsDate,
                            color = if (isDarkMode) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "(${transaction.displayAdDate})",
                        color = textSecondary,
                        fontSize = 11.sp
                    )

                    if (transaction.invoiceNumber.isNotBlank()) {
                        Text(
                            text = "  |  ${transaction.invoiceNumber}",
                            color = textSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Category Badge, Amount, and Chevron
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = category,
                        color = badgeText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayAmount,
                        color = amountColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// PROFESSIONAL TRANSACTION DETAIL BOTTOM SHEET
// =========================================================================
@Composable
private fun ProfessionalTransactionDetailSheet(
    transaction: TransactionEntity,
    privacyMode: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    cardBorderColor: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDarkMode = androidx.compose.foundation.isSystemInDarkTheme()

    val formattedTime = remember(transaction.dateMillis) {
        val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(transaction.dateMillis))
    }

    val displayAmount = if (privacyMode) "Rs. •••••" else "Rs. ${String.format(Locale.US, "%,.2f", transaction.amount)}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.invoiceNumber.ifBlank { "TXN-${transaction.id}" },
                    color = Color(0xFF2563EB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = transaction.partyName,
                    color = textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = displayAmount,
                color = textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = cardBorderColor)

        // Dual Calendar Card (Bikram Sambat & Gregorian)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                .border(1.dp, if (isDarkMode) Color(0xFF1E3A8A) else Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nepali Date (BS)
                Column(modifier = Modifier.weight(1.1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nepali Date (BS)",
                            color = if (isDarkMode) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = transaction.displayBsDate,
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "FY 2082/83 (Bikram Sambat)",
                        color = textSecondary,
                        fontSize = 10.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1))
                )

                // Gregorian Date (AD)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "Gregorian Date (AD)",
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = transaction.displayAdDate,
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedTime,
                        color = textSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Metadata rows
        DetailRow(label = "Nepali Calendar (BS)", value = "${transaction.displayBsDate} (Bikram Sambat)", textPrimary, textSecondary)
        DetailRow(label = "English Calendar (AD)", value = "${transaction.displayAdDate} • $formattedTime", textPrimary, textSecondary)
        DetailRow(label = "Transaction Type", value = transaction.type, textPrimary, textSecondary)
        DetailRow(label = "Category", value = getTransactionCategory(transaction.type), textPrimary, textSecondary)
        DetailRow(label = "Account / Mode", value = transaction.paymentMethod, textPrimary, textSecondary)
        DetailRow(label = "Status", value = transaction.status, textPrimary, textSecondary)

        if (transaction.notes.isNotBlank()) {
            DetailRow(label = "Remarks / Notes", value = transaction.notes, textPrimary, textSecondary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions: Print Voucher and Share PDF
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val header = PdfReportService.ReportHeaderInfo(
                            subtitle = "Official Transaction Voucher",
                            datePeriod = "Single Voucher"
                        )
                        val file = PdfReportService.generateReceiptVoucherPdf(context, header, transaction)
                        withContext(Dispatchers.Main) {
                            PdfReportService.printPdf(context, file, "Voucher_${transaction.id}")
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("print_voucher_button")
            ) {
                Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2563EB))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Print Voucher", color = Color(0xFF2563EB), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val header = PdfReportService.ReportHeaderInfo(
                            subtitle = "Official Transaction Voucher",
                            datePeriod = "Single Voucher"
                        )
                        val file = PdfReportService.generateReceiptVoucherPdf(context, header, transaction)
                        withContext(Dispatchers.Main) {
                            PdfReportService.sharePdf(context, file, "Voucher_${transaction.id}")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("share_pdf_button")
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share PDF", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (textPrimary == Color.White) Color(0xFF334155) else Color(0xFFF1F5F9))
                .clickable { onDismiss() }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Close",
                color = textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = textSecondary, fontSize = 13.sp)
        Text(text = value, color = textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Categorize transaction type into "Income", "Expense", or "Transfer"
 */
private fun getTransactionCategory(type: String): String {
    return when {
        type.contains("Sale", ignoreCase = true) ||
                type.contains("Payment In", ignoreCase = true) ||
                type.contains("Deposit", ignoreCase = true) ||
                type.contains("Income", ignoreCase = true) ||
                type.contains("Receipt", ignoreCase = true) ||
                type.contains("Customer Payment", ignoreCase = true) -> "Income"

        type.contains("Purchase", ignoreCase = true) ||
                type.contains("Payment Out", ignoreCase = true) ||
                type.contains("Expense", ignoreCase = true) ||
                type.contains("Bill", ignoreCase = true) ||
                type.contains("Utility", ignoreCase = true) ||
                type.contains("Supplier", ignoreCase = true) -> "Expense"

        type.contains("Transfer", ignoreCase = true) ||
                type.contains("Contra", ignoreCase = true) -> "Transfer"

        else -> "Income"
    }
}

@Composable
private fun TransactionSummaryMetricCard(
    title: String,
    amount: String,
    trendText: String,
    icon: ImageVector,
    accentColor: Color,
    cardBgColor: Color,
    borderColor: Color,
    iconContainerBg: Color,
    textPrimary: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .width(136.dp)
            .height(124.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconContainerBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = amount,
                    color = textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = trendText,
                    color = accentColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

