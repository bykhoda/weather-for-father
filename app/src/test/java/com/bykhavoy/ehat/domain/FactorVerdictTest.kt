package com.bykhavoy.ehat.domain

import com.bykhavoy.ehat.domain.WindStatus.CALM
import com.bykhavoy.ehat.domain.WindStatus.HARSH
import com.bykhavoy.ehat.domain.model.Factor
import com.bykhavoy.ehat.domain.model.FactorId
import com.bykhavoy.ehat.domain.model.HourlyPoint
import com.bykhavoy.ehat.domain.model.Thresholds
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FactorVerdictTest {

    private val t = Thresholds.DEFAULT

    private fun factors(h: HourlyPoint): List<Factor> {
        val month = h.time.atZone(FactorEngine.AQTAU).month
        return FactorEngine.buildFactors(h, month, t)
    }

    private fun factor(h: HourlyPoint, id: FactorId): Factor =
        factors(h).first { it.id == id }

    private val julyNoon = TestData.aqtau(2025, 7, 1, 12)
    private val janNoon = TestData.aqtau(2025, 1, 15, 12)

    @Test fun all_calm_gives_null_binding_and_yes() {
        val v = FactorEngine.verdictOf(factors(TestData.hour(julyNoon)))
        assertEquals(CALM, v.status)
        assertNull(v.binding)
        assertEquals("ДА", v.headline)
    }

    @Test fun two_harsh_binding_is_deterministic_by_priority() {
        // WIND harsh (gust 16) and DUST harsh (pm10 200). DUST outranks WIND.
        val h = TestData.hour(julyNoon, gust = 16.0, pm10 = 200.0)
        val v1 = FactorEngine.verdictOf(factors(h))
        val v2 = FactorEngine.verdictOf(factors(h))
        assertEquals(FactorId.DUST, v1.binding!!.id)
        assertEquals(v1.binding!!.id, v2.binding!!.id) // repeatable
    }

    @Test fun heat_two_sided_through_factor() {
        assertEquals(HARSH, FactorEngine.verdictOf(factors(TestData.hour(julyNoon, apparent = -2.0))).status)
        assertEquals(CALM, FactorEngine.verdictOf(factors(TestData.hour(julyNoon, apparent = 20.0))).status)
        assertEquals(HARSH, FactorEngine.verdictOf(factors(TestData.hour(julyNoon, apparent = 38.0))).status)
    }

    @Test fun uv_at_night_is_irrelevant_even_at_extreme_value() {
        val h = TestData.hour(julyNoon, isDay = false, uv = 11.0)
        val uv = factor(h, FactorId.UV)
        assertFalse(uv.relevant)
        assertEquals(CALM, FactorEngine.verdictOf(factors(h)).status)
    }

    @Test fun sea_in_january_is_irrelevant() {
        val h = TestData.hour(janNoon, wave = 2.0) // would be HARSH in summer
        assertFalse(factor(h, FactorId.SEA).relevant)
    }

    @Test fun thunderstorm_makes_precip_harsh_even_at_zero_probability() {
        val h = TestData.hour(julyNoon, precip = 0.0, code = 96)
        val v = FactorEngine.verdictOf(factors(h))
        assertEquals(HARSH, v.status)
        assertEquals(FactorId.PRECIP, v.binding!!.id)
    }

    @Test fun air_quality_unavailable_keeps_screen_alive_on_five_factors() {
        val h = TestData.hour(julyNoon, pm10 = null) // air-quality endpoint failed
        assertFalse(factor(h, FactorId.DUST).relevant)
        val relevantCount = factors(h).count { it.relevant }
        assertTrue(relevantCount >= 5) // wind, heat, uv, precip, sea
        assertEquals(CALM, FactorEngine.verdictOf(factors(h)).status)
    }

    @Test fun marine_null_disables_sea_without_heuristic() {
        val h = TestData.hour(julyNoon, wave = null)
        val sea = factor(h, FactorId.SEA)
        assertFalse(sea.relevant)
        assertNull(sea.value) // no invented value
    }

    @Test fun calm_wind_but_harsh_uv_paints_scene_harsh_wind_keeps_own_status() {
        val h = TestData.hour(julyNoon, gust = 4.0, uv = 10.0)
        val v = FactorEngine.verdictOf(factors(h))
        assertEquals(HARSH, v.status)
        assertEquals(FactorId.UV, v.binding!!.id)
        assertEquals(CALM, factor(h, FactorId.WIND).status) // wind number stays green
    }
}
