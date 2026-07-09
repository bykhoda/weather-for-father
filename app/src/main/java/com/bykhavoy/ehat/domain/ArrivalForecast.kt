package com.bykhavoy.ehat.domain

import com.bykhavoy.ehat.domain.model.HourlyPoint
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

/**
 * The key feature rp5 does not offer (spec §5.2): the weather at the moment he
 * ARRIVES, not the weather now. He drives ~1h; current wind at the dacha is
 * useless to him.
 */
object ArrivalForecast {

    /**
     * Nearest hourly slot to (now + travel).
     *
     * @param hasData filter for "usable" slots. Defaults to gust present, so a
     *   null-gust hour at arrival is skipped and the nearest real slot is used
     *   instead (spec §13.8) — the caller then labels its actual time.
     * @return the chosen slot, or null if [hourly] has no usable slot.
     */
    fun forecastOnArrival(
        hourly: List<HourlyPoint>,
        now: Instant,
        travel: Duration,
        hasData: (HourlyPoint) -> Boolean = { it.gustMs != null },
    ): HourlyPoint? {
        val arrival = now.plus(travel)
        return hourly
            .filter(hasData)
            .minByOrNull { abs(Duration.between(it.time, arrival).toMillis()) }
    }
}
