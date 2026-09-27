package com.example.ui.screens.inventory

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InventoryItemEntity
import com.example.ui.theme.AppTheme
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditInventoryItemSheet(
    itemToEdit: InventoryItemEntity? = null,
    existingCategories: List<String> = emptyList(),
    defaultUnit: String = "pcs",
    defaultLowStock: Double = 5.0,
    onSave: (
        name: String,
        sku: String,
        category: String,
        quantity: Double,
        unit: String,
        purchasePrice: Double,
        salePrice: Double,
        minStock: Double
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = AppTheme.isDark
    val colors = AppTheme.colors

    var name by remember(itemToEdit) { mutableStateOf(itemToEdit?.name ?: "") }
    var sku by remember(itemToEdit) { mutableStateOf(itemToEdit?.sku ?: "") }
    var category by remember(itemToEdit) {
        mutableStateOf(itemToEdit?.category?.ifBlank { "General" } ?: "General")
    }
    var unit by remember(itemToEdit) {
        mutableStateOf(itemToEdit?.unit?.ifBlank { defaultUnit } ?: defaultUnit)
    }
    var quantityText by remember(itemToEdit) {
        mutableStateOf(if (itemToEdit != null) {
            if (itemToEdit.stockQuantity % 1.0 == 0.0) itemToEdit.stockQuantity.toInt().toString()
            else itemToEdit.stockQuantity.toString()
        } else "")
    }
    var purchasePriceText by remember(itemToEdit) {
        mutableStateOf(if (itemToEdit != null && itemToEdit.purchasePrice > 0.0) {
            if (itemToEdit.purchasePrice % 1.0 == 0.0) itemToEdit.purchasePrice.toInt().toString()
            else itemToEdit.purchasePrice.toString()
        } else "")
    }
    var salePriceText by remember(itemToEdit) {
        mutableStateOf(if (itemToEdit != null && itemToEdit.salePrice > 0.0) {
            if (itemToEdit.salePrice % 1.0 == 0.0) itemToEdit.salePrice.toInt().toString()
            else itemToEdit.salePrice.toString()
        } else "")
    }
    var minStockText by remember(itemToEdit) {
        mutableStateOf(if (itemToEdit != null) {
            if (itemToEdit.minStockAlert % 1.0 == 0.0) itemToEdit.minStockAlert.toInt().toString()
            else itemToEdit.minStockAlert.toString()
        } else if (defaultLowStock % 1.0 == 0.0) defaultLowStock.toInt().toString() else defaultLowStock.toString())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Derived margin calculation
    val purchaseVal = purchasePriceText.toDoubleOrNull() ?: 0.0
    val saleVal = salePriceText.toDoubleOrNull() ?: 0.0
    val marginVal = remember(purchaseVal, saleVal) { saleVal - purchaseVal }
    val marginPercent = remember(purchaseVal, saleVal) {
        if (purchaseVal > 0.0) (marginVal / purchaseVal) * 100.0 else 0.0
    }

    val isEditMode = itemToEdit != null
    val defaultUnits = listOf("pcs", "kg", "box", "litre", "meter", "packet", "dozen")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Outlined.Sell else Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = colors.primaryBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = if (isEditMode) "Edit Inventory Item" else "Add New Inventory Item",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = colors.cardBorder)
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFCA5A5),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Item Name (Required)
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (errorMessage != null) errorMessage = null
            },
            label = { Text("Product / Item Name *") },
            placeholder = { Text("e.g. Paracetamol 500mg, CAT6 Cable") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("item_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryBright,
                unfocusedBorderColor = colors.cardBorder,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedContainerColor = colors.cardBackground,
                unfocusedContainerColor = colors.cardBackground
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // SKU / Barcode & Category
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU / Barcode") },
                placeholder = { Text("e.g. MED-001") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("item_sku_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryBright,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.cardBackground,
                    unfocusedContainerColor = colors.cardBackground
                )
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                placeholder = { Text("e.g. General") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("item_category_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryBright,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.cardBackground,
                    unfocusedContainerColor = colors.cardBackground
                )
            )
        }

        // Category suggestions
        val categoriesToShow = remember(existingCategories) {
            (listOf("General", "Hardware", "Grocery", "Electronics", "Stationery", "Medicines") + existingCategories)
                .distinct()
                .filter { it.isNotBlank() }
                .take(6)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categoriesToShow.forEach { cat ->
                val isSelected = category.equals(cat, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.cardBackground)
                        .border(
                            1.dp,
                            if (isSelected) colors.primaryBright else colors.cardBorder,
                            RoundedCornerShape(50)
                        )
                        .clickable { category = cat }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) colors.primaryBright else colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stock Quantity & Unit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text(if (isEditMode) "Current Stock Qty" else "Opening Stock Qty") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("item_quantity_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryBright,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.cardBackground,
                    unfocusedContainerColor = colors.cardBackground
                )
            )

            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit") },
                placeholder = { Text("pcs") },
                singleLine = true,
                modifier = Modifier
                    .weight(0.8f)
                    .testTag("item_unit_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryBright,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.cardBackground,
                    unfocusedContainerColor = colors.cardBackground
                )
            )
        }

        // Quick Unit selection
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            defaultUnits.forEach { u ->
                val isSelected = unit.equals(u, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.cardBackground)
                        .border(
                            1.dp,
                            if (isSelected) colors.primaryBright else colors.cardBorder,
                            RoundedCornerShape(50)
                        )
                        .clickable { unit = u }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = u,
                        color = if (isSelected) colors.primaryBright else colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Purchase Price & Sale Price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = purchasePriceText,
                onValueChange = { purchasePriceText = it },
                label = { Text("Purchase Price (Rs.)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("item_purchase_price_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryBright,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.cardBackground,
                    unfocusedContainerColor = colors.cardBackground
                )
            )

            OutlinedTextField(
                value = salePriceText,
                onValueChange = { salePriceText = it },
                label = { Text("Sale Price (Rs.)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("item_sale_price_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryBright,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.cardBackground,
                    unfocusedContainerColor = colors.cardBackground
                )
            )
        }

        // Live Margin Card Preview
        if (saleVal > 0.0 || purchaseVal > 0.0) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Estimated Gross Profit", color = colors.textMuted, fontSize = 11.sp)
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%,.2f", marginVal)} / $unit",
                            color = if (marginVal >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (marginVal >= 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${if (marginVal >= 0) "+" else ""}${String.format(Locale.US, "%.1f", marginPercent)}% Margin",
                            color = if (marginVal >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Minimum Stock Alert
        OutlinedTextField(
            value = minStockText,
            onValueChange = { minStockText = it },
            label = { Text("Low Stock Alert Level") },
            placeholder = { Text("e.g. 5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("item_min_stock_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryBright,
                unfocusedBorderColor = colors.cardBorder,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedContainerColor = colors.cardBackground,
                unfocusedContainerColor = colors.cardBackground
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            ) {
                Text("Cancel", color = colors.textMuted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter an item name."
                        return@Button
                    }
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    val pPrice = purchasePriceText.toDoubleOrNull() ?: 0.0
                    val sPrice = salePriceText.toDoubleOrNull() ?: 0.0
                    val minStock = minStockText.toDoubleOrNull() ?: 5.0

                    onSave(
                        name.trim(),
                        sku.trim(),
                        category.trim().ifBlank { "General" },
                        qty,
                        unit.trim().ifBlank { "pcs" },
                        pPrice,
                        sPrice,
                        minStock
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBright),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("save_item_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEditMode) "Save Changes" else "Add Item",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
