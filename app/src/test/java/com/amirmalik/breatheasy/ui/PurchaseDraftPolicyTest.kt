package com.amirmalik.breatheasy.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseDraftPolicyTest {
    @Test
    fun untouchedDefaultDateFollowsTheCurrentLocalDay() {
        assertTrue(
            shouldRefreshDefaultPurchaseDate(
                hasCustomPurchaseDate = false,
                draftPrepared = false,
            ),
        )
    }

    @Test
    fun submittedDraftKeepsItsTimestampForAnIdempotentRetry() {
        assertFalse(
            shouldRefreshDefaultPurchaseDate(
                hasCustomPurchaseDate = false,
                draftPrepared = true,
            ),
        )
    }

    @Test
    fun manuallySelectedDateDoesNotMoveAtMidnight() {
        assertFalse(
            shouldRefreshDefaultPurchaseDate(
                hasCustomPurchaseDate = true,
                draftPrepared = false,
            ),
        )
    }

    @Test
    fun manuallySelectedSubmittedDateAlsoRemainsPinned() {
        assertFalse(
            shouldRefreshDefaultPurchaseDate(
                hasCustomPurchaseDate = true,
                draftPrepared = true,
            ),
        )
    }
}
