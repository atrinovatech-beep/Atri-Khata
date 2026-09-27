package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PartyEntity
import com.example.service.BulkImportService
import com.example.service.ImportResultSummary
import com.example.service.ItemImportParsedRow
import com.example.service.PartyImportParsedRow
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.RedBadge
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite

@Composable
fun BulkImportScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onClose()
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Parties", "Items")

    val parties by viewModel.allParties.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.allInventoryItems.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("bulk_import_screen_root")
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("bulk_import_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bulk Import",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Upload Parties & Items from Excel / CSV",
                        color = SkyBlueBright,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("bulk_import_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSubtle
                    )
                }
            }
        }

        // Dual-Tab Navigation (Parties vs Items)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SurfaceDark,
            contentColor = SkyBlueBright,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = SkyBlueBright
                )
            },
            divider = {
                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                val count = if (index == 0) parties.size else inventoryItems.size
                val icon = if (index == 0) Icons.Outlined.Group else Icons.Outlined.Inventory2

                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.testTag("bulk_import_tab_$title"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSelected) SkyBlueBright else TextSubtle
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                color = if (isSelected) TextWhite else TextSubtle,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) SkyBlue.copy(alpha = 0.2f) else CardDark)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$count in DB",
                                    color = if (isSelected) SkyBlueBright else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> PartiesBulkImportTab(
                    viewModel = viewModel,
                    existingParties = parties,
                    onClose = onClose
                )
                1 -> ItemsBulkImportTab(
                    viewModel = viewModel,
                    existingItems = inventoryItems,
                    onClose = onClose
                )
            }
        }
    }
}

/**
 * ============================================================================
 * PARTIES BULK IMPORT TAB
 * ============================================================================
 */
