package com.bykhavoy.ehat.domain

import com.bykhavoy.ehat.domain.model.Thresholds
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalmWindowTest {

    private val t = Thresholds.DEFAULT
    private val start = TestData.aqtau(2025, 7, 1, 0)

    private fun series(gusts: List<Double>) = gusts.mapIndexed { i, g ->
        TestData.hour(start.plus(Duration.ofHours(i.toLong())), gust = g)
    }

    @Test fun finds_first_window_of_three_calm_hours() {
        // gusts: harsh, harsh, calm, calm, calm, harsh  -> window starts at index 2
        val hourly = series(listOf(16.0, 16.0, 3.0, 3.0, 3.0, 16.0))
        val at = CalmWindow.firstCalmWindow(hourly, t)
        assertEquals(hourly[2].time, at)
    }

    @Test fun returns_null_when_no_window() {
        val hourly = series(listOf(16.0, 3.0, 16.0, 3.0, 3.0)) // never 3 calm in a row
        assertNull(CalmWindow.firstCalmWindow(hourly, t))
    }

    @Test fun short_series_returns_null() {
        assertNull(CalmWindow.firstCalmWindow(series(listOf(3.0, 3.0)), t))
    }
}
