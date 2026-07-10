package com.bykhavoy.ehat.domain.model

/** All data for one location, merged across endpoints. */
data class LocationForecast(
    val location: Location,
    val current: CurrentPoint,
    val hourly: List<HourlyPoint>,
)

/**
 * The full picture the UI renders: one entry per configured point, in the order
 * the user set them. DUST (air-quality) and SEA (marine) come from separate
 * endpoints and may be absent; that absence is expressed as nulls inside the
 * [HourlyPoint]s, not as a missing forecast.
 */
data class Forecast(
    val locations: List<LocationForecast>,
)
