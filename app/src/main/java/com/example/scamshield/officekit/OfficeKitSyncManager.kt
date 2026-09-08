package com.example.scamshield.officekit

import android.content.Context
import android.content.SharedPreferences
import com.example.scamshield.model.CategoryFormatter
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.ui.components.buildThreatChainNodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

data class OfficeKitState(
    val consoleIp: String = "10.0.2.2",
    val consolePort: Int = 8085,
    val connectionStatus: String = "DISCONNECTED", // CONNECTED, DISCONNECTED, CONNECTING, ERROR
    val isConnected: Boolean = false,
    val lastError: String? = null,
    val lastSyncTime: Long = 0L,
    val totalSyncedCount: Int = 0
)

class OfficeKitSyncManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = appContext.getSharedPreferences("office_kit_prefs", Context.MODE_PRIVATE)

    private val sentEventIds = ConcurrentHashMap<String, Long>()

    private val _syncState = MutableStateFlow(
        OfficeKitState(
            consoleIp = prefs.getString("console_ip", "10.0.2.2") ?: "10.0.2.2",
            consolePort = prefs.getInt("console_port", 8085)
        )
    )
    val syncState: StateFlow<OfficeKitState> = _syncState.asStateFlow()

    companion object {
        @Volatile
        private var instance: OfficeKitSyncManager? = null

        fun getInstance(context: Context): OfficeKitSyncManager {
            return instance ?: synchronized(this) {
                instance ?: OfficeKitSyncManager(context).also { instance = it }
            }
        }
    }

    fun updateConsoleIp(ip: String) {
        val trimmed = ip.trim()
        if (trimmed.isNotBlank()) {
            prefs.edit().putString("console_ip", trimmed).apply()
            _syncState.value = _syncState.value.copy(
                consoleIp = trimmed,
                connectionStatus = "DISCONNECTED",
                isConnected = false,
                lastError = null
            )
        }
    }

    fun testConnection(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        scope.launch {
            _syncState.value = _syncState.value.copy(
                connectionStatus = "CONNECTING",
                lastError = null
            )

            try {
                val targetIp = _syncState.value.consoleIp
                val targetPort = _syncState.value.consolePort
                val endpointUrl = "http://$targetIp:$targetPort/api/ping"

                val json = JSONObject().apply {
                    put("type", "PING")
                    put("device", "ScamShield Android")
                }

                println("OFFICE KIT SYNC: Sending PING to $endpointUrl")

                val url = URL(endpointUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connectTimeout = 3000
                    readTimeout = 3000
                    doOutput = true
                }

                conn.outputStream.use { os ->
                    val input = json.toString().toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = conn.responseCode
                val responseText = if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                }
                conn.disconnect()

                if (responseCode in 200..299 && responseText.contains("PONG")) {
                    println("OFFICE KIT SYNC: Received PONG from laptop! Response: $responseText")
                    _syncState.value = _syncState.value.copy(
                        connectionStatus = "CONNECTED",
                        isConnected = true,
                        lastError = null,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    onResult(true, "✓ LAPTOP CONNECTED (PONG Received)")
                } else {
                    val errorMsg = "HTTP $responseCode: $responseText"
                    println("OFFICE KIT SYNC: $errorMsg")
                    _syncState.value = _syncState.value.copy(
                        connectionStatus = "ERROR",
                        isConnected = false,
                        lastError = errorMsg
                    )
                    onResult(false, errorMsg)
                }
            } catch (e: Throwable) {
                val errorMsg = e.javaClass.simpleName + ": " + (e.message ?: "Connection failed")
                println("OFFICE KIT SYNC Error: $errorMsg")
                _syncState.value = _syncState.value.copy(
                    connectionStatus = "ERROR",
                    isConnected = false,
                    lastError = errorMsg
                )
                onResult(false, errorMsg)
            }
        }
    }

    fun syncThreatEvent(
        result: RiskAnalysisResult,
        source: String = "Notification Shield",
        sourcePkg: String? = null
    ) {
        val eventId = "EVENT_" + (result.rawPayload.hashCode().toString() + "_" + System.currentTimeMillis())

        // Local deduplication check
        if (sentEventIds.containsKey(eventId)) {
            return
        }
        sentEventIds[eventId] = System.currentTimeMillis()

        scope.launch {
            try {
                val formattedCategory = CategoryFormatter.formatCategory(result.threatCategory, result.signals)
                val chainNodes = buildThreatChainNodes(result)
                val signalsList = result.signals.map { it.title.uppercase() }

                val json = JSONObject().apply {
                    put("eventId", eventId)
                    put("timestamp", System.currentTimeMillis())
                    put("source", source)
                    put("sourceApp", sourcePkg ?: JSONObject.NULL)
                    put("messageType", formattedCategory)
                    put("riskScore", result.score)
                    put("riskLevel", result.level.name)
                    put("confidencePercent", result.confidencePercent)
                    put("signals", JSONArray(signalsList))
                    put("threatChain", JSONArray(chainNodes))
                    put("domain", result.recipient ?: JSONObject.NULL)
                    put("recommendedAction", result.recommendedAction)
                    put("isDevTest", false)
                }

                val targetIp = _syncState.value.consoleIp
                val targetPort = _syncState.value.consolePort
                val endpointUrl = "http://$targetIp:$targetPort/api/threat-event"

                println("OFFICE KIT SYNC: Sending ThreatEvent to $endpointUrl (${formattedCategory}, ${result.score}/100)")

                val url = URL(endpointUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connectTimeout = 3000
                    readTimeout = 3000
                    doOutput = true
                }

                conn.outputStream.use { os ->
                    val input = json.toString().toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = conn.responseCode
                conn.disconnect()

                if (responseCode in 200..299) {
                    println("OFFICE KIT SYNC: ThreatEvent synced successfully! Response: $responseCode")
                    _syncState.value = _syncState.value.copy(
                        connectionStatus = "CONNECTED",
                        isConnected = true,
                        lastError = null,
                        lastSyncTime = System.currentTimeMillis(),
                        totalSyncedCount = _syncState.value.totalSyncedCount + 1
                    )
                } else {
                    println("OFFICE KIT SYNC: Server returned non-200 code: $responseCode")
                    _syncState.value = _syncState.value.copy(
                        connectionStatus = "ERROR",
                        isConnected = false,
                        lastError = "HTTP $responseCode"
                    )
                }
            } catch (e: Throwable) {
                val errorMsg = e.javaClass.simpleName + ": " + (e.message ?: "Sync failed")
                println("OFFICE KIT SYNC: Connection warning: $errorMsg")
                _syncState.value = _syncState.value.copy(
                    connectionStatus = "ERROR",
                    isConnected = false,
                    lastError = errorMsg
                )
            }
        }
    }
}
