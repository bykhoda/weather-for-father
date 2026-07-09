package com.bykhavoy.ehat.domain.model

/** A named point queried against Open-Meteo. Coordinates live in Constants.kt. */
data class Location(
    val name: String,
    val lat: Double,
    val lon: Double,
)
