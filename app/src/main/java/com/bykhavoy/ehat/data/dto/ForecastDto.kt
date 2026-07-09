package com.bykhavoy.ehat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Open-Meteo /v1/forecast response for ONE point. When several coordinates are
 * requested (comma-separated), the API returns a JSON array of these — see
 * OpenMeteoClient. Hourly value lists are List<Double?>: Open-Meteo legitimately
 * returns null for individual hours (spec §13.8), and a non-nullable list would
 * throw SerializationException on the happy path.
 */
@Serializable
data class ForecastResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerialName("utc_offset_seconds") val utcOffsetSeconds: Int = 0,
    val current: CurrentDto? = null,
    val hourly: ForecastHourlyDto? = null,
)

@Serializable
data class CurrentDto(
    val time: String? = null,
    @SerialName("wind_speed_10m") val windSpeed: Double? = null,
    @SerialName("wind_gusts_10m") val windGusts: Double? = null,
    @SerialName("wind_direction_10m") val windDirection: Double? = null,
    @SerialName("temperature_2m") val temperature: Double? = null,
)

@Serializable
data class ForecastHourlyDto(
    val time: List<String> = emptyList(),
    @SerialName("wind_speed_10m") val windSpeed: List<Double?> = emptyList(),
    @SerialName("wind_gusts_10m") val windGusts: List<Double?> = emptyList(),
    @SerialName("wind_direction_10m") val windDirection: List<Double?> = emptyList(),
    @SerialName("apparent_temperature") val apparentTemp: List<Double?> = emptyList(),
    @SerialName("uv_index") val uvIndex: List<Double?> = emptyList(),
    @SerialName("precipitation_probability") val precipProb: List<Double?> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("is_day") val isDay: List<Int?> = emptyList(),
)
