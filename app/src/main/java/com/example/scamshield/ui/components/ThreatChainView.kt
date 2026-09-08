package com.example.scamshield.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scamshield.model.CategoryFormatter
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.ui.theme.DarkBorder
import com.example.scamshield.ui.theme.DarkSurfaceVariant
import com.example.scamshield.ui.theme.PrimaryShield
import com.example.scamshield.ui.theme.RiskHigh
import com.example.scamshield.ui.theme.RiskLow
import com.example.scamshield.ui.theme.RiskSuspicious
import com.example.scamshield.ui.theme.TextSecondary

@Composable
fun ThreatChainView(
    result: RiskAnalysisResult,
    modifier: Modifier = Modifier
) {
    val chainNodes = buildThreatChainNodes(result)

    val finalColor = when {
        result.score >= 85 && result.level == RiskLevel.HIGH_RISK -> RiskHigh
        result.level == RiskLevel.HIGH_RISK -> RiskHigh
        result.level == RiskLevel.SUSPICIOUS -> RiskSuspicious
        else -> RiskLow
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
            .padding(16.dp)
    ) {
        ScamShieldSectionLabel(number = "02", title = "THREAT CHAIN")
        Spacer(modifier = Modifier.height(14.dp))

        chainNodes.forEachIndexed { index, node ->
            val isLast = index == chainNodes.size - 1
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isLast) finalColor.copy(alpha = 0.15f) else DarkBorder.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (isLast) finalColor else DarkBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = node,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isLast) finalColor else TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (!isLast) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.South,
                        contentDescription = "Chain Flow",
                        tint = PrimaryShield,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun buildThreatChainNodes(result: RiskAnalysisResult): List<String> {
    val nodes = mutableListOf<String>()

    // Node 1: Input Type
    nodes.add("MESSAGE")

    // Node 2: Category if specific
    val formattedCat = CategoryFormatter.formatCategory(result.threatCategory, result.signals)
    if (formattedCat != "GENERAL" && formattedCat != "UNKNOWN") {
        nodes.add(formattedCat)
    }

    // Extracted signals
    val signalCodes = result.signals.map { it.code }.toSet()

    // 1. Account / Verification / OTP
    if (signalCodes.any { it == "TEXT_OTP_HARVEST" || it == "JOB_OTP_SCAM_COMBINATION" || it == "URL_EMBEDDED_CREDENTIALS" }) {
        nodes.add("OTP REQUEST")
    } else if (signalCodes.any { it == "TEXT_ACCOUNT_THREAT" } || formattedCat == "OTP / ACCOUNT SECURITY") {
        if (!nodes.contains("OTP REQUEST")) {
            nodes.add("ACCOUNT VERIFICATION")
        }
    }

    // 2. Payment request
    if (signalCodes.any {
        it == "RECRUITMENT_FEE_DEMAND" || it == "RECRUITMENT_PAY_FOR_OFFER" ||
        it == "TEXT_MONEY_TRANSFER_DEMAND" || it == "JOB_PAYMENT_SCAM_COMBINATION" ||
        it == "PAYMENT_SUSPICIOUS_HANDLE" || it == "PAYMENT_PRIZE_REWARD_CLAIM" ||
        it == "TEXT_REWARD_REFUND_SCAM"
    }) {
        nodes.add("PAYMENT REQUEST")
    }

    // 3. Suspicious Link
    if (signalCodes.any {
        it == "URL_UNVERIFIED_DESTINATION" || it == "URL_APPLICATION_LINK" ||
        it == "TEXT_CLICK_LINK_REQUEST" || it == "URL_SUSPICIOUS_PATH" ||
        it == "REDIRECT_CHAIN_DETECTED"
    } || result.threatCategory.contains("URL") || result.threatCategory.contains("Web Link")) {
        nodes.add("SUSPICIOUS LINK")
    }

    // 4. Look-alike Domain
    if (signalCodes.any {
        it == "URL_LOOKALIKE_BRAND" || it == "URL_BRAND_IMPERSONATION" ||
        it == "DOMAIN_IP_HOST"
    }) {
        nodes.add("LOOK-ALIKE DOMAIN")
    }

    // 5. Urgent action
    if (signalCodes.any { it == "TEXT_URGENT_PRESSURE" } && nodes.size < 5) {
        nodes.add("URGENT ACTION")
    }

    // Fallback top signal if nodes list is too short
    if (nodes.size == 1 && result.signals.isNotEmpty()) {
        nodes.add(result.signals.first().title.uppercase())
    }

    // Final Node: Risk Level
    val finalLabel = when {
        result.score >= 85 && result.level == RiskLevel.HIGH_RISK -> "CRITICAL"
        result.level == RiskLevel.HIGH_RISK -> "HIGH RISK"
        result.level == RiskLevel.SUSPICIOUS -> "SUSPICIOUS"
        else -> "LOW RISK"
    }
    nodes.add(finalLabel)

    // Deduplicate consecutive nodes
    val distinctNodes = mutableListOf<String>()
    nodes.forEach { node ->
        if (distinctNodes.isEmpty() || distinctNodes.last() != node) {
            distinctNodes.add(node)
        }
    }

    return distinctNodes
}
