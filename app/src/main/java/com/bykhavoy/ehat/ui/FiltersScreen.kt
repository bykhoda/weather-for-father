package com.bykhavoy.ehat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.R
import com.bykhavoy.ehat.ui.components.Chip
import com.bykhavoy.ehat.ui.components.GhostButton
import com.bykhavoy.ehat.ui.components.IconAction
import com.bykhavoy.ehat.ui.components.PrimaryButton
import com.bykhavoy.ehat.ui.components.Segmented
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.Stroke
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltersScreen(
    initialStep: Int,
    initialEnabled: Set<Col>,
    initialStartMs: Long?,
    initialEndMs: Long?,
    hasSeaTemp: Boolean,
    hasWave: Boolean,
    onApply: (step: Int, columns: Set<Col>, startMs: Long?, endMs: Long?) -> Unit,
    onEditPlaces: () -> Unit,
    onClose: () -> Unit,
    initialOwmKey: String = "",
    onSaveOwmKey: (String) -> Unit = {},
) {
    var owmKey by remember { mutableStateOf(initialOwmKey) }
    val defaultStart = remember { LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
    val defaultEnd = remember { LocalDate.now().plusDays(13).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }

    // Forecast horizon is 14 days — grey out everything else so you can't land on empty dates.
    val selectable = remember(defaultStart, defaultEnd) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in defaultStart..defaultEnd
            override fun isSelectableYear(year: Int): Boolean {
                val today = LocalDate.now()
                return year in today.year..today.plusDays(13).year
            }
        }
    }

    val rangeState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMs ?: defaultStart,
        initialSelectedEndDateMillis = initialEndMs ?: defaultEnd,
        selectableDates = selectable,
    )
    var step by remember { mutableIntStateOf(initialStep) }
    var enabled by remember { mutableStateOf(initialEnabled) }

    val toggleable = Col.entries.filter {
        !it.core && (it != Col.SEA_TEMP || hasSeaTemp) && (it != Col.WAVE || hasWave)
    }

    Column(Modifier.fillMaxSize().background(Bg)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Фильтры", color = Ink, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(Modifier.weight(1f))
            IconAction(R.drawable.ic_close, onClick = onClose, tint = InkDim, size = 22.dp)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Stroke))

        Row(Modifier.weight(1f).fillMaxWidth()) {
            // Calendar range on the left
            Box(Modifier.weight(1.1f).fillMaxHeight()) {
                DateRangePicker(
                    state = rangeState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(Stroke))
            // Options on the right
            Column(
                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(20.dp),
            ) {
                Text("Места", color = InkDim, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "📍 Изменить места",
                    color = Calm, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Calm.copy(alpha = 0.12f))
                        .clickable { onEditPlaces() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )

                Spacer(Modifier.height(24.dp))
                Text("Шаг", color = InkDim, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Segmented(listOf("Каждый час", "Каждые 3 часа"), if (step == 1) 0 else 1) { step = if (it == 0) 1 else 3 }

                Spacer(Modifier.height(24.dp))
                Text("Колонки", color = InkDim, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    toggleable.forEach { col ->
                        Chip(col.chip, active = col in enabled) {
                            enabled = if (col in enabled) enabled - col else enabled + col
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Слои погоды на карте", color = InkDim, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    owmKey, { owmKey = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ключ OpenWeatherMap") },
                    placeholder = { Text("вставьте ключ сюда") },
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Для слоёв погоды. Бесплатно на openweathermap.org · «Радар» работает без ключа.",
                    color = InkDim, fontSize = 12.sp,
                )
            }
        }

        // Bottom action bar
        Box(Modifier.fillMaxWidth().height(1.dp).background(Stroke))
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GhostButton("Сбросить") {
                step = 3
                enabled = DEFAULT_ENABLED
                rangeState.setSelection(defaultStart, defaultEnd)
            }
            Spacer(Modifier.weight(1f))
            PrimaryButton("Применить") {
                val s = rangeState.selectedStartDateMillis
                val e = rangeState.selectedEndDateMillis
                onSaveOwmKey(owmKey)
                onApply(step, enabled, s, e)
                onClose()
            }
        }
    }
}

private val DEFAULT_ENABLED = setOf(
    Col.SKY, Col.TEMP, Col.WIND, Col.GUST, Col.DIR, Col.PRECIP, Col.HUMIDITY, Col.SEA_TEMP, Col.WAVE,
)
