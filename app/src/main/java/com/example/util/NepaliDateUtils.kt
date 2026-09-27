package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Robust Nepali Bikram Sambat (BS) and Gregorian (AD) Dual Calendar Utility
 * Supports accurate BS <-> AD conversions, dual date formatting, and fiscal year calculations.
 */
object NepaliDateUtils {

    val NEPALI_MONTHS_EN = listOf(
        "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    )

    val NEPALI_MONTHS_NP = listOf(
        "बैशाख", "जेठ", "असार", "साउन", "भाद्र", "असोज",
        "कार्तिक", "मंसिर", "पुष", "माघ", "फागुन", "चैत"
    )

    data class NepaliDate(
        val year: Int,
        val month: Int, // 1 to 12
        val day: Int
    ) {
        val monthNameEn: String
            get() = NEPALI_MONTHS_EN.getOrElse(month - 1) { "Baisakh" }

        val monthNameNp: String
            get() = NEPALI_MONTHS_NP.getOrElse(month - 1) { "बैशाख" }

        val formattedBs: String
            get() = String.format(Locale.US, "%04d/%02d/%02d", year, month, day)

        val formattedBsWithMonth: String
            get() = "$formattedBs ($monthNameEn)"

        val fiscalYear: String
            get() = if (month >= 4) "FY $year/${(year + 1) % 100}" else "FY ${year - 1}/${year % 100}"
    }

    // Days in Nepali months for Bikram Sambat years 2078 to 2086
    private val bsYearData = mapOf(
        2078 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2079 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2080 to intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2081 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2082 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2083 to intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 29, 31),
        2084 to intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
        2085 to intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2086 to intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30)
    )

    // Anchor: 2080/01/01 BS corresponds to 2023-04-14 AD (UTC)
    private val ANCHOR_BS_YEAR = 2080
    private val ANCHOR_AD_MILLIS: Long = run {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kathmandu"), Locale.US)
        cal.clear()
        cal.set(2023, Calendar.APRIL, 14, 0, 0, 0)
        cal.timeInMillis
    }

    /**
     * Converts AD timestamp in milliseconds to NepaliDate (BS).
     */
    fun adToBs(dateMillis: Long): NepaliDate {
        val targetCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kathmandu"), Locale.US).apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val anchorCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kathmandu"), Locale.US).apply {
            timeInMillis = ANCHOR_AD_MILLIS
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var diffDays = ((targetCal.timeInMillis - anchorCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        if (diffDays >= 0) {
            var bsYear = ANCHOR_BS_YEAR
            var bsMonth = 1
            var bsDay = 1

            while (diffDays > 0) {
                val monthDays = getDaysInBsMonth(bsYear, bsMonth)
                val remainingDaysInMonth = monthDays - bsDay + 1
                if (diffDays >= remainingDaysInMonth) {
                    diffDays -= remainingDaysInMonth
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
            return NepaliDate(bsYear, bsMonth, bsDay)
        } else {
            // Dates before anchor (approximate backward calculation)
            var absDiff = -diffDays
            var bsYear = ANCHOR_BS_YEAR
            var bsMonth = 1
            var bsDay = 1

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
            return NepaliDate(bsYear, bsMonth, bsDay)
        }
    }

    /**
     * Converts BS Year, Month, Day to approximate AD timestamp millis.
     */
    fun bsToAd(year: Int, month: Int, day: Int): Long {
        var totalDays = 0
        if (year >= ANCHOR_BS_YEAR) {
            for (y in ANCHOR_BS_YEAR until year) {
                val months = bsYearData[y] ?: bsYearData[2082]!!
                totalDays += months.sum()
            }
            for (m in 1 until month) {
                totalDays += getDaysInBsMonth(year, m)
            }
            totalDays += (day - 1)
            return ANCHOR_AD_MILLIS + (totalDays.toLong() * 24 * 60 * 60 * 1000L)
        } else {
            for (y in year until ANCHOR_BS_YEAR) {
                val months = bsYearData[y] ?: bsYearData[2080]!!
                totalDays += months.sum()
            }
            for (m in 1 until month) {
                totalDays -= getDaysInBsMonth(year, m)
            }
            totalDays -= (day - 1)
            return ANCHOR_AD_MILLIS - (totalDays.toLong() * 24 * 60 * 60 * 1000L)
        }
    }

    fun getDaysInBsMonth(year: Int, month: Int): Int {
        val months = bsYearData[year] ?: bsYearData[2082]!!
        val mIndex = (month - 1).coerceIn(0, 11)
        return months[mIndex]
    }

    fun formatBsDate(dateMillis: Long): String {
        return adToBs(dateMillis).formattedBs
    }

    fun formatBsDateWithMonth(dateMillis: Long): String {
        return adToBs(dateMillis).formattedBsWithMonth
    }

    fun formatAdDate(dateMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        return sdf.format(Date(dateMillis))
    }

    fun formatAdDateTime(dateMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        return sdf.format(Date(dateMillis))
    }

    fun getTodayBs(): NepaliDate {
        return adToBs(System.currentTimeMillis())
    }

    fun getTodayBsString(): String {
        return getTodayBs().formattedBs
    }

    fun getTodayAdString(): String {
        return formatAdDate(System.currentTimeMillis())
    }

    fun parseBsString(bsString: String): NepaliDate? {
        val parts = bsString.trim().split("/", "-", ".")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            return NepaliDate(y, m.coerceIn(1, 12), d.coerceIn(1, 32))
        }
        return null
    }
}
