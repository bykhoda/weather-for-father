package com.bykhavoy.ehat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.data.net.FetchDiagnostics
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.HeadlineStyle
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim
import com.bykhavoy.ehat.ui.theme.LabelStyle

/**
 * Hidden diagnostics screen (spec §6, §13.10). The head unit likely has no
 * `adb logcat`, so this is the ONLY channel for technical truth: last success,
 * HTTP status, content-type, Date header, computed clock offset, retry count,
 * error, and the raw response body. Users never see this.
 */
@Composable
fun DebugScreen(
    diagnostics: FetchDiagnostics,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("‹", style = HeadlineStyle, color = InkDim, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("Диагностика", style = HeadlineStyle, color = Ink)
            Spacer(Modifier.weight(1f))
            Text("Обновить", style = LabelStyle, color = Calm, modifier = Modifier.clickable { onRefresh() })
        }
        Spacer(Modifier.height(16.dp))

        val d = diagnostics
        Line("Последний успех", d.lastSuccessAt?.toString() ?: "—")
        Line("HTTP-код", d.lastHttpCode?.toString() ?: "—")
        Line("Content-Type", d.lastContentType ?: "—")
        Line("Заголовок Date", d.lastServerDate?.toString() ?: "—")
        Line("Сдвиг часов, мс", d.clockOffsetMs.toString())
        Line("Тип ошибки", d.lastErrorType ?: "—")
        Line("Текст ошибки", d.lastErrorMessage ?: "—")
        Line("Попыток", d.attempts.toString())
        Line("Последняя попытка", d.lastAttemptAt?.toString() ?: "—")

        Spacer(Modifier.height(12.dp))
        Text("Сырой ответ (первые 4 КБ):", style = LabelStyle, color = InkDim)
        Spacer(Modifier.height(6.dp))
        Text(
            text = d.rawBodySnippet ?: "—",
            color = Ink,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().verticalScroll(scroll),
        )
    }
}

@Composable
private fun Line(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = LabelStyle, color = InkDim, modifier = Modifier.width(180.dp))
        Text(value, color = Ink, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
    }
}
