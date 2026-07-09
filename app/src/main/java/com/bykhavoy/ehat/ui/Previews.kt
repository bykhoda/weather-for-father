package com.bykhavoy.ehat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bykhavoy.ehat.domain.WindStatus
import com.bykhavoy.ehat.domain.model.Factor
import com.bykhavoy.ehat.domain.model.FactorId
import com.bykhavoy.ehat.ui.theme.EhatTheme

/** Landscape preview device profile matching the head unit (spec §2). */
private const val W = 960
private const val H = 540

private fun sampleFactors(binding: FactorId, bindingStatus: WindStatus): List<Factor> = listOf(
    Factor(FactorId.PRECIP, 5.0, "%", true, WindStatus.CALM, "осадки 5%", "без осадков"),
    Factor(FactorId.DUST, 30.0, "мкг/м³", true, WindStatus.CALM, "PM10 30", "воздух чистый"),
    Factor(FactorId.WIND, 15.0, "м/с", true, if (binding == FactorId.WIND) bindingStatus else WindStatus.WINDY, "порывы 15 м/с", "порывы до 15, на пляже неуютно"),
    Factor(FactorId.HEAT, 34.0, "°", true, WindStatus.CALM, "ощущается 34°", "34°, комфортно"),
    Factor(FactorId.UV, 9.0, "", true, if (binding == FactorId.UV) bindingStatus else WindStatus.WINDY, "УФ 9", "УФ 9 — обгоришь за полчаса"),
).map { if (it.id == binding) it.copy(status = bindingStatus) else it }

private fun sampleState(status: WindStatus, binding: FactorId): UiState {
    val headline = when (status) {
        WindStatus.CALM -> "ДА"
        WindStatus.WINDY -> "ДА, НО"
        WindStatus.HARSH -> "НЕ СЕГОДНЯ"
    }
    val reason = when (status) {
        WindStatus.CALM -> "Всё спокойно, можно ехать"
        WindStatus.WINDY -> "Порывы до 11, на пляже неуютно"
        WindStatus.HARSH -> "УФ 9 — обгоришь за полчаса · сложится завтра после обеда"
    }
    return UiState(
        phase = UiState.Phase.CONTENT,
        sceneStatus = status,
        aktau = CardModel("Актау", null, 6, 9, 315f, WindStatus.CALM, null),
        dacha = CardModel("Дача", "к приезду, ~14:00", 11, 15, 320f, WindStatus.WINDY, "встречный · ориент. −7% хода"),
        factors = sampleFactors(binding, status),
        bindingId = binding,
        histogram = List(72) { i ->
            HistogramBar(
                gustMs = (6 + (i % 12)).toFloat(),
                status = if (i % 12 > 8) WindStatus.HARSH else if (i % 12 > 5) WindStatus.WINDY else WindStatus.CALM,
                present = i != 30, // one gap to show null handling
            )
        },
        cta = CtaModel(headline, reason, status),
        freshness = Freshness("обновлено 4 минуты назад", FreshLevel.FRESH),
    )
}

private val noop: () -> Unit = {}

@Preview(name = "CALM", widthDp = W, heightDp = H, showBackground = true)
@Composable
private fun PreviewCalm() = EhatTheme {
    MainScreen(sampleState(WindStatus.CALM, FactorId.WIND), noop, noop, noop, noop)
}

@Preview(name = "WINDY", widthDp = W, heightDp = H, showBackground = true)
@Composable
private fun PreviewWindy() = EhatTheme {
    MainScreen(sampleState(WindStatus.WINDY, FactorId.WIND), noop, noop, noop, noop)
}

@Preview(name = "HARSH (UV binding, calm wind)", widthDp = W, heightDp = H, showBackground = true)
@Composable
private fun PreviewHarsh() = EhatTheme {
    MainScreen(sampleState(WindStatus.HARSH, FactorId.UV), noop, noop, noop, noop)
}
