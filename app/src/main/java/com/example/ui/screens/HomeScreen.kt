package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import com.example.ui.ActiveDialog
import com.example.ui.MainViewModel
import com.example.ui.NavTab
import com.example.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedMonthFilter by remember { mutableStateOf("This Month") }
    var showMonthFilterSheet by remember { mutableStateOf(false) }
    var showNepaliCalendarSheet by remember { mutableStateOf(false) }

    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()

    val totalSales by viewModel.totalSales.collectAsStateWithLifecycle()
    val totalPurchases by viewModel.totalPurchases.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle()
    val totalToReceive by viewModel.totalToReceive.collectAsStateWithLifecycle()
    val totalToGive by viewModel.totalToGive.collectAsStateWithLifecycle()
    val bankBalance by viewModel.cashAndBankBalance.collectAsStateWithLifecycle()
    val businessProfile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val unreadNotifications by viewModel.unreadNotifications.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    // Calculated / formatted values purely from Room Database
    val totalBalanceValue = (bankBalance + totalToReceive - totalToGive).coerceAtLeast(0.0)
    val formattedTotalBalance = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", totalBalanceValue)}"
    val formattedIncome = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", totalSales)}"
    val formattedExpense = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", totalExpenses + totalPurchases)}"
    val formattedBank = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", bankBalance)}"
    val formattedOutstanding = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", totalToReceive)}"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .testTag("home_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // =========================================================================
            // 1. TOP HEADER & GREETING BANNER (CORPORATE DEEP NAVY BLUE)
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A1C44),
                                Color(0xFF0E2760),
                                Color(0xFF13367F)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Top Navigation Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hamburger Menu Icon
                        IconButton(
                            onClick = { viewModel.openDialog(ActiveDialog.BUSINESS_SWITCHER) },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("home_hamburger_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Stylish 3D Geometric Book / Ledger Logo
                        AtriKhataLogo(modifier = Modifier.size(34.dp))

                        Spacer(modifier = Modifier.width(10.dp))

                        // App Name and Tagline
                        Column {
                            Text(
                                text = "Atri Khata",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                            Text(
                                text = "Accounting & Finance",
                                color = Color(0xFF93C5FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Notification Bell with unread badge (3)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.openDialog(ActiveDialog.NOTIFICATIONS) }
                                .testTag("home_notifications_bell"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(23.dp)
                            )
                            // Unread Notification Badge (dynamic)
                            if (unreadNotifications > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 4.dp, end = 4.dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unreadNotifications",
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Profile Avatar & Cloud Sync Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                                .border(1.5.dp, if (userSession?.isLoggedIn == true) Color(0xFF38BDF8) else Color.White, CircleShape)
                                .clickable { viewModel.openDialog(ActiveDialog.USER_ACCOUNT_SYNC) }
                                .testTag("home_profile_avatar"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userSession?.isLoggedIn == true) {
                                Text(
                                    text = (userSession?.displayName?.takeIf { it.isNotBlank() } ?: userSession?.email ?: "U").take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Sync status dot indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isSyncing) Color(0xFFFBBF24) else if (userSession?.isLoggedIn == true) Color(0xFF10B981) else Color(0xFF94A3B8))
                                    .border(1.dp, Color(0xFF0F172A), CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Greeting & Business Banner with Nepali Date Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Greeting and Business
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Namaste",
                                color = Color.White,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = businessProfile?.businessName ?: "My Business",
                                color = Color(0xFF93C5FD),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Nepali Date Selector Widget: "2082/06/21 (Baisakh)"
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x331E3A8A))
                                .border(1.dp, Color(0x4D93C5FD), RoundedCornerShape(12.dp))
                                .clickable { showNepaliCalendarSheet = true }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                .testTag("nepali_date_widget"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Nepali Calendar",
                                tint = Color(0xFF93C5FD),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "2082/06/21",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "(Baisakh)",
                                    color = Color(0xFF93C5FD),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF93C5FD),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =========================================================================
            // 2. HERO ANALYTICS CARD (TOTAL BALANCE & SPARKLINE)
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x331E40AF),
                        spotColor = Color(0x331E40AF)
                    )
                    .testTag("hero_total_balance_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E4AB8),
                                    Color(0xFF1B42A4),
                                    Color(0xFF123180)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Top row of hero card: Wallet Icon + Total Balance + Dropdown Filter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Circular wallet icon container
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x3360A5FA))
                                        .border(1.dp, Color(0x4493C5FD), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Total Balance",
                                        color = Color(0xFFBFDBFE),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formattedTotalBalance,
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }

                            // "This Month" filter dropdown pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2E0B1A40))
                                    .border(1.dp, Color(0x4D60A5FA), RoundedCornerShape(12.dp))
                                    .clickable { showMonthFilterSheet = true }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                                    .testTag("hero_month_filter_button"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedMonthFilter,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom row of hero card: Growth Badge & Visual Trend Line Chart / Sparkline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Trend indicator Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (allTransactions.isEmpty()) "•" else "↑",
                                    color = Color(0xFF22C55E),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (allTransactions.isEmpty()) "Clean Ledger" else "${allTransactions.size} transactions",
                                    color = Color(0xFF93C5FD),
                                    fontSize = 11.5.sp
                                )
                            }

                            // Visual Trend Line Chart (Sparkline) matching the screenshot
                            VisualTrendSparkline(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(46.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 3. FOUR SUMMARY METRIC CARDS (HORIZONTAL ROW)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Total Income
                SummaryMetricCard(
                    title = "Total Income",
                    amount = formattedIncome,
                    trendText = if (privacyMode) "••" else if (totalSales > 0) "Sales" else "No Sales",
                    isPositiveTrend = true,
                    icon = Icons.Outlined.Payments,
                    accentColor = Color(0xFF059669),
                    cardBgColor = Color(0xFFE8F8F0),
                    borderColor = Color(0xFFC6F0DC),
                    iconContainerBg = Color(0xFFD1F4E4),
                    onClick = { viewModel.setTab(NavTab.TRANSACTIONS) },
                    testTag = "metric_total_income"
                )

                // 2. Total Expense
                SummaryMetricCard(
                    title = "Total Expense",
                    amount = formattedExpense,
                    trendText = if (privacyMode) "••" else if (totalExpenses + totalPurchases > 0) "Expenses" else "No Expenses",
                    isPositiveTrend = false,
                    icon = Icons.Outlined.AccountBalanceWallet,
                    accentColor = Color(0xFFE11D48),
                    cardBgColor = Color(0xFFFDF0F0),
                    borderColor = Color(0xFFFCD6D6),
                    iconContainerBg = Color(0xFFFCE4E4),
                    onClick = { viewModel.openDialog(ActiveDialog.QUICK_ENTRY) },
                    testTag = "metric_total_expense"
                )

                // 3. Bank Balance
                SummaryMetricCard(
                    title = "Bank Balance",
                    amount = formattedBank,
                    trendText = if (privacyMode) "••" else if (bankBalance > 0) "Liquid" else "Zero",
                    isPositiveTrend = true,
                    icon = Icons.Outlined.AccountBalance,
                    accentColor = Color(0xFF2563EB),
                    cardBgColor = Color(0xFFEFF6FF),
                    borderColor = Color(0xFFDBEAFE),
                    iconContainerBg = Color(0xFFDBEAFE),
                    onClick = { viewModel.openDialog(ActiveDialog.ACCOUNT_TRANSFER) },
                    testTag = "metric_bank_balance"
                )

                // 4. Outstanding
                SummaryMetricCard(
                    title = "Outstanding",
                    amount = formattedOutstanding,
                    trendText = if (privacyMode) "••" else if (totalToReceive > 0) "Receivable" else "Settled",
                    isPositiveTrend = false,
                    icon = Icons.Outlined.ReceiptLong,
                    accentColor = Color(0xFF7C3AED),
                    cardBgColor = Color(0xFFF5F3FF),
                    borderColor = Color(0xFFEDE9FE),
                    iconContainerBg = Color(0xFFEDE9FE),
                    onClick = { viewModel.setTab(NavTab.PARTIES) },
                    testTag = "metric_outstanding"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 4. QUICK ACTIONS GRID (8 ICONS)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Section Title with "View All >"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quick Actions",
                        color = AppTheme.colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "View All >",
                        color = Color(0xFF0284C7),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { viewModel.openDialog(ActiveDialog.EDIT_SHORTCUTS) }
                            .testTag("quick_actions_view_all")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 1 of Quick Actions (4 items)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionItem(
                        icon = Icons.Outlined.ReceiptLong,
                        label = "New Sales",
                        iconColor = Color(0xFF059669),
                        containerBg = Color(0xFFE8F8F0),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.openDialog(ActiveDialog.SALES_INVOICE) },
                        testTag = "action_new_sales"
                    )

                    QuickActionItem(
                        icon = Icons.Outlined.ShoppingCart,
                        label = "Purchase",
                        iconColor = Color(0xFF2563EB),
                        containerBg = Color(0xFFEFF6FF),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.openPurchaseDialog() },
                        testTag = "action_purchase"
                    )

                    QuickActionItem(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        label = "Expense",
                        iconColor = Color(0xFF7C3AED),
                        containerBg = Color(0xFFF5EEFE),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.openDialog(ActiveDialog.QUICK_ENTRY) },
                        testTag = "action_expense"
                    )

                    QuickActionItem(
                        icon = Icons.Outlined.AccountBalance,
                        label = "Bank & Cash",
                        iconColor = Color(0xFFEA580C),
                        containerBg = Color(0xFFFFF7ED),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.openDialog(ActiveDialog.ACCOUNT_TRANSFER) },
                        testTag = "action_bank_cash"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2 of Quick Actions (4 items)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionItem(
                        icon = Icons.Default.Person,
                        label = "Customers",
                        iconColor = Color(0xFF059669),
                        containerBg = Color(0xFFECFDF5),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(NavTab.PARTIES) },
                        testTag = "action_customers"
                    )

                    QuickActionItem(
                        icon = Icons.Outlined.Groups,
                        label = "Suppliers",
                        iconColor = Color(0xFF4F46E5),
                        containerBg = Color(0xFFEEF2FF),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(NavTab.PARTIES) },
                        testTag = "action_suppliers"
                    )

                    QuickActionItem(
                        icon = Icons.Outlined.Leaderboard,
                        label = "Reports",
                        iconColor = Color(0xFFDB2777),
                        containerBg = Color(0xFFFDF2F8),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(NavTab.REPORTS) },
                        testTag = "action_reports"
                    )

                    QuickActionItem(
                        icon = Icons.Outlined.Settings,
                        label = "Settings",
                        iconColor = Color(0xFF475569),
                        containerBg = Color(0xFFF1F5F9),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.openDialog(ActiveDialog.SETTINGS) },
                        testTag = "action_settings"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 5. RECENT TRANSACTIONS SECTION
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = Color(0x1A000000),
                        spotColor = Color(0x1A000000)
                    )
                    .testTag("recent_transactions_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header: Clock Icon + "Recent Transactions" + "View All >"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.primaryCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = AppTheme.colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Recent Transactions",
                                color = AppTheme.colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "View All >",
                            color = AppTheme.colors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { viewModel.setTab(NavTab.TRANSACTIONS) }
                                .testTag("recent_transactions_view_all")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (allTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.inputBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ReceiptLong,
                                        contentDescription = null,
                                        tint = AppTheme.colors.textMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "No Recent Transactions",
                                    color = AppTheme.colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Record your first sale, purchase, or payment",
                                    color = AppTheme.colors.textMuted,
                                    fontSize = 12.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                    } else {
                        val recentList = allTransactions.take(5)
                        recentList.forEachIndexed { index, tx ->
                            val isIncome = tx.type.contains("Sale", true) || tx.type.contains("In", true) || tx.type.contains("Receipt", true)
                            val icon = when {
                                tx.type.contains("Sale", true) -> Icons.Outlined.ReceiptLong
                                tx.type.contains("Purchase", true) -> Icons.Outlined.ShoppingCart
                                tx.type.contains("In", true) || tx.type.contains("Receipt", true) -> Icons.Default.Person
                                tx.type.contains("Out", true) -> Icons.Outlined.AccountBalanceWallet
                                else -> Icons.Outlined.AccountBalanceWallet
                            }
                            val iconBg = if (isIncome) Color(0xFFE8F8F0) else Color(0xFFFEECEC)
                            val iconTint = if (isIncome) Color(0xFF059669) else Color(0xFFE11D48)

                            RecentTransactionItem(
                                icon = icon,
                                title = if (tx.partyName.isNotBlank()) tx.partyName else tx.type,
                                nepaliDate = if (tx.dateBs.isNotBlank()) tx.dateBs else tx.dateAd,
                                category = tx.type,
                                amount = if (privacyMode) "Rs. ••••••••" else "Rs. ${String.format(Locale.US, "%,.2f", tx.amount)}",
                                status = tx.status,
                                isIncome = isIncome,
                                iconBg = iconBg,
                                iconTint = iconTint,
                                onClick = { viewModel.setTab(NavTab.TRANSACTIONS) }
                            )

                            if (index < recentList.size - 1) {
                                HorizontalDivider(color = AppTheme.colors.divider, modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // =========================================================================
        // BOTTOM SHEETS: MONTH FILTER & NEPALI DATE CALENDAR
        // =========================================================================
        if (showMonthFilterSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showMonthFilterSheet = false },
                sheetState = sheetState,
                containerColor = AppTheme.colors.surface
            ) {
                MonthFilterBottomSheet(
                    currentFilter = selectedMonthFilter,
                    onSelect = {
                        selectedMonthFilter = it
                        showMonthFilterSheet = false
                    }
                )
            }
        }

        if (showNepaliCalendarSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showNepaliCalendarSheet = false },
                sheetState = sheetState,
                containerColor = AppTheme.colors.surface
            ) {
                NepaliCalendarBottomSheet(
                    onClose = { showNepaliCalendarSheet = false }
                )
            }
        }
    }
}

