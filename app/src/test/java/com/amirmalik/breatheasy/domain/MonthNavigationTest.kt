package com.amirmalik.breatheasy.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class MonthNavigationTest {
    private val currentMonth = YearMonth.of(2026, 11)

    @Test
    fun returnsExactOffsetsWithinTheSupportedWindow() {
        assertEquals(0, MonthNavigation.offsetForMonth(currentMonth, currentMonth))
        assertEquals(1, MonthNavigation.offsetForMonth(YearMonth.of(2026, 10), currentMonth))
        assertEquals(60, MonthNavigation.offsetForMonth(YearMonth.of(2021, 11), currentMonth))
    }

    @Test
    fun refusesMonthsOutsideTheSupportedWindowInsteadOfClampingThem() {
        assertNull(MonthNavigation.offsetForMonth(YearMonth.of(2021, 10), currentMonth))
        assertNull(MonthNavigation.offsetForMonth(YearMonth.of(2026, 12), currentMonth))
    }

    @Test
    fun nullAnchorFollowsTheNewCurrentMonth() {
        assertEquals(
            YearMonth.of(2026, 12),
            MonthNavigation.selectedMonth(null, YearMonth.of(2026, 12)),
        )
    }

    @Test
    fun explicitSelectionRemainsOnTheSameMonthWhenCurrentMonthAdvances() {
        val selected = YearMonth.of(2026, 10)

        assertEquals(selected, MonthNavigation.selectedMonth(selected, YearMonth.of(2026, 11)))
        assertEquals(selected, MonthNavigation.selectedMonth(selected, YearMonth.of(2026, 12)))
    }

    @Test
    fun absoluteSelectionIsBoundedByTheSupportedHistoryWindow() {
        assertEquals(
            YearMonth.of(2021, 12),
            MonthNavigation.selectedMonth(YearMonth.of(2021, 10), YearMonth.of(2026, 12)),
        )
    }
}
