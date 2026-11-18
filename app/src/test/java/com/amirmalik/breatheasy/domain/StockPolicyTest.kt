package com.amirmalik.breatheasy.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StockPolicyTest {
    @Test
    fun allowsSmokingWhenTrackedStockCoversTheEntry() {
        StockPolicy.requireAvailable(remainingCigarettes = 1L)
        StockPolicy.requireAvailable(remainingCigarettes = 12L)
        StockPolicy.requireAvailable(remainingCigarettes = 12L, requestedCigarettes = 12)
    }

    @Test
    fun requiresConfirmationWhenTrackedStockIsEmpty() {
        val error = assertThrows(TrackedStockUnavailableException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = 0L)
        }
        assertEquals(0L, error.remainingCigarettes)
        assertEquals(1, error.requestedCigarettes)
    }

    @Test
    fun requiresConfirmationWhenTrackedStockIsAlreadyNegative() {
        val error = assertThrows(TrackedStockUnavailableException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = -4L)
        }
        assertEquals(-4L, error.remainingCigarettes)
    }

    @Test
    fun requiresConfirmationWhenAnEntryExceedsPositiveStock() {
        val error = assertThrows(TrackedStockUnavailableException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = 1L, requestedCigarettes = 2)
        }
        assertEquals(1L, error.remainingCigarettes)
        assertEquals(2, error.requestedCigarettes)
    }

    @Test
    fun explicitConfirmationAllowsRecordingActualSmokingBelowAvailableStock() {
        StockPolicy.requireAvailable(remainingCigarettes = 0L, allowNegativeStock = true)
        StockPolicy.requireAvailable(remainingCigarettes = -4L, allowNegativeStock = true)
        StockPolicy.requireAvailable(
            remainingCigarettes = 1L,
            requestedCigarettes = 3,
            allowNegativeStock = true,
        )
    }

    @Test
    fun rejectsInvalidRequestedQuantity() {
        assertThrows(IllegalArgumentException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = 5L, requestedCigarettes = 0)
        }
    }
}
