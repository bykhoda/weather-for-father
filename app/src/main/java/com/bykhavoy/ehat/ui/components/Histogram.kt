package com.bykhavoy.ehat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.bykhavoy.ehat.ui.HistogramBar
import com.bykhavoy.ehat.ui.theme.statusColor

/**
 * 72-hour gust histogram, each bar coloured by that hour's VERDICT status
 * (spec §14.10) — the user sees "when it all comes together", not just "when the
 * wind drops". No axes or labels: read by shape, not numbers. Null hours are
 * gaps, never interpolated (spec §13.8). A gentle left-to-right reveal wave.
 */
@Composable
fun Histogram(
    bars: List<HistogramBar>,
    modifier: Modifier = Modifier,
) {
    if (bars.isEmpty()) return
    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { target = 1f }
    val reveal by animateFloatAsState(target, tween(900), label = "hist")
    val maxGust = (bars.maxOfOrNull { it.gustMs } ?: 1f).coerceAtLeast(1f)

    Canvas(modifier) {
        val n = bars.size
        val gap = 2f
        val barW = ((size.width - gap * (n - 1)) / n).coerceAtLeast(1f)
        bars.forEachIndexed { i, bar ->
            if (!bar.present) return@forEachIndexed
            // per-bar reveal so the wave sweeps left->right
            val local = ((reveal * n - i) / 6f).coerceIn(0f, 1f)
            if (local <= 0f) return@forEachIndexed
            val norm = (bar.gustMs / maxGust).coerceIn(0f, 1f)
            val h = size.height * norm * local
            val x = i * (barW + gap)
            drawRect(
                color = statusColor(bar.status),
                topLeft = Offset(x, size.height - h),
                size = Size(barW, h),
            )
        }
    }
}
