package com.example.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PartyEntity
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Advanced "Add New Party" Form & Bottom Sheet tailored for professional accounting ledgers
 * (similar to Nepali/Global business ledgers like Mero Karobar, Vyapar, Khatabook).
 *
 * Core Features:
 * - Party Type Switcher: 1. Customer vs 2. Supplier
 * - Dynamic Categories: Wholesale, Retail, Distributor, Walk-in / Manufacturer, Wholesaler, Service Provider
 * - Financial Indicators: To Receive (Dr) vs To Give (Cr) with Opening Balance and accounting validation
 * - Communication & Contact: Phone Number, Secondary Contact Number, Contact Person, Email
 * - Tax & Legal: PAN / VAT Registration Number
 * - Location & Ledger Notes: Address / City, Notes / Description
 * - Register Date: Interactive calendar date picker (defaulting to current date)
 * - Corporate Navy & Sky Blue palette with full light/dark mode compliance
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddPartySheet(
    onSaveParty: (PartyEntity) -> Unit,
    onCancel: () -> Unit,
    initialType: String = "Customer",
    partyToEdit: PartyEntity? = null
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark

    // Form States
    var partyType by remember { mutableStateOf(partyToEdit?.type ?: initialType) } // "Customer" or "Supplier"
    var name by remember { mutableStateOf(partyToEdit?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    // Categories
    val customerCategories = remember { listOf("Wholesale", "Retail", "Distributor", "Walk-in", "Corporate", "Other") }
    val supplierCategories = remember { listOf("Wholesaler", "Distributor", "Manufacturer", "Retail", "Service Provider", "Importer", "Other") }
    var selectedCategory by remember { mutableStateOf(partyToEdit?.category ?: (if (partyType == "Customer") "Retail" else "Wholesaler")) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Opening Balance & Financial Indicator (Dr/Cr)
    var openingBalanceText by remember {
        mutableStateOf(if (partyToEdit != null && partyToEdit.openingBalance > 0) String.format(Locale.US, "%.2f", partyToEdit.openingBalance) else "")
    }
    // Customer defaults to Dr (To Receive), Supplier defaults to Cr (To Give)
    var balanceType by remember {
        mutableStateOf(partyToEdit?.balanceType ?: (if (partyType == "Customer") "To Receive (Dr)" else "To Give (Cr)"))
    }

    // Automatically update default indicator & category when party type changes
    LaunchedEffect(partyType) {
        if (partyType == "Customer") {
            if (balanceType == "To Give (Cr)" && openingBalanceText.isBlank()) {
                balanceType = "To Receive (Dr)"
            }
            if (selectedCategory in supplierCategories) {
                selectedCategory = "Retail"
            }
        } else {
            if (balanceType == "To Receive (Dr)" && openingBalanceText.isBlank()) {
                balanceType = "To Give (Cr)"
            }
            if (selectedCategory in customerCategories) {
                selectedCategory = "Wholesaler"
            }
        }
    }

    // Contact Details
    var phone by remember { mutableStateOf(partyToEdit?.phone ?: "") }
    var contactNumber by remember { mutableStateOf(partyToEdit?.contactNumber ?: "") } // Secondary phone / alt number
    var contactPerson by remember { mutableStateOf(partyToEdit?.contactPerson ?: "") } // Representative name
    var email by remember { mutableStateOf(partyToEdit?.email ?: "") }

    // Tax & Legal
    var panVatNumber by remember { mutableStateOf(partyToEdit?.panVatNumber ?: "") }

    // Location & Notes
    var address by remember { mutableStateOf(partyToEdit?.address ?: "") }
    var city by remember { mutableStateOf(partyToEdit?.city ?: "") }
    var notes by remember { mutableStateOf(partyToEdit?.notes ?: "") }

    // Register Date (Calendar Picker)
    var registerDateMillis by remember { mutableLongStateOf(partyToEdit?.registerDate ?: System.currentTimeMillis()) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag("add_party_sheet_root")
    ) {
        // TOP HEADER BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (partyToEdit != null) {
                        "Edit Party Details"
                    } else if (partyType == "Customer") {
                        "Add New Customer"
                    } else {
                        "Add New Supplier"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (partyToEdit != null) "Update customer/supplier profile & balance" else "Professional Accounting Ledger Entry",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier.testTag("add_party_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = if (isDark) TextMuted else Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 1. PARTY TYPE SWITCHER (TOP SELECTOR)
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "PARTY TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = if (isDark) SkyBlueBright else SkyBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Customer
                    PartyTypeOptionCard(
                        title = "Customer",
                        subtitle = "Buyer / Client",
                        icon = Icons.Outlined.Person,
                        isSelected = partyType == "Customer",
                        activeColor = SkyBlue,
                        activeBg = SkyBlueCardBg,
                        activeBorder = SkyBlueCardBorder,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            partyType = "Customer"
                            nameError = null
                        },
                        testTag = "party_type_customer_toggle"
                    )

                    // Option 2: Supplier
                    PartyTypeOptionCard(
                        title = "Supplier",
                        subtitle = "Vendor / Merchant",
                        icon = Icons.Outlined.LocalShipping,
                        isSelected = partyType == "Supplier",
                        activeColor = OrangeAccent,
                        activeBg = OrangeCardBg,
                        activeBorder = OrangeCardBorder,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            partyType = "Supplier"
                            nameError = null
                        },
                        testTag = "party_type_supplier_toggle"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. BASIC INFORMATION SECTION
        FormSectionContainer(
            title = if (partyType == "Customer") "CUSTOMER IDENTITY & CATEGORY" else "SUPPLIER IDENTITY & CATEGORY",
            isDark = isDark
        ) {
            // Party / Business Name (MANDATORY)
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null && it.isNotBlank()) nameError = null
                },
                label = {
                    Text(if (partyType == "Customer") "Customer / Business Name *" else "Supplier / Firm Name *")
                },
                placeholder = {
                    Text(
                        if (partyType == "Customer") "e.g. Ram Traders, Sita Sharma"
                        else "e.g. Acme Supplies, Global Traders"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = if (nameError != null) Color(0xFFEF4444) else (if (isDark) SkyBlueBright else SkyBlue)
                    )
                },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(text = nameError!!, color = Color(0xFFEF4444), fontSize = 11.sp)
                    } else {
                        Text("Mandatory for tax invoices and ledger tracking", color = TextSubtle, fontSize = 11.sp)
                    }
                },
                singleLine = true,
                colors = formTextFieldColors(isDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Dropdown Selector
            Text(
                text = "Category / Segment",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) TextWhite else Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) SurfaceDark else Color.White)
                        .border(
                            1.dp,
                            if (isCategoryDropdownExpanded) (if (isDark) SkyBlueBright else SkyBlue)
                            else (if (isDark) CardBorder else Color(0xFFCBD5E1)),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { isCategoryDropdownExpanded = true }
                        .padding(horizontal = 14.dp)
                        .testTag("add_party_category_dropdown"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null,
                            tint = if (isDark) SkyBlueBright else SkyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selectedCategory,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) TextWhite else Color(0xFF0F172A)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Category",
                        tint = if (isDark) SkyBlueBright else SkyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false },
                    modifier = Modifier
                        .background(if (isDark) CardDark else Color.White)
                        .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                ) {
                    val availableCategories = if (partyType == "Customer") customerCategories else supplierCategories
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category,
                                        color = if (isSelected) (if (isDark) SkyBlueBright else SkyBlue)
                                               else (if (isDark) TextWhite else Color(0xFF0F172A)),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.5.sp
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = if (isDark) SkyBlueBright else SkyBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedCategory = category
                                isCategoryDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("category_chip_$category")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. FINANCIAL INDICATOR & OPENING BALANCE (ON THE SAME LINE)
        FormSectionContainer(
            title = "FINANCIAL BALANCE & OPENING DUES",
            isDark = isDark
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Opening Balance input field
                Column(modifier = Modifier.weight(1.15f)) {
                    Text(
                        text = if (partyType == "Customer") "OPENING DUES (RS.)" else "OPENING PAYABLES (RS.)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (isDark) SkyBlueBright else SkyBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = openingBalanceText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                openingBalanceText = it
                            }
                        },
                        placeholder = { Text("0.00", fontSize = 13.sp) },
                        prefix = {
                            Text(
                                text = "Rs. ",
                                color = if (isDark) SkyBlueBright else SkyBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = formTextFieldColors(isDark),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_party_opening_balance_input")
                    )
                }

                // Right: Dr / Cr Segmented Indicator on the SAME line
                Column(modifier = Modifier.weight(1.05f)) {
                    Text(
                        text = "BALANCE TYPE (DR/CR)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (isDark) SkyBlueBright else SkyBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) SurfaceDark else Color(0xFFF1F5F9))
                            .border(
                                1.dp,
                                if (isDark) CardBorder else Color(0xFFCBD5E1),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isDrSelected = balanceType == "To Receive (Dr)"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDrSelected) SkyBlueCardBg else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (isDrSelected) SkyBlue else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { balanceType = "To Receive (Dr)" }
                                .testTag("balance_type_dr_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "To Receive",
                                    color = if (isDrSelected) SkyBlueBright else (if (isDark) TextMuted else Color(0xFF64748B)),
                                    fontWeight = if (isDrSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "(Dr)",
                                    color = if (isDrSelected) SkyBlue else TextSubtle,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        val isCrSelected = balanceType == "To Give (Cr)"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCrSelected) OrangeCardBg else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (isCrSelected) OrangeAccent else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { balanceType = "To Give (Cr)" }
                                .testTag("balance_type_cr_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "To Give",
                                    color = if (isCrSelected) OrangeAccent else (if (isDark) TextMuted else Color(0xFF64748B)),
                                    fontWeight = if (isCrSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "(Cr)",
                                    color = if (isCrSelected) OrangeAccent else TextSubtle,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            // Explanatory guideline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (balanceType == "To Receive (Dr)") {
                        "Party owes you this amount (Debit / Asset)."
                    } else {
                        "You owe this party this amount (Credit / Liability)."
                    },
                    color = TextMuted,
                    fontSize = 10.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. CONTACT & COMMUNICATION SECTION
        FormSectionContainer(
            title = "PHONE & CONTACT PERSON",
            isDark = isDark
        ) {
            // Primary Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Primary Phone Number") },
                placeholder = { Text("e.g. 9852020149") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = if (isDark) SkyBlueBright else SkyBlue
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = formTextFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_phone_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary / Alternate Contact Number
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Secondary Contact Number (Alt)") },
                placeholder = { Text("e.g. 01-4432100 or 9800000000") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Call,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = formTextFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_contact_number_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Person Name
            OutlinedTextField(
                value = contactPerson,
                onValueChange = { contactPerson = it },
                label = { Text("Contact Person / Representative") },
                placeholder = { Text("e.g. Ramesh Karki (Manager)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                colors = formTextFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_contact_person_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Email Address
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("e.g. contact@business.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = formTextFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_email_input")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. TAX REGISTRATION & ADDRESS
        FormSectionContainer(
            title = "TAX (PAN / VAT) & LOCATION",
            isDark = isDark
        ) {
            // PAN / VAT Number
            OutlinedTextField(
                value = panVatNumber,
                onValueChange = { if (it.length <= 15) panVatNumber = it },
                label = { Text("PAN / VAT Registration Number") },
                placeholder = { Text("e.g. 609823415") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = if (isDark) SkyBlueBright else SkyBlue
                    )
                },
                supportingText = {
                    Text("Official 9-digit tax identifier for billing & VAT returns", color = TextSubtle, fontSize = 11.sp)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = formTextFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_pan_vat_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Address / City
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / Street / Location") },
                placeholder = { Text("e.g. Bhattimod, Biratnagar-6") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                colors = formTextFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_address_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // City
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City / District") },
                placeholder = { Text("e.g. Biratnagar, Morang") },
                colors = formTextFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_city_input")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. REGISTRATION DATE & NOTES
        FormSectionContainer(
            title = "REGISTRATION DATE & NOTES",
            isDark = isDark
        ) {
            // Register Date with Interactive Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = registerDateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selected = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    set(Calendar.HOUR_OF_DAY, 12)
                                    set(Calendar.MINUTE, 0)
                                }
                                registerDateMillis = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                OutlinedTextField(
                    value = dateFormatter.format(registerDateMillis),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Registration Date") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = "Select Date",
                            tint = if (isDark) SkyBlueBright else SkyBlue
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (isDark) TextWhite else Color(0xFF0F172A),
                        disabledBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1),
                        disabledLabelColor = if (isDark) SkyBlueBright else SkyBlue,
                        disabledLeadingIconColor = if (isDark) SkyBlueBright else SkyBlue
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_party_date_picker_field")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description / Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Description / Remarks / Payment Terms") },
                placeholder = { Text("e.g. 15-day credit cycle, wholesale discount tier") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                minLines = 2,
                maxLines = 4,
                colors = formTextFieldColors(isDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_party_notes_input")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ACTION BUTTONS (Cancel & Save)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDark) TextWhite else Color(0xFF334155)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) CardBorder else Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("add_party_cancel_button")
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isBlank()) {
                        nameError = "Please enter party name"
                        Toast.makeText(context, "Party Name is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val openingBal = openingBalanceText.toDoubleOrNull() ?: 0.0
                    val isDr = balanceType.contains("Receive") || balanceType.contains("Dr")
                    val isCr = balanceType.contains("Give") || balanceType.contains("Cr")

                    val party = if (partyToEdit != null) {
                        partyToEdit.copy(
                            name = trimmedName,
                            phone = phone.trim(),
                            contactNumber = contactNumber.trim(),
                            type = partyType,
                            category = selectedCategory,
                            balanceToReceive = if (partyToEdit.openingBalance != openingBal) (if (isDr) openingBal else 0.0) else partyToEdit.balanceToReceive,
                            balanceToGive = if (partyToEdit.openingBalance != openingBal) (if (isCr) openingBal else 0.0) else partyToEdit.balanceToGive,
                            openingBalance = openingBal,
                            balanceType = balanceType,
                            panVatNumber = panVatNumber.trim(),
                            email = email.trim(),
                            contactPerson = contactPerson.trim(),
                            address = address.trim(),
                            city = city.trim(),
                            notes = notes.trim(),
                            registerDate = registerDateMillis
                        )
                    } else {
                        PartyEntity(
                            name = trimmedName,
                            phone = phone.trim(),
                            contactNumber = contactNumber.trim(),
                            type = partyType,
                            category = selectedCategory,
                            balanceToReceive = if (isDr) openingBal else 0.0,
                            balanceToGive = if (isCr) openingBal else 0.0,
                            openingBalance = openingBal,
                            balanceType = balanceType,
                            panVatNumber = panVatNumber.trim(),
                            email = email.trim(),
                            contactPerson = contactPerson.trim(),
                            address = address.trim(),
                            city = city.trim(),
                            notes = notes.trim(),
                            registerDate = registerDateMillis,
                            createdAt = System.currentTimeMillis()
                        )
                    }

                    onSaveParty(party)
                    Toast.makeText(
                        context,
                        if (partyToEdit != null) "Successfully updated '$trimmedName'" else "Successfully added ${if (partyType == "Customer") "Customer" else "Supplier"} '$trimmedName'",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (partyType == "Customer") SkyBlue else OrangeAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.4f)
                    .height(50.dp)
                    .testTag("add_party_submit_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (partyToEdit != null) "Update Party" else if (partyType == "Customer") "Save Customer" else "Save Supplier",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Backward-compatible overload accepting simple arguments
 */
@Composable
fun AddPartySheet(
    onSave: (String, String, String, Double, String) -> Unit,
    onCancel: () -> Unit
) {
    AddPartySheet(
        onSaveParty = { party ->
            val bal = if (party.balanceToReceive > 0) party.balanceToReceive else party.balanceToGive
            onSave(party.name, party.phone, party.type, bal, party.address)
        },
        onCancel = onCancel
    )
}

// -----------------------------------------------------------------------------
// HELPER COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun PartyTypeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    activeBorder: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) activeBg else (if (isDark) SurfaceDark else Color(0xFFF1F5F9)),
        label = "bgAnim"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) activeBorder else (if (isDark) CardBorder else Color(0xFFE2E8F0)),
        label = "borderAnim"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .border(1.5.dp, animatedBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor.copy(alpha = 0.2f) else (if (isDark) CardDark else Color(0xFFE2E8F0))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) activeColor else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) (if (isDark) TextWhite else Color(0xFF0F172A)) else TextMuted
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isSelected) activeColor else TextSubtle
                )
            }
        }
    }
}

@Composable
private fun FormSectionContainer(
    title: String,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = if (isDark) SkyBlueBright else SkyBlue
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun formTextFieldColors(isDark: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = if (isDark) TextWhite else Color(0xFF0F172A),
    unfocusedTextColor = if (isDark) TextWhite else Color(0xFF0F172A),
    focusedBorderColor = if (isDark) SkyBlueBright else SkyBlue,
    unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1),
    focusedLabelColor = if (isDark) SkyBlueBright else SkyBlue,
    unfocusedLabelColor = TextMuted,
    cursorColor = if (isDark) SkyBlueBright else SkyBlue,
    focusedContainerColor = if (isDark) SurfaceDark else Color.White,
    unfocusedContainerColor = if (isDark) SurfaceDark else Color.White
)
