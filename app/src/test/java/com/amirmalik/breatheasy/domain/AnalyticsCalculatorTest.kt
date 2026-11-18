package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.FxSnapshot
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.PurchaseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class AnalyticsCalculatorTest {
    private val utc = ZoneId.of("UTC")

    @Test fun monthlyReportUsesPurchaseDateAndKeepsNativeSubtotals() {
        val month = YearMonth.of(2026, 11)
        val purchases = listOf(
            Purchase(purchasedAtMillis = Instant.parse("2026-11-02T12:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 1_400, currency = CurrencyCode.USD),
            Purchase(purchasedAtMillis = Instant.parse("2026-11-18T12:00:00Z").toEpochMilli(),
                type = PurchaseType.SINGLE, quantity = 2, amountMinor = 500, currency = CurrencyCode.PKR),
            Purchase(purchasedAtMillis = Instant.parse("2026-10-31T23:59:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 900, currency = CurrencyCode.USD),
        )

        val report = AnalyticsCalculator.spending(purchases, month, CurrencyCode.USD, 2, utc)
        assertEquals(1_400L, report.convertedTotalMinor)
        assertEquals(1, report.unconvertedPurchaseCount)
        assertEquals(500L, report.nativeSubtotals[CurrencyCode.PKR])
        assertEquals(900L, report.monthlyPoints.first().convertedMinor)
    }

    @Test fun crossCurrencyConversionUsesTheExchangeRateStoredWithPurchase() {
        val purchase = Purchase(
            purchasedAtMillis = Instant.parse("2026-11-10T10:00:00Z").toEpochMilli(),
            type = PurchaseType.PACK, quantity = 1, amountMinor = 28_000,
            currency = CurrencyCode.PKR,
            fxSnapshot = FxSnapshot(pkrPerUsd = "280", rwfPerUsd = "1450"),
        )
        val report = AnalyticsCalculator.spending(listOf(purchase), YearMonth.of(2026, 11), CurrencyCode.USD, 1, utc)
        assertEquals(100L, report.convertedTotalMinor)
        assertEquals(0, report.unconvertedPurchaseCount)
    }

    @Test fun missingRateIsReportedInsteadOfCountedAsZero() {
        val purchase = Purchase(
            purchasedAtMillis = Instant.parse("2026-11-10T10:00:00Z").toEpochMilli(),
            type = PurchaseType.PACK, quantity = 1, amountMinor = 28_000,
            currency = CurrencyCode.PKR,
            fxSnapshot = FxSnapshot(null, null),
        )
        val report = AnalyticsCalculator.spending(listOf(purchase), YearMonth.of(2026, 11), CurrencyCode.USD, 1, utc)
        assertEquals(0L, report.convertedTotalMinor)
        assertEquals(1, report.unconvertedPurchaseCount)
        assertNull(report.previousMonthTotalMinor)
    }
}
