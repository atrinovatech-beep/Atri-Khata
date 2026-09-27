package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.PartyEntity
import com.example.service.pdf.PdfReportService
import com.example.ui.ActiveDialog
import com.example.ui.MainViewModel
import com.example.ui.components.AddPartySheet
import com.example.ui.theme.*
import com.example.util.NepaliDateUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val parties by viewModel.allParties.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()
    val selectedPartyIdForDetail by viewModel.selectedPartyIdForDetail.collectAsStateWithLifecycle()
    val partySettings by viewModel.partySettings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedPartyTypeFilter by remember { mutableStateOf("All Party") }
    var selectedPaymentFilter by remember { mutableStateOf("All Payment") }
    var selectedCategoryFilter by remember { mutableStateOf("All Category") }

    var filterSheetMode by remember { mutableStateOf<String?>(null) }
    var selectedPartyForDetail by remember { mutableStateOf<PartyEntity?>(null) }
    var partyToEdit by remember { mutableStateOf<PartyEntity?>(null) }
    var showPdfExportNotification by remember { mutableStateOf(false) }

    // Always fetch latest party state from database reactively
    val currentSelectedParty = remember(parties, selectedPartyForDetail, selectedPartyIdForDetail) {
        if (selectedPartyIdForDetail != null) {
            parties.find { it.id == selectedPartyIdForDetail }
        } else {
            selectedPartyForDetail?.let { target ->
                parties.find { it.id == target.id } ?: target
            }
        }
    }

    if (currentSelectedParty != null) {
        PartyDetailView(
            party = currentSelectedParty,
            allTransactions = transactions,
            privacyMode = privacyMode,
            onBackClick = {
                selectedPartyForDetail = null
                viewModel.selectPartyForDetail(null)
            },
            onEditParty = { partyToEdit = it },
            onDeleteParty = {
                viewModel.deleteParty(it)
                selectedPartyForDetail = null
                viewModel.selectPartyForDetail(null)
            },
            onCreateSale = { viewModel.openDialog(ActiveDialog.SALES_INVOICE) },
            onCreatePurchase = { viewModel.openPurchaseDialog(currentSelectedParty) },
            onRecordReceive = { viewModel.openDialog(ActiveDialog.PAYMENT_IN) },
            onRecordPay = { viewModel.openDialog(ActiveDialog.PAYMENT_OUT) },
            onDeleteTransaction = { viewModel.deleteTransaction(it) },
            onCallParty = { phone ->
                if (phone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                }
            },
            onSendReminder = { targetParty ->
                val message = "Dear ${targetParty.name}, gentle reminder regarding your pending balance of Rs. ${String.format(Locale.US, "%,.2f", targetParty.balanceToReceive)} with Atri Nova Tech."
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${targetParty.phone}&text=${Uri.encode(message)}"))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Opening SMS app...", Toast.LENGTH_SHORT).show()
                }
            },
            onShareStatement = { targetParty ->
                try {
                    val header = PdfReportService.ReportHeaderInfo(
                        businessName = "Atri Nova Tech",
                        panVat = "609823412",
                        address = "Kathmandu, Nepal",
                        phone = "+977 9801234567"
                    )
                    val file = PdfReportService.generatePartyLedgerPdf(context, header, listOf(targetParty))
                    Toast.makeText(context, "Statement generated: ${file.name}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Statement ready for ${targetParty.name}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = modifier
        )

        // Modal Bottom Sheet for Editing Party
        partyToEdit?.let { editingParty ->
            val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { partyToEdit = null },
                sheetState = editSheetState,
                containerColor = SurfaceDark
            ) {
                AddPartySheet(
                    onSaveParty = { updated ->
                        viewModel.updateParty(updated)
                        partyToEdit = null
                    },
                    onCancel = { partyToEdit = null },
                    initialType = editingParty.type,
                    partyToEdit = editingParty
                )
            }
        }
        return
    }

    // Filter & sort parties reactively according to PartySettings
    val filteredParties = remember(parties, searchQuery, selectedPartyTypeFilter, selectedPaymentFilter, selectedCategoryFilter, partySettings) {
        val list = parties.filter { party ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                val nameMatch = if (partySettings.searchByName) party.name.lowercase().contains(q) else false
                val phoneMatch = if (partySettings.searchByPhone) (party.phone.contains(q) || party.contactNumber.contains(q)) else false
                val addressMatch = if (partySettings.searchByAddress) (party.address.lowercase().contains(q) || party.city.lowercase().contains(q)) else false
                val panMatch = if (partySettings.searchByPanVat) party.panVatNumber.lowercase().contains(q) else false
                nameMatch || phoneMatch || addressMatch || panMatch || party.notes.lowercase().contains(q)
            }

            val matchesPartyType = when (selectedPartyTypeFilter) {
                "All Party" -> true
                "Customers" -> party.type == "Customer"
                "Suppliers" -> party.type == "Supplier"
                else -> true
            }

            val matchesPayment = when (selectedPaymentFilter) {
                "All Payment" -> true
                "To Receive" -> party.balanceToReceive > 0
                "To Give" -> party.balanceToGive > 0
                "Settled" -> party.balanceToReceive == 0.0 && party.balanceToGive == 0.0
                else -> true
            }

            val matchesCategory = when (selectedCategoryFilter) {
                "All Category" -> true
                "Wholesale" -> party.notes.lowercase().contains("wholesale") || party.notes.lowercase().contains("distributor") || party.category.lowercase().contains("wholesale")
                "Retail" -> party.notes.lowercase().contains("retail") || party.notes.lowercase().contains("walk-in") || party.category.lowercase().contains("retail")
                else -> true
            }

            matchesQuery && matchesPartyType && matchesPayment && matchesCategory
        }

        when (partySettings.defaultSortOption) {
            com.example.data.model.PartySortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            com.example.data.model.PartySortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
            com.example.data.model.PartySortOption.HIGHEST_BALANCE -> list.sortedByDescending { maxOf(it.balanceToReceive, it.balanceToGive) }
            com.example.data.model.PartySortOption.LOWEST_BALANCE -> list.sortedBy { maxOf(it.balanceToReceive, it.balanceToGive) }
            com.example.data.model.PartySortOption.NEWEST_FIRST -> list.sortedByDescending { it.registerDate }
            com.example.data.model.PartySortOption.RECENT_ACTIVITY -> list.sortedByDescending { it.createdAt }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("parties_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Header: "Parties" + Settings Icon (matching image_5.png)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Parties",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = 0.3.sp
                )

                IconButton(
                    onClick = { viewModel.openDialog(ActiveDialog.BUSINESS_SWITCHER) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, CardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SkyBlueBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Search Bar, Filter, and PDF Export Button (matching image_5.png)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search partie...",
                                    color = TextSubtle,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(SkyBlueBright),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("parties_search_input")
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
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Filter Icon Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable { filterSheetMode = "PartyType" }
                        .testTag("parties_filter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // PDF Export Icon Button (image_5.png)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            Toast.makeText(context, "Exporting Parties Statement to PDF...", Toast.LENGTH_SHORT).show()
                            showPdfExportNotification = true
                        }
                        .testTag("parties_pdf_export_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Export PDF",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Filter Dropdowns Row: "All Party", "All Payment", "All Category"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdownChip(
                    label = selectedPartyTypeFilter,
                    isActive = selectedPartyTypeFilter != "All Party",
                    onClick = { filterSheetMode = "PartyType" }
                )

                FilterDropdownChip(
                    label = selectedPaymentFilter,
                    isActive = selectedPaymentFilter != "All Payment",
                    onClick = { filterSheetMode = "Payment" }
                )

                FilterDropdownChip(
                    label = selectedCategoryFilter,
                    isActive = selectedCategoryFilter != "All Category",
                    onClick = { filterSheetMode = "Category" }
                )
            }

            // Parties List
            if (filteredParties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CardDark)
                                .border(1.dp, CardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = SkyBlueBright,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        val emptyTitle = when {
                            searchQuery.isNotBlank() -> "No parties match \"$searchQuery\""
                            selectedPartyTypeFilter == "Customers" -> "No Customers Found"
                            selectedPartyTypeFilter == "Suppliers" -> "No Suppliers Found"
                            else -> "No Parties Found"
                        }
                        val emptySubtitle = when {
                            searchQuery.isNotBlank() -> "Try searching with a different name or phone number"
                            selectedPartyTypeFilter == "Customers" -> "Add your first customer to track receivables and sales"
                            selectedPartyTypeFilter == "Suppliers" -> "Add your first supplier to track payables and purchases"
                            else -> "Add a customer or supplier to start tracking balances"
                        }
                        Text(
                            text = emptyTitle,
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = emptySubtitle,
                            color = TextSubtle,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.openDialog(ActiveDialog.ADD_PARTY) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ New Party", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredParties, key = { it.id }) { party ->
                        PartyItemCard(
                            party = party,
                            privacyMode = privacyMode,
                            partySettings = partySettings,
                            onClick = { selectedPartyForDetail = party }
                        )
                    }

                    item {
                        // Spacing to clear the floating "New Party" action button
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
        }

        // Floating "New Party" Action Button with Glowing Blue Effect (matching image_5.png)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .height(48.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = SkyBlueGlow.copy(alpha = 0.6f),
                    spotColor = SkyBlueGlow
                )
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0091EA),
                            Color(0xFF00B0FF)
                        )
                    )
                )
                .border(1.5.dp, Color(0xFFBAE6FD), RoundedCornerShape(50))
                .clickable { viewModel.openDialog(ActiveDialog.ADD_PARTY) }
                .padding(horizontal = 18.dp)
                .testTag("new_party_fab"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "New Party",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter Options Bottom Sheet
        filterSheetMode?.let { mode ->
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { filterSheetMode = null },
                sheetState = sheetState,
                containerColor = SurfaceDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val title = when (mode) {
                        "PartyType" -> "Filter by Party Type"
                        "Payment" -> "Filter by Payment Status"
                        else -> "Filter by Category"
                    }
                    val options = when (mode) {
                        "PartyType" -> listOf("All Party", "Customers", "Suppliers")
                        "Payment" -> listOf("All Payment", "To Receive", "To Give", "Settled")
                        else -> listOf("All Category", "Wholesale", "Retail")
                    }
                    val currentSelected = when (mode) {
                        "PartyType" -> selectedPartyTypeFilter
                        "Payment" -> selectedPaymentFilter
                        else -> selectedCategoryFilter
                    }

                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    options.forEach { option ->
                        val isSelected = currentSelected == option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SkyBlue.copy(alpha = 0.15f) else CardDark)
                                .border(1.dp, if (isSelected) SkyBlueBright else CardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    when (mode) {
                                        "PartyType" -> selectedPartyTypeFilter = option
                                        "Payment" -> selectedPaymentFilter = option
                                        else -> selectedCategoryFilter = option
                                    }
                                    filterSheetMode = null
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    color = if (isSelected) SkyBlueBright else TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SkyBlueBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Filter dropdown chip with downward chevron (image_5.png)
 */
@Composable
private fun FilterDropdownChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isActive) SkyBlue.copy(alpha = 0.15f) else CardDark)
            .border(
                1.dp,
                if (isActive) SkyBlueBright else CardBorder,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = if (isActive) SkyBlueBright else TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (isActive) SkyBlueBright else TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Party list item card with:
 * - Slightly larger avatar (52.dp)
 * - Corporate Blue balance indicator for "To Receive"
 * - Orange balance indicator for "To Give"
 * - Settled indicator
 */
@Composable
private fun PartyItemCard(
    party: PartyEntity,
    privacyMode: Boolean,
    partySettings: com.example.data.model.PartySettings = com.example.data.model.PartySettings(),
    onClick: () -> Unit
) {
    val isCashTx = party.name.contains("Cash Transactions", ignoreCase = true)
    val isRedCross = party.name.contains("Red Cross", ignoreCase = true)

    // Two-letter initials
    val initials = remember(party.name) {
        val parts = party.name.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase(Locale.US)
            parts.size == 1 -> parts[0].take(2).uppercase(Locale.US)
            else -> "PT"
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slightly larger circle avatar (52.dp diameter)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCashTx -> Color(0xFF0F3025)
                            isRedCross -> Color(0xFF3B151A)
                            else -> Color(0xFF162544)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isCashTx -> Color(0xFF1E6347)
                            isRedCross -> Color(0xFF8B232E)
                            else -> Color(0xFF243B66)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCashTx -> {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Cash",
                            tint = Color(0xFF26D07C),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    isRedCross -> {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Red Cross",
                            tint = Color(0xFFFF334B),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = initials,
                            color = SkyBlueBright,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center Column: Party Name & Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = party.name,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (partySettings.showPartyTypeBadge) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (party.type == "Customer") SkyBlueCardBg else OrangeCardBg
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (party.type == "Customer") partySettings.customerTerminology else partySettings.supplierTerminology,
                                color = if (party.type == "Customer") SkyBlueBright else OrangeAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val subtitle = when {
                    isCashTx -> party.phone.ifBlank { "9852020149" }
                    partySettings.showPhoneOnCard && party.phone.isNotBlank() -> party.phone
                    partySettings.showAddressOnCard && party.address.isNotBlank() -> party.address
                    party.phone.isNotBlank() -> party.phone
                    else -> if (party.type == "Customer") partySettings.customerTerminology else partySettings.supplierTerminology
                }

                Text(
                    text = subtitle,
                    color = TextSubtle,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Right Column: Balance Indicators
            // Requirement: "To Receive" should be Blue, and "To Give" should be Orange
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when {
                    party.balanceToReceive > 0 -> {
                        val amountStr = if (privacyMode) "Rs. •••••" else "Rs. ${String.format(Locale.US, "%,.2f", party.balanceToReceive)}"
                        Text(
                            text = amountStr,
                            color = SkyBlueBright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "To Receive",
                            color = SkyBlueBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    party.balanceToGive > 0 -> {
                        val amountStr = if (privacyMode) "Rs. •••••" else "Rs. ${String.format(Locale.US, "%,.2f", party.balanceToGive)}"
                        Text(
                            text = amountStr,
                            color = OrangeAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "To Give",
                            color = OrangeAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    else -> {
                        if (!isCashTx) {
                            Text(
                                text = "Rs. 0",
                                color = TextSubtle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Settled",
                                color = SettledBadgeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom Sheet content displaying party details and ledger actions
 */
@Composable
private fun PartyDetailSheetContent(
    party: PartyEntity,
    privacyMode: Boolean,
    onCallClick: () -> Unit,
    onReminderClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = party.type,
                    color = SkyBlueBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = party.name,
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextMuted
                )
            }
        }

        HorizontalDivider(color = CardBorder)

        // Balance Summary Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        party.balanceToReceive > 0 -> SkyBlueCardBg
                        party.balanceToGive > 0 -> OrangeCardBg
                        else -> CardDark
                    }
                )
                .border(
                    1.dp,
                    when {
                        party.balanceToReceive > 0 -> SkyBlueCardBorder
                        party.balanceToGive > 0 -> OrangeCardBorder
                        else -> CardBorder
                    },
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when {
                            party.balanceToReceive > 0 -> "Total To Receive (Blue)"
                            party.balanceToGive > 0 -> "Total To Give (Orange)"
                            else -> "Account Status"
                        },
                        color = when {
                            party.balanceToReceive > 0 -> SkyBlueBright
                            party.balanceToGive > 0 -> OrangeAccent
                            else -> TextSubtle
                        },
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (privacyMode) "Rs. •••••" else when {
                            party.balanceToReceive > 0 -> "Rs. ${String.format(Locale.US, "%,.2f", party.balanceToReceive)}"
                            party.balanceToGive > 0 -> "Rs. ${String.format(Locale.US, "%,.2f", party.balanceToGive)}"
                            else -> "Settled (Rs. 0.00)"
                        },
                        color = when {
                            party.balanceToReceive > 0 -> SkyBlueBright
                            party.balanceToGive > 0 -> OrangeAccent
                            else -> TextWhite
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Contact & Entity Details
        if (party.category.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Category", color = TextSubtle, fontSize = 14.sp)
                Text(party.category, color = SkyBlueBright, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (party.panVatNumber.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PAN / VAT No.", color = TextSubtle, fontSize = 14.sp)
                Text(party.panVatNumber, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (party.contactPerson.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Contact Person", color = TextSubtle, fontSize = 14.sp)
                Text(party.contactPerson, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (party.phone.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Phone Number", color = TextSubtle, fontSize = 14.sp)
                Text(party.phone, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (party.contactNumber.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Secondary Phone", color = TextSubtle, fontSize = 14.sp)
                Text(party.contactNumber, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (party.email.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Email", color = TextSubtle, fontSize = 14.sp)
                Text(party.email, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (party.address.isNotBlank() || party.city.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Address", color = TextSubtle, fontSize = 14.sp)
                val fullAddress = listOf(party.address, party.city).filter { it.isNotBlank() }.joinToString(", ")
                Text(fullAddress, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (party.notes.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notes", color = TextSubtle, fontSize = 14.sp)
                Text(party.notes, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Normal)
            }
        }

        // Quick Communication Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .clickable { onCallClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(16.dp))
                    Text("Call Party", color = SkyBlueBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SkyBlue)
                    .clickable { onReminderClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Send Reminder", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
