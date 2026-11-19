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

        // Aggregate the history once instead of filtering the full list once per
        // chart month. This keeps report work close to O(purchases + monthCount)
        // for users with long-running ledgers.
        val firstMonth = selectedMonth.minusMonths(monthCount.toLong() - 1L)
        val totalsByMonth = HashMap<YearMonth, Long>()
        val countsByMonth = HashMap<YearMonth, Int>()
        purchases.forEach { purchase ->
            if (purchase.currency.isoCode == reportCurrency.isoCode) {
                val month = YearMonth.from(Instant.ofEpochMilli(purchase.purchasedAtMillis).atZone(zoneId))
                if (month >= firstMonth && month <= selectedMonth) {
                    totalsByMonth[month] = SafeArithmetic.add(
                        totalsByMonth[month] ?: 0L,
                        purchase.amountMinor,
                    )
                    countsByMonth[month] = (countsByMonth[month] ?: 0) + 1
                }
            }
        }

        val points = (monthCount - 1 downTo 0).map { offset ->
            val month = selectedMonth.minusMonths(offset.toLong())
            MonthlySpendPoint(
                month = month,
                totalMinor = totalsByMonth[month] ?: 0L,
                purchaseCount = countsByMonth[month] ?: 0,
            )
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
        val firstMonth = selectedMonth.minusMonths(monthCount.toLong() - 1L)
        val totalsByMonth = HashMap<YearMonth, Long>()
        events.forEach { event ->
            val month = YearMonth.from(Instant.ofEpochMilli(event.smokedAtMillis).atZone(zoneId))
            if (month >= firstMonth && month <= selectedMonth) {
                totalsByMonth[month] = SafeArithmetic.add(
                    totalsByMonth[month] ?: 0L,
                    event.quantity.toLong(),
                )
            }
        }

        return (monthCount - 1 downTo 0).map { offset ->
            val month = selectedMonth.minusMonths(offset.toLong())
            MonthlyConsumptionPoint(month, totalsByMonth[month] ?: 0L)
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
                SafeArithmetic.add(total, purchase.amountMinor)
            },
            purchaseCount = matchingPurchases.size,
        )
    }
}
