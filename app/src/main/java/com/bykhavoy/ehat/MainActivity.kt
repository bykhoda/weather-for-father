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
import com.bykhavoy.ehat.data.net.ConnectivityObserver
import com.bykhavoy.ehat.ui.DebugScreen
import com.bykhavoy.ehat.ui.MainScreen
import com.bykhavoy.ehat.ui.MainViewModel
import com.bykhavoy.ehat.ui.SettingsScreen
import com.bykhavoy.ehat.ui.theme.EhatTheme

/** The Activity. Landscape + splash are configured in the manifest / theme. */
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

        // Splash lives exactly as long as it takes to read the cache (~30 ms).
        // NEVER a fixed delay (spec §13.11).
        splash.setKeepOnScreenCondition { !vm.cacheLoaded.value }

        connectivity = ConnectivityObserver(this) { vm.refresh() }

        setContent { EhatTheme { AppRoot(vm) } }
    }

    override fun onStart() {
        super.onStart()
        connectivity.start()
    }

    override fun onStop() {
        super.onStop()
        connectivity.stop()
    }
}

private enum class Screen { MAIN, SETTINGS, DEBUG }

@Composable
private fun AppRoot(vm: MainViewModel) {
    var screen by remember { mutableStateOf(Screen.MAIN) }
    val ui by vm.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = screen != Screen.MAIN) { screen = Screen.MAIN }

    when (screen) {
        Screen.MAIN -> MainScreen(
            state = ui,
            onOpenSettings = { screen = Screen.SETTINGS },
            onOpenDebug = { screen = Screen.DEBUG },
            onRetry = { vm.refresh() },
            onCta = { vm.refresh() },
        )
        Screen.SETTINGS -> {
            val t by vm.thresholds.collectAsStateWithLifecycle()
            SettingsScreen(thresholds = t, onChange = { vm.updateThresholds(it) }, onBack = { screen = Screen.MAIN })
        }
        Screen.DEBUG -> {
            val d by vm.diagnostics.collectAsStateWithLifecycle()
            DebugScreen(diagnostics = d, onRefresh = { vm.refresh() }, onBack = { screen = Screen.MAIN })
        }
    }
}
