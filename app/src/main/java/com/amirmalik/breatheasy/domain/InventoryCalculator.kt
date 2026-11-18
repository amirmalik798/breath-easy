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
        val purchased = purchases.sumOf { (it.totalCigarettes ?: 0).toLong() }
        val smoked = smokeEvents.sumOf { it.quantity.toLong() }
        return Math.addExact(startingStock.toLong(), purchased) - smoked
    }
}
