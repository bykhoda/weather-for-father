package com.bykhavoy.ehat.data.net

import com.bykhavoy.ehat.domain.model.Forecast
import java.time.Instant

/** Outcome of one full fetch (all three endpoints merged). */
sealed interface ApiResult {
    data class Ok(val forecast: Forecast, val serverDate: Instant?) : ApiResult
    data class Err(val error: FetchError, val retryAfterSec: Long? = null) : ApiResult
}

/** The network boundary. Real impl: [OpenMeteoClient]; tests inject a fake. */
interface OpenMeteoApi {
    suspend fun fetch(): ApiResult
}

/**
 * Everything the user must NOT see but a developer needs (spec §13.10).
 * Surfaced only on the hidden debug screen.
 */
data class FetchDiagnostics(
    val lastSuccessAt: Instant? = null,
    val lastHttpCode: Int? = null,
    val lastContentType: String? = null,
    val lastServerDate: Instant? = null,
    val clockOffsetMs: Long = 0L,
    val lastErrorType: String? = null,
    val lastErrorMessage: String? = null,
    val attempts: Int = 0,
    val lastAttemptAt: Instant? = null,
    val rawBodySnippet: String? = null,
)
