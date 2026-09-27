package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountTimelinePoint
import com.example.data.model.TimelineMetricType
import com.example.data.model.TimelineTimeRange
import com.example.service.pdf.LedgerPdfGenerator
import com.example.ui.MainViewModel
import com.example.ui.components.charts.FinancialHealthCard
import com.example.ui.components.charts.RechartsComposeChart
import com.example.ui.components.charts.RechartsWebView
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.util.Locale

enum class ChartEngineMode {
    COMPOSE_NATIVE,
    RECHARTS_WEBVIEW
}

@Composable
fun LedgerDashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val monthlyData by viewModel.monthlyLedgerAnalytics.collectAsStateWithLifecycle()
    val ledgerEntries by viewModel.allLedgerEntries.collectAsStateWithLifecycle()
    val timelineData by viewModel.balanceTimeline.collectAsStateWithLifecycle()
    val healthAudit by viewModel.financialHealthAudit.collectAsStateWithLifecycle()
    val selectedMetric by viewModel.chartMetricType.collectAsStateWithLifecycle()
    val selectedRange by viewModel.chartTimeRange.collectAsStateWithLifecycle()
    val selectedAccount by viewModel.timelineAccountName.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Balances & Health, 1: Monthly Inflow/Outflow
    var engineMode by remember { mutableStateOf(ChartEngineMode.COMPOSE_NATIVE) }
    var isSyncing by remember { mutableStateOf(false) }

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    val latestPoint = remember(timelineData) { timelineData.lastOrNull() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .testTag("ledger_dashboard_root")
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("ledger_dashboard_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ledger Analytics & Health",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recharts Balances Over Time & GL Audit",
                        color = SkyBlueBright,
                        fontSize = 11.5.sp
                    )
                }

                // Sync Ledger button
                IconButton(
                    onClick = {
                        isSyncing = true
                        viewModel.syncLedgerFromTransactions { count ->
                            isSyncing = false
                            Toast.makeText(context, "Synchronized $count ledger vouchers", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("ledger_sync_btn")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SkyBlueBright, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", tint = SkyBlueBright)
                    }
                }

                // Export PDF button
                IconButton(
                    onClick = {
                        try {
                            val header = LedgerPdfGenerator.LedgerReportHeader(
                                businessName = viewModel.selectedBusiness.value.ifBlank { "Atri Khata Business" },
                                subtitle = "Official Double-Entry Balance & GL Health Report",
                                fiscalYear = "FY 2081/82"
                            )
                            val file = LedgerPdfGenerator.generateGeneralLedgerPdf(context, header, ledgerEntries)
                            LedgerPdfGenerator.openLedgerReport(context, file)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("ledger_pdf_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = SkyBlueBright)
                }
            }
        }

        // Top Navigation Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = SurfaceDark,
            contentColor = SkyBlueBright,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = SkyBlueBright,
                    height = 2.5.dp
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Timeline,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (activeTab == 0) SkyBlueBright else TextSubtle
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Balances & Health",
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_balances_health")
            )

            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (activeTab == 1) SkyBlueBright else TextSubtle
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Monthly Inflow / Outflow",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_monthly_inflow")
            )
        }

        // Scrollable Tab Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (activeTab == 0) {
                // =============================================================
                // TAB 0: ACCOUNT BALANCES OVER TIME & FINANCIAL HEALTH (RECHARTS)
                // =============================================================

                // Top Controls: Engine Switcher & Time Horizon Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Range Selector Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimelineTimeRange.entries.forEach { range ->
                            val isSelected = selectedRange == range
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SkyBlue else SurfaceDark)
                                    .border(
                                        1.dp,
                                        if (isSelected) SkyBlueBright else CardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setChartTimeRange(range) }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = range.displayName,
                                    color = if (isSelected) TextWhite else TextSubtle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Recharts Engine Toggle (Compose Native vs Recharts WebView)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Native Compose Option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (engineMode == ChartEngineMode.COMPOSE_NATIVE) SkyBlue.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { engineMode = ChartEngineMode.COMPOSE_NATIVE }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (engineMode == ChartEngineMode.COMPOSE_NATIVE) SkyBlueBright else TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Native",
                                    color = if (engineMode == ChartEngineMode.COMPOSE_NATIVE) SkyBlueBright else TextMuted,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Web Recharts Option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (engineMode == ChartEngineMode.RECHARTS_WEBVIEW) SkyBlue.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { engineMode = ChartEngineMode.RECHARTS_WEBVIEW }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Language,
                                    contentDescription = null,
                                    tint = if (engineMode == ChartEngineMode.RECHARTS_WEBVIEW) SkyBlueBright else TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Recharts Web",
                                    color = if (engineMode == ChartEngineMode.RECHARTS_WEBVIEW) SkyBlueBright else TextMuted,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Metric Filter Chips (Assets vs Liabilities, Liquid Cash, Working Capital, Net Worth, Account Detail)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimelineMetricType.entries.forEach { metric ->
                        val isSelected = selectedMetric == metric
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SkyBlue.copy(alpha = 0.18f) else SurfaceDark)
                                .border(
                                    1.dp,
                                    if (isSelected) SkyBlueBright else CardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setChartMetricType(metric) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SkyBlueBright)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = metric.displayName,
                                    color = if (isSelected) TextWhite else TextSubtle,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // If "Individual Account" selected, show Account Picker Chips
                if (selectedMetric == TimelineMetricType.ACCOUNT_DETAIL) {
                    val availableAccounts = accounts.ifEmpty {
                        listOf("Cash in Hand", "Bank Account", "Accounts Receivable", "Accounts Payable", "Sales Account")
                            .mapIndexed { idx, name ->
                                com.example.data.local.entity.AccountBalanceEntity(
                                    accountCode = "100$idx",
                                    accountName = name,
                                    accountType = "Asset"
                                )
                            }
                    }

                    Column {
                        Text("Select Ledger Account:", color = TextSubtle, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableAccounts.forEach { acc ->
                                val isSelected = selectedAccount == acc.accountName
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF14B8A6).copy(alpha = 0.2f) else CardDark)
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF14B8A6) else CardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setTimelineAccountName(acc.accountName) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = acc.accountName,
                                        color = if (isSelected) TextWhite else TextSubtle,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Recharts Visualization Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedMetric.displayName,
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedMetric.subtitle,
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                            }

                            // Engine Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SkyBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (engineMode == ChartEngineMode.COMPOSE_NATIVE) "Compose Recharts" else "Recharts WebView",
                                    color = SkyBlueBright,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Render Selected Engine
                        if (engineMode == ChartEngineMode.COMPOSE_NATIVE) {
                            RechartsComposeChart(
                                timelineData = timelineData,
                                metricType = selectedMetric,
                                selectedAccountName = selectedAccount
                            )
                        } else {
                            RechartsWebView(
                                timelineData = timelineData,
                                metricType = selectedMetric,
                                selectedAccountName = selectedAccount
                            )
                        }
                    }
                }

                // Executive Balance KPI Cards Row
                if (latestPoint != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiSummaryPill(
                            title = "NET WORTH (EQUITY)",
                            value = "Rs. ${currencyFormatter.format(latestPoint.netWorth)}",
                            subtitle = "Assets - Liabilities",
                            valueColor = SkyBlueBright,
                            modifier = Modifier.weight(1f)
                        )
                        KpiSummaryPill(
                            title = "LIQUID CASH & BANK",
                            value = "Rs. ${currencyFormatter.format(latestPoint.liquidCash)}",
                            subtitle = "Cash In Hand + Bank",
                            valueColor = Color(0xFF22C55E),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiSummaryPill(
                            title = "WORKING CAPITAL",
                            value = "Rs. ${currencyFormatter.format(latestPoint.workingCapital)}",
                            subtitle = "AR Rs.${currencyFormatter.format(latestPoint.accountsReceivable)} vs AP Rs.${currencyFormatter.format(latestPoint.accountsPayable)}",
                            valueColor = Color(0xFFA855F7),
                            modifier = Modifier.weight(1f)
                        )
                        KpiSummaryPill(
                            title = "SOLVENCY RATIO",
                            value = "${String.format(Locale.US, "%.1f", if (latestPoint.totalLiabilities > 0) latestPoint.totalAssets / latestPoint.totalLiabilities else 4.5)}x",
                            subtitle = "Assets / Liabilities",
                            valueColor = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Financial Health Audit Card
                FinancialHealthCard(audit = healthAudit)

                // Chronological Balance Progression Table Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Account Balances Over Time History",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceDark)
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DATE (BS/AD)", color = TextSubtle, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                            Text("ASSETS", color = TextSubtle, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text("LIABILITIES", color = TextSubtle, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            Text("NET WORTH", color = TextSubtle, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.1f))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val displayPoints = timelineData.takeLast(10).reversed()
                        displayPoints.forEachIndexed { index, pt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text(pt.dateLabel, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(pt.dateBs.ifBlank { pt.dateAd }, color = TextMuted, fontSize = 9.5.sp)
                                }
                                Text("Rs.${currencyFormatter.format(pt.totalAssets)}", color = Color(0xFF22C55E), fontSize = 11.5.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text("Rs.${currencyFormatter.format(pt.totalLiabilities)}", color = Color(0xFFEF4444), fontSize = 11.5.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text(
                                    text = "Rs.${currencyFormatter.format(pt.netWorth)}",
                                    color = SkyBlueBright,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f)
                                )
                            }
                            if (index < displayPoints.size - 1) {
                                HorizontalDivider(color = CardBorder.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 6.dp))
                            }
                        }
                    }
                }

            } else {
                // =============================================================
                // TAB 1: MONTHLY INFLOW VS OUTFLOW (P&L)
                // =============================================================

                val totalAnnualIncome = remember(monthlyData) { monthlyData.sumOf { it.income } }
                val totalAnnualExpense = remember(monthlyData) { monthlyData.sumOf { it.expense } }
                val netAnnualProfit = totalAnnualIncome - totalAnnualExpense
                val overallMargin = if (totalAnnualIncome > 0) ((netAnnualProfit / totalAnnualIncome) * 100) else 0.0

                // Executive KPI Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiSummaryPill(
                        title = "TOTAL INCOME",
                        value = "Rs. ${currencyFormatter.format(totalAnnualIncome)}",
                        subtitle = "12 Months Operating Rev",
                        valueColor = Color(0xFF22C55E),
                        modifier = Modifier.weight(1f)
                    )
                    KpiSummaryPill(
                        title = "TOTAL EXPENSES",
                        value = "Rs. ${currencyFormatter.format(totalAnnualExpense)}",
                        subtitle = "${String.format(Locale.US, "%.1f", if (totalAnnualIncome > 0) (totalAnnualExpense / totalAnnualIncome) * 100 else 0.0)}% of Income",
                        valueColor = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Net Profit & Margin Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NET MARGIN (PROFIT / LOSS)", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${if (netAnnualProfit >= 0) "+ " else "- "}Rs. ${currencyFormatter.format(Math.abs(netAnnualProfit))}",
                                color = if (netAnnualProfit >= 0) SkyBlueBright else Color(0xFFEF4444),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (netAnnualProfit >= 0) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", overallMargin)}% Margin",
                                color = if (netAnnualProfit >= 0) Color(0xFF22C55E) else Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Monthly Inflow vs Outflow Canvas & Table Section
                MonthlyIncomeExpenseSection(monthlyData = monthlyData)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KpiSummaryPill(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, CardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = TextSubtle, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}
