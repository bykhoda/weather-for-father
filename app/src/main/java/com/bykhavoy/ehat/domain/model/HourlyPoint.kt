package com.bykhavoy.ehat.domain.model

import java.time.Instant

/**
 * One hourly slot, already merged from the three Open-Meteo endpoints
 * (forecast + air-quality + marine). Every measured field is nullable because
 * Open-Meteo legitimately returns null for individual hours the model did not
 * compute (spec §13.8). Downstream code treats null as "factor absent", never
 * as zero.
 */
data class HourlyPoint(
    val time: Instant,
    val isDay: Boolean,
    val windSpeedMs: Double?,
    val gustMs: Double?,
    /** Meteorological convention: direction the wind blows FROM. */
    val windFromDeg: Double?,
    val apparentTempC: Double?,
    val uvIndex: Double?,
    val precipProbPct: Double?,
    val weatherCode: Int?,
    val pm10: Double?,
    val waveHeightM: Double?,
    val seaTempC: Double?,
    val humidityPct: Double?,
    val temperatureC: Double?,
)

/** Snapshot of "right now" for a location (Open-Meteo `current` block). */
data class CurrentPoint(
    val time: Instant,
    val windSpeedMs: Double?,
    val gustMs: Double?,
    val windFromDeg: Double?,
    val temperatureC: Double?,
)
