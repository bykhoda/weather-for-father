package com.bykhavoy.ehat.domain.model

/** All data for one location, merged across endpoints. */
data class LocationForecast(
    val location: Location,
    val current: CurrentPoint,
    val hourly: List<HourlyPoint>,
)

/**
 * The full picture the UI renders: both points in a single object.
 * DUST (air-quality) and SEA (marine) come from separate endpoints and may be
 * absent; that absence is expressed as nulls inside the [HourlyPoint]s, not as
 * a missing forecast.
 */
data class Forecast(
    val aktau: LocationForecast,
    val dacha: LocationForecast,
)
