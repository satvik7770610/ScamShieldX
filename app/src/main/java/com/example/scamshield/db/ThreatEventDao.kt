package com.example.scamshield.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatEventDao {

    @Query("SELECT * FROM threat_events ORDER BY timestamp DESC")
    fun getAllThreatEvents(): Flow<List<ThreatEventEntity>>

    @Query("SELECT * FROM threat_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentThreatEvents(limit: Int = 10): Flow<List<ThreatEventEntity>>

    @Query("SELECT * FROM threat_events WHERE id = :id")
    suspend fun getThreatEventById(id: Long): ThreatEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreatEvent(event: ThreatEventEntity): Long

    @Query("DELETE FROM threat_events")
    suspend fun clearAllThreatEvents()

    @Query("DELETE FROM threat_events WHERE id = :id")
    suspend fun deleteThreatEventById(id: Long)
}