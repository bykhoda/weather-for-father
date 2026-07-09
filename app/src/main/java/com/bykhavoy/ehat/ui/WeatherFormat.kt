package com.bykhavoy.ehat.ui

import androidx.compose.ui.graphics.Color
import com.bykhavoy.ehat.ui.theme.Ink

/** Small presentation helpers for the forecast table (light theme). */
object WeatherFormat {

    /** Compact sky glyph from a WMO weather code + day/night. */
    fun skyGlyph(code: Int?, isDay: Boolean): String = when (code) {
        null -> "·"
        0 -> if (isDay) "☀" else "☾"
        1, 2 -> if (isDay) "⛅" else "☁"
        3 -> "☁"
        45, 48 -> "🌫"
        in 51..57 -> "🌦"
        in 61..67 -> "🌧"
        in 71..77 -> "❄"
        in 80..82 -> "🌧"
        in 85..86 -> "❄"
        in 95..99 -> "⛈"
        else -> "·"
    }

    /** Temperature colour readable on a light background: cool blue → warm coral. */
    fun tempColor(t: Int?): Color = when {
        t == null -> Ink
        t <= 0 -> Color(0xFF2E7DD1)
        t <= 12 -> Color(0xFF4E93C9)
        t <= 22 -> Ink
        t <= 30 -> Color(0xFFD9812A)
        else -> Color(0xFFD8492E)
    }
}
