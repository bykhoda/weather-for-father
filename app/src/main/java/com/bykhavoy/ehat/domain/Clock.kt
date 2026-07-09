package com.bykhavoy.ehat.domain

import java.time.Instant

/**
 * Time source, injected everywhere instead of calling [Instant.now] directly.
 *
 * Rationale (spec §13.4): head units often have wrong system time (no NTP, no
 * SIM, reset on battery pull). All time-dependent logic — above all
 * [forecastOnArrival] — must be driven by a trusted clock, not the device
 * clock. The data layer corrects skew against the server `Date` header and
 * feeds a corrected [Clock] here. Tests inject a fixed clock for determinism.
 */
fun interface Clock {
    fun now(): Instant
}

/** System clock plus an offset (millis) derived from the server `Date` header. */
class SystemClock(@Volatile var offsetMillis: Long = 0L) : Clock {
    override fun now(): Instant = Instant.now().plusMillis(offsetMillis)
}

/** Fixed clock for tests. */
class FixedClock(private val fixed: Instant) : Clock {
    override fun now(): Instant = fixed
}
