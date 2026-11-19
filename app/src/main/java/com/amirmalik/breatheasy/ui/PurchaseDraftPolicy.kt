package com.amirmalik.breatheasy.ui

/**
 * The default purchase date follows the device's local day only before submission.
 * Once an ID/timestamp pair has been submitted, it must stay unchanged for safe retries.
 */
internal fun shouldRefreshDefaultPurchaseDate(
    hasCustomPurchaseDate: Boolean,
    draftPrepared: Boolean,
): Boolean = !hasCustomPurchaseDate && !draftPrepared