// =========================================================================
// ATRI KHATA GEOMETRIC 3D LOGO
// =========================================================================
@Composable
fun AtriKhataLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Left Book Page (Deep Blue)
        val leftPath = Path().apply {
            moveTo(w * 0.15f, h * 0.25f)
            lineTo(w * 0.48f, h * 0.15f)
            lineTo(w * 0.48f, h * 0.82f)
            lineTo(w * 0.15f, h * 0.92f)
            close()
        }
        drawPath(
            path = leftPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                start = Offset(0f, 0f),
                end = Offset(w * 0.5f, h)
            )
        )

        // Right Book Page (Bright Cyan Glow)
        val rightPath = Path().apply {
            moveTo(w * 0.52f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.25f)
            lineTo(w * 0.85f, h * 0.92f)
            lineTo(w * 0.52f, h * 0.82f)
            close()
        }
        drawPath(
            path = rightPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF67E8F9), Color(0xFF38BDF8)),
                start = Offset(w * 0.5f, 0f),
                end = Offset(w, h)
            )
        )

        // Center spine accent
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.50f, h * 0.14f),
            end = Offset(w * 0.50f, h * 0.83f),
            strokeWidth = 2f
        )
    }
}

// =========================================================================
// VISUAL SPARKLINE TREND CHART (HERO CARD)
// =========================================================================
@Composable
fun VisualTrendSparkline(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, h * 0.80f)
            cubicTo(
                w * 0.25f, h * 0.90f,
                w * 0.35f, h * 0.50f,
                w * 0.50f, h * 0.65f
            )
            cubicTo(
                w * 0.65f, h * 0.75f,
                w * 0.75f, h * 0.20f,
                w * 1.00f, h * 0.10f
            )
        }

        // Translucent gradient fill under curve
        val fillPath = Path().apply {
            addPath(path)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x5538BDF8), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Glowing stroke line
        drawPath(
            path = path,
            color = Color(0xFF38BDF8),
            style = Stroke(
                width = 2.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Glowing Endpoint Dot at top right
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.4f),
            radius = 6.dp.toPx(),
            center = Offset(w, h * 0.10f)
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(w, h * 0.10f)
        )
    }
}

