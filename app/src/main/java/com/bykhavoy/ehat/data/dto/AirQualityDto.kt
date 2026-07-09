package com.bykhavoy.ehat.data.dto

import kotlinx.serialization.Serializable

/** Open-Meteo air-quality-api response for one point (spec §14.4, DUST factor). */
@Serializable
data class AirQualityResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hourly: AirQualityHourlyDto? = null,
)

@Serializable
data class AirQualityHourlyDto(
    val time: List<String> = emptyList(),
    val pm10: List<Double?> = emptyList(),
    /** Desert dust concentration; not available for every region. Optional. */
    val dust: List<Double?> = emptyList(),
)
