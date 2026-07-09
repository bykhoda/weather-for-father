package com.bykhavoy.ehat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The one "wow" element (spec §12.4): a field of short streaks flowing WITH the
 * wind. Motion vector is directionFromDeg + 180 (meteo "from" -> "to"), matching
 * the arrow and §5.3 — get this wrong and the field flies into the wind, which
 * the user would notice in a day.
 *
 * Performance: one Canvas, one flat array, no per-frame allocation, a single
 * frame loop via withFrameNanos (NOT infiniteRepeatable per particle). Alpha is
 * capped at 0.2 so it never competes with the numbers.
 */
@Composable
fun WindField(
    speedMs: Float,
    directionFromDeg: Float,
    color: Color,
    modifier: Modifier = Modifier,
    particleCount: Int = 120,
) {
    val animColor by animateColorAsState(color, tween(900), label = "windFieldColor")
    val speed by rememberUpdatedState(speedMs)
    val dirDeg by rememberUpdatedState(directionFromDeg)

    val xs = remember { FloatArray(particleCount) { Random.nextFloat() } }
    val ys = remember { FloatArray(particleCount) { Random.nextFloat() } }
    val variance = remember { FloatArray(particleCount) { 0.6f + Random.nextFloat() * 0.8f } }

    val tick = remember { mutableLongStateOf(0L) }
    val lastNanos = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { now ->
                val prev = lastNanos.longValue
                val dt = if (prev == 0L) 0f else ((now - prev) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos.longValue = now

                val bearing = Math.toRadians((dirDeg + 180f).toDouble())
                val dx = sin(bearing).toFloat()
                val dy = -cos(bearing).toFloat()
                val v = (0.015f + speed * 0.012f) * dt
                for (i in xs.indices) {
                    xs[i] += dx * v * variance[i]
                    ys[i] += dy * v * variance[i]
                    if (xs[i] > 1.05f) xs[i] -= 1.1f
                    if (xs[i] < -0.05f) xs[i] += 1.1f
                    if (ys[i] > 1.05f) ys[i] -= 1.1f
                    if (ys[i] < -0.05f) ys[i] += 1.1f
                }
                tick.longValue = now
            }
        }
    }

    Canvas(modifier) {
        val frame = tick.longValue // read to subscribe: redraw each frame
        if (frame < 0L) return@Canvas
        val len = (8f + speed * 1.5f).coerceIn(8f, 34f)
        val alpha = (0.05f + speed * 0.01f).coerceAtMost(0.2f)
        val bearing = Math.toRadians((dirDeg + 180f).toDouble())
        val lx = sin(bearing).toFloat() * len
        val ly = -cos(bearing).toFloat() * len
        for (i in xs.indices) {
            val px = xs[i] * size.width
            val py = ys[i] * size.height
            drawLine(
                color = animColor.copy(alpha = alpha),
                start = Offset(px, py),
                end = Offset(px + lx, py + ly),
                strokeWidth = 2f,
            )
        }
    }
}
