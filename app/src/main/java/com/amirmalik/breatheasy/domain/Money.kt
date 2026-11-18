package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import com.amirmalik.breatheasy.data.FxSnapshot
import com.amirmalik.breatheasy.data.Purchase
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Money {
    private val mathContext = MathContext.DECIMAL128

    /** Parse a user-entered major-unit amount into exact minor units. */
    fun parseToMinor(input: String, currency: CurrencyCode): Long? {
        return try {
            val raw = input.trim()
            val decimalSeparator = DecimalFormatSymbols.getInstance(Locale.getDefault()).decimalSeparator
            val normalized = if (decimalSeparator == '.') raw else raw.replace(decimalSeparator, '.')
            // Reject grouping separators instead of guessing whether a comma means
            // thousands or a decimal fraction. This prevents an accidental 100x error.
            if (normalized.isEmpty() || !normalized.matches(Regex("(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)"))) {
                null
            } else {
                BigDecimal(normalized)
                    .setScale(currency.fractionDigits, RoundingMode.UNNECESSARY)
                    .movePointRight(currency.fractionDigits)
                    .longValueExact()
                    .takeIf { it > 0L }
            }
        } catch (_: NumberFormatException) {
            null
        } catch (_: ArithmeticException) {
            null
        }
    }

    fun format(minor: Long, currency: CurrencyCode, includeCode: Boolean = false): String {
        val value = BigDecimal.valueOf(minor, currency.fractionDigits)
        val pattern = if (currency.fractionDigits == 0) "#,##0" else "#,##0." + "0".repeat(currency.fractionDigits)
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.getDefault()))
        val amount = formatter.format(value)
        return if (includeCode) "${currency.symbol} $amount ${currency.isoCode}" else "${currency.symbol} $amount"
    }

    /**
     * Convert using the rates captured on the purchase. Rates are units of the named
     * currency per one USD. Returns null rather than guessing when a required rate is missing.
     */
    fun convertMinor(
        amountMinor: Long,
        from: CurrencyCode,
        to: CurrencyCode,
        rates: FxSnapshot,
    ): Long? {
        if (from == to) return amountMinor
        val fromPerUsd = unitsPerUsd(from, rates) ?: return null
        val toPerUsd = unitsPerUsd(to, rates) ?: return null
        if (fromPerUsd.signum() <= 0 || toPerUsd.signum() <= 0) return null

        return try {
            val sourceMajor = BigDecimal.valueOf(amountMinor, from.fractionDigits)
            val usdMajor = sourceMajor.divide(fromPerUsd, mathContext)
            val targetMajor = usdMajor.multiply(toPerUsd, mathContext)
            targetMajor
                .movePointRight(to.fractionDigits)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        } catch (_: ArithmeticException) {
            null
        }
    }

    fun snapshot(purchase: Purchase) = purchase.fxSnapshot

    private fun unitsPerUsd(currency: CurrencyCode, rates: FxSnapshot): BigDecimal? = when (currency) {
        CurrencyCode.USD -> BigDecimal.ONE
        CurrencyCode.PKR -> rates.pkrPerUsd?.toBigDecimalOrNull()
        CurrencyCode.RWF -> rates.rwfPerUsd?.toBigDecimalOrNull()
    }
}
