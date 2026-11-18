package com.amirmalik.breatheasy.domain

/** Raised when an actual smoking entry exceeds tracked cigarette inventory. */
class TrackedStockUnavailableException(
    val remainingCigarettes: Long,
    val requestedCigarettes: Int,
) : IllegalStateException(
    if (remainingCigarettes == 0L) {
        "Tracked cigarette stock is empty. Confirm actual smoking to record it while keeping stock at zero."
    } else {
        "Only $remainingCigarettes cigarettes are tracked, but this entry logs $requestedCigarettes. Confirm actual smoking to continue."
    },
)

/** Enforces explicit confirmation and calculates the exact inventory debit. */
object StockPolicy {
    fun requireAvailable(
        remainingCigarettes: Long,
        requestedCigarettes: Int = 1,
        confirmUnstockedSmoking: Boolean = false,
    ) {
        require(remainingCigarettes >= 0L) { "Tracked stock cannot be negative." }
        require(requestedCigarettes > 0) { "The smoking entry must contain at least one cigarette." }
        if (remainingCigarettes < requestedCigarettes.toLong() && !confirmUnstockedSmoking) {
            throw TrackedStockUnavailableException(remainingCigarettes, requestedCigarettes)
        }
    }

    /**
     * Returns how many cigarettes are actually debited from tracked inventory.
     * The caller records the full actual smoking quantity separately.
     */
    fun stockDebit(
        remainingCigarettes: Long,
        requestedCigarettes: Int,
        confirmUnstockedSmoking: Boolean = false,
    ): Int {
        requireAvailable(
            remainingCigarettes = remainingCigarettes,
            requestedCigarettes = requestedCigarettes,
            confirmUnstockedSmoking = confirmUnstockedSmoking,
        )
        return minOf(remainingCigarettes, requestedCigarettes.toLong()).toInt()
    }
}
