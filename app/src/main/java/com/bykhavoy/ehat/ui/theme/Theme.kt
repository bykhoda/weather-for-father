package com.bykhavoy.ehat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** Dark only (spec §6, §12.1). No light theme, no theme toggle. */
private val EhatColors = darkColorScheme(
    primary = Calm,
    background = Bg,
    surface = Bg,
    onPrimary = Bg,
    onBackground = Ink,
    onSurface = Ink,
)

@Composable
fun EhatTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = EhatColors, content = content)
}
