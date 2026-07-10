package com.bykhavoy.ehat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.domain.WindStatus
import com.bykhavoy.ehat.ui.components.FreshnessStamp
import com.bykhavoy.ehat.ui.components.LocationTabs
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Harsh
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.Windy
import kotlinx.coroutines.withTimeoutOrNull

private val SeaBlue = androidx.compose.ui.graphics.Color(0xFF2FA9BC)
private val Hairline = androidx.compose.ui.graphics.Color(0x12000000)

@Composable
fun HomeScreen(
    state: UiState,
    onSelectTab: (Int) -> Unit,
    onOpenDay: (Int) -> Unit,
    onRefresh: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val nowRow = state.days.firstOrNull { it.hasNow }?.rows?.firstOrNull { it.isNow }
    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Погода",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        if (withTimeoutOrNull(2_000) { waitForUpOrCancellation() } == null) onOpenDebug()
                    }
                },
            )
            Spacer(Modifier.width(20.dp))
            if (state.tabs.isNotEmpty()) LocationTabs(state.tabs, state.selectedTab, onSelectTab)
            Spacer(Modifier.weight(1f))
            state.freshness?.let { FreshnessStamp(it) }
            Spacer(Modifier.width(14.dp))
            Text("⟳", color = Calm, fontSize = 22.sp, modifier = Modifier.clickable { onRefresh() }.padding(4.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        if (state.refreshing && state.phase == UiState.Phase.CONTENT) {
            LinearProgressIndicator(
                Modifier.fillMaxWidth().height(2.dp),
                color = Calm,
                trackColor = androidx.compose.ui.graphics.Color.Transparent,
            )
        }

        when (state.phase) {
            UiState.Phase.LOADING -> SkeletonList()
            UiState.Phase.EMPTY -> EmptyState(state.emptyLabel, onRefresh)
            UiState.Phase.CONTENT -> Column(Modifier.fillMaxSize()) {
                if (nowRow != null) {
                    NowHeader(nowRow, state.showSea)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                }
                val allTemps = state.days.flatMap { d -> d.rows.mapNotNull { it.tempC } }
                val weekMin = allTemps.minOrNull()
                val weekMax = allTemps.maxOrNull()
                LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    itemsIndexed(state.days) { i, day ->
                        DayCard(day, state.hasSeaTemp, weekMin, weekMax) { onOpenDay(i) }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                    }
                }
            }
        }
    }
}

