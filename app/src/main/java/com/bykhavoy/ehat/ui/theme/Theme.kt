package com.bykhavoy.ehat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Light theme to suit the white cabin (spec direction changed from dark). */
private val EhatColors = lightColorScheme(
    primary = Calm,
    background = Bg,
    surface = Card,
    onPrimary = Card,
    onBackground = Ink,
    onSurface = Ink,
)

@Composable
fun EhatTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EhatColors, content = content)
}
