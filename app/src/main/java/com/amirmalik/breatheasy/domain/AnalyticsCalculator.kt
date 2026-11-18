package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.SmokeEvent
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** One bar in the monthly-spend chart, containing only the selected currency. */
data class MonthlySpendPoint(
    val month: YearMonth,
    val totalMinor: Long,
    val purchaseCount: Int,
)

data class MonthlyConsumptionPoint(
    val month: YearMonth,
    val cigaretteCount: Long,
)

data class SpendingSummary(
    val selectedMonth: YearMonth,
    val reportCurrency: CurrencyCode,
    val totalMinor: Long,
    val purchaseCount: Int,
    val monthlyPoints: List<MonthlySpendPoint>,
    val previousMonthTotalMinor: Long?,
) {
    val changeFromPreviousMinor: Long?
        get() = previousMonthTotalMinor?.let { totalMinor - it }
}

object AnalyticsCalculator {
    fun spending(
        purchases: List<Purchase>,
        selectedMonth: YearMonth,
        reportCurrency: CurrencyCode,
        monthCount: Int = 6,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): SpendingSummary {
        require(monthCount in 1..36) { "monthCount must be between 1 and 36" }
        val points = (monthCount - 1 downTo 0).map { offset ->
            val month = selectedMonth.minusMonths(offset.toLong())
            pointForMonth(purchases, month, reportCurrency, zoneId)
        }
        val selected = points.last()
        val previousPoint = points.getOrNull(points.lastIndex - 1)
        val previous = previousPoint?.totalMinor
        return SpendingSummary(
            selectedMonth = selectedMonth,
            reportCurrency = reportCurrency,
            totalMinor = selected.totalMinor,
            purchaseCount = selected.purchaseCount,
            monthlyPoints = points,
            previousMonthTotalMinor = previous,
        )
    }

    fun monthlyConsumption(
        events: List<SmokeEvent>,
        selectedMonth: YearMonth,
        monthCount: Int = 12,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<MonthlyConsumptionPoint> {
        require(monthCount in 1..36) { "monthCount must be between 1 and 36" }
        return (monthCount - 1 downTo 0).map { offset ->
            val month = selectedMonth.minusMonths(offset.toLong())
            val total = events.asSequence()
                .filter { YearMonth.from(Instant.ofEpochMilli(it.smokedAtMillis).atZone(zoneId)) == month }
                .fold(0L) { count, event -> Math.addExact(count, event.quantity.toLong()) }
            MonthlyConsumptionPoint(month, total)
        }
    }

    fun pointForMonth(
        purchases: List<Purchase>,
        month: YearMonth,
        reportCurrency: CurrencyCode,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): MonthlySpendPoint {
        val matchingPurchases = purchases.filter { purchase ->
            purchase.currency.isoCode == reportCurrency.isoCode &&
                YearMonth.from(Instant.ofEpochMilli(purchase.purchasedAtMillis).atZone(zoneId)) == month
        }
        return MonthlySpendPoint(
            month = month,
            totalMinor = matchingPurchases.fold(0L) { total, purchase ->
                Math.addExact(total, purchase.amountMinor)
            },
            purchaseCount = matchingPurchases.size,
        )
    }
}
