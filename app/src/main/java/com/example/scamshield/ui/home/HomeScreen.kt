package com.example.scamshield.ui.home

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.ThreatEvent
import com.example.scamshield.ui.components.ScamShieldSectionLabel
import com.example.scamshield.ui.components.SecurityDivider
import com.example.scamshield.ui.theme.DarkBorder
import com.example.scamshield.ui.theme.DarkSurface
import com.example.scamshield.ui.theme.DarkSurfaceVariant
import com.example.scamshield.ui.theme.PrimaryShield
import com.example.scamshield.ui.theme.RiskHigh
import com.example.scamshield.ui.theme.RiskLow
import com.example.scamshield.ui.theme.RiskSuspicious
import com.example.scamshield.ui.theme.TextMuted
import com.example.scamshield.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onNavigateToQrScanner: () -> Unit,
    onNavigateToCameraShield: () -> Unit,
    onNavigateToTextAnalyzer: () -> Unit,
    onNavigateToScreenshotShield: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToHistoryDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMlTestLab: () -> Unit,
    onNavigateToRiskResult: (String) -> Unit,
    onNavigateToShieldScan: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // BRAND LOGO HEADER
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "ScamShield Logo",
                    tint = PrimaryShield,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SCAMSHIELD X",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // HERO STATEMENT
            Text(
                text = "PROTECTION\nBEFORE YOU\nCLICK, SCAN\nOR PAY.",
                fontSize = 32.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 36.sp,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // STATUS INDICATOR LINE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(RiskLow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROTECTION ACTIVE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RiskLow,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "ON-DEVICE ANALYSIS",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            SecurityDivider(verticalPadding = 16.dp)

            // 01 PROACTIVE SHIELD
            ScamShieldSectionLabel(number = "01", title = "PROACTIVE SHIELD")
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = PrimaryShield,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "NOTIFICATION SHIELD",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (uiState.isNotificationShieldActive) "ACTIVE" else "INACTIVE",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isNotificationShieldActive) RiskLow else TextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isNotificationShieldActive) RiskLow else TextMuted)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Incoming notifications are checked on-device for suspicious links, payment requests, OTP requests and scam patterns.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MANAGE NOTIFICATION ACCESS →",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryShield,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            SecurityDivider(verticalPadding = 16.dp)

            // 02 PRIMARY ACTIONS (CHECK SOMETHING)
            ScamShieldSectionLabel(number = "02", title = "CHECK SOMETHING")
            Spacer(modifier = Modifier.height(10.dp))

            EditorialActionRow(
                number = "01",
                title = "SCAN QR",
                subtitle = "Check payment & web QR codes",
                icon = Icons.Default.QrCodeScanner,
                onClick = onNavigateToQrScanner
            )

            SecurityDivider(verticalPadding = 8.dp)

            EditorialActionRow(
                number = "02",
                title = "CHECK LINK",
                subtitle = "Analyze suspicious URLs & domains",
                icon = Icons.Default.Link,
                onClick = onNavigateToTextAnalyzer
            )

            SecurityDivider(verticalPadding = 8.dp)

            EditorialActionRow(
                number = "03",
                title = "ANALYZE TEXT",
                subtitle = "Detect scam language & requests",
                icon = Icons.AutoMirrored.Filled.Message,
                onClick = onNavigateToTextAnalyzer
            )

            SecurityDivider(verticalPadding = 8.dp)

            EditorialActionRow(
                number = "04",
                title = "SCREENSHOT",
                subtitle = "Extract and scan screenshot content",
                icon = Icons.Default.Image,
                onClick = onNavigateToScreenshotShield
            )

            SecurityDivider(verticalPadding = 8.dp)

            EditorialActionRow(
                number = "05",
                title = "CAMERA SHIELD",
                subtitle = "Real-time camera text OCR",
                icon = Icons.Default.CameraAlt,
                onClick = onNavigateToCameraShield
            )

            SecurityDivider(verticalPadding = 8.dp)

            EditorialActionRow(
                number = "06",
                title = "SHIELD SCAN",
                subtitle = "Select anything on screen to analyze",
                icon = Icons.Default.CropFree,
                onClick = onNavigateToShieldScan
            )

            SecurityDivider(verticalPadding = 16.dp)

            // 03 RECENT ACTIVITY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScamShieldSectionLabel(number = "03", title = "RECENT ACTIVITY")

                TextButton(onClick = onNavigateToHistory) {
                    Text(
                        text = "VIEW ALL →",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryShield
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.recentThreats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "NO THREATS DETECTED",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RiskLow,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.recentThreats.take(3).forEach { event ->
                        RecentActivityRow(
                            event = event,
                            onClick = {
                                val historyId = event.eventId.toLongOrNull()
                                if (historyId != null && historyId > 0L) {
                                    onNavigateToHistoryDetail(historyId)
                                } else {
                                    onNavigateToRiskResult(event.anonymizedCategory)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorialActionRow(
    number: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = number,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = PrimaryShield
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryShield,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Action",
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun RecentActivityRow(
    event: ThreatEvent,
    onClick: () -> Unit
) {
    val (levelLabel, levelColor) = when (event.riskLevel) {
        RiskLevel.HIGH_RISK -> "HIGH RISK" to RiskHigh
        RiskLevel.SUSPICIOUS -> "SUSPICIOUS" to RiskSuspicious
        RiskLevel.LOW -> "LOW RISK" to RiskLow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = levelLabel,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.anonymizedCategory.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "${event.riskScore} / 100",
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = levelColor
            )
        }
    }
}
