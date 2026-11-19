package com.amirmalik.breatheasy.ui

/**
 * Decides whether the purchase-delete confirmation can be closed after a result.
 *
 * A failed delete must remain retryable while the purchase still exists in the
 * latest snapshot. A committed delete closes the confirmation even if refreshing
 * the snapshot failed after the database write.
 */
internal fun shouldClosePurchaseDeleteConfirmation(
    pendingPurchaseId: String?,
    currentPurchaseIds: Set<String>,
    resultMessage: String,
): Boolean {
    val deletingId = pendingPurchaseId ?: return false
    return deletingId !in currentPurchaseIds || resultMessage.startsWith("Purchase removed.")
}
