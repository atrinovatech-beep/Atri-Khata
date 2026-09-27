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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.ui.theme.AppTheme
import com.example.util.NepaliDateUtils

/**
 * Dual AD & BS Date Selector Component
 * Displays both Gregorian (AD) and Bikram Sambat (BS) dates with interactive Nepali date picker.
 * 100% theme-aware in Light and Dark mode.
 */
@Composable
fun DualDateSelector(
    modifier: Modifier = Modifier,
    initialDateMillis: Long = System.currentTimeMillis(),
    onDateChanged: (dateMillis: Long, bsDate: String, adDate: String) -> Unit
) {
    val colors = AppTheme.colors
    var currentDateMillis by remember { mutableLongStateOf(initialDateMillis) }
    var showCustomPicker by remember { mutableStateOf(false) }

    val currentNepali = remember(currentDateMillis) {
        NepaliDateUtils.adToBs(currentDateMillis)
    }

    var selectedBsYear by remember(currentNepali) { mutableIntStateOf(currentNepali.year) }
    var selectedBsMonth by remember(currentNepali) { mutableIntStateOf(currentNepali.month) }
    var selectedBsDay by remember(currentNepali) { mutableIntStateOf(currentNepali.day) }

    val formattedBs = remember(currentDateMillis) {
        NepaliDateUtils.formatBsDate(currentDateMillis)
    }
    val formattedAd = remember(currentDateMillis) {
        NepaliDateUtils.formatAdDate(currentDateMillis)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag("dual_date_selector_card"),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Label and Toggle Custom Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transaction Date",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.primary.copy(alpha = 0.12f))
                        .clickable { showCustomPicker = !showCustomPicker }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showCustomPicker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Nepali Calendar Picker",
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (showCustomPicker) "Hide Picker" else "Change Date",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Date Display: BS (Nepali) & AD (Gregorian) Side-by-Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nepali Date (BS) - Primary
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.inputBackground)
                        .border(1.dp, colors.primary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BS (Nepali)",
                                color = colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "2082/83",
                                    color = colors.primary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = formattedBs,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentNepali.monthNameEn,
                            color = colors.textSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Gregorian Date (AD) - Secondary
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.inputBackground)
                        .border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "AD (Gregorian)",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = formattedAd,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Standard Time",
                            color = colors.textMuted,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Preset Chips: Today, Yesterday
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Today Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.primary.copy(alpha = 0.12f))
                        .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .clickable {
                            val now = System.currentTimeMillis()
                            currentDateMillis = now
                            val bs = NepaliDateUtils.formatBsDate(now)
                            val ad = NepaliDateUtils.formatAdDate(now)
                            onDateChanged(now, bs, ad)
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Today (आज)",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Yesterday Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            val yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
                            currentDateMillis = yesterday
                            val bs = NepaliDateUtils.formatBsDate(yesterday)
                            val ad = NepaliDateUtils.formatAdDate(yesterday)
                            onDateChanged(yesterday, bs, ad)
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Yesterday (हिजो)",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Expandable Custom Nepali Date Selector (BS Picker)
            if (showCustomPicker) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Select Bikram Sambat (BS) Date:",
                            color = colors.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Year, Month, Day adjustment selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // BS Year Selector (2080 - 2085)
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text("Year", color = colors.textSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.inputBackground)
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "-",
                                        color = colors.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                if (selectedBsYear > 2080) selectedBsYear--
                                            }
                                            .padding(horizontal = 6.dp)
                                    )
                                    Text(
                                        text = "$selectedBsYear",
                                        color = colors.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "+",
                                        color = colors.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                if (selectedBsYear < 2085) selectedBsYear++
                                            }
                                            .padding(horizontal = 6.dp)
                                    )
                                }
                            }

                            // BS Month Selector (1..12)
                            Column(modifier = Modifier.weight(1.4f)) {
                                Text("Month", color = colors.textSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.inputBackground)
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "<",
                                        color = colors.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                if (selectedBsMonth > 1) selectedBsMonth--
                                            }
                                            .padding(horizontal = 4.dp)
                                    )
                                    Text(
                                        text = NepaliDateUtils.NEPALI_MONTHS_EN.getOrElse(selectedBsMonth - 1) { "Baisakh" }
                                            .take(4),
                                        color = colors.textPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ">",
                                        color = colors.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                if (selectedBsMonth < 12) selectedBsMonth++
                                            }
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }

                            // BS Day Selector (1..32)
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text("Day", color = colors.textSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.inputBackground)
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "-",
                                        color = colors.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                if (selectedBsDay > 1) selectedBsDay--
                                            }
                                            .padding(horizontal = 6.dp)
                                    )
                                    Text(
                                        text = String.format(java.util.Locale.US, "%02d", selectedBsDay),
                                        color = colors.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "+",
                                        color = colors.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                val maxDays = NepaliDateUtils.getDaysInBsMonth(selectedBsYear, selectedBsMonth)
                                                if (selectedBsDay < maxDays) selectedBsDay++
                                            }
                                            .padding(horizontal = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Apply Selected BS Date Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primary)
                                .clickable {
                                    val computedAdMillis = NepaliDateUtils.bsToAd(selectedBsYear, selectedBsMonth, selectedBsDay)
                                    currentDateMillis = computedAdMillis
                                    val bsStr = String.format(java.util.Locale.US, "%04d/%02d/%02d", selectedBsYear, selectedBsMonth, selectedBsDay)
                                    val adStr = NepaliDateUtils.formatAdDate(computedAdMillis)
                                    onDateChanged(computedAdMillis, bsStr, adStr)
                                    showCustomPicker = false
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Apply Date (${String.format(java.util.Locale.US, "%04d/%02d/%02d", selectedBsYear, selectedBsMonth, selectedBsDay)})",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
