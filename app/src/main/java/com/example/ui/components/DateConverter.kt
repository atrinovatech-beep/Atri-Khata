package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.util.NepaliCalendar
import java.util.Calendar

enum class ConverterMode {
    BS_TO_AD,
    AD_TO_BS
}

/**
 * Interactive Date Converter Component
 * Converts seamlessly between Bikram Sambat (BS) and Gregorian (AD) dates.
 */
@Composable
fun DateConverterCard(
    modifier: Modifier = Modifier,
    initialDateMillis: Long = System.currentTimeMillis(),
    onApplyDate: ((dateMillis: Long, bsFormatted: String, adFormatted: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // Initialize with current date
    val todayDual = remember { NepaliCalendar.adMillisToDual(initialDateMillis) }

    var mode by remember { mutableStateOf(ConverterMode.BS_TO_AD) }

    // BS Input State
    var bsYear by remember { mutableIntStateOf(todayDual.bsDate.year) }
    var bsMonth by remember { mutableIntStateOf(todayDual.bsDate.month) }
    var bsDay by remember { mutableIntStateOf(todayDual.bsDate.day) }

    // AD Input State
    var adYear by remember { mutableIntStateOf(todayDual.adDate.year) }
    var adMonth by remember { mutableIntStateOf(todayDual.adDate.month) }
    var adDay by remember { mutableIntStateOf(todayDual.adDate.day) }

    // Calculate conversion result in real-time
    val conversionResult = remember(mode, bsYear, bsMonth, bsDay, adYear, adMonth, adDay) {
        when (mode) {
            ConverterMode.BS_TO_AD -> NepaliCalendar.bsToDual(bsYear, bsMonth, bsDay)
            ConverterMode.AD_TO_BS -> NepaliCalendar.adToDual(adYear, adMonth, adDay)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .testTag("date_converter_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title and Swap Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkyBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Nepali Date Converter",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "BS (Bikram Sambat) ⇄ AD (Gregorian)",
                            color = SkyBlueBright,
                            fontSize = 11.sp
                        )
                    }
                }

                // Swap Direction Button
                IconButton(
                    onClick = {
                        mode = if (mode == ConverterMode.BS_TO_AD) ConverterMode.AD_TO_BS else ConverterMode.BS_TO_AD
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, CircleShape)
                        .testTag("date_converter_swap_mode_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Mode",
                        tint = SkyBlueBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector Tabs (BS -> AD vs AD -> BS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDark)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeTabButton(
                    title = "BS ➔ AD (Bikram Sambat)",
                    isSelected = mode == ConverterMode.BS_TO_AD,
                    onClick = { mode = ConverterMode.BS_TO_AD },
                    modifier = Modifier.weight(1f),
                    testTag = "date_converter_tab_bs_to_ad"
                )
                ModeTabButton(
                    title = "AD ➔ BS (English Date)",
                    isSelected = mode == ConverterMode.AD_TO_BS,
                    onClick = { mode = ConverterMode.AD_TO_BS },
                    modifier = Modifier.weight(1f),
                    testTag = "date_converter_tab_ad_to_bs"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Presets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip(
                    label = "Today",
                    onClick = {
                        val t = NepaliCalendar.getTodayDual()
                        bsYear = t.bsDate.year
                        bsMonth = t.bsDate.month
                        bsDay = t.bsDate.day
                        adYear = t.adDate.year
                        adMonth = t.adDate.month
                        adDay = t.adDate.day
                    },
                    testTag = "preset_today"
                )

                PresetChip(
                    label = "Yesterday",
                    onClick = {
                        val y = NepaliCalendar.adMillisToDual(System.currentTimeMillis() - 24 * 60 * 60 * 1000L)
                        bsYear = y.bsDate.year
                        bsMonth = y.bsDate.month
                        bsDay = y.bsDate.day
                        adYear = y.adDate.year
                        adMonth = y.adDate.month
                        adDay = y.adDate.day
                    },
                    testTag = "preset_yesterday"
                )

                PresetChip(
                    label = "Start of Month",
                    onClick = {
                        if (mode == ConverterMode.BS_TO_AD) {
                            bsDay = 1
                        } else {
                            adDay = 1
                        }
                    },
                    testTag = "preset_start_of_month"
                )

                PresetChip(
                    label = "FY Start (1 Shrawan)",
                    onClick = {
                        bsMonth = 4
                        bsDay = 1
                        val res = NepaliCalendar.bsToDual(bsYear, 4, 1)
                        adYear = res.adDate.year
                        adMonth = res.adDate.month
                        adDay = res.adDate.day
                    },
                    testTag = "preset_fy_start"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Date Inputs
            AnimatedContent(
                targetState = mode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "DateInputsTransition"
            ) { targetMode ->
                when (targetMode) {
                    ConverterMode.BS_TO_AD -> {
                        BsDateInputRow(
                            selectedYear = bsYear,
                            selectedMonth = bsMonth,
                            selectedDay = bsDay,
                            onYearChange = { bsYear = it },
                            onMonthChange = { bsMonth = it },
                            onDayChange = { bsDay = it }
                        )
                    }
                    ConverterMode.AD_TO_BS -> {
                        AdDateInputRow(
                            selectedYear = adYear,
                            selectedMonth = adMonth,
                            selectedDay = adDay,
                            onYearChange = { adYear = it },
                            onMonthChange = { adMonth = it },
                            onDayChange = { adDay = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-Time Conversion Result Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SkyBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (mode == ConverterMode.BS_TO_AD) "CONVERTED AD DATE" else "CONVERTED BS DATE",
                            color = SkyBlueBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        // Copy Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CardDark)
                                .clickable {
                                    val textToCopy = if (mode == ConverterMode.BS_TO_AD) {
                                        "${conversionResult.adDate.formattedAd} (${conversionResult.adDate.formattedLongEn})"
                                    } else {
                                        "${conversionResult.bsDate.formattedBs} (${conversionResult.bsDate.formattedLongNp})"
                                    }
                                    clipboard.setText(AnnotatedString(textToCopy))
                                    Toast.makeText(context, "Copied: $textToCopy", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = SkyBlueBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", color = SkyBlueBright, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Primary Display
                    if (mode == ConverterMode.BS_TO_AD) {
                        Text(
                            text = conversionResult.adDate.formattedLongEn,
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Standard Format: ${conversionResult.adDate.formattedAd}",
                            color = TextSubtle,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = conversionResult.bsDate.formattedLongNp,
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${conversionResult.bsDate.formattedBs} (${conversionResult.bsDate.monthNameEn}) • ${conversionResult.bsDate.formattedBsNepali}",
                            color = TextSubtle,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Dual Details Grid (Weekday, Fiscal Year, Relative time)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailItem(
                            label = "Day of Week",
                            value = "${conversionResult.bsDate.dayOfWeekNp} (${conversionResult.adDate.dayOfWeekEn})"
                        )
                        DetailItem(
                            label = "Fiscal Year",
                            value = conversionResult.bsDate.fiscalYear
                        )
                        DetailItem(
                            label = "Timeline",
                            value = conversionResult.relativeDescription
                        )
                    }
                }
            }

            // Apply Date Button (if callback provided)
            if (onApplyDate != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        onApplyDate(
                            conversionResult.timeMillis,
                            conversionResult.bsDate.formattedBs,
                            conversionResult.adDate.formattedAd
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("date_converter_apply_btn")
                ) {
                    Text(
                        text = "Apply Selected Date",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Modal Bottom Sheet variant for Date Converter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateConverterBottomSheet(
    onDismiss: () -> Unit,
    initialDateMillis: Long = System.currentTimeMillis(),
    onApplyDate: ((dateMillis: Long, bsFormatted: String, adFormatted: String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Nepali Date Converter",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSubtle)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DateConverterCard(
                initialDateMillis = initialDateMillis,
                onApplyDate = { millis, bs, ad ->
                    onApplyDate?.invoke(millis, bs, ad)
                    onDismiss()
                }
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Internal Sub-components
// ----------------------------------------------------------------------------

@Composable
private fun ModeTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SkyBlue else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextSubtle,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Text(text = label, color = SkyBlueBright, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BsDateInputRow(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit
) {
    val supportedYears = remember { NepaliCalendar.getSupportedBsYears() }
    val maxDays = remember(selectedYear, selectedMonth) {
        NepaliCalendar.getDaysInBsMonth(selectedYear, selectedMonth)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // BS Year Dropdown
        DateDropdownSelector(
            label = "Year (BS)",
            currentValue = "$selectedYear BS",
            modifier = Modifier.weight(1.1f),
            testTag = "bs_year_selector"
        ) { onDismiss ->
            supportedYears.forEach { yr ->
                DropdownMenuItem(
                    text = { Text("$yr BS (${NepaliCalendar.toNepaliDigits(yr.toString())})", color = TextWhite, fontSize = 13.sp) },
                    onClick = {
                        onYearChange(yr)
                        onDismiss()
                    }
                )
            }
        }

        // BS Month Dropdown
        DateDropdownSelector(
            label = "Month",
            currentValue = "${NepaliCalendar.NEPALI_MONTHS_EN[selectedMonth - 1]} (${NepaliCalendar.NEPALI_MONTHS_NP[selectedMonth - 1]})",
            modifier = Modifier.weight(1.5f),
            testTag = "bs_month_selector"
        ) { onDismiss ->
            NepaliCalendar.NEPALI_MONTHS_EN.forEachIndexed { idx, mNameEn ->
                val mNameNp = NepaliCalendar.NEPALI_MONTHS_NP[idx]
                DropdownMenuItem(
                    text = { Text("${idx + 1}. $mNameEn ($mNameNp)", color = TextWhite, fontSize = 13.sp) },
                    onClick = {
                        onMonthChange(idx + 1)
                        if (selectedDay > NepaliCalendar.getDaysInBsMonth(selectedYear, idx + 1)) {
                            onDayChange(NepaliCalendar.getDaysInBsMonth(selectedYear, idx + 1))
                        }
                        onDismiss()
                    }
                )
            }
        }

        // BS Day Dropdown
        DateDropdownSelector(
            label = "Day",
            currentValue = "$selectedDay (${NepaliCalendar.toNepaliDigits(selectedDay.toString())})",
            modifier = Modifier.weight(1.1f),
            testTag = "bs_day_selector"
        ) { onDismiss ->
            (1..maxDays).forEach { d ->
                DropdownMenuItem(
                    text = { Text("$d (${NepaliCalendar.toNepaliDigits(d.toString())})", color = TextWhite, fontSize = 13.sp) },
                    onClick = {
                        onDayChange(d)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun AdDateInputRow(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit
) {
    val supportedYears = remember { (2013..2034).toList() }
    val maxDays = remember(selectedYear, selectedMonth) {
        val cal = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth - 1, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // AD Year Dropdown
        DateDropdownSelector(
            label = "Year (AD)",
            currentValue = "$selectedYear AD",
            modifier = Modifier.weight(1.1f),
            testTag = "ad_year_selector"
        ) { onDismiss ->
            supportedYears.forEach { yr ->
                DropdownMenuItem(
                    text = { Text("$yr AD", color = TextWhite, fontSize = 13.sp) },
                    onClick = {
                        onYearChange(yr)
                        onDismiss()
                    }
                )
            }
        }

        // AD Month Dropdown
        DateDropdownSelector(
            label = "Month",
            currentValue = NepaliCalendar.GREGORIAN_MONTHS_EN[selectedMonth - 1],
            modifier = Modifier.weight(1.4f),
            testTag = "ad_month_selector"
        ) { onDismiss ->
            NepaliCalendar.GREGORIAN_MONTHS_EN.forEachIndexed { idx, mName ->
                DropdownMenuItem(
                    text = { Text("${idx + 1}. $mName", color = TextWhite, fontSize = 13.sp) },
                    onClick = {
                        onMonthChange(idx + 1)
                        onDismiss()
                    }
                )
            }
        }

        // AD Day Dropdown
        DateDropdownSelector(
            label = "Day",
            currentValue = selectedDay.toString(),
            modifier = Modifier.weight(0.9f),
            testTag = "ad_day_selector"
        ) { onDismiss ->
            (1..maxDays).forEach { d ->
                DropdownMenuItem(
                    text = { Text(d.toString(), color = TextWhite, fontSize = 13.sp) },
                    onClick = {
                        onDayChange(d)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun DateDropdownSelector(
    label: String,
    currentValue: String,
    modifier: Modifier = Modifier,
    testTag: String,
    menuContent: @Composable (onDismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = label, color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceDark)
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentValue,
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SkyBlueBright,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(SurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            ) {
                menuContent { expanded = false }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(text = label, color = TextSubtle, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
