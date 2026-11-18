package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.SmokeEvent
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** One bar in the monthly-spend chart. */
data class MonthlySpendPoint(
    val month: YearMonth,
    val convertedMinor: Long,
    val convertedPurchaseCount: Int,
    val unconvertedPurchaseCount: Int,
    val nativeSubtotals: Map<CurrencyCode, Long>,
)

data class MonthlyConsumptionPoint(
    val month: YearMonth,
    val cigaretteCount: Int,
)

data class SpendingSummary(
    val selectedMonth: YearMonth,
    val reportCurrency: CurrencyCode,
    val convertedTotalMinor: Long,
    val convertedPurchaseCount: Int,
    val unconvertedPurchaseCount: Int,
    val nativeSubtotals: Map<CurrencyCode, Long>,
    val monthlyPoints: List<MonthlySpendPoint>,
    val previousMonthTotalMinor: Long?,
) {
    val changeFromPreviousMinor: Long?
        get() = previousMonthTotalMinor?.let { convertedTotalMinor - it }
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
        // Comparisons are meaningful only when both months are complete in the
        // selected reporting currency. Never compare partial totals as if they
        // were complete spending figures.
        val previous = previousPoint
            ?.takeIf { it.unconvertedPurchaseCount == 0 && selected.unconvertedPurchaseCount == 0 }
            ?.convertedMinor
        return SpendingSummary(
            selectedMonth = selectedMonth,
            reportCurrency = reportCurrency,
            convertedTotalMinor = selected.convertedMinor,
            convertedPurchaseCount = selected.convertedPurchaseCount,
            unconvertedPurchaseCount = selected.unconvertedPurchaseCount,
            nativeSubtotals = selected.nativeSubtotals,
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
                .sumOf { it.quantity }
            MonthlyConsumptionPoint(month, total)
        }
    }

    fun pointForMonth(
        purchases: List<Purchase>,
        month: YearMonth,
        reportCurrency: CurrencyCode,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): MonthlySpendPoint {
        val monthPurchases = purchases.filter { purchase ->
            YearMonth.from(Instant.ofEpochMilli(purchase.purchasedAtMillis).atZone(zoneId)) == month
        }
        var total = 0L
        var convertedCount = 0
        var unconvertedCount = 0
        val native = linkedMapOf<CurrencyCode, Long>()

        monthPurchases.forEach { purchase ->
            native[purchase.currency] = Math.addExact(native[purchase.currency] ?: 0L, purchase.amountMinor)
            val converted = Money.convertMinor(
                amountMinor = purchase.amountMinor,
                from = purchase.currency,
                to = reportCurrency,
                rates = purchase.fxSnapshot,
            )
            if (converted == null) {
                unconvertedCount++
            } else {
                total = Math.addExact(total, converted)
                convertedCount++
            }
        }
        return MonthlySpendPoint(
            month = month,
            convertedMinor = total,
            convertedPurchaseCount = convertedCount,
            unconvertedPurchaseCount = unconvertedCount,
            nativeSubtotals = native,
        )
    }
}
