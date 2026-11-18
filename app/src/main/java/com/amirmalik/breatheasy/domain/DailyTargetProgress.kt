package com.amirmalik.breatheasy.domain

/**
 * A bounded visual representation of daily consumption against a user-defined target.
 * Consumption above the target remains visible in the count and status, while the
 * progress fraction stays within the arc's valid 0..1 range.
 */
data class DailyTargetProgress(
    val progressFraction: Float,
    val remainingCigarettes: Long,
    val excessCigarettes: Long,
    val statusLabel: String,
) {
    companion object {
        fun calculate(consumed: Long, target: Int): DailyTargetProgress {
            require(consumed >= 0L) { "Consumed cigarettes cannot be negative." }
            require(target > 0) { "Daily target must be greater than zero." }

            val targetLong = target.toLong()
            val remaining = (targetLong - consumed).coerceAtLeast(0L)
            val excess = (consumed - targetLong).coerceAtLeast(0L)
            val fraction = (consumed.toDouble() / targetLong.toDouble())
                .toFloat()
                .coerceIn(0f, 1f)

            val label = when {
                remaining > 0L -> "$remaining below target"
                excess > 0L -> "$excess over target"
                else -> "Target reached"
            }

            return DailyTargetProgress(
                progressFraction = fraction,
                remainingCigarettes = remaining,
                excessCigarettes = excess,
                statusLabel = label,
            )
        }
    }
}
