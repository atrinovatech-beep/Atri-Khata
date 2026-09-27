package com.example.ui.screens.inventory

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.model.StockStatus
import com.example.ui.MainViewModel
import com.example.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryItemDetailSheet(
    item: InventoryItemEntity,
    viewModel: MainViewModel,
    onEdit: (InventoryItemEntity) -> Unit,
    onStockAdjust: (InventoryItemEntity) -> Unit,
    onDelete: (InventoryItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val isDark = AppTheme.isDark
    val status = StockStatus.fromItem(item)

    val invoiceItems: List<SalesInvoiceItemEntity> by viewModel.getInvoiceItemsForProduct(item.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val statusColor = when (status) {
        StockStatus.IN_STOCK -> Color(0xFF10B981)
        StockStatus.LOW_STOCK -> Color(0xFFF59E0B)
        StockStatus.OUT_OF_STOCK -> Color(0xFFEF4444)
    }

    val stockValue = item.stockQuantity * item.purchasePrice
    val margin = item.salePrice - item.purchasePrice
    val marginPercent = if (item.purchasePrice > 0.0) (margin / item.purchasePrice) * 100.0 else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = colors.primaryBright,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = item.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "SKU: ${item.sku.ifBlank { "N/A" }}  •  ${item.category}",
                        fontSize = 12.sp,
                        color = colors.textMuted
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
        Spacer(modifier = Modifier.height(14.dp))

        // Stock Status & Quantity Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Stock Level",
                        fontSize = 13.sp,
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium
                    )

                    // Stock Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = status.label,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%,.2f", item.stockQuantity),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = item.unit,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Alert threshold: ${String.format(Locale.US, "%.1f", item.minStockAlert)} ${item.unit}",
                    fontSize = 12.sp,
                    color = if (status == StockStatus.LOW_STOCK) Color(0xFFF59E0B) else colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Financial & Valuation Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Purchase Price
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Purchase Price", fontSize = 11.sp, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs. ${String.format(Locale.US, "%,.2f", item.purchasePrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            // Sale Price
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Sale Price", fontSize = 11.sp, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs. ${String.format(Locale.US, "%,.2f", item.salePrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryBright
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Stock Valuation
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Stock Valuation", fontSize = 11.sp, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs. ${String.format(Locale.US, "%,.2f", stockValue)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            // Estimated Margin
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Gross Margin", fontSize = 11.sp, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%,.2f", margin)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (margin >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${String.format(Locale.US, "%.0f", marginPercent)}%)",
                            fontSize = 11.sp,
                            color = if (margin >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions Row
        Text("Quick Actions", color = colors.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onEdit(item) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                    .testTag("detail_edit_item_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = colors.primaryBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit", color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { onStockAdjust(item) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp)
                    .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .testTag("detail_adjust_stock_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.SyncAlt,
                    contentDescription = null,
                    tint = colors.primaryBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adjust Stock", color = colors.primaryBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .testTag("detail_delete_item_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sales Invoices History Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = colors.primaryBright,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Sales Invoice History",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Text(
                text = "${invoiceItems.size} Entries",
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (invoiceItems.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No sales recorded yet",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "When sales invoices are created containing this product, transaction records will appear here.",
                        color = colors.textMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                invoiceItems.forEach { itemLine ->
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
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Invoice Item (Qty: ${itemLine.quantity} ${itemLine.unit})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormat.format(Date(itemLine.createdAt)),
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Rs. ${String.format(Locale.US, "%,.2f", itemLine.lineTotal)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryBright
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "@ Rs. ${String.format(Locale.US, "%,.2f", itemLine.rate)}",
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stock Movement / Ledger Section (Prepared for Phase 2)
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = colors.primaryBright,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Stock Ledger Ready",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Real-time stock tracking active for item ${item.name}.",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Inventory Item?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete '${item.name}'? This item will be removed from your active inventory.",
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                    if (invoiceItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Notice: This item has ${invoiceItems.size} historical sales records. Historical invoices will safely preserve product name snapshots.",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(item)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.cardBackground
        )
    }
}
