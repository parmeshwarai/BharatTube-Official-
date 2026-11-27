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

class ChhotuAiBackendClient(
    private val baseUrlProvider: () -> String = { BuildConfig.CHHOTU_AI_BASE_URL },
    private val tokenProvider: () -> String = { BuildConfig.CHHOTU_AI_APP_TOKEN }
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun resolvedBaseUrl(): String {
        val raw = baseUrlProvider().trim()
        return if (raw.isEmpty() || raw == "MY_CHHOTU_AI_BASE_URL") "" else raw
    }

    private fun resolvedToken(): String {
        val raw = tokenProvider().trim()
        return if (raw.isEmpty() || raw == "MY_CHHOTU_AI_APP_TOKEN") "" else raw
    }

    fun isConfigured(): Boolean = resolvedBaseUrl().isNotBlank()

    suspend fun probeHealth(): BackendConnectionState = withContext(Dispatchers.IO) {
        val rawUrl = resolvedBaseUrl()
        val now = System.currentTimeMillis()
        if (rawUrl.isEmpty()) {
            return@withContext BackendConnectionState(
                isConfigured = false,
                isOnline = false,
                statusLabel = "Not Connected (CHHOTU_AI_BASE_URL required in AI Studio Secrets)",
                endpointMasked = "Not Configured",
                lastCheckedAt = now
            )
        }

        val validUrl = ChhotuSecurityGuard.validateHttpsEndpoint(rawUrl).getOrElse { err ->
            return@withContext BackendConnectionState(
                isConfigured = true,
                isOnline = false,
                statusLabel = "Security Block: ${err.message}",
                endpointMasked = ChhotuSecurityGuard.maskEndpoint(rawUrl),
                lastCheckedAt = now,
                errorDetail = err.message ?: "Invalid endpoint"
            )
        }

        val startMs = System.currentTimeMillis()
        val requestBuilder = Request.Builder()
            .url("$validUrl/health")
            .get()
        val token = resolvedToken()
        if (token.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        try {
            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                val ok = response.isSuccessful
                BackendConnectionState(
                    isConfigured = true,
                    isOnline = ok,
                    statusLabel = if (ok) "Connected (Existing Chhotu AI Online)" else "Server Returned HTTP ${response.code}",
                    httpCode = response.code,
                    latencyMs = elapsed,
                    endpointMasked = ChhotuSecurityGuard.maskEndpoint(validUrl),
                    lastCheckedAt = System.currentTimeMillis(),
                    errorDetail = if (ok) "" else "HTTP ${response.code}: ${response.message}"
                )
            }
        } catch (e: Exception) {
            BackendConnectionState(
                isConfigured = true,
                isOnline = false,
                statusLabel = "Unreachable: ${e.javaClass.simpleName}",
                latencyMs = System.currentTimeMillis() - startMs,
                endpointMasked = ChhotuSecurityGuard.maskEndpoint(validUrl),
                lastCheckedAt = System.currentTimeMillis(),
                errorDetail = e.message ?: "Network error reaching Chhotu AI backend"
            )
        }
    }

    suspend fun sendChatToExistingBackend(
        userId: String,
        userMessage: String,
        languageCode: String,
        sanitizedAppSummary: String,
        recentHistory: List<ChhotuChatMessageEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        val rawUrl = resolvedBaseUrl()
        if (rawUrl.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Existing Chhotu AI Backend is Not Connected. Set CHHOTU_AI_BASE_URL in AI Studio Secrets.")
            )
        }

        val validUrl = ChhotuSecurityGuard.validateHttpsEndpoint(rawUrl).getOrElse {
            return@withContext Result.failure(it)
        }

        val historyArray = JSONArray()
        recentHistory.takeLast(8).forEach { msg ->
            historyArray.put(
                JSONObject().apply {
                    put("role", if (msg.role == "USER") "user" else "assistant")
                    put("content", ChhotuSecurityGuard.sanitizeForOutboundContext(msg.text))
                }
            )
        }

        val payload = JSONObject().apply {
            put("assistantName", "Chhotu AI")
            put("platform", "BharatTube")
            put("userId", userId)
            put("language", languageCode)
            put("message", ChhotuSecurityGuard.sanitizeForOutboundContext(userMessage))
            put("appContextSummary", ChhotuSecurityGuard.sanitizeForOutboundContext(sanitizedAppSummary))
            put("history", historyArray)
        }

        val requestBuilder = Request.Builder()
            .url("$validUrl/chat")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Accept", "application/json")
        val token = resolvedToken()
        if (token.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        try {
            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("Chhotu AI backend error HTTP ${response.code}")
                    )
                }
                val parsedReply = parseAssistantReply(bodyStr)
                if (parsedReply.isBlank()) {
                    Result.failure(IllegalStateException("Chhotu AI server returned an empty reply."))
                } else {
                    Result.success(parsedReply)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAssistantReply(rawBody: String): String {
        val trimmed = rawBody.trim()
        if (trimmed.isEmpty()) return ""
        return try {
            val json = JSONObject(trimmed)
            json.optString("reply")
                .ifBlank { json.optString("response") }
                .ifBlank { json.optString("message") }
                .ifBlank { json.optString("answer") }
                .ifBlank { json.optString("text") }
                .ifBlank { trimmed }
        } catch (_: Exception) {
            trimmed
        }
    }
}
