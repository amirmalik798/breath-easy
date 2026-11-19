package com.amirmalik.breatheasy.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseDeletionStateTest {
    @Test
    fun failedDeleteKeepsConfirmationOpenWhilePurchaseStillExists() {
        assertFalse(
            shouldClosePurchaseDeleteConfirmation(
                pendingPurchaseId = "purchase-1",
                currentPurchaseIds = setOf("purchase-1"),
                resultMessage = "The purchase could not be removed.",
            ),
        )
    }

    @Test
    fun deleteClosesConfirmationWhenLatestSnapshotNoLongerContainsPurchase() {
        assertTrue(
            shouldClosePurchaseDeleteConfirmation(
                pendingPurchaseId = "purchase-1",
                currentPurchaseIds = emptySet(),
                resultMessage = "This purchase was already removed.",
            ),
        )
    }

    @Test
    fun committedDeleteClosesConfirmationEvenWhenSnapshotRefreshFails() {
        assertTrue(
            shouldClosePurchaseDeleteConfirmation(
                pendingPurchaseId = "purchase-1",
                currentPurchaseIds = setOf("purchase-1"),
                resultMessage = "Purchase removed. Monthly totals have been updated. Saved, but the latest data could not be reloaded.",
            ),
        )
    }

    @Test
    fun unrelatedMessagesDoNotCloseConfirmationWithoutPendingDelete() {
        assertFalse(
            shouldClosePurchaseDeleteConfirmation(
                pendingPurchaseId = null,
                currentPurchaseIds = emptySet(),
                resultMessage = "Purchase added to your ledger",
            ),
        )
    }
}
