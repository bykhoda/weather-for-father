package com.bykhavoy.ehat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bykhavoy.ehat.domain.model.Forecast
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * Persists the last successful forecast so the app can paint cache FIRST on
 * launch and only then hit the network (spec §7). Corrupt/legacy JSON is
 * treated as "no cache" rather than crashing.
 */
class ForecastCache(private val store: DataStore<Preferences>) : ForecastCachePort {

    private val key = stringPreferencesKey("forecast_snapshot")
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun save(forecast: Forecast, fetchedAt: Instant) {
        val snapshot = CachedForecast.from(forecast, fetchedAt)
        store.edit { it[key] = json.encodeToString(snapshot) }
    }

    override suspend fun load(): Pair<Forecast, Instant>? {
        val raw = store.data.first()[key] ?: return null
        return runCatching { json.decodeFromString<CachedForecast>(raw).toDomain() }.getOrNull()
    }

    override suspend fun clear() {
        store.edit { it.remove(key) }
    }
}
