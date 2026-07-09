package com.bykhavoy.ehat

import android.content.Context
import com.bykhavoy.ehat.data.Constants
import com.bykhavoy.ehat.data.ForecastCache
import com.bykhavoy.ehat.data.ForecastRepository
import com.bykhavoy.ehat.data.SettingsStore
import com.bykhavoy.ehat.data.appDataStore
import com.bykhavoy.ehat.data.net.FetchDiagnostics
import com.bykhavoy.ehat.data.net.OpenMeteoClient
import com.bykhavoy.ehat.domain.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * The entire dependency graph, wired by hand (spec §2: no Hilt/Koin — the app
 * is four classes). Created once in [EhatApp].
 */
class AppGraph(context: Context) {
    private val appContext = context.applicationContext
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val clock = SystemClock()
    val diagnostics = MutableStateFlow(FetchDiagnostics())

    val cache = ForecastCache(appContext.appDataStore)
    val settings = SettingsStore(appContext.appDataStore)

    val api = OpenMeteoClient(
        aktau = Constants.HOME,
        dacha = Constants.SEA,
        clock = clock,
        onDiagnostics = { transform -> diagnostics.update { it.transform() } },
    )

    val repository = ForecastRepository(api, cache, clock, scope, diagnostics)
}
