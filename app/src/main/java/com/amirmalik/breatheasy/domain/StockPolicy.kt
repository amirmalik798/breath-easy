package com.amirmalik.breatheasy.domain

/**
 * Raised when a smoking event would exceed the tracked cigarette inventory.
 * The UI can ask the user to confirm that the smoking event really occurred.
 */
class TrackedStockUnavailableException(
    val remainingCigarettes: Long,
    val requestedCigarettes: Int,
) : IllegalStateException(
    if (remainingCigarettes <= 0L) {
        "Tracked cigarette stock is empty. Confirm actual smoking to log anyway."
    } else {
        "Only $remainingCigarettes cigarettes are tracked, but this entry logs $requestedCigarettes. Confirm actual smoking to continue."
    },
)

/** Enforces stock availability unless the user explicitly confirms actual smoking. */
object StockPolicy {
    fun requireAvailable(
        remainingCigarettes: Long,
        requestedCigarettes: Int = 1,
        allowNegativeStock: Boolean = false,
    ) {
        require(requestedCigarettes > 0) { "The smoking entry must contain at least one cigarette." }
        if (remainingCigarettes < requestedCigarettes.toLong() && !allowNegativeStock) {
            throw TrackedStockUnavailableException(remainingCigarettes, requestedCigarettes)
        }
    }
}
