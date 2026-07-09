package com.bykhavoy.ehat.ui.theme

import androidx.compose.ui.graphics.Color
import com.bykhavoy.ehat.domain.WindStatus

// Design tokens (spec §12.2). The status palette is deliberately teal ↔ amber ↔
// coral, NOT red/green: the target user is a man 55+, and red-green is the worst
// possible pair for protanopia/deuteranopia. This trio stays distinct under all
// color-blindness types. Colour is NEVER the sole carrier of status — always
// paired with text and an icon (spec §12.2, §5.1).
val Calm = Color(0xFF2DD4BF)   // teal
val Windy = Color(0xFFFBBF24)  // amber
val Harsh = Color(0xFFFB6B4B)  // coral

val Bg = Color(0xFF0B1015)      // NOT pure black: pure black smears grey on IPS
val Surface = Color(0x09FFFFFF) // 3.5% white
val Stroke = Color(0x17FFFFFF)
val Ink = Color(0xFFF2F5F7)
val InkDim = Color(0xFF7C8A94)

fun statusColor(status: WindStatus): Color = when (status) {
    WindStatus.CALM -> Calm
    WindStatus.WINDY -> Windy
    WindStatus.HARSH -> Harsh
}