// =========================================================================
// SUMMARY METRIC CARD (4 CARDS)
// =========================================================================
@Composable
fun SummaryMetricCard(
    title: String,
    amount: String,
    trendText: String,
    isPositiveTrend: Boolean,
    icon: ImageVector,
    accentColor: Color,
    cardBgColor: Color,
    borderColor: Color,
    iconContainerBg: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .width(132.dp)
            .height(122.dp)
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
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = amount,
                    color = Color(0xFF0F172A),
                    fontSize = 12.5.sp,
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

// =========================================================================
// QUICK ACTION ITEM (8 ICONS)
// =========================================================================
@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    containerBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, iconColor.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = AppTheme.colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

// =========================================================================
// RECENT TRANSACTION LIST ITEM
// =========================================================================
@Composable
fun RecentTransactionItem(
    icon: ImageVector,
    title: String,
    nepaliDate: String,
    category: String,
    amount: String,
    status: String,
    isIncome: Boolean,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title and Date
        Column(modifier = Modifier.weight(1.3f)) {
            Text(
                text = title,
                color = AppTheme.colors.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = nepaliDate,
                color = AppTheme.colors.textMuted,
                fontSize = 11.sp
            )
        }

        // Category Tag
        Text(
            text = category,
            color = AppTheme.colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        // Amount
        Text(
            text = amount,
            color = if (isIncome) Color(0xFF059669) else Color(0xFFE11D48),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Status Badge: "Paid" or "Received"
        val statusBg = if (status == "Received") Color(0xFFCCFBF1) else Color(0xFFDCFCE7)
        val statusText = if (status == "Received") Color(0xFF0F766E) else Color(0xFF15803D)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusBg)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = status,
                color = statusText,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

// =========================================================================
// MONTH FILTER BOTTOM SHEET
// =========================================================================
@Composable
fun MonthFilterBottomSheet(
    currentFilter: String,
    onSelect: (String) -> Unit
) {
    val filters = listOf(
        "This Month",
        "Last Month",
        "This Quarter",
        "Fiscal Year 2082/83",
        "All Time"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Select Analytics Period",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(14.dp))

        filters.forEach { filter ->
            val isSelected = filter == currentFilter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
                if (isSelected) {
                    Text("✓", color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// =========================================================================
// NEPALI CALENDAR BOTTOM SHEET
// =========================================================================
@Composable
fun NepaliCalendarBottomSheet(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Nepali Bikram Sambat (BS) Calendar",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Close",
                color = Color(0xFF0284C7),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onClose() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Today's Date", color = Color(0xFF2563EB), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text("2082 Baisakh 21, Thursday", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Gregorian: September 17, 2026 AD • Nepal Standard Time (UTC +5:45)", color = Color(0xFF64748B), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Fiscal Year: FY 2082/2083", color = Color(0xFF475569), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text("Active Tax Period: Shrawan - Ashadh (Nepal IRD Standard)", color = Color(0xFF64748B), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(20.dp))
    }
}
