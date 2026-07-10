package com.bykhavoy.ehat.ui

import com.bykhavoy.ehat.domain.WindStatus

/**
 * Table columns. `core` columns are always shown; the rest are user-toggled via
 * filter chips (persisted). SEA_TEMP/WAVE additionally require sea data present.
 */
enum class Col(val header: String, val widthDp: Int, val core: Boolean, val chip: String) {
    TIME("Время", 84, true, "время"),   // only column always shown (the axis)
    SKY("небо", 52, false, "небо"),
    TEMP("t, °C", 72, false, "температура"),
    WIND("ветер, м/с", 92, false, "ветер"),
    FEELS("ощущ, °C", 84, false, "ощущается"),
    HUMIDITY("влаж, %", 74, false, "влажность"),
    GUST("порывы, м/с", 104, false, "порывы"),
    DIR("напр", 96, false, "направление"),
    PRECIP("осадки, %", 92, false, "осадки"),
    SEA_TEMP("вода, °C", 80, false, "вода"),
    WAVE("волна, м", 80, false, "волна");

    val sea: Boolean get() = this == SEA_TEMP || this == WAVE
}

data class UiState(
    val phase: Phase = Phase.LOADING,
    val tabs: List<String> = emptyList(),
    val selectedTab: Int = 0,
    val stepHours: Int = 3,
    val enabled: Set<Col> = emptySet(),
    val rangeStartMs: Long? = null,
    val rangeEndMs: Long? = null,
    val days: List<DaySection> = emptyList(),
    val hasSeaTemp: Boolean = false,
    val hasWave: Boolean = false,
    val refreshing: Boolean = false,
    val freshness: Freshness? = null,
    val emptyLabel: String? = null,
) {
    enum class Phase { LOADING, CONTENT, EMPTY }

    val showSea: Boolean get() = hasSeaTemp || hasWave

    /** A sea column is available only when that specific data actually exists. */
    fun available(col: Col): Boolean = when (col) {
        Col.SEA_TEMP -> hasSeaTemp
        Col.WAVE -> hasWave
        else -> true
    }

    /** Ordered columns actually rendered right now. */
    val visibleColumns: List<Col>
        get() = Col.entries.filter { (it.core || it in enabled) && available(it) }
}

data class DaySection(
    val title: String,
    val rows: List<HourRow>,
    val hasNow: Boolean = false,
    val nowTempC: Int? = null,
)

data class HourRow(
    val time: String,
    val sky: String,
    val tempC: Int?,
    val feelsC: Int?,
    val humidityPct: Int?,
    val windMs: Int?,
    val gustMs: Int?,
    val windStatus: WindStatus,
    val windFromDeg: Float?,
    val compass: String,
    val precipPct: Int?,
    val seaTempC: Int?,
    val waveM: Double?,
    val isNow: Boolean,
)
