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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.RedBadge
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TextWhite

@Composable
fun ExploreAppSection(
    onQuickEntryClick: () -> Unit,
    onQuickPosClick: () -> Unit,
    onReportsClick: () -> Unit,
    onCreditReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Explore App",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quick Entry
            ExploreCard(
                icon = Icons.Default.Calculate,
                title = "Quick\nEntry",
                onClick = onQuickEntryClick,
                testTag = "explore_quick_entry",
                modifier = Modifier.weight(1f)
            )

            // Quick POS
            ExploreCard(
                icon = Icons.Default.PointOfSale,
                title = "Quick\nPOS",
                onClick = onQuickPosClick,
                testTag = "explore_quick_pos",
                modifier = Modifier.weight(1f)
            )

            // View Reports
            ExploreCard(
                icon = Icons.Default.InsertChart,
                title = "View\nReports",
                onClick = onReportsClick,
                testTag = "explore_view_reports",
                modifier = Modifier.weight(1f)
            )

            // Credit Reminder (with "New" badge)
            ExploreCard(
                icon = Icons.Default.Chat,
                title = "Credit\nReminder",
                hasBadge = true,
                badgeText = "New",
                onClick = onCreditReminderClick,
                testTag = "explore_credit_reminder",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ExploreCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String,
    hasBadge: Boolean = false,
    badgeText: String = "",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(108.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(28.dp)
                )

                if (hasBadge) {
                    Box(
                        modifier = Modifier
                            .offset(x = 18.dp, y = (-6).dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(RedBadge)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
