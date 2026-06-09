package com.simplifybiz.ops.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Date formatting helpers for the mobile app.
 *
 * The server returns dates in two forms:
 *   - "2026-07-07" — date fields like date_due
 *   - "2026-06-04 10:59:45" — created_at/updated_at timestamps
 *
 * The plugin accepts MM/DD/YYYY for date_due when submitting.
 * For display we want human-readable: "Jul 7, 2026" and "Jun 4, 2026 at 10:59 AM".
 */

private val MONTHS_SHORT = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

/**
 * Format a date string for display. Accepts both YYYY-MM-DD and MM/DD/YYYY.
 * Returns "Jul 7, 2026" or the raw input if parsing fails.
 */
fun formatDateForDisplay(raw: String): String {
    if (raw.isBlank()) return ""
    val parsed = parseAnyDate(raw) ?: return raw
    val month = MONTHS_SHORT[parsed.monthNumber - 1]
    return "$month ${parsed.dayOfMonth}, ${parsed.year}"
}

/**
 * Format a timestamp like "2026-06-04 10:59:45" as "Jun 4, 2026 at 10:59 AM"
 */
fun formatDateTimeForDisplay(raw: String): String {
    if (raw.isBlank()) return ""
    val parts = raw.trim().split(" ", "T")
    val datePart = parts.getOrNull(0) ?: return raw
    val timePart = parts.getOrNull(1) ?: ""

    val parsed = parseAnyDate(datePart) ?: return raw
    val dateStr = "${MONTHS_SHORT[parsed.monthNumber - 1]} ${parsed.dayOfMonth}, ${parsed.year}"

    if (timePart.isBlank()) return dateStr

    val timeBits = timePart.split(":")
    val hourRaw = timeBits.getOrNull(0)?.toIntOrNull() ?: return dateStr
    val minute = timeBits.getOrNull(1)?.padStart(2, '0') ?: "00"
    val ampm = if (hourRaw >= 12) "PM" else "AM"
    val hour12 = when {
        hourRaw == 0 -> 12
        hourRaw > 12 -> hourRaw - 12
        else -> hourRaw
    }
    return "$dateStr at $hour12:$minute $ampm"
}

/**
 * Parse a date in either YYYY-MM-DD or MM/DD/YYYY format. Returns null on failure.
 */
fun parseAnyDate(raw: String): LocalDate? {
    val trimmed = raw.trim()
    return try {
        when {
            trimmed.contains("-") -> {
                val parts = trimmed.split("-")
                if (parts.size != 3) return null
                LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            }
            trimmed.contains("/") -> {
                val parts = trimmed.split("/")
                if (parts.size != 3) return null
                LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert a LocalDate to MM/DD/YYYY string (what the API expects when submitting).
 */
fun LocalDate.toApiDateString(): String {
    val mm = monthNumber.toString().padStart(2, '0')
    val dd = dayOfMonth.toString().padStart(2, '0')
    return "$mm/$dd/$year"
}

/**
 * Convert epoch millis (from DatePicker) to a LocalDate in the system timezone.
 */
fun millisToLocalDate(millis: Long): LocalDate {
    return Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.UTC)
        .date
}
