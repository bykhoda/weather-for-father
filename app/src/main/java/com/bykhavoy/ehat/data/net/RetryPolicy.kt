package com.bykhavoy.ehat.data.net

import kotlin.math.pow

/**
 * When and how long to retry (spec §13.5).
 *
 * Retried:     Timeout, Server(5xx), RateLimited
 * NOT retried: Offline, Client(4xx), Malformed, CaptivePortal
 *
 * Backoff is exponential with jitter (1s, 2s, 4s ± 30%). Jitter is mandatory:
 * without it, when a whole dead zone regains signal, every client's retries
 * line up into a synchronized burst.
 */
object RetryPolicy {
    const val MAX_ATTEMPTS = 3
    private const val BASE_MS = 1_000L
    private const val RATE_LIMIT_BASE_MS = 30_000L
    private const val JITTER = 0.30

    fun isRetryable(error: FetchError): Boolean = when (error) {
        FetchError.Timeout, is FetchError.Server, FetchError.RateLimited -> true
        FetchError.Offline, is FetchError.Client, is FetchError.Malformed, FetchError.CaptivePortal -> false
    }

    /**
     * @param attempt 1-based index of the attempt that just failed.
     * @param retryAfterSec value of the `Retry-After` header, if present.
     * @param rand injectable 0..1 source for deterministic tests.
     */
    fun backoffMillis(
        attempt: Int,
        error: FetchError,
        retryAfterSec: Long? = null,
        rand: () -> Double = Math::random,
    ): Long {
        if (retryAfterSec != null) return retryAfterSec * 1_000L
        val base = if (error == FetchError.RateLimited) RATE_LIMIT_BASE_MS else BASE_MS
        val exp = base * 2.0.pow((attempt - 1).coerceAtLeast(0)).toLong()
        val jitterFactor = 1.0 + (rand() * 2.0 - 1.0) * JITTER // in [0.7, 1.3]
        return (exp * jitterFactor).toLong()
    }
}
