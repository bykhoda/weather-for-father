package com.bykhavoy.ehat.domain

import com.bykhavoy.ehat.domain.model.HourlyPoint
import java.time.Instant
import java.time.LocalDateTime

/** Shared builders for domain tests. Pure JVM — no Android, no Robolectric. */
object TestData {

    /** Instant at the given local Asia/Aqtau wall-clock time. */
    fun aqtau(y: Int, mo: Int, d: Int, h: Int, min: Int = 0): Instant =
        LocalDateTime.of(y, mo, d, h, min).atZone(FactorEngine.AQTAU).toInstant()

    fun hour(
        time: Instant,
        isDay: Boolean = true,
        wind: Double? = 3.0,
        gust: Double? = 5.0,
        windFrom: Double? = 315.0,
        apparent: Double? = 22.0,
        uv: Double? = 3.0,
        precip: Double? = 0.0,
        code: Int? = 0,
        pm10: Double? = 20.0,
        wave: Double? = 0.2,
        seaTemp: Double? = 24.0,
        humidity: Double? = 45.0,
        temp: Double? = 24.0,
    ): HourlyPoint = HourlyPoint(
        time = time,
        isDay = isDay,
        windSpeedMs = wind,
        gustMs = gust,
        windFromDeg = windFrom,
        apparentTempC = apparent,
        uvIndex = uv,
        precipProbPct = precip,
        weatherCode = code,
        pm10 = pm10,
        waveHeightM = wave,
        seaTempC = seaTemp,
        humidityPct = humidity,
        temperatureC = temp,
    )
}
