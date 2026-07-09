package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.data.net.OpenMeteoParser
import com.bykhavoy.ehat.domain.model.FactorId
import com.bykhavoy.ehat.domain.FactorEngine
import com.bykhavoy.ehat.domain.model.Location
import com.bykhavoy.ehat.domain.model.Thresholds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpenMeteoParserTest {

    private fun res(name: String): String =
        javaClass.getResourceAsStream("/$name")!!.bufferedReader().readText()

    @Test fun parses_two_point_array() {
        val list = OpenMeteoParser.parseForecast(res("forecast_aktau.json"))
        assertEquals(2, list.size)
        assertEquals(4, list[0].hourly!!.time.size)
        assertEquals(9.1, list[0].current!!.windGusts)
    }

    @Test fun parses_null_gust_inside_hourly() {
        val list = OpenMeteoParser.parseForecast(res("forecast_aktau.json"))
        // index 2 has a null gust in the fixture.
        assertNull(list[0].hourly!!.windGusts[2])
    }

    @Test fun parses_marine_all_null() {
        val list = OpenMeteoParser.parseMarine(res("marine_null.json"))
        assertEquals(2, list.size)
        assertTrue(list[0].hourly!!.waveHeight.all { it == null })
    }

    @Test fun merge_carries_pm10_and_leaves_null_gust_null() {
        val forecasts = OpenMeteoParser.parseForecast(res("forecast_aktau.json"))
        val air = OpenMeteoParser.parseAirQuality(res("air_quality.json"))
        val marine = OpenMeteoParser.parseMarine(res("marine_null.json"))

        val aktau = ForecastMapper.merge(Location("Актау", 43.6481, 51.1722), forecasts[0], air[0], marine[0])
        // Hour index 2 (14:00): pm10 = 160 from air-quality, gust stays null from forecast.
        val hour14 = aktau.hourly[2]
        assertEquals(160.0, hour14.pm10)
        assertNull(hour14.gustMs)
        assertNull(hour14.waveHeightM) // marine all-null

        // SEA must be irrelevant with no wave data — no heuristic (spec §14.4).
        val month = hour14.time.atZone(FactorEngine.AQTAU).month
        val sea = FactorEngine.buildFactors(hour14, month, Thresholds.DEFAULT).first { it.id == FactorId.SEA }
        assertFalse(sea.relevant)
    }
}
