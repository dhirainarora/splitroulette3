package com.example.engine

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.random.Random

enum class SplitMode(
    val title: String,
    val subtitle: String,
    val description: String
) {
    FAIR(
        title = "FAIR",
        subtitle = "Varied & Dynamic",
        description = "Lively, unpredictable spread across everyone without punishing anyone."
    ),
    CHAOS(
        title = "CHAOS",
        subtitle = "Maximum Entropy",
        description = "Total wild card! Highly randomized spread where 2 or 3 people pay heavily, or unpredictable swings."
    ),
    WILD(
        title = "WILD",
        subtitle = "Heavy Blows",
        description = "1, 2, or more people take heavy hits depending on group size, others get off easy."
    ),
    MAYHEM(
        title = "MAYHEM",
        subtitle = "Roulette Stakes",
        description = "Extreme disparity! 1 or 2 victims take up to 85%+ of the bill, others pay token notes."
    )
}

enum class RoundingOption(
    val title: String,
    val description: String
) {
    CLEAN_NOTE(
        title = "Clean ₹50 / ₹10",
        description = "Shares are rounded to clean denominations for easy UPI or cash notes."
    ),
    CLEAN_10(
        title = "Clean 10s",
        description = "Shares are rounded to the nearest 10 for quick contactless transfers."
    ),
    EXACT(
        title = "Exact Coins",
        description = "Mathematically precise allocation down to individual coins and cents."
    )
}

data class CurrencyConfig(
    val code: String,
    val symbol: String,
    val name: String,
    val majorStepCents: Long, // 5000 = ₹50 or 500 = $5
    val minorStepCents: Long  // 1000 = ₹10 or 100 = $1
) {
    companion object {
        val INR = CurrencyConfig("INR", "₹", "Indian Rupee", 5000L, 1000L)
        val USD = CurrencyConfig("USD", "$", "US Dollar", 500L, 100L)
        val EUR = CurrencyConfig("EUR", "€", "Euro", 500L, 100L)
        val GBP = CurrencyConfig("GBP", "£", "British Pound", 500L, 100L)

        val ALL = listOf(INR, USD, EUR, GBP)

        fun fromCode(code: String): CurrencyConfig {
            return ALL.find { it.code.equals(code, ignoreCase = true) } ?: INR
        }
    }
}

data class SplitParticipant(
    val rank: Int = 0,
    val name: String,
    val amountCents: Long,
    val formattedAmount: String,
    val percentageOfTotal: Double,
    val diffFromAverageCents: Long,
    val formattedDiff: String,
    val isBiggestHit: Boolean = false
)

data class SplitResult(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalBillCents: Long,
    val formattedTotal: String,
    val currency: CurrencyConfig,
    val mode: SplitMode,
    val roundingOption: RoundingOption,
    val participants: List<SplitParticipant>,
    val biggestHitPerson: SplitParticipant,
    val roundingNote: String
)

object SplitEngine {

    /**
     * Formats an amount in cents according to the selected currency.
     * Whole numbers are formatted cleanly without awkward decimals (.00).
     */
    fun formatCurrency(amountCents: Long, currency: CurrencyConfig): String {
        val symbol = currency.symbol
        val isNegative = amountCents < 0
        val absCents = abs(amountCents)
        val whole = absCents / 100
        val remainder = absCents % 100

        val formattedNumber = if (remainder == 0L) {
            String.format(Locale.US, "%,d", whole)
        } else {
            String.format(Locale.US, "%,d.%02d", whole, remainder)
        }

        return if (isNegative) "-$symbol$formattedNumber" else "$symbol$formattedNumber"
    }

