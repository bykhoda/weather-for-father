package com.bykhavoy.ehat.ui

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.domain.WindStatus
import com.bykhavoy.ehat.ui.components.FreshnessStamp
import com.bykhavoy.ehat.ui.components.Segmented
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
            if (state.tabs.isNotEmpty()) Segmented(state.tabs, state.selectedTab, onSelectTab)
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
            UiState.Phase.LOADING -> Loader()
            UiState.Phase.EMPTY -> EmptyState(state.emptyLabel, onRefresh)
            UiState.Phase.CONTENT -> LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                itemsIndexed(state.days) { i, day ->
                    DayCard(day, state.hasSeaTemp) { onOpenDay(i) }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                }
            }
        }
    }
}

@Composable
private fun DayCard(day: DaySection, showSea: Boolean, onClick: () -> Unit) {
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
        Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 12.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(day.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.weight(2.2f))
        Text(mid?.sky ?: "·", fontSize = 24.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
        Text(
            "${tMax?.let { "$it°" } ?: "—"}  ${tMin?.let { "$it°" } ?: ""}",
            color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
            modifier = Modifier.weight(1.4f), textAlign = TextAlign.Center,
        )
        Text(
            gustMax?.let { "порывы $it" } ?: "—",
            color = worst?.let { gustColor(it.windStatus) } ?: InkDim,
            fontWeight = FontWeight.Medium, fontSize = 15.sp,
            modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center,
        )
        if (precipMax != null && precipMax >= 30) {
            Text("💧$precipMax%", color = InkDim, fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        } else {
            Spacer(Modifier.weight(1f))
        }
        if (showSea) {
            Text(sea?.let { "$it°" } ?: "—", color = SeaBlue, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
        }
        Text("›", color = InkDim, fontSize = 22.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
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
