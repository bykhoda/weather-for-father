package com.bykhavoy.ehat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bykhavoy.ehat.data.Constants
import com.bykhavoy.ehat.data.net.ConnectivityObserver
import com.bykhavoy.ehat.ui.DayDetailScreen
import com.bykhavoy.ehat.ui.DebugScreen
import com.bykhavoy.ehat.ui.FiltersScreen
import com.bykhavoy.ehat.ui.HomeScreen
import com.bykhavoy.ehat.ui.MainViewModel
import com.bykhavoy.ehat.ui.MapScreen
import com.bykhavoy.ehat.ui.WaterScreen
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Card
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
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

private enum class Dest { WEATHER, MAP, WATER }

@Composable
private fun AppRoot(vm: MainViewModel) {
    var dest by remember { mutableStateOf(Dest.WEATHER) }
    var dayIndex by remember { mutableStateOf<Int?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var showDebug by remember { mutableStateOf(false) }
    val ui by vm.uiState.collectAsStateWithLifecycle()

    val canGoBack = showDebug || showFilters || dayIndex != null || dest != Dest.WEATHER
    BackHandler(enabled = canGoBack) {
        when {
            showDebug -> showDebug = false
            showFilters -> showFilters = false
            dayIndex != null -> dayIndex = null
            else -> dest = Dest.WEATHER
        }
    }

    Row(Modifier.fillMaxSize().background(Bg)) {
        NavigationRail(containerColor = Card) {
            Spacer(Modifier.height(8.dp))
            RailItem("📋", "Погода", dest == Dest.WEATHER && !showFilters) { dest = Dest.WEATHER; dayIndex = null; showFilters = false }
            RailItem("🗺", "Карта", dest == Dest.MAP && !showFilters) { dest = Dest.MAP; showFilters = false }
            RailItem("💧", "Вода", dest == Dest.WATER && !showFilters) { dest = Dest.WATER; showFilters = false }
            Spacer(Modifier.weight(1f))
            RailItem("⚙", "Фильтры", showFilters) { showFilters = true }
            Spacer(Modifier.height(8.dp))
        }

        Box(Modifier.weight(1f).fillMaxHeight()) {
            when {
                showDebug -> {
                    val d by vm.diagnostics.collectAsStateWithLifecycle()
                    DebugScreen(diagnostics = d, onRefresh = { vm.refresh() }, onBack = { showDebug = false })
                }
                showFilters -> FiltersScreen(
                    initialStep = ui.stepHours,
                    initialEnabled = ui.enabled,
                    initialStartMs = ui.rangeStartMs,
                    initialEndMs = ui.rangeEndMs,
                    hasSeaTemp = ui.hasSeaTemp,
                    hasWave = ui.hasWave,
                    onApply = { step, cols, s, e -> vm.applyFilters(step, cols, s, e) },
                    onClose = { showFilters = false },
                )
                dest == Dest.MAP -> {
                    val loc = Constants.LOCATIONS.getOrElse(ui.selectedTab) { Constants.SEA }
                    MapScreen(loc.lat, loc.lon, loc.name) { dest = Dest.WEATHER }
                }
                dest == Dest.WATER -> WaterScreen(Constants.LADA_WATER_URL) { dest = Dest.WEATHER }
                else -> {
                    val di = dayIndex
                    val day = di?.let { ui.days.getOrNull(it) }
                    if (di != null && day != null) {
                        DayDetailScreen(
                            day = day,
                            locationName = ui.tabs.getOrElse(ui.selectedTab) { "" },
                            hasSeaTemp = ui.hasSeaTemp,
                            hasWave = ui.hasWave,
                            enabled = ui.enabled,
                            onOpenFilters = { showFilters = true },
                            onBack = { dayIndex = null },
                        )
                    } else {
                        HomeScreen(
                            state = ui,
                            onSelectTab = { vm.selectTab(it) },
                            onOpenDay = { dayIndex = it },
                            onRefresh = { vm.refresh() },
                            onOpenDebug = { showDebug = true },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RailItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = { Text(icon, fontSize = 22.sp) },
        label = { Text(label, fontSize = 12.sp) },
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = Calm,
            selectedTextColor = Calm,
            indicatorColor = Calm.copy(alpha = 0.14f),
            unselectedIconColor = InkDim,
            unselectedTextColor = InkDim,
        ),
    )
}
