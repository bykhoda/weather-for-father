package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.data.dto.AirQualityResponse
import com.bykhavoy.ehat.data.dto.ForecastResponse
import com.bykhavoy.ehat.data.dto.MarineResponse
import com.bykhavoy.ehat.domain.FactorEngine
import com.bykhavoy.ehat.domain.model.CurrentPoint
import com.bykhavoy.ehat.domain.model.HourlyPoint
import com.bykhavoy.ehat.domain.model.Location
import com.bykhavoy.ehat.domain.model.LocationForecast
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Merges the three endpoint responses (forecast + air-quality + marine) for one
 * location into the domain [LocationForecast]. Air-quality and marine are
 * optional: if either failed, its columns are simply absent (null), which the
 * factor engine reads as "factor not shown" (spec §14.4).
 */
object ForecastMapper {

    private val zone: ZoneId = FactorEngine.AQTAU

    /** Open-Meteo emits local wall-clock times without an offset, e.g. "2025-07-01T14:00". */
    fun parseTime(s: String): Instant =
        LocalDateTime.parse(s).atZone(zone).toInstant()

    private fun indexByTime(times: List<String>): Map<Instant, Int> =
        times.withIndex().associate { (i, s) -> parseTime(s) to i }

    fun merge(
        location: Location,
        forecast: ForecastResponse,
        air: AirQualityResponse?,
        marine: MarineResponse?,
    ): LocationForecast {
        val fh = forecast.hourly ?: error("forecast.hourly missing") // caller guards via Malformed
        val airH = air?.hourly
        val marineH = marine?.hourly
        val airIdx = airH?.let { indexByTime(it.time) } ?: emptyMap()
        val marineIdx = marineH?.let { indexByTime(it.time) } ?: emptyMap()

        val hourly = fh.time.indices.map { i ->
            val t = parseTime(fh.time[i])
            val ai = airIdx[t]
            val mi = marineIdx[t]
            HourlyPoint(
                time = t,
                isDay = (fh.isDay.getOrNull(i) ?: 1) == 1,
                windSpeedMs = fh.windSpeed.getOrNull(i),
                gustMs = fh.windGusts.getOrNull(i),
                windFromDeg = fh.windDirection.getOrNull(i),
                apparentTempC = fh.apparentTemp.getOrNull(i),
                uvIndex = fh.uvIndex.getOrNull(i),
                precipProbPct = fh.precipProb.getOrNull(i),
                weatherCode = fh.weatherCode.getOrNull(i),
                pm10 = ai?.let { idx -> airH?.pm10?.getOrNull(idx) ?: airH?.dust?.getOrNull(idx) },
                waveHeightM = mi?.let { idx -> marineH?.waveHeight?.getOrNull(idx) },
                seaTempC = mi?.let { idx -> marineH?.seaTemp?.getOrNull(idx) },
                humidityPct = fh.humidity.getOrNull(i),
                temperatureC = fh.temperature.getOrNull(i),
            )
        }

        val c = forecast.current
        val current = CurrentPoint(
            time = c?.time?.let { parseTime(it) } ?: (hourly.firstOrNull()?.time ?: Instant.EPOCH),
            windSpeedMs = c?.windSpeed,
            gustMs = c?.windGusts,
            windFromDeg = c?.windDirection,
            temperatureC = c?.temperature,
        )
        return LocationForecast(location, current, hourly)
    }
}
