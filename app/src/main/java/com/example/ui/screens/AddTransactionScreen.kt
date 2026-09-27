package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.TransactionEntity
import com.example.ui.AccountingViewModel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.util.NepaliDateUtils
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

/**
 * Transaction Type Mode
 */
enum class TransactionEntryType(
    val title: String,
    val subtitle: String,
    val accountingTerm: String,
    val primaryColor: Color,
    val icon: ImageVector,
    val defaultCategory: String
) {
    DEBIT(
        title = "Debit (Dr)",
        subtitle = "Money Out / Expense / Asset Increase",
        accountingTerm = "Debit Entry",
        primaryColor = Color(0xFFEF4444), // Coral Red
        icon = Icons.Default.ArrowUpward,
        defaultCategory = "Operating Expense"
    ),
    CREDIT(
        title = "Credit (Cr)",
        subtitle = "Money In / Income / Liability Increase",
        accountingTerm = "Credit Entry",
        primaryColor = Color(0xFF10B981), // Emerald Green
        icon = Icons.Default.ArrowDownward,
        defaultCategory = "Sales Income"
    )
}

/**
 * Modern Jetpack Compose Screen for adding new financial transactions.
 * Features dedicated fields for:
 * - Amount with quick increment chips and formatted previews
 * - Type (Debit vs Credit) segmented toggle with accounting rules
 * - Dual Date Picker (Nepali BS and Gregorian AD) with quick selector
 * - Particulars / Description with smart suggestion tags
 * - Chart of Accounts link & payment method selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSaveSuccess: (TransactionEntity) -> Unit = {},
    viewModel: AccountingViewModel = viewModel(),
    initialType: TransactionEntryType = TransactionEntryType.DEBIT,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // State bindings
    var entryType by remember { mutableStateOf(initialType) }
    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var partyNameInput by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf("Cash in Hand") }
    var selectedCategory by remember { mutableStateOf(initialType.defaultCategory) }
    var invoiceNumberInput by remember {
        mutableStateOf("TXN-${System.currentTimeMillis() % 1000000}")
    }

    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isSaving by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var showDualDatePickerDialog by remember { mutableStateOf(false) }

    // Computed dates
    val displayBsDate = remember(selectedDateMillis) {
        NepaliDateUtils.formatBsDate(selectedDateMillis)
    }
    val displayAdDate = remember(selectedDateMillis) {
        NepaliDateUtils.formatAdDate(selectedDateMillis)
    }

    // Chart of accounts from ViewModel
    val activeAccounts by viewModel.activeAccounts.collectAsStateWithLifecycle()

    // Preset quick suggestion chips
    val debitCategories = listOf(
        "Operating Expense", "Office Rent", "Utility Bill", "Vendor Payment",
        "Inventory Purchase", "Salaries & Wages", "Refreshment", "Asset Purchase"
    )
    val creditCategories = listOf(
        "Sales Income", "Customer Receipt", "Service Revenue", "Direct Deposit",
        "Owner Capital", "Refund Received", "Interest Income"
    )
    val activeCategories = if (entryType == TransactionEntryType.DEBIT) debitCategories else creditCategories

    val commonAccounts = listOf(
        "Cash in Hand", "Bank Account", "Fonepay / Online", "Cheque", "Accounts Payable"
    )

    // Calendar Picker Dialog
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                selectedDateMillis = cal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        containerColor = AppTheme.colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "New Financial Transaction",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "Record ${entryType.title} to Ledger",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            amountInput = ""
                            descriptionInput = ""
                            partyNameInput = ""
                            selectedDateMillis = System.currentTimeMillis()
                            amountError = null
                        },
                        modifier = Modifier.testTag("reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Form",
                            tint = AppTheme.colors.textMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // 1. Transaction Type Selector (Debit vs Credit)
            TransactionTypeSegmentedControl(
                selectedType = entryType,
                onTypeSelected = { newType ->
                    entryType = newType
                    selectedCategory = newType.defaultCategory
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Amount Input Hero Card
            AmountInputCard(
                amountInput = amountInput,
                onAmountChange = {
                    amountInput = it
                    amountError = null
                },
                entryType = entryType,
                errorMessage = amountError,
                onQuickAdd = { addVal ->
                    val current = amountInput.toDoubleOrNull() ?: 0.0
                    val updated = current + addVal
                    amountInput = if (updated % 1.0 == 0.0) updated.toLong().toString() else updated.toString()
                    amountError = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Dual Date Picker (Bikram Sambat BS & Gregorian AD)
            DualDatePickerCard(
                dateMillis = selectedDateMillis,
                displayBsDate = displayBsDate,
                displayAdDate = displayAdDate,
                onOpenDatePicker = { showDualDatePickerDialog = true },
                onSelectToday = { selectedDateMillis = System.currentTimeMillis() },
                onSelectYesterday = {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    selectedDateMillis = cal.timeInMillis
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Description & Narration Section
            DescriptionNarrationCard(
                description = descriptionInput,
                onDescriptionChange = { descriptionInput = it },
                activeCategories = activeCategories,
                selectedCategory = selectedCategory,
                onCategorySelect = { cat ->
                    selectedCategory = cat
                    if (descriptionInput.isBlank()) {
                        descriptionInput = cat
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Account & Party Section
            AccountPartyCard(
                selectedAccount = selectedAccount,
                onAccountSelect = { selectedAccount = it },
                commonAccounts = commonAccounts,
                availableAccounts = activeAccounts.map { it.accountName },
                partyName = partyNameInput,
                onPartyNameChange = { partyNameInput = it },
                invoiceNumber = invoiceNumberInput,
                onInvoiceNumberChange = { invoiceNumberInput = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Live Double-Entry Preview Card
            AccountingPreviewCard(
                entryType = entryType,
                amount = amountInput.toDoubleOrNull() ?: 0.0,
                selectedAccount = selectedAccount,
                category = selectedCategory,
                displayBsDate = displayBsDate,
                invoiceNumber = invoiceNumberInput
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 7. Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("cancel_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.colors.textPrimary
                    )
                }

                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            amountError = "Please enter a valid amount greater than 0"
                            return@Button
                        }

                        isSaving = true
                        focusManager.clearFocus()

                        // Map entryType to standard transaction type string
                        val transactionTypeString = if (entryType == TransactionEntryType.DEBIT) {
                            if (selectedCategory.contains("Purchase", ignoreCase = true)) "Purchase" else "Payment Out"
                        } else {
                            if (selectedCategory.contains("Sale", ignoreCase = true)) "Sales Invoice" else "Payment In"
                        }

                        val transaction = TransactionEntity(
                            partyName = partyNameInput.ifBlank { if (entryType == TransactionEntryType.DEBIT) "General Expense" else "General Customer" },
                            type = transactionTypeString,
                            amount = amount,
                            dateMillis = selectedDateMillis,
                            dateBs = displayBsDate,
                            dateAd = displayAdDate,
                            paymentMethod = selectedAccount,
                            invoiceNumber = invoiceNumberInput.ifBlank { "TXN-${System.currentTimeMillis() % 1000000}" },
                            notes = descriptionInput.ifBlank { selectedCategory },
                            status = "Completed",
                            account = selectedAccount,
                            category = selectedCategory
                        )

                        viewModel.recordTransaction(
                            transaction = transaction,
                            autoPostLedger = true
                        ) { newId ->
                            isSaving = false
                            Toast.makeText(context, "${entryType.accountingTerm} recorded successfully!", Toast.LENGTH_SHORT).show()
                            onSaveSuccess(transaction.copy(id = newId))
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .testTag("save_transaction_button"),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = entryType.primaryColor
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Save Transaction",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save ${entryType.title}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showDualDatePickerDialog) {
        com.example.ui.components.DualCalendarPickerDialog(
            initialDateMillis = selectedDateMillis,
            onDismissRequest = { showDualDatePickerDialog = false },
            onDateConfirmed = { millis, _, _ ->
                selectedDateMillis = millis
                showDualDatePickerDialog = false
            }
        )
    }
}

/**
 * 1. Transaction Type Segmented Control (Debit vs Credit)
 */
