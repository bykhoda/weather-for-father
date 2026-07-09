package com.bykhavoy.ehat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Open-Meteo marine-api response for one point (spec §14.4, SEA factor).
 * Global wave models cover the enclosed Caspian poorly, so wave_height may be
 * entirely null. That is expected — SEA is then simply dropped, with NO
 * wind-based heuristic substituted (spec §14.4).
 */
@Serializable
data class MarineResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hourly: MarineHourlyDto? = null,
)

@Serializable
data class MarineHourlyDto(
    val time: List<String> = emptyList(),
    @SerialName("wave_height") val waveHeight: List<Double?> = emptyList(),
    @SerialName("sea_surface_temperature") val seaTemp: List<Double?> = emptyList(),
)
