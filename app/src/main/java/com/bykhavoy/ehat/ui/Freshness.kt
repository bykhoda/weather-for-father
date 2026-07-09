package com.bykhavoy.ehat.ui

import com.bykhavoy.ehat.domain.FactorEngine
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

/** Freshness stamp model + rules (spec §6, §13.9). Always visible; never a dialog. */
enum class FreshLevel { FRESH, STALE, VERY_STALE }

data class Freshness(val text: String, val level: FreshLevel)

private val hhmm = DateTimeFormatter.ofPattern("HH:mm")

fun freshnessOf(fetchedAt: Instant, now: Instant): Freshness {
    val age = Duration.between(fetchedAt, now)
    val stampTime = fetchedAt.atZone(FactorEngine.AQTAU).format(hhmm)
    return when {
        age > Duration.ofHours(12) ->
            Freshness("Данные устарели. Нет связи с $stampTime", FreshLevel.VERY_STALE)
        age > Duration.ofHours(3) ->
            Freshness("Нет связи. Данные от $stampTime", FreshLevel.STALE)
        else -> {
            val mins = age.toMinutes().coerceAtLeast(0)
            val text = if (mins < 1) "обновлено только что" else "обновлено ${agoMinutes(mins)}"
            Freshness(text, FreshLevel.FRESH)
        }
    }
}

private fun agoMinutes(mins: Long): String {
    val n = mins % 100
    val unit = when {
        n in 11..14 -> "минут"
        mins % 10 == 1L -> "минуту"
        mins % 10 in 2..4 -> "минуты"
        else -> "минут"
    }
    return "$mins $unit назад"
}
