package com.example.scamshield.ui.result

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamshield.model.CategoryFormatter
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.ScreenshotAnalysisResult
import com.example.scamshield.model.UrlRiskResult
import com.example.scamshield.ui.components.RiskScoreDisplay
import com.example.scamshield.ui.components.ScamShieldSectionLabel
import com.example.scamshield.ui.components.SecurityDivider
import com.example.scamshield.ui.components.ThreatChainView
import com.example.scamshield.ui.theme.DarkBorder
import com.example.scamshield.ui.theme.DarkSurface
import com.example.scamshield.ui.theme.DarkSurfaceVariant
import com.example.scamshield.ui.theme.PrimaryShield
import com.example.scamshield.ui.theme.RiskHigh
import com.example.scamshield.ui.theme.RiskLow
import com.example.scamshield.ui.theme.RiskSuspicious
import com.example.scamshield.ui.theme.TextMuted
import com.example.scamshield.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskResultScreen(
    rawPayload: String,
    onBack: () -> Unit,
    isSharedContent: Boolean = false,
    viewModel: RiskResultViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(rawPayload) {
        viewModel.evaluatePayload(rawPayload)
    }

    val result = uiState.result
    val screenshotResult = uiState.screenshotResult

    // Single-shot haptic warning triggered ONCE when entering HIGH RISK
    LaunchedEffect(result?.rawPayload, result?.level) {
        if (result != null && result.level == RiskLevel.HIGH_RISK) {
            triggerHapticWarning(context)
        }
    }

    LaunchedEffect(uiState.reportSubmitted) {
        if (uiState.reportSubmitted) {
            snackbarHostState.showSnackbar("Threat event reported and saved to local log.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SCAMSHIELD", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading || result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryShield)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SCAMSHIELD X",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryShield
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Analyzing content, domain signals & on-device intelligence...",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }
        } else {
            val level = result.level
            val formattedCategory = CategoryFormatter.formatCategory(result.threatCategory, result.signals)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Shared Content Banner
                if (isSharedContent) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, PrimaryShield, RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = PrimaryShield,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shared content received — Analyzing...",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryShield
                            )
                        }
                    }
                }

                // Headline Warning Title
                val headlineTitle = when (level) {
                    RiskLevel.HIGH_RISK -> "🚨 POTENTIAL FRAUD DETECTED"
                    RiskLevel.SUSPICIOUS -> "⚠️ SUSPICIOUS MESSAGE"
                    RiskLevel.LOW -> "✓ LOW RISK"
                }

                Text(
                    text = headlineTitle,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = when (level) {
                        RiskLevel.HIGH_RISK -> RiskHigh
                        RiskLevel.SUSPICIOUS -> RiskSuspicious
                        RiskLevel.LOW -> RiskLow
                    },
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Score Display
                RiskScoreDisplay(score = result.score, level = level)

                Spacer(modifier = Modifier.height(12.dp))

                // Message Type Display Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(4.dp)),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MESSAGE TYPE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formattedCategory,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                SecurityDivider(verticalPadding = 16.dp)

                // Visual Threat Chain
                ThreatChainView(result = result)

                SecurityDivider(verticalPadding = 16.dp)

                // Multi-URL Section if present
                if (screenshotResult != null && screenshotResult.urlResults.isNotEmpty()) {
                    MultiUrlSection(screenshotResult = screenshotResult)
                    SecurityDivider(verticalPadding = 16.dp)
                }

                // Recipient Context
                val payment = uiState.paymentPayload
                val recipientText = result.recipient ?: payment?.payeeVpa ?: payment?.payeeName
                if (!recipientText.isNullOrBlank()) {
                    ScamShieldSectionLabel(number = "03", title = "RECIPIENT CONTEXT")
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(4.dp)),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            DetailRow(label = "Recipient Address", value = recipientText)
                            if (!payment?.payeeName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                DetailRow(label = "Payee Name", value = payment?.payeeName ?: "")
                            }
                            if (!payment?.amount.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                DetailRow(label = "Demanded Amount", value = "₹${payment?.amount}")
                            }
                        }
                    }

                    SecurityDivider(verticalPadding = 16.dp)
                }

                // Detected Signals Section
                ScamShieldSectionLabel(
                    number = "04",
                    title = if (level == RiskLevel.LOW) "SIGNALS ANALYZED (${result.signals.size})" else "WHY WE FLAGGED IT (${result.signals.size})"
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (result.signals.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(4.dp)),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text(
                            text = "No significant suspicious signals detected in this message.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.signals.forEachIndexed { index, signal ->
                            RiskSignalRow(index = index + 1, signal = signal)
                        }
                    }
                }

                SecurityDivider(verticalPadding = 16.dp)

                // Recommended Action Section
                ScamShieldSectionLabel(number = "05", title = "RECOMMENDED ACTION")
                Spacer(modifier = Modifier.height(8.dp))

                val hasPaymentThreat = result.signals.any {
                    it.code == "RECRUITMENT_FEE_DEMAND" || it.code == "RECRUITMENT_PAY_FOR_OFFER" ||
                    it.code == "TEXT_MONEY_TRANSFER_DEMAND" || it.code == "JOB_PAYMENT_SCAM_COMBINATION" ||
                    it.code == "PAYMENT_SUSPICIOUS_HANDLE" || it.code == "PAYMENT_PRIZE_REWARD_CLAIM"
                }

                val hasOtpThreat = result.signals.any {
                    it.code == "TEXT_OTP_HARVEST" || it.code == "JOB_OTP_SCAM_COMBINATION" ||
                    it.code == "URL_EMBEDDED_CREDENTIALS"
                }

                val hasUrlThreat = result.signals.any {
                    it.code == "URL_LOOKALIKE_BRAND" || it.code == "URL_BRAND_IMPERSONATION" ||
                    it.code == "DOMAIN_IP_HOST" || it.code == "URL_SUSPICIOUS_PATH"
                } || result.threatCategory.contains("URL") || result.threatCategory.contains("Web Link")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(4.dp)),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (level == RiskLevel.HIGH_RISK) {
                            if (hasUrlThreat) {
                                Text(
                                    text = "• DON'T CLICK",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = RiskHigh
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            if (hasPaymentThreat) {
                                Text(
                                    text = "• DON'T PAY",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = RiskHigh
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            if (hasOtpThreat) {
                                Text(
                                    text = "• DON'T SHARE OTP",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = RiskHigh
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = "• VERIFY THROUGH AN OFFICIAL CHANNEL",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else if (level == RiskLevel.SUSPICIOUS) {
                            Text(
                                text = "• VERIFY BEFORE CONTINUING",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = RiskSuspicious
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• VERIFY THROUGH AN OFFICIAL CHANNEL",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "• NO IMMEDIATE THREAT DETECTED",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = RiskLow
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Always verify unknown senders before making payments or sharing private information.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.recommendedAction,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action CTA Buttons
                val primaryButtonText = when {
                    level == RiskLevel.HIGH_RISK && hasPaymentThreat -> "DON'T PAY"
                    level == RiskLevel.HIGH_RISK && hasOtpThreat -> "DON'T SHARE OTP"
                    level == RiskLevel.HIGH_RISK && hasUrlThreat -> "DON'T CLICK"
                    level == RiskLevel.HIGH_RISK -> "DON'T PROCEED"
                    level == RiskLevel.SUSPICIOUS -> "VERIFY BEFORE CONTINUING"
                    else -> "SAFE TO PROCEED"
                }

                val primaryButtonColor = when (level) {
                    RiskLevel.HIGH_RISK -> RiskHigh
                    RiskLevel.SUSPICIOUS -> RiskSuspicious
                    RiskLevel.LOW -> RiskLow
                }

                val primaryTextColor = if (level == RiskLevel.HIGH_RISK) Color.White else Color.Black

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryButtonColor,
                        contentColor = primaryTextColor
                    )
                ) {
                    Text(
                        text = primaryButtonText,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.toggleVerifyDialog(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Verify",
                                tint = PrimaryShield,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VERIFY FIRST",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryShield
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleReportDialog(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Report",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REPORT",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Verify Dialog
        if (uiState.showVerifyDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleVerifyDialog(false) },
                title = {
                    Text(
                        text = "How to Verify Safely",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "1. Call the recipient directly via a known, saved phone number or official website number.\n\n" +
                                    "2. Do NOT use phone numbers provided inside suspicious SMS messages or screenshots.\n\n" +
                                    "3. Remember: Banks and legitimate merchants will NEVER require upfront deposits to release prizes or refunds.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.toggleVerifyDialog(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryShield,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Understood", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Report Dialog
        if (uiState.showReportDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleReportDialog(false) },
                title = {
                    Text(
                        text = "Report Threat Signal",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "Report this suspicious payload to your local ScamShield log. No personal identifiable information will be transmitted.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.submitReport() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RiskHigh,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirm Report", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.toggleReportDialog(false) }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

@Composable
private fun MultiUrlSection(screenshotResult: ScreenshotAnalysisResult) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LINKS FOUND (${screenshotResult.urlResults.size})",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (screenshotResult.highRiskCount > 0) {
                    Text(
                        text = "🔴 ${screenshotResult.highRiskCount} High",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RiskHigh
                    )
                }
                if (screenshotResult.suspiciousCount > 0) {
                    Text(
                        text = "🟡 ${screenshotResult.suspiciousCount} Suspicious",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RiskSuspicious
                    )
                }
                if (screenshotResult.lowRiskCount > 0) {
                    Text(
                        text = "🟢 ${screenshotResult.lowRiskCount} Low",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RiskLow
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            screenshotResult.urlResults.forEach { urlResult ->
                UrlRiskCard(urlResult = urlResult)
            }
        }
    }
}

@Composable
private fun UrlRiskCard(urlResult: UrlRiskResult) {
    var expanded by remember { mutableStateOf(false) }

    val (badgeText, badgeColor) = when (urlResult.riskLevel) {
        RiskLevel.HIGH_RISK -> Pair("🔴 HIGH RISK", RiskHigh)
        RiskLevel.SUSPICIOUS -> Pair("🟡 SUSPICIOUS", RiskSuspicious)
        RiskLevel.LOW -> Pair("🟢 LOW RISK", RiskLow)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )

                Text(
                    text = "${urlResult.riskScore} / 100",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = urlResult.domain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (urlResult.contextSnippet.isNotBlank() && urlResult.contextSnippet != "General context") {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Context: \"${urlResult.contextSnippet}\"",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = urlResult.explanation,
                fontSize = 11.sp,
                color = TextMuted
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val actionBtnText = when (urlResult.riskLevel) {
                    RiskLevel.HIGH_RISK -> "DON'T OPEN"
                    RiskLevel.SUSPICIOUS -> "VERIFY FIRST"
                    RiskLevel.LOW -> "PROCEED"
                }

                Text(
                    text = actionBtnText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expanded) "Hide Signals" else "View Signals (${urlResult.signals.size})",
                        fontSize = 10.sp,
                        color = PrimaryShield
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = PrimaryShield,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Full URL: ${urlResult.normalizedUrl}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    urlResult.signals.forEachIndexed { index, signal ->
                        RiskSignalRow(index = index + 1, signal = signal)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(180.dp)
        )
    }
}

@Composable
private fun RiskSignalRow(index: Int, signal: RiskSignal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, DarkBorder, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = String.format(Locale.ROOT, "%02d", index),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = PrimaryShield
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "⚠ ${signal.title.uppercase()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = signal.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

private fun triggerHapticWarning(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        }
    } catch (_: Exception) {
    }
}
