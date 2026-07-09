package com.bykhavoy.ehat.data.net

/**
 * Failure taxonomy (spec §13.2). Every failure maps to exactly one of these,
 * which in turn drives both the retry policy (§13.5) and the UI state (§13.9).
 * The user never sees any of these — only a freshness stamp changes. Developers
 * see them on the debug screen (§13.10).
 */
sealed interface FetchError {
    /** No route to host: UnknownHostException, ConnectException. Never retried. */
    data object Offline : FetchError

    /** SocketTimeoutException, InterruptedIOException. Retried. */
    data object Timeout : FetchError

    /** 5xx — server is down. Retried. */
    data class Server(val code: Int) : FetchError

    /** 4xx — our bug (bad coords/params). NOT retried. */
    data class Client(val code: Int) : FetchError

    /** 429 — Open-Meteo rate limit. Retried with a long pause. */
    data object RateLimited : FetchError

    /** Response does not parse: SerializationException, schema drift. NOT retried. */
    data class Malformed(val cause: String) : FetchError

    /** Hotspot/captive portal returned HTML (usually 200) instead of JSON. NOT retried. */
    data object CaptivePortal : FetchError
}
