package com.bykhavoy.ehat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.domain.WindStatus
import com.bykhavoy.ehat.ui.Col
import com.bykhavoy.ehat.ui.HourRow
import com.bykhavoy.ehat.ui.WeatherFormat
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Harsh
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.Windy

private val Hairline = Color(0x12000000)
private val SeaBlue = Color(0xFF2FA9BC)

/**
 * Hourly table for a single day. Columns have fixed widths and the whole table
 * scrolls horizontally, so one selected column reads as a tidy compact list
 * (left-aligned) instead of two values stretched across the screen.
 */
@Composable
fun ForecastTable(
    rows: List<HourRow>,
    columns: List<Col>,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val totalWidth = columns.sumOf { it.widthDp }.dp
    Column(modifier.horizontalScroll(scroll)) {
        // header
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            columns.forEach { col ->
                Text(
                    col.header,
                    modifier = Modifier.width(col.widthDp.dp),
                    color = InkDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = if (col == Col.TIME) TextAlign.Start else TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
        rows.forEachIndexed { i, row ->
            Row(
                Modifier
                    .background(if (row.isNow) Calm.copy(alpha = 0.10f) else Color.Transparent)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                columns.forEach { col -> CellFor(col, row) }
            }
            if (i != rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        }
    }
}

@Composable
private fun CellFor(col: Col, row: HourRow) {
    val w = col.widthDp.dp
    when (col) {
        Col.TIME -> Cell(row.time, w, TextAlign.Start, if (row.isNow) Calm else InkDim, bold = row.isNow)
        Col.SKY -> Cell(row.sky, w, color = Ink, size = 18.sp)
        Col.TEMP -> Cell(num(row.tempC, "°"), w, color = WeatherFormat.tempColor(row.tempC), bold = true)
        Col.FEELS -> Cell(num(row.feelsC, "°"), w, color = InkDim)
        Col.HUMIDITY -> Cell(num(row.humidityPct, "%"), w, color = InkDim)
        Col.WIND -> Cell(num(row.windMs, ""), w, color = Ink)
        Col.GUST -> Cell(num(row.gustMs, ""), w, color = gustColor(row.windStatus), bold = row.windStatus != WindStatus.CALM)
        Col.DIR -> DirCell(row, w)
        Col.PRECIP -> Cell(num(row.precipPct, "%"), w, color = if ((row.precipPct ?: 0) >= 50) Ink else InkDim)
        Col.SEA_TEMP -> Cell(num(row.seaTempC, "°"), w, color = SeaBlue, bold = true)
        Col.WAVE -> Cell(row.waveM?.let { "$it" } ?: "—", w, color = InkDim)
    }
}

@Composable
private fun DirCell(row: HourRow, w: androidx.compose.ui.unit.Dp) {
    Row(Modifier.width(w), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        val deg = row.windFromDeg
        if (deg != null) {
            Text("↑", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.rotate(deg + 180f))
            Text(" ${row.compass}", color = InkDim, fontSize = 13.sp)
        } else {
            Text("—", color = InkDim, fontSize = 15.sp)
        }
    }
}

@Composable
private fun Cell(
    text: String,
    w: androidx.compose.ui.unit.Dp,
    align: TextAlign = TextAlign.Center,
    color: Color = Ink,
    bold: Boolean = false,
    size: androidx.compose.ui.unit.TextUnit = 16.sp,
) {
    Text(
        text = text,
        modifier = Modifier.width(w),
        color = color,
        fontSize = size,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = align,
        maxLines = 1,
    )
}

private fun gustColor(status: WindStatus): Color = when (status) {
    WindStatus.CALM -> Ink
    WindStatus.WINDY -> Windy
    WindStatus.HARSH -> Harsh
}

private fun num(v: Int?, suffix: String): String = if (v == null) "—" else "$v$suffix"
