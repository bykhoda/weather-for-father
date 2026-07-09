package com.bykhavoy.ehat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bykhavoy.ehat.ui.CtaModel
import com.bykhavoy.ehat.ui.statusGlyph
import com.bykhavoy.ehat.ui.theme.HeadlineStyle
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.LabelStyle
import com.bykhavoy.ehat.ui.theme.Stroke
import com.bykhavoy.ehat.ui.theme.statusColor

/**
 * The button the app exists to be (spec §5.4, §14.10): «ПОЕХАТЬ?» + a one-line
 * answer. Colour follows the VERDICT. Press scales to 0.985 (tween 120) — the
 * only motion here; no pulse, no shimmer (spec §12.7).
 */
@Composable
fun CtaButton(
    cta: CtaModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, tween(120), label = "cta")
    val color = statusColor(cta.status)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, Stroke, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 28.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ПОЕХАТЬ?", style = LabelStyle, color = Ink)
            Spacer(Modifier.width(16.dp))
            Text(statusGlyph(cta.status), style = HeadlineStyle, color = color)
            Spacer(Modifier.width(10.dp))
            Text(cta.headline, style = HeadlineStyle, color = color)
        }
        Spacer(Modifier.height(4.dp))
        Text(cta.reason, style = LabelStyle, color = Ink)
    }
}
