package com.amirmalik.breatheasy.data
import com.amirmalik.breatheasy.domain.SafeArithmetic

import android.content.Context
import android.icu.util.ULocale
import android.telephony.TelephonyManager
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Currency
import java.util.Locale
import java.util.UUID

/**
 * Currency metadata backed by the platform ISO-4217 currency database.
 * Currency is selected once at database creation and then remains stable for this install.
 */
data class CurrencyCode(
    val isoCode: String,
    val displayName: String,
    val symbol: String,
    val fractionDigits: Int,
) {
    companion object {
        /**
         * Resolve a known ISO-4217 currency code without substituting the current
         * device locale. Use this for persisted transaction data so an invalid
         * historical code is never silently reinterpreted as another currency.
         */
        fun fromCodeOrNull(value: String?): CurrencyCode? {
            val code = value?.trim()?.uppercase(Locale.ROOT)
                ?.takeIf { it.matches(Regex("[A-Z]{3}")) } ?: return null
            val currency = runCatching { Currency.getInstance(code) }.getOrNull()
                ?: return null
            return fromJavaCurrency(currency)
        }

        /**
         * Convenience resolver for defaults and optional values. Persisted transaction
         * records should use [fromCodeOrNull] and handle an unknown code explicitly.
         */
        fun fromCode(value: String?): CurrencyCode =
            fromCodeOrNull(value) ?: fromLocale(Locale.getDefault())

        fun fromCountry(countryCode: String?): CurrencyCode? {
            val country = countryCode?.trim()?.uppercase(Locale.ROOT)
                ?.takeIf { it.matches(Regex("[A-Z]{2}")) } ?: return null
            val currency = runCatching {
                Currency.getInstance(Locale("", country))
            }.getOrNull() ?: return null
            return fromJavaCurrency(currency)
        }

        /**
         * Prefer the country of the currently registered mobile network when available.
         * Wi-Fi-only devices and phones without service fall back to the configured device
         * region. This does not request location or phone-state permissions.
         */
        fun detect(context: Context): CurrencyCode {
            val networkCountry = runCatching {
                val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                telephony?.networkCountryIso
            }.getOrNull()
            fromCountry(networkCountry)?.let { return it }

            val configuredLocales = context.resources.configuration.locales
            for (index in 0 until configuredLocales.size()) {
                fromCountry(configuredLocales[index].country)?.let { return it }
            }

            return fromLocale(Locale.getDefault(Locale.Category.FORMAT))
        }

        fun fromLocale(locale: Locale): CurrencyCode {
            val likelyLocale = ULocale.addLikelySubtags(ULocale.forLocale(locale)).toLocale()
            val currency = sequenceOf(locale, likelyLocale)
                .mapNotNull { candidate -> runCatching { Currency.getInstance(candidate) }.getOrNull() }
                .firstOrNull { it.defaultFractionDigits >= 0 }
                ?: throw IllegalStateException("No ISO currency could be resolved for the device region.")
            return fromJavaCurrency(currency)
                ?: throw IllegalStateException("The device region resolved to a currency without a supported minor unit.")
        }

        private fun fromJavaCurrency(currency: Currency): CurrencyCode? {
            val digits = currency.defaultFractionDigits
            if (digits < 0) return null
            val displayLocale = Locale.getDefault(Locale.Category.DISPLAY)
            return CurrencyCode(
                isoCode = currency.currencyCode,
                displayName = currency.getDisplayName(displayLocale),
                symbol = currency.getSymbol(displayLocale),
                fractionDigits = digits,
            )
        }
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
) {
    val totalCigarettes: Long?
        get() = type.cigarettesPerUnit?.let { unit ->
            Math.multiplyExact(quantity.toLong(), unit.toLong())
        }
}

data class SmokeEvent(
    val id: String = UUID.randomUUID().toString(),
    val smokedAtMillis: Long = System.currentTimeMillis(),
    /** Actual cigarettes smoked. This always contributes to smoking totals. */
    val quantity: Int = 1,
    val trigger: CravingTrigger = CravingTrigger.OTHER,
    val note: String = "",
    /** Cigarettes taken from tracked stock; zero when smoking without available stock. */
    val stockQuantity: Int = quantity,
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
    val displayCurrency: CurrencyCode = CurrencyCode.fromLocale(Locale.getDefault()),
    /** User-defined pacing target; not a medically recommended limit. */
    val dailyTarget: Int = 4,
    val baselinePerDay: Int = 15,
    /** Cigarettes physically on hand before the user begins logging purchases. */
    val startingStockCigarettes: Int = 0,
)

data class AppSnapshot(
    val preferences: AppPreferences = AppPreferences(),
    val purchases: List<Purchase> = emptyList(),
    val smokeEvents: List<SmokeEvent> = emptyList(),
    val cravingEvents: List<CravingEvent> = emptyList(),
    val isLoading: Boolean = true,
) {
    /** Uses Long totals so long histories cannot wrap into negative daily consumption. */
    fun cigarettesFor(dayStartMillis: Long, dayEndExclusiveMillis: Long): Long {
        require(dayEndExclusiveMillis >= dayStartMillis) { "Day range end cannot precede its start." }
        return smokeEvents.asSequence()
            .filter { it.smokedAtMillis >= dayStartMillis && it.smokedAtMillis < dayEndExclusiveMillis }
            .fold(0L) { total, event -> SafeArithmetic.add(total, event.quantity.toLong()) }
    }

    fun purchasesFor(
        month: YearMonth,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<Purchase> = purchases.filter {
        YearMonth.from(Instant.ofEpochMilli(it.purchasedAtMillis).atZone(zoneId)) == month
    }
}
