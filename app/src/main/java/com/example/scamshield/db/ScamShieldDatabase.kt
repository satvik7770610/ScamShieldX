package com.example.scamshield.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ThreatEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScamShieldDatabase : RoomDatabase() {

    abstract fun threatEventDao(): ThreatEventDao

    companion object {
        @Volatile
        private var INSTANCE: ScamShieldDatabase? = null

        fun getDatabase(context: Context): ScamShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScamShieldDatabase::class.java,
                    "scamshield_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}