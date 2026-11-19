package com.amirmalik.breatheasy.domain

import com.amirmalik.breatheasy.data.CurrencyCode
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

object Money {
    /** Parse a user-entered major-unit amount into exact minor units. */
    fun parseToMinor(
        input: String,
        currency: CurrencyCode,
        locale: Locale = Locale.getDefault(),
    ): Long? {
        return try {
            val raw = input.trim()
            val symbols = DecimalFormatSymbols.getInstance(locale)
            val decimalSeparator = symbols.decimalSeparator
            val groupingSeparator = symbols.groupingSeparator
            // A locale's grouping character must never be reinterpreted as a decimal
            // separator (for example, German "1.000" must not become 1.00).
            val containsGroupingSeparator =
                groupingSeparator != decimalSeparator && raw.contains(groupingSeparator)
            val normalized = if (decimalSeparator == '.') raw else raw.replace(decimalSeparator, '.')
            if (containsGroupingSeparator || normalized.isEmpty() ||
                !normalized.matches(Regex("(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)"))
            ) {
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
        val formatted = formatMajor(value, currency)
        return if (includeCode) formatted + " (" + currency.isoCode + ")" else formatted
    }

    /**
     * Format a major-unit amount using the selected currency and the device locale.
     * Currency symbols/codes are separated from the number with a normal space,
     * even on locales whose platform format concatenates them or inserts NBSP.
     */
    fun formatMajor(value: BigDecimal, currency: CurrencyCode): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            this.currency = java.util.Currency.getInstance(currency.isoCode)
            minimumFractionDigits = currency.fractionDigits
            maximumFractionDigits = currency.fractionDigits
        }
        return normalizeCurrencySpacing(formatter.format(value))
    }

    private fun normalizeCurrencySpacing(formatted: String): String {
        var result = formatted
            .replace('\u00A0', ' ')
            .replace('\u202F', ' ')
            .replace('\u2007', ' ')

        val firstDigitIndex = result.indexOfFirst(Char::isDigit)
        if (firstDigitIndex < 0) return result

        val prefix = result.substring(0, firstDigitIndex)
        val prefixHasCurrency = prefix.any {
            it.isLetter() || Character.getType(it) == Character.CURRENCY_SYMBOL.toInt()
        }
        if (prefixHasCurrency && prefix.isNotEmpty() && !prefix.last().isWhitespace()) {
            result = result.substring(0, firstDigitIndex) + " " + result.substring(firstDigitIndex)
        }

        val lastDigitIndex = result.indexOfLast(Char::isDigit)
        val suffix = result.substring(lastDigitIndex + 1)
        val suffixHasCurrency = suffix.any {
            it.isLetter() || Character.getType(it) == Character.CURRENCY_SYMBOL.toInt()
        }
        if (suffixHasCurrency && suffix.isNotEmpty() && !suffix.first().isWhitespace()) {
            result = result.substring(0, lastDigitIndex + 1) + " " + suffix
        }

        return result
    }
}
