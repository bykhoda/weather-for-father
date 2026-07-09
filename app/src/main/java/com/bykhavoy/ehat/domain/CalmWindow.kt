package com.bykhavoy.ehat.domain

import com.bykhavoy.ehat.domain.model.HourlyPoint
import com.bykhavoy.ehat.domain.model.Thresholds
import java.time.Instant

/**
 * Finds the next stretch when it all comes together (spec §14.9) — not "when
 * the wind drops" but "when every relevant factor is CALM" for at least
 * [minHours] hours in a row. Used for the "НЕ СЕГОДНЯ" reason line.
 */
object CalmWindow {

    /**
     * @return start [Instant] of the first window of >= [minHours] consecutive
     *   hours all at verdict-status CALM, or null if there is none in the series
     *   ("в ближайшие 3 дня не сложится").
     */
    fun firstCalmWindow(
        hourly: List<HourlyPoint>,
        t: Thresholds,
        minHours: Int = 3,
    ): Instant? {
        if (hourly.size < minHours) return null
        val statuses = FactorEngine.hourlyVerdictStatuses(hourly, t)
        var run = 0
        for (idx in statuses.indices) {
            if (statuses[idx] == WindStatus.CALM) {
                run++
                if (run >= minHours) {
                    return hourly[idx - minHours + 1].time
                }
            } else {
                run = 0
            }
        }
        return null
    }
}
