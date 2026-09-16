package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SplitRecord::class], version = 1, exportSchema = false)
abstract class SplitDatabase : RoomDatabase() {
    abstract fun splitDao(): SplitDao

    companion object {
        @Volatile
        private var INSTANCE: SplitDatabase? = null

        fun getInstance(context: Context): SplitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SplitDatabase::class.java,
                    "split_roulette.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
