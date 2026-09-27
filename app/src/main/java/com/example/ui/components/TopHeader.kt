package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.GoldCoinBg
import com.example.ui.theme.GoldCoinBorder
import com.example.ui.theme.RedBadge
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun TopHeader(
    businessName: String,
    coinBalance: Int,
    unreadNotifications: Int,
    onBusinessClick: () -> Unit,
    onCoinsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Business Profile & Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onBusinessClick)
                .testTag("business_switcher_header")
        ) {
            // Circular Logo
            Image(
                painter = painterResource(id = R.drawable.ic_atri_nova_logo),
                contentDescription = "Business Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = businessName,
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Switch Business",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Rewards Coin Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(GoldCoinBg)
                .border(1.dp, GoldCoinBorder, RoundedCornerShape(20.dp))
                .clickable(onClick = onCoinsClick)
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("rewards_coin_badge")
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_gold_coin),
                contentDescription = "Gold Coin",
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = coinBalance.toString(),
                color = GoldCoin,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Notification Bell with Red Badge
        Box(
            modifier = Modifier
                .testTag("notifications_bell_button")
        ) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (unreadNotifications > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 24.dp, y = 4.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(RedBadge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadNotifications.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
