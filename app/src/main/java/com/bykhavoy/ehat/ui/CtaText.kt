package com.bykhavoy.ehat.ui

import com.bykhavoy.ehat.domain.FactorEngine
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Humanizes the calm-window instant for the "НЕ СЕГОДНЯ" reason (spec §14.9). */
object CtaText {

    fun calmWindowPhrase(window: Instant?, now: Instant): String {
        if (window == null) return "в ближайшие 3 дня не сложится"
        val z = window.atZone(FactorEngine.AQTAU)
        val today = now.atZone(FactorEngine.AQTAU).toLocalDate()
        val dayWord = when (ChronoUnit.DAYS.between(today, z.toLocalDate())) {
            0L -> "сегодня"
            1L -> "завтра"
            2L -> "послезавтра"
            else -> "в ${weekday(z.toLocalDate())}"
        }
        return "сложится $dayWord ${partOfDay(z.hour)}"
    }

    private fun partOfDay(hour: Int): String = when (hour) {
        in 5..10 -> "утром"
        in 11..16 -> "после обеда"
        in 17..21 -> "вечером"
        else -> "ночью"
    }

    private fun weekday(date: LocalDate): String = when (date.dayOfWeek.value) {
        1 -> "понедельник"
        2 -> "вторник"
        3 -> "среду"
        4 -> "четверг"
        5 -> "пятницу"
        6 -> "субботу"
        else -> "воскресенье"
    }
}
