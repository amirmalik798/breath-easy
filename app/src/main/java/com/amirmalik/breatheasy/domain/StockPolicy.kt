package com.amirmalik.breatheasy.domain

/**
 * Raised when a smoking event would make tracked cigarette inventory negative.
 * The UI can ask the user to confirm that the smoking event really occurred.
 */
class TrackedStockUnavailableException(
    val remainingCigarettes: Long,
) : IllegalStateException(
    if (remainingCigarettes == 0L) {
        "Tracked cigarette stock is empty. Confirm actual smoking to log anyway."
    } else {
        "Tracked cigarette stock is already below zero. Confirm actual smoking to log anyway."
    },
)

/** Enforces stock availability unless the user explicitly confirms actual smoking. */
object StockPolicy {
    fun requireAvailable(
        remainingCigarettes: Long,
        allowNegativeStock: Boolean = false,
    ) {
        if (remainingCigarettes <= 0L && !allowNegativeStock) {
            throw TrackedStockUnavailableException(remainingCigarettes)
        }
    }
}
