package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object Money {
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
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            this.currency = java.util.Currency.getInstance(currency.isoCode)
            minimumFractionDigits = currency.fractionDigits
            maximumFractionDigits = currency.fractionDigits
        }
        val formatted = formatter.format(value)
        return if (includeCode) "$formatted (${currency.isoCode})" else formatted
    }
}
