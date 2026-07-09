package com.bykhavoy.ehat.domain

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArrivalForecastTest {

    private val travel = Duration.ofMinutes(60)

    @Test fun picks_nearest_slot_not_next() {
        // now 13:31, +60m -> arrival 14:31. 15:00 (29m) is closer than 14:00 (31m).
        val now = TestData.aqtau(2025, 7, 1, 13, 31)
        val hourly = (12..17).map { h -> TestData.hour(TestData.aqtau(2025, 7, 1, h)) }
        val chosen = ArrivalForecast.forecastOnArrival(hourly, now, travel)
        assertEquals(TestData.aqtau(2025, 7, 1, 15), chosen!!.time)
    }

    @Test fun works_across_day_boundary() {
        val now = TestData.aqtau(2025, 7, 1, 23, 31)
        val hourly = listOf(23, 24 /*00 next day*/).map { h ->
            TestData.hour(TestData.aqtau(2025, 7, 1, 0).plus(Duration.ofHours(h.toLong())))
        }
        val chosen = ArrivalForecast.forecastOnArrival(hourly, now, travel)
        // arrival 00:31 next day -> nearest is 00:00 next day.
        assertEquals(TestData.aqtau(2025, 7, 2, 0), chosen!!.time)
    }

    @Test fun empty_list_does_not_crash() {
        val now = TestData.aqtau(2025, 7, 1, 13, 0)
        assertNull(ArrivalForecast.forecastOnArrival(emptyList(), now, travel))
    }

    @Test fun skips_slot_with_null_gust_at_arrival() {
        val now = TestData.aqtau(2025, 7, 1, 13, 0) // arrival 14:00
        val hourly = listOf(
            TestData.hour(TestData.aqtau(2025, 7, 1, 14), gust = null), // arrival hour, no data
            TestData.hour(TestData.aqtau(2025, 7, 1, 15), gust = 11.0),
        )
        val chosen = ArrivalForecast.forecastOnArrival(hourly, now, travel)
        assertEquals(TestData.aqtau(2025, 7, 1, 15), chosen!!.time)
    }
}
