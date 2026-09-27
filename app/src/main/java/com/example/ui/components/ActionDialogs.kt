package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.ui.screens.inventory.AddEditInventoryItemSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PartyEntity
import com.example.ui.ActiveDialog
import com.example.ui.MainViewModel
import com.example.ui.screens.AdvancedStaffManagementSheet
import com.example.ui.screens.GoogleDriveBackupSheet
import com.example.ui.screens.SettingsSheet
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionDialogHost(
    viewModel: MainViewModel,
    activeDialog: ActiveDialog,
    parties: List<PartyEntity>,
    inventoryItems: List<InventoryItemEntity>
) {
    if (activeDialog == ActiveDialog.NONE) return

    if (activeDialog == ActiveDialog.SALES_INVOICE) {
        androidx.activity.compose.BackHandler {
            viewModel.closeDialog()
        }
        val transactionSettings by viewModel.transactionSettings.collectAsStateWithLifecycle()
        val invoiceSettings by viewModel.invoiceSettings.collectAsStateWithLifecycle()
        val inventorySettings by viewModel.inventorySettings.collectAsStateWithLifecycle()
        val partySettings by viewModel.partySettings.collectAsStateWithLifecycle()

        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.background
        ) {
            SalesInvoiceSheet(
                parties = parties,
                inventoryItems = inventoryItems,
                transactionSettings = transactionSettings,
                invoiceSettings = invoiceSettings,
                inventorySettings = inventorySettings,
                partySettings = partySettings,
                onSaveInvoice = { invoice, items, payments, isSaveAndNew, attachments ->
                    viewModel.createSalesInvoice(
                        invoice = invoice,
                        items = items,
                        payments = payments,
                        activities = emptyList(),
                        attachments = attachments
                    ) { _ ->
                        viewModel.addRewardCoins(15)
                        if (!isSaveAndNew) {
                            viewModel.closeDialog()
                        }
                    }
                },
                onQuickAddParty = { party ->
                    viewModel.addParty(party)
                },
                onCancel = { viewModel.closeDialog() }
            )
        }
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeDialog() },
        sheetState = sheetState,
        containerColor = AppTheme.colors.surface,
        tonalElevation = 8.dp
    ) {
        when (activeDialog) {
            ActiveDialog.QUICK_ENTRY -> QuickEntrySheet(
                onSave = { partyName, type, amount, note, dateMillis, dateBs, dateAd ->
                    viewModel.addTransaction(
                        partyName = partyName,
                        type = type,
                        amount = amount,
                        paymentMethod = "Cash",
                        invoiceNumber = "QE-${(1000..9999).random()}",
                        notes = note,
                        dateMillis = dateMillis,
                        dateBs = dateBs,
                        dateAd = dateAd
                    )
                    viewModel.closeDialog()
                },
                onCancel = { viewModel.closeDialog() }
            )

            ActiveDialog.QUICK_POS -> QuickPosSheet(
                inventoryItems = inventoryItems,
                onCheckout = { totalAmount, note ->
                    viewModel.addTransaction(
                        partyName = "Walk-in POS Customer",
                        type = "Sales Invoice",
                        amount = totalAmount,
                        paymentMethod = "Cash",
                        invoiceNumber = "POS-${(1000..9999).random()}",
                        notes = note
                    )
                    viewModel.addRewardCoins(25)
                    viewModel.closeDialog()
                },
                onCancel = { viewModel.closeDialog() }
            )

            ActiveDialog.ADD_PARTY -> AddPartySheet(
                onSaveParty = { party ->
                    viewModel.addParty(party)
                    viewModel.closeDialog()
                },
                onCancel = { viewModel.closeDialog() }
            )

            ActiveDialog.SALES_INVOICE -> {
                // Handled above as full-screen surface
            }

            ActiveDialog.PAYMENT_IN -> PaymentInSheet(
                parties = parties,
                onSave = { partyName, amount, depositAccount, recNum, note, dateMillis, dateBs, dateAd, isSaveAndNew ->
                    viewModel.addTransaction(
                        partyName = partyName,
                        type = "Payment In",
                        amount = amount,
                        paymentMethod = depositAccount,
                        invoiceNumber = recNum,
                        notes = note,
                        dateMillis = dateMillis,
                        dateBs = dateBs,
                        dateAd = dateAd
                    )
                    viewModel.addRewardCoins(15)
                    if (!isSaveAndNew) {
                        viewModel.closeDialog()
                    }
                },
                onCancel = { viewModel.closeDialog() }
            )

            ActiveDialog.PAYMENT_OUT -> PaymentOutSheet(
                parties = parties,
                onSave = { partyName, amount, mode, note, dateMillis, dateBs, dateAd ->
                    viewModel.addTransaction(
                        partyName = partyName,
                        type = "Payment Out",
                        amount = amount,
                        paymentMethod = mode,
                        invoiceNumber = "PAY-${(1000..9999).random()}",
                        notes = note,
                        dateMillis = dateMillis,
                        dateBs = dateBs,
                        dateAd = dateAd
                    )
                    viewModel.closeDialog()
                },
                onCancel = { viewModel.closeDialog() }
            )

            ActiveDialog.ADD_ITEM -> {
                val categories by viewModel.allInventoryCategories.collectAsStateWithLifecycle()
                val defaultUnit by viewModel.defaultMeasurementUnit.collectAsStateWithLifecycle()
                val defaultLowStock by viewModel.defaultLowStockThreshold.collectAsStateWithLifecycle()
                AddEditInventoryItemSheet(
                    existingCategories = categories,
                    defaultUnit = defaultUnit,
                    defaultLowStock = defaultLowStock.toDouble(),
                    onSave = { name, sku, category, qty, unit, pPrice, sPrice, minStock ->
                        viewModel.addInventoryItem(name, sku, category, qty, unit, pPrice, sPrice, minStock)
                        viewModel.closeDialog()
                    },
                    onDismiss = { viewModel.closeDialog() }
                )
            }

            ActiveDialog.CREDIT_REMINDER -> CreditReminderSheet(
                parties = parties.filter { it.balanceToReceive > 0 },
                onSendReminder = {
                    viewModel.closeDialog()
                },
                onCancel = { viewModel.closeDialog() }
            )

            ActiveDialog.VIEW_REPORTS -> ViewReportsSheet(
                viewModel = viewModel,
                onClose = { viewModel.closeDialog() }
            )

            ActiveDialog.BUSINESS_SWITCHER -> BusinessSwitcherSheet(
                currentBusiness = viewModel.selectedBusiness.value,
                onSelect = { viewModel.selectBusiness(it) },
                onCancel = { viewModel.closeDialog() }
            )

            ActiveDialog.NOTIFICATIONS -> NotificationsSheet(
                onClose = {
                    viewModel.markNotificationsRead()
                    viewModel.closeDialog()
                }
            )

            ActiveDialog.REWARDS_WALLET -> RewardsWalletSheet(
                coinBalance = viewModel.rewardCoins.value,
                onClose = { viewModel.closeDialog() }
            )

            ActiveDialog.EDIT_SHORTCUTS -> EditShortcutsSheet(
                onClose = { viewModel.closeDialog() }
            )

            ActiveDialog.MANAGE_STAFF -> AdvancedStaffManagementSheet(
                viewModel = viewModel,
                onClose = { viewModel.closeDialog() }
            )

            ActiveDialog.ACCOUNT_TRANSFER -> CashAndBankAccountsSheet(
                viewModel = viewModel,
                onClose = { viewModel.closeDialog() }
            )

            ActiveDialog.GOOGLE_DRIVE_BACKUP -> GoogleDriveBackupSheet(
                viewModel = viewModel,
                onClose = { viewModel.closeDialog() }
            )

            ActiveDialog.USER_ACCOUNT_SYNC -> UserAccountSyncSheet(
                viewModel = viewModel,
                onClose = { viewModel.closeDialog() },
                onSignOut = { viewModel.closeDialog() }
            )

            ActiveDialog.SETTINGS -> SettingsSheet(
                viewModel = viewModel,
                onClose = { viewModel.closeDialog() }
            )

            ActiveDialog.PURCHASE_INVOICE -> {
                val transactionSettings by viewModel.transactionSettings.collectAsStateWithLifecycle()
                val inventorySettings by viewModel.inventorySettings.collectAsStateWithLifecycle()
                val partySettings by viewModel.partySettings.collectAsStateWithLifecycle()
                val preselectedParty by viewModel.preselectedPartyForPurchase.collectAsStateWithLifecycle()

                PurchaseInvoiceSheet(
                    parties = parties,
                    inventoryItems = inventoryItems,
                    transactionSettings = transactionSettings,
                    inventorySettings = inventorySettings,
                    partySettings = partySettings,
                    preselectedParty = preselectedParty,
                    onSavePurchase = { purchaseNumber, party, customPartyName, lineItems, subtotal, discountAmount, taxableAmount, vatAmount, otherCharges, roundOff, grandTotal, paidAmount, paymentMethod, supplierBillNumber, referenceNumber, purchaseDateMillis, purchaseDateBs, purchaseDateAd, dueDateMillis, dueDateBs, purchaseType, warehouse, notes, attachmentUri, updateStockCost, isSaveAndNew ->
                        viewModel.recordPurchaseInvoice(
                            purchaseNumber = purchaseNumber,
                            party = party,
                            customPartyName = customPartyName,
                            lineItems = lineItems,
                            subtotal = subtotal,
                            discountAmount = discountAmount,
                            taxableAmount = taxableAmount,
                            vatAmount = vatAmount,
                            otherCharges = otherCharges,
                            roundOff = roundOff,
                            grandTotal = grandTotal,
                            paidAmount = paidAmount,
                            paymentMethod = paymentMethod,
                            supplierBillNumber = supplierBillNumber,
                            referenceNumber = referenceNumber,
                            purchaseDateMillis = purchaseDateMillis,
                            purchaseDateBs = purchaseDateBs,
                            purchaseDateAd = purchaseDateAd,
                            dueDateMillis = dueDateMillis,
                            dueDateBs = dueDateBs,
                            purchaseType = purchaseType,
                            warehouse = warehouse,
                            notes = notes,
                            attachmentUri = attachmentUri,
                            updateStockCost = updateStockCost,
                            onSuccess = {
                                if (!isSaveAndNew) {
                                    viewModel.closeDialog()
                                }
                            }
                        )
                    },
                    onCancel = { viewModel.closeDialog() },
                    onQuickAddParty = { newParty ->
                        viewModel.addParty(newParty)
                    }
                )
            }

            ActiveDialog.NONE -> Unit
        }
    }
}

