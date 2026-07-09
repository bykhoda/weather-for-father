package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.data.net.ApiResult
import com.bykhavoy.ehat.data.net.FetchDiagnostics
import com.bykhavoy.ehat.data.net.FetchError
import com.bykhavoy.ehat.data.net.OpenMeteoApi
import com.bykhavoy.ehat.domain.FixedClock
import com.bykhavoy.ehat.domain.model.CurrentPoint
import com.bykhavoy.ehat.domain.model.Forecast
import com.bykhavoy.ehat.domain.model.HourlyPoint
import com.bykhavoy.ehat.domain.model.Location
import com.bykhavoy.ehat.domain.model.LocationForecast
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ForecastRepositoryTest {

    private val clock = FixedClock(Instant.parse("2025-07-01T07:00:00Z"))

    private fun sample(): Forecast {
        val loc = Location("Актау", 43.6481, 51.1722)
        val t0 = Instant.parse("2025-07-01T07:00:00Z")
        val cur = CurrentPoint(t0, 5.0, 7.0, 315.0, 30.0)
        val hourly = (0..2).map {
            HourlyPoint(t0.plusSeconds(3600L * it), true, 5.0, 7.0, 315.0, 30.0, 3.0, 0.0, 0, 20.0, null, null, 45.0, 28.0)
        }
        val lf = LocationForecast(loc, cur, hourly)
        return Forecast(lf, lf)
    }

    private class FakeApi(
        private val result: ApiResult,
        private val gate: CompletableDeferred<Unit>? = null,
    ) : OpenMeteoApi {
        var calls = 0
        override suspend fun fetch(): ApiResult {
            calls++
            gate?.await()
            return result
        }
    }

    private class FakeCache(var stored: Pair<Forecast, Instant>? = null) : ForecastCachePort {
        override suspend fun save(forecast: Forecast, fetchedAt: Instant) { stored = forecast to fetchedAt }
        override suspend fun load(): Pair<Forecast, Instant>? = stored
        override suspend fun clear() { stored = null }
    }

    @Test fun offline_does_not_retry() = runTest {
        val api = FakeApi(ApiResult.Err(FetchError.Offline))
        val repo = ForecastRepository(api, FakeCache(), clock, this, MutableStateFlow(FetchDiagnostics())) { }
        repo.refresh()
        assertEquals(1, api.calls)
        assertIs<ForecastState.Empty>(repo.state.value)
    }

    @Test fun client_400_does_not_retry() = runTest {
        val api = FakeApi(ApiResult.Err(FetchError.Client(400)))
        val repo = ForecastRepository(api, FakeCache(), clock, this, MutableStateFlow(FetchDiagnostics())) { }
        repo.refresh()
        assertEquals(1, api.calls)
    }

    @Test fun server_503_retries_exactly_three_times_then_gives_up() = runTest {
        val api = FakeApi(ApiResult.Err(FetchError.Server(503)))
        val repo = ForecastRepository(api, FakeCache(), clock, this, MutableStateFlow(FetchDiagnostics())) { }
        repo.refresh()
        assertEquals(3, api.calls)
        assertIs<ForecastState.Empty>(repo.state.value)
    }

    @Test fun timeout_with_cache_stays_loaded_not_empty() = runTest {
        val cache = FakeCache(sample() to Instant.parse("2025-07-01T06:00:00Z"))
        val api = FakeApi(ApiResult.Err(FetchError.Timeout))
        val repo = ForecastRepository(api, cache, clock, this, MutableStateFlow(FetchDiagnostics())) { }
        repo.loadCache()
        repo.refresh()
        assertIs<ForecastState.Loaded>(repo.state.value)
    }

    @Test fun two_concurrent_refreshes_make_one_network_call() = runTest {
        val gate = CompletableDeferred<Unit>()
        val api = FakeApi(ApiResult.Ok(sample(), null), gate)
        val repo = ForecastRepository(api, FakeCache(), clock, this, MutableStateFlow(FetchDiagnostics())) { }

        val d1 = async { repo.refresh() }
        val d2 = async { repo.refresh() }
        runCurrent() // let both reach the single-flight guard
        gate.complete(Unit)
        d1.await(); d2.await()

        assertEquals(1, api.calls)
        assertIs<ForecastState.Loaded>(repo.state.value)
    }

    @Test fun success_updates_diagnostics_attempt_count() = runTest {
        val diag = MutableStateFlow(FetchDiagnostics())
        val api = FakeApi(ApiResult.Ok(sample(), null))
        val repo = ForecastRepository(api, FakeCache(), clock, this, diag) { }
        repo.refresh()
        assertTrue(diag.value.attempts >= 1)
    }
}
