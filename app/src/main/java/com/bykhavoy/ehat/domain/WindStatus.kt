package com.bykhavoy.ehat.domain

/**
 * The three-level "traffic light" reused for every factor (spec §5.1, §14.3).
 * ordinal order matters: CALM < WINDY < HARSH, so the worst factor is the max.
 */
enum class WindStatus { CALM, WINDY, HARSH }
