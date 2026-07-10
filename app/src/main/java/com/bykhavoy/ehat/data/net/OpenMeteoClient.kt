package com.bykhavoy.ehat.data.net

import com.bykhavoy.ehat.data.ForecastMapper
import com.bykhavoy.ehat.domain.SystemClock
import com.bykhavoy.ehat.domain.model.Forecast
import com.bykhavoy.ehat.domain.model.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Talks to the three Open-Meteo endpoints (spec §4, §14.4). The forecast
 * endpoint is fatal on failure; air-quality and marine are best-effort and
 * degrade to null columns. All three run in parallel; a slow or dead optional
 * endpoint never blocks the forecast.
 */
class OpenMeteoClient(
    private val places: () -> List<Location>,
    private val clock: SystemClock,
    private val onDiagnostics: (FetchDiagnostics.() -> FetchDiagnostics) -> Unit = {},
    private val http: OkHttpClient = defaultHttp(),
) : OpenMeteoApi {

    @Volatile
    var lastRetryAfterSec: Long? = null
        private set

    private fun forecastUrl(lat: String, lon: String) = "https://api.open-meteo.com/v1/forecast".toHttpUrl().newBuilder()
        .addQueryParameter("latitude", lat)
        .addQueryParameter("longitude", lon)
        .addQueryParameter(
            "hourly",
            "temperature_2m,relative_humidity_2m,apparent_temperature," +
                "wind_speed_10m,wind_gusts_10m,wind_direction_10m," +
                "uv_index,precipitation_probability,weather_code,is_day",
        )
        .addQueryParameter("current", "wind_speed_10m,wind_gusts_10m,wind_direction_10m,temperature_2m")
        .addQueryParameter("wind_speed_unit", "ms")
        .addQueryParameter("timezone", "Asia/Aqtau")
        .addQueryParameter("forecast_days", "14")
        .build().toString()

    private fun airUrl(lat: String, lon: String) = "https://air-quality-api.open-meteo.com/v1/air-quality".toHttpUrl().newBuilder()
        .addQueryParameter("latitude", lat)
        .addQueryParameter("longitude", lon)
        .addQueryParameter("hourly", "pm10,dust")
        .addQueryParameter("timezone", "Asia/Aqtau")
        .addQueryParameter("forecast_days", "7")
        .build().toString()

    private fun marineUrl(lat: String, lon: String) = "https://marine-api.open-meteo.com/v1/marine".toHttpUrl().newBuilder()
        .addQueryParameter("latitude", lat)
        .addQueryParameter("longitude", lon)
        .addQueryParameter("hourly", "wave_height,sea_surface_temperature")
        .addQueryParameter("timezone", "Asia/Aqtau")
        .addQueryParameter("forecast_days", "14")
        .build().toString()

    override suspend fun fetch(): ApiResult = withContext(Dispatchers.IO) {
        val pl = places()
        if (pl.isEmpty()) return@withContext ApiResult.Err(FetchError.Malformed("no places configured"))
        val lat = pl.joinToString(",") { it.lat.toString() }
        val lon = pl.joinToString(",") { it.lon.toString() }
        coroutineScope {
            // Optional endpoints run in parallel and never fail the whole fetch.
            val airDef = async { (executeRaw(airUrl(lat, lon)) as? Raw.Ok)?.outcome?.body }
            val marineDef = async { (executeRaw(marineUrl(lat, lon)) as? Raw.Ok)?.outcome?.body }

            when (val fRaw = executeRaw(forecastUrl(lat, lon))) {
                is Raw.Fail -> {
                    recordError(fRaw.error)
                    return@coroutineScope ApiResult.Err(fRaw.error, lastRetryAfterSec)
                }
                is Raw.Ok -> {
                    val out = fRaw.outcome
                    applyClockSkew(out.serverDate)

                    val forecasts = try {
                        OpenMeteoParser.parseForecast(out.body)
                    } catch (e: SerializationException) {
                        val err = FetchError.Malformed(e.message ?: "serialization")
                        recordError(err, out)
                        return@coroutineScope ApiResult.Err(err)
                    }
                    if (forecasts.size < pl.size || (forecasts.firstOrNull()?.hourly?.time?.size ?: 0) < 24) {
                        val err = FetchError.Malformed("expected ${pl.size} points with >=24 hours")
                        recordError(err, out)
                        return@coroutineScope ApiResult.Err(err)
                    }

                    val air = airDef.await()?.let { runCatching { OpenMeteoParser.parseAirQuality(it) }.getOrNull() }
                    val marine = marineDef.await()?.let { runCatching { OpenMeteoParser.parseMarine(it) }.getOrNull() }

                    val forecast = Forecast(
                        locations = pl.indices.map { i ->
                            ForecastMapper.merge(pl[i], forecasts[i], air?.getOrNull(i), marine?.getOrNull(i))
                        },
                    )
                    recordSuccess(out)
                    ApiResult.Ok(forecast, out.serverDate)
                }
            }
        }
    }

    // --- low level ------------------------------------------------------------

    private data class HttpOutcome(
        val code: Int,
        val contentType: String?,
        val serverDate: Instant?,
        val body: String,
    )

    private sealed interface Raw {
        data class Ok(val outcome: HttpOutcome) : Raw
        data class Fail(val error: FetchError) : Raw
    }

    private fun executeRaw(url: String): Raw {
        val req = Request.Builder().url(url).header("Accept", "application/json").build()
        return try {
            http.newCall(req).execute().use { resp ->
                val code = resp.code
                val contentType = resp.header("Content-Type")
                val serverDate = resp.headers.getDate("Date")?.toInstant()
                lastRetryAfterSec = resp.header("Retry-After")?.toLongOrNull()

                if (!resp.isSuccessful) {
                    return when {
                        code == 429 -> Raw.Fail(FetchError.RateLimited)
                        code in 500..599 -> Raw.Fail(FetchError.Server(code))
                        code in 400..499 -> Raw.Fail(FetchError.Client(code))
                        else -> Raw.Fail(FetchError.Server(code))
                    }
                }
                // Captive portal check BEFORE parsing (spec §13.3): a login page
                // is HTTP 200 with HTML, not JSON.
                if (contentType == null || !contentType.contains("application/json", ignoreCase = true)) {
                    return Raw.Fail(FetchError.CaptivePortal)
                }
                val body = resp.body?.string().orEmpty()
                Raw.Ok(HttpOutcome(code, contentType, serverDate, body))
            }
        } catch (e: UnknownHostException) {
            Raw.Fail(FetchError.Offline)
        } catch (e: ConnectException) {
            Raw.Fail(FetchError.Offline)
        } catch (e: InterruptedIOException) {
            // SocketTimeoutException extends this.
            Raw.Fail(FetchError.Timeout)
        } catch (e: java.io.IOException) {
            Raw.Fail(FetchError.Offline)
        }
    }

    /** Trust the server clock over a possibly-broken head-unit clock (spec §13.4). */
    private fun applyClockSkew(serverDate: Instant?) {
        if (serverDate == null) return
        val skew = Duration.between(Instant.now(), serverDate)
        if (skew.abs() > Duration.ofMinutes(10)) {
            clock.offsetMillis = skew.toMillis()
        }
        onDiagnostics { copy(lastServerDate = serverDate, clockOffsetMs = clock.offsetMillis) }
    }

    private fun recordSuccess(out: HttpOutcome) = onDiagnostics {
        copy(
            lastSuccessAt = clock.now(),
            lastHttpCode = out.code,
            lastContentType = out.contentType,
            lastServerDate = out.serverDate,
            clockOffsetMs = clock.offsetMillis,
            lastErrorType = null,
            lastErrorMessage = null,
            rawBodySnippet = out.body.take(4096),
        )
    }

    private fun recordError(error: FetchError, out: HttpOutcome? = null) = onDiagnostics {
        copy(
            lastHttpCode = out?.code ?: lastHttpCode,
            lastContentType = out?.contentType ?: lastContentType,
            lastErrorType = error::class.simpleName,
            lastErrorMessage = error.toString(),
            lastAttemptAt = Instant.now(),
            rawBodySnippet = out?.body?.take(4096) ?: rawBodySnippet,
        )
    }

    companion object {
        fun defaultHttp(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS) // spec §7 / §13.5 — not 30
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }
}
