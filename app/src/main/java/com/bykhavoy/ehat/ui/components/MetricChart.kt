package com.bykhavoy.ehat.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.ui.HourRow
import com.bykhavoy.ehat.ui.Metric
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim

/**
 * Smooth area chart for one metric across a day (Apple-Weather style): current
 * value big at the top, a soft gradient area under a smooth line, min/max and a
 * few time labels. Missing points are skipped, not interpolated to zero.
 */
@Composable
fun MetricChart(
    rows: List<HourRow>,
    metric: Metric,
    modifier: Modifier = Modifier,
) {
    val pts = rows.mapIndexedNotNull { i, r -> metric.value(r)?.let { i to it } }

    Column(modifier) {
        val current = pts.lastOrNull()?.second
        Row(verticalAlignment = Alignment.Bottom) {
            Text(metric.label, color = InkDim, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(12.dp))
            Text(
                if (current != null) "${current.toInt()}${metric.unit}" else "—",
                color = metric.color,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(10.dp))

        Box(Modifier.fillMaxWidth().height(150.dp)) {
            if (pts.size < 2) {
                Text("нет данных", color = InkDim, fontSize = 14.sp, modifier = Modifier.align(Alignment.Center))
            } else {
                val minV = pts.minOf { it.second }
                val maxV = pts.maxOf { it.second }
                val range = (maxV - minV).coerceAtLeast(1f)
                val n = rows.size

                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val padY = 18f
                    fun px(i: Int) = if (n <= 1) 0f else w * (i.toFloat() / (n - 1))
                    fun py(v: Float) = h - padY - (v - minV) / range * (h - 2 * padY)

                    val line = Path()
                    val area = Path()
                    pts.forEachIndexed { idx, (i, v) ->
                        val x = px(i)
                        val y = py(v)
                        if (idx == 0) {
                            line.moveTo(x, y)
                            area.moveTo(x, h)
                            area.lineTo(x, y)
                        } else {
                            val (pi, pv) = pts[idx - 1]
                            val pxPrev = px(pi)
                            val pyPrev = py(pv)
                            val midX = (pxPrev + x) / 2
                            line.cubicTo(midX, pyPrev, midX, y, x, y)
                            area.cubicTo(midX, pyPrev, midX, y, x, y)
                        }
                    }
                    val lastX = px(pts.last().first)
                    area.lineTo(lastX, h)
                    area.close()

                    drawPath(
                        area,
                        Brush.verticalGradient(listOf(metric.color.copy(alpha = 0.28f), metric.color.copy(alpha = 0f))),
                    )
                    drawPath(line, metric.color, style = Stroke(width = 3.5f))

                    // endpoint dot
                    val lastV = pts.last().second
                    drawCircle(metric.color, radius = 5f, center = Offset(lastX, py(lastV)))
                }

                Text("${maxV.toInt()}", color = InkDim, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopStart))
                Text("${minV.toInt()}", color = InkDim, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomStart))
            }
        }

        if (rows.size >= 2) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(rows.first().time, color = InkDim, fontSize = 12.sp)
                Text(rows[rows.size / 2].time, color = InkDim, fontSize = 12.sp)
                Text(rows.last().time, color = InkDim, fontSize = 12.sp)
            }
        }
    }
}
