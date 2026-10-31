package com.kawaqdr.liquidglass.ui

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
            listOf(
                Color.White.copy(alpha = 0.20f),
                tint,
                Color.White.copy(alpha = 0.055f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.48f), Color.White.copy(alpha = 0.08f))
        ),
        shape = shape
    )
