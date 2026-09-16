package com.example.data

import com.example.engine.CurrencyConfig
import com.example.engine.RoundingOption
import com.example.engine.SplitEngine
import com.example.engine.SplitMode
import com.example.engine.SplitParticipant
import com.example.engine.SplitResult
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class SplitRepository(private val splitDao: SplitDao) {
    val allSplits: Flow<List<SplitRecord>> = splitDao.getAllSplits()

    suspend fun insert(result: SplitResult): Long {
        val record = toRecord(result)
        return splitDao.insertSplit(record)
    }

    suspend fun deleteById(id: Long) {
        splitDao.deleteSplitById(id)
    }

    suspend fun clearAll() {
        splitDao.deleteAllSplits()
    }

    companion object {
        fun toRecord(result: SplitResult): SplitRecord {
            val jsonArray = JSONArray()
            result.participants.forEach { p ->
                val obj = JSONObject().apply {
                    put("rank", p.rank)
                    put("name", p.name)
                    put("amountCents", p.amountCents)
                    put("formattedAmount", p.formattedAmount)
                    put("percentage", p.percentageOfTotal)
                    put("diffCents", p.diffFromAverageCents)
                    put("formattedDiff", p.formattedDiff)
                    put("isBiggestHit", p.isBiggestHit)
                }
                jsonArray.put(obj)
            }

            return SplitRecord(
                id = result.id,
                timestamp = result.timestamp,
                totalBillCents = result.totalBillCents,
                currencyCode = result.currency.code,
                currencySymbol = result.currency.symbol,
                mode = result.mode.name,
                roundingOption = result.roundingOption.name,
                peopleCount = result.participants.size,
                biggestPayerName = result.biggestHitPerson.name,
                biggestPayerAmountCents = result.biggestHitPerson.amountCents,
                participantsJson = jsonArray.toString(),
                roundingNote = result.roundingNote
            )
        }

        fun toResult(record: SplitRecord): SplitResult {
            val currency = CurrencyConfig.fromCode(record.currencyCode)
            val mode = runCatching { SplitMode.valueOf(record.mode) }.getOrDefault(SplitMode.CHAOS)
            val roundingOption = runCatching { RoundingOption.valueOf(record.roundingOption) }.getOrDefault(RoundingOption.CLEAN_NOTE)

            val participants = mutableListOf<SplitParticipant>()
            val jsonArray = JSONArray(record.participantsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                participants.add(
                    SplitParticipant(
                        rank = obj.optInt("rank", i + 1),
                        name = obj.optString("name", "Person ${i + 1}"),
                        amountCents = obj.optLong("amountCents", 0L),
                        formattedAmount = obj.optString("formattedAmount", SplitEngine.formatCurrency(obj.optLong("amountCents", 0L), currency)),
                        percentageOfTotal = obj.optDouble("percentage", 0.0),
                        diffFromAverageCents = obj.optLong("diffCents", 0L),
                        formattedDiff = obj.optString("formattedDiff", ""),
                        isBiggestHit = obj.optBoolean("isBiggestHit", i == 0)
                    )
                )
            }

            val biggestHit = participants.firstOrNull() ?: SplitParticipant(
                rank = 1,
                name = record.biggestPayerName,
                amountCents = record.biggestPayerAmountCents,
                formattedAmount = SplitEngine.formatCurrency(record.biggestPayerAmountCents, currency),
                percentageOfTotal = 100.0,
                diffFromAverageCents = 0L,
                formattedDiff = "+0",
                isBiggestHit = true
            )

            return SplitResult(
                id = record.id,
                timestamp = record.timestamp,
                totalBillCents = record.totalBillCents,
                formattedTotal = SplitEngine.formatCurrency(record.totalBillCents, currency),
                currency = currency,
                mode = mode,
                roundingOption = roundingOption,
                participants = participants,
                biggestHitPerson = biggestHit,
                roundingNote = record.roundingNote
            )
        }
    }
}
