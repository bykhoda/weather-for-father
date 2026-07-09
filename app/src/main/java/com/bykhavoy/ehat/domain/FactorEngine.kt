package com.bykhavoy.ehat.domain

import com.bykhavoy.ehat.domain.WindStatus.CALM
import com.bykhavoy.ehat.domain.WindStatus.HARSH
import com.bykhavoy.ehat.domain.WindStatus.WINDY
import com.bykhavoy.ehat.domain.model.Factor
import com.bykhavoy.ehat.domain.model.FactorId
import com.bykhavoy.ehat.domain.model.HourlyPoint
import com.bykhavoy.ehat.domain.model.PRIORITY
import com.bykhavoy.ehat.domain.model.Thresholds
import com.bykhavoy.ehat.domain.model.Verdict
import java.time.Month
import java.time.ZoneId
import kotlin.math.roundToInt

/**
 * Turns an [HourlyPoint] into the six factors and a single [Verdict]
 * (spec §14). This is the heart of the "binding constraint" model: find the one
 * factor that decides the day and name it, instead of dumping a table.
 */
object FactorEngine {

    /** Asia/Aqtau, UTC+5, no DST (spec §4). Never trust the device TZ. */
    val AQTAU: ZoneId = ZoneId.of("Asia/Aqtau")

    private fun i(v: Double): String = v.roundToInt().toString()
    private fun d1(v: Double): String = ((v * 10).roundToInt() / 10.0).toString()

    // --- Seasonal / time-of-day relevance (spec §14.5) -------------------------
    private fun seasonallyRelevant(id: FactorId, hour: HourlyPoint, month: Month): Boolean =
        when (id) {
            FactorId.WIND, FactorId.HEAT, FactorId.PRECIP, FactorId.DUST -> true
            FactorId.UV -> hour.isDay // UV at 21:00 is 0 — noise, not information
            FactorId.SEA -> month in Month.MAY..Month.SEPTEMBER // nobody swims the Caspian in winter
        }

    /** Build all six factors for one hour, ordered by [PRIORITY]. */
    fun buildFactors(hour: HourlyPoint, month: Month, t: Thresholds): List<Factor> =
        PRIORITY.map { id -> buildFactor(id, hour, month, t) }

    private fun buildFactor(id: FactorId, hour: HourlyPoint, month: Month, t: Thresholds): Factor {
        val seasonal = seasonallyRelevant(id, hour, month)
        return when (id) {
            FactorId.WIND -> {
                val v = hour.gustMs
                factor(id, v, "м/с", seasonal && v != null,
                    status = v?.let { StatusMapper.windStatus(it, t) }) { st ->
                    when (st) {
                        CALM -> Triple("ветер ${i(v!!)} м/с", "ветер ${i(v)} м/с, спокойно", "порывы ${i(v)} м/с")
                        WINDY -> Triple("порывы ${i(v!!)} м/с", "порывы до ${i(v)}, на пляже неуютно", "порывы ${i(v)} м/с")
                        HARSH -> Triple("порывы ${i(v!!)} м/с", "порывы до ${i(v)}, сдует", "порывы ${i(v)} м/с")
                    }
                }
            }
            FactorId.HEAT -> {
                val v = hour.apparentTempC
                factor(id, v, "°", seasonal && v != null,
                    status = v?.let { StatusMapper.heatStatus(it, t) }) { st ->
                    val cold = v != null && v < t.heatColdWindyC
                    when (st) {
                        CALM -> Triple("ощущается ${i(v!!)}°", "${i(v)}°, комфортно", "${i(v)}°")
                        WINDY -> if (cold) Triple("ощущается ${i(v!!)}°", "${i(v)}°, прохладно", "${i(v)}°")
                        else Triple("ощущается ${i(v!!)}°", "${i(v)}°, жарковато", "${i(v)}°")
                        HARSH -> if (cold) Triple("ощущается ${i(v!!)}°", "холодно, ${i(v)}°", "${i(v)}°")
                        else Triple("ощущается ${i(v!!)}°", "жара ${i(v)}°, пекло", "${i(v)}°")
                    }
                }
            }
            FactorId.UV -> {
                val v = hour.uvIndex
                factor(id, v, "", seasonal && v != null,
                    status = v?.let { StatusMapper.uvStatus(it, t) }) { st ->
                    when (st) {
                        CALM -> Triple("УФ ${i(v!!)}", "УФ ${i(v)}, безопасно", "УФ ${i(v)}")
                        WINDY -> Triple("УФ ${i(v!!)}", "УФ ${i(v)}, мажься кремом", "УФ ${i(v)}")
                        HARSH -> Triple("УФ ${i(v!!)}", "УФ ${i(v)} — обгоришь за полчаса", "УФ ${i(v)}")
                    }
                }
            }
            FactorId.PRECIP -> {
                val storm = StatusMapper.isThunderstorm(hour.weatherCode)
                val v = hour.precipProbPct
                val relevant = seasonal && (v != null || storm)
                val status = when {
                    !relevant -> null
                    storm -> HARSH
                    else -> StatusMapper.precipStatus(v!!, hour.weatherCode, t)
                }
                factor(id, v, "%", relevant, status) { st ->
                    when {
                        storm -> Triple("гроза", "гроза, сиди дома", "гроза")
                        st == CALM -> Triple("осадки ${i(v!!)}%", "без осадков", "${i(v)}%")
                        st == WINDY -> Triple("дождь ${i(v!!)}%", "возможен дождь, ${i(v)}%", "дождь ${i(v)}%")
                        else -> Triple("дождь ${i(v!!)}%", "ливень, ${i(v)}%", "дождь ${i(v)}%")
                    }
                }
            }
            FactorId.DUST -> {
                val v = hour.pm10
                factor(id, v, "мкг/м³", seasonal && v != null,
                    status = v?.let { StatusMapper.dustStatus(it, t) }) { st ->
                    when (st) {
                        CALM -> Triple("PM10 ${i(v!!)}", "воздух чистый", "PM10 ${i(v)}")
                        WINDY -> Triple("PM10 ${i(v!!)}", "пыльно, PM10 ${i(v)}", "PM10 ${i(v)}")
                        HARSH -> Triple("PM10 ${i(v!!)}", "пыльная буря, PM10 ${i(v)}", "PM10 ${i(v)}")
                    }
                }
            }
            FactorId.SEA -> {
                val v = hour.waveHeightM
                factor(id, v, "м", seasonal && v != null,
                    status = v?.let { StatusMapper.seaStatus(it, t) }) { st ->
                    when (st) {
                        CALM -> Triple("волна ${d1(v!!)} м", "море спокойное", "волна ${d1(v)} м")
                        WINDY -> Triple("волна ${d1(v!!)} м", "волна ${d1(v)} м, качает", "волна ${d1(v)} м")
                        HARSH -> Triple("волна ${d1(v!!)} м", "волна больше метра", "волна ${d1(v)} м")
                    }
                }
            }
        }
    }

