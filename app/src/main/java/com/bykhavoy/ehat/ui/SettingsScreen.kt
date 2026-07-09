package com.bykhavoy.ehat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bykhavoy.ehat.domain.model.Thresholds
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.HeadlineStyle
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.LabelStyle
import kotlin.math.roundToInt

/**
 * Settings (spec §6): the daily-tuned knobs — wind thresholds and travel time —
 * plus UV harsh. One screen, a Back tap, nothing else. He knows the wind here
 * better than any model does; let him set the line.
 */
@Composable
fun SettingsScreen(
    thresholds: Thresholds,
    onChange: (Thresholds) -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Bg).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("‹", style = HeadlineStyle, color = InkDim, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("Настройки", style = HeadlineStyle, color = Ink)
        }
        Spacer(Modifier.height(24.dp))

        SliderRow("Ветрено от, м/с", thresholds.windWindyMs, 3f, 15f) {
            onChange(thresholds.copy(windWindyMs = it.toDouble()))
        }
        SliderRow("Сиди дома от, м/с", thresholds.windHarshMs, 8f, 25f) {
            onChange(thresholds.copy(windHarshMs = it.toDouble()))
        }
        SliderRow("Опасный УФ от", thresholds.uvHarsh, 4f, 12f) {
            onChange(thresholds.copy(uvHarsh = it.toDouble()))
        }
        SliderRow("Время в пути, мин", thresholds.travelMinutes.toFloat(), 15f, 120f, step = 5f) {
            onChange(thresholds.copy(travelMinutes = it.roundToInt().toLong()))
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    step: Float = 1f,
    onValue: (Float) -> Unit,
) {
    val steps = (((max - min) / step).roundToInt() - 1).coerceAtLeast(0)
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, style = LabelStyle, color = Ink, modifier = Modifier.weight(1f))
            Text("${value.roundToInt()}", style = LabelStyle, color = Calm)
        }
        Slider(
            value = value.coerceIn(min, max),
            onValueChange = onValue,
            valueRange = min..max,
            steps = steps,
            colors = SliderDefaults.colors(thumbColor = Calm, activeTrackColor = Calm),
        )
    }
}
