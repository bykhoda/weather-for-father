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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bykhavoy.ehat.ui.theme.Bg
import com.bykhavoy.ehat.ui.components.BackButton
import com.bykhavoy.ehat.ui.theme.Calm
import com.bykhavoy.ehat.ui.theme.Ink
import com.bykhavoy.ehat.ui.theme.InkDim

private class PlaceRow(name: String, lat: String, lon: String) {
    var name by mutableStateOf(name)
    var lat by mutableStateOf(lat)
    var lon by mutableStateOf(lon)
}

private fun PlaceRow.toPlaceUi(): PlaceUi? {
    val n = name.trim()
    val la = lat.replace(',', '.').toDoubleOrNull()
    val lo = lon.replace(',', '.').toDoubleOrNull()
    return if (n.isNotBlank() && la != null && lo != null) PlaceUi(n, la, lo) else null
}

private fun trimNum(d: Double): String = if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()

/** Add/edit any number of forecast points. Used both as onboarding and as an editor. */
@Composable
fun PlacesScreen(
    initial: List<PlaceUi>,
    isOnboarding: Boolean,
    onDone: (List<PlaceUi>) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val rows = remember {
        mutableStateListOf<PlaceRow>().apply {
            addAll(initial.map { PlaceRow(it.name, trimNum(it.lat), trimNum(it.lon)) })
            if (isEmpty()) add(PlaceRow("", "", ""))
        }
    }

    Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                BackButton(onBack)
                Spacer(Modifier.width(12.dp))
            }
            Text(if (isOnboarding) "Ваши места" else "Мои места", color = Ink, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Добавьте места, погоду которых хотите видеть. Координаты — широта и долгота " +
                "(в 2GIS: долгий тап по точке → «Что здесь»).",
            color = InkDim, fontSize = 14.sp,
        )
        Spacer(Modifier.height(18.dp))

        rows.forEachIndexed { i, r ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(r.name, { r.name = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.weight(1.4f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(r.lat, { r.lat = it }, label = { Text("Широта") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(r.lon, { r.lon = it }, label = { Text("Долгота") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                Spacer(Modifier.width(6.dp))
                Text(
                    "✕",
                    color = if (rows.size > 1) Color(0xFFE0553C) else Color(0x22000000),
                    fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.clickable { if (rows.size > 1) rows.removeAt(i) }.padding(6.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "＋ Добавить место",
            color = Calm, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
            modifier = Modifier.clickable { rows.add(PlaceRow("", "", "")) }.padding(vertical = 8.dp),
        )

        Spacer(Modifier.height(20.dp))
        val valid = rows.mapNotNull { it.toPlaceUi() }
        Text(
            if (isOnboarding) "Готово" else "Сохранить",
            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (valid.isEmpty()) Color(0x33000000) else Calm)
                .clickable { if (valid.isNotEmpty()) onDone(valid) }
                .padding(horizontal = 32.dp, vertical = 14.dp),
        )
        Spacer(Modifier.height(24.dp))
    }
}
