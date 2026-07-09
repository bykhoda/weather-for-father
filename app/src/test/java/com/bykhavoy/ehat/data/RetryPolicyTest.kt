package com.bykhavoy.ehat.data

import com.bykhavoy.ehat.data.net.FetchError
import com.bykhavoy.ehat.data.net.RetryPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RetryPolicyTest {

    // rand=0.5 -> jitter factor exactly 1.0, so backoff is deterministic.
    private val noJitter = { 0.5 }

    @Test fun retryable_set() {
        assertTrue(RetryPolicy.isRetryable(FetchError.Timeout))
        assertTrue(RetryPolicy.isRetryable(FetchError.Server(503)))
        assertTrue(RetryPolicy.isRetryable(FetchError.RateLimited))
    }

    @Test fun non_retryable_set() {
        assertFalse(RetryPolicy.isRetryable(FetchError.Offline))
        assertFalse(RetryPolicy.isRetryable(FetchError.Client(400)))
        assertFalse(RetryPolicy.isRetryable(FetchError.Malformed("x")))
        assertFalse(RetryPolicy.isRetryable(FetchError.CaptivePortal))
    }

    @Test fun exponential_backoff_1_2_4_seconds() {
        assertEquals(1_000L, RetryPolicy.backoffMillis(1, FetchError.Server(503), rand = noJitter))
        assertEquals(2_000L, RetryPolicy.backoffMillis(2, FetchError.Server(503), rand = noJitter))
        assertEquals(4_000L, RetryPolicy.backoffMillis(3, FetchError.Server(503), rand = noJitter))
    }

    @Test fun rate_limited_starts_at_30s() {
        assertEquals(30_000L, RetryPolicy.backoffMillis(1, FetchError.RateLimited, rand = noJitter))
    }

    @Test fun retry_after_header_wins() {
        assertEquals(7_000L, RetryPolicy.backoffMillis(1, FetchError.Server(503), retryAfterSec = 7, rand = noJitter))
    }

    @Test fun jitter_stays_within_thirty_percent() {
        val low = RetryPolicy.backoffMillis(1, FetchError.Server(503), rand = { 0.0 })  // -30%
        val high = RetryPolicy.backoffMillis(1, FetchError.Server(503), rand = { 1.0 }) // +30%
        assertEquals(700L, low)
        assertEquals(1_300L, high)
    }
}
