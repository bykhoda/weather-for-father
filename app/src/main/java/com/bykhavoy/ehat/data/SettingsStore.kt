package com.bykhavoy.ehat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persisted view preferences, edited from the Filters screen:
 * table step (1h/3h), a date range (calendar range picker; null = full 2 weeks),
 * and which columns are shown.
 */
class SettingsStore(private val store: DataStore<Preferences>) {

    private val stepKey = intPreferencesKey("step_hours")
    private val colsKey = stringSetPreferencesKey("enabled_columns")
    private val startKey = longPreferencesKey("range_start_ms")
    private val endKey = longPreferencesKey("range_end_ms")

    val stepHours: Flow<Int> = store.data.map { it[stepKey] ?: DEFAULT_STEP }
    val enabledColumns: Flow<Set<String>> = store.data.map { it[colsKey] ?: DEFAULT_COLUMNS }

    /** Selected date range in epoch millis, or null for the full fetched horizon. */
    val range: Flow<Pair<Long, Long>?> = store.data.map { p ->
        val s = p[startKey]; val e = p[endKey]
        if (s != null && e != null) s to e else null
    }

    /** Apply the whole draft at once (Filters "Применить"). start/end null = full range. */
    suspend fun applyFilters(step: Int, columns: Set<String>, startMs: Long?, endMs: Long?) = store.edit { p ->
        p[stepKey] = step
        p[colsKey] = columns
        if (startMs != null && endMs != null) {
            p[startKey] = startMs
            p[endKey] = endMs
        } else {
            p.remove(startKey); p.remove(endKey)
        }
    }

    companion object {
        const val DEFAULT_STEP = 3
        val DEFAULT_COLUMNS = setOf("SKY", "TEMP", "WIND", "GUST", "DIR", "PRECIP", "HUMIDITY", "SEA_TEMP", "WAVE")
    }
}
