package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShortcutIconBg
import com.example.ui.theme.ShortcutIconTint
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TextWhite

@Composable
fun ShortcutsSection(
    onAddPartyClick: () -> Unit,
    onSalesInvoiceClick: () -> Unit,
    onPaymentInClick: () -> Unit,
    onPaymentOutClick: () -> Unit,
    onAddNewItemClick: () -> Unit,
    onPurchaseInvoiceClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onEditMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shortcuts",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onEditMenuClick)
                    .testTag("shortcuts_edit_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Menu",
                    tint = TealAccent,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Edit Menu",
                    color = TealAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Row 1 of Shortcuts (matches image_3.png exactly)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShortcutItem(
                icon = Icons.Default.PersonAdd,
                label = "Add Party",
                onClick = onAddPartyClick,
                testTag = "shortcut_add_party"
            )

            ShortcutItem(
                icon = Icons.Default.LocalOffer,
                label = "Sales Invoice",
                onClick = onSalesInvoiceClick,
                testTag = "shortcut_sales_invoice"
            )

            ShortcutItem(
                icon = Icons.Default.CallReceived,
                label = "Payment In",
                onClick = onPaymentInClick,
                testTag = "shortcut_payment_in"
            )

            ShortcutItem(
                icon = Icons.Default.CallMade,
                label = "Payment Out",
                onClick = onPaymentOutClick,
                testTag = "shortcut_payment_out"
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Row 2 of Shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShortcutItem(
                icon = Icons.Default.AddBox,
                label = "Add New Item",
                onClick = onAddNewItemClick,
                testTag = "shortcut_add_item"
            )

            ShortcutItem(
                icon = Icons.Default.ShoppingCart,
                label = "Purchase",
                onClick = onPurchaseInvoiceClick,
                testTag = "shortcut_purchase_invoice"
            )

            ShortcutItem(
                icon = Icons.Default.ReceiptLong,
                label = "Expense",
                onClick = onExpenseClick,
                testTag = "shortcut_expense"
            )

            ShortcutItem(
                icon = Icons.Default.Edit,
                label = "More Actions",
                onClick = onEditMenuClick,
                testTag = "shortcut_more_actions"
            )
        }
    }
}

@Composable
fun ShortcutItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(ShortcutIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ShortcutIconTint,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = TextWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
