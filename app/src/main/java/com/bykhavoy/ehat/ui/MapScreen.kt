package com.bykhavoy.ehat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.ui.components.Chip
import com.bykhavoy.ehat.ui.components.WebPane
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.Stroke

private data class Overlay(val label: String, val id: String)

private val OVERLAYS = listOf(
    Overlay("Ветер", "wind"),
    Overlay("Осадки", "rain"),
    Overlay("Порывы", "gust"),
    Overlay("Температура", "temp"),
    Overlay("Облачность", "clouds"),
)

private fun windyUrl(lat: Double, lon: Double, overlay: String): String =
    "https://embed.windy.com/embed2.html?lat=$lat&lon=$lon&detailLat=$lat&detailLon=$lon" +
        "&zoom=8&level=surface&overlay=$overlay&marker=true&type=map&location=coordinates" +
        "&metricWind=m%2Fs&metricTemp=%C2%B0C&radarRange=-1"

@Composable
fun MapScreen(lat: Double, lon: Double, locationName: String, onBack: () -> Unit) {
    var overlay by remember { mutableStateOf("wind") }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", color = Calm, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(14.dp))
            Text("Карта · $locationName", color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OVERLAYS.forEach { o -> Chip(o.label, active = o.id == overlay) { overlay = o.id } }
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Stroke))

        WebPane(windyUrl(lat, lon, overlay), Modifier.fillMaxSize())
    }
}
