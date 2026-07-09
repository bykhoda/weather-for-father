package com.bykhavoy.ehat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bykhavoy.ehat.domain.WindStatus
import com.bykhavoy.ehat.ui.theme.EhatTheme

private const val W = 1280
private const val H = 720

private val defaultEnabled = setOf(
    Col.SKY, Col.TEMP, Col.WIND, Col.GUST, Col.DIR, Col.PRECIP, Col.HUMIDITY, Col.SEA_TEMP, Col.WAVE,
)

private fun sampleRows(sea: Boolean): List<HourRow> = (0..7).map { i ->
    val h = i * 3
    val gust = 6 + (i % 5) * 3
    HourRow(
        time = "%02d:00".format(h),
        sky = listOf("☀", "⛅", "☁", "🌧", "⛈")[i % 5],
        tempC = 22 + (i % 6),
        feelsC = 24 + (i % 6),
        humidityPct = 40 + i * 3,
        windMs = 4 + (i % 4),
        gustMs = gust,
        windStatus = when {
            gust >= 14 -> WindStatus.HARSH
            gust >= 8 -> WindStatus.WINDY
            else -> WindStatus.CALM
        },
        windFromDeg = (i * 40f) % 360f,
        compass = listOf("С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ")[i % 8],
        precipPct = (i * 12) % 100,
        seaTempC = if (sea) 24 + (i % 3) else null,
        waveM = if (sea) (0.2 + i * 0.1) else null,
        isNow = i == 2,
    )
}

private fun sampleDays(sea: Boolean) = listOf(
    DaySection("Четверг, 10 июля", sampleRows(sea)),
    DaySection("Пятница, 11 июля", sampleRows(sea)),
    DaySection("Суббота, 12 июля", sampleRows(sea)),
)

private fun sampleState(sea: Boolean) = UiState(
    phase = UiState.Phase.CONTENT,
    tabs = listOf("Грин Парк", "Ивушка"),
    selectedTab = if (sea) 1 else 0,
    stepHours = 3,
    enabled = defaultEnabled,
    days = sampleDays(sea),
    hasSeaTemp = sea,
    hasWave = sea,
    freshness = Freshness("обновлено 4 минуты назад", FreshLevel.FRESH),
)

@Preview(name = "Список дней", widthDp = W, heightDp = H, showBackground = true)
@Composable
private fun PreviewHome() = EhatTheme {
    HomeScreen(sampleState(true), {}, {}, {}, {}, {}, {})
}

@Preview(name = "Детали дня", widthDp = W, heightDp = H, showBackground = true)
@Composable
private fun PreviewDetail() = EhatTheme {
    DayDetailScreen(
        day = sampleDays(true).first(),
        locationName = "Ивушка",
        hasSeaTemp = true,
        hasWave = true,
        enabled = defaultEnabled,
        onOpenFilters = {},
        onBack = {},
    )
}
