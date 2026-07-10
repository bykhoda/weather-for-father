package com.bykhavoy.ehat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bykhavoy.ehat.data.Constants
import com.bykhavoy.ehat.data.ForecastRepository
import com.bykhavoy.ehat.data.ForecastState
import com.bykhavoy.ehat.data.SettingsStore
import com.bykhavoy.ehat.data.net.ApiResult
import com.bykhavoy.ehat.data.net.FetchDiagnostics
import com.bykhavoy.ehat.domain.Clock
import com.bykhavoy.ehat.domain.FactorEngine
import com.bykhavoy.ehat.domain.StatusMapper
import com.bykhavoy.ehat.domain.WindStatus
import com.bykhavoy.ehat.domain.model.HourlyPoint
import com.bykhavoy.ehat.domain.model.LocationForecast
import com.bykhavoy.ehat.domain.model.Thresholds
import com.bykhavoy.ehat.ui.components.compassFrom
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Builds the tabular forecast state. Clean, filterable, rp5-style. */
class MainViewModel(
    private val repository: ForecastRepository,
    private val settings: SettingsStore,
    private val clock: Clock,
    val diagnostics: StateFlow<FetchDiagnostics>,
) : ViewModel() {

    private val _cacheLoaded = MutableStateFlow(false)
    val cacheLoaded: StateFlow<Boolean> = _cacheLoaded.asStateFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    // Default to Ивушка (the sea point).
    private val selectedTab = MutableStateFlow(1)
    private val refreshing = MutableStateFlow(false)

    val uiState: StateFlow<UiState> =
        combine(
            combine(
                repository.state,
                settings.stepHours,
                settings.enabledColumns,
                settings.range,
                selectedTab,
            ) { state, step, colNames, range, tab -> build(state, step, colNames, range, tab) },
            refreshing,
        ) { s, r -> s.copy(refreshing = r) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        viewModelScope.launch {
            repository.loadCache()
            _cacheLoaded.value = true
            doRefresh()
        }
        viewModelScope.launch {
            while (true) {
                delay(Constants.AUTO_REFRESH_MINUTES * 60_000)
                doRefresh()
            }
        }
    }

    private suspend fun doRefresh() {
        refreshing.value = true
        try {
            val result = repository.refresh()
            // Only alert when we already show cached data — a cold failure is the Empty screen.
            if (result is ApiResult.Err && repository.state.value is ForecastState.Loaded) {
                _errors.tryEmit("Не удалось обновить — показаны сохранённые данные")
            }
        } finally {
            refreshing.value = false
        }
    }

    fun refresh() = viewModelScope.launch { doRefresh() }
    fun selectTab(index: Int) { selectedTab.value = index }

    /** Commit the Filters draft (step, columns, date range). null range = full horizon. */
    fun applyFilters(step: Int, columns: Set<Col>, startMs: Long?, endMs: Long?) = viewModelScope.launch {
        settings.applyFilters(step, columns.map { it.name }.toSet(), startMs, endMs)
    }

    // --- mapping ---------------------------------------------------------------

    private val ru = Locale("ru")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val dayFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM", ru)
    private val zone = FactorEngine.AQTAU
    private val tabNames = Constants.LOCATIONS.map { it.name }

    private fun enabledOf(names: Set<String>): Set<Col> =
        names.mapNotNull { runCatching { Col.valueOf(it) }.getOrNull() }.toSet()

    private fun build(state: ForecastState, step: Int, colNames: Set<String>, range: Pair<Long, Long>?, tab: Int): UiState {
        val enabled = enabledOf(colNames)
        val startMs = range?.first
        val endMs = range?.second
        return when (state) {
            is ForecastState.Loading ->
                UiState(UiState.Phase.LOADING, tabNames, tab, step, enabled, startMs, endMs)
            is ForecastState.Empty ->
                UiState(UiState.Phase.EMPTY, tabNames, tab, step, enabled, startMs, endMs, emptyLabel = state.errorLabel)
            is ForecastState.Loaded -> {
                val now = clock.now()
                val loc = if (tab == 0) state.data.aktau else state.data.dacha
                UiState(
                    phase = UiState.Phase.CONTENT,
                    tabs = tabNames,
                    selectedTab = tab,
                    stepHours = step,
                    enabled = enabled,
                    rangeStartMs = startMs,
                    rangeEndMs = endMs,
                    days = buildDays(loc, step, now, range),
                    hasSeaTemp = loc.hourly.any { it.seaTempC != null },
                    hasWave = loc.hourly.any { it.waveHeightM != null },
                    freshness = freshnessOf(state.fetchedAt, now),
                )
            }
        }
    }

    private fun buildDays(loc: LocationForecast, step: Int, now: Instant, range: Pair<Long, Long>?): List<DaySection> {
        val start = range?.first?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
        val end = range?.second?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
        val filtered = loc.hourly.filter { it.time.atZone(zone).hour % step == 0 }
        // The "now" row is the current step-bucket (latest point at or before now),
        // so it stays marked even at a 3-hour step where the exact hour is skipped.
        val nowBucket = filtered.filter { !it.time.isAfter(now) }.maxByOrNull { it.time }?.time
        return filtered
            .groupBy { it.time.atZone(zone).toLocalDate() }
            .filter { (date, _) -> inRange(date, start, end) }
            .map { (date, points) ->
                val title = dayFmt.format(date.atStartOfDay(zone)).replaceFirstChar { it.uppercase(ru) }
                val rows = points.map { row(it, nowBucket) }
                DaySection(title, rows, hasNow = rows.any { it.isNow }, nowTempC = rows.firstOrNull { it.isNow }?.tempC)
            }
    }

    private fun inRange(date: LocalDate, start: LocalDate?, end: LocalDate?): Boolean {
        if (start == null || end == null) return true
        return !date.isBefore(start) && !date.isAfter(end)
    }

    private fun row(h: HourlyPoint, nowBucket: Instant?): HourRow = HourRow(
        time = h.time.atZone(zone).format(timeFmt),
        sky = WeatherFormat.skyGlyph(h.weatherCode, h.isDay),
        tempC = h.temperatureC?.roundToInt() ?: h.apparentTempC?.roundToInt(),
        feelsC = h.apparentTempC?.roundToInt(),
        humidityPct = h.humidityPct?.roundToInt(),
        windMs = h.windSpeedMs?.roundToInt(),
        gustMs = h.gustMs?.roundToInt(),
        windStatus = h.gustMs?.let { StatusMapper.windStatus(it, Thresholds.DEFAULT) } ?: WindStatus.CALM,
        windFromDeg = h.windFromDeg?.toFloat(),
        compass = h.windFromDeg?.let { compassFrom(it.toFloat()) } ?: "",
        precipPct = h.precipProbPct?.roundToInt(),
        seaTempC = h.seaTempC?.roundToInt(),
        waveM = h.waveHeightM?.let { (it * 10).roundToInt() / 10.0 },
        isNow = nowBucket != null && h.time == nowBucket,
    )

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
