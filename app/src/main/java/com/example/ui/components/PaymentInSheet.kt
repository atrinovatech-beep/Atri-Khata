package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PartyEntity
import com.example.ui.theme.AppTheme
import com.example.util.NepaliDateUtils
import java.text.DecimalFormat

/**
 * Corporate Blue Palette for Payment In Sheet
 */
private val SheetNavyBg: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.background
private val SheetCardBg: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.cardBackground
private val SheetSurfaceDark: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.surface
private val SheetCardBorder: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.cardBorder
private val SheetBorderMuted: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.cardBorderLight
private val SheetSkyBlue: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.primary
private val SheetTextPrimary: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.textPrimary
private val SheetTextSecondary: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.textSecondary
private val SheetTextMuted: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.textMuted
private val SheetEmerald = Color(0xFF10B981)
private val SheetAmber = Color(0xFFF59E0B)

private val paymentCurrencyFormat = DecimalFormat("#,##,##0.00")

/**
 * Deposit Account Data Model
 */
data class DepositAccountOption(
    val name: String,
    val subtitle: String,
    val icon: ImageVector
)

private val defaultDepositAccounts = listOf(
    DepositAccountOption("Cash-in-Hand", "Liquid Counter Cash", Icons.Outlined.Payments),
    DepositAccountOption("Nabil Bank A/C", "Current A/C: 0100145228001", Icons.Outlined.AccountBalance),
    DepositAccountOption("Global IME Bank", "Savings A/C: 1120038891001", Icons.Outlined.AccountBalance),
    DepositAccountOption("eSewa / Fonepay", "Digital Merchant Wallet", Icons.Outlined.QrCodeScanner)
)

