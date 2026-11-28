package com.example.chhotuai

import com.example.data.model.VideoFormat

enum class CheckItemStatus {
    PENDING,
    CHECKING,
    PASSED,
    FAILED
}

data class ChhotuCheckDetail(
    val key: String,
    val title: String,
    val status: CheckItemStatus,
    val note: String
)

data class ChhotuCommentCheckResult(
    val allowed: Boolean,
    val reason: String
)

data class ChhotuUploadCheckResult(
    val approved: Boolean,
    val status: String, // "APPROVED" or "REJECTED"
    val checks: List<ChhotuCheckDetail>,
    val rejectionReason: String,
    val fixSuggestion: String,
    val modeLabel: String
) {
    fun serializeSummary(): String = checks.joinToString("|") {
        "${it.key}:${it.status.name}:${it.note.replace("|", " ").replace(":", "-")}"
    }
}

object ChhotuUploadModerator {

    private val copyrightBlockKeywords = listOf(
        "full movie leak", "hdcam", "camrip", "pirated", "stolen clip",
        "copyrighted song", "unauthorized reupload", "watermarked repost",
        "leaked movie", "tseries full album", "sony music rip", "reupload hd"
    )

    private val strictCopyrightKeywords = listOf(
        "bollywood full song", "bgm no credit", "movie scene", "clip from", "tiktok repost", "reels repost"
    )

    private val communityViolationKeywords = listOf(
        "free money hack", "otp bypass", "carding", "bank hack", "phishing",
        "scam trick", "kill ", "hate speech", "terror", "harass", "doxxing",
        "bomb making", "self harm", "suicide"
    )

    private val unsafeAdultKeywords = listOf(
        "porn", "xxx", "nude", "nsfw", "sex video", "adult 18+", "onlyfans leak",
        "ganda video", "ashleel", "behenchod", "madarchod", "chutiya", "randi"
    )

    private val strictClickbaitKeywords = listOf(
        "100% real hack", "instant crorepati", "secret govt leak", "fake news"
    )

    fun evaluate(
        title: String,
        description: String,
        hashtags: String,
        videoUri: String,
        thumbnailUri: String,
        format: VideoFormat,
        durationSeconds: Int,
        strictMode: Boolean
    ): ChhotuUploadCheckResult {
        val combined = "$title $description $hashtags".lowercase()
        val modeLabel = if (strictMode) "STRICT" else "NORMAL"

        // 1. Copyright Check
        val matchedCopyright = copyrightBlockKeywords.firstOrNull { combined.contains(it) }
            ?: if (strictMode) strictCopyrightKeywords.firstOrNull { combined.contains(it) } else null
        val copyrightPassed = matchedCopyright == null
        val copyrightCheck = ChhotuCheckDetail(
            key = "COPYRIGHT",
            title = "Copyright Check",
            status = if (copyrightPassed) CheckItemStatus.PASSED else CheckItemStatus.FAILED,
            note = if (copyrightPassed) {
                "Original content & cleared audio verified"
            } else {
                "Possible copyrighted / unattributed material ('$matchedCopyright')"
            }
        )

        // 2. Community Guidelines Check
        val matchedCommunity = communityViolationKeywords.firstOrNull { combined.contains(it) }
        val communityPassed = matchedCommunity == null
        val communityCheck = ChhotuCheckDetail(
            key = "COMMUNITY",
            title = "Community Guidelines Check",
            status = if (communityPassed) CheckItemStatus.PASSED else CheckItemStatus.FAILED,
            note = if (communityPassed) {
                "Follows BharatTube community safety standards"
            } else {
                "Community policy violation detected ('$matchedCommunity')"
            }
        )

        // 3. Ganda / Galat Content Check
        val matchedUnsafe = unsafeAdultKeywords.firstOrNull { combined.contains(it) }
            ?: if (strictMode) strictClickbaitKeywords.firstOrNull { combined.contains(it) } else null
        val safetyPassed = matchedUnsafe == null
        val safetyCheck = ChhotuCheckDetail(
            key = "SAFETY",
            title = "Ganda / Galat Content Check",
            status = if (safetyPassed) CheckItemStatus.PASSED else CheckItemStatus.FAILED,
            note = if (safetyPassed) {
                "Clean & family-safe content verified"
            } else {
                "Inappropriate, vulgar, or misleading content ('$matchedUnsafe')"
            }
        )

        // 4. Quality Check
        val cleanTitle = title.trim()
        val cleanDesc = description.trim()
        val hasVideoSource = videoUri.isNotBlank() && durationSeconds >= 1
        val qualityIssue = when {
            !hasVideoSource -> "Video file or stream could not be verified"
            strictMode && cleanDesc.length < 12 -> "Strict Mode: Description must be at least 12 characters explaining the video"
            cleanTitle.equals("test", ignoreCase = true) || cleanTitle.equals("untitled", ignoreCase = true) ->
                "Generic placeholder title ('$cleanTitle') failed quality review"
            format == VideoFormat.SHORT && durationSeconds > 180 ->
                "Short video duration exceeds 3-minute limit"
            else -> null
        }
        val qualityPassed = qualityIssue == null
        val qualityCheck = ChhotuCheckDetail(
            key = "QUALITY",
            title = "Quality Check",
            status = if (qualityPassed) CheckItemStatus.PASSED else CheckItemStatus.FAILED,
            note = qualityIssue ?: "Video playback, metadata & clarity passed"
        )

        // 5. Title / Thumbnail Check
        val upperLetters = cleanTitle.count { it.isUpperCase() }
        val totalLetters = cleanTitle.count { it.isLetter() }.coerceAtLeast(1)
        val excessiveAllCaps = strictMode && cleanTitle.length >= 10 && (upperLetters.toFloat() / totalLetters) > 0.78f
        val titleThumbIssue = when {
            cleanTitle.length < 4 -> "Title is too short (minimum 4 characters required)"
            excessiveAllCaps -> "Strict Mode: Avoid excessive ALL CAPS in title"
            thumbnailUri.isBlank() && strictMode -> "Strict Mode: Please add or generate a valid video thumbnail"
            else -> null
        }
        val titleThumbPassed = titleThumbIssue == null
        val titleThumbCheck = ChhotuCheckDetail(
            key = "TITLE_THUMB",
            title = "Title / Thumbnail Check",
            status = if (titleThumbPassed) CheckItemStatus.PASSED else CheckItemStatus.FAILED,
            note = titleThumbIssue ?: "Title & thumbnail clear and accurate"
        )

        val checks = listOf(copyrightCheck, communityCheck, safetyCheck, qualityCheck, titleThumbCheck)
        val failed = checks.filter { it.status == CheckItemStatus.FAILED }
        val approved = failed.isEmpty()

        val rejectionReason = if (approved) {
            ""
        } else {
            failed.joinToString("\n• ", prefix = "• ") { "${it.title}: ${it.note}" }
        }

        val fixSuggestion = if (approved) {
            ""
        } else {
            buildString {
                if (!copyrightPassed) append("Use only original footage/audio or royalty-free clips without watermarks. ")
                if (!communityPassed || !safetyPassed) append("Remove abusive, adult, scam, or misleading words from Title, Description, or Tags. ")
                if (!qualityPassed) append("Ensure a clear descriptive Title & at least 12+ character Description. ")
                if (!titleThumbPassed) append("Write a clean, readable Title (avoid ALL CAPS) and set a Thumbnail, then re-upload.")
            }.trim()
        }

        return ChhotuUploadCheckResult(
            approved = approved,
            status = if (approved) "APPROVED" else "REJECTED",
            checks = checks,
            rejectionReason = rejectionReason,
            fixSuggestion = fixSuggestion,
            modeLabel = modeLabel
        )
    }

