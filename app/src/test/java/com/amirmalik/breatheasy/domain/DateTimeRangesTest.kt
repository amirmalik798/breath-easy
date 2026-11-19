package com.amirmalik.breatheasy.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

class DateTimeRangesTest {
    @Test
    fun springForwardDayContainsTwentyThreeHours() {
        assertDayLength(
            date = LocalDate.of(2026, 3, 8),
            zone = ZoneId.of("America/New_York"),
            expectedHours = 23,
        )
    }

    @Test
    fun fallBackDayContainsTwentyFiveHours() {
        assertDayLength(
            date = LocalDate.of(2026, 11, 1),
            zone = ZoneId.of("America/New_York"),
            expectedHours = 25,
        )
    }

    @Test
    fun ordinaryDayContainsTwentyFourHours() {
        assertDayLength(
            date = LocalDate.of(2026, 11, 19),
            zone = ZoneId.of("Africa/Kigali"),
            expectedHours = 24,
        )
    }

    private fun assertDayLength(date: LocalDate, zone: ZoneId, expectedHours: Long) {
        val range = DateTimeRanges.dayRangeMillis(date, zone)
        assertEquals(
            Duration.ofHours(expectedHours).toMillis(),
            range.last - range.first + 1L,
        )
        assertEquals(date.atStartOfDay(zone).toInstant().toEpochMilli(), range.first)
        assertEquals(date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1L, range.last)
    }
}
