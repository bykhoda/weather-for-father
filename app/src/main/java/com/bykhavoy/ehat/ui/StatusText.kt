package com.bykhavoy.ehat.ui

import com.bykhavoy.ehat.domain.WindStatus

/** Alpha for non-binding factor chips (spec §14.10). */
const val CaptionOn = 0.55f

/** Text label for a status (spec §5.1). Colour is never the sole carrier. */
fun statusLabel(status: WindStatus): String = when (status) {
    WindStatus.CALM -> "НОРМАЛЬНО"
    WindStatus.WINDY -> "ВЕТРЕНО"
    WindStatus.HARSH -> "СИДИ ДОМА"
}

/** Distinct glyph per status — a second, colour-independent channel. */
fun statusGlyph(status: WindStatus): String = when (status) {
    WindStatus.CALM -> "●"
    WindStatus.WINDY -> "▲"
    WindStatus.HARSH -> "■"
}
