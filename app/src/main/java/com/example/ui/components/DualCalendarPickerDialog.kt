package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.util.NepaliCalendar
import java.util.Calendar

enum class CalendarTabMode {
    BS_NEPALI,
    AD_ENGLISH
}

/**
 * Interactive Dual BS (Nepali Bikram Sambat) and AD (Gregorian) Calendar Date Picker Dialog.
 * Designed for financial transaction entry with instant bidirectional synchronization,
 * day of week calculation, and fiscal year tagging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualCalendarPickerDialog(
    initialDateMillis: Long = System.currentTimeMillis(),
    onDismissRequest: () -> Unit,
    onDateConfirmed: (dateMillis: Long, bsDateString: String, adDateString: String) -> Unit
) {
    // Current dual conversion baseline
    val initialDual = remember(initialDateMillis) {
        NepaliCalendar.adMillisToDual(initialDateMillis)
    }

    var activeTab by remember { mutableStateOf(CalendarTabMode.BS_NEPALI) }

    // BS State
    var bsYear by remember { mutableIntStateOf(initialDual.bsDate.year) }
    var bsMonth by remember { mutableIntStateOf(initialDual.bsDate.month) }
    var bsDay by remember { mutableIntStateOf(initialDual.bsDate.day) }

    // AD State
    var adYear by remember { mutableIntStateOf(initialDual.adDate.year) }
    var adMonth by remember { mutableIntStateOf(initialDual.adDate.month) }
    var adDay by remember { mutableIntStateOf(initialDual.adDate.day) }

    // Derived active conversion result
    val currentResult = remember(activeTab, bsYear, bsMonth, bsDay, adYear, adMonth, adDay) {
        if (activeTab == CalendarTabMode.BS_NEPALI) {
            NepaliCalendar.bsToDual(bsYear, bsMonth, bsDay)
        } else {
            NepaliCalendar.adToDual(adYear, adMonth, adDay)
        }
    }

    // Supported BS years
    val supportedBsYears = remember { NepaliCalendar.getSupportedBsYears() }
    val maxDaysInBsMonth = remember(bsYear, bsMonth) {
        NepaliCalendar.getDaysInBsMonth(bsYear, bsMonth)
    }

    // Supported AD years (approx matching 2070-2090 BS -> 2013-2034 AD)
    val supportedAdYears = remember { (2015..2035).toList() }
    val maxDaysInAdMonth = remember(adYear, adMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, adYear)
            set(Calendar.MONTH, adMonth - 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("dual_calendar_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
            border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                // 1. Dialog Header with Calendar Mode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SkyBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = SkyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Select Transaction Date",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Bikram Sambat (BS) & Gregorian (AD)",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.textMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_calendar_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppTheme.colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Calendar Segmented Tabs (BS vs AD)
                PrimaryTabRow(
                    selectedTabIndex = if (activeTab == CalendarTabMode.BS_NEPALI) 0 else 1,
                    containerColor = AppTheme.colors.cardBackground,
                    contentColor = SkyBlue,
                    divider = {}
                ) {
                    Tab(
                        selected = activeTab == CalendarTabMode.BS_NEPALI,
                        onClick = {
                            // Sync current state to BS when switching
                            bsYear = currentResult.bsDate.year
                            bsMonth = currentResult.bsDate.month
                            bsDay = currentResult.bsDate.day
                            activeTab = CalendarTabMode.BS_NEPALI
                        },
                        text = {
                            Text(
                                text = "Nepali (BS)",
                                fontWeight = if (activeTab == CalendarTabMode.BS_NEPALI) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tab_bs_nepali")
                    )
                    Tab(
                        selected = activeTab == CalendarTabMode.AD_ENGLISH,
                        onClick = {
                            // Sync current state to AD when switching
                            adYear = currentResult.adDate.year
                            adMonth = currentResult.adDate.month
                            adDay = currentResult.adDate.day
                            activeTab = CalendarTabMode.AD_ENGLISH
                        },
                        text = {
                            Text(
                                text = "English (AD)",
                                fontWeight = if (activeTab == CalendarTabMode.AD_ENGLISH) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tab_ad_english")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Dual Date Live Summary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.cardBackground)
                        .border(1.dp, AppTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Nepali BS
                        Column {
                            Text(
                                text = "Nepali BS Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.textMuted
                            )
                            Text(
                                text = currentResult.bsDate.formattedBs,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SkyBlueBright,
                                modifier = Modifier.testTag("summary_bs_date")
                            )
                            Text(
                                text = "${currentResult.bsDate.monthNameEn} (${currentResult.bsDate.monthNameNp})",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.textSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Sync",
                            tint = AppTheme.colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )

                        // Right: English AD
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "English AD Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.textMuted
                            )
                            Text(
                                text = currentResult.adDate.formattedAd,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
                                modifier = Modifier.testTag("summary_ad_date")
                            )
                            Text(
                                text = "${currentResult.adDate.dayOfWeekEn} (${currentResult.bsDate.dayOfWeekNp})",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Interactive Year & Month Selectors
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "CalendarPickerContent"
                ) { tab ->
                    if (tab == CalendarTabMode.BS_NEPALI) {
                        NepaliBsPickerBody(
                            selectedYear = bsYear,
                            onYearChange = { bsYear = it },
                            supportedYears = supportedBsYears,
                            selectedMonth = bsMonth,
                            onMonthChange = { bsMonth = it },
                            selectedDay = bsDay,
                            onDayChange = { bsDay = it },
                            maxDays = maxDaysInBsMonth
                        )
                    } else {
                        EnglishAdPickerBody(
                            selectedYear = adYear,
                            onYearChange = { adYear = it },
                            supportedYears = supportedAdYears,
                            selectedMonth = adMonth,
                            onMonthChange = { adMonth = it },
                            selectedDay = adDay,
                            onDayChange = { adDay = it },
                            maxDays = maxDaysInAdMonth
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Quick Presets: Today, Yesterday, Fiscal Year Start
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val today = NepaliCalendar.getTodayDual()
                                bsYear = today.bsDate.year
                                bsMonth = today.bsDate.month
                                bsDay = today.bsDate.day
                                adYear = today.adDate.year
                                adMonth = today.adDate.month
                                adDay = today.adDate.day
                            }
                            .testTag("preset_today"),
                        shape = RoundedCornerShape(8.dp),
                        color = AppTheme.colors.cardBackground,
                        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                tint = SkyBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Today (आज)",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.textPrimary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                                val yesterday = NepaliCalendar.adMillisToDual(cal.timeInMillis)
                                bsYear = yesterday.bsDate.year
                                bsMonth = yesterday.bsDate.month
                                bsDay = yesterday.bsDate.day
                                adYear = yesterday.adDate.year
                                adMonth = yesterday.adDate.month
                                adDay = yesterday.adDate.day
                            }
                            .testTag("preset_yesterday"),
                        shape = RoundedCornerShape(8.dp),
                        color = AppTheme.colors.cardBackground,
                        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                    ) {
                        Text(
                            text = "Yesterday (हिजो)",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                bsDay = 1
                            }
                            .testTag("preset_month_start"),
                        shape = RoundedCornerShape(8.dp),
                        color = AppTheme.colors.cardBackground,
                        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                    ) {
                        Text(
                            text = "1st of Month (महिनाको १)",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = AppTheme.colors.cardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // 6. Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_date_picker"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppTheme.colors.textPrimary
                        )
                    }

                    Button(
                        onClick = {
                            onDateConfirmed(
                                currentResult.timeMillis,
                                currentResult.bsDate.formattedBs,
                                currentResult.adDate.formattedAd
                            )
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("confirm_date_picker"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Apply Date",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * BS (Bikram Sambat) Date Picker Layout
 */
