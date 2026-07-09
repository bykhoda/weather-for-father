package com.bykhavoy.ehat.ui

import com.bykhavoy.ehat.domain.WindStatus
import com.bykhavoy.ehat.domain.model.Factor

/** Everything MainScreen needs, precomputed in the ViewModel (no logic in @Composable). */
data class UiState(
    val phase: Phase = Phase.LOADING,
    val sceneStatus: WindStatus = WindStatus.CALM,
    val aktau: CardModel? = null,
    val dacha: CardModel? = null,
    val factors: List<Factor> = emptyList(),
    val bindingId: com.bykhavoy.ehat.domain.model.FactorId? = null,
    val histogram: List<HistogramBar> = emptyList(),
    val cta: CtaModel? = null,
    val freshness: Freshness? = null,
    val emptyLabel: String? = null,
) {
    enum class Phase { LOADING, CONTENT, EMPTY }
}

data class CardModel(
    val title: String,
    val subtitle: String?,      // e.g. "к приезду, ~14:00" (dacha only)
    val windMs: Int,
    val gustMs: Int,
    val windFromDeg: Float,
    val status: WindStatus,     // this location's OWN wind status (its number colour)
    val secondary: String?,     // e.g. "встречный · ориент. −7% хода" (dacha only)
)

data class HistogramBar(
    val gustMs: Float,
    val status: WindStatus,     // coloured by the hour's VERDICT, not just wind (spec §14.10)
    val present: Boolean,       // false = null hour, drawn as a gap (spec §13.8)
)

data class CtaModel(
    val headline: String,       // ДА / ДА, НО / НЕ СЕГОДНЯ
    val reason: String,
    val status: WindStatus,
)
