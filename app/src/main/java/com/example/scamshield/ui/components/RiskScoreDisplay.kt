package com.example.scamshield.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.ui.theme.RiskHigh
import com.example.scamshield.ui.theme.RiskHighContainer
import com.example.scamshield.ui.theme.RiskLow
import com.example.scamshield.ui.theme.RiskLowContainer
import com.example.scamshield.ui.theme.RiskSuspicious
import com.example.scamshield.ui.theme.RiskSuspiciousContainer

@Composable
fun RiskScoreDisplay(
    score: Int,
    level: RiskLevel,
    modifier: Modifier = Modifier
) {
    val levelLabel = when {
        score >= 85 && level == RiskLevel.HIGH_RISK -> "CRITICAL RISK"
        level == RiskLevel.HIGH_RISK -> "HIGH RISK"
        level == RiskLevel.SUSPICIOUS -> "SUSPICIOUS"
        else -> "LOW RISK"
    }

    val (accentColor, containerBg) = when (level) {
        RiskLevel.HIGH_RISK -> Pair(RiskHigh, RiskHighContainer)
        RiskLevel.SUSPICIOUS -> Pair(RiskSuspicious, RiskSuspiciousContainer)
        RiskLevel.LOW -> Pair(RiskLow, RiskLowContainer)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerBg, RoundedCornerShape(4.dp))
            .border(1.5.dp, accentColor, RoundedCornerShape(4.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(3.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = score.toString(),
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                    Text(
                        text = "/ 100",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = levelLabel,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 2.sp
            )
        }
    }
}
