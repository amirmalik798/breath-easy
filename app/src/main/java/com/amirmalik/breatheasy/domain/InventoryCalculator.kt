package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.SmokeEvent

/**
 * Reconciles tracked cigarette stock. Actual smoking totals use SmokeEvent.quantity;
 * inventory depletion uses only SmokeEvent.stockQuantity, so an unstocked cigarette
 * does not consume a purchase entered later. Rolling tobacco is excluded.
 */
object InventoryCalculator {
    fun remainingCigarettes(
        startingStock: Int,
        purchases: List<Purchase>,
        smokeEvents: List<SmokeEvent>,
    ): Long {
        require(startingStock >= 0) { "Starting stock cannot be negative" }
        val purchased = purchases.fold(0L) { total, purchase ->
            Math.addExact(total, purchase.totalCigarettes ?: 0L)
        }
        val consumedFromStock = smokeEvents.fold(0L) { total, event ->
            require(event.stockQuantity in 0..event.quantity) {
                "Stock consumption must be between zero and the number of cigarettes smoked."
            }
            Math.addExact(total, event.stockQuantity.toLong())
        }
        return Math.subtractExact(Math.addExact(startingStock.toLong(), purchased), consumedFromStock)
            .coerceAtLeast(0L)
    }
}
