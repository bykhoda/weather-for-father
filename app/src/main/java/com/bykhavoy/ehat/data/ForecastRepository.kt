package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.data.net.ApiResult
import com.bykhavoy.ehat.data.net.FetchDiagnostics
import com.bykhavoy.ehat.data.net.FetchError
import com.bykhavoy.ehat.data.net.OpenMeteoApi
import com.bykhavoy.ehat.data.net.RetryPolicy
import com.bykhavoy.ehat.domain.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the forecast state machine (spec §7, §13). Responsibilities:
 *  - paint cache first, network second;
 *  - retry only retryable failures, with backoff+jitter (§13.5);
 *  - single-flight concurrent refreshes (§13.6);
 *  - never surface an error as a dialog — only via state + freshness stamp.
 */
class ForecastRepository(
    private val api: OpenMeteoApi,
    private val cache: ForecastCachePort,
    private val clock: Clock,
    private val scope: CoroutineScope,
    private val diagnostics: MutableStateFlow<FetchDiagnostics>,
    private val sleeper: suspend (Long) -> Unit = { delay(it) },
) {
    private val _state = MutableStateFlow<ForecastState>(ForecastState.Loading)
    val state: StateFlow<ForecastState> = _state.asStateFlow()

    private val fetchMutex = Mutex()
    private var inFlight: Deferred<ApiResult>? = null

    /** Load cache synchronously into state before any network call (spec §7). */
    suspend fun loadCache() {
        cache.load()?.let { (forecast, at) ->
            _state.value = ForecastState.Loaded(forecast, at)
        }
    }

    /**
     * Single-flight refresh (spec §13.6): concurrent callers share one in-flight
     * fetch instead of firing a burst of parallel requests.
     */
    suspend fun refresh(): ApiResult {
        val deferred = fetchMutex.withLock {
            inFlight?.takeIf { it.isActive } ?: scope.async { fetchWithRetry() }.also { inFlight = it }
        }
        return deferred.await()
    }

    private suspend fun fetchWithRetry(): ApiResult {
        var attempt = 0
        while (true) {
            attempt++
            diagnostics.update { it.copy(attempts = attempt, lastAttemptAt = clock.now()) }

            when (val result = api.fetch()) {
                is ApiResult.Ok -> {
                    val at = clock.now()
                    cache.save(result.forecast, at)
                    _state.value = ForecastState.Loaded(result.forecast, at)
                    return result
                }
                is ApiResult.Err -> {
                    val canRetry = RetryPolicy.isRetryable(result.error) && attempt < RetryPolicy.MAX_ATTEMPTS
                    // Offline never retries — no point burning battery for a known answer (§13.5).
                    if (canRetry) {
                        sleeper(RetryPolicy.backoffMillis(attempt, result.error, result.retryAfterSec))
                        continue
                    }
                    // Terminal failure: keep cache if we have it, else show the one empty state.
                    if (_state.value !is ForecastState.Loaded) {
                        _state.value = ForecastState.Empty(labelFor(result.error))
                    }
                    return result
                }
            }
        }
    }

    private fun labelFor(error: FetchError): String = when (error) {
        FetchError.Offline, FetchError.Timeout, FetchError.CaptivePortal -> "Нет связи"
        is FetchError.Server, FetchError.RateLimited -> "Сервис недоступен"
        is FetchError.Client, is FetchError.Malformed -> "Ошибка данных"
    }
}
