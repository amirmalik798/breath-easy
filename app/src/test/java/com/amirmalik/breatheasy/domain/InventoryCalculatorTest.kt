package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.PurchaseType
import com.amirmalik.breatheasy.data.SmokeEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryCalculatorTest {
    @Test fun stockIsOpeningBalancePlusRecordedCigarettesLessSmokingEvents() {
        val purchases = listOf(
            Purchase(type = PurchaseType.PACK, quantity = 2, amountMinor = 2_000, currency = CurrencyCode.fromCode("RWF")),
            Purchase(type = PurchaseType.SINGLE, quantity = 3, amountMinor = 300, currency = CurrencyCode.fromCode("RWF")),
        )
        val events = listOf(SmokeEvent(quantity = 8), SmokeEvent(quantity = 5))
        assertEquals(30L, InventoryCalculator.remainingCigarettes(0, purchases, events))
    }

    @Test fun veryLargeLegacyCartonCountsDoNotOverflowAnInt() {
        val purchases = listOf(
            Purchase(
                type = PurchaseType.CARTON,
                quantity = Int.MAX_VALUE,
                amountMinor = 1,
                currency = CurrencyCode.fromCode("USD"),
            ),
        )
        assertEquals(Int.MAX_VALUE.toLong() * 200L,
            InventoryCalculator.remainingCigarettes(0, purchases, emptyList()))
    }

    @Test fun negativeStockIsRetainedWhenMoreCigarettesAreLoggedThanPurchased() {
        val events = listOf(SmokeEvent(quantity = 5))
        assertEquals(-5L, InventoryCalculator.remainingCigarettes(0, emptyList(), events))
    }

    @Test fun rollingTobaccoDoesNotInventAStickCount() {
        val purchases = listOf(
            Purchase(type = PurchaseType.ROLLING, quantity = 2, amountMinor = 500, currency = CurrencyCode.fromCode("PKR")),
        )
        assertEquals(7L, InventoryCalculator.remainingCigarettes(7, purchases, emptyList()))
    }
}
