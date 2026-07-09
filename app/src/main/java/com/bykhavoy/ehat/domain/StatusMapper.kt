package com.bykhavoy.ehat.domain

import com.bykhavoy.ehat.domain.WindStatus.CALM
import com.bykhavoy.ehat.domain.WindStatus.HARSH
import com.bykhavoy.ehat.domain.WindStatus.WINDY
import com.bykhavoy.ehat.domain.model.Thresholds

/**
 * Maps raw numbers to [WindStatus] (spec §5.1, §14.6).
 *
 * Threshold semantics are ">= boundary" (inclusive lower edge), which is the
 * classic off-by-one trap. Tested explicitly: 7.9 -> CALM, 8.0 -> WINDY,
 * 13.9 -> WINDY, 14.0 -> HARSH.
 */
object StatusMapper {

    /**
     * Ascending one-sided scale (higher is worse): WIND, UV, PRECIP%, DUST, SEA.
     * Robust to a user who sets windy > harsh via the sliders (spec §5.1): the
     * harsh test runs first, so behaviour stays monotonic and never crashes.
     */
    fun ascending(value: Double, windy: Double, harsh: Double): WindStatus = when {
        value >= harsh -> HARSH
        value >= windy -> WINDY
        else -> CALM
    }

    /** Wind by gusts, not mean speed (spec §5.1). */
    fun windStatus(gustMs: Double, t: Thresholds): WindStatus =
        ascending(gustMs, t.windWindyMs, t.windHarshMs)

    /**
     * HEAT is two-sided (spec §14.6): both extremes are bad. This is the one
     * factor where a naive `>=` mapping produces a silent bug, so it is written
     * out explicitly.
     *
     * Defaults: CALM 15..30, WINDY [5,15) or [30,36), HARSH <5 or >=36.
     */
    fun heatStatus(apparentC: Double, t: Thresholds): WindStatus = when {
        apparentC >= t.heatWarmHarshC || apparentC < t.heatColdHarshC -> HARSH
        apparentC >= t.heatWarmWindyC || apparentC < t.heatColdWindyC -> WINDY
        else -> CALM
    }

    fun uvStatus(uv: Double, t: Thresholds): WindStatus =
        ascending(uv, t.uvWindy, t.uvHarsh)

    fun dustStatus(pm10: Double, t: Thresholds): WindStatus =
        ascending(pm10, t.dustWindyPm10, t.dustHarshPm10)

    fun seaStatus(waveM: Double, t: Thresholds): WindStatus =
        ascending(waveM, t.seaWindyM, t.seaHarshM)

    /** WMO thunderstorm codes 95–99: instant HARSH regardless of probability (spec §14.6). */
    fun isThunderstorm(weatherCode: Int?): Boolean = weatherCode != null && weatherCode in 95..99

    fun precipStatus(probPct: Double, weatherCode: Int?, t: Thresholds): WindStatus =
        if (isThunderstorm(weatherCode)) HARSH
        else ascending(probPct, t.precipWindyPct, t.precipHarshPct)
}
