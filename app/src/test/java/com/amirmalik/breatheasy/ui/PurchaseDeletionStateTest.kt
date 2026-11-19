package com.amirmalik.breatheasy.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PurchaseDeletionStateTest {
    @Test
    fun noPendingPurchaseDeleteDoesNothing() {
        assertEquals(
            PurchaseDeleteResolution(operationFinished = false, closeConfirmation = false),
            resolvePurchaseDeleteCompletion(
                pendingPurchaseId = null,
                submittedRevision = 4L,
                currentRevision = 5L,
                isBusy = false,
                purchaseStillExists = false,
                snapshotLoadFailed = false,
            ),
        )
    }

    @Test
    fun inFlightDeleteRemainsPendingUntilTheViewModelIsIdle() {
        assertEquals(
            PurchaseDeleteResolution(operationFinished = false, closeConfirmation = false),
            resolvePurchaseDeleteCompletion(
                pendingPurchaseId = "purchase-1",
                submittedRevision = 4L,
                currentRevision = 5L,
                isBusy = true,
                purchaseStillExists = false,
                snapshotLoadFailed = false,
            ),
        )
    }

    @Test
    fun unrelatedRevisionCannotCompleteTheDelete() {
        assertEquals(
            PurchaseDeleteResolution(operationFinished = false, closeConfirmation = false),
            resolvePurchaseDeleteCompletion(
                pendingPurchaseId = "purchase-1",
                submittedRevision = 5L,
                currentRevision = 5L,
                isBusy = false,
                purchaseStillExists = false,
                snapshotLoadFailed = false,
            ),
        )
    }

    @Test
    fun failedDeleteReenablesRetryWithoutClosingConfirmation() {
        assertEquals(
            PurchaseDeleteResolution(operationFinished = true, closeConfirmation = false),
            resolvePurchaseDeleteCompletion(
                pendingPurchaseId = "purchase-1",
                submittedRevision = 4L,
                currentRevision = 5L,
                isBusy = false,
                purchaseStillExists = true,
                snapshotLoadFailed = false,
            ),
        )
    }

    @Test
    fun successfulDeleteClosesConfirmationWhenTheFreshSnapshotOmitsThePurchase() {
        assertEquals(
            PurchaseDeleteResolution(operationFinished = true, closeConfirmation = true),
            resolvePurchaseDeleteCompletion(
                pendingPurchaseId = "purchase-1",
                submittedRevision = 4L,
                currentRevision = 5L,
                isBusy = false,
                purchaseStillExists = false,
                snapshotLoadFailed = false,
            ),
        )
    }

    @Test
    fun snapshotLoadFailureDoesNotLeaveDeleteDialogOverTheReloadError() {
        assertEquals(
            PurchaseDeleteResolution(operationFinished = true, closeConfirmation = true),
            resolvePurchaseDeleteCompletion(
                pendingPurchaseId = "purchase-1",
                submittedRevision = 4L,
                currentRevision = 5L,
                isBusy = false,
                purchaseStillExists = true,
                snapshotLoadFailed = true,
            ),
        )
    }
}
