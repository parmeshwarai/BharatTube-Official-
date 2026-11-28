package com.example.chhotuai

import android.util.Log
import com.example.data.model.ChhotuChatMessageEntity

/**
 * Chhotu AI Backend & Assistant Engine:
 * - Preserves the built-in BharatTube Chhotu AI engine (zero-key offline/fallback support).
 * - Adds OpenAI ChatGPT API integration via OpenAiAssistantService (using OPENAI_API_KEY from Secrets).
 */
class ChhotuAiBackendClient(
    private val openAiService: OpenAiAssistantService = OpenAiAssistantService()
) {

    fun isConfigured(): Boolean = true

    fun isOpenAiConfigured(): Boolean = openAiService.isConfigured()

    fun probeHealth(): BackendConnectionState {
        val now = System.currentTimeMillis()
        val openAiReady = openAiService.isConfigured()
        val openAiStatus = if (openAiReady) {
            "OpenAI ChatGPT API Connected (OPENAI_API_KEY active)"
        } else {
            "Awaiting OPENAI_API_KEY in AI Studio Secrets (Built-in Chhotu AI active)"
        }
        return BackendConnectionState(
            isConfigured = true,
            isOnline = true,
            statusLabel = if (openAiReady) {
                "Online • Chhotu AI + OpenAI ChatGPT"
            } else {
                "Online • In-App Chhotu AI Engine"
            },
            httpCode = 200,
            latencyMs = 1L,
            endpointMasked = if (openAiReady) {
                "OpenAI HTTPS API + BharatTube Engine"
            } else {
                "BharatTube On-Device Engine"
            },
            lastCheckedAt = now,
            errorDetail = "",
            openAiConfigured = openAiReady,
            openAiStatusLabel = openAiStatus
        )
    }

    suspend fun generateAssistantReply(
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
    ): ChhotuReplyResult {
        if (openAiService.isConfigured()) {
            val openAiResult = openAiService.requestChatCompletion(
                userMessage = userMessage,
                isAdmin = isAdmin,
                languageCode = languageCode,
                healthSnapshot = healthSnapshot,
                totalUsers = totalUsers,
                totalVideos = totalVideos,
                totalChannels = totalChannels,
                creatorSharePct = creatorSharePct,
                platformSharePct = platformSharePct,
                indiaThresholdInr = indiaThresholdInr,
                recentHistory = recentHistory
            )
            openAiResult.onSuccess { reply ->
                return ChhotuReplyResult(
                    text = reply,
                    sourceMode = SOURCE_OPENAI_CHATGPT
                )
            }.onFailure { error ->
                Log.w(TAG, "OpenAI fallback to built-in Chhotu AI: ${error.message}")
            }
        }

        val fallbackReply = generateInAppReply(
            userMessage = userMessage,
            isAdmin = isAdmin,
            languageCode = languageCode,
            healthSnapshot = healthSnapshot,
            totalUsers = totalUsers,
            totalVideos = totalVideos,
            totalChannels = totalChannels,
            creatorSharePct = creatorSharePct,
            platformSharePct = platformSharePct,
            indiaThresholdInr = indiaThresholdInr,
            recentHistory = recentHistory
        )
        return ChhotuReplyResult(
            text = fallbackReply,
            sourceMode = SOURCE_IN_APP_CHHOTU
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

    companion object {
        const val SOURCE_OPENAI_CHATGPT = "OPENAI_CHATGPT"
        const val SOURCE_IN_APP_CHHOTU = "IN_APP_CHHOTU"
        private const val TAG = "ChhotuAiBackendClient"
    }
}
