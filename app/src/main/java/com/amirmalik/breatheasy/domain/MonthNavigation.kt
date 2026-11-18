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

    fun offsetForMonth(targetMonth: YearMonth, currentMonth: YearMonth): Int? {
        val offset = ChronoUnit.MONTHS.between(targetMonth, currentMonth)
        return offset.toInt().takeIf { offset in 0L..MAX_MONTH_OFFSET.toLong() }
    }
}
