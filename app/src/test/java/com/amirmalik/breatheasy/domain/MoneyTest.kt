package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.FxSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test fun parsesUsdAndPkrIntoExactMinorUnits() {
        assertEquals(1_405L, Money.parseToMinor("14.05", CurrencyCode.USD))
        assertEquals(28_075L, Money.parseToMinor("280.75", CurrencyCode.PKR))
    }

    @Test fun rwfDoesNotAcceptFractionalUnits() {
        assertEquals(25_000L, Money.parseToMinor("25,000", CurrencyCode.RWF))
        assertNull(Money.parseToMinor("25.50", CurrencyCode.RWF))
    }

    @Test fun rejectsZeroNegativeAndMalformedAmounts() {
        assertNull(Money.parseToMinor("0", CurrencyCode.USD))
        assertNull(Money.parseToMinor("-2.00", CurrencyCode.USD))
        assertNull(Money.parseToMinor("12..3", CurrencyCode.USD))
    }

    @Test fun convertsUsingPurchaseSnapshotNotCurrentPreferences() {
        val rates = FxSnapshot(pkrPerUsd = "280", rwfPerUsd = "1450")
        assertEquals(28_000L, Money.convertMinor(100L, CurrencyCode.USD, CurrencyCode.PKR, rates))
        assertEquals(1_450L, Money.convertMinor(100L, CurrencyCode.USD, CurrencyCode.RWF, rates))
        assertEquals(100L, Money.convertMinor(28_000L, CurrencyCode.PKR, CurrencyCode.USD, rates))
        assertEquals(518L, Money.convertMinor(10_000L, CurrencyCode.PKR, CurrencyCode.RWF, rates))
    }

    @Test fun doesNotInventMissingExchangeRates() {
        assertNull(Money.convertMinor(100L, CurrencyCode.PKR, CurrencyCode.RWF, FxSnapshot(null, null)))
        assertEquals(100L, Money.convertMinor(100L, CurrencyCode.PKR, CurrencyCode.PKR, FxSnapshot(null, null)))
    }
}