    /**
     * Executes the constrained randomized allocation algorithm.
     * Guaranteed:
     * 1. sum(amounts) == totalBillCents exactly down to the cent.
     * 2. every person pays a strictly positive amount (> 0).
     * 3. Honors denomination quantum steps wherever mathematically possible.
     */
    fun calculateSplit(
        names: List<String>,
        totalBill: Double,
        currency: CurrencyConfig,
        mode: SplitMode,
        roundingOption: RoundingOption
    ): SplitResult {
        require(names.size >= 2) { "At least 2 participants required" }
        require(totalBill > 0.0) { "Bill must be greater than zero" }

        val n = names.size
        val totalBillCents = (totalBill * 100.0).roundToLong()
        require(totalBillCents >= n) { "Bill is too small for $n participants" }

        // Step 1: Generate high-entropy randomized raw weights based on selected mode
        val weights = generateWeights(n, mode)

        // Step 2: Determine appropriate quantum step size
        var quantum = when (roundingOption) {
            RoundingOption.CLEAN_NOTE -> currency.majorStepCents
            RoundingOption.CLEAN_10 -> currency.minorStepCents
            RoundingOption.EXACT -> if (totalBillCents % 100L == 0L) 100L else 1L
        }

        // Graceful fallback if bill is small relative to quantum
        if (totalBillCents < n * quantum) {
            quantum = currency.minorStepCents
        }
        if (totalBillCents < n * quantum) {
            quantum = if (totalBillCents % 100L == 0L) 100L else 1L
        }
        if (totalBillCents < n * quantum) {
            quantum = 1L
        }

        // Step 3: Quantize into units and resolve remainder
        val totalUnits = totalBillCents / quantum
        val oddRemainder = totalBillCents % quantum

        // Each person gets a guaranteed base of 1 unit
        val allocUnits = LongArray(n) { 1L }
        var availableUnits = totalUnits - n

        if (availableUnits > 0) {
            // Allocate proportionally by weight
            val fractionalParts = DoubleArray(n)
            var allocatedCount = 0L

            for (i in 0 until n) {
                val floatUnits = availableUnits * weights[i]
                val base = floor(floatUnits).toLong()
                allocUnits[i] += base
                allocatedCount += base
                fractionalParts[i] = floatUnits - base
            }

            var leftover = availableUnits - allocatedCount
            // Distribute leftover units based on highest fractional remnants with random tie-breakers
            val indexed = (0 until n).sortedWith(
                compareByDescending<Int> { fractionalParts[it] }
                    .thenBy { Random.nextInt() }
            )

            var idx = 0
            while (leftover > 0) {
                allocUnits[indexed[idx % n]] += 1L
                leftover--
                idx++
            }
        }

        // Step 4: Convert units back to cents
        val finalCents = LongArray(n) { allocUnits[it] * quantum }

        // If the receipt has an odd remainder that doesn't divide by quantum,
        // absorb it cleanly into the highest payer (or random if tied)
        if (oddRemainder > 0L) {
            var maxIdx = 0
            for (i in 1 until n) {
                if (finalCents[i] > finalCents[maxIdx]) {
                    maxIdx = i
                }
            }
            finalCents[maxIdx] += oddRemainder
        }

        // Step 5: INVARIANT ASSERTIONS (Strict verification)
        val verifiedSum = finalCents.sum()
        check(verifiedSum == totalBillCents) {
            "Internal Math Invariant Violated: Sum $verifiedSum != Bill $totalBillCents"
        }
        check(finalCents.all { it > 0L }) {
            "Internal Math Invariant Violated: Found non-positive allocation"
        }

        // Step 6: Assemble participants sorted by payment descending
        val avgCents = totalBillCents / n
        val participantList = names.mapIndexed { index, name ->
            val cents = finalCents[index]
            val pct = (cents.toDouble() / totalBillCents.toDouble()) * 100.0
            val diff = cents - avgCents
            val sign = if (diff >= 0) "+" else "-"
            val absDiffFormatted = formatCurrency(abs(diff), currency)
            val formattedDiff = "$sign$absDiffFormatted"

            SplitParticipant(
                rank = 0, // Will be set after sorting
                name = name.trim(),
                amountCents = cents,
                formattedAmount = formatCurrency(cents, currency),
                percentageOfTotal = pct,
                diffFromAverageCents = diff,
                formattedDiff = formattedDiff,
                isBiggestHit = false
            )
        }.sortedByDescending { it.amountCents }

        // Mark ranks and Biggest Hit
        val maxAmount = participantList.first().amountCents
        val rankedParticipants = participantList.mapIndexed { rankIdx, p ->
            p.copy(
                rank = rankIdx + 1,
                isBiggestHit = p.amountCents == maxAmount
            )
        }

        val roundingNote = when {
            oddRemainder == 0L && quantum >= 1000L -> "Clean Cash & UPI Denominations (100% Exact)"
            oddRemainder > 0L -> "Clean Denominations (Receipt Remainder Balanced)"
            else -> "Exact Currency Split (100% Exact)"
        }

        return SplitResult(
            timestamp = System.currentTimeMillis(),
            totalBillCents = totalBillCents,
            formattedTotal = formatCurrency(totalBillCents, currency),
            currency = currency,
            mode = mode,
            roundingOption = roundingOption,
            participants = rankedParticipants,
            biggestHitPerson = rankedParticipants.first(),
            roundingNote = roundingNote
        )
    }

