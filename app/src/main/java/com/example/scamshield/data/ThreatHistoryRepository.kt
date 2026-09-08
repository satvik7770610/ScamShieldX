package com.example.scamshield.data

import android.content.Context
import com.example.scamshield.db.ScamShieldDatabase
import com.example.scamshield.db.ThreatEventEntity
import com.example.scamshield.model.RiskAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ThreatHistoryRepository private constructor(context: Context) {

    private val inMemoryList = MutableStateFlow<List<ThreatEventEntity>>(emptyList())

    private val dao = try {
        ScamShieldDatabase.getDatabase(context).threatEventDao()
    } catch (e: Throwable) {
        println("ScamShieldDatabase initialization warning: ${e.message}. Using in-memory threat history fallback.")
        null
    }

    fun getAllHistory(): Flow<List<ThreatEventEntity>> {
        return dao?.getAllThreatEvents() ?: inMemoryList
    }

    fun getRecentHistory(limit: Int = 10): Flow<List<ThreatEventEntity>> {
        return dao?.getRecentThreatEvents(limit) ?: inMemoryList.map { it.take(limit) }
    }

    suspend fun getHistoryById(id: Long): ThreatEventEntity? = withContext(Dispatchers.IO) {
        val dbResult = try {
            dao?.getThreatEventById(id)
        } catch (_: Throwable) {
            null
        }
        dbResult ?: inMemoryList.value.find { it.id == id }
    }

    suspend fun saveAnalysisResult(result: RiskAnalysisResult, inputType: String) = withContext(Dispatchers.IO) {
        val signalsString = result.signals.joinToString(", ") { it.title }
        val dbEntity = ThreatEventEntity(
            id = 0,
            timestamp = System.currentTimeMillis(),
            inputType = inputType,
            riskScore = result.score,
            riskLevel = result.level.name,
            threatCategory = result.threatCategory,
            shortExplanation = result.recommendedAction,
            detectedSignals = signalsString,
            recipient = result.recipient
        )

        println("Saving new history: type=$inputType, score=${result.score}, level=${result.level.name}")

        try {
            if (dao != null) {
                val insertedId = dao.insertThreatEvent(dbEntity)
                println("History saved successfully to Room: id=$insertedId")
            } else {
                val memEntity = dbEntity.copy(id = System.currentTimeMillis())
                inMemoryList.value = (listOf(memEntity) + inMemoryList.value).take(50)
                println("History saved successfully to Singleton in-memory: id=${memEntity.id}, count=${inMemoryList.value.size}")
            }
        } catch (_: Throwable) {
            val memEntity = dbEntity.copy(id = System.currentTimeMillis())
            inMemoryList.value = (listOf(memEntity) + inMemoryList.value).take(50)
            println("History saved fallback in-memory: id=${memEntity.id}, count=${inMemoryList.value.size}")
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        try {
            dao?.clearAllThreatEvents()
        } catch (_: Throwable) {
        }
        inMemoryList.value = emptyList()
        println("History cleared: count=0")
    }

    companion object {
        @Volatile
        private var INSTANCE: ThreatHistoryRepository? = null

        fun getInstance(context: Context): ThreatHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ThreatHistoryRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}