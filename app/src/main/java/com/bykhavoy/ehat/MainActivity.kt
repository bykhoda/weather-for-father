package com.bykhavoy.ehat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bykhavoy.ehat.data.Constants
import com.bykhavoy.ehat.data.net.ConnectivityObserver
import com.bykhavoy.ehat.ui.DayDetailScreen
import com.bykhavoy.ehat.ui.DebugScreen
import com.bykhavoy.ehat.ui.FiltersScreen
import com.bykhavoy.ehat.ui.HomeScreen
import com.bykhavoy.ehat.ui.MainViewModel
import com.bykhavoy.ehat.ui.WaterScreen
import com.bykhavoy.ehat.ui.theme.EhatTheme

class MainActivity : ComponentActivity() {

    private lateinit var connectivity: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        val graph = (application as EhatApp).graph
        val vm = ViewModelProvider(
            this,
            MainViewModel.Factory(graph.repository, graph.settings, graph.clock, graph.diagnostics),
        )[MainViewModel::class.java]

        splash.setKeepOnScreenCondition { !vm.cacheLoaded.value }
        connectivity = ConnectivityObserver(this) { vm.refresh() }

        setContent { EhatTheme { AppRoot(vm) } }
    }

    override fun onStart() { super.onStart(); connectivity.start() }
    override fun onStop() { super.onStop(); connectivity.stop() }
}

private enum class Screen { HOME, DETAIL, FILTERS, WATER, DEBUG }

@Composable
private fun AppRoot(vm: MainViewModel) {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var dayIndex by remember { mutableStateOf(0) }
    // Where Filters was opened from, so applying/closing returns there.
    var filtersFrom by remember { mutableStateOf(Screen.HOME) }
    val ui by vm.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = screen != Screen.HOME) {
        screen = if (screen == Screen.FILTERS) filtersFrom else Screen.HOME
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            state = ui,
            onSelectTab = { vm.selectTab(it) },
            onOpenDay = { dayIndex = it; screen = Screen.DETAIL },
            onOpenFilters = { filtersFrom = Screen.HOME; screen = Screen.FILTERS },
            onOpenWater = { screen = Screen.WATER },
            onRefresh = { vm.refresh() },
            onOpenDebug = { screen = Screen.DEBUG },
        )
        Screen.DETAIL -> {
            val day = ui.days.getOrNull(dayIndex)
            if (day == null) {
                screen = Screen.HOME
            } else {
                DayDetailScreen(
                    day = day,
                    locationName = ui.tabs.getOrElse(ui.selectedTab) { "" },
                    hasSeaTemp = ui.hasSeaTemp,
                    hasWave = ui.hasWave,
                    enabled = ui.enabled,
                    onOpenFilters = { filtersFrom = Screen.DETAIL; screen = Screen.FILTERS },
                    onBack = { screen = Screen.HOME },
                )
            }
        }
        Screen.FILTERS -> FiltersScreen(
            initialStep = ui.stepHours,
            initialEnabled = ui.enabled,
            initialStartMs = ui.rangeStartMs,
            initialEndMs = ui.rangeEndMs,
            hasSeaTemp = ui.hasSeaTemp,
            hasWave = ui.hasWave,
            onApply = { step, cols, s, e -> vm.applyFilters(step, cols, s, e) },
            onClose = { screen = filtersFrom },
        )
        Screen.WATER -> WaterScreen(url = Constants.LADA_WATER_URL, onBack = { screen = Screen.HOME })
        Screen.DEBUG -> {
            val d by vm.diagnostics.collectAsStateWithLifecycle()
            DebugScreen(diagnostics = d, onRefresh = { vm.refresh() }, onBack = { screen = Screen.HOME })
        }
    }
}
