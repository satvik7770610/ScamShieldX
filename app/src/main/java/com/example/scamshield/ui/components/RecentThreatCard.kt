package com.example.scamshield.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scamshield.model.ThreatEvent
import com.example.scamshield.ui.theme.DarkBorder
import com.example.scamshield.ui.theme.DarkSurfaceVariant
import com.example.scamshield.ui.theme.PrimaryShield
import com.example.scamshield.ui.theme.TextMuted
import com.example.scamshield.ui.theme.TextSecondary

@Composable
fun RecentThreatCard(
    event: ThreatEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        event.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.threatType.uppercase(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryShield
                )

                RiskBadge(
                    level = event.riskLevel,
                    score = event.riskScore
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.anonymizedCategory.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.summary,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = relativeTime,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
        }
    }
}