@Composable
private fun PartiesBulkImportTab(
    viewModel: MainViewModel,
    existingParties: List<PartyEntity>,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    var fileName by remember { mutableStateOf<String?>(null) }
    var rawCsvContent by remember { mutableStateOf<String?>(null) }
    var headers by remember { mutableStateOf<List<String>>(emptyList()) }
    var rawRows by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var columnMapping by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var parsedRows by remember { mutableStateOf<List<PartyImportParsedRow>>(emptyList()) }

    var updateExistingDuplicates by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportResultSummary?>(null) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showMappingDetails by remember { mutableStateOf(false) }

    // System fields definition for Parties
    val systemFields = listOf(
        "name" to "Party Name (Required)",
        "phone" to "Phone Number",
        "type" to "Type (Customer/Supplier)",
        "openingBalance" to "Opening Balance",
        "balanceType" to "Balance Type (Dr/Cr)",
        "category" to "Category",
        "panVatNumber" to "PAN/VAT Number",
        "city" to "City",
        "address" to "Address",
        "contactNumber" to "Alt Contact Number",
        "email" to "Email Address",
        "creditLimit" to "Credit Limit",
        "notes" to "Notes"
    )

    fun reprocess(newHeaders: List<String>, newRows: List<List<String>>, newMapping: Map<String, String>) {
        headers = newHeaders
        rawRows = newRows
        columnMapping = newMapping
        parsedRows = BulkImportService.processParties(newHeaders, newRows, newMapping, existingParties)
    }

    // System file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (content.isNotBlank()) {
                    fileName = uri.lastPathSegment ?: "parties_import.csv"
                    rawCsvContent = content
                    val (h, r) = BulkImportService.parseDelimitedText(content)
                    val mapping = BulkImportService.suggestPartyMapping(h)
                    reprocess(h, r, mapping)
                    Toast.makeText(context, "Loaded ${r.size} records", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Selected file is empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Recalculate parsed rows whenever existingParties changes
    LaunchedEffect(existingParties) {
        if (headers.isNotEmpty() && rawRows.isNotEmpty()) {
            parsedRows = BulkImportService.processParties(headers, rawRows, columnMapping, existingParties)
        }
    }

    val validCount = parsedRows.count { it.isValid }
    val duplicateCount = parsedRows.count { it.isDuplicate }
    val invalidCount = parsedRows.count { !it.isValid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("parties_import_list")
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            // File selection and source card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SkyBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.UploadFile,
                                contentDescription = null,
                                tint = SkyBlueBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select Party File",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Supports CSV, TSV or Excel exported comma-separated tables",
                                color = TextSubtle,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("parties_choose_file_btn")
                        ) {
                            Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose File", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                val sample = BulkImportService.getSamplePartiesCsv()
                                fileName = "sample_parties.csv"
                                rawCsvContent = sample
                                val (h, r) = BulkImportService.parseDelimitedText(sample)
                                val mapping = BulkImportService.suggestPartyMapping(h)
                                reprocess(h, r, mapping)
                                Toast.makeText(context, "Sample template loaded", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBlueBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("parties_load_sample_btn")
                        ) {
                            Text("Sample", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showPasteDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBlueBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("parties_paste_csv_btn")
                        ) {
                            Icon(Icons.Outlined.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste", fontSize = 13.sp)
                        }
                    }

                    if (fileName != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$fileName (${rawRows.size} rows)",
                                color = TextWhite,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    showMappingDetails = !showMappingDetails
                                },
                                modifier = Modifier.testTag("parties_toggle_mapping_btn")
                            ) {
                                Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = SkyBlueBright)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showMappingDetails) "Hide Mapping" else "Map Columns", fontSize = 12.sp, color = SkyBlueBright)
                            }
                        }
                    }
                }
            }

            // Column Mapping Section (Collapsible)
            AnimatedVisibility(visible = showMappingDetails && headers.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Column Mapping", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            TextButton(
                                onClick = {
                                    val auto = BulkImportService.suggestPartyMapping(headers)
                                    reprocess(headers, rawRows, auto)
                                    Toast.makeText(context, "Mappings auto-detected", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = SkyBlueBright)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto Detect", fontSize = 12.sp, color = SkyBlueBright)
                            }
                        }

                        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                        systemFields.forEach { (fieldKey, fieldLabel) ->
                            val currentMapped = columnMapping[fieldKey] ?: ""
                            var menuExpanded by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(fieldLabel, color = TextWhite, fontSize = 12.sp, modifier = Modifier.weight(1.2f))

                                Box(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, if (currentMapped.isNotBlank()) SkyBlue.copy(alpha = 0.5f) else CardBorder, RoundedCornerShape(6.dp))
                                            .clickable { menuExpanded = true }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = currentMapped.ifBlank { "-- Skip / None --" },
                                            color = if (currentMapped.isNotBlank()) SkyBlueBright else TextMuted,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier.background(SurfaceDark)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("-- Skip / None --", color = TextMuted, fontSize = 12.sp) },
                                            onClick = {
                                                val newMap = columnMapping.toMutableMap()
                                                newMap.remove(fieldKey)
                                                menuExpanded = false
                                                reprocess(headers, rawRows, newMap)
                                            }
                                        )
                                        headers.forEach { h ->
                                            DropdownMenuItem(
                                                text = { Text(h, color = TextWhite, fontSize = 12.sp) },
                                                onClick = {
                                                    val newMap = columnMapping.toMutableMap()
                                                    newMap[fieldKey] = h
                                                    menuExpanded = false
                                                    reprocess(headers, rawRows, newMap)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duplicate Handling & Validation Summary
            if (parsedRows.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Duplicate Handling", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { updateExistingDuplicates = false },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !updateExistingDuplicates,
                                onClick = { updateExistingDuplicates = false },
                                colors = RadioButtonDefaults.colors(selectedColor = SkyBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Skip Duplicate Records", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Keep existing party records intact", color = TextSubtle, fontSize = 11.sp)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { updateExistingDuplicates = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = updateExistingDuplicates,
                                onClick = { updateExistingDuplicates = true },
                                colors = RadioButtonDefaults.colors(selectedColor = SkyBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Update Existing Records", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Overwrite fields with new CSV data for matched parties", color = TextSubtle, fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

                        // Status counts row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusBadge(label = "Total", count = parsedRows.size, color = TextWhite)
                            StatusBadge(label = "Valid", count = validCount, color = SkyBlueBright)
                            StatusBadge(label = "Duplicate", count = duplicateCount, color = AmberWarn)
                            StatusBadge(label = "Invalid", count = invalidCount, color = RedBadge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview Table Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Data Preview (${parsedRows.size} rows)",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Preview Table Rows
        if (parsedRows.isNotEmpty()) {
            items(parsedRows) { row ->
                PartyPreviewRowItem(
                    row = row,
                    updateExisting = updateExistingDuplicates
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Import Action Button
                val importableCount = if (updateExistingDuplicates) {
                    parsedRows.count { it.isValid }
                } else {
                    parsedRows.count { it.isValid && !it.isDuplicate }
                }

                Button(
                    onClick = {
                        isImporting = true
                        val toInsert = mutableListOf<PartyEntity>()
                        val toUpdate = mutableListOf<PartyEntity>()
                        var skipped = 0

                        parsedRows.forEach { r ->
                            if (!r.isValid) {
                                // Skip invalid
                            } else if (r.isDuplicate) {
                                if (updateExistingDuplicates && r.existingPartyId != null) {
                                    toUpdate.add(r.party.copy(id = r.existingPartyId))
                                } else {
                                    skipped++
                                }
                            } else {
                                toInsert.add(r.party.copy(id = 0L))
                            }
                        }

                        viewModel.bulkImportParties(toInsert, toUpdate) { ins, upd ->
                            isImporting = false
                            importResult = ImportResultSummary(
                                totalProcessed = parsedRows.size,
                                importedCount = ins,
                                updatedCount = upd,
                                skippedDuplicatesCount = skipped,
                                invalidCount = invalidCount
                            )
                        }
                    },
                    enabled = importableCount > 0 && !isImporting,
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("parties_import_action_btn")
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importing Records...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Outlined.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import $importableCount Parties",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Paste Dialog
    if (showPasteDialog) {
        PasteCsvDialog(
            title = "Paste Parties CSV / TSV",
            placeholder = "Party Name,Phone,Type,Opening Balance\nRam & Sons,9841000000,Customer,5000",
            onDismiss = { showPasteDialog = false },
            onConfirm = { text ->
                showPasteDialog = false
                if (text.isNotBlank()) {
                    fileName = "pasted_parties.csv"
                    rawCsvContent = text
                    val (h, r) = BulkImportService.parseDelimitedText(text)
                    val mapping = BulkImportService.suggestPartyMapping(h)
                    reprocess(h, r, mapping)
                    Toast.makeText(context, "Loaded ${r.size} records from pasted text", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Result Dialog
    importResult?.let { summary ->
        ImportResultDialog(
            summary = summary,
            type = "Parties",
            onDismiss = {
                importResult = null
                onClose()
            },
            onImportMore = {
                importResult = null
                fileName = null
                rawCsvContent = null
                headers = emptyList()
                rawRows = emptyList()
                parsedRows = emptyList()
            }
        )
    }
}

/**
 * ============================================================================
 * ITEMS BULK IMPORT TAB
 * ============================================================================
 */
@Composable
private fun ItemsBulkImportTab(
    viewModel: MainViewModel,
    existingItems: List<InventoryItemEntity>,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    var fileName by remember { mutableStateOf<String?>(null) }
    var rawCsvContent by remember { mutableStateOf<String?>(null) }
    var headers by remember { mutableStateOf<List<String>>(emptyList()) }
    var rawRows by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var columnMapping by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var parsedRows by remember { mutableStateOf<List<ItemImportParsedRow>>(emptyList()) }

    var updateExistingDuplicates by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<ImportResultSummary?>(null) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showMappingDetails by remember { mutableStateOf(false) }

    // System fields definition for Items
    val systemFields = listOf(
        "name" to "Item Name (Required)",
        "sku" to "SKU / Barcode",
        "category" to "Category",
        "stockQuantity" to "Stock Quantity",
        "unit" to "Unit (pcs/kg/box)",
        "purchasePrice" to "Purchase Price",
        "salePrice" to "Sale Price",
        "minStockAlert" to "Min Stock Alert"
    )

    fun reprocess(newHeaders: List<String>, newRows: List<List<String>>, newMapping: Map<String, String>) {
        headers = newHeaders
        rawRows = newRows
        columnMapping = newMapping
        parsedRows = BulkImportService.processItems(newHeaders, newRows, newMapping, existingItems)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (content.isNotBlank()) {
                    fileName = uri.lastPathSegment ?: "items_import.csv"
                    rawCsvContent = content
                    val (h, r) = BulkImportService.parseDelimitedText(content)
                    val mapping = BulkImportService.suggestItemMapping(h)
                    reprocess(h, r, mapping)
                    Toast.makeText(context, "Loaded ${r.size} item records", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Selected file is empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(existingItems) {
        if (headers.isNotEmpty() && rawRows.isNotEmpty()) {
            parsedRows = BulkImportService.processItems(headers, rawRows, columnMapping, existingItems)
        }
    }

    val validCount = parsedRows.count { it.isValid }
    val duplicateCount = parsedRows.count { it.isDuplicate }
    val invalidCount = parsedRows.count { !it.isValid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("items_import_list")
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            // File selection and source card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SkyBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = SkyBlueBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select Items File",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Upload inventory items with SKU, price, stock & category",
                                color = TextSubtle,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("items_choose_file_btn")
                        ) {
                            Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose File", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                val sample = BulkImportService.getSampleItemsCsv()
                                fileName = "sample_items.csv"
                                rawCsvContent = sample
                                val (h, r) = BulkImportService.parseDelimitedText(sample)
                                val mapping = BulkImportService.suggestItemMapping(h)
                                reprocess(h, r, mapping)
                                Toast.makeText(context, "Sample template loaded", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBlueBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("items_load_sample_btn")
                        ) {
                            Text("Sample", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showPasteDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBlueBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("items_paste_csv_btn")
                        ) {
                            Icon(Icons.Outlined.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste", fontSize = 13.sp)
                        }
                    }

                    if (fileName != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$fileName (${rawRows.size} rows)",
                                color = TextWhite,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    showMappingDetails = !showMappingDetails
                                },
                                modifier = Modifier.testTag("items_toggle_mapping_btn")
                            ) {
                                Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = SkyBlueBright)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showMappingDetails) "Hide Mapping" else "Map Columns", fontSize = 12.sp, color = SkyBlueBright)
                            }
                        }
                    }
                }
            }

            // Column Mapping Section (Collapsible)
            AnimatedVisibility(visible = showMappingDetails && headers.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Column Mapping", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            TextButton(
                                onClick = {
                                    val auto = BulkImportService.suggestItemMapping(headers)
                                    reprocess(headers, rawRows, auto)
                                    Toast.makeText(context, "Mappings auto-detected", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = SkyBlueBright)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto Detect", fontSize = 12.sp, color = SkyBlueBright)
                            }
                        }

                        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                        systemFields.forEach { (fieldKey, fieldLabel) ->
                            val currentMapped = columnMapping[fieldKey] ?: ""
                            var menuExpanded by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(fieldLabel, color = TextWhite, fontSize = 12.sp, modifier = Modifier.weight(1.2f))

                                Box(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, if (currentMapped.isNotBlank()) SkyBlue.copy(alpha = 0.5f) else CardBorder, RoundedCornerShape(6.dp))
                                            .clickable { menuExpanded = true }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = currentMapped.ifBlank { "-- Skip / None --" },
                                            color = if (currentMapped.isNotBlank()) SkyBlueBright else TextMuted,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier.background(SurfaceDark)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("-- Skip / None --", color = TextMuted, fontSize = 12.sp) },
                                            onClick = {
                                                val newMap = columnMapping.toMutableMap()
                                                newMap.remove(fieldKey)
                                                menuExpanded = false
                                                reprocess(headers, rawRows, newMap)
                                            }
                                        )
                                        headers.forEach { h ->
                                            DropdownMenuItem(
                                                text = { Text(h, color = TextWhite, fontSize = 12.sp) },
                                                onClick = {
                                                    val newMap = columnMapping.toMutableMap()
                                                    newMap[fieldKey] = h
                                                    menuExpanded = false
                                                    reprocess(headers, rawRows, newMap)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duplicate Handling & Validation Summary
            if (parsedRows.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Duplicate Handling", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { updateExistingDuplicates = false },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !updateExistingDuplicates,
                                onClick = { updateExistingDuplicates = false },
                                colors = RadioButtonDefaults.colors(selectedColor = SkyBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Skip Duplicate Items", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Keep existing item details intact", color = TextSubtle, fontSize = 11.sp)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { updateExistingDuplicates = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = updateExistingDuplicates,
                                onClick = { updateExistingDuplicates = true },
                                colors = RadioButtonDefaults.colors(selectedColor = SkyBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Update Existing Items", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Update price, stock and category for matching name/SKU", color = TextSubtle, fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

                        // Status counts row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusBadge(label = "Total", count = parsedRows.size, color = TextWhite)
                            StatusBadge(label = "Valid", count = validCount, color = SkyBlueBright)
                            StatusBadge(label = "Duplicate", count = duplicateCount, color = AmberWarn)
                            StatusBadge(label = "Invalid", count = invalidCount, color = RedBadge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview Table Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Data Preview (${parsedRows.size} items)",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Preview Table Rows
        if (parsedRows.isNotEmpty()) {
            items(parsedRows) { row ->
                ItemPreviewRowItem(
                    row = row,
                    updateExisting = updateExistingDuplicates
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                val importableCount = if (updateExistingDuplicates) {
                    parsedRows.count { it.isValid }
                } else {
                    parsedRows.count { it.isValid && !it.isDuplicate }
                }

                Button(
                    onClick = {
                        isImporting = true
                        val toInsert = mutableListOf<InventoryItemEntity>()
                        val toUpdate = mutableListOf<InventoryItemEntity>()
                        var skipped = 0

                        parsedRows.forEach { r ->
                            if (!r.isValid) {
                                // Skip invalid
                            } else if (r.isDuplicate) {
                                if (updateExistingDuplicates && r.existingItemId != null) {
                                    toUpdate.add(r.item.copy(id = r.existingItemId))
                                } else {
                                    skipped++
                                }
                            } else {
                                toInsert.add(r.item.copy(id = 0L))
                            }
                        }

                        viewModel.bulkImportItems(toInsert, toUpdate) { ins, upd ->
                            isImporting = false
                            importResult = ImportResultSummary(
                                totalProcessed = parsedRows.size,
                                importedCount = ins,
                                updatedCount = upd,
                                skippedDuplicatesCount = skipped,
                                invalidCount = invalidCount
                            )
                        }
                    },
                    enabled = importableCount > 0 && !isImporting,
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("items_import_action_btn")
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importing Items...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Outlined.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import $importableCount Items",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Paste Dialog
    if (showPasteDialog) {
        PasteCsvDialog(
            title = "Paste Items CSV / TSV",
            placeholder = "Item Name,SKU,Category,Stock Qty,Unit,Sale Price\nHP Toner,HP-1020,Printers,20,pcs,1800",
            onDismiss = { showPasteDialog = false },
            onConfirm = { text ->
                showPasteDialog = false
                if (text.isNotBlank()) {
                    fileName = "pasted_items.csv"
                    rawCsvContent = text
                    val (h, r) = BulkImportService.parseDelimitedText(text)
                    val mapping = BulkImportService.suggestItemMapping(h)
                    reprocess(h, r, mapping)
                    Toast.makeText(context, "Loaded ${r.size} items from pasted text", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Result Dialog
    importResult?.let { summary ->
        ImportResultDialog(
            summary = summary,
            type = "Items",
            onDismiss = {
                importResult = null
                onClose()
            },
            onImportMore = {
                importResult = null
                fileName = null
                rawCsvContent = null
                headers = emptyList()
                rawRows = emptyList()
                parsedRows = emptyList()
            }
        )
    }
}

/**
 * ============================================================================
 * PREVIEW ROW COMPONENTS
 * ============================================================================
 */
@Composable
private fun PartyPreviewRowItem(
    row: PartyImportParsedRow,
    updateExisting: Boolean
) {
    val borderColor = when {
        !row.isValid -> RedBadge.copy(alpha = 0.6f)
        row.isDuplicate -> AmberWarn.copy(alpha = 0.6f)
        else -> CardBorder
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${row.rowIndex}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = row.party.name.ifBlank { "(Empty Name)" },
                        color = if (row.party.name.isNotBlank()) TextWhite else RedBadge,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Row Status Chip
                when {
                    !row.isValid -> {
                        ChipBadge(text = "Invalid", bgColor = RedBadge.copy(alpha = 0.2f), textColor = RedBadge)
                    }
                    row.isDuplicate -> {
                        val label = if (updateExisting) "Update" else "Skip Duplicate"
                        ChipBadge(text = label, bgColor = AmberWarn.copy(alpha = 0.2f), textColor = AmberWarn)
                    }
                    else -> {
                        ChipBadge(text = "Valid", bgColor = SkyBlue.copy(alpha = 0.2f), textColor = SkyBlueBright)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Details line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (row.party.phone.isNotBlank()) {
                    Text("Phone: ${row.party.phone}", color = TextSubtle, fontSize = 11.sp)
                }
                Text("Type: ${row.party.type}", color = TextSubtle, fontSize = 11.sp)
                Text("Category: ${row.party.category}", color = TextSubtle, fontSize = 11.sp)
                if (row.party.openingBalance > 0) {
                    Text("Bal: Rs. ${row.party.openingBalance.toInt()} (${row.party.balanceType})", color = TextSubtle, fontSize = 11.sp)
                }
                if (row.party.panVatNumber.isNotBlank()) {
                    Text("PAN: ${row.party.panVatNumber}", color = TextSubtle, fontSize = 11.sp)
                }
                if (row.party.city.isNotBlank()) {
                    Text("City: ${row.party.city}", color = TextSubtle, fontSize = 11.sp)
                }
            }

            if (!row.isValid && row.validationError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = RedBadge, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(row.validationError, color = RedBadge, fontSize = 11.sp)
                }
            } else if (row.isDuplicate) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarn, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (updateExisting) "Matches existing party ID ${row.existingPartyId} (will be updated)" else "Matches existing party in database (will be skipped)",
                        color = AmberWarn,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemPreviewRowItem(
    row: ItemImportParsedRow,
    updateExisting: Boolean
) {
    val borderColor = when {
        !row.isValid -> RedBadge.copy(alpha = 0.6f)
        row.isDuplicate -> AmberWarn.copy(alpha = 0.6f)
        else -> CardBorder
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${row.rowIndex}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = row.item.name.ifBlank { "(Empty Item Name)" },
                        color = if (row.item.name.isNotBlank()) TextWhite else RedBadge,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                when {
                    !row.isValid -> {
                        ChipBadge(text = "Invalid", bgColor = RedBadge.copy(alpha = 0.2f), textColor = RedBadge)
                    }
                    row.isDuplicate -> {
                        val label = if (updateExisting) "Update" else "Skip Duplicate"
                        ChipBadge(text = label, bgColor = AmberWarn.copy(alpha = 0.2f), textColor = AmberWarn)
                    }
                    else -> {
                        ChipBadge(text = "Valid", bgColor = SkyBlue.copy(alpha = 0.2f), textColor = SkyBlueBright)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SKU: ${row.item.sku}", color = TextSubtle, fontSize = 11.sp)
                Text("Category: ${row.item.category}", color = TextSubtle, fontSize = 11.sp)
                Text("Sale: Rs. ${row.item.salePrice}", color = SkyBlueBright, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                if (row.item.purchasePrice > 0) {
                    Text("Buy: Rs. ${row.item.purchasePrice}", color = TextSubtle, fontSize = 11.sp)
                }
                Text("Stock: ${row.item.stockQuantity} ${row.item.unit}", color = TextSubtle, fontSize = 11.sp)
            }

            if (!row.isValid && row.validationError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = RedBadge, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(row.validationError, color = RedBadge, fontSize = 11.sp)
                }
            } else if (row.isDuplicate) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarn, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (updateExisting) "Matches existing item ID ${row.existingItemId} (will be updated)" else "Matches existing item name/SKU (will be skipped)",
                        color = AmberWarn,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * ============================================================================
 * HELPER DIALOGS AND WIDGETS
 * ============================================================================
 */
@Composable
private fun StatusBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = TextSubtle, fontSize = 11.sp)
    }
}

@Composable
private fun ChipBadge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PasteCsvDialog(
    title: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Paste comma or tab-separated data including header row:",
                    color = TextSubtle,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(placeholder, color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
            ) {
                Text("Process Data", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSubtle)
            }
        }
    )
}

@Composable
private fun ImportResultDialog(
    summary: ImportResultSummary,
    type: String,
    onDismiss: () -> Unit,
    onImportMore: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SkyBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SkyBlueBright,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "Import Completed!",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bulk operation for $type finished successfully.",
                    color = TextSubtle,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResultStatRow("Newly Imported", summary.importedCount.toString(), SkyBlueBright)
                        ResultStatRow("Updated Existing", summary.updatedCount.toString(), AmberWarn)
                        ResultStatRow("Duplicates Skipped", summary.skippedDuplicatesCount.toString(), TextSubtle)
                        if (summary.invalidCount > 0) {
                            ResultStatRow("Invalid (Skipped)", summary.invalidCount.toString(), RedBadge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                modifier = Modifier.testTag("import_result_done_btn")
            ) {
                Text("Done", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onImportMore,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBlueBright),
                modifier = Modifier.testTag("import_result_more_btn")
            ) {
                Text("Import More")
            }
        }
    )
}

@Composable
private fun ResultStatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextWhite, fontSize = 13.sp)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
