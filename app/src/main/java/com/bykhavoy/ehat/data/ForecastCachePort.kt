package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.domain.model.Forecast
import java.time.Instant

/** Cache seam so the repository can be unit-tested without a real DataStore. */
interface ForecastCachePort {
    suspend fun save(forecast: Forecast, fetchedAt: Instant)
    suspend fun load(): Pair<Forecast, Instant>?
    suspend fun clear()
}
