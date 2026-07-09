package com.bykhavoy.ehat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Ink

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
