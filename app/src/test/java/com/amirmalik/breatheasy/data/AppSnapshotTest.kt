package com.amirmalik.breatheasy.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSnapshotTest {
    @Test
    fun smokingTotalsIncludeCigarettesRecordedWithoutTrackedStock() {
        val snapshot = AppSnapshot(
            preferences = AppPreferences(displayCurrency = CurrencyCode.fromCode("USD")),
            smokeEvents = listOf(
                SmokeEvent(quantity = 1, stockQuantity = 0),
                SmokeEvent(quantity = 2, stockQuantity = 1),
            ),
            isLoading = false,
        )

        assertEquals(3L, snapshot.cigarettesFor(0L, Long.MAX_VALUE))
    }

    @Test
    fun dailyConsumptionUsesLongArithmeticForLongHistories() {
        val snapshot = AppSnapshot(
            preferences = AppPreferences(displayCurrency = CurrencyCode.fromCode("USD")),
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
            preferences = AppPreferences(displayCurrency = CurrencyCode.fromCode("USD")),
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
