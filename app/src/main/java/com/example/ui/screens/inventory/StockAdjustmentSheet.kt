package com.example.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.SyncAlt
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

enum class AdjustmentType {
    ADD,
    REDUCE,
    SET_EXACT
}

@Composable
fun StockAdjustmentSheet(
    targetItem: InventoryItemEntity,
    allowNegativeStock: Boolean = false,
    requireAdjustmentReason: Boolean = true,
    onAdjust: (newQuantity: Double, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    var adjustmentType by remember { mutableStateOf(AdjustmentType.ADD) }
    var quantityText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("Physical Audit") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentStock = targetItem.stockQuantity
    val qtyVal = quantityText.toDoubleOrNull() ?: 0.0

    val newStock = remember(adjustmentType, currentStock, qtyVal) {
        when (adjustmentType) {
            AdjustmentType.ADD -> currentStock + qtyVal
            AdjustmentType.REDUCE -> currentStock - qtyVal
            AdjustmentType.SET_EXACT -> qtyVal
        }
    }

    val quickReasons = listOf(
        "Physical Audit",
        "Stock In / Restock",
        "Damaged / Expired",
        "Returned Goods",
        "Transfer / Other"
    )

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
                        imageVector = Icons.Outlined.SyncAlt,
                        contentDescription = null,
                        tint = colors.primaryBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Stock Adjustment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = targetItem.name,
                        fontSize = 13.sp,
                        color = colors.primaryBright,
                        fontWeight = FontWeight.Medium
                    )
                }
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

        // Current Stock vs New Stock Preview Card
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
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current Stock", color = colors.textMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", currentStock)} ${targetItem.unit}",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.SyncAlt,
                    contentDescription = null,
                    tint = colors.primaryBright,
                    modifier = Modifier.size(20.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("New Stock", color = colors.textMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", newStock)} ${targetItem.unit}",
                        color = if (newStock < 0 && !allowNegativeStock) Color(0xFFEF4444) else colors.primaryBright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Adjustment Type Selector
        Text("Adjustment Mode", color = colors.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                AdjustmentType.ADD to "Add (+)",
                AdjustmentType.REDUCE to "Reduce (-)",
                AdjustmentType.SET_EXACT to "Set Exact (=)"
            ).forEach { (type, label) ->
                val isSelected = adjustmentType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.cardBackground)
                        .border(
                            1.dp,
                            if (isSelected) colors.primaryBright else colors.cardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            adjustmentType = type
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) colors.primaryBright else colors.textMuted,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFCA5A5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Quantity Input
        OutlinedTextField(
            value = quantityText,
            onValueChange = {
                quantityText = it
                errorMessage = null
            },
            label = {
                Text(when (adjustmentType) {
                    AdjustmentType.ADD -> "Quantity to Add (${targetItem.unit})"
                    AdjustmentType.REDUCE -> "Quantity to Reduce (${targetItem.unit})"
                    AdjustmentType.SET_EXACT -> "New Stock Quantity (${targetItem.unit})"
                })
            },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adjustment_qty_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryBright,
                unfocusedBorderColor = colors.cardBorder,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedContainerColor = colors.cardBackground,
                unfocusedContainerColor = colors.cardBackground
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Reason / Remarks
        OutlinedTextField(
            value = reasonText,
            onValueChange = { reasonText = it },
            label = { Text("Reason / Remarks") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adjustment_reason_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryBright,
                unfocusedBorderColor = colors.cardBorder,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedContainerColor = colors.cardBackground,
                unfocusedContainerColor = colors.cardBackground
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick reason chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickReasons.forEach { r ->
                val isSelected = reasonText == r
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.cardBackground)
                        .border(
                            1.dp,
                            if (isSelected) colors.primaryBright else colors.cardBorder,
                            RoundedCornerShape(50)
                        )
                        .clickable { reasonText = r }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = r,
                        color = if (isSelected) colors.primaryBright else colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

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
                    if (quantityText.isBlank() || qtyVal <= 0.0 && adjustmentType != AdjustmentType.SET_EXACT) {
                        errorMessage = "Please enter a valid quantity."
                        return@Button
                    }
                    if (requireAdjustmentReason && reasonText.trim().isBlank()) {
                        errorMessage = "Please specify a reason for this stock adjustment."
                        return@Button
                    }
                    if (newStock < 0.0 && !allowNegativeStock) {
                        errorMessage = "Stock cannot be negative ($newStock ${targetItem.unit}). Check Inventory settings to allow negative stock."
                        return@Button
                    }

                    onAdjust(newStock, reasonText.trim().ifBlank { "Stock Adjustment" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBright),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("confirm_adjustment_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Apply Adjustment",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
