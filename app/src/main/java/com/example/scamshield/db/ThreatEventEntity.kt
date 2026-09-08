package com.example.scamshield.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threat_events")
data class ThreatEventEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val inputType: String,
    val riskScore: Int,
    val riskLevel: String,
    val threatCategory: String,
    val shortExplanation: String,
    val detectedSignals: String,
    val recipient: String? = null
)