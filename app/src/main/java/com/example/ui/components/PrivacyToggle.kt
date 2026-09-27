package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun PrivacyToggle(
    privacyMode: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable { onToggle(!privacyMode) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("privacy_mode_container"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (privacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Privacy Icon",
                tint = if (privacyMode) TealAccent else TextMuted,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Privacy Mode",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = privacyMode,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TealAccent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color(0xFF2C2F3E),
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.testTag("privacy_mode_switch")
        )
    }
}
