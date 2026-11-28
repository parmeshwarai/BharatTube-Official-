package com.example.data.model

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

enum class AnalyticsTimeframe(
    val label: String,
    val viewScale: Double,
    val pointCount: Int,
    val windowDays: Int
) {
    LAST_7_DAYS("Last 7 Days", 0.28, 7, 7),
    LAST_28_DAYS("28 Days", 0.62, 14, 28),
    LAST_90_DAYS("90 Days", 0.86, 15, 90),
    ALL_TIME("All Time", 1.0, 12, 365)
}

data class LabelShare(
    val label: String,
    val percent: Int,
    val subtitle: String = ""
)

data class VideoAnalyticsSnapshot(
    val videoId: String,
    val timeframe: AnalyticsTimeframe,
    val views: Long,
    val likes: Long,
    val comments: Long,
    val shares: Long,
    val watchTimeHours: Double,
    val watchTimeMinutes: Long,
    val watchTimeChangePct: Double,
    val earningsInr: Double,
    val earningsChangePct: Double,
    val subscribersGained: Int,
    val subscribersLost: Int,
    val netSubscribers: Int,
    val avgRetentionPct: Double,
    val watchTimeSeries: List<Float>,
    val retentionSeries: List<Float>,
    val trafficSources: List<LabelShare>,
    val ageGroups: List<LabelShare>,
    val genderSplit: List<LabelShare>,
    val topCities: List<LabelShare>,
    val topCountries: List<LabelShare>
)

data class StudioOverviewAnalytics(
    val timeframe: AnalyticsTimeframe,
    val totalViews: Long,
    val totalWatchHours: Long,
    val totalEarningsInr: Double,
    val viewsChangePct: Double,
    val watchTimeChangePct: Double,
    val earningsChangePct: Double,
    val trendSeries: List<Float>,
    val dateLabels: List<String>
)

object BharatTubeAnalyticsEngine {

    fun buildVideoAnalytics(
        video: VideoEntity,
        commentCount: Int,
        timeframe: AnalyticsTimeframe,
        settings: PlatformSettingsEntity,
        userBoostPct: Int = 0
    ): VideoAnalyticsSnapshot {
        val seed = abs(video.id.hashCode())
        val scale = timeframe.viewScale

        val scaledViews = max(if (video.viewCount > 0) 1L else 0L, (video.viewCount * scale).toLong())
        val scaledLikes = max(if (video.likeCount > 0) 1L else 0L, (video.likeCount * scale).toLong())
        val baseShares = if (video.shareCount > 0L) video.shareCount else (video.viewCount / 140L).coerceAtLeast(if (video.viewCount > 0) 3L else 0L)
        val scaledShares = (baseShares * scale).toLong()
        val effectiveComments = max(commentCount.toLong(), if (video.viewCount > 500L) (video.viewCount / 460L).coerceAtLeast(4L) else commentCount.toLong())
        val scaledComments = (effectiveComments * scale).toLong()

        val baseWatchMins = if (video.watchTimeMinutes > 0L) {
            video.watchTimeMinutes
        } else {
            val avgMinsPerView = ((video.durationSeconds.coerceAtLeast(15) * 0.48) / 60.0).coerceIn(0.25, 14.0)
            (video.viewCount * avgMinsPerView).toLong()
        }
        val scaledWatchMins = (baseWatchMins * scale).toLong()
        val watchHours = scaledWatchMins / 60.0

        val totalLifetimeEarned = BharatTubeCatalog.calculateCreatorEarningsInr(
            viewCount = video.viewCount,
            netRevenueInr = video.netRevenueInr,
            creatorSharePct = settings.creatorSharePct,
            per1000ViewsPaymentInr = settings.per1000ViewsPaymentInr,
            bonusPercent = settings.bonusPercent,
            userBoostPct = userBoostPct
        )
        val scaledEarnings = totalLifetimeEarned * scale

        val baseGained = if (video.subscribersGained > 0) video.subscribersGained else ((video.viewCount / 220L).toInt()).coerceAtLeast(if (video.viewCount > 0) 2 else 0)
        val baseLost = if (video.subscribersLost > 0) video.subscribersLost else (baseGained / 14).coerceAtLeast(0)
        val gained = (baseGained * scale).roundToInt()
        val lost = (baseLost * scale).roundToInt()

        val watchTimeSeries = List(timeframe.pointCount) { idx ->
            val progress = idx.toFloat() / (timeframe.pointCount - 1).coerceAtLeast(1)
            val wave = kotlin.math.sin((idx + (seed % 7)) * 0.85).toFloat() * 0.14f
            (0.28f + progress * 0.56f + wave).coerceIn(0.12f, 0.98f)
        }

        val isShort = video.format == VideoFormat.SHORT.name
        val avgRetention = if (isShort) 68.4 + (seed % 14) else 39.5 + (seed % 12)
        val retentionSeries = List(16) { idx ->
            val t = idx / 15f
            val baseDrop = if (isShort) {
                1.0f - (t * 0.34f) + kotlin.math.sin(t * 4f).toFloat() * 0.03f
            } else {
                0.98f - (0.56f * (1f - kotlin.math.exp((-2.8 * t).toDouble()).toFloat()))
            }
            baseDrop.coerceIn(0.18f, 1.0f)
        }

        val trafficSources = if (isShort) {
            listOf(
                LabelShare("Shorts Feed", 54),
                LabelShare("Home", 21),
                LabelShare("Search", 15),
                LabelShare("Suggested", 10)
            )
        } else {
            listOf(
                LabelShare("Home", 36),
                LabelShare("Search", 32),
                LabelShare("Suggested", 22),
                LabelShare("Shorts Feed", 10)
            )
        }

        val ageGroups = listOf(
            LabelShare("25-34", 44),
            LabelShare("18-24", 34),
            LabelShare("35-44", 15),
            LabelShare("45-54", 7)
        )
        val malePct = 62 + (seed % 11)
        val genderSplit = listOf(
            LabelShare("Male", malePct),
            LabelShare("Female", 97 - malePct),
            LabelShare("Other", 3)
        )
        val topCities = listOf(
            LabelShare("Mumbai", 24, formatShareViews(scaledViews, 24)),
            LabelShare("Delhi NCR", 21, formatShareViews(scaledViews, 21)),
            LabelShare("Bengaluru", 18, formatShareViews(scaledViews, 18)),
            LabelShare("Jaipur", 12, formatShareViews(scaledViews, 12)),
            LabelShare("Lucknow", 9, formatShareViews(scaledViews, 9))
        )
        val topCountries = listOf(
            LabelShare("India", 74, formatShareViews(scaledViews, 74)),
            LabelShare("United States", 11, formatShareViews(scaledViews, 11)),
            LabelShare("UAE", 8, formatShareViews(scaledViews, 8)),
            LabelShare("United Kingdom", 7, formatShareViews(scaledViews, 7))
        )

        return VideoAnalyticsSnapshot(
            videoId = video.id,
            timeframe = timeframe,
            views = scaledViews,
            likes = scaledLikes,
            comments = scaledComments,
            shares = scaledShares,
            watchTimeHours = watchHours,
            watchTimeMinutes = scaledWatchMins,
            watchTimeChangePct = 14.2 + (seed % 11),
            earningsInr = scaledEarnings,
            earningsChangePct = 11.5 + (seed % 9),
            subscribersGained = gained,
            subscribersLost = lost,
            netSubscribers = gained - lost,
            avgRetentionPct = avgRetention,
            watchTimeSeries = watchTimeSeries,
            retentionSeries = retentionSeries,
            trafficSources = trafficSources,
            ageGroups = ageGroups,
            genderSplit = genderSplit,
            topCities = topCities,
            topCountries = topCountries
        )
    }

