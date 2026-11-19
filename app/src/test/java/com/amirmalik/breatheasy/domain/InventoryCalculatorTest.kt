package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.PurchaseType
import com.amirmalik.breatheasy.data.SmokeEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class InventoryCalculatorTest {
    private val usd = CurrencyCode.fromCode("USD")

    @Test
    fun stockIsOpeningBalancePlusRecordedCigarettesLessSmokingEvents() {
        val purchases = listOf(
            Purchase(type = PurchaseType.PACK, quantity = 2, amountMinor = 2_000, currency = CurrencyCode.fromCode("RWF")),
            Purchase(type = PurchaseType.SINGLE, quantity = 3, amountMinor = 300, currency = CurrencyCode.fromCode("RWF")),
        )
        val events = listOf(SmokeEvent(quantity = 8), SmokeEvent(quantity = 5))
        assertEquals(30L, InventoryCalculator.remainingCigarettes(0, purchases, events))
    }

    @Test
    fun veryLargeLegacyCartonCountsDoNotOverflowAnInt() {
        val purchases = listOf(
            Purchase(
                type = PurchaseType.CARTON,
                quantity = Int.MAX_VALUE,
                amountMinor = 1,
                currency = CurrencyCode.fromCode("USD"),
            ),
        )
        assertEquals(
            Int.MAX_VALUE.toLong() * 200L,
            InventoryCalculator.remainingCigarettes(0, purchases, emptyList()),
        )
    }

    @Test
    fun trackedStockNeverBecomesNegativeWhenSmokingExceedsInventory() {
        val events = listOf(SmokeEvent(quantity = 5, stockQuantity = 5))
        assertEquals(0L, InventoryCalculator.remainingCigarettes(0, emptyList(), events))
    }

    @Test
    fun smokingWithoutStockDoesNotConsumeAPurchaseRecordedLater() {
        val purchases = listOf(
            Purchase(type = PurchaseType.PACK, quantity = 1, amountMinor = 2_000, currency = CurrencyCode.fromCode("RWF")),
        )
        val events = listOf(SmokeEvent(quantity = 1, stockQuantity = 0))
        assertEquals(20L, InventoryCalculator.remainingCigarettes(0, purchases, events))
    }

    @Test
    fun aPartiallyCoveredSmokingEntryDebitsOnlyAvailableStock() {
        val purchases = listOf(
            Purchase(type = PurchaseType.SINGLE, quantity = 1, amountMinor = 100, currency = CurrencyCode.fromCode("RWF")),
        )
        val events = listOf(SmokeEvent(quantity = 3, stockQuantity = 1))
        assertEquals(0L, InventoryCalculator.remainingCigarettes(0, purchases, events))
    }

    @Test
    fun rollingTobaccoDoesNotInventAStickCount() {
        val purchases = listOf(
            Purchase(type = PurchaseType.ROLLING, quantity = 2, amountMinor = 500, currency = CurrencyCode.fromCode("PKR")),
        )
        assertEquals(7L, InventoryCalculator.remainingCigarettes(7, purchases, emptyList()))
    }

    @Test
    fun reconcilesOpeningStockPurchasesAndOnlyTrackedSmoking() {
        val purchases = listOf(
            Purchase(type = PurchaseType.PACK, quantity = 2, amountMinor = 2_000, currency = usd),
            Purchase(type = PurchaseType.SINGLE, quantity = 3, amountMinor = 300, currency = usd),
            Purchase(type = PurchaseType.CARTON, quantity = 1, amountMinor = 10_000, currency = usd),
            Purchase(type = PurchaseType.ROLLING, quantity = 7, amountMinor = 5_000, currency = usd),
        )
        val events = listOf(
            SmokeEvent(quantity = 4, stockQuantity = 4),
            SmokeEvent(quantity = 3, stockQuantity = 1),
            SmokeEvent(quantity = 8, stockQuantity = 0),
        )

        assertEquals(243L, InventoryCalculator.remainingCigarettes(5, purchases, events))
    }

    @Test
    fun unstockedSmokingStillCountsAsSmokingButDoesNotReduceInventory() {
        val purchases = listOf(
            Purchase(type = PurchaseType.PACK, quantity = 1, amountMinor = 1_000, currency = usd),
        )
        val events = listOf(SmokeEvent(quantity = 4, stockQuantity = 0))

        assertEquals(20L, InventoryCalculator.remainingCigarettes(0, purchases, events))
    }

    @Test
    fun stockReconciliationNeverReturnsANegativeBalanceAfterPurchaseRemoval() {
        val event = SmokeEvent(quantity = 5, stockQuantity = 5)

        assertEquals(0L, InventoryCalculator.remainingCigarettes(0, emptyList(), listOf(event)))
        val replacementPurchase = Purchase(
            type = PurchaseType.PACK,
            quantity = 1,
            amountMinor = 1_000,
            currency = usd,
        )
        assertEquals(15L, InventoryCalculator.remainingCigarettes(0, listOf(replacementPurchase), listOf(event)))
    }

    @Test
    fun rejectsAStockDebitLargerThanTheActualSmokingQuantity() {
        assertThrows(IllegalArgumentException::class.java) {
            InventoryCalculator.remainingCigarettes(
                startingStock = 10,
                purchases = emptyList(),
                smokeEvents = listOf(SmokeEvent(quantity = 1, stockQuantity = 2)),
            )
        }
    }
}
