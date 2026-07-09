package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.BuildConfig
import com.bykhavoy.ehat.domain.model.Location

/**
 * Fixed locations shown in the app. Two family points, both near the Caspian:
 *  - HOME  = ЖК «Грин Парк» in Aktau (~800 m from the sea)
 *  - SEA   = «Ивушка» resort at Тёплый пляж (~25 km south), where sea-water
 *            temperature and waves are most relevant.
 * SEA coordinates come from local.properties (BuildConfig); HOME is a constant.
 */
object Constants {
    val HOME = Location("Грин Парк", 43.6300, 51.1580)
    val SEA = Location("Ивушка", BuildConfig.DACHA_LAT, BuildConfig.DACHA_LON)

    val LOCATIONS = listOf(HOME, SEA)

    /** Foreground auto-refresh cadence (minutes). */
    const val AUTO_REFRESH_MINUTES: Long = 15L
}
