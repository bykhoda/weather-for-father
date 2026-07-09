package com.bykhavoy.ehat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bykhavoy.ehat.data.Constants
import com.bykhavoy.ehat.data.ForecastRepository
import com.bykhavoy.ehat.data.ForecastState
import com.bykhavoy.ehat.data.SettingsStore
import com.bykhavoy.ehat.data.net.FetchDiagnostics
import com.bykhavoy.ehat.domain.ArrivalForecast
import com.bykhavoy.ehat.domain.CalmWindow
import com.bykhavoy.ehat.domain.Clock
import com.bykhavoy.ehat.domain.FactorEngine
import com.bykhavoy.ehat.domain.StatusMapper
import com.bykhavoy.ehat.domain.WindMath
import com.bykhavoy.ehat.domain.WindStatus
import com.bykhavoy.ehat.domain.model.CurrentPoint
import com.bykhavoy.ehat.domain.model.Forecast
import com.bykhavoy.ehat.domain.model.HourlyPoint
import com.bykhavoy.ehat.domain.model.LocationForecast
import com.bykhavoy.ehat.domain.model.Thresholds
import com.bykhavoy.ehat.domain.model.Verdict
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The only place UI logic lives (spec §11). Maps ForecastState + Thresholds into
 * a fully-computed [UiState]; composables just draw it.
 */
class MainViewModel(
    private val repository: ForecastRepository,
    private val settings: SettingsStore,
    private val clock: Clock,
    val diagnostics: StateFlow<FetchDiagnostics>,
) : ViewModel() {

    /** Gate for the splash: it stays only until the cache has been read (spec §13.11). */
    private val _cacheLoaded = MutableStateFlow(false)
    val cacheLoaded: StateFlow<Boolean> = _cacheLoaded.asStateFlow()

    val thresholds: StateFlow<Thresholds> =
        settings.thresholds.stateIn(viewModelScope, SharingStarted.Eagerly, Thresholds.DEFAULT)

    val uiState: StateFlow<UiState> =
        combine(repository.state, settings.thresholds) { state, t -> build(state, t) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        viewModelScope.launch {
            repository.loadCache() // paint cache first (spec §7)
            _cacheLoaded.value = true
            repository.refresh()
        }
        viewModelScope.launch {
            while (true) {
                delay(Constants.AUTO_REFRESH_MINUTES * 60_000)
                repository.refresh() // foreground auto-refresh (spec §7)
            }
        }
    }

    fun refresh() = viewModelScope.launch { repository.refresh() }

    fun updateThresholds(t: Thresholds) = viewModelScope.launch { settings.update(t) }

    // --- mapping ---------------------------------------------------------------

    private val hhmm = DateTimeFormatter.ofPattern("HH:mm")

    private fun build(state: ForecastState, t: Thresholds): UiState = when (state) {
        is ForecastState.Loading -> UiState(phase = UiState.Phase.LOADING)
        is ForecastState.Empty -> UiState(phase = UiState.Phase.EMPTY, emptyLabel = state.errorLabel)
        is ForecastState.Loaded -> buildContent(state.data, state.fetchedAt, t)
    }

    private fun buildContent(forecast: Forecast, fetchedAt: Instant, t: Thresholds): UiState {
        val now = clock.now()

        val aktauCard = currentCard(forecast.aktau, t)

        val arrival = ArrivalForecast.forecastOnArrival(forecast.dacha.hourly, now, t.travel)
        val arrivalHour = arrival ?: fallbackHour(forecast.dacha)
        val dachaCard = arrivalCard(forecast.dacha, arrivalHour, t)

        val month = arrivalHour.time.atZone(FactorEngine.AQTAU).month
        val factors = FactorEngine.buildFactors(arrivalHour, month, t)
        val verdict = FactorEngine.verdictOf(factors)

        val histogram = forecast.dacha.hourly.take(72).map { h ->
            HistogramBar(
                gustMs = (h.gustMs ?: 0.0).toFloat(),
                status = FactorEngine.verdictStatusOf(h, t),
                present = h.gustMs != null,
            )
        }

        val cta = buildCta(verdict, forecast, t, now)

        return UiState(
            phase = UiState.Phase.CONTENT,
            sceneStatus = verdict.status,
            aktau = aktauCard,
            dacha = dachaCard,
            factors = factors,
            bindingId = verdict.binding?.id,
            histogram = histogram,
            cta = cta,
            freshness = freshnessOf(fetchedAt, now),
        )
    }

    private fun currentCard(loc: LocationForecast, t: Thresholds): CardModel {
        val c: CurrentPoint = loc.current
        val gust = c.gustMs ?: 0.0
        return CardModel(
            title = loc.location.name,
            subtitle = null,
            windMs = (c.windSpeedMs ?: 0.0).roundToInt(),
            gustMs = gust.roundToInt(),
            windFromDeg = (c.windFromDeg ?: 0.0).toFloat(),
            status = StatusMapper.windStatus(gust, t),
            secondary = null,
        )
    }

    private fun arrivalCard(loc: LocationForecast, hour: HourlyPoint, t: Thresholds): CardModel {
        val gust = hour.gustMs ?: 0.0
        val wind = hour.windSpeedMs ?: 0.0
        val dir = hour.windFromDeg ?: 0.0
        val arrivalLabel = "к приезду, ~${hour.time.atZone(FactorEngine.AQTAU).format(hhmm)}"

        val headwind = WindMath.headwindComponent(wind, dir, Constants.ROUTE_BEARING)
        val secondary = when {
            headwind > 0.5 -> {
                val pct = WindMath.rangePenaltyPercent(Constants.CAR_SPEED_MS, headwind).roundToInt()
                "встречный · ориент. −$pct% хода"
            }
            headwind < -0.5 -> "попутный ветер"
            else -> null
        }
        return CardModel(
            title = loc.location.name,
            subtitle = arrivalLabel,
            windMs = wind.roundToInt(),
            gustMs = gust.roundToInt(),
            windFromDeg = dir.toFloat(),
            status = StatusMapper.windStatus(gust, t),
            secondary = secondary,
        )
    }

    private fun buildCta(verdict: Verdict, forecast: Forecast, t: Thresholds, now: Instant): CtaModel {
        val reason = when (verdict.status) {
            WindStatus.CALM -> "Всё спокойно, можно ехать"
            WindStatus.WINDY -> verdict.binding?.verdictLine ?: "Есть нюанс, но ехать можно"
            WindStatus.HARSH -> {
                val window = CalmWindow.firstCalmWindow(forecast.dacha.hourly, t)
                val base = verdict.binding?.verdictLine ?: "Сегодня не стоит"
                "$base · ${CtaText.calmWindowPhrase(window, now)}"
            }
        }
        return CtaModel(headline = verdict.headline, reason = reason, status = verdict.status)
    }

    /** Nearest-to-now hour, used only if arrival selection returns nothing. */
    private fun fallbackHour(loc: LocationForecast): HourlyPoint {
        val n = clock.now()
        return loc.hourly.minByOrNull { abs(java.time.Duration.between(it.time, n).toMillis()) }
            ?: HourlyPoint(n, true, null, null, null, null, null, null, null, null, null, null)
    }

    class Factory(
        private val repository: ForecastRepository,
        private val settings: SettingsStore,
        private val clock: Clock,
        private val diagnostics: StateFlow<FetchDiagnostics>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(repository, settings, clock, diagnostics) as T
    }
}
