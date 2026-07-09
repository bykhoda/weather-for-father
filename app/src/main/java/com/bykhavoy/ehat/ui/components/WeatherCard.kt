package com.bykhavoy.ehat.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import com.bykhavoy.ehat.ui.CardModel
import com.bykhavoy.ehat.ui.statusGlyph
import com.bykhavoy.ehat.ui.statusLabel
import com.bykhavoy.ehat.ui.theme.CaptionStyle
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.LabelStyle
import com.bykhavoy.ehat.ui.theme.NumberStyle
import com.bykhavoy.ehat.ui.theme.UnitStyle
import com.bykhavoy.ehat.ui.theme.statusColor

/**
 * One location card (spec §6). The big number animates by SCROLLING
 * (animateIntAsState), never by swap, and uses tabular figures so 9 -> 11 does
 * not shift anything. The number keeps its OWN wind colour even when the scene
 * verdict is worse (spec §14.10).
 */
@Composable
fun WeatherCard(
    card: CardModel,
    modifier: Modifier = Modifier,
) {
    val wind by animateIntAsState(card.windMs, tween(400), label = "wind")
    val color = statusColor(card.status)

    Column(
        modifier = modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(card.title.uppercase(), style = LabelStyle, color = Ink)
        if (card.subtitle != null) {
            Text(card.subtitle, style = CaptionStyle, color = InkDim)
        }
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text("$wind", style = NumberStyle, color = color)
            Spacer(Modifier.width(6.dp))
            Text("м/с", style = UnitStyle, color = InkDim, modifier = Modifier.padding(bottom = 16.dp))
        }

        Text("порывы до ${card.gustMs}", style = CaptionStyle, color = InkDim)
        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            WindArrow(card.windFromDeg, color, Modifier.size(28.dp))
            Spacer(Modifier.width(6.dp))
            Text(compassFrom(card.windFromDeg), style = CaptionStyle, color = InkDim)
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(statusGlyph(card.status), style = LabelStyle, color = color)
            Spacer(Modifier.width(8.dp))
            Text(statusLabel(card.status), style = LabelStyle, color = Ink, fontWeight = FontWeight.Bold)
        }

        if (card.secondary != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                card.secondary,
                style = CaptionStyle,
                color = InkDim,
                textAlign = TextAlign.Center,
            )
        }
    }
}
