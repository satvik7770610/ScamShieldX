package com.example.scamshield.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val ThinDividerColor = Color(0xFF1E293B)

@Composable
fun SecurityDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    verticalPadding: Dp = 16.dp,
    color: Color = ThinDividerColor
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding)
            .height(thickness)
            .background(color)
    )
}