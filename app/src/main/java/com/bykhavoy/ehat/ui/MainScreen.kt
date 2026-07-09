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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.bykhavoy.ehat.ui.components.AmbientGlow
import com.bykhavoy.ehat.ui.components.CtaButton
import com.bykhavoy.ehat.ui.components.FactorStrip
import com.bykhavoy.ehat.ui.components.FreshnessStamp
import com.bykhavoy.ehat.ui.components.Histogram
import com.bykhavoy.ehat.ui.components.WeatherCard
import com.bykhavoy.ehat.ui.components.WindField
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.HeadlineStyle
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.LabelStyle
import com.bykhavoy.ehat.ui.theme.Stroke
import com.bykhavoy.ehat.ui.theme.Surface
import com.bykhavoy.ehat.ui.theme.statusColor
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun MainScreen(
    state: UiState,
    onOpenSettings: () -> Unit,
    onOpenDebug: () -> Unit,
    onRetry: () -> Unit,
    onCta: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Bg)) {
        // Wind field + glow only when we have content; otherwise a calm backdrop.
        val scene = state.dacha ?: state.aktau
        if (state.phase == UiState.Phase.CONTENT && scene != null) {
            WindField(
                speedMs = scene.gustMs.toFloat(),
                directionFromDeg = scene.windFromDeg,
                color = statusColor(state.sceneStatus),
                modifier = Modifier.fillMaxSize(),
            )
            AmbientGlow(statusColor(state.sceneStatus), Modifier.fillMaxSize())
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            TopBar(state, onOpenSettings, onOpenDebug)
            Spacer(Modifier.height(8.dp))

            when (state.phase) {
                UiState.Phase.LOADING -> Skeleton(Modifier.weight(1f))
                UiState.Phase.EMPTY -> EmptyState(state.emptyLabel, onRetry, Modifier.weight(1f))
                UiState.Phase.CONTENT -> Content(state, onCta, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TopBar(state: UiState, onOpenSettings: () -> Unit, onOpenDebug: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Long-press (2s) the title to open the hidden debug screen (spec §6, §13.10).
        Text(
            "ЕХАТЬ?",
            style = HeadlineStyle,
            color = Ink,
            modifier = Modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    val held = withTimeoutOrNull(2_000) { waitForUpOrCancellation() }
                    if (held == null) onOpenDebug() // stayed down for 2s
                }
            },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            state.freshness?.let { FreshnessStamp(it) }
            Spacer(Modifier.width(16.dp))
            Text("⚙", style = HeadlineStyle, color = InkDim, modifier = Modifier.clickable { onOpenSettings() })
        }
    }
}

@Composable
private fun Content(state: UiState, onCta: () -> Unit, modifier: Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().weight(1f)) {
            state.aktau?.let { WeatherCard(it, Modifier.weight(1f).fillMaxHeight()) }
            Box(Modifier.width(1.dp).fillMaxHeight().background(Stroke))
            state.dacha?.let { WeatherCard(it, Modifier.weight(1f).fillMaxHeight()) }
        }
        Spacer(Modifier.height(8.dp))
        FactorStrip(state.factors, state.bindingId, Modifier.fillMaxWidth().padding(vertical = 4.dp))
        Spacer(Modifier.height(8.dp))
        Histogram(state.histogram, Modifier.fillMaxWidth().height(56.dp))
        Spacer(Modifier.height(12.dp))
        state.cta?.let { CtaButton(it, onCta) }
    }
}

@Composable
private fun Skeleton(modifier: Modifier) {
    // Spec §13.9: skeleton, NOT a spinner.
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(2) {
            Box(
                Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Surface),
            )
        }
    }
}

@Composable
private fun EmptyState(label: String?, onRetry: () -> Unit, modifier: Modifier) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label ?: "Нет связи", style = HeadlineStyle, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text("Прогноз загрузится, когда появится интернет.", style = LabelStyle, color = InkDim)
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
                .clickable { onRetry() }
                .padding(horizontal = 32.dp, vertical = 16.dp),
        ) {
            Text("Повторить", style = LabelStyle, color = Ink)
        }
    }
}