    /**
     * Assemble a [Factor]. When not relevant, status collapses to CALM and
     * labels are blank — an irrelevant factor is never shown (spec §14.5), so
     * its labels are never read.
     */
    private inline fun factor(
        id: FactorId,
        value: Double?,
        unit: String,
        relevant: Boolean,
        status: WindStatus?,
        labels: (WindStatus) -> Triple<String, String, String>,
    ): Factor {
        if (!relevant || status == null) {
            return Factor(id, value, unit, CALM, relevant = false, shortLabel = "", verdictLine = "")
        }
        val (short, verdict, _) = labels(status)
        return Factor(id, value, unit, status, relevant = true, shortLabel = short, verdictLine = verdict)
    }

    /**
     * The verdict: worst relevant factor wins; ties broken by [PRIORITY]
     * (spec §14.7). Deterministic — the same factors always yield the same
     * binding, so the headline never flickers.
     */
    fun verdictOf(factors: List<Factor>): Verdict {
        val active = factors.filter { it.relevant }
        val worstOrdinal = active.maxOfOrNull { it.status.ordinal }
        if (worstOrdinal == null || worstOrdinal == CALM.ordinal) {
            return Verdict(CALM, binding = null, headline = "ДА", reason = "Все условия в норме")
        }
        val binding = PRIORITY.firstNotNullOfOrNull { id ->
            active.firstOrNull { it.id == id && it.status.ordinal == worstOrdinal }
        }!!
        return when (binding.status) {
            WINDY -> Verdict(WINDY, binding, "ДА, НО", binding.verdictLine)
            HARSH -> Verdict(HARSH, binding, "НЕ СЕГОДНЯ", binding.verdictLine)
            CALM -> Verdict(CALM, null, "ДА", "Все условия в норме") // unreachable
        }
    }

    /** Worst relevant status for one hour — used to colour the histogram (spec §14.10). */
    fun verdictStatusOf(hour: HourlyPoint, t: Thresholds): WindStatus {
        val month = hour.time.atZone(AQTAU).month
        return verdictOf(buildFactors(hour, month, t)).status
    }

    /** Per-hour verdict status across the whole series (spec §14.10 histogram). */
    fun hourlyVerdictStatuses(hourly: List<HourlyPoint>, t: Thresholds): List<WindStatus> =
        hourly.map { verdictStatusOf(it, t) }
}
