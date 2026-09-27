package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * High-precision Nepali Calendar (Bikram Sambat - BS) and Gregorian (AD) Utility
 * Supports bidirectional seamless conversion, Devanagari numerals, fiscal years,
 * and comprehensive date formatting.
 */
object NepaliCalendar {

    val NEPALI_MONTHS_EN = listOf(
        "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    )

    val NEPALI_MONTHS_NP = listOf(
        "बैशाख", "जेठ", "असार", "साउन", "भाद्र", "असोज",
        "कार्तिक", "मंसिर", "पुष", "माघ", "फागुन", "चैत"
    )

    val GREGORIAN_MONTHS_EN = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val WEEKDAYS_EN = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    val WEEKDAYS_NP = listOf(
        "आइतबार", "सोमबार", "मंगलबार", "बुधबार", "बिहीबार", "शुक्रबार", "शनिबार"
    )

    private val NEPALI_DIGITS = mapOf(
        '0' to '०', '1' to '१', '2' to '२', '3' to '३', '4' to '४',
        '5' to '५', '6' to '६', '7' to '७', '8' to '८', '9' to '९'
    )

    data class BsDate(
        val year: Int,
        val month: Int, // 1 to 12
        val day: Int,
        val dayOfWeekIndex: Int = 1 // 1=Sun, 7=Sat
    ) {
        val monthNameEn: String get() = NEPALI_MONTHS_EN.getOrElse(month - 1) { "Baisakh" }
        val monthNameNp: String get() = NEPALI_MONTHS_NP.getOrElse(month - 1) { "बैशाख" }
        val dayOfWeekEn: String get() = WEEKDAYS_EN.getOrElse(dayOfWeekIndex - 1) { "Sunday" }
        val dayOfWeekNp: String get() = WEEKDAYS_NP.getOrElse(dayOfWeekIndex - 1) { "आइतबार" }

        val formattedBs: String
            get() = String.format(Locale.US, "%04d/%02d/%02d", year, month, day)

        val formattedBsNepali: String
            get() = toNepaliDigits(String.format(Locale.US, "%04d/%02d/%02d", year, month, day))

        val formattedLongEn: String
            get() = "$day $monthNameEn $year, $dayOfWeekEn"

        val formattedLongNp: String
            get() = "${toNepaliDigits(day.toString())} $monthNameNp ${toNepaliDigits(year.toString())}, $dayOfWeekNp"

        val fiscalYear: String
            get() = if (month >= 4) "FY $year/${(year + 1) % 100}" else "FY ${year - 1}/${year % 100}"
    }

    data class AdDate(
        val year: Int,
        val month: Int, // 1 to 12
        val day: Int,
        val dayOfWeekIndex: Int = 1
    ) {
        val monthNameEn: String get() = GREGORIAN_MONTHS_EN.getOrElse(month - 1) { "January" }
        val dayOfWeekEn: String get() = WEEKDAYS_EN.getOrElse(dayOfWeekIndex - 1) { "Sunday" }
        val dayOfWeekNp: String get() = WEEKDAYS_NP.getOrElse(dayOfWeekIndex - 1) { "आइतबार" }

        val formattedAd: String
            get() = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

        val formattedLongEn: String
            get() = "$day $monthNameEn $year, $dayOfWeekEn"
    }

    data class ConversionResult(
        val bsDate: BsDate,
        val adDate: AdDate,
        val timeMillis: Long,
        val daysFromToday: Int,
        val relativeDescription: String
    )

    // Number of days in BS months (years 2070 to 2090)
    private val bsMonthDaysMap = mapOf(
        2070 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2071 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2072 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2073 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2074 to intArrayOf(31, 31, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30),
        2075 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2076 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2077 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2078 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2079 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2080 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2081 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2082 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2083 to intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 29, 31),
        2084 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2085 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2086 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2087 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
        2088 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2089 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2090 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30)
    )

    // Base Anchor: 2080-01-01 BS corresponds to 2023-04-14 AD
    private const val ANCHOR_BS_YEAR = 2080
    private const val ANCHOR_BS_MONTH = 1
    private const val ANCHOR_BS_DAY = 1

    private val ANCHOR_CALENDAR: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kathmandu"), Locale.US).apply {
        clear()
        set(2023, Calendar.APRIL, 14, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val ANCHOR_AD_MILLIS: Long = ANCHOR_CALENDAR.timeInMillis

    fun toNepaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(NEPALI_DIGITS[ch] ?: ch)
        }
        return sb.toString()
    }

    fun getDaysInBsMonth(year: Int, month: Int): Int {
        val yearData = bsMonthDaysMap[year] ?: bsMonthDaysMap[2082]!!
        val mIdx = (month - 1).coerceIn(0, 11)
        return yearData[mIdx]
    }

    fun getSupportedBsYears(): List<Int> {
        return (2070..2090).toList()
    }

    /**
     * Convert Gregorian AD date millis into both BS and AD models.
     */
    fun adMillisToDual(adMillis: Long): ConversionResult {
        val targetCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kathmandu"), Locale.US).apply {
            timeInMillis = adMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val adYear = targetCal.get(Calendar.YEAR)
        val adMonth = targetCal.get(Calendar.MONTH) + 1
        val adDay = targetCal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat

        val anchorCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kathmandu"), Locale.US).apply {
            timeInMillis = ANCHOR_AD_MILLIS
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var diffDays = ((targetCal.timeInMillis - anchorCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        var bsYear = ANCHOR_BS_YEAR
        var bsMonth = ANCHOR_BS_MONTH
        var bsDay = ANCHOR_BS_DAY

        if (diffDays >= 0) {
            while (diffDays > 0) {
                val monthDays = getDaysInBsMonth(bsYear, bsMonth)
                val remaining = monthDays - bsDay + 1
                if (diffDays >= remaining) {
                    diffDays -= remaining
                    bsDay = 1
                    bsMonth++
                    if (bsMonth > 12) {
                        bsMonth = 1
                        bsYear++
                    }
                } else {
                    bsDay += diffDays
                    diffDays = 0
                }
            }
        } else {
            var absDiff = -diffDays
            while (absDiff > 0) {
                bsMonth--
                if (bsMonth < 1) {
                    bsMonth = 12
                    bsYear--
                }
                val monthDays = getDaysInBsMonth(bsYear, bsMonth)
                if (absDiff > monthDays) {
                    absDiff -= monthDays
                } else {
                    bsDay = monthDays - absDiff + 1
                    absDiff = 0
                }
            }
        }

        val bsDate = BsDate(bsYear, bsMonth, bsDay, dayOfWeek)
        val adDate = AdDate(adYear, adMonth, adDay, dayOfWeek)

        val todayCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kathmandu"), Locale.US).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val daysDiff = ((targetCal.timeInMillis - todayCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        val relative = when {
            daysDiff == 0 -> "Today"
            daysDiff == -1 -> "Yesterday"
            daysDiff == 1 -> "Tomorrow"
            daysDiff < 0 -> "${-daysDiff} days ago"
            else -> "In $daysDiff days"
        }

        return ConversionResult(
            bsDate = bsDate,
            adDate = adDate,
            timeMillis = targetCal.timeInMillis,
            daysFromToday = daysDiff,
            relativeDescription = relative
        )
    }

    /**
     * Convert BS Date (Year, Month, Day) to AD timestamp and dual model.
     */
    fun bsToDual(bsYear: Int, bsMonth: Int, bsDay: Int): ConversionResult {
        val validYear = bsYear.coerceIn(2070, 2090)
        val validMonth = bsMonth.coerceIn(1, 12)
        val maxDays = getDaysInBsMonth(validYear, validMonth)
        val validDay = bsDay.coerceIn(1, maxDays)

        var totalDays = 0
        if (validYear >= ANCHOR_BS_YEAR) {
            for (y in ANCHOR_BS_YEAR until validYear) {
                val months = bsMonthDaysMap[y] ?: bsMonthDaysMap[2082]!!
                totalDays += months.sum()
            }
            for (m in 1 until validMonth) {
                totalDays += getDaysInBsMonth(validYear, m)
            }
            totalDays += (validDay - 1)
        } else {
            for (y in validYear until ANCHOR_BS_YEAR) {
                val months = bsMonthDaysMap[y] ?: bsMonthDaysMap[2080]!!
                totalDays -= months.sum()
            }
            for (m in 1 until validMonth) {
                totalDays += getDaysInBsMonth(validYear, m)
            }
            totalDays += (validDay - 1)
        }

        val targetMillis = ANCHOR_AD_MILLIS + (totalDays.toLong() * 24 * 60 * 60 * 1000L)
        return adMillisToDual(targetMillis)
    }

    /**
     * Convert Gregorian AD Year, Month (1-12), Day to dual model.
     */
    fun adToDual(adYear: Int, adMonth: Int, adDay: Int): ConversionResult {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kathmandu"), Locale.US).apply {
            clear()
            set(adYear, adMonth - 1, adDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return adMillisToDual(cal.timeInMillis)
    }

    fun getTodayDual(): ConversionResult {
        return adMillisToDual(System.currentTimeMillis())
    }

    fun parseBsString(str: String): BsDate? {
        val parts = str.trim().split("/", "-", ".")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            val maxD = getDaysInBsMonth(y, m)
            return BsDate(y, m.coerceIn(1, 12), d.coerceIn(1, maxD))
        }
        return null
    }

    fun parseAdString(str: String): AdDate? {
        val parts = str.trim().split("/", "-", ".")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            return AdDate(y, m.coerceIn(1, 12), d.coerceIn(1, 31))
        }
        return null
    }
}
