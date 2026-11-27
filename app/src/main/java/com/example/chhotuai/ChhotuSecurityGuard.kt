package com.example.chhotuai

import java.net.InetAddress
import java.net.URI

object ChhotuSecurityGuard {
    private val sensitiveQueryPatterns = listOf(
        Regex("\\b(password|passcode|otp|pin|cvv|secret|api[_\\s-]?key|token)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(other\\s+user|another\\s+user|sabhi\\s+user|doosre\\s+user).*(email|phone|upi|bank|private|data)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(upi|bank\\s*account).*\\b(user_|@)", RegexOption.IGNORE_CASE)
    )

    private val privilegedActionPatterns = listOf(
        Regex("\\b(ban\\s+user|suspend\\s+account|delete\\s+user)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(send\\s+payout|transfer\\s+money|pay\\s+now|release\\s+payment)\\b", RegexOption.IGNORE_CASE)
    )

    fun isBlockedSensitiveRequest(query: String, isAdmin: Boolean): String? {
        if (sensitiveQueryPatterns.any { it.containsMatchIn(query) }) {
            return "Security Policy: Chhotu AI never accesses or shares passwords, OTPs, API keys, bank details, or another user's private data."
        }
        if (!isAdmin && privilegedActionPatterns.any { it.containsMatchIn(query) }) {
            return "Security Policy: Account bans, payouts, and platform controls require Admin authorization and explicit confirmation."
        }
        return null
    }

    fun sanitizeForOutboundContext(raw: String): String {
        return raw
            .replace(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+"), "[redacted-identifier]")
            .replace(Regex("\\b\\d{6,16}\\b"), "[redacted-number]")
    }

    fun validateHttpsEndpoint(rawUrl: String): Result<String> {
        val trimmed = rawUrl.trim().trimEnd('/')
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalStateException("CHHOTU_AI_BASE_URL is not configured."))
        }
        val uri = try {
            URI(trimmed)
        } catch (_: Exception) {
            return Result.failure(IllegalArgumentException("Invalid Chhotu AI server URL format."))
        }
        if (!uri.scheme.equals("https", ignoreCase = true)) {
            return Result.failure(SecurityException("Only HTTPS URLs are allowed for secure Chhotu AI backend calls."))
        }
        val host = uri.host?.lowercase()?.trim()
            ?: return Result.failure(IllegalArgumentException("Missing host in Chhotu AI server URL."))

        if (host == "localhost" || host.endsWith(".local") || host == "169.254.169.254") {
            return Result.failure(SecurityException("Loopback or metadata hosts are blocked for security."))
        }
        try {
            val addr = InetAddress.getByName(host)
            if (addr.isAnyLocalAddress || addr.isLoopbackAddress || addr.isLinkLocalAddress || addr.isSiteLocalAddress) {
                return Result.failure(SecurityException("Private/internal network IP targets are blocked."))
            }
        } catch (_: Exception) {
            // Host DNS resolution will be handled by OkHttp during the actual network call
        }
        return Result.success(trimmed)
    }

    fun maskEndpoint(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return "Not Configured"
        return try {
            val uri = URI(trimmed)
            "${uri.scheme ?: "https"}://${uri.host ?: "server"}"
        } catch (_: Exception) {
            "Configured (invalid URI)"
        }
    }
}
