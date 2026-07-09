package com.bykhavoy.ehat.data.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Silent auto-recovery (spec §13.7). ConnectivityManager works without Google
 * Play Services, so it is safe on the head unit. When the car leaves a dead
 * zone, [onAvailable] fires and the repository refreshes — no UI, the user just
 * notices the data is always fresh. Unregister in onStop().
 */
class ConnectivityObserver(
    context: Context,
    private val onAvailable: () -> Unit,
) {
    private val cm = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = onAvailable()
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching { cm.registerNetworkCallback(request, callback) }
    }

    fun stop() {
        runCatching { cm.unregisterNetworkCallback(callback) }
    }
}
