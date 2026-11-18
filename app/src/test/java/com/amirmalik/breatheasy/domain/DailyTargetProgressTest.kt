package com.amirmalik.breatheasy.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyTargetProgressTest {
    @Test
    fun belowTargetShowsRemainingAmountAndPartialProgress() {
        val result = DailyTargetProgress.calculate(consumed = 7L, target = 10)

        assertEquals(0.7f, result.progressFraction, 0.0001f)
        assertEquals(3L, result.remainingCigarettes)
        assertEquals(0L, result.excessCigarettes)
        assertEquals("3 below target", result.statusLabel)
    }

    @Test
    fun exactTargetIsReportedAsReached() {
        val result = DailyTargetProgress.calculate(consumed = 10L, target = 10)

        assertEquals(1f, result.progressFraction)
        assertEquals(0L, result.remainingCigarettes)
        assertEquals(0L, result.excessCigarettes)
        assertEquals("Target reached", result.statusLabel)
    }

    @Test
    fun exceedingTargetIsNotReportedAsTargetReached() {
        val result = DailyTargetProgress.calculate(consumed = 11L, target = 10)

        assertEquals(1f, result.progressFraction)
        assertEquals(0L, result.remainingCigarettes)
        assertEquals(1L, result.excessCigarettes)
        assertEquals("1 over target", result.statusLabel)
    }

    @Test
    fun veryLargeConsumptionKeepsProgressBoundedAndUsesLongExcess() {
        val result = DailyTargetProgress.calculate(consumed = Int.MAX_VALUE.toLong() + 100L, target = 10)

        assertEquals(1f, result.progressFraction)
        assertEquals(Int.MAX_VALUE.toLong() + 90L, result.excessCigarettes)
    }

    @Test
    fun invalidTargetAndNegativeConsumptionAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            DailyTargetProgress.calculate(consumed = 1L, target = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyTargetProgress.calculate(consumed = -1L, target = 10)
        }
    }
}
