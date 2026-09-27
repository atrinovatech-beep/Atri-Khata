package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.dao.AccountBalanceSummary
import com.example.data.local.entity.AccountBalanceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountTimelinePoint
import com.example.data.model.FinancialHealthAudit
import com.example.data.model.TimelineMetricType
import com.example.data.model.TimelineTimeRange
import com.example.data.repository.AccountingRepository
import com.example.data.repository.FinancialSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for presenting accounting transactions and live account balances
 * to Jetpack Compose UI screens.
 *
 * Exposes reactive StateFlows for transactions, search/filter criteria, Chart of Accounts,
 * and comprehensive balance summaries.
 */
class AccountingViewModel(
    application: Application,
    private val repository: AccountingRepository
) : AndroidViewModel(application) {

    /**
     * Secondary convenience constructor for standard ViewModelProvider without custom factory.
     */
    constructor(application: Application) : this(
        application,
        createDefaultRepository(application)
    )

    // =========================================================================
    // UI Filtering & Search State
    // =========================================================================

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<String?>(null) // null for "All"
    val selectedTypeFilter: StateFlow<String?> = _selectedTypeFilter.asStateFlow()

    private val _selectedAccountFilter = MutableStateFlow<String?>(null)
    val selectedAccountFilter: StateFlow<String?> = _selectedAccountFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // =========================================================================
    // Transaction StateFlows
    // =========================================================================

    val rawTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.getRecentTransactions(15)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Filtered and searched transaction list matching user query, selected type, and account.
     */
    val transactions: StateFlow<List<TransactionEntity>> = combine(
        rawTransactions,
        _searchQuery,
        _selectedTypeFilter,
        _selectedAccountFilter
    ) { txList, query, typeFilter, accountFilter ->
        txList.filter { tx ->
            val matchesQuery = query.isBlank() ||
                    tx.partyName.contains(query, ignoreCase = true) ||
                    tx.invoiceNumber.contains(query, ignoreCase = true) ||
                    tx.notes.contains(query, ignoreCase = true) ||
                    tx.amount.toString().contains(query)

            val matchesType = typeFilter == null ||
                    tx.type.equals(typeFilter, ignoreCase = true)

            val matchesAccount = accountFilter == null ||
                    tx.account.equals(accountFilter, ignoreCase = true) ||
                    tx.paymentMethod.equals(accountFilter, ignoreCase = true)

            matchesQuery && matchesType && matchesAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSales: StateFlow<Double> = repository.totalSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPurchases: StateFlow<Double> = repository.totalPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPaymentIn: StateFlow<Double> = repository.totalPaymentIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPaymentOut: StateFlow<Double> = repository.totalPaymentOut
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // =========================================================================
    // Account Balances & Chart of Accounts StateFlows
    // =========================================================================

    val allAccounts: StateFlow<List<AccountBalanceEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccounts: StateFlow<List<AccountBalanceEntity>> = repository.activeAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountSummaries: StateFlow<List<AccountBalanceSummary>> = repository.accountSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAssets: StateFlow<Double> = repository.totalAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLiabilities: StateFlow<Double> = repository.totalLiabilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalEquity: StateFlow<Double> = repository.totalEquity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.totalExpense
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val liquidCashBalance: StateFlow<Double> = repository.liquidCashBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val accountsReceivable: StateFlow<Double> = repository.accountsReceivable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val accountsPayable: StateFlow<Double> = repository.accountsPayable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val financialSummary: StateFlow<FinancialSummary> = repository.financialSummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // =========================================================================
    // Balance Timeline & Financial Health (Recharts Integration)
    // =========================================================================

    private val _selectedMetricType = MutableStateFlow(TimelineMetricType.ASSETS_VS_LIABILITIES)
    val selectedMetricType: StateFlow<TimelineMetricType> = _selectedMetricType.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimelineTimeRange.ALL_TIME)
    val selectedTimeRange: StateFlow<TimelineTimeRange> = _selectedTimeRange.asStateFlow()

    private val _selectedTimelineAccount = MutableStateFlow("Cash in Hand")
    val selectedTimelineAccount: StateFlow<String> = _selectedTimelineAccount.asStateFlow()

    val balanceTimeline: StateFlow<List<AccountTimelinePoint>> = repository.balanceTimeline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialHealthAudit: StateFlow<FinancialHealthAudit?> = repository.financialHealthAudit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setChartMetricType(type: TimelineMetricType) {
        _selectedMetricType.value = type
    }

    fun setChartTimeRange(range: TimelineTimeRange) {
        _selectedTimeRange.value = range
    }

    fun setTimelineAccount(accountName: String) {
        _selectedTimelineAccount.value = accountName
    }

    // =========================================================================
    // Actions & Operations
    // =========================================================================

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: String?) {
        _selectedTypeFilter.value = if (type.equals("All", ignoreCase = true)) null else type
    }

    fun setAccountFilter(account: String?) {
        _selectedAccountFilter.value = if (account.equals("All", ignoreCase = true)) null else account
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedTypeFilter.value = null
        _selectedAccountFilter.value = null
    }

    fun recordTransaction(
        transaction: TransactionEntity,
        autoPostLedger: Boolean = true,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newId = repository.recordTransaction(transaction, autoPostLedger)
                _uiMessage.value = "Transaction #${transaction.invoiceNumber.ifBlank { newId.toString() }} recorded successfully"
                onComplete(newId)
            } catch (e: Exception) {
                _uiMessage.value = "Failed to record transaction: ${e.localizedMessage}"
            }
        }
    }

    fun updateTransaction(
        transaction: TransactionEntity,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateTransaction(transaction)
                _uiMessage.value = "Transaction updated"
                onComplete()
            } catch (e: Exception) {
                _uiMessage.value = "Failed to update transaction: ${e.localizedMessage}"
            }
        }
    }

    fun deleteTransaction(
        transaction: TransactionEntity,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteTransaction(transaction)
                _uiMessage.value = "Transaction removed"
                onComplete()
            } catch (e: Exception) {
                _uiMessage.value = "Failed to delete transaction: ${e.localizedMessage}"
            }
        }
    }

    fun createAccount(
        account: AccountBalanceEntity,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = repository.insertAccount(account)
                _uiMessage.value = "Account ${account.accountName} created"
                onComplete(id)
            } catch (e: Exception) {
                _uiMessage.value = "Failed to create account: ${e.localizedMessage}"
            }
        }
    }

    fun updateAccount(
        account: AccountBalanceEntity,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateAccount(account)
                _uiMessage.value = "Account ${account.accountName} updated"
                onComplete()
            } catch (e: Exception) {
                _uiMessage.value = "Failed to update account: ${e.localizedMessage}"
            }
        }
    }

    fun deleteAccount(
        account: AccountBalanceEntity,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteAccount(account)
                _uiMessage.value = "Account ${account.accountName} deleted"
                onComplete()
            } catch (e: Exception) {
                _uiMessage.value = "Failed to delete account: ${e.localizedMessage}"
            }
        }
    }

    fun syncAccountBalances(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            try {
                repository.syncBalancesWithLedger()
                _uiMessage.value = "Account balances synced with ledger"
            } catch (e: Exception) {
                _uiMessage.value = "Sync failed: ${e.localizedMessage}"
            } finally {
                _isRefreshing.value = false
                onComplete()
            }
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // =========================================================================
    // Factory
    // =========================================================================

    companion object {
        fun createDefaultRepository(application: Application): AccountingRepository {
            val db = AppDatabase.getDatabase(application, CoroutineScope(SupervisorJob() + Dispatchers.IO))
            return AccountingRepository(
                transactionDao = db.transactionDao(),
                accountBalanceDao = db.accountBalanceDao(),
                ledgerDao = db.ledgerDao()
            )
        }

        fun provideFactory(
            application: Application,
            repository: AccountingRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AccountingViewModel::class.java)) {
                    return AccountingViewModel(application, repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