@Composable
fun PaymentInSheet(
    parties: List<PartyEntity>,
    onSave: (partyName: String, amount: Double, depositAccount: String, recNum: String, note: String, dateMillis: Long, dateBs: String, dateAd: String, isSaveAndNew: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    // ---------------- Date States ----------------
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedDateBs by remember { mutableStateOf(NepaliDateUtils.formatBsDate(System.currentTimeMillis())) }
    var selectedDateAd by remember { mutableStateOf(NepaliDateUtils.formatAdDate(System.currentTimeMillis())) }
    var isBsDateMode by remember { mutableStateOf(true) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // ---------------- Receipt Number State ----------------
    var receiptNumber by remember {
        val bsYear = NepaliDateUtils.adToBs(System.currentTimeMillis()).year
        mutableStateOf("REC-$bsYear-${(1000..9999).random()}")
    }

    // ---------------- Customer States ----------------
    var selectedParty by remember {
        mutableStateOf<PartyEntity?>(parties.firstOrNull { it.type == "Customer" } ?: parties.firstOrNull())
    }
    var customPartyName by remember { mutableStateOf("") }
    var isWalkInCustomer by remember { mutableStateOf(selectedParty == null) }
    var showPartyPickerModal by remember { mutableStateOf(false) }

    // ---------------- Amount & TDS States ----------------
    var receivedAmountText by remember { mutableStateOf("") }
    var isTdsApplicable by remember { mutableStateOf(false) }
    var selectedTdsPreset by remember { mutableStateOf("1.5%") }
    var customTdsPercentText by remember { mutableStateOf("1.5") }

    // ---------------- Deposit Account & Ref States ----------------
    var selectedAccount by remember { mutableStateOf(defaultDepositAccounts[0]) }
    var isAccountDropdownExpanded by remember { mutableStateOf(false) }
    var transactionCode by remember { mutableStateOf("") }

    // ---------------- Remarks State ----------------
    var remarksText by remember { mutableStateOf("") }

    // ---------------- Dynamic Calculations ----------------
    val receivedAmount by remember {
        derivedStateOf {
            receivedAmountText.toDoubleOrNull() ?: 0.0
        }
    }

    val tdsPercentage by remember {
        derivedStateOf {
            if (isTdsApplicable) {
                customTdsPercentText.toDoubleOrNull() ?: 0.0
            } else 0.0
        }
    }

    // In standard accounting, if TDS is withheld at source (e.g., 1.5%), the customer pays (Gross - TDS).
    // So if Customer settled Rs. X, Received is (X * (1 - tds%)), OR if Received Amount is entered,
    // TDS = (Received Amount * (tds% / (100 - tds%))) or direct TDS % on Received Amount.
    // For transparent ease of use: TDS Amount = Received Amount * (tdsPercentage / 100).
    // Total Credit Settled = Received Amount + TDS Amount.
    val tdsDeductedAmount by remember {
        derivedStateOf {
            if (isTdsApplicable && receivedAmount > 0) {
                receivedAmount * (tdsPercentage / 100.0)
            } else 0.0
        }
    }

    val totalSettlementAmount by remember {
        derivedStateOf {
            receivedAmount + tdsDeductedAmount
        }
    }

    fun submitReceipt(isSaveAndNew: Boolean) {
        val finalPartyName = when {
            selectedParty != null && !isWalkInCustomer -> selectedParty!!.name
            customPartyName.isNotBlank() -> customPartyName.trim()
            else -> "Counter Customer"
        }

        val finalNotes = buildString {
            if (remarksText.isNotBlank()) append(remarksText.trim())
            if (transactionCode.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append("Ref: ").append(transactionCode.trim())
            }
            if (isTdsApplicable && tdsDeductedAmount > 0) {
                if (isNotEmpty()) append(" • ")
                append("TDS: Rs. ").append(paymentCurrencyFormat.format(tdsDeductedAmount))
                    .append(" (").append(customTdsPercentText).append("%)")
            }
            if (isNotEmpty()) append(" • ")
            append("Account: ").append(selectedAccount.name)
        }

        onSave(
            finalPartyName,
            receivedAmount,
            selectedAccount.name,
            receiptNumber,
            finalNotes,
            selectedDateMillis,
            selectedDateBs,
            selectedDateAd,
            isSaveAndNew
        )

        if (isSaveAndNew) {
            // Reset fields for the next entry
            val bsYear = NepaliDateUtils.adToBs(System.currentTimeMillis()).year
            receiptNumber = "REC-$bsYear-${(1000..9999).random()}"
            receivedAmountText = ""
            transactionCode = ""
            remarksText = ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(SheetNavyBg)
            .padding(horizontal = 18.dp)
            .testTag("payment_in_sheet")
    ) {
        // Drag Handle & Header Bar
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF334155))
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Header Title with Icon & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E3A8A).copy(alpha = 0.6f))
                            .border(1.dp, SheetSkyBlue, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Payments,
                            contentDescription = null,
                            tint = SheetSkyBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Record Payment In",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = SheetTextPrimary
                        )
                        Text(
                            text = "Cash & Bank Receipt Voucher",
                            fontSize = 11.5.sp,
                            color = SheetTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SheetTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 1: Clean Single-Line Date Field
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SheetCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SheetCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "RECEIPT DATE",
                        color = SheetSkyBlue,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Single-Line Compact Date Container with quick AD/BS toggle option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SheetSurfaceDark)
                            .border(1.dp, SheetCardBorder.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Date Icon & Date Text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePickerDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Receipt Date",
                                tint = SheetSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBsDateMode) "$selectedDateBs BS" else "$selectedDateAd AD",
                                color = SheetTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${if (isBsDateMode) selectedDateAd else selectedDateBs})",
                                color = SheetTextMuted,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        // Right: Compact AD/BS toggle & CHANGE button on same line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(0.8.dp, SheetSkyBlue.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable { isBsDateMode = !isBsDateMode }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.SwapHoriz,
                                        contentDescription = "Toggle AD / BS",
                                        tint = SheetSkyBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isBsDateMode) "AD" else "BS",
                                        color = SheetSkyBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SheetSkyBlue)
                                    .clickable { showDatePickerDialog = true }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "CHANGE",
                                    color = Color(0xFF0A0F1D),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 2: Receipt Number & Customer Balance (Same Line)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SheetCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SheetCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Rec. No. Field with refresh generator
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = "RECEIPT NO.",
                                color = SheetSkyBlue,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = receiptNumber,
                                    onValueChange = { receiptNumber = it },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SheetTextPrimary
                                    ),
                                    colors = paymentSheetTextFieldColors(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        val bsYear = NepaliDateUtils.adToBs(selectedDateMillis).year
                                        receiptNumber = "REC-$bsYear-${(1000..9999).random()}"
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, SheetBorderMuted, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Regenerate Receipt Number",
                                        tint = SheetSkyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Right: Dynamic Customer Balance Badge on the Same Line
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CURRENT BALANCE",
                                color = SheetSkyBlue,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val party = selectedParty
                            val isDebit = (party?.balanceToReceive ?: 0.0) > 0
                            val isCredit = (party?.balanceToReceive ?: 0.0) < 0
                            val balanceAmount = party?.balanceToReceive ?: 0.0

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            party == null || isWalkInCustomer -> Color(0xFF1E293B).copy(alpha = 0.6f)
                                            isDebit -> SheetAmber.copy(alpha = 0.15f)
                                            isCredit -> SheetSkyBlue.copy(alpha = 0.15f)
                                            else -> SheetEmerald.copy(alpha = 0.15f)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when {
                                            party == null || isWalkInCustomer -> SheetBorderMuted
                                            isDebit -> SheetAmber.copy(alpha = 0.7f)
                                            isCredit -> SheetSkyBlue.copy(alpha = 0.7f)
                                            else -> SheetEmerald.copy(alpha = 0.7f)
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (party != null && !isWalkInCustomer) {
                                    Column {
                                        Text(
                                            text = when {
                                                isDebit -> "Due: Rs. ${paymentCurrencyFormat.format(balanceAmount)}"
                                                isCredit -> "Adv: Rs. ${paymentCurrencyFormat.format(kotlin.math.abs(balanceAmount))}"
                                                else -> "Clear (Rs. 0.00)"
                                            },
                                            color = when {
                                                isDebit -> SheetAmber
                                                isCredit -> SheetSkyBlue
                                                else -> SheetEmerald
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isDebit) "To Receive (Dr)" else if (isCredit) "Advance Paid (Cr)" else "All Dues Paid",
                                            color = SheetTextMuted,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "No Customer Selected",
                                        color = SheetTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 3: Customer Name Field (On its own single full-width line)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SheetCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SheetCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CUSTOMER NAME *",
                        color = SheetSkyBlue,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Single full-width interactive customer selector / input line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SheetSurfaceDark)
                            .border(1.dp, SheetBorderMuted, RoundedCornerShape(8.dp))
                            .clickable { showPartyPickerModal = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(SheetSkyBlue.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Customer",
                                        tint = SheetSkyBlue,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = when {
                                            selectedParty != null && !isWalkInCustomer -> selectedParty!!.name
                                            customPartyName.isNotBlank() -> customPartyName
                                            else -> "Walk-in Cash Customer"
                                        },
                                        color = SheetTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (selectedParty != null && !isWalkInCustomer) {
                                            if (selectedParty!!.phone.isNotBlank()) selectedParty!!.phone else "Registered Customer"
                                        } else {
                                            "Tap to select customer from directory or ledger"
                                        },
                                        color = SheetTextSecondary,
                                        fontSize = 10.5.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E3A8A).copy(alpha = 0.5f))
                                        .border(0.8.dp, SheetSkyBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (selectedParty != null && !isWalkInCustomer) "CHANGE" else "SELECT",
                                        color = SheetSkyBlue,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = SheetSkyBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (selectedParty != null && !isWalkInCustomer) {
                        val party = selectedParty!!
                        if (party.panVatNumber.isNotBlank() || party.address.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (party.panVatNumber.isNotBlank()) {
                                    Text(
                                        text = "PAN/VAT: ${party.panVatNumber}",
                                        color = SheetTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                if (party.address.isNotBlank()) {
                                    Text(
                                        text = party.address,
                                        color = SheetTextMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customPartyName,
                            onValueChange = { customPartyName = it },
                            placeholder = { Text("Or enter custom customer name...", fontSize = 12.5.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 13.sp,
                                color = SheetTextPrimary
                            ),
                            colors = paymentSheetTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 4: Received Amount & TDS Checkbox (Same Line)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SheetCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SheetCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Received Amount input field
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "RECEIVED AMOUNT (RS.) *",
                                color = SheetSkyBlue,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = receivedAmountText,
                                onValueChange = { receivedAmountText = it },
                                placeholder = { Text("0.00", fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SheetSkyBlue
                                ),
                                colors = paymentSheetTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }

                        // Right: TDS Checkbox and "TDS Applicable" label on the same horizontal line
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = "TAX DEDUCTION",
                                color = SheetSkyBlue,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isTdsApplicable) SheetSkyBlue.copy(alpha = 0.12f) else SheetSurfaceDark)
                                    .border(
                                        1.dp,
                                        if (isTdsApplicable) SheetSkyBlue else SheetBorderMuted,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { isTdsApplicable = !isTdsApplicable }
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isTdsApplicable,
                                    onCheckedChange = { isTdsApplicable = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SheetSkyBlue,
                                        checkmarkColor = Color(0xFF0A0F1D),
                                        uncheckedColor = SheetTextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Column {
                                    Text(
                                        text = "TDS Applicable",
                                        color = if (isTdsApplicable) SheetSkyBlue else SheetTextPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isTdsApplicable) "$customTdsPercentText% IRD Withhold" else "Withholding tax",
                                        color = SheetTextMuted,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Dynamically triggered TDS Calculation Summary when checked
                    AnimatedVisibility(
                        visible = isTdsApplicable,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = SheetBorderMuted.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Preset TDS Rate selection chips
                                Column(modifier = Modifier.weight(1.1f)) {
                                    Text(
                                        text = "TDS RATE (%)",
                                        color = SheetSkyBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf("1.5%", "10%", "15%").forEach { rate ->
                                            val isSelected = selectedTdsPreset == rate
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(34.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) SheetSkyBlue else SheetSurfaceDark)
                                                    .border(
                                                        0.8.dp,
                                                        if (isSelected) SheetSkyBlue else SheetBorderMuted,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        selectedTdsPreset = rate
                                                        customTdsPercentText = rate.replace("%", "")
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = rate,
                                                    color = if (isSelected) Color.Black else SheetTextPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // TDS Deducted Live Badge & Summary
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "TDS AMOUNT",
                                        color = SheetSkyBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SheetAmber.copy(alpha = 0.12f))
                                            .border(1.dp, SheetAmber.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = "+ Rs. ${paymentCurrencyFormat.format(tdsDeductedAmount)}",
                                            color = SheetAmber,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 5: Deposit Account & Transaction Code (Same Line)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SheetCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SheetCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Deposit Account Dropdown
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "DEPOSIT ACCOUNT",
                                color = SheetSkyBlue,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SheetSurfaceDark)
                                        .border(1.dp, SheetBorderMuted, RoundedCornerShape(8.dp))
                                        .clickable { isAccountDropdownExpanded = true }
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = selectedAccount.icon,
                                            contentDescription = null,
                                            tint = SheetSkyBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = selectedAccount.name,
                                            color = SheetTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = SheetSkyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = isAccountDropdownExpanded,
                                    onDismissRequest = { isAccountDropdownExpanded = false },
                                    modifier = Modifier.background(SheetNavyBg)
                                ) {
                                    defaultDepositAccounts.forEach { acc ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(acc.icon, contentDescription = null, tint = SheetSkyBlue, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(acc.name, color = SheetTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                        Text(acc.subtitle, color = SheetTextMuted, fontSize = 10.sp)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedAccount = acc
                                                isAccountDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Right: Transaction Code / Ref # on the SAME line
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = "TRANSACTION CODE",
                                color = SheetSkyBlue,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = transactionCode,
                                onValueChange = { transactionCode = it },
                                placeholder = { Text("Cheque / UTR / Trace", fontSize = 11.5.sp) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SheetTextPrimary
                                ),
                                colors = paymentSheetTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 6: Remarks / Notes
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SheetCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SheetCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "REMARKS / VOUCHER NOTES",
                        color = SheetSkyBlue,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        placeholder = { Text("e.g., Part payment against INV-2082-1045, cleared by RTGS") },
                        colors = paymentSheetTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 7: Final Summary Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SheetSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, SheetCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RECEIPT SUMMARY",
                        color = SheetSkyBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ReceiptSummaryRow(
                        label = "Cash / Bank Received",
                        amount = "Rs. ${paymentCurrencyFormat.format(receivedAmount)}"
                    )

                    if (isTdsApplicable && tdsDeductedAmount > 0) {
                        ReceiptSummaryRow(
                            label = "TDS Deducted ($customTdsPercentText%)",
                            amount = "+ Rs. ${paymentCurrencyFormat.format(tdsDeductedAmount)}",
                            amountColor = SheetAmber
                        )
                    }

                    ReceiptSummaryRow(
                        label = "Deposited To",
                        amount = selectedAccount.name,
                        amountColor = SheetSkyBlue
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = SheetCardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL SETTLED",
                                color = SheetTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Credit to Customer Ledger",
                                color = SheetTextMuted,
                                fontSize = 10.5.sp
                            )
                        }

                        Text(
                            text = "Rs. ${paymentCurrencyFormat.format(totalSettlementAmount)}",
                            color = SheetSkyBlue,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section 8: Dual Action Buttons ("Save & New" and "Save")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Button: "Save & New"
                OutlinedButton(
                    onClick = { submitReceipt(isSaveAndNew = true) },
                    enabled = receivedAmount > 0,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SheetSkyBlue,
                        disabledContentColor = SheetTextMuted
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        if (receivedAmount > 0) SheetSkyBlue else SheetBorderMuted
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("save_and_new_button")
                ) {
                    Text(
                        text = "Save & New",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Primary Button: "Save"
                Button(
                    onClick = { submitReceipt(isSaveAndNew = false) },
                    enabled = receivedAmount > 0,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SheetSkyBlue,
                        contentColor = Color(0xFF0A0F1D),
                        disabledContainerColor = Color(0xFF1E293B),
                        disabledContentColor = SheetTextMuted
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("save_payment_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Receipt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // ---------------- Dialog: Date Picker Modal ----------------
    if (showDatePickerDialog) {
        PaymentDatePickerDialog(
            currentMillis = selectedDateMillis,
            onDateSelected = { millis, bs, ad ->
                selectedDateMillis = millis
                selectedDateBs = bs
                selectedDateAd = ad
                showDatePickerDialog = false
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }

    // ---------------- Dialog: Party Picker Modal ----------------
    if (showPartyPickerModal) {
        PaymentCustomerPickerModal(
            parties = parties,
            selectedParty = selectedParty,
            onSelectParty = { party ->
                selectedParty = party
                isWalkInCustomer = false
                showPartyPickerModal = false
            },
            onSelectWalkIn = {
                selectedParty = null
                isWalkInCustomer = true
                showPartyPickerModal = false
            },
            onDismiss = { showPartyPickerModal = false }
        )
    }
}

@Composable
private fun ReceiptSummaryRow(
    label: String,
    amount: String,
    amountColor: Color = SheetTextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = SheetTextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = amount,
            color = amountColor,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Customer Selection Dialog for Payment In
 */
@Composable
private fun PaymentCustomerPickerModal(
    parties: List<PartyEntity>,
    selectedParty: PartyEntity?,
    onSelectParty: (PartyEntity) -> Unit,
    onSelectWalkIn: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredParties = remember(parties, searchQuery) {
        parties.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true) ||
                    it.city.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .clip(RoundedCornerShape(16.dp)),
            color = SheetNavyBg,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, SheetCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Customer",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SheetTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SheetTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search customer name, phone...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SheetSkyBlue)
                    },
                    colors = paymentSheetTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Walk-in Option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, SheetSkyBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onSelectWalkIn() }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(SheetSkyBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SheetSkyBlue, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Walk-in / Counter Customer",
                                color = SheetTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "Direct receipt without linking customer ledger",
                                color = SheetTextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "REGISTERED CUSTOMERS (${filteredParties.size})",
                    color = SheetSkyBlue,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(filteredParties) { _, party ->
                        val isSelected = selectedParty?.id == party.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SheetSkyBlue.copy(alpha = 0.15f) else SheetSurfaceDark)
                                .border(
                                    1.dp,
                                    if (isSelected) SheetSkyBlue else Color(0xFF1E293B),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectParty(party) }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E3A8A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = party.name.take(1).uppercase(),
                                            color = SheetSkyBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = party.name,
                                            color = SheetTextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${party.type} • ${party.phone.ifBlank { "No Phone" }}",
                                            color = SheetTextMuted,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                val hasDue = party.balanceToReceive > 0
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (hasDue) SheetAmber.copy(alpha = 0.15f) else Color(0xFF1E293B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (hasDue) "Due: Rs. ${paymentCurrencyFormat.format(party.balanceToReceive)}" else "Clear",
                                        color = if (hasDue) SheetAmber else SheetEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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

/**
 * Compact Date Picker Modal for Payment In
 */
@Composable
private fun PaymentDatePickerDialog(
    currentMillis: Long,
    onDateSelected: (Long, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val nepaliDate = remember(currentMillis) { NepaliDateUtils.adToBs(currentMillis) }
    var tempYear by remember { mutableIntStateOf(nepaliDate.year) }
    var tempMonth by remember { mutableIntStateOf(nepaliDate.month) }
    var tempDay by remember { mutableIntStateOf(nepaliDate.day) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = SheetNavyBg,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, SheetCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Change Receipt Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SheetTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SheetTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val calculatedMillis = remember(tempYear, tempMonth, tempDay) {
                    NepaliDateUtils.bsToAd(tempYear, tempMonth, tempDay)
                }
                val previewBs = remember(tempYear, tempMonth, tempDay) {
                    String.format(java.util.Locale.US, "%04d/%02d/%02d", tempYear, tempMonth, tempDay)
                }
                val previewAd = remember(calculatedMillis) {
                    NepaliDateUtils.formatAdDate(calculatedMillis)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SheetSurfaceDark)
                        .border(1.dp, SheetSkyBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$previewBs BS",
                            color = SheetSkyBlue,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$previewAd AD",
                            color = SheetTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PaymentNumberStepperColumn(
                        label = "BS Year",
                        value = tempYear.toString(),
                        onMinus = { if (tempYear > 2078) tempYear-- },
                        onPlus = { if (tempYear < 2086) tempYear++ }
                    )

                    PaymentNumberStepperColumn(
                        label = "Month",
                        value = "${NepaliDateUtils.NEPALI_MONTHS_EN.getOrElse(tempMonth - 1) { "" }} ($tempMonth)",
                        onMinus = { if (tempMonth > 1) tempMonth-- else tempMonth = 12 },
                        onPlus = { if (tempMonth < 12) tempMonth++ else tempMonth = 1 }
                    )

                    val maxDays = NepaliDateUtils.getDaysInBsMonth(tempYear, tempMonth)
                    PaymentNumberStepperColumn(
                        label = "Day",
                        value = tempDay.toString(),
                        onMinus = { if (tempDay > 1) tempDay-- else tempDay = maxDays },
                        onPlus = { if (tempDay < maxDays) tempDay++ else tempDay = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onDateSelected(calculatedMillis, previewBs, previewAd)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SheetSkyBlue,
                        contentColor = Color(0xFF0A0F1D)
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("Apply Date", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            }
        }
    }
}

@Composable
private fun PaymentNumberStepperColumn(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = SheetTextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        IconButton(
            onClick = onPlus,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
        ) {
            Icon(Icons.Default.ArrowDropUp, contentDescription = "Up", tint = SheetSkyBlue)
        }
        Text(
            text = value,
            color = SheetTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        IconButton(
            onClick = onMinus,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Down", tint = SheetSkyBlue)
        }
    }
}

@Composable
private fun paymentSheetTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SheetTextPrimary,
    unfocusedTextColor = SheetTextPrimary,
    focusedBorderColor = SheetSkyBlue,
    unfocusedBorderColor = SheetBorderMuted,
    focusedLabelColor = SheetSkyBlue,
    unfocusedLabelColor = SheetTextSecondary,
    cursorColor = SheetSkyBlue,
    focusedContainerColor = SheetSurfaceDark,
    unfocusedContainerColor = SheetSurfaceDark
)
