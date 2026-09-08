package com.example.scamshield.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.ui.theme.OnRiskHighContainer
import com.example.scamshield.ui.theme.OnRiskLowContainer
import com.example.scamshield.ui.theme.OnRiskSuspiciousContainer
import com.example.scamshield.ui.theme.RiskHigh
import com.example.scamshield.ui.theme.RiskHighContainer
import com.example.scamshield.ui.theme.RiskLow
import com.example.scamshield.ui.theme.RiskLowContainer
import com.example.scamshield.ui.theme.RiskSuspicious
import com.example.scamshield.ui.theme.RiskSuspiciousContainer

@Composable
fun RiskBadge(
    level: RiskLevel,
    score: Int? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon, label) = when (level) {
        RiskLevel.LOW -> Quadruple(
            RiskLowContainer,
            OnRiskLowContainer,
            Icons.Default.CheckCircle,
            "LOW RISK"
        )
        RiskLevel.SUSPICIOUS -> Quadruple(
            RiskSuspiciousContainer,
            OnRiskSuspiciousContainer,
            Icons.Default.Info,
            "SUSPICIOUS"
        )
        RiskLevel.HIGH_RISK -> Quadruple(
            RiskHighContainer,
            OnRiskHighContainer,
            Icons.Default.Warning,
            "HIGH RISK"
        )
    }

    val iconTint = when (level) {
        RiskLevel.LOW -> RiskLow
        RiskLevel.SUSPICIOUS -> RiskSuspicious
        RiskLevel.HIGH_RISK -> RiskHigh
    }

    val textLabel = if (score != null) "$label ($score)" else label

    Row(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = textLabel,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)