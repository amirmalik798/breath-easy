package com.amirmalik.breatheasy.ui.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.YearMonth

class SpendingReportPeriodTest {
    @Test
    fun everyPeriodEndsAtTheSelectedCurrentMonth() {
        val currentMonth = YearMonth.of(2026, 11)

        assertEquals(
            listOf(YearMonth.of(2026, 11)),
            SpendingReportPeriod.CURRENT_MONTH.monthsEndingAt(currentMonth),
        )
        assertEquals(
            listOf(YearMonth.of(2026, 9), YearMonth.of(2026, 10), YearMonth.of(2026, 11)),
            SpendingReportPeriod.LAST_3_MONTHS.monthsEndingAt(currentMonth),
        )
        assertEquals(6, SpendingReportPeriod.LAST_6_MONTHS.monthsEndingAt(currentMonth).size)
        assertEquals(12, SpendingReportPeriod.LAST_12_MONTHS.monthsEndingAt(currentMonth).size)
    }

    @Test
    fun reportingWindowRollsAcrossYearBoundariesInChronologicalOrder() {
        val months = SpendingReportPeriod.LAST_3_MONTHS.monthsEndingAt(YearMonth.of(2026, 1))

        assertEquals(
            listOf(YearMonth.of(2025, 11), YearMonth.of(2025, 12), YearMonth.of(2026, 1)),
            months,
        )
        assertFalse(months.zipWithNext().any { (left, right) -> !left.isBefore(right) })
    }
}
