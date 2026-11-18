package com.amirmalik.breatheasy.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StockPolicyTest {
    @Test
    fun allowsSmokingWhenTrackedStockIsPositive() {
        StockPolicy.requireAvailable(remainingCigarettes = 1L)
        StockPolicy.requireAvailable(remainingCigarettes = 12L)
    }

    @Test
    fun requiresConfirmationWhenTrackedStockIsEmpty() {
        val error = assertThrows(TrackedStockUnavailableException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = 0L)
        }
        assertEquals(0L, error.remainingCigarettes)
    }

    @Test
    fun requiresConfirmationWhenTrackedStockIsAlreadyNegative() {
        val error = assertThrows(TrackedStockUnavailableException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = -4L)
        }
        assertEquals(-4L, error.remainingCigarettes)
    }

    @Test
    fun explicitConfirmationAllowsRecordingActualSmokingBelowZero() {
        StockPolicy.requireAvailable(remainingCigarettes = 0L, allowNegativeStock = true)
        StockPolicy.requireAvailable(remainingCigarettes = -4L, allowNegativeStock = true)
    }
}
