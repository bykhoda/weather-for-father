package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.domain.model.Forecast
import java.time.Instant

/**
 * What the repository currently holds (spec §7, §13.9). Freshness thresholds
 * (>3h, >12h) are computed in the UI from [fetchedAt] rather than baked into
 * the type, so the same cached data can render at different staleness levels
 * without a new emission.
 */
sealed interface ForecastState {
    /** First launch, no cache yet, network in flight. UI shows a skeleton, never a spinner. */
    data object Loading : ForecastState

    /** Data present, most recently fetched at [fetchedAt]. */
    data class Loaded(val data: Forecast, val fetchedAt: Instant) : ForecastState

    /** No cache and the network failed. The only state with a retry button. */
    data class Empty(val errorLabel: String) : ForecastState
}