    /**
     * Generates normalized weights summing to 1.0 according to mode specifications.
     */
    private fun generateWeights(n: Int, mode: SplitMode): DoubleArray {
        val raw = DoubleArray(n)

        when (mode) {
            SplitMode.FAIR -> {
                // Dynamic, lively spread around average.
                // Significant jitter (+-35% to +-50%) so everyone pays something visibly different!
                for (i in 0 until n) {
                    val base = 1.0
                    val jitter = Random.nextDouble(-0.45, 0.45)
                    raw[i] = (base + jitter).coerceAtLeast(0.25)
                }
            }

            SplitMode.CHAOS -> {
                // Maximum Entropy! User explicitly requested high randomness:
                // Not always 1 person paying the most; sometimes 2 or 3 people get hit hard!
                val scenario = Random.nextInt(4)
                when (scenario) {
                    0 -> {
                        // Scenario 0: Two heavy hitters (30% probability)
                        val indices = (0 until n).shuffled()
                        val hit1 = indices[0]
                        val hit2 = if (n > 1) indices[1] else indices[0]

                        for (i in 0 until n) {
                            raw[i] = if (i == hit1 || i == hit2) {
                                Random.nextDouble(3.5, 6.0)
                            } else {
                                Random.nextDouble(0.4, 1.2)
                            }
                        }
                    }

                    1 -> {
                        // Scenario 1: Three heavy hitters (if n >= 4) or 2 (if n <= 3)
                        val indices = (0 until n).shuffled()
                        val hitCount = if (n >= 4) 3 else 2
                        val hits = indices.take(hitCount).toSet()

                        for (i in 0 until n) {
                            raw[i] = if (i in hits) {
                                Random.nextDouble(2.8, 4.5)
                            } else {
                                Random.nextDouble(0.3, 1.0)
                            }
                        }
                    }

                    2 -> {
                        // Scenario 2: Exponential power curve (unpredictable steepness)
                        for (i in 0 until n) {
                            val r = Random.nextDouble(1.0, 8.0)
                            raw[i] = Math.pow(r, 2.2)
                        }
                        raw.shuffle()
                    }

                    else -> {
                        // Scenario 3: Bimodal split - top tier vs low tier
                        val indices = (0 until n).shuffled()
                        val topTierCount = (1..maxOf(1, n / 2)).random()
                        val topTier = indices.take(topTierCount).toSet()

                        for (i in 0 until n) {
                            raw[i] = if (i in topTier) {
                                Random.nextDouble(3.0, 5.5)
                            } else {
                                Random.nextDouble(0.5, 1.3)
                            }
                        }
                    }
                }
            }

            SplitMode.WILD -> {
                // Heavy blows! 1 or 2 (or 3 in larger groups) take substantial hits.
                val indices = (0 until n).shuffled()
                val heavyCount = when {
                    n <= 2 -> 1
                    n in 3..5 -> if (Random.nextBoolean()) 1 else 2
                    else -> if (Random.nextDouble() < 0.6) 2 else 3
                }
                val heavySet = indices.take(heavyCount).toSet()

                for (i in 0 until n) {
                    raw[i] = if (i in heavySet) {
                        Random.nextDouble(4.5, 7.5)
                    } else {
                        Random.nextDouble(0.3, 0.9)
                    }
                }
            }

            SplitMode.MAYHEM -> {
                // Extreme Roulette Disparity!
                // 50% chance: 1 victim takes 80-88% of the bill!
                // 50% chance: 2 victims take ~85% of the bill collectively!
                val indices = (0 until n).shuffled()
                val victimCount = if (n >= 3 && Random.nextBoolean()) 2 else 1
                val victimSet = indices.take(victimCount).toSet()

                for (i in 0 until n) {
                    raw[i] = if (i in victimSet) {
                        Random.nextDouble(12.0, 20.0)
                    } else {
                        Random.nextDouble(0.2, 0.6) // Token amounts
                    }
                }
            }
        }

        // Normalize weights so sum is 1.0
        val sum = raw.sum()
        return DoubleArray(n) { raw[it] / sum }
    }
}
