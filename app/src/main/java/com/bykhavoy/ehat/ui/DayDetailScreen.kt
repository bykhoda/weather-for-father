package com.bykhavoy.ehat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.ui.components.Chip
import com.bykhavoy.ehat.ui.components.ForecastTable
import com.bykhavoy.ehat.ui.components.MetricChart
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim

private val Hairline = androidx.compose.ui.graphics.Color(0x12000000)

@Composable
fun DayDetailScreen(
    day: DaySection,
    locationName: String,
    hasSeaTemp: Boolean,
    hasWave: Boolean,
    enabled: Set<Col>,
    onOpenFilters: () -> Unit,
    onBack: () -> Unit,
) {
    var metric by remember { mutableStateOf(Metric.TEMP) }
    fun available(c: Col) = when (c) {
        Col.SEA_TEMP -> hasSeaTemp
        Col.WAVE -> hasWave
        else -> true
    }
    val metrics = Metric.entries.filter { !it.sea || hasSeaTemp }
    val columns = Col.entries.filter { (it.core || it in enabled) && available(it) }

    Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", color = Calm, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(day.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(locationName, color = InkDim, fontSize = 14.sp)
            }
            Text("Фильтры", color = Calm, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.clickable { onOpenFilters() }.padding(6.dp))
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { m -> Chip(m.label, active = m == metric) { metric = m } }
            }
            Spacer(Modifier.height(16.dp))
            MetricChart(day.rows, metric, Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Spacer(Modifier.height(8.dp))

        ForecastTable(day.rows, columns, Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(24.dp))
    }
}
