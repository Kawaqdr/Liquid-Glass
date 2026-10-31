package com.mdrakibjs.liquidglass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.glassSurface(
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    tint: Color = Color(0x332B3C55)
): Modifier = this
    .clip(shape)
    .background(
        Brush.linearGradient(
            colorStops = arrayOf(
                0f to Color.White.copy(alpha = 0.19f),
                0.42f to tint.copy(alpha = 0.72f),
                1f to Color.White.copy(alpha = 0.045f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0f to Color.White.copy(alpha = 0.52f),
                0.48f to Color.White.copy(alpha = 0.15f),
                1f to Color.White.copy(alpha = 0.055f)
            )
        ),
        shape = shape
    )
