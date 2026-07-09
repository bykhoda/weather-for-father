package com.bykhavoy.ehat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Ambient glow rising from the bottom edge (spec §12.5). The user reads a status
 * change with peripheral vision — "the screen went red" — before reading the
 * number. Colour crossfades over 900 ms; it should never be consciously noticed.
 */
@Composable
fun AmbientGlow(
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(statusColor, tween(900), label = "glow")
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(w / 2f, h * 1.15f),
                radius = h * 0.9f,
            ),
        )
    }
}
