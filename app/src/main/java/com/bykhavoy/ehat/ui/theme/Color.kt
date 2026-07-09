package com.bykhavoy.ehat.ui.theme

import androidx.compose.ui.graphics.Color
import com.bykhavoy.ehat.domain.WindStatus

// Light, iOS-style palette to suit the Denza's white cabin. Soft teal accent.
val Calm = Color(0xFF12B3A6)   // soft teal accent
val Windy = Color(0xFFE0952A)  // amber
val Harsh = Color(0xFFE0553C)  // coral

val Bg = Color(0xFFF4F5F7)      // off-white background
val Card = Color(0xFFFFFFFF)    // white surface for rows/cards
val Surface = Color(0x0A000000) // ~4% black overlay
val Stroke = Color(0x14000000)  // hairline / separators
val Ink = Color(0xFF1B1D21)     // near-black text
val InkDim = Color(0xFF7C828B)  // secondary grey

fun statusColor(status: WindStatus): Color = when (status) {
    WindStatus.CALM -> Calm
    WindStatus.WINDY -> Windy
    WindStatus.HARSH -> Harsh
}
