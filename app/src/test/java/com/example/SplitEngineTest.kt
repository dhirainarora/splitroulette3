package com.example

import com.example.engine.CurrencyConfig
import com.example.engine.RoundingOption
import com.example.engine.SplitEngine
import com.example.engine.SplitMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitEngineTest {

    @Test
    fun testMathematicalExactSumInFairMode() {
        val names = listOf("Alice", "Bob", "Charlie", "David")
        val bill = 2000.0

        for (i in 0 until 20) {
            val result = SplitEngine.calculateSplit(
                names = names,
                totalBill = bill,
                currency = CurrencyConfig.INR,
                mode = SplitMode.FAIR,
                roundingOption = RoundingOption.CLEAN_NOTE
            )

            assertEquals(200000L, result.totalBillCents)
            val sum = result.participants.sumOf { it.amountCents }
            assertEquals(200000L, sum)
            assertTrue(result.participants.all { it.amountCents > 0L })
            assertEquals(4, result.participants.size)
        }
    }

    @Test
    fun testChaosModeMultipleHeavyHitters() {
        val names = listOf("Alice", "Bob", "Charlie", "Diana", "Evan")
        val bill = 5000.0

        for (i in 0 until 30) {
            val result = SplitEngine.calculateSplit(
                names = names,
                totalBill = bill,
                currency = CurrencyConfig.INR,
                mode = SplitMode.CHAOS,
                roundingOption = RoundingOption.CLEAN_NOTE
            )

            val sum = result.participants.sumOf { it.amountCents }
            assertEquals(500000L, sum)
            assertTrue(result.participants.all { it.amountCents > 0L })
            assertNotNull(result.biggestHitPerson)
            assertEquals(1, result.participants.first().rank)
        }
    }

    @Test
    fun testWildAndMayhemModes() {
        val names = listOf("P1", "P2", "P3", "P4")
        val bill = 1000.0

        for (mode in listOf(SplitMode.WILD, SplitMode.MAYHEM)) {
            val result = SplitEngine.calculateSplit(
                names = names,
                totalBill = bill,
                currency = CurrencyConfig.USD,
                mode = mode,
                roundingOption = RoundingOption.CLEAN_10
            )

            val sum = result.participants.sumOf { it.amountCents }
            assertEquals(100000L, sum)
            assertTrue(result.participants.all { it.amountCents > 0L })
        }
    }

    @Test
    fun testOddReceiptRemainderBalancedCleanly() {
        val names = listOf("A", "B", "C")
        val oddBill = 2017.0 // Non-multiple of 50 or 10

        val result = SplitEngine.calculateSplit(
            names = names,
            totalBill = oddBill,
            currency = CurrencyConfig.INR,
            mode = SplitMode.CHAOS,
            roundingOption = RoundingOption.CLEAN_NOTE
        )

        val sum = result.participants.sumOf { it.amountCents }
        assertEquals(201700L, sum)
        assertTrue(result.participants.all { it.amountCents > 0L })
    }

    @Test
    fun testTwoPeopleSplit() {
        val names = listOf("Alex", "Sam")
        val bill = 1250.0

        val result = SplitEngine.calculateSplit(
            names = names,
            totalBill = bill,
            currency = CurrencyConfig.INR,
            mode = SplitMode.MAYHEM,
            roundingOption = RoundingOption.CLEAN_NOTE
        )

        val sum = result.participants.sumOf { it.amountCents }
        assertEquals(125000L, sum)
        assertEquals(2, result.participants.size)
        assertTrue(result.participants.all { it.amountCents > 0L })
    }
}
