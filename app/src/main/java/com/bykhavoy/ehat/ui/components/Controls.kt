package com.bykhavoy.ehat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.R
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim

/** iOS-style segmented control (light). */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x0F000000))
            .padding(3.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selected
            Text(
                label,
                color = if (active) Color.White else Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) Calm else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

/** Location selector — rounded pill chips with a pin, weather-app style. */
@Composable
fun LocationTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selected
            Text(
                "📍 $label",
                color = if (active) Color.White else Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (active) Calm else Color(0x0A000000))
                    .then(if (active) Modifier else Modifier.border(1.dp, Color(0x14000000), RoundedCornerShape(20.dp)))
                    .clickable { onSelect(i) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            )
        }
    }
}

/** Линейная иконка с тинтом темы (адаптируется под свет/тьму). */
@Composable
fun AppIcon(res: Int, modifier: Modifier = Modifier, tint: Color = Ink, size: Dp = 22.dp, desc: String? = null) {
    Icon(painterResource(res), contentDescription = desc, tint = tint, modifier = modifier.size(size))
}

/** Кликабельная иконка с круглой тап-целью. */
@Composable
fun IconAction(res: Int, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Ink, size: Dp = 22.dp, pad: Dp = 8.dp, desc: String? = null) {
    Icon(
        painterResource(res), contentDescription = desc, tint = tint,
        modifier = modifier.clip(CircleShape).clickable { onClick() }.padding(pad).size(size),
    )
}

/** Единая кнопка «назад» — чистая линейная стрелка. */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconAction(R.drawable.ic_back, onClick = onClick, modifier = modifier, tint = Calm, size = 24.dp, desc = "Назад")
}

/** Основная (акцентная) кнопка. */
@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(14.dp)).background(Calm).clickable { onClick() }.padding(horizontal = 28.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

/** Вторичная (тихая) кнопка. */
@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color(0x0F000000)).clickable { onClick() }.padding(horizontal = 24.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

/** Toggle chip for filters / metric switcher. */
@Composable
fun Chip(label: String, active: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (active) Color.White else Ink,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) Calm else Color(0x0F000000))
            .then(if (active) Modifier else Modifier.border(1.dp, Color(0x14000000), RoundedCornerShape(20.dp)))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
