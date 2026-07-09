package com.bykhavoy.ehat.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/** Single DataStore for both the forecast cache and the settings (spec §2 stack). */
val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "ehat")
