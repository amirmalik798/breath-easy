package com.amirmalik.breatheasy.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSnapshotTest {
    @Test
    fun dailyConsumptionUsesLongArithmeticForLongHistories() {
        val snapshot = AppSnapshot(
            smokeEvents = listOf(
                SmokeEvent(smokedAtMillis = 100L, quantity = Int.MAX_VALUE),
                SmokeEvent(smokedAtMillis = 200L, quantity = Int.MAX_VALUE),
                SmokeEvent(smokedAtMillis = 300L, quantity = 10),
            ),
        )

        assertEquals(Int.MAX_VALUE.toLong() * 2L + 10L, snapshot.cigarettesFor(100L, 301L))
    }

    @Test
    fun dailyConsumptionUsesAnExclusiveEndBoundary() {
        val snapshot = AppSnapshot(
            smokeEvents = listOf(
                SmokeEvent(smokedAtMillis = 99L, quantity = 4),
                SmokeEvent(smokedAtMillis = 100L, quantity = 1),
                SmokeEvent(smokedAtMillis = 199L, quantity = 2),
                SmokeEvent(smokedAtMillis = 200L, quantity = 8),
            ),
        )

        assertEquals(3L, snapshot.cigarettesFor(100L, 200L))
    }
}
