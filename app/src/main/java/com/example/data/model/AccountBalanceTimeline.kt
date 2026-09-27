package com.example.data.model

/**
 * A time-series data point representing balances of accounts and key financial health indicators
 * at a specific point in time in the accounting ledger.
 */
data class AccountTimelinePoint(
    val timestampMillis: Long,
    val dateLabel: String,      // e.g. "Sep 15", "06/01"
    val dateBs: String,         // Nepali Bikram Sambat date e.g. "2081-06-01"
    val dateAd: String,         // Gregorian date e.g. "2024-09-17"

    // Core accounting equation: Assets = Liabilities + Equity
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,       // Equity = Assets - Liabilities

    // Key liquid & operational accounts
    val liquidCash: Double,     // Cash in Hand + Bank Account
    val accountsReceivable: Double,
    val accountsPayable: Double,
    val workingCapital: Double, // liquidCash + accountsReceivable - accountsPayable

    // Cumulative Operating Performance
    val cumulativeIncome: Double,
    val cumulativeExpense: Double,
    val cumulativeNetProfit: Double,

    // Specific Chart of Accounts balances mapped by account name
    val accountBalances: Map<String, Double> = emptyMap()
)

/**
 * Audit and financial health assessment computed directly from the ledger.
 */
data class FinancialHealthAudit(
    val overallScore: Int,                 // 0 to 100
    val rating: String,                    // "Excellent", "Strong", "Stable", "Caution"
    val currentRatio: Double,              // Current Assets / Current Liabilities
    val quickRatio: Double,                // Liquid Cash / Current Liabilities
    val debtToAssetRatio: Double,          // Liabilities / Assets
    val netMarginPercent: Double,          // Profit / Revenue %
    val workingCapital: Double,            // Assets - Liabilities
    val liquidCashRunwayDays: Int,         // Days of operational runway
    val isLedgerBalanced: Boolean,         // Double-Entry verification: Sum(Dr) == Sum(Cr)
    val totalDebitVolume: Double,
    val totalCreditVolume: Double,
    val balanceDifference: Double,         // 0.0 for balanced ledger
    val insights: List<HealthInsight>
)

data class HealthInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val metricValue: String? = null
)

enum class InsightType {
    POSITIVE, NEUTRAL, WARNING
}

enum class TimelineMetricType(val displayName: String, val subtitle: String) {
    ASSETS_VS_LIABILITIES("Assets vs Liabilities", "Solvency & Net Worth trajectory"),
    LIQUID_CASH("Liquid Cash & Bank", "Cash in Hand + Bank Account liquidity"),
    WORKING_CAPITAL("Working Capital (AR vs AP)", "Receivables vs Payables balance"),
    NET_WORTH("Net Worth (Equity)", "Total cumulative equity over time"),
    ACCOUNT_DETAIL("Individual Account", "Track single account ledger trajectory")
}

enum class TimelineTimeRange(val displayName: String, val days: Int) {
    DAYS_7("7D", 7),
    DAYS_30("30D", 30),
    DAYS_90("90D", 90),
    FISCAL_YEAR("FY 2081/82", 365),
    ALL_TIME("All", -1)
}
