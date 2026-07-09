package com.bykhavoy.ehat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bykhavoy.ehat.data.Constants
import com.bykhavoy.ehat.data.ForecastRepository
import com.bykhavoy.ehat.data.ForecastState
import com.bykhavoy.ehat.data.SettingsStore
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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

    // Default to Ивушка (the sea point).
    private val selectedTab = MutableStateFlow(1)

    val uiState: StateFlow<UiState> =
        combine(
            repository.state,
            settings.stepHours,
            settings.enabledColumns,
            settings.range,
            selectedTab,
        ) { state, step, colNames, range, tab ->
            build(state, step, colNames, range, tab)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        viewModelScope.launch {
            repository.loadCache()
            _cacheLoaded.value = true
            repository.refresh()
        }
        viewModelScope.launch {
            while (true) {
                delay(Constants.AUTO_REFRESH_MINUTES * 60_000)
                repository.refresh()
            }
        }
    }

    fun refresh() = viewModelScope.launch { repository.refresh() }
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
        val nowHour = now.truncatedTo(ChronoUnit.HOURS)
        val start = range?.first?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
        val end = range?.second?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
        val filtered = loc.hourly.filter { it.time.atZone(zone).hour % step == 0 }
        return filtered
            .groupBy { it.time.atZone(zone).toLocalDate() }
            .filter { (date, _) -> inRange(date, start, end) }
            .map { (date, points) ->
                val title = dayFmt.format(date.atStartOfDay(zone)).replaceFirstChar { it.uppercase(ru) }
                DaySection(title, points.map { row(it, nowHour) })
            }
    }

    private fun inRange(date: LocalDate, start: LocalDate?, end: LocalDate?): Boolean {
        if (start == null || end == null) return true
        return !date.isBefore(start) && !date.isAfter(end)
    }

    private fun row(h: HourlyPoint, nowHour: Instant): HourRow = HourRow(
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
        isNow = h.time.truncatedTo(ChronoUnit.HOURS) == nowHour,
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
