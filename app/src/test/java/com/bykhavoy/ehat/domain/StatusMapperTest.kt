package com.bykhavoy.ehat.domain

import com.bykhavoy.ehat.domain.WindStatus.CALM
import com.bykhavoy.ehat.domain.WindStatus.HARSH
import com.bykhavoy.ehat.domain.WindStatus.WINDY
import com.bykhavoy.ehat.domain.model.Thresholds
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusMapperTest {

    private val t = Thresholds.DEFAULT // windy 8, harsh 14

    @Test fun wind_boundaries_off_by_one() {
        assertEquals(CALM, StatusMapper.windStatus(7.9, t))
        assertEquals(WINDY, StatusMapper.windStatus(8.0, t))
        assertEquals(WINDY, StatusMapper.windStatus(13.9, t))
        assertEquals(HARSH, StatusMapper.windStatus(14.0, t))
    }

    @Test fun custom_thresholds_apply() {
        val custom = Thresholds(windWindyMs = 5.0, windHarshMs = 10.0)
        assertEquals(CALM, StatusMapper.windStatus(4.9, custom))
        assertEquals(WINDY, StatusMapper.windStatus(5.0, custom))
        assertEquals(HARSH, StatusMapper.windStatus(10.0, custom))
    }

    @Test fun inverted_sliders_do_not_crash_and_stay_predictable() {
        // User dragged windy above harsh. harsh test runs first -> monotonic.
        val inverted = Thresholds(windWindyMs = 14.0, windHarshMs = 8.0)
        assertEquals(HARSH, StatusMapper.windStatus(10.0, inverted)) // >= 8
        assertEquals(CALM, StatusMapper.windStatus(6.0, inverted))
    }

    @Test fun heat_is_two_sided() {
        assertEquals(HARSH, StatusMapper.heatStatus(-2.0, t)) // too cold
        assertEquals(CALM, StatusMapper.heatStatus(20.0, t))  // comfortable
        assertEquals(HARSH, StatusMapper.heatStatus(38.0, t)) // too hot
    }

    @Test fun heat_boundaries() {
        assertEquals(CALM, StatusMapper.heatStatus(15.0, t))  // lower CALM edge
        assertEquals(WINDY, StatusMapper.heatStatus(5.0, t))  // cold WINDY edge
        assertEquals(HARSH, StatusMapper.heatStatus(4.9, t))  // cold HARSH
        assertEquals(WINDY, StatusMapper.heatStatus(30.0, t)) // warm WINDY edge
        assertEquals(HARSH, StatusMapper.heatStatus(36.0, t)) // warm HARSH edge
        assertEquals(CALM, StatusMapper.heatStatus(29.9, t))
    }

    @Test fun thunderstorm_forces_harsh_regardless_of_probability() {
        assertEquals(HARSH, StatusMapper.precipStatus(0.0, 96, t)) // hail thunderstorm, 0% prob
        assertEquals(CALM, StatusMapper.precipStatus(0.0, 0, t))
        assertEquals(WINDY, StatusMapper.precipStatus(40.0, 61, t))
    }
}
