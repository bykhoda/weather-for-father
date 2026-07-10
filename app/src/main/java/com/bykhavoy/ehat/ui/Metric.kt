package com.bykhavoy.ehat.ui

import androidx.compose.ui.graphics.Color

/** Metrics selectable on the day-detail chart (Apple-Weather-style switcher). */
enum class Metric(val label: String, val unit: String, val color: Color) {
    TEMP("Температура", "°C", Color(0xFFE0864B)),
    WIND("Ветер", " м/с", Color(0xFF12B3A6)),
    GUST("Порывы", " м/с", Color(0xFFE0952A)),
    HUMIDITY("Влажность", "%", Color(0xFF4E93C9)),
    PRECIP("Осадки", "%", Color(0xFF5AA9D6)),
    SEA("Вода", "°C", Color(0xFF2FA9BC));

    fun value(row: HourRow): Float? = when (this) {
        TEMP -> row.tempC?.toFloat()
        WIND -> row.windMs?.toFloat()
        GUST -> row.gustMs?.toFloat()
        HUMIDITY -> row.humidityPct?.toFloat()
        PRECIP -> row.precipPct?.toFloat()
        SEA -> row.seaTempC?.toFloat()
    }

    val sea: Boolean get() = this == SEA
}
