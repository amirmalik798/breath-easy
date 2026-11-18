package com.amirmalik.breatheasy.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import com.amirmalik.breatheasy.domain.DateTimeRanges
import java.time.ZoneId

class DayRangeTest {
    private val losAngeles = ZoneId.of("America/Los_Angeles")

    @Test
    fun localDayWindowIsTwentyThreeHoursWhenDaylightSavingStarts() {
        val range = DateTimeRanges.dayRangeMillis(LocalDate.of(2026, 3, 8), losAngeles)
        assertEquals(23L * 60L * 60L * 1_000L, range.last - range.first + 1L)
    }

    @Test
    fun localDayWindowIsTwentyFiveHoursWhenDaylightSavingEnds() {
        val range = BreatheEasyViewModel.dayRangeMillis(LocalDate.of(2026, 11, 1), losAngeles)
        assertEquals(25L * 60L * 60L * 1_000L, range.last - range.first + 1L)
    }

    @Test
    fun localDayWindowIncludesStartAndExcludesNextMidnight() {
        val date = LocalDate.of(2026, 6, 12)
        val range = BreatheEasyViewModel.dayRangeMillis(date, losAngeles)
        assertEquals(date.atStartOfDay(losAngeles).toInstant().toEpochMilli(), range.first)
        assertEquals(date.plusDays(1).atStartOfDay(losAngeles).toInstant().toEpochMilli(), range.last + 1L)
    }
}
