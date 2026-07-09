package com.bykhavoy.ehat.domain.model

import com.bykhavoy.ehat.domain.WindStatus

/** The six — and only six — decision factors (spec §14.2, §14.3). */
enum class FactorId { WIND, HEAT, UV, PRECIP, DUST, SEA }

/**
 * Deterministic tie-break order when several factors share the worst status
 * (spec §14.7). Fixed so the verdict never flickers between two equally-bad
 * factors on refresh. Storm beats dust, dust beats wind.
 */
val PRIORITY: List<FactorId> = listOf(
    FactorId.PRECIP, FactorId.DUST, FactorId.WIND, FactorId.HEAT, FactorId.UV, FactorId.SEA,
)

/**
 * One evaluated factor at a given moment.
 *
 * @param relevant whether it participates in the verdict right now. Irrelevant
 *   factors are not shown at all (not greyed, not struck through) — spec §14.5.
 */
data class Factor(
    val id: FactorId,
    val value: Double?,
    val unit: String,
    val status: WindStatus,
    val relevant: Boolean,
    val shortLabel: String,
    val verdictLine: String,
)
