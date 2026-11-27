package com.example.ads

import com.example.data.model.BharatTubeCatalog

sealed interface ActivePlayerAd {
    val totalSeconds: Int

    data class PreRoll(
        val skipAfterSeconds: Int,
        override val totalSeconds: Int
    ) : ActivePlayerAd

    data class MidRoll(
        val cueSeconds: Int,
        override val totalSeconds: Int,
        val breakIndex: Int,
        val totalBreaks: Int
    ) : ActivePlayerAd
}

data class LongVideoAdSchedule(
    val durationSeconds: Int,
    val showPreRoll: Boolean,
    val preRollSkipAfterSeconds: Int,
    val preRollTotalSeconds: Int,
    val midRollCueSeconds: List<Int>,
    val midRollDurationSeconds: Int
) {
    fun summaryText(): String {
        if (!showPreRoll && midRollCueSeconds.isEmpty()) {
            return "Ads disabled for this video"
        }
        return when {
            durationSeconds < LongVideoAdPolicy.SHORT_LONG_VIDEO_MAX_SECONDS ->
                "Under 3 min video • Pre-Roll 5s Skippable Ad only (No Mid-Roll)"
            durationSeconds <= LongVideoAdPolicy.MID_ROLL_ELIGIBLE_MIN_SECONDS ->
                "3–8 min video • Pre-Roll 5s Skippable Ad only (20s Mid-Roll starts > 8 min)"
            else -> {
                val cuesText = if (midRollCueSeconds.isEmpty()) {
                    "None"
                } else {
                    midRollCueSeconds.joinToString(", ") { BharatTubeCatalog.formatDuration(it) }
                }
                "Over 8 min video • 5s Skip Pre-Roll + 20s Mid-Roll every 8 min ($cuesText)"
            }
        }
    }
}

object LongVideoAdPolicy {
    const val SHORT_LONG_VIDEO_MAX_SECONDS = 180 // 3 minutes
    const val MID_ROLL_ELIGIBLE_MIN_SECONDS = 480 // 8 minutes
    const val PRE_ROLL_SKIP_SECONDS = 5
    const val PRE_ROLL_TOTAL_SECONDS = 12
    const val MID_ROLL_INTERVAL_SECONDS = 480 // Every 8 minutes
    const val MID_ROLL_DURATION_SECONDS = 20

    fun buildSchedule(
        durationSeconds: Int,
        isLongVideo: Boolean,
        adEligible: Boolean,
        adsEnabled: Boolean,
        preRollEnabled: Boolean,
        midRollEnabled: Boolean
    ): LongVideoAdSchedule {
        if (!isLongVideo || !adEligible || !adsEnabled) {
            return LongVideoAdSchedule(
                durationSeconds = durationSeconds,
                showPreRoll = false,
                preRollSkipAfterSeconds = 0,
                preRollTotalSeconds = 0,
                midRollCueSeconds = emptyList(),
                midRollDurationSeconds = 0
            )
        }

        val cues = if (midRollEnabled && durationSeconds > MID_ROLL_ELIGIBLE_MIN_SECONDS) {
            (MID_ROLL_INTERVAL_SECONDS until durationSeconds step MID_ROLL_INTERVAL_SECONDS).toList()
        } else {
            emptyList()
        }

        return LongVideoAdSchedule(
            durationSeconds = durationSeconds,
            showPreRoll = preRollEnabled,
            preRollSkipAfterSeconds = PRE_ROLL_SKIP_SECONDS,
            preRollTotalSeconds = PRE_ROLL_TOTAL_SECONDS,
            midRollCueSeconds = cues,
            midRollDurationSeconds = MID_ROLL_DURATION_SECONDS
        )
    }

    fun findPendingMidRollCue(
        currentPositionSeconds: Int,
        schedule: LongVideoAdSchedule,
        triggeredCues: Set<Int>
    ): Int? {
        if (schedule.midRollCueSeconds.isEmpty()) return null
        return schedule.midRollCueSeconds
            .lastOrNull { cue -> cue <= currentPositionSeconds && cue !in triggeredCues }
    }
}
