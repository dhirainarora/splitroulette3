package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {
    @Query("SELECT * FROM split_records ORDER BY timestamp DESC")
    fun getAllSplits(): Flow<List<SplitRecord>>

    @Query("SELECT * FROM split_records WHERE id = :id")
    suspend fun getSplitById(id: Long): SplitRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: SplitRecord): Long

    @Query("DELETE FROM split_records WHERE id = :id")
    suspend fun deleteSplitById(id: Long)

    @Query("DELETE FROM split_records")
    suspend fun deleteAllSplits()
}
