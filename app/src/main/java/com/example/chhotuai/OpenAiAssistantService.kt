package com.example.chhotuai

import com.example.BuildConfig
import com.example.data.model.ChhotuChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Backend/Data-layer client for OpenAI Chat Completions API.
 * Reads OPENAI_API_KEY exclusively from BuildConfig (injected from Google AI Studio Secrets / .env)
 * and never exposes the key to UI composables, logs, or user-visible error messages.
 */
class OpenAiAssistantService(
    private val keyProvider: () -> String = { BuildConfig.OPENAI_API_KEY },
    private val httpClient: OkHttpClient = defaultHttpClient()
) {
    fun isConfigured(): Boolean = isUsableApiKey(keyProvider())

    suspend fun requestChatCompletion(
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
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = keyProvider().trim()
        if (!isUsableApiKey(apiKey)) {
            return@withContext Result.failure(IllegalStateException("OPENAI_API_KEY is not configured in Secrets."))
        }

        val validatedUrl = ChhotuSecurityGuard.validateHttpsEndpoint(OPENAI_CHAT_COMPLETIONS_URL)
            .getOrElse { return@withContext Result.failure(it) }

        val payload = buildChatPayload(
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

        val request = Request.Builder()
            .url(validatedUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val safeErrorMessage = extractSafeApiError(rawBody, response.code)
                    throw IllegalStateException(safeErrorMessage)
                }
                val content = extractAssistantContent(rawBody)
                if (content.isBlank()) {
                    throw IllegalStateException("Empty response received from OpenAI.")
                }
                content
            }
        }
    }

    internal fun buildChatPayload(
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
    ): JSONObject {
        val messagesArray = JSONArray()
        messagesArray.put(
            JSONObject()
                .put("role", "system")
                .put(
                    "content",
                    buildSystemPrompt(
                        isAdmin = isAdmin,
                        languageCode = languageCode,
                        healthSnapshot = healthSnapshot,
                        totalUsers = totalUsers,
                        totalVideos = totalVideos,
                        totalChannels = totalChannels,
                        creatorSharePct = creatorSharePct,
                        platformSharePct = platformSharePct,
                        indiaThresholdInr = indiaThresholdInr
                    )
                )
        )

        recentHistory
            .filter { it.sourceMode != "SECURITY_BLOCK" && it.text.isNotBlank() }
            .takeLast(8)
            .forEach { entry ->
                val role = if (entry.role == "USER") "user" else "assistant"
                val sanitizedHistory = ChhotuSecurityGuard.sanitizeForOutboundContext(entry.text).take(1200)
                messagesArray.put(
                    JSONObject()
                        .put("role", role)
                        .put("content", sanitizedHistory)
                )
            }

        val sanitizedCurrent = ChhotuSecurityGuard.sanitizeForOutboundContext(userMessage).take(2000)
        messagesArray.put(
            JSONObject()
                .put("role", "user")
                .put("content", sanitizedCurrent)
        )

        return JSONObject()
            .put("model", DEFAULT_MODEL)
            .put("messages", messagesArray)
            .put("temperature", 0.7)
            .put("max_tokens", 800)
    }

    private fun buildSystemPrompt(
        isAdmin: Boolean,
        languageCode: String,
        healthSnapshot: SystemHealthSnapshot?,
        totalUsers: Int,
        totalVideos: Int,
        totalChannels: Int,
        creatorSharePct: Int,
        platformSharePct: Int,
        indiaThresholdInr: Double
    ): String {
        val preferredLang = if (languageCode.equals("hi", ignoreCase = true)) {
            "Hindi (or natural Hinglish when appropriate)"
        } else {
            "English (or match the user's language if they write in Hindi/Hinglish)"
        }
        val healthSummary = if (healthSnapshot != null) {
            "${healthSnapshot.connectedCount}/${healthSnapshot.totalCount} modules Connected"
        } else {
            "diagnostics initializing"
        }
        val adminNote = if (isAdmin) {
            "The current user is an authorized BharatTube Owner/Admin. Admin-only revenue split is $creatorSharePct% Creator / $platformSharePct% BharatTube, and India payout threshold is Rs.${indiaThresholdInr.toInt()}."
        } else {
            "The current user is a standard creator/viewer. NEVER disclose internal platform/creator revenue split percentages or admin-only business controls. You may mention that Creator Studio tracks views/earnings and payouts start at Rs.${indiaThresholdInr.toInt()} in India after verification."
        }

        return """
            You are Chhotu AI, the official intelligent assistant inside the BharatTube video & creator app (powered by OpenAI for ChatGPT-grade help).
            Preferred response language: $preferredLang.
            Tone: Helpful, clear, friendly, accurate, and concise like ChatGPT.

            BharatTube App Knowledge:
            - Bottom Navigation: Home, Shorts, '+' (Upload Video, Create Short, Go Live, Create Post), Subscriptions, Profile.
            - Camera & Upload: Front/Back camera, Flash, Timer, Gallery picker, live filters (Normal, Beauty, Warm, Cool, Black & White, Vintage), and Upload Details (Title, Description, Thumbnail, Visibility Public/Private/Unlisted, Audience, Playlist, Comments ON/OFF).
            - Channel & Profile: Banner & circular Logo upload/crop, @handle, Edit Channel, Manage Videos, BharatTube Studio (Dashboard, Videos, Analytics, Earn & Comments).
            - Live BharatTube Snapshot: $totalUsers accounts, $totalChannels channels, $totalVideos videos, system status: $healthSummary.
            - $adminNote

            General Capabilities:
            - Answer both BharatTube app questions AND general ChatGPT-style questions (video scripts, viral titles, descriptions, tags, coding, study help, writing, ideas, general knowledge).
            - Strict Privacy & Security: Never ask for or reveal passwords, OTPs, API keys, bank details, or private user data.
        """.trimIndent()
    }

    internal fun extractAssistantContent(rawJson: String): String {
        val root = JSONObject(rawJson)
        val choices = root.optJSONArray("choices") ?: return ""
        if (choices.length() == 0) return ""
        val first = choices.optJSONObject(0) ?: return ""
        val message = first.optJSONObject("message") ?: return ""
        return message.optString("content", "").trim()
    }

    private fun extractSafeApiError(rawBody: String, statusCode: Int): String {
        val parsedMsg = runCatching {
            JSONObject(rawBody).optJSONObject("error")?.optString("message", "").orEmpty()
        }.getOrDefault("")
        val cleanMsg = parsedMsg
            .replace(Regex("sk-[A-Za-z0-9_-]+"), "[redacted]")
            .take(160)
        return if (cleanMsg.isNotBlank()) {
            "OpenAI API error (HTTP $statusCode): $cleanMsg"
        } else {
            "OpenAI API error (HTTP $statusCode)"
        }
    }

    companion object {
        const val OPENAI_CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
        const val DEFAULT_MODEL = "gpt-4o-mini"
        private const val PLACEHOLDER_KEY = "OPENAI_API_KEY_DEFAULT_VALUE"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun isUsableApiKey(raw: String): Boolean {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return false
            if (trimmed == PLACEHOLDER_KEY) return false
            if (trimmed.startsWith("MY_", ignoreCase = true) || trimmed.startsWith("YOUR_", ignoreCase = true)) {
                return false
            }
            return true
        }

        fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
