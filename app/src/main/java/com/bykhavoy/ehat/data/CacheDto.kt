package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.domain.model.CurrentPoint
import com.bykhavoy.ehat.domain.model.Forecast
import com.bykhavoy.ehat.domain.model.HourlyPoint
import com.bykhavoy.ehat.domain.model.Location
import com.bykhavoy.ehat.domain.model.LocationForecast
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Serializable snapshot of the merged [Forecast] for the on-device cache
 * (spec §7). Instants are stored as epoch millis so no custom java.time
 * serializer is needed. This is the whole cached payload plus its fetch time.
 */
@Serializable
data class CachedForecast(
    val fetchedAtEpochMs: Long,
    val locations: List<CachedLocation> = emptyList(),
) {
    fun toDomain(): Pair<Forecast, Instant> =
        Forecast(locations.map { it.toDomain() }) to Instant.ofEpochMilli(fetchedAtEpochMs)

    companion object {
        fun from(forecast: Forecast, fetchedAt: Instant) = CachedForecast(
            fetchedAtEpochMs = fetchedAt.toEpochMilli(),
            locations = forecast.locations.map { CachedLocation.from(it) },
        )
    }
}

@Serializable
data class CachedLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val current: CachedCurrent,
    val hourly: List<CachedHour>,
) {
    fun toDomain() = LocationForecast(
        location = Location(name, lat, lon),
        current = current.toDomain(),
        hourly = hourly.map { it.toDomain() },
    )

    companion object {
        fun from(l: LocationForecast) = CachedLocation(
            name = l.location.name,
            lat = l.location.lat,
            lon = l.location.lon,
            current = CachedCurrent.from(l.current),
            hourly = l.hourly.map { CachedHour.from(it) },
        )
    }
}

@Serializable
data class CachedCurrent(
    val timeMs: Long,
    val wind: Double?,
    val gust: Double?,
    val dir: Double?,
    val temp: Double?,
) {
    fun toDomain() = CurrentPoint(Instant.ofEpochMilli(timeMs), wind, gust, dir, temp)

    companion object {
        fun from(c: CurrentPoint) = CachedCurrent(c.time.toEpochMilli(), c.windSpeedMs, c.gustMs, c.windFromDeg, c.temperatureC)
    }
}

@Serializable
data class CachedHour(
    val timeMs: Long,
    val isDay: Boolean,
    val wind: Double?,
    val gust: Double?,
    val dir: Double?,
    val apparent: Double?,
    val uv: Double?,
    val precip: Double?,
    val code: Int?,
    val pm10: Double?,
    val wave: Double?,
    val seaTemp: Double?,
    val humidity: Double?,
    val temp: Double?,
) {
    fun toDomain() = HourlyPoint(
        time = Instant.ofEpochMilli(timeMs),
        isDay = isDay,
        windSpeedMs = wind,
        gustMs = gust,
        windFromDeg = dir,
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

    companion object {
        fun from(h: HourlyPoint) = CachedHour(
            timeMs = h.time.toEpochMilli(),
            isDay = h.isDay,
            wind = h.windSpeedMs,
            gust = h.gustMs,
            dir = h.windFromDeg,
            apparent = h.apparentTempC,
            uv = h.uvIndex,
            precip = h.precipProbPct,
            code = h.weatherCode,
            pm10 = h.pm10,
            wave = h.waveHeightM,
            seaTemp = h.seaTempC,
            humidity = h.humidityPct,
            temp = h.temperatureC,
        )
    }
}
