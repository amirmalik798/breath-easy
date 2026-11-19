package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.SmokeEvent
import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.PurchaseType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

class AnalyticsCalculatorTest {
    private val utc = ZoneOffset.UTC

    @Test
    fun monthlyReportUsesPurchaseDateAndOnlyTheSelectedCurrency() {
        val purchases = listOf(
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-03T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 1_400,
                currency = CurrencyCode.fromCode("USD"),
            ),
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-05T10:00:00Z").toEpochMilli(),
                type = PurchaseType.SINGLE, quantity = 2, amountMinor = 500,
                currency = CurrencyCode.fromCode("PKR"),
            ),
            Purchase(
                purchasedAtMillis = Instant.parse("2026-10-31T23:30:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 900,
                currency = CurrencyCode.fromCode("USD"),
            ),
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-07T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 700,
                currency = CurrencyCode.fromCode("USD"),
            ),
        )

        val report = AnalyticsCalculator.spending(
            purchases, YearMonth.of(2026, 11), CurrencyCode.fromCode("USD"), 2, utc,
        )

        assertEquals(2, report.purchaseCount)
        assertEquals(2_100L, report.totalMinor)
        assertEquals(900L, report.monthlyPoints.first().totalMinor)
        assertEquals(1, report.monthlyPoints.first().purchaseCount)
        assertEquals(2_100L, report.monthlyPoints.last().totalMinor)
    }

    @Test
    fun purchasesInOtherCurrenciesNeverEnterSelectedCurrencyTotals() {
        val purchases = listOf(
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-03T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 1_400,
                currency = CurrencyCode.fromCode("USD"),
            ),
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-04T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 2, amountMinor = 500_000,
                currency = CurrencyCode.fromCode("JPY"),
            ),
        )

        val report = AnalyticsCalculator.spending(
            purchases, YearMonth.of(2026, 11), CurrencyCode.fromCode("USD"), 1, utc,
        )

        assertEquals(1, report.purchaseCount)
        assertEquals(1_400L, report.totalMinor)
        assertEquals(1_400L, report.monthlyPoints.single().totalMinor)
    }

    @Test
    fun previousMonthComparisonUsesOnlyMatchingCurrencyPurchases() {
        val purchases = listOf(
            Purchase(
                purchasedAtMillis = Instant.parse("2026-10-10T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 1_000,
                currency = CurrencyCode.fromCode("USD"),
            ),
            Purchase(
                purchasedAtMillis = Instant.parse("2026-10-11T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 5_000,
                currency = CurrencyCode.fromCode("PKR"),
            ),
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-10T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 750,
                currency = CurrencyCode.fromCode("USD"),
            ),
        )

        val report = AnalyticsCalculator.spending(
            purchases, YearMonth.of(2026, 11), CurrencyCode.fromCode("USD"), 2, utc,
        )

        assertEquals(1_000L, report.previousMonthTotalMinor)
        assertEquals(-250L, report.changeFromPreviousMinor)
    }
    @Test
    fun monthlyGroupingUsesTheExplicitReportingTimeZoneAtMonthBoundaries() {
        val purchases = listOf(
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-01T00:30:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 900,
                currency = CurrencyCode.fromCode("USD"),
            ),
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-01T08:30:00Z").toEpochMilli(),
                type = PurchaseType.PACK, quantity = 1, amountMinor = 1_200,
                currency = CurrencyCode.fromCode("USD"),
            ),
        )

        val report = AnalyticsCalculator.spending(
            purchases,
            YearMonth.of(2026, 11),
            CurrencyCode.fromCode("USD"),
            monthCount = 2,
            zoneId = java.time.ZoneId.of("America/Los_Angeles"),
        )

        assertEquals(900L, report.monthlyPoints.first().totalMinor)
        assertEquals(1, report.monthlyPoints.first().purchaseCount)
        assertEquals(1_200L, report.totalMinor)
        assertEquals(1, report.purchaseCount)
    }

    @Test
    fun monthlyConsumptionDoesNotOverflowAtIntRange() {
        val events = listOf(
            SmokeEvent(
                smokedAtMillis = Instant.parse("2026-11-03T10:00:00Z").toEpochMilli(),
                quantity = Int.MAX_VALUE,
            ),
            SmokeEvent(
                smokedAtMillis = Instant.parse("2026-11-04T10:00:00Z").toEpochMilli(),
                quantity = Int.MAX_VALUE,
            ),
        )

        val result = AnalyticsCalculator.monthlyConsumption(
            events,
            YearMonth.of(2026, 11),
            monthCount = 1,
            zoneId = utc,
        ).single()

        assertEquals(Int.MAX_VALUE.toLong() * 2L, result.cigaretteCount)
    }


    @Test
    fun monthlyConsumptionGroupsEventsByTheExplicitReportingTimeZone() {
        val events = listOf(
            SmokeEvent(
                smokedAtMillis = Instant.parse("2026-11-01T00:30:00Z").toEpochMilli(),
                quantity = 2,
            ),
            SmokeEvent(
                smokedAtMillis = Instant.parse("2026-11-01T08:30:00Z").toEpochMilli(),
                quantity = 3,
            ),
            SmokeEvent(
                smokedAtMillis = Instant.parse("2026-10-31T23:00:00Z").toEpochMilli(),
                quantity = 5,
            ),
        )

        val result = AnalyticsCalculator.monthlyConsumption(
            events,
            YearMonth.of(2026, 11),
            monthCount = 2,
            zoneId = java.time.ZoneId.of("America/Los_Angeles"),
        )

        assertEquals(7L, result.first().cigaretteCount)
        assertEquals(3L, result.last().cigaretteCount)
    }

    @Test
    fun spendingTotalsSaturateInsteadOfWrappingWhenHistoryIsExtremelyLarge() {
        val purchases = listOf(
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-03T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK,
                quantity = 1,
                amountMinor = Long.MAX_VALUE,
                currency = CurrencyCode.fromCode("USD"),
            ),
            Purchase(
                purchasedAtMillis = Instant.parse("2026-11-04T10:00:00Z").toEpochMilli(),
                type = PurchaseType.PACK,
                quantity = 1,
                amountMinor = 1L,
                currency = CurrencyCode.fromCode("USD"),
            ),
        )

        val report = AnalyticsCalculator.spending(
            purchases,
            YearMonth.of(2026, 11),
            CurrencyCode.fromCode("USD"),
            monthCount = 1,
            zoneId = utc,
        )

        assertEquals(Long.MAX_VALUE, report.totalMinor)
    }

}