    fun buildStudioOverview(
        videos: List<VideoEntity>,
        timeframe: AnalyticsTimeframe,
        settings: PlatformSettingsEntity,
        userBoostPct: Int
    ): StudioOverviewAnalytics {
        val snaps = videos.map { buildVideoAnalytics(it, 0, timeframe, settings, userBoostPct) }
        val totalViews = snaps.sumOf { it.views }
        val totalWatchHours = snaps.sumOf { it.watchTimeHours }.toLong()
        val totalEarnings = snaps.sumOf { it.earningsInr }
        val points = timeframe.pointCount
        val trend = List(points) { idx ->
            val avg = if (snaps.isEmpty()) 0.35f else snaps.map { it.watchTimeSeries[idx % it.watchTimeSeries.size] }.average().toFloat()
            avg.coerceIn(0.15f, 0.98f)
        }
        val labels = when (timeframe) {
            AnalyticsTimeframe.LAST_7_DAYS -> listOf("Day 1", "Day 3", "Day 5", "Today")
            AnalyticsTimeframe.LAST_28_DAYS -> listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4")
            AnalyticsTimeframe.LAST_90_DAYS -> listOf("M1", "M2", "M3", "Now")
            AnalyticsTimeframe.ALL_TIME -> listOf("Start", "Mid", "Peak", "Now")
        }
        return StudioOverviewAnalytics(
            timeframe = timeframe,
            totalViews = totalViews,
            totalWatchHours = totalWatchHours,
            totalEarningsInr = totalEarnings,
            viewsChangePct = 12.4,
            watchTimeChangePct = 8.7,
            earningsChangePct = 15.2,
            trendSeries = trend,
            dateLabels = labels
        )
    }

    private fun formatShareViews(totalViews: Long, pct: Int): String {
        val count = (totalViews * pct) / 100L
        return "${BharatTubeCatalog.formatCompactCount(count)} views"
    }
}
