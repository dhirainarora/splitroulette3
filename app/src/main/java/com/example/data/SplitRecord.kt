package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "split_records")
data class SplitRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val totalBillCents: Long,
    val currencyCode: String,
    val currencySymbol: String,
    val mode: String,
    val roundingOption: String,
    val peopleCount: Int,
    val biggestPayerName: String,
    val biggestPayerAmountCents: Long,
    val participantsJson: String,
    val roundingNote: String
)