@Composable
private fun TransactionTypeSegmentedControl(
    selectedType: TransactionEntryType,
    onTypeSelected: (TransactionEntryType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("type_segmented_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionEntryType.entries.forEach { type ->
                val isSelected = selectedType == type
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) type.primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "TypeBg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) type.primaryColor else Color.Transparent,
                    animationSpec = tween(200),
                    label = "TypeBorder"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onTypeSelected(type) }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .testTag("type_${type.name.lowercase()}_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = type.title,
                                tint = if (isSelected) type.primaryColor else AppTheme.colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = type.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) type.primaryColor else AppTheme.colors.textPrimary
                            )
                        }
                        Text(
                            text = if (type == TransactionEntryType.DEBIT) "Expense / Outflow" else "Revenue / Inflow",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = if (isSelected) type.primaryColor.copy(alpha = 0.8f) else AppTheme.colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Amount Input Hero Card
 */
@Composable
private fun AmountInputCard(
    amountInput: String,
    onAmountChange: (String) -> Unit,
    entryType: TransactionEntryType,
    errorMessage: String?,
    onQuickAdd: (Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("amount_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(
            1.dp,
            if (errorMessage != null) AppTheme.colors.error else AppTheme.colors.cardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Amount",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textSecondary
                )
                Text(
                    text = "Currency: Rs. (NPR)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Input Field
            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    // Allow only digits and a single decimal point
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        onAmountChange(input)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                placeholder = {
                    Text(
                        text = "0.00",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppTheme.colors.textMuted.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Text(
                        text = "Rs.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = entryType.primaryColor,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                },
                trailingIcon = {
                    if (amountInput.isNotBlank()) {
                        IconButton(onClick = { onAmountChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Amount",
                                tint = AppTheme.colors.textMuted
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = entryType.primaryColor,
                    unfocusedBorderColor = AppTheme.colors.cardBorder,
                    focusedContainerColor = AppTheme.colors.cardBackground,
                    unfocusedContainerColor = AppTheme.colors.cardBackground
                )
            )

            // Validation error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.error,
                    modifier = Modifier.testTag("amount_error_text")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Add Chips
            Text(
                text = "Quick Presets:",
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(500.0, 1000.0, 5000.0, 10000.0, 50000.0).forEach { preset ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onQuickAdd(preset) }
                            .testTag("preset_${preset.toInt()}"),
                        shape = RoundedCornerShape(8.dp),
                        color = AppTheme.colors.cardBackground,
                        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                    ) {
                        Text(
                            text = "+Rs. ${NumberFormat.getNumberInstance(Locale.US).format(preset)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Dual Date Picker (Bikram Sambat BS & Gregorian AD)
 */
@Composable
private fun DualDatePickerCard(
    dateMillis: Long,
    displayBsDate: String,
    displayAdDate: String,
    onOpenDatePicker: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectYesterday: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("date_picker_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date Selection",
                        tint = SkyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Transaction Posting Date",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textSecondary
                    )
                }

                IconButton(
                    onClick = onOpenDatePicker,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("open_calendar_picker")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Pick Date",
                        tint = SkyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display dual dates in interactive cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.cardBackground)
                    .clickable { onOpenDatePicker() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bikram Sambat (BS)
                Column {
                    Text(
                        text = "Nepali BS Calendar",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textMuted
                    )
                    Text(
                        text = displayBsDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkyBlueBright,
                        modifier = Modifier.testTag("display_bs_date")
                    )
                }

                // Divider dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.textMuted.copy(alpha = 0.5f))
                )

                // Gregorian (AD)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "English AD Calendar",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textMuted
                    )
                    Text(
                        text = displayAdDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.testTag("display_ad_date")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick date selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectToday() }
                        .testTag("quick_date_today"),
                    shape = RoundedCornerShape(8.dp),
                    color = AppTheme.colors.cardBackground,
                    border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectYesterday() }
                        .testTag("quick_date_yesterday"),
                    shape = RoundedCornerShape(8.dp),
                    color = AppTheme.colors.cardBackground,
                    border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                ) {
                    Text(
                        text = "Yesterday",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenDatePicker() }
                        .testTag("quick_date_custom"),
                    shape = RoundedCornerShape(8.dp),
                    color = AppTheme.colors.cardBackground,
                    border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                ) {
                    Text(
                        text = "Custom Date...",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * 4. Description & Narration Card
 */
@Composable
private fun DescriptionNarrationCard(
    description: String,
    onDescriptionChange: (String) -> Unit,
    activeCategories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("description_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "Description",
                    tint = SkyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Description / Particulars",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input"),
                placeholder = {
                    Text(
                        text = "Enter transaction details, particulars, or narration...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textMuted
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBlue,
                    unfocusedBorderColor = AppTheme.colors.cardBorder,
                    focusedContainerColor = AppTheme.colors.cardBackground,
                    unfocusedContainerColor = AppTheme.colors.cardBackground
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Suggestion Pills
            Text(
                text = "Quick Particulars / Classification:",
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeCategories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SkyBlue.copy(alpha = 0.2f),
                            selectedLabelColor = SkyBlueBright
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SkyBlue else AppTheme.colors.cardBorder
                        )
                    )
                }
            }
        }
    }
}

/**
 * 5. Account & Party Section
 */
@Composable
private fun AccountPartyCard(
    selectedAccount: String,
    onAccountSelect: (String) -> Unit,
    commonAccounts: List<String>,
    availableAccounts: List<String>,
    partyName: String,
    onPartyNameChange: (String) -> Unit,
    invoiceNumber: String,
    onInvoiceNumberChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_party_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = "Account & Reference",
                    tint = SkyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Account & Reference",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account Selector Chips
            Text(
                text = "Payment / Contra Account:",
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val accountsToShow = if (availableAccounts.isNotEmpty()) availableAccounts.take(6) else commonAccounts
                accountsToShow.forEach { account ->
                    val isSelected = selectedAccount == account
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAccountSelect(account) },
                        label = {
                            Text(
                                text = account,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SkyBlue.copy(alpha = 0.2f),
                            selectedLabelColor = SkyBlueBright
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SkyBlue else AppTheme.colors.cardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Optional Party Name
            OutlinedTextField(
                value = partyName,
                onValueChange = onPartyNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_name_input"),
                label = { Text("Party / Contact Name (Optional)") },
                placeholder = { Text("e.g. Ramesh Suppliers, Sunita Traders, Walk-in") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Party Name",
                        tint = AppTheme.colors.textMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBlue,
                    unfocusedBorderColor = AppTheme.colors.cardBorder,
                    focusedContainerColor = AppTheme.colors.cardBackground,
                    unfocusedContainerColor = AppTheme.colors.cardBackground
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Voucher / Reference Number
            OutlinedTextField(
                value = invoiceNumber,
                onValueChange = onInvoiceNumberChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voucher_number_input"),
                label = { Text("Voucher / Ref Number") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Voucher Number",
                        tint = AppTheme.colors.textMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBlue,
                    unfocusedBorderColor = AppTheme.colors.cardBorder,
                    focusedContainerColor = AppTheme.colors.cardBackground,
                    unfocusedContainerColor = AppTheme.colors.cardBackground
                )
            )
        }
    }
}

/**
 * 6. Live Double-Entry Accounting Preview Card
 */
@Composable
private fun AccountingPreviewCard(
    entryType: TransactionEntryType,
    amount: Double,
    selectedAccount: String,
    category: String,
    displayBsDate: String,
    invoiceNumber: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("accounting_preview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Ledger Preview",
                    tint = SkyBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Double-Entry Ledger Posting Preview",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = AppTheme.colors.divider)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Voucher Mode:",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted
                )
                Text(
                    text = "${entryType.title} (${entryType.accountingTerm})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = entryType.primaryColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Primary Account:",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted
                )
                Text(
                    text = selectedAccount,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Classification:",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Posting Date (BS):",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted
                )
                Text(
                    text = displayBsDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = AppTheme.colors.divider)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Amount:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
                Text(
                    text = "Rs. ${NumberFormat.getNumberInstance(Locale.US).format(amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = entryType.primaryColor
                )
            }
        }
    }
}
