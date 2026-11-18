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
    fun requiresConfirmationWhenEntryExceedsAvailableStock() {
        val error = assertThrows(TrackedStockUnavailableException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = 1L, requestedCigarettes = 2)
        }
        assertEquals(1L, error.remainingCigarettes)
        assertEquals(2, error.requestedCigarettes)
    }

    @Test
    fun confirmationAllowsActualSmokingWithoutAuthorizingNegativeStock() {
        StockPolicy.requireAvailable(remainingCigarettes = 0L, confirmUnstockedSmoking = true)
        StockPolicy.requireAvailable(
            remainingCigarettes = 1L,
            requestedCigarettes = 3,
            confirmUnstockedSmoking = true,
        )
    }

    @Test
    fun confirmedSmokingAtZeroStockRecordsZeroStockDebit() {
        assertEquals(
            0,
            StockPolicy.stockDebit(
                remainingCigarettes = 0L,
                requestedCigarettes = 1,
                confirmUnstockedSmoking = true,
            ),
        )
    }

    @Test
    fun confirmedMultiCigaretteEntryDebitsOnlyWhatIsAvailable() {
        assertEquals(
            2,
            StockPolicy.stockDebit(
                remainingCigarettes = 2L,
                requestedCigarettes = 5,
                confirmUnstockedSmoking = true,
            ),
        )
    }

    @Test
    fun fullyCoveredEntryDebitsItsFullQuantity() {
        assertEquals(
            2,
            StockPolicy.stockDebit(
                remainingCigarettes = 8L,
                requestedCigarettes = 2,
            ),
        )
    }

    @Test
    fun rejectsInvalidRequestedQuantityAndNegativeInventory() {
        assertThrows(IllegalArgumentException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = 5L, requestedCigarettes = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            StockPolicy.requireAvailable(remainingCigarettes = -1L)
        }
    }
}
