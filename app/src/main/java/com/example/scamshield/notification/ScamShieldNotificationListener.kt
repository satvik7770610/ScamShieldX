package com.example.scamshield.notification

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.scamshield.MainActivity
import com.example.scamshield.data.ThreatHistoryRepository
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.CategoryFormatter
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.officekit.OfficeKitSyncManager
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ScamShieldNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var riskEngine: RiskEngine
    private lateinit var repository: ThreatHistoryRepository

    companion object {
        const val WARNING_CHANNEL_ID = "scamshield_warning_channel"
        const val WARNING_CHANNEL_NAME = "ScamShield Threat Warnings"
        const val TEST_SOURCE_CHANNEL_ID = "scamshield_test_source_channel"
        const val TEST_SOURCE_CHANNEL_NAME = "ScamShield Test"

        private val processedHashes = ConcurrentHashMap<String, Long>()
        private const val DUP_WINDOW_MS = 30_000L // 30 seconds deduplication

        fun isNotificationAccessGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return flat != null && flat.contains(packageName)
        }

        fun sendTestNotification(context: Context, title: String, body: String, isHighRisk: Boolean) {
            createTestNotificationChannel(context)
            val notificationManager = NotificationManagerCompat.from(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                body.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val testNotif = NotificationCompat.Builder(context, TEST_SOURCE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            try {
                println("NOTIFICATION TEST: Posting test notification title=$title")
                notificationManager.notify("TEST_EXTERNAL_MESSAGE", body.hashCode(), testNotif)
            } catch (e: SecurityException) {
                println("NOTIFICATION TEST: Security exception posting test notification: ${e.message}")
            }
        }

        private fun createTestNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    TEST_SOURCE_CHANNEL_ID,
                    TEST_SOURCE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Simulates incoming messaging notifications from third-party apps for test purposes"
                    enableVibration(true)
                }
                val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        riskEngine = RuleBasedRiskEngine()
        repository = ThreatHistoryRepository.getInstance(applicationContext)
        createNotificationChannel()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationShieldDiagnostics.updateListenerConnected(true)
        println("NOTIFICATION LISTENER: Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationShieldDiagnostics.updateListenerConnected(false)
        println("NOTIFICATION LISTENER: Listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val pkgName = sbn.packageName ?: ""
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sbn.notification?.channelId ?: ""
        } else {
            ""
        }
        val tag = sbn.tag ?: ""

        // 1. SELF-NOTIFICATION LOOP PREVENTION: Ignore ScamShield's OWN warning notifications
        if (pkgName == packageName && (channelId == WARNING_CHANNEL_ID || tag == "SCAMSHIELD_WARNING")) {
            NotificationShieldDiagnostics.recordIgnored()
            println("NOTIFICATION LISTENER: Ignored self warning notification tag=$tag")
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val fullText = "$title $text $bigText".trim()
        if (fullText.isBlank() || fullText.length < 10) {
            NotificationShieldDiagnostics.recordIgnored()
            return
        }

        // 2. Deduplication check
        val currentTime = System.currentTimeMillis()
        val textHash = fullText.hashCode().toString()
        val lastProcessed = processedHashes[textHash]
        if (lastProcessed != null && (currentTime - lastProcessed) < DUP_WINDOW_MS) {
            NotificationShieldDiagnostics.recordIgnored()
            println("NOTIFICATION LISTENER: Ignored duplicate notification hash=$textHash")
            return
        }
        processedHashes[textHash] = currentTime
        processedHashes.entries.removeIf { (currentTime - it.value) > DUP_WINDOW_MS }

        println("NOTIFICATION LISTENER: onNotificationPosted called")
        println("NOTIFICATION LISTENER: package = $pkgName")
        println("NOTIFICATION LISTENER: title = $title")
        println("NOTIFICATION LISTENER: text = $text")
        println("NOTIFICATION LISTENER: analysis started")

        NotificationShieldDiagnostics.recordReceived(pkgName)

        serviceScope.launch {
            try {
                // 3. Analyze incoming notification content through existing engine
                val result = riskEngine.analyze(AnalysisInput.TextInput(fullText))

                NotificationShieldDiagnostics.recordAnalyzed(result.score, result.threatCategory)
                println("NOTIFICATION LISTENER: analysis completed score = ${result.score}, category = ${result.threatCategory}")

                // 4. Sync threat event to Office Kit Threat Console
                OfficeKitSyncManager.getInstance(applicationContext)
                    .syncThreatEvent(result, "Notification Shield", pkgName)

                // 5. Save to Threat History if threat detected
                if (result.level == RiskLevel.HIGH_RISK || result.level == RiskLevel.SUSPICIOUS) {
                    repository.saveAnalysisResult(result, "NOTIFICATION")
                }

                // 5. Post appropriate warning notification based on risk level
                when (result.level) {
                    RiskLevel.HIGH_RISK -> postThreatWarningNotification(pkgName, fullText, result, RiskLevel.HIGH_RISK)
                    RiskLevel.SUSPICIOUS -> postThreatWarningNotification(pkgName, fullText, result, RiskLevel.SUSPICIOUS)
                    RiskLevel.LOW -> println("NOTIFICATION LISTENER: Low risk result analyzed (${result.score}/100). No warning notification posted.")
                }
            } catch (e: Throwable) {
                println("NOTIFICATION LISTENER: Analysis error: ${e.message}")
            }
        }
    }

    private fun postThreatWarningNotification(
        sourcePkg: String,
        payload: String,
        result: RiskAnalysisResult,
        level: RiskLevel
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            setAction(Intent.ACTION_SEND)
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, payload)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            payload.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayCategory = CategoryFormatter.formatCategory(result.threatCategory, result.signals)

        val notifTitle = when (level) {
            RiskLevel.HIGH_RISK -> "🚨 POTENTIAL FRAUD DETECTED"
            RiskLevel.SUSPICIOUS -> "⚠️ SUSPICIOUS MESSAGE"
            RiskLevel.LOW -> "✓ LOW RISK"
        }

        val topSignals = result.signals.take(3)
        val signalsText = if (topSignals.isNotEmpty()) {
            topSignals.joinToString("\n") { "• ${it.title}" }
        } else {
            "No significant suspicious signals detected."
        }

        val hasPaymentThreat = result.signals.any {
            it.code == "RECRUITMENT_FEE_DEMAND" || it.code == "RECRUITMENT_PAY_FOR_OFFER" ||
            it.code == "TEXT_MONEY_TRANSFER_DEMAND" || it.code == "JOB_PAYMENT_SCAM_COMBINATION" ||
            it.code == "PAYMENT_SUSPICIOUS_HANDLE" || it.code == "PAYMENT_PRIZE_REWARD_CLAIM"
        }

        val hasOtpThreat = result.signals.any {
            it.code == "TEXT_OTP_HARVEST" || it.code == "JOB_OTP_SCAM_COMBINATION" ||
            it.code == "URL_EMBEDDED_CREDENTIALS"
        }

        val actionWarning = when (level) {
            RiskLevel.HIGH_RISK -> {
                when {
                    hasPaymentThreat && hasOtpThreat -> "DON'T CLICK • DON'T PAY • DON'T SHARE OTP"
                    hasOtpThreat -> "DON'T CLICK • DON'T SHARE OTP"
                    hasPaymentThreat -> "DON'T CLICK • DON'T PAY"
                    else -> "DON'T CLICK • DON'T PAY • DON'T SHARE OTP"
                }
            }
            RiskLevel.SUSPICIOUS -> "VERIFY BEFORE CONTINUING"
            RiskLevel.LOW -> "SAFE TO PROCEED"
        }

        val bigTextContent = buildString {
            append("Risk Score: ${result.score}/100\n\n")
            append("Message Type:\n$displayCategory\n\n")
            append("Why we flagged it:\n$signalsText\n\n")
            append(actionWarning)
        }

        val builder = NotificationCompat.Builder(this, WARNING_CHANNEL_ID)
            .setSmallIcon(if (level == RiskLevel.LOW) R.drawable.ic_dialog_info else R.drawable.stat_sys_warning)
            .setContentTitle(notifTitle)
            .setContentText("Risk Score: ${result.score}/100 • $displayCategory")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextContent))
            .setPriority(
                when (level) {
                    RiskLevel.HIGH_RISK -> NotificationCompat.PRIORITY_HIGH
                    RiskLevel.SUSPICIOUS -> NotificationCompat.PRIORITY_DEFAULT
                    RiskLevel.LOW -> NotificationCompat.PRIORITY_LOW
                }
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "OPEN SCAMSHIELD", pendingIntent)

        if (level == RiskLevel.HIGH_RISK || level == RiskLevel.SUSPICIOUS) {
            builder.setDefaults(Notification.DEFAULT_ALL)
        } else {
            builder.setDefaults(0)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify("SCAMSHIELD_WARNING", payload.hashCode(), builder.build())
        } catch (e: SecurityException) {
            println("NOTIFICATION LISTENER: Permission warning: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WARNING_CHANNEL_ID,
                WARNING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies user when an incoming notification from another app contains high-risk scam patterns"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }
}