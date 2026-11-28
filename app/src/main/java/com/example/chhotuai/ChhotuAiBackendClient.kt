package com.example.chhotuai

import com.example.data.model.ChhotuChatMessageEntity

/**
 * Direct In-App Chhotu AI Engine running inside BharatTube with zero external API keys or Secrets.
 */
class ChhotuAiBackendClient {

    fun isConfigured(): Boolean = true

    fun probeHealth(): BackendConnectionState {
        val now = System.currentTimeMillis()
        return BackendConnectionState(
            isConfigured = true,
            isOnline = true,
            statusLabel = "Online • In-App Chhotu AI Engine (No Key Needed)",
            httpCode = 200,
            latencyMs = 1L,
            endpointMasked = "BharatTube On-Device Engine",
            lastCheckedAt = now,
            errorDetail = ""
        )
    }

    fun generateInAppReply(
        userMessage: String,
        isAdmin: Boolean,
        languageCode: String,
        healthSnapshot: SystemHealthSnapshot?,
        totalUsers: Int,
        totalVideos: Int,
        totalChannels: Int,
        creatorSharePct: Int,
        platformSharePct: Int,
        indiaThresholdInr: Double,
        recentHistory: List<ChhotuChatMessageEntity>
    ): String {
        val sanitized = ChhotuSecurityGuard.sanitizeForOutboundContext(userMessage)
        val topic = ChhotuUserSupportEngine.detectTopic(sanitized)
        return ChhotuUserSupportEngine.buildDirectReply(
            rawQuery = sanitized,
            topic = topic,
            isAdmin = isAdmin,
            languageCode = languageCode,
            health = healthSnapshot,
            totalUsers = totalUsers,
            totalVideos = totalVideos,
            totalChannels = totalChannels,
            creatorSharePct = creatorSharePct,
            platformSharePct = platformSharePct,
            indiaThresholdInr = indiaThresholdInr,
            historyCount = recentHistory.size
        )
    }
}
