package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Robust Nepali Bikram Sambat (BS) and Gregorian (AD) Dual Calendar Utility
 * Supports accurate BS <-> AD conversions, dual date formatting, and fiscal year calculations.
 * Integrates with [NepaliCalendar] for 2070-2090 BS date tables.
 */
object NepaliDateUtils {

    val NEPALI_MONTHS_EN = NepaliCalendar.NEPALI_MONTHS_EN
    val NEPALI_MONTHS_NP = NepaliCalendar.NEPALI_MONTHS_NP

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

        val formattedBsNepaliDigits: String
            get() = NepaliCalendar.toNepaliDigits(formattedBs)

        val fiscalYear: String
            get() = if (month >= 4) "FY $year/${(year + 1) % 100}" else "FY ${year - 1}/${year % 100}"
    }

    /**
     * Converts AD timestamp in milliseconds to NepaliDate (BS).
     */
    fun adToBs(dateMillis: Long): NepaliDate {
        val dual = NepaliCalendar.adMillisToDual(dateMillis)
        return NepaliDate(dual.bsDate.year, dual.bsDate.month, dual.bsDate.day)
    }

    /**
     * Converts BS Year, Month, Day to AD timestamp millis.
     */
    fun bsToAd(year: Int, month: Int, day: Int): Long {
        return NepaliCalendar.bsToDual(year, month, day).timeMillis
    }

    /**
     * Converts BS NepaliDate to AD timestamp millis.
     */
    fun bsToAd(nepaliDate: NepaliDate): Long {
        return bsToAd(nepaliDate.year, nepaliDate.month, nepaliDate.day)
    }

    /**
     * Returns the maximum days in a given BS month (29 to 32 days).
     */
    fun getDaysInBsMonth(year: Int, month: Int): Int {
        return NepaliCalendar.getDaysInBsMonth(year, month)
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

    fun getNepaliDayOfWeek(dateMillis: Long): String {
        val dual = NepaliCalendar.adMillisToDual(dateMillis)
        return dual.bsDate.dayOfWeekNp
    }

    fun getEnglishDayOfWeek(dateMillis: Long): String {
        val dual = NepaliCalendar.adMillisToDual(dateMillis)
        return dual.adDate.dayOfWeekEn
    }

    fun getFiscalYear(dateMillis: Long): String {
        return adToBs(dateMillis).fiscalYear
    }

    fun toNepaliDigits(input: String): String {
        return NepaliCalendar.toNepaliDigits(input)
    }

    fun parseBsString(bsString: String): NepaliDate? {
        val parts = bsString.trim().split("/", "-", ".")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            val maxD = getDaysInBsMonth(y, m)
            return NepaliDate(y, m.coerceIn(1, 12), d.coerceIn(1, maxD))
        }
        return null
    }

    /**
     * Converts a BS string (YYYY/MM/DD) to formatted Gregorian AD date string (e.g. "17 Sep 2026").
     */
    fun convertBsStringToAdDateString(bsString: String, formatPattern: String = "dd MMM yyyy"): String? {
        val bsDate = parseBsString(bsString) ?: return null
        val millis = bsToAd(bsDate)
        val sdf = SimpleDateFormat(formatPattern, Locale.US)
        return sdf.format(Date(millis))
    }

    /**
     * Converts a Gregorian AD date string to BS date string (YYYY/MM/DD).
     */
    fun convertAdStringToBsString(adString: String, formatPattern: String = "yyyy-MM-dd"): String? {
        return try {
            val sdf = SimpleDateFormat(formatPattern, Locale.US)
            val date = sdf.parse(adString) ?: return null
            formatBsDate(date.time)
        } catch (e: Exception) {
            null
        }
    }
}
