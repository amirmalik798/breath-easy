package com.amirmalik.breatheasy.ui

/**
 * Resolves a pending delete only after a newer ViewModel operation has completed.
 * Snackbar messages are deliberately excluded because they can be delayed behind
 * an earlier snackbar and must not mutate the state of a newer confirmation.
 */
internal data class PurchaseDeleteResolution(
    val operationFinished: Boolean,
    val closeConfirmation: Boolean,
)

internal fun resolvePurchaseDeleteCompletion(
    pendingPurchaseId: String?,
    submittedRevision: Long,
    currentRevision: Long,
    isBusy: Boolean,
    purchaseStillExists: Boolean,
    snapshotLoadFailed: Boolean,
): PurchaseDeleteResolution {
    if (pendingPurchaseId == null || isBusy || currentRevision == submittedRevision) {
        return PurchaseDeleteResolution(operationFinished = false, closeConfirmation = false)
    }

    // A successful delete can be followed by a failed snapshot read. Close the
    // delete dialog so it cannot cover the blocking reload-error dialog.
    return PurchaseDeleteResolution(
        operationFinished = true,
        closeConfirmation = !purchaseStillExists || snapshotLoadFailed,
    )
}
