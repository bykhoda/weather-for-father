package com.bykhavoy.ehat.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Typography (spec §12.3). Rules that are not negotiable:
 *  - `tnum` (tabular figures) so 9 -> 11 does not shift the layout.
 *  - Minimum weight Bold: the head-unit screen sits under direct sun; anything
 *    thinner than SemiBold physically disappears. NEVER FontWeight.Light.
 */
val NumberStyle = TextStyle(
    fontSize = 96.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 88.sp,
    letterSpacing = (-0.03).em,
    fontFeatureSettings = "tnum",
)

val NumberStyleSmall = TextStyle(
    fontSize = 64.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 60.sp,
    letterSpacing = (-0.02).em,
    fontFeatureSettings = "tnum",
)

val UnitStyle = TextStyle(
    fontSize = 22.sp,
    fontWeight = FontWeight.SemiBold,
)

val LabelStyle = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.SemiBold,
    fontFeatureSettings = "tnum",
)

val CaptionStyle = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight.SemiBold,
    fontFeatureSettings = "tnum",
)

val HeadlineStyle = TextStyle(
    fontSize = 40.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = (-0.01).em,
)
