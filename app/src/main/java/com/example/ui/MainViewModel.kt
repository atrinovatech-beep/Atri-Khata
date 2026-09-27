package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.analytics.FinancialHealthEngine
import com.example.data.local.AppDatabase
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.StaffMemberEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountTimelinePoint
import com.example.data.model.AppSettings
import com.example.data.model.AppThemeMode
import com.example.data.model.BusinessInventoryPreset
import com.example.data.model.BusinessInvoicePreset
import com.example.data.model.BusinessTransactionPreset
import com.example.data.model.FinancialHealthAudit
import com.example.data.model.InventorySettings
import com.example.data.model.InvoiceSettings
import com.example.data.model.PartySettings
import com.example.data.model.BusinessPartyPreset
import com.example.data.model.PartySortOption
import com.example.data.model.PurchaseLineItem
import com.example.data.model.TimelineMetricType
import com.example.data.model.TimelineTimeRange
import com.example.data.model.TransactionSettings
import com.example.data.repository.BusinessRepository
import com.example.util.NepaliDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat

enum class NavTab {
    HOME,
    TRANSACTIONS,
    REPORTS,
    PARTIES,
    INVENTORY,
    MORE
}

enum class ActiveDialog {
    NONE,
    QUICK_ENTRY,
    QUICK_POS,
    VIEW_REPORTS,
    CREDIT_REMINDER,
    ADD_PARTY,
    SALES_INVOICE,
    PAYMENT_IN,
    PAYMENT_OUT,
    ADD_ITEM,
    BUSINESS_SWITCHER,
    NOTIFICATIONS,
    REWARDS_WALLET,
    EDIT_SHORTCUTS,
    MANAGE_STAFF,
    ACCOUNT_TRANSFER,
    GOOGLE_DRIVE_BACKUP,
    SETTINGS,
    PURCHASE_INVOICE,
    USER_ACCOUNT_SYNC,
    BULK_IMPORT,
    DATE_CONVERTER,
    LEDGER_DASHBOARD,
    FIREBASE_CLOUD_BACKUP,
    ADD_TRANSACTION
}

data class SearchResults(
    val parties: List<PartyEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val items: List<InventoryItemEntity> = emptyList()
)