@Composable
private fun NepaliBsPickerBody(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    supportedYears: List<Int>,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedDay: Int,
    onDayChange: (Int) -> Unit,
    maxDays: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // Year Selector Scrollable Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (selectedYear > supportedYears.first()) onYearChange(selectedYear - 1) },
                enabled = selectedYear > supportedYears.first(),
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev Year", tint = AppTheme.colors.textPrimary)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                supportedYears.forEach { yr ->
                    val isSelected = yr == selectedYear
                    FilterChip(
                        selected = isSelected,
                        onClick = { onYearChange(yr) },
                        label = { Text(text = "$yr BS") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SkyBlue.copy(alpha = 0.2f),
                            selectedLabelColor = SkyBlueBright
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SkyBlue else AppTheme.colors.cardBorder
                        ),
                        modifier = Modifier.testTag("bs_year_$yr")
                    )
                }
            }

            IconButton(
                onClick = { if (selectedYear < supportedYears.last()) onYearChange(selectedYear + 1) },
                enabled = selectedYear < supportedYears.last(),
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Year", tint = AppTheme.colors.textPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month Selector (Baisakh to Chaitra)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NepaliCalendar.NEPALI_MONTHS_EN.forEachIndexed { index, monthName ->
                val monthIndex = index + 1
                val isSelected = monthIndex == selectedMonth
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onMonthChange(monthIndex)
                        if (selectedDay > NepaliCalendar.getDaysInBsMonth(selectedYear, monthIndex)) {
                            onDayChange(NepaliCalendar.getDaysInBsMonth(selectedYear, monthIndex))
                        }
                    },
                    label = {
                        Text(
                            text = "${index + 1}. $monthName",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SkyBlue.copy(alpha = 0.2f),
                        selectedLabelColor = SkyBlueBright
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) SkyBlue else AppTheme.colors.cardBorder
                    ),
                    modifier = Modifier.testTag("bs_month_$monthIndex")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Day Number Grid (1 to 29..32)
        Text(
            text = "Select Day of ${NepaliCalendar.NEPALI_MONTHS_EN[selectedMonth - 1]}:",
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(maxDays) { index ->
                val dayNum = index + 1
                val isSelected = dayNum == selectedDay

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SkyBlue else AppTheme.colors.cardBackground)
                        .border(
                            1.dp,
                            if (isSelected) SkyBlue else AppTheme.colors.cardBorder,
                            CircleShape
                        )
                        .clickable { onDayChange(dayNum) }
                        .testTag("bs_day_$dayNum"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dayNum",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else AppTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * AD (Gregorian) Date Picker Layout
 */
@Composable
private fun EnglishAdPickerBody(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    supportedYears: List<Int>,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedDay: Int,
    onDayChange: (Int) -> Unit,
    maxDays: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // Year Selector Scrollable Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (selectedYear > supportedYears.first()) onYearChange(selectedYear - 1) },
                enabled = selectedYear > supportedYears.first(),
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev Year", tint = AppTheme.colors.textPrimary)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                supportedYears.forEach { yr ->
                    val isSelected = yr == selectedYear
                    FilterChip(
                        selected = isSelected,
                        onClick = { onYearChange(yr) },
                        label = { Text(text = "$yr AD") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SkyBlue.copy(alpha = 0.2f),
                            selectedLabelColor = SkyBlueBright
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SkyBlue else AppTheme.colors.cardBorder
                        ),
                        modifier = Modifier.testTag("ad_year_$yr")
                    )
                }
            }

            IconButton(
                onClick = { if (selectedYear < supportedYears.last()) onYearChange(selectedYear + 1) },
                enabled = selectedYear < supportedYears.last(),
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Year", tint = AppTheme.colors.textPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month Selector (January to December)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NepaliCalendar.GREGORIAN_MONTHS_EN.forEachIndexed { index, monthName ->
                val monthIndex = index + 1
                val isSelected = monthIndex == selectedMonth
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onMonthChange(monthIndex)
                    },
                    label = {
                        Text(
                            text = monthName.take(3),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SkyBlue.copy(alpha = 0.2f),
                        selectedLabelColor = SkyBlueBright
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) SkyBlue else AppTheme.colors.cardBorder
                    ),
                    modifier = Modifier.testTag("ad_month_$monthIndex")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Day Number Grid (1 to 28..31)
        Text(
            text = "Select Day of ${NepaliCalendar.GREGORIAN_MONTHS_EN[selectedMonth - 1]}:",
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(maxDays) { index ->
                val dayNum = index + 1
                val isSelected = dayNum == selectedDay

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SkyBlue else AppTheme.colors.cardBackground)
                        .border(
                            1.dp,
                            if (isSelected) SkyBlue else AppTheme.colors.cardBorder,
                            CircleShape
                        )
                        .clickable { onDayChange(dayNum) }
                        .testTag("ad_day_$dayNum"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dayNum",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else AppTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