// ----------------- SUB-SHEETS -----------------

@Composable
fun QuickEntrySheet(
    onSave: (String, String, Double, String, Long, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Sales Invoice") }
    var partyName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var selectedDateMillis by remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }
    var selectedDateBs by remember { mutableStateOf(com.example.util.NepaliDateUtils.formatBsDate(System.currentTimeMillis())) }
    var selectedDateAd by remember { mutableStateOf(com.example.util.NepaliDateUtils.formatAdDate(System.currentTimeMillis())) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Entry",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dual AD and BS Date Selector
        DualDateSelector(
            initialDateMillis = selectedDateMillis,
            onDateChanged = { millis, bs, ad ->
                selectedDateMillis = millis
                selectedDateBs = bs
                selectedDateAd = ad
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Type selector tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CardDark)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Sales Invoice", "Payment In", "Expense", "Purchase").forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) TealAccent else Color.Transparent)
                        .clickable { selectedType = type }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (type == "Sales Invoice") "Sale" else type,
                        color = if (isSelected) Color.Black else TextWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (Rs.)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = customTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_entry_amount_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = partyName,
            onValueChange = { partyName = it },
            label = { Text("Party / Client Name (Optional)") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Notes / Reference") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    onSave(
                        partyName.ifBlank { "Counter Entry" },
                        selectedType,
                        amount,
                        note,
                        selectedDateMillis,
                        selectedDateBs,
                        selectedDateAd
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("quick_entry_save_button")
        ) {
            Text("Save Entry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun QuickPosSheet(
    inventoryItems: List<InventoryItemEntity>,
    onCheckout: (Double, String) -> Unit,
    onCancel: () -> Unit
) {
    var cartItems by remember { mutableStateOf(mapOf<Long, Int>()) }
    var selectedItemQuery by remember { mutableStateOf("") }

    val filteredItems = remember(selectedItemQuery, inventoryItems) {
        if (selectedItemQuery.isBlank()) inventoryItems
        else inventoryItems.filter { it.name.contains(selectedItemQuery, ignoreCase = true) }
    }

    val totalAmount = cartItems.entries.sumOf { (itemId, qty) ->
        val item = inventoryItems.find { it.id == itemId }
        (item?.salePrice ?: 0.0) * qty
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quick POS Counter",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Instant Barcode & Billing terminal",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = selectedItemQuery,
            onValueChange = { selectedItemQuery = it },
            placeholder = { Text("Filter items or scan barcode...") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Select Products to Add:",
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(filteredItems) { item ->
                val qty = cartItems[item.id] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Rs. ${item.salePrice} / ${item.unit}", color = TealAccent, fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (qty > 0) {
                            IconButton(
                                onClick = {
                                    val current = cartItems[item.id] ?: 0
                                    if (current > 1) {
                                        cartItems = cartItems + (item.id to (current - 1))
                                    } else {
                                        cartItems = cartItems - item.id
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("-", color = TealAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = qty.toString(),
                                color = TextWhite,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val current = cartItems[item.id] ?: 0
                                cartItems = cartItems + (item.id to (current + 1))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = TealAccent)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total & Checkout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TealCardBg)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Payable", color = TextMuted, fontSize = 12.sp)
                Text("Rs. $totalAmount", color = TealAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (totalAmount > 0) {
                        onCheckout(totalAmount, "POS counter receipt: ${cartItems.size} items")
                    }
                },
                enabled = totalAmount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Charge Cash", fontWeight = FontWeight.Bold)
            }
        }
    }
}



@Composable
fun PaymentOutSheet(
    parties: List<PartyEntity>,
    onSave: (String, Double, String, String, Long, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var partyName by remember { mutableStateOf(parties.find { it.type == "Supplier" }?.name ?: "") }
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Bank Transfer") }
    var note by remember { mutableStateOf("") }

    var selectedDateMillis by remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }
    var selectedDateBs by remember { mutableStateOf(com.example.util.NepaliDateUtils.formatBsDate(System.currentTimeMillis())) }
    var selectedDateAd by remember { mutableStateOf(com.example.util.NepaliDateUtils.formatAdDate(System.currentTimeMillis())) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Record Payment Out", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RoseAccent)
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dual Date Selector
        DualDateSelector(
            initialDateMillis = selectedDateMillis,
            onDateChanged = { millis, bs, ad ->
                selectedDateMillis = millis
                selectedDateBs = bs
                selectedDateAd = ad
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = partyName,
            onValueChange = { partyName = it },
            label = { Text("Paid To (Supplier / Vendor)") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount Paid (Rs.) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Remarks / Voucher #") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    onSave(
                        partyName.ifBlank { "Vendor Outflow" },
                        amount,
                        paymentMode,
                        note,
                        selectedDateMillis,
                        selectedDateBs,
                        selectedDateAd
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoseAccent, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Confirm Payment Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun AddItemSheet(
    onSave: (String, String, String, Double, String, Double, Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Hardware") }
    var qtyText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    var pPriceText by remember { mutableStateOf("") }
    var sPriceText by remember { mutableStateOf("") }
    var minStockText by remember { mutableStateOf("5") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Add Inventory Item", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item Name *") },
            colors = customTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_item_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU / Barcode") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = pPriceText,
                onValueChange = { pPriceText = it },
                label = { Text("Purchase Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = sPriceText,
                onValueChange = { sPriceText = it },
                label = { Text("Sale Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = qtyText,
                onValueChange = { qtyText = it },
                label = { Text("Stock Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit (pcs/kg)") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        name,
                        sku,
                        category,
                        qtyText.toDoubleOrNull() ?: 0.0,
                        unit,
                        pPriceText.toDoubleOrNull() ?: 0.0,
                        sPriceText.toDoubleOrNull() ?: 0.0,
                        minStockText.toDoubleOrNull() ?: 5.0
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_item_submit_button")
        ) {
            Text("Save Product Item", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun CreditReminderSheet(
    parties: List<PartyEntity>,
    onSendReminder: (PartyEntity) -> Unit,
    onCancel: () -> Unit
) {
    var sentMessageFor by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Credit Reminder Service", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text("Send automated SMS & WhatsApp payment reminders", fontSize = 12.sp, color = TextMuted)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (parties.isEmpty()) {
            Text("All customers are currently cleared! No pending dues.", color = TealAccent, fontSize = 14.sp)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                items(parties) { party ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(party.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(party.phone, color = TextMuted, fontSize = 12.sp)
                                Text("Pending Due: Rs. ${party.balanceToReceive}", color = TealAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }

                            if (sentMessageFor == party.id) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sent", color = TealAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        sentMessageFor = party.id
                                        onSendReminder(party)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remind", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViewReportsSheet(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    GenerateFinancialReportSheet(
        viewModel = viewModel,
        onClose = onClose
    )
}

@Composable
private fun ReportMetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BusinessSwitcherSheet(
    currentBusiness: String,
    onSelect: (String) -> Unit,
    onCancel: () -> Unit
) {
    val activeEntity = if (currentBusiness.isNotBlank()) currentBusiness else "My Business"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Business Entity", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TealAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TealAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = TealAccent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(activeEntity, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Active Business Profile", color = TealAccent, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.Check, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun NotificationsSheet(
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notifications & Alerts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CardDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TealAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "No Notifications",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "You are all caught up! Important reminders and transaction alerts will appear here.",
                    color = TextMuted,
                    fontSize = 12.5.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
fun RewardsWalletSheet(
    coinBalance: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Atri Nova Rewards", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = GoldCoinBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Available Balance", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldCoin, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(coinBalance.toString(), color = GoldCoin, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Coins", color = GoldCoin, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Redeem Perks:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "Free 50 SMS Reminders" to "500 Coins",
            "Thermal Receipt Custom Branding" to "800 Coins",
            "Priority Cloud Backup (1 Month)" to "1200 Coins"
        ).forEach { (perk, cost) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardDark)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(perk, color = TextWhite, fontSize = 13.sp)
                Text(cost, color = GoldCoin, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun EditShortcutsSheet(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Customize Shortcuts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        listOf("Add Party", "Sales Invoice", "Payment In", "Payment Out", "Add New Item", "Expense", "Day Book").forEach { shortcut ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(shortcut, color = TextWhite, fontSize = 14.sp)
                Icon(Icons.Default.Check, contentDescription = null, tint = TealAccent)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedBorderColor = TealAccent,
    unfocusedBorderColor = CardBorder,
    focusedLabelColor = TealAccent,
    unfocusedLabelColor = TextMuted,
    cursorColor = TealAccent
)

@Composable
fun ManageStaffSheet(onClose: () -> Unit) {
    var showAddStaffForm by remember { mutableStateOf(false) }
    var staffName by remember { mutableStateOf("") }
    var staffPhone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Sales Operator") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Staff & Access Control",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Role-based permissions & security PINs",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!showAddStaffForm) {
            val staffMembers = listOf(
                Triple("Prakash Sharma", "Owner / Super Admin", "Full System Access • All Warehouses"),
                Triple("Anita Basnet", "Store Manager", "Invoicing, Items & Party Ledgers"),
                Triple("Ramesh Shrestha", "Sales Operator", "Sales Invoicing & POS Only"),
                Triple("Sunita Gurung", "Billing Clerk", "Restricted Day Book Access")
            )

            staffMembers.forEach { (name, role, perms) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SkyBlueCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            color = SkyBlueBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SkyBlueCardBg)
                                    .border(0.5.dp, SkyBlueCardBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(role, color = SkyBlueBright, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(perms, color = TextSubtle, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAddStaffForm = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3FF), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Invite New Staff Member", fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedTextField(
                value = staffName,
                onValueChange = { staffName = it },
                label = { Text("Staff Full Name") },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = staffPhone,
                onValueChange = { staffPhone = it },
                label = { Text("Mobile Number (Nepal +977)") },
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Select Access Role:", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))

            listOf("Store Manager", "Sales Operator", "Delivery / Field Staff").forEach { role ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedRole == role) SkyBlueCardBg else CardDark)
                        .clickable { selectedRole = role }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        colors = RadioButtonDefaults.colors(selectedColor = SkyBlueBright, unselectedColor = TextMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(role, color = TextWhite, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = { showAddStaffForm = false },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Cancel", color = TextMuted)
                }
                Button(
                    onClick = { showAddStaffForm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3FF), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Add Staff", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun CashAndBankAccountsSheet(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Cash & Bank Accounts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text("Real-time ledger accounts & liquid balance", fontSize = 12.sp, color = TextMuted)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val accounts = listOf(
            Triple("Cash-in-Hand (Counter Cash)", "Liquid Physical Drawer", 24500.00),
            Triple("Nabil Bank A/C (Current)", "A/C: 0100145228001", 68500.00),
            Triple("Global IME Bank (Savings)", "A/C: 1120038891001", 31500.00)
        )

        accounts.forEach { (title, subtitle, amount) ->
            val formatted = if (privacyMode) "Rs. •••••" else "Rs. ${String.format(java.util.Locale.US, "%,.2f", amount)}"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, color = TextSubtle, fontSize = 12.sp)
                }

                Text(
                    text = formatted,
                    color = Color(0xFF38BDF8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3FF), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Done", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