data class MonthlyLedgerData(
    val monthIndex: Int, // 1 to 12
    val monthName: String,
    val income: Double,
    val expense: Double,
    val netProfit: Double = income - expense,
    val marginPercent: Double = if (income > 0) ((income - expense) / income) * 100 else 0.0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = BusinessRepository(
        database.partyDao(),
        database.transactionDao(),
        database.inventoryDao(),
        database.staffDao(),
        database.salesInvoiceDao(),
        database.businessProfileDao()
    )

    val ledgerRepository = com.example.data.repository.LedgerRepository(database.ledgerDao())
    val accountBalanceRepository = com.example.data.repository.AccountBalanceRepository(
        database.accountBalanceDao(),
        database.ledgerDao()
    )
    val accountingRepository = com.example.data.repository.AccountingRepository(
        database.transactionDao(),
        database.accountBalanceDao(),
        database.ledgerDao()
    )

    val financialSummary: StateFlow<com.example.data.repository.FinancialSummary> =
        accountingRepository.financialSummary.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            com.example.data.repository.FinancialSummary()
        )

    val allLedgerEntries: StateFlow<List<com.example.data.local.entity.LedgerEntry>> =
        ledgerRepository.activeEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts: StateFlow<List<com.example.data.local.entity.AccountBalanceEntity>> =
        accountBalanceRepository.activeAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAssetsBalance: StateFlow<Double> =
        accountBalanceRepository.totalAssets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLiabilitiesBalance: StateFlow<Double> =
        accountBalanceRepository.totalLiabilities.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalEquityBalance: StateFlow<Double> =
        accountBalanceRepository.totalEquity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncomeBalance: StateFlow<Double> =
        accountBalanceRepository.totalIncome.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenseBalance: StateFlow<Double> =
        accountBalanceRepository.totalExpense.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val trialBalance: StateFlow<List<com.example.data.local.dao.AccountBalanceRow>> =
        ledgerRepository.trialBalance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyLedgerAnalytics: StateFlow<List<MonthlyLedgerData>> = combine(
        allLedgerEntries,
        repository.allTransactions
    ) { ledgerEntries, transactions ->
        val monthNames = listOf("Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin", "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra")
        val incomeByMonth = DoubleArray(12) { 0.0 }
        val expenseByMonth = DoubleArray(12) { 0.0 }

        if (ledgerEntries.isNotEmpty()) {
            ledgerEntries.filter { !it.isCancelled }.forEach { entry ->
                val monthIdx = extractNepaliMonthIndex(entry.postingDateBS, entry.postingDateMillis)
                if (monthIdx in 0..11) {
                    if (entry.accountType.equals("Income", ignoreCase = true) || entry.voucherType.contains("Sales", ignoreCase = true)) {
                        incomeByMonth[monthIdx] += entry.credit.coerceAtLeast(entry.debit)
                    } else if (entry.accountType.equals("Expense", ignoreCase = true) || entry.voucherType.contains("Purchase", ignoreCase = true) || entry.voucherType.contains("Payment Out", ignoreCase = true)) {
                        expenseByMonth[monthIdx] += entry.debit.coerceAtLeast(entry.credit)
                    }
                }
            }
        } else {
            transactions.filter { it.status != "Cancelled" }.forEach { tx ->
                val monthIdx = extractNepaliMonthIndex(tx.dateBs, tx.dateMillis)
                if (monthIdx in 0..11) {
                    val type = tx.type.lowercase()
                    if (type.contains("sale") || type.contains("receipt") || type.contains("payment in")) {
                        incomeByMonth[monthIdx] += tx.amount
                    } else if (type.contains("purchase") || type.contains("expense") || type.contains("payment out")) {
                        expenseByMonth[monthIdx] += tx.amount
                    }
                }
            }
        }

        val hasData = incomeByMonth.any { it > 0.0 } || expenseByMonth.any { it > 0.0 }
        if (!hasData) {
            val starterIncome = doubleArrayOf(45000.0, 52000.0, 68000.0, 74000.0, 85000.0, 92000.0, 110000.0, 98000.0, 88000.0, 95000.0, 105000.0, 118000.0)
            val starterExpense = doubleArrayOf(28000.0, 31000.0, 42000.0, 45000.0, 49000.0, 56000.0, 62000.0, 58000.0, 51000.0, 55000.0, 60000.0, 65000.0)
            monthNames.mapIndexed { idx, name ->
                MonthlyLedgerData(
                    monthIndex = idx + 1,
                    monthName = name,
                    income = starterIncome[idx],
                    expense = starterExpense[idx]
                )
            }
        } else {
            monthNames.mapIndexed { idx, name ->
                MonthlyLedgerData(
                    monthIndex = idx + 1,
                    monthName = name,
                    income = incomeByMonth[idx],
                    expense = expenseByMonth[idx]
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // Balance Timeline & Financial Health (Recharts Integration)
    // =========================================================================

    private val _chartMetricType = MutableStateFlow(TimelineMetricType.ASSETS_VS_LIABILITIES)
    val chartMetricType: StateFlow<TimelineMetricType> = _chartMetricType.asStateFlow()

    private val _chartTimeRange = MutableStateFlow(TimelineTimeRange.ALL_TIME)
    val chartTimeRange: StateFlow<TimelineTimeRange> = _chartTimeRange.asStateFlow()

    private val _timelineAccountName = MutableStateFlow("Cash in Hand")
    val timelineAccountName: StateFlow<String> = _timelineAccountName.asStateFlow()

    val balanceTimeline: StateFlow<List<AccountTimelinePoint>> = combine(
        allLedgerEntries,
        allAccounts,
        repository.allTransactions,
        _chartTimeRange
    ) { ledgerEntries, accounts, transactions, timeRange ->
        FinancialHealthEngine.calculateTimelinePoints(
            ledgerEntries = ledgerEntries,
            accounts = accounts,
            transactions = transactions,
            timeRange = timeRange
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialHealthAudit: StateFlow<FinancialHealthAudit> = combine(
        balanceTimeline,
        allAccounts,
        allLedgerEntries
    ) { timeline, accounts, ledgerEntries ->
        FinancialHealthEngine.auditFinancialHealth(
            timeline = timeline,
            accounts = accounts,
            ledgerEntries = ledgerEntries
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialHealthAudit(
            overallScore = 85,
            rating = "Strong",
            currentRatio = 2.4,
            quickRatio = 1.9,
            debtToAssetRatio = 0.22,
            netMarginPercent = 28.5,
            workingCapital = 185000.0,
            liquidCashRunwayDays = 120,
            isLedgerBalanced = true,
            totalDebitVolume = 350000.0,
            totalCreditVolume = 350000.0,
            balanceDifference = 0.0,
            insights = emptyList()
        )
    )

    fun setChartMetricType(type: TimelineMetricType) {
        _chartMetricType.value = type
    }

    fun setChartTimeRange(range: TimelineTimeRange) {
        _chartTimeRange.value = range
    }

    fun setTimelineAccountName(name: String) {
        _timelineAccountName.value = name
    }

    private fun extractNepaliMonthIndex(bsDate: String, millis: Long): Int {
        if (bsDate.isNotBlank()) {
            val parts = bsDate.split("/", "-", ".")
            if (parts.size >= 2) {
                val m = parts[1].toIntOrNull()
                if (m != null && m in 1..12) return m - 1
            }
        }
        val nepali = NepaliDateUtils.adToBs(millis)
        return (nepali.month - 1).coerceIn(0, 11)
    }

    fun syncLedgerFromTransactions(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val txList = allTransactions.value
            var count = 0
            txList.forEach { tx ->
                val type = tx.type.lowercase()
                val res = if (type.contains("sale")) {
                    ledgerRepository.recordSalesInvoiceGL(
                        invoiceNumber = tx.invoiceNumber.ifBlank { "TX-${tx.id}" },
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        grandTotal = tx.amount,
                        paidAmount = if (tx.paymentMethod.lowercase() != "credit") tx.amount else 0.0,
                        paymentMethod = tx.paymentMethod,
                        dateMillis = tx.dateMillis,
                        dateBs = tx.dateBs,
                        dateAd = tx.dateAd,
                        remarks = tx.notes
                    )
                } else if (type.contains("purchase")) {
                    ledgerRepository.recordPurchaseInvoiceGL(
                        purchaseNumber = tx.invoiceNumber.ifBlank { "PUR-${tx.id}" },
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        grandTotal = tx.amount,
                        paidAmount = if (tx.paymentMethod.lowercase() != "credit") tx.amount else 0.0,
                        paymentMethod = tx.paymentMethod,
                        dateMillis = tx.dateMillis,
                        dateBs = tx.dateBs,
                        dateAd = tx.dateAd,
                        remarks = tx.notes
                    )
                } else if (type.contains("payment in") || type.contains("receipt")) {
                    ledgerRepository.recordPaymentInGL(
                        receiptNumber = tx.invoiceNumber.ifBlank { "REC-${tx.id}" },
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        amount = tx.amount,
                        paymentMethod = tx.paymentMethod,
                        dateMillis = tx.dateMillis,
                        dateBs = tx.dateBs,
                        dateAd = tx.dateAd,
                        remarks = tx.notes
                    )
                } else if (type.contains("payment out") || type.contains("expense")) {
                    ledgerRepository.recordPaymentOutGL(
                        voucherNumber = tx.invoiceNumber.ifBlank { "PAY-${tx.id}" },
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        amount = tx.amount,
                        paymentMethod = tx.paymentMethod,
                        dateMillis = tx.dateMillis,
                        dateBs = tx.dateBs,
                        dateAd = tx.dateAd,
                        remarks = tx.notes
                    )
                } else null

                if (res != null && res.isSuccess) count++
            }
            // Synchronize chart of accounts balances from posted ledger entries
            accountBalanceRepository.syncBalancesWithLedger()
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    val allSalesInvoices: StateFlow<List<com.example.data.local.entity.SalesInvoiceEntity>> =
        repository.allSalesInvoices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoicesWithDetails: StateFlow<List<com.example.data.local.relation.SalesInvoiceWithDetails>> =
        repository.allInvoicesWithDetails.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessProfile: StateFlow<com.example.data.local.entity.BusinessProfileEntity?> =
        repository.businessProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Navigation & View State
    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _selectedInvoiceId = MutableStateFlow<Long?>(null)
    val selectedInvoiceId: StateFlow<Long?> = _selectedInvoiceId.asStateFlow()

    fun selectInvoiceForDetail(invoiceId: Long?) {
        _selectedInvoiceId.value = invoiceId
    }

    private val _selectedPartyIdForDetail = MutableStateFlow<Long?>(null)
    val selectedPartyIdForDetail: StateFlow<Long?> = _selectedPartyIdForDetail.asStateFlow()

    fun selectPartyForDetail(partyId: Long?) {
        _selectedPartyIdForDetail.value = partyId
    }

    private val _privacyMode = MutableStateFlow(false)
    val privacyMode: StateFlow<Boolean> = _privacyMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeDialog = MutableStateFlow(ActiveDialog.NONE)
    val activeDialog: StateFlow<ActiveDialog> = _activeDialog.asStateFlow()

    private val _preselectedPartyForPurchase = MutableStateFlow<PartyEntity?>(null)
    val preselectedPartyForPurchase: StateFlow<PartyEntity?> = _preselectedPartyForPurchase.asStateFlow()

    private val _rewardCoins = MutableStateFlow(0)
    val rewardCoins: StateFlow<Int> = _rewardCoins.asStateFlow()

    private val _selectedBusiness = MutableStateFlow("My Business")
    val selectedBusiness: StateFlow<String> = _selectedBusiness.asStateFlow()

    private val _unreadNotifications = MutableStateFlow(0)
    val unreadNotifications: StateFlow<Int> = _unreadNotifications.asStateFlow()

    // Data from Room Database
    val allParties: StateFlow<List<PartyEntity>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInventoryItems: StateFlow<List<InventoryItemEntity>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<InventoryItemEntity>> = repository.lowStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInventoryCategories: StateFlow<List<String>> = repository.allInventoryCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStaff: StateFlow<List<StaffMemberEntity>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Google Drive Backup State
    private val _isGoogleAccountConnected = MutableStateFlow(false)
    val isGoogleAccountConnected: StateFlow<Boolean> = _isGoogleAccountConnected.asStateFlow()

    private val _connectedGoogleEmail = MutableStateFlow("")
    val connectedGoogleEmail: StateFlow<String> = _connectedGoogleEmail.asStateFlow()

    private val _lastBackupTime = MutableStateFlow("Never")
    val lastBackupTime: StateFlow<String> = _lastBackupTime.asStateFlow()

    private val _lastBackupSize = MutableStateFlow("0 KB")
    val lastBackupSize: StateFlow<String> = _lastBackupSize.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _autoBackupEnabled = MutableStateFlow(false)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()

    private val _backupFrequency = MutableStateFlow("Daily (Midnight)")
    val backupFrequency: StateFlow<String> = _backupFrequency.asStateFlow()

    // Atri Khata Google Auth & Cloud Sync Engine
    val sessionManager = com.example.data.auth.UserSessionManager(application)
    val googleAuthService = com.example.service.auth.GoogleAuthService(application)
    val cloudSyncEngine = com.example.service.sync.CloudSyncEngine(application)
    val firebaseBackupService = com.example.service.backup.FirebaseStorageBackupService(application)

    private val _isFirebaseBackingUp = MutableStateFlow(false)
    val isFirebaseBackingUp: StateFlow<Boolean> = _isFirebaseBackingUp.asStateFlow()

    private val _isFirebaseRestoring = MutableStateFlow(false)
    val isFirebaseRestoring: StateFlow<Boolean> = _isFirebaseRestoring.asStateFlow()

    private val _lastFirebaseBackupResult = MutableStateFlow<com.example.service.backup.CloudBackupResult?>(null)
    val lastFirebaseBackupResult: StateFlow<com.example.service.backup.CloudBackupResult?> = _lastFirebaseBackupResult.asStateFlow()

    private val _lastFirebaseRestoreResult = MutableStateFlow<com.example.service.backup.CloudRestoreResult?>(null)
    val lastFirebaseRestoreResult: StateFlow<com.example.service.backup.CloudRestoreResult?> = _lastFirebaseRestoreResult.asStateFlow()

    private val _firebaseSnapshots = MutableStateFlow<List<com.example.service.backup.CloudBackupSnapshot>>(emptyList())
    val firebaseSnapshots: StateFlow<List<com.example.service.backup.CloudBackupSnapshot>> = _firebaseSnapshots.asStateFlow()

    val userSession: StateFlow<com.example.data.auth.AtriUserSession?> = sessionManager.currentSession

    val isUserAuthenticated: StateFlow<Boolean> = sessionManager.currentSession
        .map { it != null && (it.isLoggedIn || it.isGuestMode) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            sessionManager.currentSession.value?.let { it.isLoggedIn || it.isGuestMode } ?: false
        )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow("Local Vault Ready")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    // Financial totals (Frappe-style)
    val totalToReceive: StateFlow<Double> = repository.totalToReceive
        .combine(_privacyMode) { amount, _ -> amount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalToGive: StateFlow<Double> = repository.totalToGive
        .combine(_privacyMode) { amount, _ -> amount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSales: StateFlow<Double> = repository.totalSales
        .combine(_privacyMode) { amount, _ -> amount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPurchases: StateFlow<Double> = repository.totalPurchases
        .combine(_privacyMode) { amount, _ -> amount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .combine(_privacyMode) { amount, _ -> amount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cashAndBankBalance: StateFlow<Double> = allTransactions.map { txList ->
        txList.filter { it.status != "Cancelled" }.sumOf { tx ->
            val type = tx.type.lowercase()
            when {
                type.contains("payment in") || type.contains("receipt") -> tx.amount
                type.contains("sale") && tx.paymentMethod.lowercase() != "credit" -> tx.amount
                type.contains("payment out") || type.contains("expense") -> -tx.amount
                type.contains("purchase") && tx.paymentMethod.lowercase() != "credit" -> -tx.amount
                else -> 0.0
            }
        }.coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Search results
    val searchResults: StateFlow<SearchResults> = combine(
        _searchQuery,
        allParties,
        allTransactions,
        allInventoryItems
    ) { query, parties, txs, items ->
        if (query.isBlank()) {
            SearchResults()
        } else {
            val q = query.trim().lowercase()
            SearchResults(
                parties = parties.filter { it.name.lowercase().contains(q) || it.phone.contains(q) },
                transactions = txs.filter { it.partyName.lowercase().contains(q) || it.invoiceNumber.lowercase().contains(q) || it.type.lowercase().contains(q) },
                items = items.filter { it.name.lowercase().contains(q) || it.sku.lowercase().contains(q) || it.category.lowercase().contains(q) }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    init {
        // Ensure pre-population runs and chart of accounts is synchronized
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.populateInitialData(database)
            accountBalanceRepository.syncBalancesWithLedger()
        }
    }

    fun syncAccountBalances(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            accountBalanceRepository.syncBalancesWithLedger()
            onComplete()
        }
    }

    // Actions
    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun togglePrivacyMode() {
        _privacyMode.value = !_privacyMode.value
    }

    fun setPrivacyMode(enabled: Boolean) {
        _privacyMode.value = enabled
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun openDialog(dialog: ActiveDialog) {
        _activeDialog.value = dialog
    }

    fun openPurchaseDialog(party: PartyEntity? = null) {
        _preselectedPartyForPurchase.value = party
        _activeDialog.value = ActiveDialog.PURCHASE_INVOICE
    }

    fun closeDialog() {
        _activeDialog.value = ActiveDialog.NONE
        _preselectedPartyForPurchase.value = null
    }

    fun selectBusiness(name: String) {
        _selectedBusiness.value = name
        closeDialog()
    }

    fun markNotificationsRead() {
        _unreadNotifications.value = 0
    }

    fun addRewardCoins(amount: Int) {
        _rewardCoins.value += amount
    }

    // Operations
    fun addParty(party: PartyEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertParty(party)
        }
    }

    fun addParty(
        name: String,
        phone: String,
        type: String,
        balance: Double,
        address: String,
        contactNumber: String = "",
        category: String = if (type == "Customer") "Retail" else "Wholesaler",
        openingBalance: Double = balance,
        balanceType: String = if (type == "Customer") "To Receive (Dr)" else "To Give (Cr)",
        panVatNumber: String = "",
        email: String = "",
        contactPerson: String = "",
        city: String = "",
        notes: String = "",
        registerDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDr = balanceType.contains("Receive", ignoreCase = true) || balanceType.contains("Dr", ignoreCase = true)
            val isCr = balanceType.contains("Give", ignoreCase = true) || balanceType.contains("Cr", ignoreCase = true)
            val toReceive = if (isDr) openingBalance else 0.0
            val toGive = if (isCr) openingBalance else 0.0

            val party = PartyEntity(
                name = name.trim(),
                phone = phone.trim(),
                contactNumber = contactNumber.trim(),
                type = type,
                category = category,
                balanceToReceive = toReceive,
                balanceToGive = toGive,
                openingBalance = openingBalance,
                balanceType = balanceType,
                panVatNumber = panVatNumber.trim(),
                email = email.trim(),
                contactPerson = contactPerson.trim(),
                address = address.trim(),
                city = city.trim(),
                notes = notes.trim(),
                registerDate = registerDate,
                createdAt = System.currentTimeMillis()
            )
            repository.insertParty(party)
        }
    }

    fun updateParty(party: PartyEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateParty(party)
        }
    }

    fun deleteParty(party: PartyEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteParty(party)
        }
    }

    fun bulkImportParties(
        partiesToInsert: List<PartyEntity>,
        partiesToUpdate: List<PartyEntity>,
        onComplete: (inserted: Int, updated: Int) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            partiesToInsert.forEach { repository.insertParty(it) }
            partiesToUpdate.forEach { repository.updateParty(it) }
            withContext(Dispatchers.Main) {
                onComplete(partiesToInsert.size, partiesToUpdate.size)
            }
        }
    }

    fun bulkImportItems(
        itemsToInsert: List<InventoryItemEntity>,
        itemsToUpdate: List<InventoryItemEntity>,
        onComplete: (inserted: Int, updated: Int) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            itemsToInsert.forEach { repository.insertItem(it) }
            itemsToUpdate.forEach { repository.updateItem(it) }
            withContext(Dispatchers.Main) {
                onComplete(itemsToInsert.size, itemsToUpdate.size)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
        }
    }

    fun addTransaction(
        partyName: String,
        type: String,
        amount: Double,
        paymentMethod: String,
        invoiceNumber: String,
        notes: String,
        dateMillis: Long = System.currentTimeMillis(),
        dateBs: String = "",
        dateAd: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolvedBs = if (dateBs.isNotBlank()) dateBs else NepaliDateUtils.formatBsDate(dateMillis)
            val resolvedAd = if (dateAd.isNotBlank()) dateAd else NepaliDateUtils.formatAdDate(dateMillis)
            val tx = TransactionEntity(
                partyName = partyName.ifBlank { "Cash Sale / Counter" },
                type = type,
                amount = amount,
                dateMillis = dateMillis,
                dateBs = resolvedBs,
                dateAd = resolvedAd,
                paymentMethod = paymentMethod,
                invoiceNumber = invoiceNumber.ifBlank { "INV-${(1000..9999).random()}" },
                notes = notes
            )
            repository.insertTransaction(tx)
        }
    }

    fun createSalesInvoice(
        invoice: com.example.data.local.entity.SalesInvoiceEntity,
        items: List<com.example.data.local.entity.SalesInvoiceItemEntity>,
        payments: List<com.example.data.local.entity.PaymentAllocationEntity> = emptyList(),
        activities: List<com.example.data.local.entity.InvoiceActivityEntity> = emptyList(),
        attachments: List<com.example.data.local.entity.InvoiceAttachmentEntity> = emptyList(),
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val invoiceId = repository.insertSalesInvoice(invoice, items, payments, activities, attachments)
            // Sales Integration: Deduct physical stock if decreaseStockOnSale is enabled
            if (_inventorySettings.value.decreaseStockOnSale) {
                items.forEach { lineItem ->
                    val prodId = lineItem.productId
                    if (prodId != null && prodId > 0) {
                        val product = repository.getInventoryItemByIdSync(prodId)
                        if (product != null) {
                            val newStock = product.stockQuantity - lineItem.quantity
                            val finalStock = if (!_inventorySettings.value.allowNegativeStock && !_inventorySettings.value.allowSaleWithoutStock) {
                                0.0.coerceAtLeast(newStock)
                            } else {
                                newStock
                            }
                            repository.updateItem(product.copy(stockQuantity = finalStock))
                        }
                    }
                }
            }
            // Also insert corresponding transaction record so transaction history and reports reflect it seamlessly
            val tx = TransactionEntity(
                partyId = invoice.partyId ?: 0L,
                partyName = invoice.partyNameSnapshot,
                type = "Sales Invoice",
                amount = invoice.grandTotal,
                dateMillis = invoice.dateMillis,
                dateBs = invoice.displayBsDate,
                dateAd = invoice.displayAdDate,
                paymentMethod = invoice.paymentMethod,
                invoiceNumber = invoice.invoiceNumber,
                notes = invoice.remarks ?: "",
                status = if (invoice.dueAmount <= 0.0) "Settled" else "Unpaid",
                salesInvoiceId = invoiceId
            )
            repository.insertTransaction(tx)
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                onSuccess(invoiceId)
            }
        }
    }

    fun getAttachmentsForInvoice(invoiceId: Long): Flow<List<com.example.data.local.entity.InvoiceAttachmentEntity>> =
        repository.getAttachmentsForInvoice(invoiceId)

    fun deleteInvoiceAttachment(attachment: com.example.data.local.entity.InvoiceAttachmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteInvoiceAttachment(attachment)
        }
    }

    fun deleteAttachment(attachment: com.example.data.local.entity.InvoiceAttachmentEntity) {
        deleteInvoiceAttachment(attachment)
    }

    fun addInvoiceAttachments(attachments: List<com.example.data.local.entity.InvoiceAttachmentEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertInvoiceAttachments(attachments)
        }
    }

    fun recordSalesInvoicePayment(
        payment: com.example.data.local.entity.PaymentAllocationEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = repository.recordInvoicePayment(payment)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to record payment")
                }
            }
        }
    }

    fun recordPurchaseInvoice(
        purchaseNumber: String,
        party: PartyEntity?,
        customPartyName: String,
        lineItems: List<PurchaseLineItem>,
        subtotal: Double,
        discountAmount: Double,
        taxableAmount: Double,
        vatAmount: Double,
        otherCharges: Double,
        roundOff: Double,
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        supplierBillNumber: String,
        referenceNumber: String,
        purchaseDateMillis: Long,
        purchaseDateBs: String,
        purchaseDateAd: String,
        dueDateMillis: Long? = null,
        dueDateBs: String = "",
        purchaseType: String = "Cash Purchase",
        warehouse: String = "Main Store",
        notes: String = "",
        attachmentUri: String? = null,
        updateStockCost: Boolean = true,
        onSuccess: (Long) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resolvedPartyName = party?.name ?: customPartyName.ifBlank { "Supplier Purchase" }
                val resolvedBs = if (purchaseDateBs.isNotBlank()) purchaseDateBs else NepaliDateUtils.formatBsDate(purchaseDateMillis)
                val resolvedAd = if (purchaseDateAd.isNotBlank()) purchaseDateAd else NepaliDateUtils.formatAdDate(purchaseDateMillis)
                val dueAmount = (grandTotal - paidAmount).coerceAtLeast(0.0)

                // 1. Compose detailed notes / description
                val itemSummary = lineItems.filter { it.name.isNotBlank() }.joinToString(", ") { "${it.name} (${it.quantity} ${it.unit})" }
                val compiledNotes = buildString {
                    if (supplierBillNumber.isNotBlank()) append("Bill: $supplierBillNumber. ")
                    if (referenceNumber.isNotBlank()) append("PO/Ref: $referenceNumber. ")
                    if (warehouse.isNotBlank() && warehouse != "Main Store") append("Warehouse: $warehouse. ")
                    if (itemSummary.isNotBlank()) append("Items: $itemSummary. ")
                    if (dueAmount > 0) append("Due Payable: Rs. $dueAmount. ")
                    if (notes.isNotBlank()) append(notes)
                }.trim()

                // 2. Insert Transaction record
                val tx = TransactionEntity(
                    partyId = party?.id ?: 0L,
                    partyName = resolvedPartyName,
                    type = "Purchase",
                    amount = grandTotal,
                    dateMillis = purchaseDateMillis,
                    dateBs = resolvedBs,
                    dateAd = resolvedAd,
                    paymentMethod = paymentMethod,
                    invoiceNumber = purchaseNumber.ifBlank { "PUR-${(1000..9999).random()}" },
                    notes = compiledNotes,
                    status = if (dueAmount <= 0.0) "Settled" else "Unpaid"
                )
                val txId = repository.insertTransaction(tx)

                // 3. Stock Integration: Increase inventory stock & optionally update cost price
                val shouldIncreaseStock = _inventorySettings.value.increaseStockOnPurchase || _transactionSettings.value.increaseStockOnPurchase
                if (shouldIncreaseStock) {
                    lineItems.forEach { lineItem ->
                        val pId = lineItem.itemId
                        if (pId != null && pId > 0) {
                            val product = repository.getInventoryItemByIdSync(pId)
                            if (product != null) {
                                val updatedQty = product.stockQuantity + lineItem.quantity
                                val updatedPrice = if (updateStockCost && lineItem.unitPrice > 0) lineItem.unitPrice else product.purchasePrice
                                repository.updateItem(
                                    product.copy(
                                        stockQuantity = updatedQty,
                                        purchasePrice = updatedPrice
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. Party Khata / Ledger Integration: update supplier's balanceToGive if unpaid/credit
                if (party != null && dueAmount > 0) {
                    val updatedParty = party.copy(
                        balanceToGive = party.balanceToGive + dueAmount
                    )
                    repository.updateParty(updatedParty)
                }

                // 5. Reward Coins
                addRewardCoins(20)

                withContext(Dispatchers.Main) {
                    onSuccess(txId)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to record purchase entry")
                }
            }
        }
    }

    fun getInvoiceWithDetails(id: Long): kotlinx.coroutines.flow.Flow<com.example.data.local.relation.SalesInvoiceWithDetails?> =
        repository.getInvoiceWithDetails(id)

    fun deleteSalesInvoice(id: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            // Restore inventory stock if decreaseStockOnSale was active
            if (_inventorySettings.value.decreaseStockOnSale) {
                val details = repository.getInvoiceWithDetailsByIdSync(id)
                details?.items?.forEach { lineItem ->
                    val prodId = lineItem.productId
                    if (prodId != null && prodId > 0) {
                        val product = repository.getInventoryItemByIdSync(prodId)
                        if (product != null) {
                            repository.updateItem(product.copy(stockQuantity = product.stockQuantity + lineItem.quantity))
                        }
                    }
                }
            }
            repository.deleteSalesInvoice(id)
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                if (_selectedInvoiceId.value == id) {
                    _selectedInvoiceId.value = null
                }
                onDeleted()
            }
        }
    }

    fun cancelSalesInvoice(id: Long, cancelledBy: String = "Admin", onCancelled: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            // Restore inventory stock if decreaseStockOnSale was active
            if (_inventorySettings.value.decreaseStockOnSale) {
                val details = repository.getInvoiceWithDetailsByIdSync(id)
                details?.items?.forEach { lineItem ->
                    val prodId = lineItem.productId
                    if (prodId != null && prodId > 0) {
                        val product = repository.getInventoryItemByIdSync(prodId)
                        if (product != null) {
                            repository.updateItem(product.copy(stockQuantity = product.stockQuantity + lineItem.quantity))
                        }
                    }
                }
            }
            repository.cancelSalesInvoice(id, cancelledBy)
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                onCancelled()
            }
        }
    }

    fun duplicateSalesInvoice(id: Long, onDuplicated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val newNumber = "INV-${(1000..9999).random()}-DUP"
            val newId = repository.duplicateSalesInvoice(id, newNumber)
            newId?.let { createdId ->
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    _selectedInvoiceId.value = createdId
                    onDuplicated(createdId)
                }
            }
        }
    }

    fun addInventoryItem(
        name: String,
        sku: String,
        category: String,
        quantity: Double,
        unit: String,
        purchasePrice: Double,
        salePrice: Double,
        minStock: Double,
        onSuccess: (Long) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val item = InventoryItemEntity(
                    name = name.trim(),
                    sku = sku.ifBlank { "SKU-${(100..999).random()}" },
                    category = category.ifBlank { "General" },
                    stockQuantity = quantity,
                    unit = unit.ifBlank { "pcs" },
                    purchasePrice = purchasePrice,
                    salePrice = salePrice,
                    minStockAlert = minStock
                )
                val id = repository.insertItem(item)
                withContext(Dispatchers.Main) {
                    onSuccess(id)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to add inventory item")
                }
            }
        }
    }

    fun updateInventoryItem(
        item: InventoryItemEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateItem(item)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to update item")
                }
            }
        }
    }

    fun deleteInventoryItem(
        item: InventoryItemEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteItem(item)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to delete item")
                }
            }
        }
    }

    fun adjustInventoryItemStock(
        item: InventoryItemEntity,
        newQuantity: Double,
        reason: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedItem = item.copy(stockQuantity = newQuantity)
                repository.updateItem(updatedItem)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to adjust stock")
                }
            }
        }
    }

    fun getInvoiceItemsForProduct(productId: Long): Flow<List<com.example.data.local.entity.SalesInvoiceItemEntity>> =
        repository.getInvoiceItemsForProduct(productId)

    suspend fun getItemInvoiceCount(productId: Long): Int =
        repository.getItemInvoiceCount(productId)

    // Staff Management Operations
    fun addStaffMember(
        name: String,
        phone: String,
        role: String,
        pin: String = "1234",
        avatarColor: Long = 0xFF00A3FF,
        canCreateSalesInvoice: Boolean = true,
        canEditSalesInvoice: Boolean = true,
        canDeleteSalesInvoice: Boolean = false,
        canViewCustomerLedger: Boolean = true,
        canGiveDiscounts: Boolean = false,
        canChangeSellingPrice: Boolean = false,
        canCreatePurchaseEntry: Boolean = false,
        canEditPurchaseEntry: Boolean = false,
        canDeletePurchaseEntry: Boolean = false,
        canViewSupplierLedger: Boolean = false,
        canAccessCashDrawer: Boolean = false,
        canViewFinancialReports: Boolean = false,
        canManageInventory: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val staff = StaffMemberEntity(
                name = name.trim(),
                phone = phone.trim(),
                role = role,
                pin = pin.ifBlank { "1234" },
                status = "Active",
                avatarColor = avatarColor,
                canCreateSalesInvoice = canCreateSalesInvoice,
                canEditSalesInvoice = canEditSalesInvoice,
                canDeleteSalesInvoice = canDeleteSalesInvoice,
                canViewCustomerLedger = canViewCustomerLedger,
                canGiveDiscounts = canGiveDiscounts,
                canChangeSellingPrice = canChangeSellingPrice,
                canCreatePurchaseEntry = canCreatePurchaseEntry,
                canEditPurchaseEntry = canEditPurchaseEntry,
                canDeletePurchaseEntry = canDeletePurchaseEntry,
                canViewSupplierLedger = canViewSupplierLedger,
                canAccessCashDrawer = canAccessCashDrawer,
                canViewFinancialReports = canViewFinancialReports,
                canManageInventory = canManageInventory
            )
            repository.insertStaff(staff)
        }
    }

    fun updateStaffMember(staff: StaffMemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStaff(staff)
        }
    }

    fun deleteStaffMember(staff: StaffMemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStaff(staff)
        }
    }

    // Cloud Backup & Restore Actions (Powered by Firebase Storage & Local Vault)
    fun refreshFirebaseSnapshots() {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionManager.currentSession.value
            val userId = session?.userId ?: "default_user"
            val snapshots = firebaseBackupService.getAvailableSnapshots(userId)
            _firebaseSnapshots.value = snapshots
        }
    }

    fun exportToFirebaseStorage(onDone: (com.example.service.backup.CloudBackupResult) -> Unit = {}) {
        if (_isFirebaseBackingUp.value) return
        val session = sessionManager.currentSession.value
        val userId = session?.userId ?: "atri_user_${System.currentTimeMillis()}"
        val userEmail = session?.email ?: _connectedGoogleEmail.value.ifBlank { "offline.backup@atrikhata.local" }

        viewModelScope.launch(Dispatchers.IO) {
            _isFirebaseBackingUp.value = true
            _isBackingUp.value = true
            val result = firebaseBackupService.exportAndUploadToFirebase(
                userId = userId,
                userEmail = userEmail,
                database = database,
                appSettings = _appSettings.value,
                transactionSettings = _transactionSettings.value,
                invoiceSettings = _invoiceSettings.value,
                partySettings = _partySettings.value
            )
            _isFirebaseBackingUp.value = false
            _isBackingUp.value = false
            _lastFirebaseBackupResult.value = result

            if (result.isSuccess) {
                val now = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())
                _lastBackupTime.value = "Today at $now"
                val sizeKb = result.backupSizeBytes / 1024
                _lastBackupSize.value = if (sizeKb > 1024) "%.1f MB".format(sizeKb / 1024f) else "$sizeKb KB"
                refreshFirebaseSnapshots()
            }

            withContext(Dispatchers.Main) {
                onDone(result)
            }
        }
    }

    fun restoreFromFirebaseStorage(
        snapshotId: String? = null,
        onDone: (com.example.service.backup.CloudRestoreResult) -> Unit = {}
    ) {
        if (_isFirebaseRestoring.value) return
        val session = sessionManager.currentSession.value
        val userId = session?.userId ?: "default_user"

        viewModelScope.launch(Dispatchers.IO) {
            _isFirebaseRestoring.value = true
            _isRestoring.value = true
            val result = firebaseBackupService.downloadAndRestoreFromFirebase(
                userId = userId,
                database = database,
                specificSnapshotFile = snapshotId,
                onSettingsRestored = { restoredApp, restoredTrans, restoredInv, restoredParty ->
                    _appSettings.value = restoredApp
                    _transactionSettings.value = restoredTrans
                    _invoiceSettings.value = restoredInv
                    _partySettings.value = restoredParty
                    _isDarkMode.value = restoredApp.themeMode == AppThemeMode.DARK
                    _currencyFormat.value = restoredApp.currencySymbol
                    _dateFormat.value = restoredApp.dateFormat
                    _salesPrefix.value = restoredTrans.salesInvoicePrefix
                    _defaultPaymentMode.value = restoredTrans.defaultPaymentMode
                    _autoRoundOff.value = restoredTrans.showRoundOff
                }
            )
            _isFirebaseRestoring.value = false
            _isRestoring.value = false
            _lastFirebaseRestoreResult.value = result
            refreshFirebaseSnapshots()

            withContext(Dispatchers.Main) {
                onDone(result)
            }
        }
    }

    fun exportBackupToFile(uri: android.net.Uri, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = firebaseBackupService.exportToFileUri(getApplication(), uri)
            withContext(Dispatchers.Main) {
                onDone(success)
            }
        }
    }

    fun restoreBackupFromFile(uri: android.net.Uri, onDone: (com.example.service.backup.CloudRestoreResult) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _isFirebaseRestoring.value = true
            val result = firebaseBackupService.restoreFromFileUri(
                context = getApplication(),
                uri = uri,
                database = database,
                onSettingsRestored = { restoredApp, restoredTrans, restoredInv, restoredParty ->
                    _appSettings.value = restoredApp
                    _transactionSettings.value = restoredTrans
                    _invoiceSettings.value = restoredInv
                    _partySettings.value = restoredParty
                    _isDarkMode.value = restoredApp.themeMode == AppThemeMode.DARK
                    _currencyFormat.value = restoredApp.currencySymbol
                    _dateFormat.value = restoredApp.dateFormat
                    _salesPrefix.value = restoredTrans.salesInvoicePrefix
                    _defaultPaymentMode.value = restoredTrans.defaultPaymentMode
                    _autoRoundOff.value = restoredTrans.showRoundOff
                }
            )
            _isFirebaseRestoring.value = false
            _lastFirebaseRestoreResult.value = result
            refreshFirebaseSnapshots()

            withContext(Dispatchers.Main) {
                onDone(result)
            }
        }
    }

    // Google Drive Backup Actions
    fun triggerGoogleDriveBackup() {
        exportToFirebaseStorage()
    }

    fun triggerGoogleDriveRestore(onComplete: () -> Unit = {}) {
        restoreFromFirebaseStorage {
            onComplete()
        }
    }

    fun toggleAutoBackup(enabled: Boolean) {
        _autoBackupEnabled.value = enabled
    }

    fun setBackupFrequency(freq: String) {
        _backupFrequency.value = freq
    }

    fun connectGoogleAccount(email: String) {
        _connectedGoogleEmail.value = email
        _isGoogleAccountConnected.value = true
        signInWithEmailDirectly(email, null) {}
    }

    fun disconnectGoogleAccount() {
        _isGoogleAccountConnected.value = false
        signOut()
    }

    suspend fun handleGoogleSignIn(
        activity: android.app.Activity,
        onResult: (com.example.service.auth.GoogleSignInResult) -> Unit
    ) {
        val result = googleAuthService.signIn(activity)
        if (result.isSuccess && result.userId != null) {
            val hasExistingCloudData = cloudSyncEngine.hasCloudProfile(result.userId)
            val newSession = com.example.data.auth.AtriUserSession(
                userId = result.userId,
                email = result.email ?: "",
                displayName = result.displayName ?: "User",
                photoUrl = result.photoUrl,
                accountType = com.example.data.auth.AccountType.BUSINESS,
                businessType = "General Trade",
                businessName = if (!result.displayName.isNullOrBlank()) "${result.displayName}'s Business" else "My Business",
                isLoggedIn = true,
                isGuestMode = false,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            sessionManager.saveSession(newSession)
            _connectedGoogleEmail.value = result.email ?: ""
            _isGoogleAccountConnected.value = true

            if (hasExistingCloudData) {
                withContext(Dispatchers.IO) {
                    cloudSyncEngine.restoreFromCloud(result.userId, database)
                }
            }
        }
        withContext(Dispatchers.Main) {
            onResult(result)
        }
    }

    fun signInWithEmailDirectly(email: String, name: String?, onComplete: (isNew: Boolean) -> Unit) {
        val result = googleAuthService.createAuthenticatedAccount(email, name)
        if (result.isSuccess && result.userId != null) {
            val hasExistingCloudData = cloudSyncEngine.hasCloudProfile(result.userId)
            val newSession = com.example.data.auth.AtriUserSession(
                userId = result.userId,
                email = result.email ?: email,
                displayName = result.displayName ?: name ?: "User",
                accountType = com.example.data.auth.AccountType.BUSINESS,
                businessType = "General Trade",
                businessName = if (!name.isNullOrBlank()) "$name's Business" else "My Business",
                isLoggedIn = true,
                isGuestMode = false,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            sessionManager.saveSession(newSession)
            _connectedGoogleEmail.value = email
            _isGoogleAccountConnected.value = true

            viewModelScope.launch(Dispatchers.IO) {
                if (hasExistingCloudData) {
                    cloudSyncEngine.restoreFromCloud(result.userId, database)
                }
                withContext(Dispatchers.Main) {
                    onComplete(!hasExistingCloudData)
                }
            }
        }
    }

    fun isNewUserSession(): Boolean {
        val session = sessionManager.currentSession.value ?: return true
        return !cloudSyncEngine.hasCloudProfile(session.userId)
    }

    fun continueAsGuest() {
        val guestSession = com.example.data.auth.AtriUserSession(
            userId = "local_offline_guest",
            email = "offline.guest@atrikhata.local",
            displayName = "Offline Guest",
            accountType = com.example.data.auth.AccountType.BUSINESS,
            businessType = "General Trade",
            businessName = "My Business",
            isLoggedIn = false,
            isGuestMode = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        sessionManager.saveSession(guestSession)
    }

    fun completeOnboarding(
        accountType: com.example.data.auth.AccountType,
        businessType: String,
        businessName: String,
        panVatNumber: String,
        isVatEnabled: Boolean,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = sessionManager.currentSession.value
            val updated = current?.copy(
                accountType = accountType,
                businessType = businessType,
                businessName = businessName,
                isLoggedIn = true
            ) ?: com.example.data.auth.AtriUserSession(
                userId = "usr_local_${System.currentTimeMillis()}",
                email = "",
                displayName = "User",
                accountType = accountType,
                businessType = businessType,
                businessName = businessName,
                isLoggedIn = true
            )
            sessionManager.saveSession(updated)

            // Update Business Profile in Room Database
            val existingProfile = database.businessProfileDao().getBusinessProfileSync()
            val newProfile = existingProfile?.copy(
                businessName = businessName,
                businessType = businessType,
                panVatNumber = panVatNumber,
                isVatEnabled = isVatEnabled,
                updatedAt = System.currentTimeMillis()
            ) ?: com.example.data.local.entity.BusinessProfileEntity(
                businessName = businessName,
                businessType = businessType,
                panVatNumber = panVatNumber,
                phone = "",
                email = updated.email,
                address = "",
                city = "",
                stateProvince = "",
                currencySymbol = "Rs.",
                defaultVatRate = 13.0,
                isVatEnabled = isVatEnabled,
                invoicePrefix = "INV",
                invoiceTerms = "1. Goods once sold will not be returned after 7 days.",
                invoiceHeader = "Tax Invoice",
                invoiceFooter = "Thank you for doing business with us."
            )
            database.businessProfileDao().insertProfile(newProfile)

            // Create initial cloud backup
            if (updated.userId.isNotBlank()) {
                cloudSyncEngine.performCloudBackup(updated.userId, updated.email, database)
            }

            withContext(Dispatchers.Main) {
                onDone()
            }
        }
    }

    fun syncCloudData(onDone: (com.example.service.sync.SyncResult) -> Unit = {}) {
        val session = sessionManager.currentSession.value ?: return
        if (_isSyncing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            _syncMessage.value = "Synchronizing with Cloud..."
            val result = cloudSyncEngine.performCloudBackup(session.userId, session.email, database)
            _isSyncing.value = false
            _syncMessage.value = result.message
            if (result.isSuccess) {
                sessionManager.updateSyncState(
                    com.example.data.auth.SyncState.SYNCED,
                    result.message,
                    result.timestamp
                )
                val now = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())
                _lastBackupTime.value = "Today at $now"
                _lastBackupSize.value = "${result.itemsSynced * 2} KB"
            }
            withContext(Dispatchers.Main) {
                onDone(result)
            }
        }
    }

    fun restoreFromCloud(onDone: (com.example.service.sync.SyncResult) -> Unit = {}) {
        val session = sessionManager.currentSession.value ?: return
        if (_isSyncing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            _syncMessage.value = "Restoring from Cloud Vault..."
            val result = cloudSyncEngine.restoreFromCloud(session.userId, database)
            _isSyncing.value = false
            _syncMessage.value = result.message
            withContext(Dispatchers.Main) {
                onDone(result)
            }
        }
    }

    fun signOut() {
        sessionManager.clearSession()
        _isGoogleAccountConnected.value = false
        _connectedGoogleEmail.value = ""
    }

    private val prefs = application.getSharedPreferences("atri_nova_prefs", android.content.Context.MODE_PRIVATE)

    // App Settings (Centralized, persistent & application-wide)
    private val _appSettings: MutableStateFlow<AppSettings> = run {
        val savedJson = prefs.getString("pref_app_settings_json", null)
        val loaded = AppSettings.fromJsonString(savedJson)
        val isDark = prefs.getBoolean("pref_dark_mode", true)
        val initial = if (loaded != null) {
            loaded.copy(
                themeMode = if (prefs.contains("pref_dark_mode")) {
                    if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT
                } else loaded.themeMode,
                currencySymbol = prefs.getString("pref_currency_format", loaded.currencySymbol) ?: loaded.currencySymbol,
                dateFormat = prefs.getString("pref_date_format", loaded.dateFormat) ?: loaded.dateFormat,
                appLockEnabled = prefs.getBoolean("pref_app_lock", loaded.appLockEnabled),
                biometricAuthEnabled = prefs.getBoolean("pref_biometric", loaded.biometricAuthEnabled),
                enableNotifications = prefs.getBoolean("pref_push_notifications", loaded.enableNotifications),
                paymentDueReminders = prefs.getBoolean("pref_payment_reminders", loaded.paymentDueReminders),
                lowStockAlerts = prefs.getBoolean("pref_low_stock_alerts", loaded.lowStockAlerts),
                privacyModeMaskBalances = prefs.getBoolean("pref_privacy_mode", loaded.privacyModeMaskBalances),
                backupFrequency = prefs.getString("pref_backup_frequency", loaded.backupFrequency) ?: loaded.backupFrequency
            )
        } else {
            AppSettings(
                themeMode = if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT,
                currencySymbol = prefs.getString("pref_currency_format", "Rs.") ?: "Rs.",
                dateFormat = prefs.getString("pref_date_format", "DD/MM/YYYY") ?: "DD/MM/YYYY",
                appLockEnabled = prefs.getBoolean("pref_app_lock", false),
                biometricAuthEnabled = prefs.getBoolean("pref_biometric", true),
                enableNotifications = prefs.getBoolean("pref_push_notifications", true),
                paymentDueReminders = prefs.getBoolean("pref_payment_reminders", true),
                lowStockAlerts = prefs.getBoolean("pref_low_stock_alerts", true),
                privacyModeMaskBalances = prefs.getBoolean("pref_privacy_mode", false),
                backupFrequency = prefs.getString("pref_backup_frequency", "Daily (Midnight)") ?: "Daily (Midnight)"
            )
        }
        MutableStateFlow(initial)
    }
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    // App Settings Legacy Preferences StateFlows for backward compatibility
    private val _isDarkMode = MutableStateFlow(
        _appSettings.value.themeMode == AppThemeMode.DARK || _appSettings.value.themeMode == AppThemeMode.SYSTEM
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currencyFormat = MutableStateFlow(_appSettings.value.currencySymbol)
    val currencyFormat: StateFlow<String> = _currencyFormat.asStateFlow()

    private val _dateFormat = MutableStateFlow(_appSettings.value.dateFormat)
    val dateFormat: StateFlow<String> = _dateFormat.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(_appSettings.value.appLockEnabled)
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _biometricAuthEnabled = MutableStateFlow(_appSettings.value.biometricAuthEnabled)
    val biometricAuthEnabled: StateFlow<Boolean> = _biometricAuthEnabled.asStateFlow()

    private val _pushNotificationsEnabled = MutableStateFlow(_appSettings.value.enableNotifications)
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()

    private val _paymentReminderAlerts = MutableStateFlow(_appSettings.value.paymentDueReminders)
    val paymentReminderAlerts: StateFlow<Boolean> = _paymentReminderAlerts.asStateFlow()

    private val _lowStockAlerts = MutableStateFlow(_appSettings.value.lowStockAlerts)
    val lowStockAlerts: StateFlow<Boolean> = _lowStockAlerts.asStateFlow()

    // Transaction Settings (Centralized, persistent & business-preset aware)
    private val _transactionSettings: MutableStateFlow<TransactionSettings> = run {
        val savedJson = prefs.getString("pref_transaction_settings_json", null)
        val loaded = TransactionSettings.fromJsonString(savedJson)
        val initial = if (loaded != null) {
            loaded.copy(
                defaultPaymentMode = prefs.getString("pref_default_payment_mode", loaded.defaultPaymentMode) ?: loaded.defaultPaymentMode,
                salesInvoicePrefix = prefs.getString("pref_sales_prefix", loaded.salesInvoicePrefix) ?: loaded.salesInvoicePrefix,
                showRoundOff = prefs.getBoolean("pref_auto_round_off", loaded.showRoundOff)
            )
        } else {
            val defaultPreset = BusinessTransactionPreset.GENERAL
            TransactionSettings.createDefaultsFor(defaultPreset).copy(
                defaultPaymentMode = prefs.getString("pref_default_payment_mode", "Cash") ?: "Cash",
                salesInvoicePrefix = prefs.getString("pref_sales_prefix", "INV-") ?: "INV-",
                showRoundOff = prefs.getBoolean("pref_auto_round_off", true)
            )
        }
        MutableStateFlow(initial)
    }
    val transactionSettings: StateFlow<TransactionSettings> = _transactionSettings.asStateFlow()

    private val _defaultPaymentMode = MutableStateFlow(_transactionSettings.value.defaultPaymentMode)
    val defaultPaymentMode: StateFlow<String> = _defaultPaymentMode.asStateFlow()

    private val _receiptPaperSize = MutableStateFlow(prefs.getString("pref_receipt_paper_size", "80mm Thermal") ?: "80mm Thermal")
    val receiptPaperSize: StateFlow<String> = _receiptPaperSize.asStateFlow()

    private val _salesPrefix = MutableStateFlow(_transactionSettings.value.salesInvoicePrefix)
    val salesPrefix: StateFlow<String> = _salesPrefix.asStateFlow()

    private val _autoPrintOnSave = MutableStateFlow(prefs.getBoolean("pref_auto_print", false))
    val autoPrintOnSave: StateFlow<Boolean> = _autoPrintOnSave.asStateFlow()

    private val _autoRoundOff = MutableStateFlow(_transactionSettings.value.showRoundOff)
    val autoRoundOff: StateFlow<Boolean> = _autoRoundOff.asStateFlow()

    // Invoice Settings (Centralized, persistent & business-preset aware)
    private val _invoiceSettings: MutableStateFlow<InvoiceSettings> = run {
        val savedJson = prefs.getString("pref_invoice_settings_json", null)
        val loaded = InvoiceSettings.fromJsonString(savedJson)
        val initial = if (loaded != null) {
            loaded.copy(
                enableVat = prefs.getBoolean("pref_tax_enabled", loaded.enableVat),
                defaultVatRate = prefs.getFloat("pref_tax_rate", loaded.defaultVatRate.toFloat()).toDouble(),
                panVatNumber = prefs.getString("pref_pan_vat_number", loaded.panVatNumber) ?: loaded.panVatNumber,
                defaultTerms = prefs.getString("pref_invoice_terms", loaded.defaultTerms) ?: loaded.defaultTerms,
                customInvoiceTitle = prefs.getString("pref_invoice_header", loaded.customInvoiceTitle) ?: loaded.customInvoiceTitle,
                footerMessage = prefs.getString("pref_invoice_footer", loaded.footerMessage) ?: loaded.footerMessage,
                showBusinessLogo = prefs.getBoolean("pref_show_logo_invoice", loaded.showBusinessLogo),
                showAuthorizedSignatureBox = prefs.getBoolean("pref_show_signature_line", loaded.showAuthorizedSignatureBox),
                defaultPaperSize = prefs.getString("pref_receipt_paper_size", loaded.defaultPaperSize) ?: loaded.defaultPaperSize,
                autoPrintOnSave = prefs.getBoolean("pref_auto_print", loaded.autoPrintOnSave),
                invoicePrefix = prefs.getString("pref_sales_prefix", loaded.invoicePrefix) ?: loaded.invoicePrefix
            )
        } else {
            val defaultPreset = BusinessInvoicePreset.GENERAL
            InvoiceSettings.createDefaultsFor(defaultPreset).copy(
                enableVat = prefs.getBoolean("pref_tax_enabled", true),
                defaultVatRate = prefs.getFloat("pref_tax_rate", 13.0f).toDouble(),
                panVatNumber = prefs.getString("pref_pan_vat_number", "609823415") ?: "609823415",
                defaultTerms = prefs.getString("pref_invoice_terms", "1. Goods once sold cannot be returned without original receipt.\n2. Payment is due within agreed terms.") ?: "1. Goods once sold cannot be returned without original receipt.\n2. Payment is due within agreed terms.",
                customInvoiceTitle = prefs.getString("pref_invoice_header", "TAX INVOICE") ?: "TAX INVOICE",
                footerMessage = prefs.getString("pref_invoice_footer", "Thank you for choosing Atri Khata! Visit again.") ?: "Thank you for choosing Atri Khata! Visit again.",
                showBusinessLogo = prefs.getBoolean("pref_show_logo_invoice", true),
                showAuthorizedSignatureBox = prefs.getBoolean("pref_show_signature_line", true),
                defaultPaperSize = prefs.getString("pref_receipt_paper_size", "A4 (Standard)") ?: "A4 (Standard)",
                autoPrintOnSave = prefs.getBoolean("pref_auto_print", false),
                invoicePrefix = prefs.getString("pref_sales_prefix", "INV-") ?: "INV-"
            )
        }
        MutableStateFlow(initial)
    }
    val invoiceSettings: StateFlow<InvoiceSettings> = _invoiceSettings.asStateFlow()

    private val _taxEnabled = MutableStateFlow(_invoiceSettings.value.enableVat)
    val taxEnabled: StateFlow<Boolean> = _taxEnabled.asStateFlow()

    private val _taxRate = MutableStateFlow(_invoiceSettings.value.defaultVatRate)
    val taxRate: StateFlow<Double> = _taxRate.asStateFlow()

    private val _panVatNumber = MutableStateFlow(_invoiceSettings.value.panVatNumber)
    val panVatNumber: StateFlow<String> = _panVatNumber.asStateFlow()

    private val _invoiceTerms = MutableStateFlow(_invoiceSettings.value.defaultTerms)
    val invoiceTerms: StateFlow<String> = _invoiceTerms.asStateFlow()

    private val _invoiceHeader = MutableStateFlow(_invoiceSettings.value.customInvoiceTitle)
    val invoiceHeader: StateFlow<String> = _invoiceHeader.asStateFlow()

    private val _invoiceFooter = MutableStateFlow(_invoiceSettings.value.footerMessage)
    val invoiceFooter: StateFlow<String> = _invoiceFooter.asStateFlow()

    private val _showLogoOnInvoice = MutableStateFlow(_invoiceSettings.value.showBusinessLogo)
    val showLogoOnInvoice: StateFlow<Boolean> = _showLogoOnInvoice.asStateFlow()

    private val _showSignatureLine = MutableStateFlow(_invoiceSettings.value.showAuthorizedSignatureBox)
    val showSignatureLine: StateFlow<Boolean> = _showSignatureLine.asStateFlow()

    // Party Settings (Centralized, persistent & business-preset aware)
    private val _partySettings: MutableStateFlow<PartySettings> = run {
        val savedJson = prefs.getString("pref_party_settings_json", null)
        val loaded = PartySettings.fromJsonString(savedJson)
        val initial = if (loaded != null) {
            loaded.copy(
                defaultCreditLimit = prefs.getFloat("pref_default_credit_limit", loaded.defaultCreditLimit.toFloat()).toDouble(),
                warnCreditLimitExceeded = prefs.getBoolean("pref_warn_credit_limit", loaded.warnCreditLimitExceeded),
                defaultCustomerCategory = prefs.getString("pref_default_party_category", loaded.defaultCustomerCategory) ?: loaded.defaultCustomerCategory,
                defaultPaymentTermDays = prefs.getInt("pref_payment_term_days", loaded.defaultPaymentTermDays)
            )
        } else {
            val defaultPreset = BusinessPartyPreset.GENERAL
            PartySettings.createDefaultsFor(defaultPreset).copy(
                defaultCreditLimit = prefs.getFloat("pref_default_credit_limit", 50000.0f).toDouble(),
                warnCreditLimitExceeded = prefs.getBoolean("pref_warn_credit_limit", true),
                defaultCustomerCategory = prefs.getString("pref_default_party_category", "Retailer") ?: "Retailer",
                defaultPaymentTermDays = prefs.getInt("pref_payment_term_days", 30)
            )
        }
        MutableStateFlow(initial)
    }
    val partySettings: StateFlow<PartySettings> = _partySettings.asStateFlow()

    private val _defaultCreditLimit = MutableStateFlow(_partySettings.value.defaultCreditLimit)
    val defaultCreditLimit: StateFlow<Double> = _defaultCreditLimit.asStateFlow()

    private val _warnCreditLimitExceeded = MutableStateFlow(_partySettings.value.warnCreditLimitExceeded)
    val warnCreditLimitExceeded: StateFlow<Boolean> = _warnCreditLimitExceeded.asStateFlow()

    private val _defaultPartyCategory = MutableStateFlow(_partySettings.value.defaultCustomerCategory)
    val defaultPartyCategory: StateFlow<String> = _defaultPartyCategory.asStateFlow()

    private val _partySortPreference = MutableStateFlow(prefs.getString("pref_party_sort", "Name (A-Z)") ?: "Name (A-Z)")
    val partySortPreference: StateFlow<String> = _partySortPreference.asStateFlow()

    private val _defaultPaymentTermDays = MutableStateFlow(_partySettings.value.defaultPaymentTermDays)
    val defaultPaymentTermDays: StateFlow<Int> = _defaultPaymentTermDays.asStateFlow()

    // Inventory Settings (Centralized, persistent & business-type aware)
    private val _inventorySettings: MutableStateFlow<InventorySettings> = run {
        val savedJson = prefs.getString("pref_inventory_settings_json", null)
        val loaded = InventorySettings.fromJsonString(savedJson)
        val initial = if (loaded != null) {
            loaded.copy(
                defaultLowStockThreshold = prefs.getInt("pref_low_stock_threshold", loaded.defaultLowStockThreshold),
                allowNegativeStock = prefs.getBoolean("pref_allow_negative_stock", loaded.allowNegativeStock),
                defaultMeasurementUnit = prefs.getString("pref_measurement_unit", loaded.defaultMeasurementUnit) ?: loaded.defaultMeasurementUnit,
                lowStockAlertEnabled = prefs.getBoolean("pref_low_stock_alerts", loaded.lowStockAlertEnabled)
            )
        } else {
            val defaultPreset = BusinessInventoryPreset.RETAIL
            InventorySettings.createDefaultsFor(defaultPreset).copy(
                defaultLowStockThreshold = prefs.getInt("pref_low_stock_threshold", 10),
                allowNegativeStock = prefs.getBoolean("pref_allow_negative_stock", false),
                defaultMeasurementUnit = prefs.getString("pref_measurement_unit", "Pcs") ?: "Pcs",
                lowStockAlertEnabled = prefs.getBoolean("pref_low_stock_alerts", true)
            )
        }
        MutableStateFlow(initial)
    }
    val inventorySettings: StateFlow<InventorySettings> = _inventorySettings.asStateFlow()

    private val _defaultLowStockThreshold = MutableStateFlow(_inventorySettings.value.defaultLowStockThreshold)
    val defaultLowStockThreshold: StateFlow<Int> = _defaultLowStockThreshold.asStateFlow()

    private val _barcodeBeepSound = MutableStateFlow(prefs.getBoolean("pref_barcode_beep", true))
    val barcodeBeepSound: StateFlow<Boolean> = _barcodeBeepSound.asStateFlow()

    private val _defaultMeasurementUnit = MutableStateFlow(_inventorySettings.value.defaultMeasurementUnit)
    val defaultMeasurementUnit: StateFlow<String> = _defaultMeasurementUnit.asStateFlow()

    private val _allowNegativeStock = MutableStateFlow(_inventorySettings.value.allowNegativeStock)
    val allowNegativeStock: StateFlow<Boolean> = _allowNegativeStock.asStateFlow()

    private val _continuousScanMode = MutableStateFlow(prefs.getBoolean("pref_continuous_scan", false))
    val continuousScanMode: StateFlow<Boolean> = _continuousScanMode.asStateFlow()

    fun updateAppSettings(update: (AppSettings) -> AppSettings) {
        val current = _appSettings.value
        val updated = update(current)
        _appSettings.value = updated

        // Sync individual legacy flows & SharedPreferences keys for 100% backward compatibility
        val isDark = updated.themeMode == AppThemeMode.DARK || (updated.themeMode == AppThemeMode.SYSTEM)
        _isDarkMode.value = isDark
        _currencyFormat.value = updated.currencySymbol
        _dateFormat.value = updated.dateFormat
        _appLockEnabled.value = updated.appLockEnabled
        _biometricAuthEnabled.value = updated.biometricAuthEnabled
        _pushNotificationsEnabled.value = updated.enableNotifications
        _paymentReminderAlerts.value = updated.paymentDueReminders
        _lowStockAlerts.value = updated.lowStockAlerts
        _backupFrequency.value = updated.backupFrequency

        prefs.edit()
            .putString("pref_app_settings_json", updated.toJsonString())
            .putBoolean("pref_dark_mode", isDark)
            .putString("pref_currency_format", updated.currencySymbol)
            .putString("pref_date_format", updated.dateFormat)
            .putBoolean("pref_app_lock", updated.appLockEnabled)
            .putBoolean("pref_biometric", updated.biometricAuthEnabled)
            .putBoolean("pref_push_notifications", updated.enableNotifications)
            .putBoolean("pref_payment_reminders", updated.paymentDueReminders)
            .putBoolean("pref_low_stock_alerts", updated.lowStockAlerts)
            .putBoolean("pref_privacy_mode", updated.privacyModeMaskBalances)
            .putString("pref_backup_frequency", updated.backupFrequency)
            .apply()
    }

    fun resetAppSettingsToDefault() {
        updateAppSettings { AppSettings() }
    }

    fun toggleDarkMode(enabled: Boolean) {
        updateAppSettings { it.copy(themeMode = if (enabled) AppThemeMode.DARK else AppThemeMode.LIGHT) }
    }

    fun setCurrencyFormat(format: String) {
        updateAppSettings { it.copy(currencySymbol = format) }
    }

    fun setDateFormat(format: String) {
        updateAppSettings { it.copy(dateFormat = format) }
    }

    fun toggleAppLock(enabled: Boolean) {
        updateAppSettings { it.copy(appLockEnabled = enabled) }
    }

    fun toggleBiometricAuth(enabled: Boolean) {
        updateAppSettings { it.copy(biometricAuthEnabled = enabled) }
    }

    fun togglePushNotifications(enabled: Boolean) {
        updateAppSettings { it.copy(enableNotifications = enabled) }
    }

    fun togglePaymentReminderAlerts(enabled: Boolean) {
        updateAppSettings { it.copy(paymentDueReminders = enabled) }
    }

    fun toggleLowStockAlerts(enabled: Boolean) {
        updateAppSettings { it.copy(lowStockAlerts = enabled) }
    }

    fun setDefaultPaymentMode(mode: String) {
        updateTransactionSettings { it.copy(defaultPaymentMode = mode) }
    }

    fun setReceiptPaperSize(size: String) {
        _receiptPaperSize.value = size
        prefs.edit().putString("pref_receipt_paper_size", size).apply()
    }

    fun setSalesPrefix(prefix: String) {
        updateTransactionSettings { it.copy(salesInvoicePrefix = prefix) }
    }

    fun toggleAutoPrintOnSave(enabled: Boolean) {
        _autoPrintOnSave.value = enabled
        prefs.edit().putBoolean("pref_auto_print", enabled).apply()
    }

    fun toggleAutoRoundOff(enabled: Boolean) {
        updateTransactionSettings { it.copy(showRoundOff = enabled) }
    }

    fun toggleTaxEnabled(enabled: Boolean) {
        updateInvoiceSettings { it.copy(enableVat = enabled) }
    }

    fun setTaxRate(rate: Double) {
        updateInvoiceSettings { it.copy(defaultVatRate = rate) }
    }

    fun setPanVatNumber(pan: String) {
        updateInvoiceSettings { it.copy(panVatNumber = pan) }
    }

    fun setInvoiceTerms(terms: String) {
        updateInvoiceSettings { it.copy(defaultTerms = terms) }
    }

    fun setInvoiceHeader(header: String) {
        updateInvoiceSettings { it.copy(customInvoiceTitle = header) }
    }

    fun setInvoiceFooter(footer: String) {
        updateInvoiceSettings { it.copy(footerMessage = footer) }
    }

    fun toggleShowLogoOnInvoice(show: Boolean) {
        updateInvoiceSettings { it.copy(showBusinessLogo = show) }
    }

    fun toggleShowSignatureLine(show: Boolean) {
        updateInvoiceSettings { it.copy(showAuthorizedSignatureBox = show) }
    }

    fun updatePartySettings(update: (PartySettings) -> PartySettings) {
        val current = _partySettings.value
        val updated = update(current)
        _partySettings.value = updated

        // Sync individual flows and prefs
        _defaultCreditLimit.value = updated.defaultCreditLimit
        _warnCreditLimitExceeded.value = updated.warnCreditLimitExceeded
        _defaultPartyCategory.value = updated.defaultCustomerCategory
        _defaultPaymentTermDays.value = updated.defaultPaymentTermDays

        prefs.edit()
            .putString("pref_party_settings_json", updated.toJsonString())
            .putFloat("pref_default_credit_limit", updated.defaultCreditLimit.toFloat())
            .putBoolean("pref_warn_credit_limit", updated.warnCreditLimitExceeded)
            .putString("pref_default_party_category", updated.defaultCustomerCategory)
            .putInt("pref_payment_term_days", updated.defaultPaymentTermDays)
            .apply()
    }

    fun applyBusinessPartyPreset(preset: BusinessPartyPreset) {
        val defaults = PartySettings.createDefaultsFor(preset)
        updatePartySettings { defaults }
    }

    fun setDefaultCreditLimit(limit: Double) {
        updatePartySettings { it.copy(defaultCreditLimit = limit) }
    }

    fun toggleWarnCreditLimitExceeded(warn: Boolean) {
        updatePartySettings { it.copy(warnCreditLimitExceeded = warn) }
    }

    fun setDefaultPartyCategory(category: String) {
        updatePartySettings { it.copy(defaultCustomerCategory = category) }
    }

    fun setPartySortPreference(sort: String) {
        _partySortPreference.value = sort
        prefs.edit().putString("pref_party_sort", sort).apply()
        val sortOpt = PartySortOption.fromDisplayName(sort)
        updatePartySettings { it.copy(defaultSortOption = sortOpt) }
    }

    fun setDefaultPaymentTermDays(days: Int) {
        updatePartySettings { it.copy(defaultPaymentTermDays = days) }
    }

    fun updateInventorySettings(update: (InventorySettings) -> InventorySettings) {
        val newSettings = update(_inventorySettings.value)
        _inventorySettings.value = newSettings
        prefs.edit().putString("pref_inventory_settings_json", newSettings.toJsonString()).apply()

        if (_defaultLowStockThreshold.value != newSettings.defaultLowStockThreshold) {
            _defaultLowStockThreshold.value = newSettings.defaultLowStockThreshold
            prefs.edit().putInt("pref_low_stock_threshold", newSettings.defaultLowStockThreshold).apply()
        }
        if (_allowNegativeStock.value != newSettings.allowNegativeStock) {
            _allowNegativeStock.value = newSettings.allowNegativeStock
            prefs.edit().putBoolean("pref_allow_negative_stock", newSettings.allowNegativeStock).apply()
        }
        if (_defaultMeasurementUnit.value != newSettings.defaultMeasurementUnit) {
            _defaultMeasurementUnit.value = newSettings.defaultMeasurementUnit
            prefs.edit().putString("pref_measurement_unit", newSettings.defaultMeasurementUnit).apply()
        }
        if (_lowStockAlerts.value != newSettings.lowStockAlertEnabled) {
            _lowStockAlerts.value = newSettings.lowStockAlertEnabled
            prefs.edit().putBoolean("pref_low_stock_alerts", newSettings.lowStockAlertEnabled).apply()
        }
    }

    fun applyBusinessInventoryPreset(preset: BusinessInventoryPreset) {
        val defaults = InventorySettings.createDefaultsFor(preset)
        updateInventorySettings { defaults }
    }

    fun updateTransactionSettings(update: (TransactionSettings) -> TransactionSettings) {
        val newSettings = update(_transactionSettings.value)
        _transactionSettings.value = newSettings
        prefs.edit().putString("pref_transaction_settings_json", newSettings.toJsonString()).apply()

        if (_defaultPaymentMode.value != newSettings.defaultPaymentMode) {
            _defaultPaymentMode.value = newSettings.defaultPaymentMode
            prefs.edit().putString("pref_default_payment_mode", newSettings.defaultPaymentMode).apply()
        }
        if (_salesPrefix.value != newSettings.salesInvoicePrefix) {
            _salesPrefix.value = newSettings.salesInvoicePrefix
            prefs.edit().putString("pref_sales_prefix", newSettings.salesInvoicePrefix).apply()
        }
        if (_autoRoundOff.value != newSettings.showRoundOff) {
            _autoRoundOff.value = newSettings.showRoundOff
            prefs.edit().putBoolean("pref_auto_round_off", newSettings.showRoundOff).apply()
        }
    }

    fun applyBusinessTransactionPreset(preset: BusinessTransactionPreset) {
        val defaults = TransactionSettings.createDefaultsFor(preset)
        updateTransactionSettings { defaults }
    }

    fun updateInvoiceSettings(update: (InvoiceSettings) -> InvoiceSettings) {
        val newSettings = update(_invoiceSettings.value)
        _invoiceSettings.value = newSettings
        prefs.edit().putString("pref_invoice_settings_json", newSettings.toJsonString()).apply()

        // Synchronize legacy individual StateFlows and SharedPreferences
        if (_taxEnabled.value != newSettings.enableVat) {
            _taxEnabled.value = newSettings.enableVat
            prefs.edit().putBoolean("pref_tax_enabled", newSettings.enableVat).apply()
        }
        if (_taxRate.value != newSettings.defaultVatRate) {
            _taxRate.value = newSettings.defaultVatRate
            prefs.edit().putFloat("pref_tax_rate", newSettings.defaultVatRate.toFloat()).apply()
        }
        if (_panVatNumber.value != newSettings.panVatNumber) {
            _panVatNumber.value = newSettings.panVatNumber
            prefs.edit().putString("pref_pan_vat_number", newSettings.panVatNumber).apply()
        }
        if (_invoiceTerms.value != newSettings.defaultTerms) {
            _invoiceTerms.value = newSettings.defaultTerms
            prefs.edit().putString("pref_invoice_terms", newSettings.defaultTerms).apply()
        }
        if (_invoiceHeader.value != newSettings.customInvoiceTitle) {
            _invoiceHeader.value = newSettings.customInvoiceTitle
            prefs.edit().putString("pref_invoice_header", newSettings.customInvoiceTitle).apply()
        }
        if (_invoiceFooter.value != newSettings.footerMessage) {
            _invoiceFooter.value = newSettings.footerMessage
            prefs.edit().putString("pref_invoice_footer", newSettings.footerMessage).apply()
        }
        if (_showLogoOnInvoice.value != newSettings.showBusinessLogo) {
            _showLogoOnInvoice.value = newSettings.showBusinessLogo
            prefs.edit().putBoolean("pref_show_logo_invoice", newSettings.showBusinessLogo).apply()
        }
        if (_showSignatureLine.value != newSettings.showAuthorizedSignatureBox) {
            _showSignatureLine.value = newSettings.showAuthorizedSignatureBox
            prefs.edit().putBoolean("pref_show_signature_line", newSettings.showAuthorizedSignatureBox).apply()
        }
        if (_receiptPaperSize.value != newSettings.defaultPaperSize) {
            _receiptPaperSize.value = newSettings.defaultPaperSize
            prefs.edit().putString("pref_receipt_paper_size", newSettings.defaultPaperSize).apply()
        }
        if (_autoPrintOnSave.value != newSettings.autoPrintOnSave) {
            _autoPrintOnSave.value = newSettings.autoPrintOnSave
            prefs.edit().putBoolean("pref_auto_print", newSettings.autoPrintOnSave).apply()
        }
        if (_salesPrefix.value != newSettings.invoicePrefix) {
            _salesPrefix.value = newSettings.invoicePrefix
            prefs.edit().putString("pref_sales_prefix", newSettings.invoicePrefix).apply()
            updateTransactionSettings { it.copy(salesInvoicePrefix = newSettings.invoicePrefix) }
        }
    }

    fun applyBusinessInvoicePreset(preset: BusinessInvoicePreset) {
        val defaults = InvoiceSettings.createDefaultsFor(preset)
        updateInvoiceSettings { defaults }
    }

    fun updateBusinessProfile(profile: com.example.data.local.entity.BusinessProfileEntity) {
        viewModelScope.launch {
            repository.updateBusinessProfile(profile)
            updateInvoiceSettings { s ->
                s.copy(
                    businessName = profile.businessName,
                    panVatNumber = profile.panVatNumber,
                    phone = profile.phone,
                    email = profile.email,
                    address = profile.address,
                    currencySymbol = profile.currencySymbol,
                    defaultTerms = profile.invoiceTerms,
                    customInvoiceTitle = profile.invoiceHeader,
                    footerMessage = profile.invoiceFooter,
                    defaultVatRate = profile.defaultVatRate,
                    enableVat = profile.isVatEnabled
                )
            }
        }
    }

    fun setDefaultLowStockThreshold(threshold: Int) {
        updateInventorySettings { it.copy(defaultLowStockThreshold = threshold) }
    }

    fun toggleBarcodeBeepSound(enabled: Boolean) {
        _barcodeBeepSound.value = enabled
        prefs.edit().putBoolean("pref_barcode_beep", enabled).apply()
    }

    fun setDefaultMeasurementUnit(unit: String) {
        updateInventorySettings { it.copy(defaultMeasurementUnit = unit) }
    }

    fun toggleAllowNegativeStock(allow: Boolean) {
        updateInventorySettings { it.copy(allowNegativeStock = allow) }
    }

    fun toggleContinuousScanMode(enabled: Boolean) {
        _continuousScanMode.value = enabled
        prefs.edit().putBoolean("pref_continuous_scan", enabled).apply()
    }

    fun formatCurrency(amount: Double): String {
        val symbol = _currencyFormat.value
        if (_privacyMode.value) {
            return "$symbol •••••"
        }
        val formatter = DecimalFormat("#,##0.00")
        val formatted = formatter.format(amount)
        return if (formatted.endsWith(".00")) {
            val intFormatter = DecimalFormat("#,##0")
            "$symbol ${intFormatter.format(amount)}"
        } else {
            "$symbol $formatted"
        }
    }
}
