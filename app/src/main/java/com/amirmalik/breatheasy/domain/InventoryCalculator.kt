package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.Purchase
import com.amirmalik.breatheasy.data.SmokeEvent

/**
 * Reconciles tracked cigarette stock. Rolling tobacco is intentionally excluded because
 * its cigarette-equivalent yield depends on how the user rolls and is not reliably known.
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
        val smoked = smokeEvents.fold(0L) { total, event ->
            Math.addExact(total, event.quantity.toLong())
        }
        return Math.subtractExact(Math.addExact(startingStock.toLong(), purchased), smoked)
    }
}
