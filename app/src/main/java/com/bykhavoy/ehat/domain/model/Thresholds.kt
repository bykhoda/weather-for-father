package com.bykhavoy.ehat.domain.model

import java.time.Duration

/**
 * User-tunable thresholds (spec §14.6). Defaults below. HEAT is the only
 * two-sided factor — both too hot and too cold are bad — so it carries four
 * numbers. Everything else is a single ascending scale.
 *
 * The settings screen (spec §6) exposes the wind thresholds and travel time
 * prominently; the rest ship as sensible defaults and remain here so a future
 * settings revision can surface them without a model change.
 */
data class Thresholds(
    val windWindyMs: Double = 8.0,
    val windHarshMs: Double = 14.0,

    val heatWarmWindyC: Double = 30.0,
    val heatWarmHarshC: Double = 36.0,
    val heatColdWindyC: Double = 15.0,
    val heatColdHarshC: Double = 5.0,

    val uvWindy: Double = 6.0,
    val uvHarsh: Double = 8.0,

    val precipWindyPct: Double = 30.0,
    val precipHarshPct: Double = 60.0,

    val dustWindyPm10: Double = 50.0,
    val dustHarshPm10: Double = 150.0,

    val seaWindyM: Double = 0.5,
    val seaHarshM: Double = 1.0,

    val travelMinutes: Long = 60L,
) {
    val travel: Duration get() = Duration.ofMinutes(travelMinutes)

    companion object {
        val DEFAULT = Thresholds()
    }
}
