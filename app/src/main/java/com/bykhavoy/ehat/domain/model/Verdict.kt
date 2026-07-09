package com.bykhavoy.ehat.domain.model

import com.bykhavoy.ehat.domain.WindStatus

/**
 * The answer the app exists to give (spec §14.7).
 * @param binding the single limiting factor; null only when everything is CALM.
 */
data class Verdict(
    val status: WindStatus,
    val binding: Factor?,
    val headline: String,
    val reason: String,
)
