package com.bykhavoy.ehat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.bykhavoy.ehat.domain.model.Thresholds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the user-tuned thresholds and travel time (spec §6, §14.6).
 * Defaults come from [Thresholds.DEFAULT] whenever a key is unset.
 */
class SettingsStore(private val store: DataStore<Preferences>) {

    private val windWindy = doublePreferencesKey("wind_windy")
    private val windHarsh = doublePreferencesKey("wind_harsh")
    private val uvHarsh = doublePreferencesKey("uv_harsh")
    private val heatWarmHarsh = doublePreferencesKey("heat_warm_harsh")
    private val travel = longPreferencesKey("travel_minutes")

    val thresholds: Flow<Thresholds> = store.data.map { p ->
        val d = Thresholds.DEFAULT
        d.copy(
            windWindyMs = p[windWindy] ?: d.windWindyMs,
            windHarshMs = p[windHarsh] ?: d.windHarshMs,
            uvHarsh = p[uvHarsh] ?: d.uvHarsh,
            heatWarmHarshC = p[heatWarmHarsh] ?: d.heatWarmHarshC,
            travelMinutes = p[travel] ?: d.travelMinutes,
        )
    }

    suspend fun update(t: Thresholds) {
        store.edit { p ->
            p[windWindy] = t.windWindyMs
            p[windHarsh] = t.windHarshMs
            p[uvHarsh] = t.uvHarsh
            p[heatWarmHarsh] = t.heatWarmHarshC
            p[travel] = t.travelMinutes
        }
    }
}
