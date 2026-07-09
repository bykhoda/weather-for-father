package com.bykhavoy.ehat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.bykhavoy.ehat.domain.model.Factor
import com.bykhavoy.ehat.domain.model.FactorId
import com.bykhavoy.ehat.ui.CaptionOn
import com.bykhavoy.ehat.ui.theme.CaptionStyle
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.statusColor

/**
 * Factor strip (spec §14.10). Fixed PRIORITY order (never re-sorted, so muscle
 * memory forms). Irrelevant factors are ABSENT, not greyed. Exactly one chip —
 * the binding — is enlarged and shows its value; the rest are small status dots.
 * Tapping any chip expands its value for 3 seconds, then collapses. No detail
 * screen.
 */
@Composable
fun FactorStrip(
    factors: List<Factor>,
    bindingId: FactorId?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        factors.filter { it.relevant }.forEach { f ->
            FactorChip(f, expanded = f.id == bindingId)
        }
    }
}

@Composable
private fun FactorChip(factor: Factor, expanded: Boolean) {
    var tapExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(tapExpanded) {
        if (tapExpanded) {
            kotlinx.coroutines.delay(3_000)
            tapExpanded = false
        }
    }
    val color = statusColor(factor.status)
    val showValue = expanded || tapExpanded

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { tapExpanded = true }.padding(4.dp),
    ) {
        // Status dot — a colour-independent size cue for the binding.
        Text(
            "●",
            style = CaptionStyle,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.size(if (expanded) 20.dp else 14.dp),
        )
        if (showValue) {
            Spacer(Modifier.width(6.dp))
            Text(
                factor.shortLabel.ifEmpty { factorName(factor.id) },
                style = CaptionStyle,
                color = Ink.copy(alpha = if (expanded) 1f else CaptionOn),
                fontWeight = if (expanded) FontWeight.Bold else FontWeight.SemiBold,
            )
        }
    }
}

fun factorName(id: FactorId): String = when (id) {
    FactorId.WIND -> "ветер"
    FactorId.HEAT -> "жара"
    FactorId.UV -> "УФ"
    FactorId.PRECIP -> "осадки"
    FactorId.DUST -> "пыль"
    FactorId.SEA -> "море"
}
