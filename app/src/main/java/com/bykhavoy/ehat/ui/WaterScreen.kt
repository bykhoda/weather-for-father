package com.bykhavoy.ehat.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.Stroke

/**
 * Real sea-water temperature by sensors, shown IN-APP via a WebView (Constants.
 * LADA_WATER_URL). WebView is a core Android component present even on the
 * head unit, so this does not depend on an external browser being installed.
 */
@Composable
fun WaterScreen(url: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", color = Calm, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Температура воды", color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("датчики · lada.kz", color = InkDim, fontSize = 13.sp)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Stroke))
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                @SuppressLint("SetJavaScriptEnabled")
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            },
        )
    }
}
