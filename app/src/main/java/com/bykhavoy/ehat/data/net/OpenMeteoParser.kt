package com.bykhavoy.ehat.data.net

import com.bykhavoy.ehat.data.dto.AirQualityResponse
import com.bykhavoy.ehat.data.dto.ForecastResponse
import com.bykhavoy.ehat.data.dto.MarineResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

/**
 * Pure JSON parsing, split out from HTTP so it is unit-testable against real
 * captured responses in src/test/resources (spec §8). Open-Meteo returns a JSON
 * array when several coordinates are requested and a bare object for one; both
 * are handled.
 */
object OpenMeteoParser {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = false
    }

    fun parseForecast(body: String): List<ForecastResponse> = decodeList(body)
    fun parseAirQuality(body: String): List<AirQualityResponse> = decodeList(body)
    fun parseMarine(body: String): List<MarineResponse> = decodeList(body)

    private inline fun <reified T> decodeList(body: String): List<T> {
        val element = json.parseToJsonElement(body)
        return if (element is JsonArray) {
            json.decodeFromString<List<T>>(body)
        } else {
            listOf(json.decodeFromString<T>(body))
        }
    }
}
