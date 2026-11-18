package com.amirmalik.breatheasy.data

import java.time.Instant
import java.time.YearMonth
import java.util.UUID

/** ISO currency choices exposed explicitly by BreatheEasy. */
enum class CurrencyCode(
    val isoCode: String,
    val displayName: String,
    val symbol: String,
    val fractionDigits: Int,
) {
    USD("USD", "US Dollar", "$", 2),
    PKR("PKR", "Pakistani Rupee", "Rs", 2),
    RWF("RWF", "Rwandan Franc", "FRw", 0);

    companion object {
        fun fromCode(value: String?): CurrencyCode =
            entries.firstOrNull { it.isoCode == value } ?: RWF
    }
}

enum class PurchaseType(val label: String, val cigarettesPerUnit: Int?) {
    PACK("Full pack", 20),
    SINGLE("Loose cigarettes", 1),
    CARTON("Carton", 200),
    ROLLING("Rolling tobacco", null),
}

enum class CravingOutcome { RESISTED, SMOKED }

enum class CravingTrigger(val label: String) {
    STRESS("Stress"), COFFEE("Coffee"), AFTER_MEAL("After a meal"),
    SOCIAL("Social situation"), BOREDOM("Boredom"), ALCOHOL("Alcohol"),
    OTHER("Other"),
}

data class FxSnapshot(
    /** Number of PKR for one USD. Null means the user has not configured this rate. */
    val pkrPerUsd: String?,
    /** Number of RWF for one USD. Null means the user has not configured this rate. */
    val rwfPerUsd: String?,
)

data class Purchase(
    val id: String = UUID.randomUUID().toString(),
    val purchasedAtMillis: Long = System.currentTimeMillis(),
    val type: PurchaseType,
    val quantity: Int,
    val amountMinor: Long,
    val currency: CurrencyCode,
    val brand: String = "",
    val location: String = "",
    val planned: Boolean = true,
    val note: String = "",
    val fxSnapshot: FxSnapshot = FxSnapshot(null, null),
) {
    val totalCigarettes: Int?
        get() = type.cigarettesPerUnit?.let { unit -> Math.multiplyExact(quantity, unit) }
}

data class SmokeEvent(
    val id: String = UUID.randomUUID().toString(),
    val smokedAtMillis: Long = System.currentTimeMillis(),
    val quantity: Int = 1,
    val trigger: CravingTrigger = CravingTrigger.OTHER,
    val note: String = "",
)

data class CravingEvent(
    val id: String = UUID.randomUUID().toString(),
    val occurredAtMillis: Long = System.currentTimeMillis(),
    val outcome: CravingOutcome,
    val trigger: CravingTrigger,
    val intensity: Int,
    val note: String = "",
    val breathingSeconds: Int = 0,
)

data class AppPreferences(
    val displayCurrency: CurrencyCode = CurrencyCode.RWF,
    /** User-defined pacing target; not a medically recommended limit. */
    val dailyTarget: Int = 4,
    val baselinePerDay: Int = 15,
    /** Cigarettes physically on hand before the user begins logging purchases. */
    val startingStockCigarettes: Int = 0,
    val pkrPerUsd: String = "",
    val rwfPerUsd: String = "",
) {
    fun fxSnapshot() = FxSnapshot(pkrPerUsd.toRateOrNull(), rwfPerUsd.toRateOrNull())
}

data class AppSnapshot(
    val preferences: AppPreferences = AppPreferences(),
    val purchases: List<Purchase> = emptyList(),
    val smokeEvents: List<SmokeEvent> = emptyList(),
    val cravingEvents: List<CravingEvent> = emptyList(),
    val isLoading: Boolean = true,
) {
    fun cigarettesFor(dayStartMillis: Long, dayEndMillis: Long): Int = smokeEvents
        .asSequence()
        .filter { it.smokedAtMillis in dayStartMillis until dayEndMillis }
        .sumOf { it.quantity }

    fun purchasesFor(month: YearMonth): List<Purchase> = purchases.filter {
        YearMonth.from(Instant.ofEpochMilli(it.purchasedAtMillis).atZone(java.time.ZoneId.systemDefault())) == month
    }
}

internal fun String.toRateOrNull(): String? = trim().takeIf { it.isNotEmpty() }