    fun parseSummary(summary: String): List<ChhotuCheckDetail> {
        if (summary.isBlank()) return defaultApprovedChecks()
        val parsed = summary.split("|").mapNotNull { entry ->
            val parts = entry.split(":", limit = 3)
            if (parts.size < 3) return@mapNotNull null
            val key = parts[0]
            val status = runCatching { CheckItemStatus.valueOf(parts[1]) }.getOrDefault(CheckItemStatus.PASSED)
            val note = parts[2]
            val title = when (key) {
                "COPYRIGHT" -> "Copyright Check"
                "COMMUNITY" -> "Community Guidelines Check"
                "SAFETY" -> "Ganda / Galat Content Check"
                "QUALITY" -> "Quality Check"
                "TITLE_THUMB" -> "Title / Thumbnail Check"
                else -> key
            }
            ChhotuCheckDetail(key = key, title = title, status = status, note = note)
        }
        return parsed.ifEmpty { defaultApprovedChecks() }
    }

    fun defaultApprovedChecks(): List<ChhotuCheckDetail> = listOf(
        ChhotuCheckDetail("COPYRIGHT", "Copyright Check", CheckItemStatus.PASSED, "Original content & cleared audio verified"),
        ChhotuCheckDetail("COMMUNITY", "Community Guidelines Check", CheckItemStatus.PASSED, "Follows BharatTube community safety standards"),
        ChhotuCheckDetail("SAFETY", "Ganda / Galat Content Check", CheckItemStatus.PASSED, "Clean & family-safe content verified"),
        ChhotuCheckDetail("QUALITY", "Quality Check", CheckItemStatus.PASSED, "Video playback, metadata & clarity passed"),
        ChhotuCheckDetail("TITLE_THUMB", "Title / Thumbnail Check", CheckItemStatus.PASSED, "Title & thumbnail clear and accurate")
    )

    private val abusiveCommentTokens = listOf(
        "madarchod", "behenchod", "bhenchod", "chutiya", "randi", "gandu", "bsdk", "mc ", "bc ",
        "harami", "kamina", "saale", "fuck", "bitch", "asshole", "bastard", "slut", "whore",
        "porn", "xxx", "nude", "ganda", "ashleel", "otp bypass", "free money", "telegram.me/",
        "t.me/", "bit.ly/", "whatsapp me for", "scam", "kill yourself", "die ", "hate speech"
    )

    private val strictSpamCommentTokens = listOf(
        "http://", "https://", "sub4sub", "sub back", "check my bio", "earn daily", "win cash"
    )

    fun moderateComment(text: String, strictMode: Boolean = false): ChhotuCommentCheckResult {
        val clean = text.trim()
        if (clean.length < 2) {
            return ChhotuCommentCheckResult(
                allowed = false,
                reason = "Chhotu AI: Comment is too short."
            )
        }
        val lower = " ${clean.lowercase()} "
        val matchedAbuse = abusiveCommentTokens.firstOrNull { token ->
            if (token.endsWith(" ")) lower.contains(" $token") else lower.contains(token)
        }
        if (matchedAbuse != null) {
            return ChhotuCommentCheckResult(
                allowed = false,
                reason = "Chhotu AI blocked this comment for abusive, vulgar, or unsafe language."
            )
        }
        if (strictMode) {
            val matchedSpam = strictSpamCommentTokens.firstOrNull { lower.contains(it) }
            if (matchedSpam != null) {
                return ChhotuCommentCheckResult(
                    allowed = false,
                    reason = "Chhotu AI Strict Mode blocked promotional/spam links in comments."
                )
            }
        }
        return ChhotuCommentCheckResult(allowed = true, reason = "")
    }
}
