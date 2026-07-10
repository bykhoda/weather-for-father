package com.bykhavoy.ehat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bykhavoy.ehat.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class PlaceDto(val name: String, val lat: Double, val lon: Double)

/**
 * User-configured points. Persisted as a JSON list so any number of places can
 * be added on the onboarding screen (and edited later). Defaults to the two
 * original points so a fresh install still shows something sensible.
 */
class PlacesStore(private val store: DataStore<Preferences>) {

    private val placesKey = stringPreferencesKey("places_json")
    private val onboardedKey = booleanPreferencesKey("places_onboarded")
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(PlaceDto.serializer())

    val places: Flow<List<Location>> = store.data.map { p ->
        val raw = p[placesKey]
        val list = if (raw.isNullOrBlank()) defaults()
        else runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(defaults())
        list.map { Location(it.name, it.lat, it.lon) }.ifEmpty { defaults().map { Location(it.name, it.lat, it.lon) } }
    }

    val onboarded: Flow<Boolean> = store.data.map { it[onboardedKey] ?: false }

    suspend fun save(list: List<Location>) {
        val dtos = list.filter { it.name.isNotBlank() }.map { PlaceDto(it.name.trim(), it.lat, it.lon) }
        store.edit { it[placesKey] = json.encodeToString(serializer, dtos) }
    }

    suspend fun setOnboarded() {
        store.edit { it[onboardedKey] = true }
    }

    private fun defaults(): List<PlaceDto> = Constants.LOCATIONS.map { PlaceDto(it.name, it.lat, it.lon) }
}
