package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavTab
import com.example.ui.theme.AppTheme

@Composable
fun BottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val navBackground = if (isDark) Color(0xFF0F172A) else Color.White
    val navBorder = if (isDark) Color(0xFF1E2F4D) else Color(0xFFE2E8F0)
    val activeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val inactiveColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(navBackground)
            .border(width = 1.dp, color = navBorder)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            NavBarItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = currentTab == NavTab.HOME,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(NavTab.HOME) },
                testTag = "tab_home"
            )

            // 2. Transactions
            NavBarItem(
                icon = Icons.Outlined.ReceiptLong,
                label = "Transactions",
                selected = currentTab == NavTab.TRANSACTIONS,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(NavTab.TRANSACTIONS) },
                testTag = "tab_transactions"
            )

            // 3. Parties
            NavBarItem(
                icon = Icons.Outlined.Groups,
                label = "Parties",
                selected = currentTab == NavTab.PARTIES,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(NavTab.PARTIES) },
                testTag = "tab_parties"
            )

            // 4. Inventory (Replacing Reports)
            NavBarItem(
                icon = Icons.Outlined.Inventory2,
                label = "Inventory",
                selected = currentTab == NavTab.INVENTORY,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(NavTab.INVENTORY) },
                testTag = "tab_inventory"
            )

            // 5. More
            NavBarItem(
                icon = Icons.Outlined.GridView,
                label = "More",
                selected = currentTab == NavTab.MORE,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabSelected(NavTab.MORE) },
                testTag = "tab_more"
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            color = if (selected) activeColor else inactiveColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1
        )

        // Active indicator line directly beneath label
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .width(26.dp)
                .height(2.5.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(if (selected) activeColor else Color.Transparent)
        )
    }
}


