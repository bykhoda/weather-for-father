package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.BuildConfig
import com.bykhavoy.ehat.domain.model.Location

/**
 * The only hardcoded coordinates in the app (spec §4). Aktau is public; the
 * dacha comes from BuildConfig (fed by local.properties, defaulting to Aktau).
 * Kept in ONE place on purpose — do not scatter coordinates through the code.
 */
object Constants {
    val AKTAU = Location("Актау", 43.6481, 51.1722)

    // ⚠️ Fill DACHA_LAT/DACHA_LON in local.properties for the real house.
    val DACHA = Location("Дача", BuildConfig.DACHA_LAT, BuildConfig.DACHA_LON)

    /** Azimuth Aktau -> dacha, degrees. Constant, computed once (spec §5.3). */
    val ROUTE_BEARING: Double = BuildConfig.ROUTE_BEARING

    /** Assumed cruising speed for the range-penalty heuristic (~100 km/h). */
    const val CAR_SPEED_MS: Double = 27.0

    /** Foreground auto-refresh cadence (spec §7). */
    const val AUTO_REFRESH_MINUTES: Long = 15L
}
