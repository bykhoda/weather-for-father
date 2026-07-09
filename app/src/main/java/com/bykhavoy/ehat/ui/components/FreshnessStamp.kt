package com.bykhavoy.ehat.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bykhavoy.ehat.ui.FreshLevel
import com.bykhavoy.ehat.ui.Freshness
import com.bykhavoy.ehat.ui.theme.CaptionStyle
import com.bykhavoy.ehat.ui.theme.Harsh
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.Windy

/**
 * The data-freshness stamp (spec §6, §13.9). Always visible. Stale data changes
 * its colour and wording; it is NEVER a dialog or toast. This is the only way a
 * network error is communicated to the user.
 */
@Composable
fun FreshnessStamp(
    freshness: Freshness,
    modifier: Modifier = Modifier,
) {
    val color = when (freshness.level) {
        FreshLevel.FRESH -> InkDim
        FreshLevel.STALE -> Windy
        FreshLevel.VERY_STALE -> Harsh
    }
    Text(freshness.text, style = CaptionStyle, color = color, modifier = modifier)
}
