package com.bykhavoy.ehat.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bykhavoy.ehat.ui.theme.Calm

@Composable
fun WebPane(url: String, modifier: Modifier = Modifier) {
    var loading by remember { mutableStateOf(true) }
    val loaded = remember { mutableStateOf(url) }

    Box(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                @SuppressLint("SetJavaScriptEnabled")
                WebView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.WHITE)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(v: WebView?, u: String?, f: Bitmap?) { loading = true }
                        override fun onPageFinished(v: WebView?, u: String?) { loading = false }
                    }
                    loadUrl(url)
                }
            },
            update = { wv -> if (loaded.value != url) { loaded.value = url; wv.loadUrl(url) } },
        )
        if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Calm)
    }
}

@Composable
fun WebPaneHtml(html: String, modifier: Modifier = Modifier) {
    var loading by remember { mutableStateOf(true) }
    val loaded = remember { mutableStateOf("") }
    Box(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                @SuppressLint("SetJavaScriptEnabled")
                WebView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.WHITE)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(v: WebView?, u: String?, f: Bitmap?) { loading = true }
                        override fun onPageFinished(v: WebView?, u: String?) { loading = false }
                    }
                    loadDataWithBaseURL("https://openweathermap.org", html, "text/html", "utf-8", null)
                    loaded.value = html
                }
            },
            update = { wv ->
                if (loaded.value != html) {
                    loaded.value = html
                    wv.loadDataWithBaseURL("https://openweathermap.org", html, "text/html", "utf-8", null)
                }
            },
        )
        if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Calm)
    }
}
