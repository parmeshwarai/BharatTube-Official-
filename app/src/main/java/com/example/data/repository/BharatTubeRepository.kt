package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.BharatTubeDao
import com.example.data.local.SeedDataPopulator
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AdCampaignEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CommentEntity
import com.example.data.model.CommunityPostEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PayoutMethod
import com.example.data.model.PayoutStatus
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistVideoCrossRef
import com.example.data.model.ReportEntity
import com.example.data.model.SearchHistoryEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.UploadStatus
import com.example.data.model.UploadTaskEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.data.model.VideoVisibility
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class BharatTubeRepository(
    private val context: Context,
    private val dao: BharatTubeDao,
    private val prefsRepo: UserPreferencesRepository
) {
    private val uploadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeUploadJobs = mutableMapOf<String, Job>()

    val preferences: Flow<UserPreferences> = prefsRepo.preferencesFlow
    val allUsers: Flow<List<UserProfileEntity>> = dao.observeAllUsers()
    val allChannels: Flow<List<ChannelEntity>> = dao.observeAllChannels()
    val allVideos: Flow<List<VideoEntity>> = dao.observeAllVideos()
    val adCampaigns: Flow<List<AdCampaignEntity>> = dao.observeAdCampaigns()
    val payouts: Flow<List<PayoutEntity>> = dao.observePayouts()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.observeAuditLogs()
    val reports: Flow<List<ReportEntity>> = dao.observeReports()
    val communityPosts: Flow<List<CommunityPostEntity>> = dao.observeCommunityPosts()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.observeAllChatMessages()
    val searchHistory: Flow<List<SearchHistoryEntity>> = dao.observeSearchHistory()
    val platformSettings: Flow<PlatformSettingsEntity?> = dao.observePlatformSettings()

    fun observeUser(uid: String): Flow<UserProfileEntity?> = dao.observeUser(uid)
    fun observeMyChannels(uid: String): Flow<List<ChannelEntity>> = dao.observeMyChannels(uid)
    fun observeChannel(channelId: String): Flow<ChannelEntity?> = dao.observeChannel(channelId)
    fun observeVideo(videoId: String): Flow<VideoEntity?> = dao.observeVideo(videoId)
    fun observeComments(videoId: String): Flow<List<CommentEntity>> = dao.observeCommentsForVideo(videoId)
    fun observePlaylists(uid: String): Flow<List<PlaylistEntity>> = dao.observePlaylists(uid)
    fun observePlaylistRefs(): Flow<List<PlaylistVideoCrossRef>> = dao.observePlaylistVideoRefs()
    fun observeWatchHistory(uid: String): Flow<List<WatchHistoryEntity>> = dao.observeWatchHistory(uid)
    fun observeUploads(uid: String): Flow<List<UploadTaskEntity>> = dao.observeUploads(uid)
    fun observeNotifications(uid: String): Flow<List<NotificationEntity>> = dao.observeNotifications(uid)
    fun observeSupportTickets(uid: String): Flow<List<SupportTicketEntity>> = dao.observeSupportTickets(uid)

    suspend fun initialize() {
        SeedDataPopulator.ensureSeeded(dao)
    }

    // Intelligent recommendation ranking based on user language, country, subscriptions & history
    fun rankVideosForUser(
        videos: List<VideoEntity>,
        channels: List<ChannelEntity>,
        history: List<WatchHistoryEntity>,
        preferredLanguage: String,
        preferredCountry: String,
        restrictedMode: Boolean
    ): List<VideoEntity> {
        val subscribedIds = channels.filter { it.isSubscribedByMe }.map { it.id }.toSet()
        val watchedVideoMap = videos.associateBy { it.id }
        val preferredCategories = history.mapNotNull { watchedVideoMap[it.videoId]?.category }
            .groupingBy { it }
            .eachCount()

        return videos
            .filter { it.visibility == VideoVisibility.PUBLIC.name }
            .filter { !restrictedMode || !it.title.contains("18+", ignoreCase = true) }
            .sortedByDescending { video ->
                var score = 0.0
                if (video.languageCode == preferredLanguage) score += 35.0
                if (video.countryCode == preferredCountry) score += 20.0
                if (video.channelId in subscribedIds) score += 45.0
                score += (preferredCategories[video.category] ?: 0) * 15.0
                score += kotlin.math.ln((video.viewCount + 1).toDouble()) * 4.0
                score += kotlin.math.ln((video.likeCount + 1).toDouble()) * 3.0
                if (video.isLive) score += 25.0
                score
            }
    }

    suspend fun setDarkTheme(enabled: Boolean) = prefsRepo.setDarkTheme(enabled)
    suspend fun setLanguage(code: String) = prefsRepo.setLanguage(code)
    suspend fun setCountry(code: String) = prefsRepo.setCountry(code)
    suspend fun setHistoryPaused(paused: Boolean) = prefsRepo.setHistoryPaused(paused)
    suspend fun setRestrictedMode(enabled: Boolean) = prefsRepo.setRestrictedMode(enabled)
    suspend fun setActiveChannel(channelId: String) = prefsRepo.setActiveChannel(channelId)

    suspend fun createOrSwitchLocalAccount(
        username: String,
        email: String,
        phone: String,
        countryCode: String,
        languageCode: String,
        isMinor: Boolean
    ): Result<UserProfileEntity> {
        if (username.isBlank()) return Result.failure(IllegalArgumentException("Username is required"))
        val cleanHandle = "@" + username.lowercase().replace(Regex("[^a-z0-9]"), "").take(16).ifEmpty { "creator" }
        val uid = "user_${UUID.randomUUID().toString().take(8)}"
        val profile = UserProfileEntity(
            uid = uid,
            username = username.trim(),
            handle = cleanHandle,
            email = email.trim(),
            phone = phone.trim(),
            authProvider = "Local Device Profile",
            bio = "BharatTube Creator",
            countryCode = countryCode,
            languageCode = languageCode,
            isMinor = isMinor,
            isAdmin = false
        )
        dao.upsertUser(profile)
        prefsRepo.setActiveUser(uid)
        prefsRepo.setLanguage(languageCode)
        prefsRepo.setCountry(countryCode)
        return Result.success(profile)
    }

    suspend fun switchExistingUser(uid: String) {
        dao.getUser(uid)?.let { user ->
            prefsRepo.setActiveUser(user.uid)
            prefsRepo.setLanguage(user.languageCode)
            prefsRepo.setCountry(user.countryCode)
        }
    }

    suspend fun updateUserProfile(user: UserProfileEntity) {
        dao.upsertUser(user)
    }

    suspend fun createChannel(
        ownerUserId: String,
        name: String,
        handle: String,
        description: String,
        category: String,
        socialLinks: String,
        avatarUri: String,
        bannerUri: String
    ): Result<ChannelEntity> {
        val normalizedHandle = if (handle.startsWith("@")) handle.trim() else "@${handle.trim()}"
        if (name.isBlank() || normalizedHandle.length < 3) {
            return Result.failure(IllegalArgumentException("Channel name and valid @handle are required"))
        }
        val existing = dao.findChannelByHandle(normalizedHandle)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Channel handle $normalizedHandle is already taken"))
        }
        val channel = ChannelEntity(
            id = "ch_${UUID.randomUUID().toString().take(8)}",
            ownerUserId = ownerUserId,
            name = name.trim(),
            handle = normalizedHandle,
            description = description.trim(),
            category = category,
            avatarUri = avatarUri,
            bannerUri = bannerUri.ifBlank { "drawable:img_hero_creators" },
            socialLinks = socialLinks.trim()
        )
        dao.upsertChannel(channel)
        prefsRepo.setActiveChannel(channel.id)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = ownerUserId,
                category = "CHANNEL",
                title = "Channel Created: ${channel.name}",
                body = "Your channel ${channel.handle} is live locally. Connect cloud storage to sync globally."
            )
        )
        return Result.success(channel)
    }

    suspend fun toggleSubscribe(channel: ChannelEntity, userId: String) {
        val newSub = !channel.isSubscribedByMe
        val delta = if (newSub) 1L else -1L
        dao.updateSubscription(channel.id, newSub, delta)
        if (newSub) {
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    category = "SUBSCRIBER",
                    title = "Subscribed to ${channel.name}",
                    body = "You'll receive alerts when ${channel.handle} publishes new videos and Shorts."
                )
            )
        }
    }

    suspend fun toggleChannelBell(channel: ChannelEntity) {
        dao.updateChannelNotification(channel.id, !channel.notificationsEnabled)
    }

    suspend fun recordVideoPlay(video: VideoEntity, userId: String, historyPaused: Boolean) {
        dao.incrementVideoView(video.id)
        if (!historyPaused) {
            dao.upsertWatchHistory(
                WatchHistoryEntity(
                    id = "hist_${userId}_${video.id}",
                    userId = userId,
                    videoId = video.id,
                    format = video.format,
                    progressMs = video.lastPositionMs,
                    watchedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun savePlaybackPosition(videoId: String, positionMs: Long) {
        dao.updatePlaybackPosition(videoId, positionMs.coerceAtLeast(0L))
    }

    suspend fun updatePlaybackQuality(videoId: String, quality: String) {
        dao.updatePlaybackQuality(videoId, quality)
    }

    suspend fun toggleLike(video: VideoEntity) {
        val nextLiked = !video.isLiked
        val delta = if (nextLiked) 1L else -1L
        dao.updateVideoLike(video.id, nextLiked, delta)
    }

    suspend fun toggleWatchLater(video: VideoEntity) {
        dao.updateWatchLater(video.id, !video.isWatchLater)
    }

    suspend fun addComment(videoId: String, user: UserProfileEntity, text: String, parentCommentId: String? = null) {
        if (user.commentRestricted || user.isBanned) return
        if (text.isBlank()) return
        dao.insertComment(
            CommentEntity(
                id = "cmt_${UUID.randomUUID().toString().take(8)}",
                videoId = videoId,
                parentCommentId = parentCommentId,
                authorUserId = user.uid,
                authorName = user.username,
                text = text.trim(),
                originalLanguage = user.languageCode
            )
        )
    }

    suspend fun likeComment(commentId: String) = dao.likeComment(commentId)

    suspend fun createPlaylist(userId: String, name: String, description: String, isPrivate: Boolean) {
        if (name.isBlank()) return
        dao.upsertPlaylist(
            PlaylistEntity(
                id = "pl_${UUID.randomUUID().toString().take(8)}",
                ownerUserId = userId,
                name = name.trim(),
                description = description.trim(),
                isPrivate = isPrivate
            )
        )
    }

    suspend fun deletePlaylist(playlistId: String) = dao.deletePlaylist(playlistId)

    suspend fun addVideoToPlaylist(playlistId: String, videoId: String) {
        dao.addVideoToPlaylist(PlaylistVideoCrossRef(playlistId, videoId))
    }

    suspend fun clearWatchHistory(userId: String) = dao.clearWatchHistory(userId)

    suspend fun recordSearch(query: String) {
        if (query.isNotBlank()) {
            dao.insertSearchHistory(SearchHistoryEntity(query.trim()))
        }
    }

    suspend fun clearSearchHistory() = dao.clearSearchHistory()

    suspend fun markAllNotificationsRead(userId: String) = dao.markAllNotificationsRead(userId)

    // Resumable Chunked Upload Queue
    fun resolveFileSizeBytes(uriString: String): Long {
        if (uriString.isBlank() || uriString.startsWith("http")) return 28_500_000L
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (idx >= 0) cursor.getLong(idx).takeIf { it > 0L } else null
                } else null
            } ?: 24_000_000L
        } catch (_: Exception) {
            24_000_000L
        }
    }

    suspend fun startChunkedUpload(
        user: UserProfileEntity,
        channel: ChannelEntity,
        title: String,
        description: String,
        videoUri: String,
        thumbnailUri: String,
        format: VideoFormat,
        visibility: VideoVisibility,
        category: String,
        hashtags: String,
        languageCode: String,
        countryCode: String
    ): Result<UploadTaskEntity> {
        if (user.isBanned || user.uploadRestricted) {
            return Result.failure(IllegalStateException("Your account is currently restricted from uploading."))
        }
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.videoUploadsEnabled) {
            return Result.failure(IllegalStateException("Uploads are temporarily disabled by Super Admin."))
        }
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Video title cannot be empty."))
        }
        val effectiveUri = videoUri.ifBlank {
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        }
        val totalBytes = resolveFileSizeBytes(effectiveUri)
        val task = UploadTaskEntity(
            id = "upl_${UUID.randomUUID().toString().take(8)}",
            ownerUserId = user.uid,
            channelId = channel.id,
            title = title.trim(),
            description = description.trim(),
            fileUri = effectiveUri,
            thumbnailUri = thumbnailUri.ifBlank {
                when (category) {
                    "Music" -> "drawable:img_thumb_music"
                    "Travel" -> "drawable:img_thumb_travel"
                    else -> "drawable:img_thumb_tech"
                }
            },
            format = format.name,
            visibility = visibility.name,
            category = category,
            hashtags = hashtags.ifBlank { "#BharatTube" },
            languageCode = languageCode,
            countryCode = countryCode,
            totalBytes = totalBytes,
            uploadedBytes = 0L,
            speedBytesPerSec = 2_400_000L,
            status = UploadStatus.UPLOADING.name
        )
        dao.upsertUploadTask(task)
        launchUploadWorker(task.id, channel)
        return Result.success(task)
    }

    private fun launchUploadWorker(taskId: String, channel: ChannelEntity) {
        activeUploadJobs[taskId]?.cancel()
        activeUploadJobs[taskId] = uploadScope.launch {
            while (true) {
                delay(450L)
                val current = dao.getUploadTask(taskId) ?: break
                if (current.status != UploadStatus.UPLOADING.name) break

                val chunkSize = (current.totalBytes / 12L).coerceAtLeast(512_000L)
                val nextUploaded = (current.uploadedBytes + chunkSize).coerceAtMost(current.totalBytes)
                if (nextUploaded >= current.totalBytes) {
                    dao.upsertUploadTask(
                        current.copy(
                            uploadedBytes = current.totalBytes,
                            status = UploadStatus.TRANSCODING.name
                        )
                    )
                    delay(700L)
                    val completed = current.copy(
                        uploadedBytes = current.totalBytes,
                        status = UploadStatus.COMPLETED.name
                    )
                    dao.upsertUploadTask(completed)
                    val video = VideoEntity(
                        id = "vid_${UUID.randomUUID().toString().take(8)}",
                        channelId = channel.id,
                        channelName = channel.name,
                        channelHandle = channel.handle,
                        title = completed.title,
                        description = completed.description,
                        thumbnailKey = completed.thumbnailUri,
                        videoUri = completed.fileUri,
                        category = completed.category,
                        hashtags = completed.hashtags,
                        languageCode = completed.languageCode,
                        countryCode = completed.countryCode,
                        format = completed.format,
                        visibility = completed.visibility,
                        durationSeconds = if (completed.format == VideoFormat.SHORT.name) 45 else 320,
                        captionsText = "Auto-captions pending cloud transcoding configuration.",
                        copyrightStatus = "SELF_DECLARED_CLEARED"
                    )
                    dao.upsertVideo(video)
                    dao.insertNotification(
                        NotificationEntity(
                            id = "notif_${UUID.randomUUID().toString().take(8)}",
                            userId = completed.ownerUserId,
                            category = "UPLOAD",
                            title = "Upload Ready: ${completed.title}",
                            body = "Saved to local BharatTube library (${completed.visibility}). Cloud CDN distribution awaits backend setup."
                        )
                    )
                    break
                } else {
                    dao.upsertUploadTask(
                        current.copy(
                            uploadedBytes = nextUploaded,
                            speedBytesPerSec = 2_200_000L + (nextUploaded % 600_000L)
                        )
                    )
                }
            }
        }
    }

    suspend fun pauseUpload(taskId: String) {
        activeUploadJobs[taskId]?.cancel()
        dao.getUploadTask(taskId)?.let {
            if (it.status == UploadStatus.UPLOADING.name) {
                dao.upsertUploadTask(it.copy(status = UploadStatus.PAUSED.name))
            }
        }
    }

    suspend fun resumeUpload(taskId: String, channel: ChannelEntity) {
        dao.getUploadTask(taskId)?.let {
            if (it.status == UploadStatus.PAUSED.name || it.status == UploadStatus.FAILED.name) {
                dao.upsertUploadTask(it.copy(status = UploadStatus.UPLOADING.name, errorMessage = ""))
                launchUploadWorker(taskId, channel)
            }
        }
    }

    suspend fun cancelUpload(taskId: String) {
        activeUploadJobs[taskId]?.cancel()
        dao.getUploadTask(taskId)?.let {
            dao.upsertUploadTask(it.copy(status = UploadStatus.CANCELLED.name))
        }
    }

    // Creator Payout Validation & Initiation (India Minimum ₹1,000, KYC + Payment + Tax + Duplicate Guard)
    suspend fun initiatePayout(
        user: UserProfileEntity,
        channel: ChannelEntity,
        availableBalanceInr: Double,
        method: PayoutMethod
    ): Result<PayoutEntity> {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.automaticPayoutEnabled || !settings.creatorMonetizationEnabled) {
            return Result.failure(IllegalStateException("Payouts are currently paused by platform administration. Balances remain preserved."))
        }
        val country = BharatTubeCatalog.countries.firstOrNull { it.code == user.countryCode } ?: BharatTubeCatalog.countries.first()
        val thresholdInr = if (country.code == "IN") settings.indiaPayoutThresholdInr else country.minPayoutThresholdLocal / country.exchangeRateFromInr
        if (availableBalanceInr < thresholdInr) {
            return Result.failure(
                IllegalStateException(
                    "Available balance ₹${String.format("%.2f", availableBalanceInr)} is below the ${country.name} minimum threshold of ₹${String.format("%.0f", thresholdInr)}."
                )
            )
        }
        if (!user.kycVerified || !user.paymentAccountVerified || !user.taxVerified) {
            return Result.failure(
                IllegalStateException("KYC, payment account, and tax verification are all required before initiating a payout.")
            )
        }
        val activeCount = dao.countActivePayoutsForChannel(channel.id)
        if (activeCount > 0) {
            return Result.failure(IllegalStateException("Duplicate payout blocked: an active payout is already awaiting provider confirmation."))
        }
        val destination = when (method) {
            PayoutMethod.UPI -> user.upiId.ifBlank { "UPI Not Set" }
            PayoutMethod.BANK_TRANSFER, PayoutMethod.INTERNATIONAL_SWIFT -> user.bankAccountMasked.ifBlank { "Bank Not Set" }
        }
        val payout = PayoutEntity(
            id = "pay_${UUID.randomUUID().toString().take(8)}",
            channelId = channel.id,
            userId = user.uid,
            amountInr = availableBalanceInr,
            method = method.name,
            destinationMasked = destination,
            transactionId = "BT-REQ-${System.currentTimeMillis().toString().takeLast(8)}",
            status = PayoutStatus.INITIATED.name
        )
        dao.upsertPayout(payout)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = user.uid,
                category = "PAYOUT",
                title = "Payout Initiated (${payout.transactionId})",
                body = "₹${String.format("%.2f", availableBalanceInr)} queued via ${method.label}. Completion awaits external payment gateway confirmation."
            )
        )
        return Result.success(payout)
    }

    // Chat & Minor Safety
    suspend fun sendChatMessage(
        roomId: String,
        roomTitle: String,
        isGroup: Boolean,
        user: UserProfileEntity,
        text: String,
        voiceDurationSec: Int = 0
    ): Result<Unit> {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.chatEnabled) {
            return Result.failure(IllegalStateException("Chat is disabled by Super Admin."))
        }
        if (user.isMinor) {
            return Result.failure(IllegalStateException("Direct & public messaging is restricted for minor accounts."))
        }
        if (user.isBanned) {
            return Result.failure(IllegalStateException("Account is suspended."))
        }
        if (text.isBlank() && voiceDurationSec <= 0) {
            return Result.failure(IllegalArgumentException("Enter a message."))
        }
        dao.insertChatMessage(
            ChatMessageEntity(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                roomId = roomId,
                roomTitle = roomTitle,
                isGroup = isGroup,
                senderId = user.uid,
                senderName = user.username,
                text = text.trim(),
                voiceDurationSec = voiceDurationSec
            )
        )
        return Result.success(Unit)
    }

    // Advertiser Campaigns
    suspend fun createAdCampaign(
        advertiserName: String,
        title: String,
        targetCountry: String,
        targetLanguage: String,
        placement: String,
        budgetInr: Double
    ): Result<AdCampaignEntity> {
        if (advertiserName.isBlank() || title.isBlank() || budgetInr < 500.0) {
            return Result.failure(IllegalArgumentException("Provide advertiser name, campaign title, and minimum ₹500 budget."))
        }
        val campaign = AdCampaignEntity(
            id = "cmp_${UUID.randomUUID().toString().take(8)}",
            advertiserName = advertiserName.trim(),
            title = title.trim(),
            targetCountry = targetCountry,
            targetLanguage = targetLanguage,
            placement = placement,
            budgetInr = budgetInr,
            approvalStatus = "PENDING_ADMIN_REVIEW"
        )
        dao.upsertAdCampaign(campaign)
        return Result.success(campaign)
    }

    suspend fun reviewAdCampaign(adminId: String, campaign: AdCampaignEntity, approved: Boolean) {
        val newStatus = if (approved) "APPROVED_TEST_MODE" else "REJECTED"
        dao.upsertAdCampaign(campaign.copy(approvalStatus = newStatus))
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = adminId,
                action = "AD_CAMPAIGN_REVIEW",
                details = "Campaign '${campaign.title}' reviewed",
                previousValue = campaign.approvalStatus,
                newValue = newStatus
            )
        )
    }

    // Super Admin Controls + Audit Logging
    suspend fun updatePlatformSettings(
        admin: UserProfileEntity,
        updated: PlatformSettingsEntity,
        actionLabel: String,
        details: String,
        previousValue: String,
        newValue: String
    ): Result<Unit> {
        if (!admin.isAdmin) {
            return Result.failure(SecurityException("Only authorized Super Admins can modify platform settings."))
        }
        if (updated.creatorSharePct + updated.platformSharePct != 100) {
            return Result.failure(IllegalArgumentException("Creator % + BharatTube % must equal 100%."))
        }
        dao.updatePlatformSettings(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = actionLabel,
                details = details,
                previousValue = previousValue,
                newValue = newValue
            )
        )
        return Result.success(Unit)
    }

    suspend fun moderateUser(admin: UserProfileEntity, target: UserProfileEntity, banned: Boolean, reason: String) {
        if (!admin.isAdmin) return
        dao.upsertUser(target.copy(isBanned = banned, banReason = if (banned) reason else ""))
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = if (banned) "USER_BANNED" else "USER_RESTORED",
                details = "Account ${target.handle} (${target.uid}): $reason",
                previousValue = "isBanned=${target.isBanned}",
                newValue = "isBanned=$banned"
            )
        )
    }

    suspend fun submitReport(
        reporterId: String,
        targetType: String,
        targetId: String,
        targetTitle: String,
        reason: String,
        details: String
    ) {
        dao.upsertReport(
            ReportEntity(
                id = "rep_${UUID.randomUUID().toString().take(8)}",
                reporterId = reporterId,
                targetType = targetType,
                targetId = targetId,
                targetTitle = targetTitle,
                reason = reason,
                details = details.trim()
            )
        )
    }

    suspend fun resolveReport(admin: UserProfileEntity, report: ReportEntity, resolutionStatus: String) {
        if (!admin.isAdmin) return
        dao.upsertReport(report.copy(status = resolutionStatus))
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = "REPORT_RESOLVED",
                details = "Report ${report.id} on ${report.targetTitle}",
                previousValue = report.status,
                newValue = resolutionStatus
            )
        )
    }

    suspend fun submitSupportTicket(userId: String, category: String, subject: String, message: String) {
        if (subject.isBlank() || message.isBlank()) return
        dao.upsertSupportTicket(
            SupportTicketEntity(
                id = "tkt_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                category = category,
                subject = subject.trim(),
                message = message.trim()
            )
        )
    }

    suspend fun createCommunityPost(channel: ChannelEntity, text: String) {
        if (text.isBlank()) return
        dao.insertCommunityPost(
            CommunityPostEntity(
                id = "post_${UUID.randomUUID().toString().take(8)}",
                channelId = channel.id,
                channelName = channel.name,
                text = text.trim()
            )
        )
    }
}