@Composable
private fun NowHeader(row: HourRow, showSea: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(row.sky, fontSize = 44.sp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(row.tempC?.let { "$it°C" } ?: "—", color = Ink, fontWeight = FontWeight.Bold, fontSize = 40.sp)
            Text("сейчас", color = Calm, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "ветер ${row.windMs ?: "—"} м/с · порывы ${row.gustMs ?: "—"} м/с",
                color = gustColor(row.windStatus), fontWeight = FontWeight.Medium, fontSize = 15.sp,
            )
            row.feelsC?.let { Text("ощущается $it°", color = InkDim, fontSize = 14.sp) }
            if (showSea && row.seaTempC != null) {
                Text("вода ${row.seaTempC}°", color = SeaBlue, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            if ((row.precipPct ?: 0) >= 30) {
                Text("💧 ${row.precipPct}%", color = InkDim, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun DayCard(day: DaySection, showSea: Boolean, weekMin: Int?, weekMax: Int?, onClick: () -> Unit) {
    val rows = day.rows
    val temps = rows.mapNotNull { it.tempC }
    val tMax = temps.maxOrNull()
    val tMin = temps.minOrNull()
    val mid = rows.getOrNull(rows.size / 2)
    val worst = rows.maxByOrNull { it.windStatus.ordinal }
    val gustMax = rows.mapNotNull { it.gustMs }.maxOrNull()
    val sea = mid?.seaTempC
    val precipMax = rows.mapNotNull { it.precipPct }.maxOrNull()

    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(day.title, color = if (day.hasNow) Calm else Ink, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            if (day.hasNow) {
                Text("сейчас${day.nowTempC?.let { " $it°C" } ?: ""}", color = Calm, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            } else if (gustMax != null) {
                Text("порывы $gustMax м/с", color = worst?.let { gustColor(it.windStatus) } ?: InkDim, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        }
        Text(mid?.sky ?: "·", fontSize = 24.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
        Box(Modifier.width(46.dp), contentAlignment = Alignment.Center) {
            if (precipMax != null && precipMax >= 30) Text("💧$precipMax%", color = SeaBlue, fontSize = 13.sp)
        }
        Text(tMin?.let { "$it°" } ?: "—", color = InkDim, fontSize = 15.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
        Spacer(Modifier.width(8.dp))
        TempRangeBar(tMin, tMax, weekMin, weekMax, Modifier.width(60.dp).height(6.dp))
        Spacer(Modifier.width(8.dp))
        Text(tMax?.let { "$it°" } ?: "—", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.width(30.dp))
        if (showSea) {
            Spacer(Modifier.width(8.dp))
            Text(sea?.let { "$it°" } ?: "—", color = SeaBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.width(38.dp), textAlign = TextAlign.Center)
        }
        Text("›", color = InkDim, fontSize = 22.sp, modifier = Modifier.width(18.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun TempRangeBar(tMin: Int?, tMax: Int?, weekMin: Int?, weekMax: Int?, modifier: Modifier) {
    if (tMin == null || tMax == null || weekMin == null || weekMax == null) {
        Box(modifier)
        return
    }
    val span = (weekMax - weekMin).coerceAtLeast(1)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val r = h / 2f
        drawRoundRect(Hairline, size = Size(w, h), cornerRadius = CornerRadius(r, r))
        val x0 = ((tMin - weekMin).toFloat() / span * w).coerceIn(0f, w)
        val x1 = ((tMax - weekMin).toFloat() / span * w).coerceIn(0f, w)
        val segW = (x1 - x0).coerceAtLeast(h)
        val left = x0.coerceAtMost(w - segW).coerceAtLeast(0f)
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(androidx.compose.ui.graphics.Color(0xFF4E93C9), androidx.compose.ui.graphics.Color(0xFFE0864B)),
            ),
            topLeft = Offset(left, 0f),
            size = Size(segW, h),
            cornerRadius = CornerRadius(r, r),
        )
    }
}

@Composable
private fun SkeletonList() {
    val bar = androidx.compose.ui.graphics.Color(0x11000000)
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(26.dp)) {
        repeat(8) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(130.dp).height(16.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).background(bar))
                Spacer(Modifier.weight(1f))
                Box(Modifier.width(52.dp).height(14.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).background(bar))
                Spacer(Modifier.width(16.dp))
                Box(Modifier.width(70.dp).height(14.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).background(bar))
                Spacer(Modifier.width(16.dp))
                Box(Modifier.width(40.dp).height(14.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).background(bar))
            }
        }
    }
}

@Composable
private fun Loader() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = Calm)
        Spacer(Modifier.height(14.dp))
        Text("Загрузка прогноза…", color = InkDim, fontSize = 15.sp)
    }
}

@Composable
private fun EmptyState(label: String?, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label ?: "Нет связи", color = Ink, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text("Прогноз загрузится, когда появится интернет.", color = InkDim, fontSize = 15.sp)
        Spacer(Modifier.height(20.dp))
        Text(
            "Повторить",
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier
                .clickable { onRetry() }
                .background(Calm, androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .padding(horizontal = 28.dp, vertical = 12.dp),
        )
    }
}

private fun gustColor(status: WindStatus) = when (status) {
    WindStatus.CALM -> Ink
    WindStatus.WINDY -> Windy
    WindStatus.HARSH -> Harsh
}
