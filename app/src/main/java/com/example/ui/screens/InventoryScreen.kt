package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.model.InventoryDashboardSummary
import com.example.data.model.InventorySortOrder
import com.example.data.model.StockStatus
import com.example.ui.ActiveDialog
import com.example.ui.MainViewModel
import com.example.ui.screens.inventory.AddEditInventoryItemSheet
import com.example.ui.screens.inventory.InventoryItemDetailSheet
import com.example.ui.screens.inventory.StockAdjustmentSheet
import com.example.ui.theme.AppTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val isDark = AppTheme.isDark

    val items by viewModel.allInventoryItems.collectAsStateWithLifecycle()
    val categoriesFromDb by viewModel.allInventoryCategories.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()
    val inventorySettings by viewModel.inventorySettings.collectAsStateWithLifecycle()
    val allowNegativeStock = inventorySettings.allowNegativeStock
    val defaultMeasurementUnit = inventorySettings.defaultMeasurementUnit
    val defaultLowStockThreshold = inventorySettings.defaultLowStockThreshold

    var searchQuery by remember { mutableStateOf("") }
    var selectedStockStatusFilter by remember { mutableStateOf<StockStatus?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var currentSortOrder by remember { mutableStateOf(InventorySortOrder.NAME_ASC) }

    // Dialog & Sheet States
    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItemEntity?>(null) }

    var showAdjustmentSheet by remember { mutableStateOf(false) }
    var adjustingItem by remember { mutableStateOf<InventoryItemEntity?>(null) }

    var selectedItemForDetail by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var showCategoryPickerSheet by remember { mutableStateOf(false) }
    var showSortPickerSheet by remember { mutableStateOf(false) }
    var showItemSelectorForAdjustment by remember { mutableStateOf(false) }

    // Dynamic Categories from real data
    val allCategories = remember(items, categoriesFromDb) {
        val set = sortedSetOf<String>()
        set.addAll(categoriesFromDb.filter { it.isNotBlank() })
        items.forEach { if (it.category.isNotBlank()) set.add(it.category) }
        set.toList()
    }

    // Reactive Dashboard Summary KPIs from Room database
    val summary = remember(items) {
        var totalQty = 0.0
        var totalVal = 0.0
        var lowCount = 0
        var outCount = 0
        val cats = mutableSetOf<String>()

        items.forEach { item ->
            totalQty += item.stockQuantity
            totalVal += (item.stockQuantity * item.purchasePrice)
            if (item.category.isNotBlank()) cats.add(item.category)
            when (StockStatus.fromItem(item)) {
                StockStatus.OUT_OF_STOCK -> outCount++
                StockStatus.LOW_STOCK -> lowCount++
                StockStatus.IN_STOCK -> Unit
            }
        }

        InventoryDashboardSummary(
            totalItems = items.size,
            totalStockQuantity = totalQty,
            totalStockValue = totalVal,
            lowStockCount = lowCount,
            outOfStockCount = outCount,
            categoriesCount = cats.size
        )
    }

    // Filtered & Sorted items reactively
    val filteredItems = remember(
        items,
        searchQuery,
        selectedStockStatusFilter,
        selectedCategoryFilter,
        currentSortOrder
    ) {
        val q = searchQuery.trim().lowercase()
        items.filter { item ->
            val matchesQuery = if (q.isBlank()) true else {
                item.name.lowercase().contains(q) ||
                        item.sku.lowercase().contains(q) ||
                        item.category.lowercase().contains(q)
            }

            val matchesStock = when (selectedStockStatusFilter) {
                null -> true
                StockStatus.IN_STOCK -> item.stockQuantity > item.minStockAlert
                StockStatus.LOW_STOCK -> item.stockQuantity > 0.0 && item.stockQuantity <= item.minStockAlert
                StockStatus.OUT_OF_STOCK -> item.stockQuantity <= 0.0
            }

            val matchesCategory = when (selectedCategoryFilter) {
                null, "All Categories" -> true
                else -> item.category.equals(selectedCategoryFilter, ignoreCase = true)
            }

            matchesQuery && matchesStock && matchesCategory
        }.let { list ->
            when (currentSortOrder) {
                InventorySortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                InventorySortOrder.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
                InventorySortOrder.STOCK_LOW_HIGH -> list.sortedBy { it.stockQuantity }
                InventorySortOrder.STOCK_HIGH_LOW -> list.sortedByDescending { it.stockQuantity }
                InventorySortOrder.PRICE_LOW_HIGH -> list.sortedBy { it.salePrice }
                InventorySortOrder.PRICE_HIGH_LOW -> list.sortedByDescending { it.salePrice }
                InventorySortOrder.RECENTLY_ADDED -> list.sortedByDescending { it.createdAt }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("inventory_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Bar
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Inventory",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = "Stock Overview & Item Catalog",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                editingItem = null
                                showAddEditSheet = true
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.cardBackground)
                                .border(1.dp, colors.cardBorder, CircleShape)
                                .testTag("inventory_add_header_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Item",
                                tint = colors.primaryBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openDialog(ActiveDialog.SETTINGS) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.cardBackground)
                                .border(1.dp, colors.cardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = colors.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Notice banner if inventory module is turned off in Settings
            if (!inventorySettings.inventoryModuleEnabled) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Info, contentDescription = null, tint = colors.primaryBright, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Inventory Module Paused", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    Text("Catalog items and stock records are preserved. Reactivate in Settings.", fontSize = 11.sp, color = colors.textMuted)
                                }
                            }
                            Button(
                                onClick = { viewModel.updateInventorySettings { it.copy(inventoryModuleEnabled = true) } },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBright),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Enable", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // 2. Inventory KPI Dashboard Cards (Room-Backed, Interactive 1-tap filtering)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Hero Total Items Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                selectedStockStatusFilter = null
                                selectedCategoryFilter = null
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Inventory Items",
                                    fontSize = 12.sp,
                                    color = colors.textMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${summary.totalItems} Items",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Total Stock Qty: ${String.format(Locale.US, "%,.1f", summary.totalStockQuantity)} Units",
                                    fontSize = 12.sp,
                                    color = colors.primaryBright
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = colors.primaryBright,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // 2x2 Grid: Stock Value | Low Stock | Out of Stock | Categories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stock Value
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Stock Value", fontSize = 11.sp, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (!inventorySettings.showStockValue) "Hidden" else if (privacyMode) "Rs. •••••" else "Rs. ${String.format(Locale.US, "%,.0f", summary.totalStockValue)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(if (!inventorySettings.showStockValue) "Disabled in Settings" else "at purchase cost", fontSize = 10.sp, color = colors.textSubtle)
                            }
                        }

                        // Low Stock (Interactive filter)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedStockStatusFilter == StockStatus.LOW_STOCK)
                                    Color(0xFFF59E0B).copy(alpha = 0.12f) else colors.cardBackground
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (selectedStockStatusFilter == StockStatus.LOW_STOCK) Color(0xFFF59E0B) else colors.cardBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedStockStatusFilter = if (selectedStockStatusFilter == StockStatus.LOW_STOCK) null else StockStatus.LOW_STOCK
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Low Stock", fontSize = 11.sp, color = colors.textMuted)
                                    if (summary.lowStockCount > 0) {
                                        Icon(
                                            imageVector = Icons.Outlined.WarningAmber,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${summary.lowStockCount} Items",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.lowStockCount > 0) Color(0xFFF59E0B) else colors.textPrimary
                                )
                                Text("tap to filter", fontSize = 10.sp, color = colors.textSubtle)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Out of Stock (Interactive filter)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedStockStatusFilter == StockStatus.OUT_OF_STOCK)
                                    Color(0xFFEF4444).copy(alpha = 0.12f) else colors.cardBackground
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (selectedStockStatusFilter == StockStatus.OUT_OF_STOCK) Color(0xFFEF4444) else colors.cardBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedStockStatusFilter = if (selectedStockStatusFilter == StockStatus.OUT_OF_STOCK) null else StockStatus.OUT_OF_STOCK
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Out of Stock", fontSize = 11.sp, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${summary.outOfStockCount} Items",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.outOfStockCount > 0) Color(0xFFEF4444) else colors.textPrimary
                                )
                                Text("tap to filter", fontSize = 10.sp, color = colors.textSubtle)
                            }
                        }

                        // Categories
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                                .clickable { showCategoryPickerSheet = true }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Categories", fontSize = 11.sp, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${summary.categoriesCount} Distinct",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text("tap to view", fontSize = 10.sp, color = colors.textSubtle)
                            }
                        }
                    }
                }
            }

            // 3. Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // + Add Item Button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.primary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .clickable {
                                editingItem = null
                                showAddEditSheet = true
                            }
                            .testTag("quick_action_add_item")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = colors.primaryBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Item",
                                color = colors.primaryBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Stock Adjustment Button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                if (!inventorySettings.allowManualStockAdjustment) {
                                    Toast.makeText(context, "Manual Stock Adjustment is disabled in Settings", Toast.LENGTH_SHORT).show()
                                } else if (items.isEmpty()) {
                                    Toast.makeText(context, "Please add an inventory item first", Toast.LENGTH_SHORT).show()
                                } else {
                                    showItemSelectorForAdjustment = true
                                }
                            }
                            .testTag("quick_action_stock_adjustment")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SyncAlt,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Stock Adjustment",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 4. Search Bar & Sort / Category Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Input
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cardBackground)
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
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
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search product, SKU, category...",
                                        color = colors.textSubtle,
                                        fontSize = 13.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(colors.primaryBright),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("inventory_search_input")
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
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Sort Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cardBackground)
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                            .clickable { showSortPickerSheet = true }
                            .testTag("inventory_sort_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = colors.primaryBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Category Filter Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedCategoryFilter != null) colors.primary.copy(alpha = 0.2f) else colors.cardBackground)
                            .border(
                                1.dp,
                                if (selectedCategoryFilter != null) colors.primaryBright else colors.cardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { showCategoryPickerSheet = true }
                            .testTag("inventory_filter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (selectedCategoryFilter != null) colors.primaryBright else colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 5. Stock Status Filter Chips (All | In Stock | Low Stock | Out of Stock)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All Chip
                    val isAllSelected = selectedStockStatusFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isAllSelected) colors.primary.copy(alpha = 0.2f) else colors.cardBackground)
                            .border(
                                1.dp,
                                if (isAllSelected) colors.primaryBright else colors.cardBorder,
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedStockStatusFilter = null }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "All Items (${items.size})",
                            color = if (isAllSelected) colors.primaryBright else colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // In Stock Chip
                    val isInStockSelected = selectedStockStatusFilter == StockStatus.IN_STOCK
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isInStockSelected) Color(0xFF10B981).copy(alpha = 0.2f) else colors.cardBackground)
                            .border(
                                1.dp,
                                if (isInStockSelected) Color(0xFF10B981) else colors.cardBorder,
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedStockStatusFilter = StockStatus.IN_STOCK }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "In Stock",
                            color = if (isInStockSelected) Color(0xFF10B981) else colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isInStockSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Low Stock Chip
                    val isLowStockSelected = selectedStockStatusFilter == StockStatus.LOW_STOCK
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isLowStockSelected) Color(0xFFF59E0B).copy(alpha = 0.2f) else colors.cardBackground)
                            .border(
                                1.dp,
                                if (isLowStockSelected) Color(0xFFF59E0B) else colors.cardBorder,
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedStockStatusFilter = StockStatus.LOW_STOCK }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Low Stock (${summary.lowStockCount})",
                            color = if (isLowStockSelected) Color(0xFFF59E0B) else colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isLowStockSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Out of Stock Chip
                    val isOutOfStockSelected = selectedStockStatusFilter == StockStatus.OUT_OF_STOCK
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isOutOfStockSelected) Color(0xFFEF4444).copy(alpha = 0.2f) else colors.cardBackground)
                            .border(
                                1.dp,
                                if (isOutOfStockSelected) Color(0xFFEF4444) else colors.cardBorder,
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedStockStatusFilter = StockStatus.OUT_OF_STOCK }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Out of Stock (${summary.outOfStockCount})",
                            color = if (isOutOfStockSelected) Color(0xFFEF4444) else colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isOutOfStockSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Active Category Chip (if filtered)
                    if (selectedCategoryFilter != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(colors.primary.copy(alpha = 0.2f))
                                .border(1.dp, colors.primaryBright, RoundedCornerShape(50))
                                .clickable { selectedCategoryFilter = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Category: $selectedCategoryFilter",
                                    color = colors.primaryBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = colors.primaryBright,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Section Title & Results Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Items (${filteredItems.size})",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Sorted by: ${currentSortOrder.label}",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // 7. Inventory Items List
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(colors.cardBackground)
                                    .border(1.dp, colors.cardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = colors.primaryBright,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            val emptyTitle = if (searchQuery.isNotBlank() || selectedStockStatusFilter != null || selectedCategoryFilter != null)
                                "No matching items found"
                            else
                                "No Inventory Items"
                            val emptySubtitle = if (items.isEmpty())
                                "Add products/items to manage your stock"
                            else
                                "Try clearing filters or search query."

                            Text(
                                text = emptyTitle,
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = emptySubtitle,
                                color = colors.textMuted,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { showAddEditSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+ Add Item", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        privacyMode = privacyMode,
                        onClick = { selectedItemForDetail = item }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button for Adding New Item
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .height(48.dp)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(50))
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
                .clickable {
                    editingItem = null
                    showAddEditSheet = true
                }
                .padding(horizontal = 18.dp)
                .testTag("add_item_fab"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Add Item",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 1. ADD / EDIT Bottom Sheet
        if (showAddEditSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = {
                    showAddEditSheet = false
                    editingItem = null
                },
                sheetState = sheetState,
                containerColor = colors.surface
            ) {
                AddEditInventoryItemSheet(
                    itemToEdit = editingItem,
                    existingCategories = allCategories,
                    defaultUnit = defaultMeasurementUnit,
                    defaultLowStock = defaultLowStockThreshold.toDouble(),
                    onSave = { name, sku, category, qty, unit, pPrice, sPrice, minStock ->
                        val current = editingItem
                        if (current != null) {
                            val updated = current.copy(
                                name = name,
                                sku = sku,
                                category = category,
                                stockQuantity = qty,
                                unit = unit,
                                purchasePrice = pPrice,
                                salePrice = sPrice,
                                minStockAlert = minStock
                            )
                            viewModel.updateInventoryItem(
                                item = updated,
                                onSuccess = {
                                    Toast.makeText(context, "Item '$name' updated", Toast.LENGTH_SHORT).show()
                                    showAddEditSheet = false
                                    editingItem = null
                                    if (selectedItemForDetail?.id == updated.id) {
                                        selectedItemForDetail = updated
                                    }
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            viewModel.addInventoryItem(
                                name = name,
                                sku = sku,
                                category = category,
                                quantity = qty,
                                unit = unit,
                                purchasePrice = pPrice,
                                salePrice = sPrice,
                                minStock = minStock,
                                onSuccess = {
                                    Toast.makeText(context, "Item '$name' added", Toast.LENGTH_SHORT).show()
                                    showAddEditSheet = false
                                    editingItem = null
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    onDismiss = {
                        showAddEditSheet = false
                        editingItem = null
                    }
                )
            }
        }

        // 2. STOCK ADJUSTMENT Bottom Sheet
        if (showAdjustmentSheet && adjustingItem != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = {
                    showAdjustmentSheet = false
                    adjustingItem = null
                },
                sheetState = sheetState,
                containerColor = colors.surface
            ) {
                val target = adjustingItem!!
                StockAdjustmentSheet(
                    targetItem = target,
                    allowNegativeStock = inventorySettings.allowNegativeStock,
                    requireAdjustmentReason = inventorySettings.requireAdjustmentReason,
                    onAdjust = { newQuantity, reason ->
                        viewModel.adjustInventoryItemStock(
                            item = target,
                            newQuantity = newQuantity,
                            reason = reason,
                            onSuccess = {
                                Toast.makeText(context, "Stock updated to $newQuantity ${target.unit}", Toast.LENGTH_SHORT).show()
                                showAdjustmentSheet = false
                                if (selectedItemForDetail?.id == target.id) {
                                    selectedItemForDetail = target.copy(stockQuantity = newQuantity)
                                }
                                adjustingItem = null
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onDismiss = {
                        showAdjustmentSheet = false
                        adjustingItem = null
                    }
                )
            }
        }

        // 3. ITEM SELECTOR FOR ADJUSTMENT (when triggered from dashboard quick action)
        if (showItemSelectorForAdjustment) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showItemSelectorForAdjustment = false },
                sheetState = sheetState,
                containerColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Select Item to Adjust Stock",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items, key = { it.id }) { itm ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        showItemSelectorForAdjustment = false
                                        adjustingItem = itm
                                        showAdjustmentSheet = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(itm.name, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, fontSize = 14.sp)
                                        Text("${itm.category} • SKU: ${itm.sku}", color = colors.textMuted, fontSize = 11.sp)
                                    }
                                    Text(
                                        "${itm.stockQuantity} ${itm.unit}",
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryBright,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. ITEM DETAIL Bottom Sheet
        selectedItemForDetail?.let { item ->
            // Keep reactive to items list changes
            val latestItem = items.find { it.id == item.id } ?: item
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { selectedItemForDetail = null },
                sheetState = sheetState,
                containerColor = colors.surface
            ) {
                InventoryItemDetailSheet(
                    item = latestItem,
                    viewModel = viewModel,
                    onEdit = { itm ->
                        editingItem = itm
                        showAddEditSheet = true
                    },
                    onStockAdjust = { itm ->
                        adjustingItem = itm
                        showAdjustmentSheet = true
                    },
                    onDelete = { itm ->
                        viewModel.deleteInventoryItem(
                            item = itm,
                            onSuccess = {
                                Toast.makeText(context, "Item '${itm.name}' deleted", Toast.LENGTH_SHORT).show()
                                selectedItemForDetail = null
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onDismiss = { selectedItemForDetail = null }
                )
            }
        }

        // 5. SORT PICKER Bottom Sheet
        if (showSortPickerSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showSortPickerSheet = false },
                sheetState = sheetState,
                containerColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sort Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    InventorySortOrder.values().forEach { order ->
                        val isSelected = currentSortOrder == order
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.cardBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.primaryBright else colors.cardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    currentSortOrder = order
                                    showSortPickerSheet = false
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.label,
                                    color = if (isSelected) colors.primaryBright else colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.primaryBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // 6. CATEGORY PICKER Bottom Sheet
        if (showCategoryPickerSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showCategoryPickerSheet = false },
                sheetState = sheetState,
                containerColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Filter by Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    // All Categories Option
                    val isAllCats = selectedCategoryFilter == null
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAllCats) colors.primary.copy(alpha = 0.15f) else colors.cardBackground)
                            .border(
                                1.dp,
                                if (isAllCats) colors.primaryBright else colors.cardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedCategoryFilter = null
                                showCategoryPickerSheet = false
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Categories (${items.size})",
                                color = if (isAllCats) colors.primaryBright else colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isAllCats) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isAllCats) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.primaryBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Individual Categories with counts
                    allCategories.forEach { cat ->
                        val count = items.count { it.category.equals(cat, ignoreCase = true) }
                        val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.cardBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.primaryBright else colors.cardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedCategoryFilter = cat
                                    showCategoryPickerSheet = false
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$cat ($count)",
                                    color = if (isSelected) colors.primaryBright else colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.primaryBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

/**
 * Modern Item Card supporting both Light and Dark Themes,
 * displaying Name, Category, SKU, Quantity, Pricing, Stock Status badge
 */
@Composable
private fun InventoryItemCard(
    item: InventoryItemEntity,
    privacyMode: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val status = StockStatus.fromItem(item)

    val statusColor = when (status) {
        StockStatus.IN_STOCK -> Color(0xFF10B981)
        StockStatus.LOW_STOCK -> Color(0xFFF59E0B)
        StockStatus.OUT_OF_STOCK -> Color(0xFFEF4444)
    }

    val initials = remember(item.name) {
        val parts = item.name.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase(Locale.US)
            parts.size == 1 -> parts[0].take(2).uppercase(Locale.US)
            else -> "PR"
        }
    }

    val salesFormatted = if (privacyMode) "Rs. •••••" else "Rs. ${String.format(Locale.US, "%,.0f", item.salePrice)}"
    val purchaseFormatted = if (privacyMode) "Rs. •••••" else "Rs. ${String.format(Locale.US, "%,.0f", item.purchasePrice)}"

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("inventory_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product initial avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.primary.copy(alpha = 0.12f))
                    .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = colors.primaryBright,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Row 1: Item Name & Category / Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Stock Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor.copy(alpha = 0.12f))
                            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = status.label,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Row 2: SKU and Category
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SKU: ${item.sku.ifBlank { "N/A" }}  •  ${item.category}",
                    color = colors.textMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Row 3: Sales, Purchase, and Stock Quantity Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Sale price
                    Column {
                        Text("Sale", color = colors.textSubtle, fontSize = 10.sp)
                        Text(
                            text = salesFormatted,
                            color = colors.primaryBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Purchase price
                    Column {
                        Text("Purchase", color = colors.textSubtle, fontSize = 10.sp)
                        Text(
                            text = purchaseFormatted,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Stock Quantity
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Stock", color = colors.textSubtle, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(colors.primary.copy(alpha = 0.12f))
                                .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%,.1f", item.stockQuantity)} ${item.unit}",
                                color = colors.primaryBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
