package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeCardBg
import com.example.ui.theme.OrangeCardBorder
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueCardBg
import com.example.ui.theme.SkyBlueCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun FrappeSummaryCards(
    toReceiveFormatted: String,
    toGiveFormatted: String,
    salesFormatted: String,
    purchaseFormatted: String,
    expenseFormatted: String,
    onReceiveClick: () -> Unit,
    onGiveClick: () -> Unit,
    onSalesClick: () -> Unit,
    onPurchaseClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onBalanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: To Receive (Corporate Blue) & To Give (Orange)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // To Receive Card (Sky Blue)
            SummaryCard(
                amountText = toReceiveFormatted,
                labelText = "To Receive ↓",
                amountColor = SkyBlueBright,
                labelColor = SkyBlueBright,
                backgroundColor = SkyBlueCardBg,
                borderColor = SkyBlueCardBorder,
                onClick = onReceiveClick,
                testTag = "card_to_receive",
                modifier = Modifier.weight(1f)
            )

            // To Give Card (Orange)
            SummaryCard(
                amountText = toGiveFormatted,
                labelText = "To Give ↑",
                amountColor = OrangeAccent,
                labelColor = OrangeAccent,
                backgroundColor = OrangeCardBg,
                borderColor = OrangeCardBorder,
                onClick = onGiveClick,
                testTag = "card_to_give",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Sales & Purchase
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                amountText = salesFormatted,
                labelText = "Sales (September)",
                amountColor = TextWhite,
                labelColor = TextMuted,
                backgroundColor = CardDark,
                borderColor = CardBorder,
                onClick = onSalesClick,
                testTag = "card_sales",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                amountText = purchaseFormatted,
                labelText = "Purchase (Septem...",
                amountColor = TextWhite,
                labelColor = TextMuted,
                backgroundColor = CardDark,
                borderColor = CardBorder,
                onClick = onPurchaseClick,
                testTag = "card_purchase",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Expense & Total Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                amountText = expenseFormatted,
                labelText = "Expense (Septemb...",
                amountColor = TextWhite,
                labelColor = TextMuted,
                backgroundColor = CardDark,
                borderColor = CardBorder,
                onClick = onExpenseClick,
                testTag = "card_expense",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                amountText = "Total Balance",
                labelText = "Cash & Bank",
                amountColor = TextWhite,
                labelColor = TextMuted,
                backgroundColor = CardDark,
                borderColor = CardBorder,
                onClick = onBalanceClick,
                testTag = "card_total_balance",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    amountText: String,
    labelText: String,
    amountColor: Color,
    labelColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .testTag(testTag)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = amountText,
                    color = amountColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = labelColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = labelText,
                color = labelColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
