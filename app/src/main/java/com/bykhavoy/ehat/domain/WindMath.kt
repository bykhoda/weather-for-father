package com.bykhavoy.ehat.domain

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Wind geometry and range-penalty heuristics (spec §5.3).
 *
 * CONVENTION — read carefully. `windFromDeg` follows the meteorological
 * convention: the direction the wind blows FROM, not toward. This is the single
 * most common bug in weather apps, so it is stated once, here, and used
 * consistently everywhere (UI particle field included, spec §12.4).
 */
object WindMath {

    /** Degrees -> radians. */
    private fun rad(deg: Double): Double = deg / 180.0 * Math.PI

    /**
     * Headwind component along the route.
     * @return positive = headwind (into your face), negative = tailwind.
     *
     * Sanity check: wind coming exactly from where you are heading
     * (windFrom == bearing) gives cos(0) = 1 -> pure headwind. Correct.
     */
    fun headwindComponent(windSpeedMs: Double, windFromDeg: Double, routeBearingDeg: Double): Double {
        val delta = rad(windFromDeg - routeBearingDeg)
        return windSpeedMs * cos(delta)
    }

    /** Crosswind component; sign encodes left/right of the route. */
    fun crosswindComponent(windSpeedMs: Double, windFromDeg: Double, routeBearingDeg: Double): Double {
        val delta = rad(windFromDeg - routeBearingDeg)
        return windSpeedMs * sin(delta)
    }

    /**
     * HEURISTIC range loss, not physics (spec §5.3). Aerodynamic drag ∝
     * (v_car + v_headwind)². At ~100 km/h aero is ~60% of total consumption.
     * Always presented in UI as "ориентировочно" — never as a hard number.
     *
     * @return percent extra consumption; 0 for zero/tailwind or non-positive car speed.
     */
    fun rangePenaltyPercent(carSpeedMs: Double, headwindMs: Double, aeroShare: Double = 0.6): Double {
        if (carSpeedMs <= 0.0) return 0.0
        if (headwindMs <= 0.0) return 0.0 // tailwind: don't advertise a phantom gain
        val ratio = ((carSpeedMs + headwindMs).pow(2) - carSpeedMs.pow(2)) / carSpeedMs.pow(2)
        return ratio * aeroShare * 100.0
    }

    /** Shortest signed angular delta in (-180, 180]; used for arrow rotation (spec §12.6). */
    fun shortestDelta(from: Double, to: Double): Double =
        ((to - from + 540.0) % 360.0) - 180.0
}
