package com.example.scamshield.ui.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.scamshield.notification.NotificationShieldDiagnostics
import com.example.scamshield.notification.ScamShieldNotificationListener
import com.example.scamshield.officekit.OfficeKitSyncManager
import com.example.scamshield.shieldscan.ShieldScanOverlayService
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMlTestLab: () -> Unit
) {
    val context = LocalContext.current
    val isNotifAccessGranted = remember {
        ScamShieldNotificationListener.isNotificationAccessGranted(context)
    }
    val diagData by NotificationShieldDiagnostics.data.collectAsState()
    val officeKitSyncManager = remember { OfficeKitSyncManager.getInstance(context) }
    val officeKitState by officeKitSyncManager.syncState.collectAsState()

    var testStatusText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 2.sp) },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 01 PROTECTION
            ScamShieldSectionLabel(number = "01", title = "PROTECTION")
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
                                text = if (isNotifAccessGranted) "ACTIVE" else "INACTIVE",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isNotifAccessGranted) RiskLow else TextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isNotifAccessGranted) RiskLow else TextMuted)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ScamShield X monitors incoming notifications on-device and warns you of high-risk phishing links, payment requests, or OTP harvesting.",
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
                            text = if (isNotifAccessGranted) "MANAGE ACCESS →" else "GRANT NOTIFICATION ACCESS →",
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

            // 02 APPLICATION
            ScamShieldSectionLabel(number = "02", title = "APPLICATION")
            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavItem(
                icon = Icons.Default.History,
                title = "THREAT HISTORY LOG",
                description = "View stored threat analysis records on local device storage",
                onClick = onNavigateToHistory
            )

            SecurityDivider(verticalPadding = 16.dp)

            // 03 SHIELD SCAN SETTINGS
            ScamShieldSectionLabel(number = "03", title = "SHIELD SCAN SETTINGS")
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "QUICK SETTINGS TILE",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryShield,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add 'Scan with ScamShield' tile to your Android Notification Shade to scan any screen with one tap.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    var isOverlayEnabled by remember {
                        mutableStateOf(Settings.canDrawOverlays(context))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FLOATING SHORTCUT OVERLAY",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOverlayEnabled) "Floating shield bubble active above other apps" else "Overlay permission required",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (Settings.canDrawOverlays(context)) {
                                    if (isOverlayEnabled) {
                                        ShieldScanOverlayService.stop(context)
                                        isOverlayEnabled = false
                                    } else {
                                        ShieldScanOverlayService.start(context)
                                        isOverlayEnabled = true
                                    }
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            border = BorderStroke(1.dp, PrimaryShield)
                        ) {
                            Text(
                                text = if (!Settings.canDrawOverlays(context)) "ENABLE OVERLAY" else if (isOverlayEnabled) "DISABLE" else "ENABLE",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = PrimaryShield
                            )
                        }
                    }
                }
            }

            SecurityDivider(verticalPadding = 16.dp)

            // 04 DEVELOPER & DIAGNOSTICS
            ScamShieldSectionLabel(number = "04", title = "DEVELOPER & DIAGNOSTICS")
            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavItem(
                icon = Icons.Default.Psychology,
                title = "ML TEST LAB",
                description = "Run raw on-device ML tensor predictions directly for model testing",
                onClick = onNavigateToMlTestLab
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Office Kit Console Sync Card with PING/PONG Connection Test
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
                        Text(
                            text = "PHONE CONNECTION",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryShield,
                            letterSpacing = 1.sp
                        )

                        val statusColor = when (officeKitState.connectionStatus) {
                            "CONNECTED" -> RiskLow
                            "CONNECTING" -> RiskSuspicious
                            "ERROR" -> RiskHigh
                            else -> TextMuted
                        }

                        Text(
                            text = "${officeKitState.connectionStatus} ${if (officeKitState.isConnected) "●" else "○"}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Connects Phone to Laptop Console on local Wi-Fi or emulator bridge (10.0.2.2).",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    var consoleIpText by remember { mutableStateOf(officeKitState.consoleIp) }

                    OutlinedTextField(
                        value = consoleIpText,
                        onValueChange = {
                            consoleIpText = it
                            officeKitSyncManager.updateConsoleIp(it)
                        },
                        label = { Text("LAPTOP IP ADDRESS (Your PC: 192.168.31.247)", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryShield,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            testStatusText = "Connecting..."
                            officeKitSyncManager.testConnection { success, msg ->
                                testStatusText = msg
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(2.dp),
                        border = BorderStroke(1.dp, PrimaryShield)
                    ) {
                        Text(
                            text = "[ TEST CONNECTION ] (PING)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryShield
                        )
                    }

                    if (testStatusText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = testStatusText ?: "",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (officeKitState.isConnected) RiskLow else RiskHigh
                        )
                    }

                    if (officeKitState.lastError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Last Error: ${officeKitState.lastError}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RiskHigh
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val lastSyncFormatted = rememberFormattedTime(officeKitState.lastSyncTime)

                    Text(
                        text = "Target: http://${officeKitState.consoleIp}:${officeKitState.consolePort} | Last Sync: $lastSyncFormatted | Total: ${officeKitState.totalSyncedCount}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notification Test Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "NOTIFICATION SHIELD TEST",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryShield,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LISTENER STATUS: ${if (diagData.isListenerConnected || isNotifAccessGranted) "CONNECTED" else "DISCONNECTED"}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (diagData.isListenerConnected || isNotifAccessGranted) RiskLow else RiskHigh
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                ScamShieldNotificationListener.sendTestNotification(
                                    context = context,
                                    title = "URGENT ACCOUNT VERIFICATION",
                                    body = "Your account will be suspended. Verify immediately: https://github.com.verification.invalid/login Send OTP to complete verification.",
                                    isHighRisk = true
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, PrimaryShield)
                        ) {
                            Text(
                                text = "TEST SCAM",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryShield
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                ScamShieldNotificationListener.sendTestNotification(
                                    context = context,
                                    title = "INTERNSHIP APPLICATION",
                                    body = "Your internship application has been received. You can check your application status on the official portal.",
                                    isHighRisk = false
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, DarkBorder)
                        ) {
                            Text(
                                text = "TEST LEGIT",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Received: ${diagData.totalReceivedCount} | Analyzed: ${diagData.totalAnalyzedCount} | Ignored: ${diagData.totalIgnoredCount}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }

            SecurityDivider(verticalPadding = 16.dp)

            // 04 ABOUT
            ScamShieldSectionLabel(number = "04", title = "ABOUT")
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryShield,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCAMSHIELD X v1.0",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"Protection before you click, scan or pay.\"\n\n\"Verification is not the same as trust.\"",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryShield,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun rememberFormattedTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Never"
    return try {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        "Recent"
    }
}
