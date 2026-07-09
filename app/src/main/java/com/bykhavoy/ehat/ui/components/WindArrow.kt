package com.bykhavoy.ehat.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import com.bykhavoy.ehat.domain.WindMath

/**
 * Wind direction arrow (spec §6, §12.6). Rotates to the wind's MOTION vector
 * (windFromDeg + 180), the same convention as the particle field, and always
 * turns along the shortest arc so 350°→10° is +20°, not a full spin.
 */
@Composable
fun WindArrow(
    windFromDeg: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val target = windFromDeg + 180f
    val angle = remember { Animatable(target) }
    LaunchedEffect(target) {
        val delta = WindMath.shortestDelta(angle.value.toDouble(), target.toDouble()).toFloat()
        angle.animateTo(
            targetValue = angle.value + delta,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 200f),
        )
    }
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val h = size.minDimension * 0.42f
        rotate(angle.value, pivot = Offset(cx, cy)) {
            // Shaft, pointing up (north at 0°).
            drawLine(
                color = color,
                start = Offset(cx, cy + h),
                end = Offset(cx, cy - h),
                strokeWidth = size.minDimension * 0.10f,
            )
            // Head.
            val head = Path().apply {
                moveTo(cx, cy - h * 1.15f)
                lineTo(cx - h * 0.5f, cy - h * 0.35f)
                lineTo(cx + h * 0.5f, cy - h * 0.35f)
                close()
            }
            drawPath(head, color)
        }
    }
}

/** Russian 8-point compass abbreviation for the direction the wind comes FROM. */
fun compassFrom(deg: Float): String {
    val points = listOf("С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ")
    val idx = (((deg % 360f) + 360f) % 360f / 45f).toInt() % 8
    return points[idx]
}
