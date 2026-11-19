package com.amirmalik.breatheasy.domain

import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Bounds the spending screen's month navigation while keeping history-row selection exact.
 * A month outside the supported window is not clickable rather than being clamped to
 * a different month.
 */
object MonthNavigation {
    const val MAX_MONTH_OFFSET = 60

    /**
     * Resolves an absolute month selection against the current month.
     * A null anchor follows the current month; a concrete anchor remains stable
     * when the calendar advances, subject only to the supported history window.
     */
    fun selectedMonth(anchor: YearMonth?, currentMonth: YearMonth): YearMonth {
        val requested = anchor ?: currentMonth
        val oldestSupported = currentMonth.minusMonths(MAX_MONTH_OFFSET.toLong())
        return requested.coerceIn(oldestSupported, currentMonth)
    }

    fun offsetForMonth(targetMonth: YearMonth, currentMonth: YearMonth): Int? {
        val offset = ChronoUnit.MONTHS.between(targetMonth, currentMonth)
        return offset.toInt().takeIf { offset in 0L..MAX_MONTH_OFFSET.toLong() }
    }
}
