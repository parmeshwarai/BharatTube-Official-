package com.example.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.chhotuai.ChhotuAiBackendClient
import com.example.chhotuai.ChhotuDiagnosticsEngine
import com.example.chhotuai.ChhotuSecurityGuard
import com.example.chhotuai.ChhotuUploadModerator
import com.example.chhotuai.ChhotuUserSupportEngine
import com.example.chhotuai.CheckStatus
import com.example.chhotuai.SystemHealthSnapshot
import com.example.data.local.BharatTubeDao
import com.example.data.local.SeedDataPopulator
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AdCampaignEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.ChannelEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChhotuChatMessageEntity
import com.example.data.model.ChhotuErrorReportEntity
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
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class BharatTubeRepository(
    private val context: Context,
    private val dao: BharatTubeDao,
    private val prefsRepo: UserPreferencesRepository
) {
    private val uploadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeUploadJobs = mutableMapOf<String, Job>()
    private val chhotuBackendClient = ChhotuAiBackendClient()
    private val chhotuDiagnosticsEngine = ChhotuDiagnosticsEngine(context, dao, chhotuBackendClient)
    private val pendingOtps = mutableMapOf<String, Pair<String, Long>>()

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
    val chhotuErrorReports: Flow<List<ChhotuErrorReportEntity>> = dao.observeChhotuErrorReports()

    fun observeUser(uid: String): Flow<UserProfileEntity?> = dao.observeUser(uid)
    fun observeMyChannels(uid: String): Flow<List<ChannelEntity>> = dao.observeMyChannels(uid)
    fun observeChannel(channelId: String): Flow<ChannelEntity?> = dao.observeChannel(channelId)
    fun observeVideo(videoId: String): Flow<VideoEntity?> = dao.observeVideo(videoId)
    val allComments: Flow<List<CommentEntity>> = dao.observeAllComments()
    fun observeComments(videoId: String): Flow<List<CommentEntity>> = dao.observeCommentsForVideo(videoId)
    fun observePlaylists(uid: String): Flow<List<PlaylistEntity>> = dao.observePlaylists(uid)
    fun observePlaylistRefs(): Flow<List<PlaylistVideoCrossRef>> = dao.observePlaylistVideoRefs()
    fun observeWatchHistory(uid: String): Flow<List<WatchHistoryEntity>> = dao.observeWatchHistory(uid)
    fun observeUploads(uid: String): Flow<List<UploadTaskEntity>> = dao.observeUploads(uid)
    fun observeNotifications(uid: String): Flow<List<NotificationEntity>> = dao.observeNotifications(uid)
    fun observeSupportTickets(uid: String): Flow<List<SupportTicketEntity>> = dao.observeSupportTickets(uid)
    fun observeChhotuChatHistory(uid: String): Flow<List<ChhotuChatMessageEntity>> = dao.observeChhotuChatHistory(uid)

    suspend fun initialize() {
        SeedDataPopulator.ensureSeeded(dao)
    }

    // Intelligent recommendation ranking: newly uploaded user videos appear at the top of Home Page
    fun rankVideosForUser(
        videos: List<VideoEntity>,
        channels: List<ChannelEntity>,
        history: List<WatchHistoryEntity>,
        preferredLanguage: String,
        preferredCountry: String,
        restrictedMode: Boolean,
        myChannelIds: Set<String> = emptySet(),
        boostedUserIds: Map<String, Int> = emptyMap(),
        homePageBoostEnabled: Boolean = true,
        workBoostPercent: Int = 30
    ): List<VideoEntity> {
        val subscribedIds = channels.filter { it.isSubscribedByMe }.map { it.id }.toSet()
        val channelOwnerMap = channels.associate { it.id to it.ownerUserId }
        val watchedVideoMap = videos.associateBy { it.id }
        val preferredCategories = history.mapNotNull { watchedVideoMap[it.videoId]?.category }
            .groupingBy { it }
            .eachCount()

        return videos
            .filter { it.visibility == VideoVisibility.PUBLIC.name && it.chhotuCheckStatus == "APPROVED" }
            .filter { !restrictedMode || !it.title.contains("18+", ignoreCase = true) }
            .sortedByDescending { video ->
                val isUserUploaded = video.id.startsWith("vid_upl_") || video.channelId in myChannelIds
                if (isUserUploaded) {
                    // Always place newly uploaded videos right at the top sorted by newest upload time
                    1_000_000_000_000.0 + video.createdAt.toDouble()
                } else {
                    var score = 0.0
                    if (video.languageCode == preferredLanguage) score += 35.0
                    if (video.countryCode == preferredCountry) score += 20.0
                    if (video.channelId in subscribedIds) score += 45.0
                    score += (preferredCategories[video.category] ?: 0) * 15.0
                    score += kotlin.math.ln((video.viewCount + 1).toDouble()) * 4.0
                    score += kotlin.math.ln((video.likeCount + 1).toDouble()) * 3.0
                    if (video.isLive) score += 25.0
                    if (homePageBoostEnabled) {
                        val ownerId = channelOwnerMap[video.channelId]
                        val userBoost = boostedUserIds[ownerId] ?: 0
                        if (userBoost > 0) {
                            score += userBoost * (1.0 + workBoostPercent / 100.0) * 2.0
                        }
                    }
                    score
                }
            }
    }

    suspend fun setDarkTheme(enabled: Boolean) = prefsRepo.setDarkTheme(enabled)
    suspend fun setLanguage(code: String) = prefsRepo.setLanguage(code)
    suspend fun setCountry(code: String) = prefsRepo.setCountry(code)
    suspend fun setHistoryPaused(paused: Boolean) = prefsRepo.setHistoryPaused(paused)
    suspend fun setRestrictedMode(enabled: Boolean) = prefsRepo.setRestrictedMode(enabled)
    suspend fun setActiveChannel(channelId: String) = prefsRepo.setActiveChannel(channelId)
    suspend fun setPushOptIn(enabled: Boolean) = prefsRepo.setPushOptIn(enabled)
    suspend fun setNotifyUploads(enabled: Boolean) = prefsRepo.setNotifyUploads(enabled)
    suspend fun setNotifyComments(enabled: Boolean) = prefsRepo.setNotifyComments(enabled)
    suspend fun setNotifyLive(enabled: Boolean) = prefsRepo.setNotifyLive(enabled)
    suspend fun setPlaybackAutoPlayNext(enabled: Boolean) = prefsRepo.setPlaybackAutoPlayNext(enabled)
    suspend fun setPlaybackDefaultQuality(quality: String) = prefsRepo.setPlaybackDefaultQuality(quality)
    suspend fun setPlaybackCaptionsEnabled(enabled: Boolean) = prefsRepo.setPlaybackCaptionsEnabled(enabled)
    suspend fun setPlaybackDataSaver(enabled: Boolean) = prefsRepo.setPlaybackDataSaver(enabled)
    suspend fun setDownloadsWifiOnly(enabled: Boolean) = prefsRepo.setDownloadsWifiOnly(enabled)
    suspend fun setDownloadsQuality(quality: String) = prefsRepo.setDownloadsQuality(quality)
    suspend fun toggleDownloadedVideo(videoId: String): Boolean = prefsRepo.toggleDownloadedVideo(videoId)
    suspend fun clearDownloadedVideos() = prefsRepo.clearDownloadedVideos()
    suspend fun setPrivacySubscriptionsPrivate(isPrivate: Boolean) = prefsRepo.setPrivacySubscriptionsPrivate(isPrivate)
    suspend fun setPrivacyPlaylistsPrivate(isPrivate: Boolean) = prefsRepo.setPrivacyPlaylistsPrivate(isPrivate)
    suspend fun signOut() = prefsRepo.setSignedOut(true)
    suspend fun signInBack() = prefsRepo.setSignedOut(false)

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
            val userChannels = dao.getAllChannelsSync().filter { it.ownerUserId == user.uid }
            userChannels.firstOrNull()?.let { prefsRepo.setActiveChannel(it.id) }
        }
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt::$password".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun ensureDefaultChannelForUser(user: UserProfileEntity) {
        val existing = dao.getAllChannelsSync().firstOrNull { it.ownerUserId == user.uid }
        if (existing != null) {
            prefsRepo.setActiveChannel(existing.id)
            return
        }
        val cleanHandle = user.handle.ifBlank { "@creator_${user.uid.takeLast(4)}" }
        val channel = ChannelEntity(
            id = "ch_${UUID.randomUUID().toString().take(8)}",
            ownerUserId = user.uid,
            name = user.username,
            handle = cleanHandle,
            description = "Official BharatTube channel of ${user.username}",
            category = "Entertainment",
            bannerUri = user.bannerUri.ifBlank { "drawable:img_hero_creators" },
            avatarUri = user.avatarUri
        )
        dao.upsertChannel(channel)
        prefsRepo.setActiveChannel(channel.id)
    }

    suspend fun signInWithGoogleAccount(name: String, email: String): Result<UserProfileEntity> {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()
        if (cleanName.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Enter a valid Google name and email address."))
        }
        val existing = dao.findUserByEmail(cleanEmail)
        if (existing != null) {
            if (existing.isBanned) return Result.failure(IllegalStateException("Account suspended: ${existing.banReason}"))
            val updated = existing.copy(authProvider = "Google Sign-In")
            dao.upsertUser(updated)
            prefsRepo.setActiveUser(updated.uid)
            ensureDefaultChannelForUser(updated)
            return Result.success(updated)
        }
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.userRegistrationEnabled) {
            return Result.failure(IllegalStateException("New registrations are currently paused by Owner Control Center."))
        }
        val uid = "user_${UUID.randomUUID().toString().take(8)}"
        val baseHandle = cleanName.lowercase().replace(Regex("[^a-z0-9]"), "").take(14).ifEmpty { "creator" }
        val profile = UserProfileEntity(
            uid = uid,
            username = cleanName,
            handle = "@$baseHandle",
            email = cleanEmail,
            authProvider = "Google Sign-In",
            bio = "BharatTube Creator",
            countryCode = "IN",
            languageCode = "hi"
        )
        dao.upsertUser(profile)
        prefsRepo.setActiveUser(profile.uid)
        ensureDefaultChannelForUser(profile)
        return Result.success(profile)
    }

    suspend fun requestAuthOtp(identifier: String): Result<String> {
        val key = identifier.trim().lowercase()
        if (key.length < 5) {
            return Result.failure(IllegalArgumentException("Enter a valid mobile number or email."))
        }
        val code = (100000 + SecureRandom().nextInt(900000)).toString()
        pendingOtps[key] = code to (System.currentTimeMillis() + 5 * 60_000L)
        return Result.success(code)
    }

    suspend fun verifyPhoneOtpLogin(phone: String, otp: String, displayName: String): Result<UserProfileEntity> {
        val normalizedPhone = phone.trim()
        if (normalizedPhone.length < 8) {
            return Result.failure(IllegalArgumentException("Enter a valid mobile number."))
        }
        val pending = pendingOtps[normalizedPhone.lowercase()]
            ?: return Result.failure(IllegalStateException("Please tap Send OTP first."))
        if (System.currentTimeMillis() > pending.second) {
            pendingOtps.remove(normalizedPhone.lowercase())
            return Result.failure(IllegalStateException("OTP expired. Request a new OTP."))
        }
        if (pending.first != otp.trim()) {
            return Result.failure(IllegalArgumentException("Invalid 6-digit OTP."))
        }
        pendingOtps.remove(normalizedPhone.lowercase())

        val existing = dao.findUserByPhone(normalizedPhone)
        if (existing != null) {
            if (existing.isBanned) return Result.failure(IllegalStateException("Account suspended: ${existing.banReason}"))
            prefsRepo.setActiveUser(existing.uid)
            ensureDefaultChannelForUser(existing)
            return Result.success(existing)
        }
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.userRegistrationEnabled) {
            return Result.failure(IllegalStateException("New registrations are paused by Owner Control Center."))
        }
        val name = displayName.trim().ifBlank { "Creator ${normalizedPhone.takeLast(4)}" }
        val uid = "user_${UUID.randomUUID().toString().take(8)}"
        val handle = "@bt_${normalizedPhone.filter { it.isDigit() }.takeLast(6).ifEmpty { uid.takeLast(5) }}"
        val profile = UserProfileEntity(
            uid = uid,
            username = name,
            handle = handle,
            phone = normalizedPhone,
            authProvider = "Mobile OTP",
            bio = "BharatTube Creator",
            countryCode = "IN",
            languageCode = "hi"
        )
        dao.upsertUser(profile)
        prefsRepo.setActiveUser(profile.uid)
        ensureDefaultChannelForUser(profile)
        return Result.success(profile)
    }

    suspend fun registerWithEmail(name: String, email: String, password: String): Result<UserProfileEntity> {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.userRegistrationEnabled) {
            return Result.failure(IllegalStateException("New sign-ups are paused by Owner Control Center."))
        }
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        if (cleanName.length < 2) return Result.failure(IllegalArgumentException("Enter your full name."))
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (dao.findUserByEmail(cleanEmail) != null) {
            return Result.failure(IllegalStateException("An account with $cleanEmail already exists. Please Login."))
        }
        val uid = "user_${UUID.randomUUID().toString().take(8)}"
        val baseHandle = cleanName.lowercase().replace(Regex("[^a-z0-9]"), "").take(14).ifEmpty { "creator" }
        val profile = UserProfileEntity(
            uid = uid,
            username = cleanName,
            handle = "@$baseHandle",
            email = cleanEmail,
            passwordHash = hashPassword(password, uid),
            authProvider = "Email & Password",
            bio = "BharatTube Creator",
            countryCode = "IN",
            languageCode = "hi"
        )
        dao.upsertUser(profile)
        prefsRepo.setActiveUser(profile.uid)
        ensureDefaultChannelForUser(profile)
        return Result.success(profile)
    }

    suspend fun loginWithEmail(email: String, password: String): Result<UserProfileEntity> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Enter both email and password."))
        }
        val user = dao.findUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalStateException("No account found for $cleanEmail."))
        if (user.isBanned) {
            return Result.failure(IllegalStateException("Account suspended: ${user.banReason}"))
        }
        if (user.passwordHash.isBlank()) {
            return Result.failure(IllegalStateException("This account has no password set. Use Forgot Password or Google Login."))
        }
        val expected = hashPassword(password, user.uid)
        if (expected != user.passwordHash) {
            return Result.failure(IllegalArgumentException("Incorrect password."))
        }
        prefsRepo.setActiveUser(user.uid)
        ensureDefaultChannelForUser(user)
        return Result.success(user)
    }

    suspend fun resetPasswordWithOtp(identifier: String, otp: String, newPassword: String): Result<UserProfileEntity> {
        val key = identifier.trim().lowercase()
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }
        val pending = pendingOtps[key]
            ?: return Result.failure(IllegalStateException("Send a verification OTP first."))
        if (System.currentTimeMillis() > pending.second || pending.first != otp.trim()) {
            return Result.failure(IllegalArgumentException("Invalid or expired OTP."))
        }
        val user = dao.findUserByEmail(key) ?: dao.findUserByPhone(identifier.trim())
            ?: return Result.failure(IllegalStateException("No matching account found for $identifier."))
        pendingOtps.remove(key)
        val updated = user.copy(
            passwordHash = hashPassword(newPassword, user.uid),
            authProvider = if (user.authProvider.isBlank()) "Email & Password" else user.authProvider
        )
        dao.upsertUser(updated)
        prefsRepo.setActiveUser(updated.uid)
        ensureDefaultChannelForUser(updated)
        return Result.success(updated)
    }

    suspend fun updateUserProfile(user: UserProfileEntity) {
        dao.upsertUser(user)
    }

    suspend fun updateChannel(channel: ChannelEntity) {
        dao.upsertChannel(channel)
        dao.updateChannelInfoOnVideos(channel.id, channel.name, channel.handle)
    }

    suspend fun updateChannelDetails(
        channel: ChannelEntity,
        name: String,
        handle: String,
        description: String,
        socialLinks: String,
        avatarUri: String,
        bannerUri: String
    ): Result<ChannelEntity> {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        val owner = dao.getUser(channel.ownerUserId)
        if (!settings.profileControlEnabled && owner?.isAdmin != true) {
            return Result.failure(IllegalStateException("Profile editing is temporarily paused by Owner Control Center."))
        }
        val cleanName = name.trim()
        val rawHandle = handle.trim().removePrefix("@").replace(Regex("[^a-zA-Z0-9_.]"), "")
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Channel name cannot be empty"))
        }
        if (rawHandle.length < 3) {
            return Result.failure(IllegalArgumentException("Handle must be at least 3 characters"))
        }
        val normalizedHandle = "@$rawHandle"
        val existing = dao.findChannelByHandle(normalizedHandle)
        if (existing != null && existing.id != channel.id) {
            return Result.failure(IllegalArgumentException("Handle $normalizedHandle is already taken"))
        }
        val updated = channel.copy(
            name = cleanName,
            handle = normalizedHandle,
            description = description.trim().take(500),
            socialLinks = socialLinks.trim(),
            avatarUri = avatarUri,
            bannerUri = bannerUri.ifBlank { "drawable:img_hero_creators" }
        )
        dao.upsertChannel(updated)
        dao.updateChannelInfoOnVideos(updated.id, updated.name, updated.handle)
        owner?.let {
            dao.upsertUser(
                it.copy(
                    avatarUri = updated.avatarUri,
                    bannerUri = updated.bannerUri
                )
            )
        }
        return Result.success(updated)
    }

    suspend fun updateVideoMetadata(
        videoId: String,
        title: String,
        description: String,
        visibility: VideoVisibility,
        category: String,
        hashtags: String? = null,
        thumbnailKey: String? = null,
        scheduledAtEpochMs: Long? = null
    ): Result<Unit> {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Video title cannot be empty"))
        }
        val existing = dao.getVideo(videoId)
            ?: return Result.failure(IllegalStateException("Video not found"))
        dao.upsertVideo(
            existing.copy(
                title = cleanTitle,
                description = description.trim(),
                visibility = visibility.name,
                category = category,
                hashtags = hashtags?.trim() ?: existing.hashtags,
                thumbnailKey = thumbnailKey?.takeIf { it.isNotBlank() } ?: existing.thumbnailKey,
                scheduledAtEpochMs = scheduledAtEpochMs ?: existing.scheduledAtEpochMs
            )
        )
        return Result.success(Unit)
    }

    suspend fun setVideoVisibility(videoId: String, visibility: VideoVisibility) {
        dao.updateVideoVisibility(videoId, visibility.name)
    }

    suspend fun deleteVideo(videoId: String) {
        dao.deleteVideo(videoId)
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
        dao.addWatchTimeMinutes(video.id, if (video.format == VideoFormat.SHORT.name) 1L else 4L)
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
        runCatching { evaluateAutoPayoutForChannel(video.channelId) }
    }

    suspend fun recordVideoShare(videoId: String) {
        dao.incrementVideoShare(videoId)
    }

    suspend fun savePlaybackPosition(videoId: String, positionMs: Long) {
        dao.updatePlaybackPosition(videoId, positionMs.coerceAtLeast(0L))
    }

    suspend fun updatePlaybackQuality(videoId: String, quality: String) {
        dao.updatePlaybackQuality(videoId, quality)
    }

    suspend fun toggleLike(video: VideoEntity, actorName: String = "A viewer") {
        val nextLiked = !video.isLiked
        val likeDelta = if (nextLiked) 1L else -1L
        val clearDislike = nextLiked && video.isDisliked
        val nextDisliked = if (clearDislike) false else video.isDisliked
        val dislikeDelta = if (clearDislike) -1L else 0L
        dao.updateVideoLikeAndDislike(video.id, nextLiked, likeDelta, nextDisliked, dislikeDelta)
        if (nextLiked) {
            val channel = dao.getAllChannelsSync().firstOrNull { it.id == video.channelId }
            if (channel != null) {
                dao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        userId = channel.ownerUserId,
                        category = "LIKE",
                        title = "New Like on '${video.title.take(36)}'",
                        body = "$actorName liked your video (${video.likeCount + 1} likes)."
                    )
                )
            }
        }
    }

    suspend fun toggleDislike(video: VideoEntity) {
        val nextDisliked = !video.isDisliked
        val dislikeDelta = if (nextDisliked) 1L else -1L
        val clearLike = nextDisliked && video.isLiked
        val nextLiked = if (clearLike) false else video.isLiked
        val likeDelta = if (clearLike) -1L else 0L
        dao.updateVideoLikeAndDislike(video.id, nextLiked, likeDelta, nextDisliked, dislikeDelta)
    }

    suspend fun toggleWatchLater(video: VideoEntity) {
        dao.updateWatchLater(video.id, !video.isWatchLater)
    }

    suspend fun addComment(videoId: String, user: UserProfileEntity, text: String, parentCommentId: String? = null): Result<Unit> {
        if (user.commentRestricted || user.isBanned) {
            return Result.failure(IllegalStateException("Your account is restricted from commenting."))
        }
        val clean = text.trim()
        if (clean.isBlank()) {
            return Result.failure(IllegalArgumentException("Enter a comment first."))
        }
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        val moderation = ChhotuUploadModerator.moderateComment(clean, settings.chhotuAiStrictMode)
        if (!moderation.allowed) {
            return Result.failure(IllegalStateException(moderation.reason))
        }
        dao.insertComment(
            CommentEntity(
                id = "cmt_${UUID.randomUUID().toString().take(8)}",
                videoId = videoId,
                parentCommentId = parentCommentId,
                authorUserId = user.uid,
                authorName = user.username,
                text = clean,
                originalLanguage = user.languageCode
            )
        )
        val video = dao.getVideo(videoId)
        val channel = video?.let { v -> dao.getAllChannelsSync().firstOrNull { it.id == v.channelId } }
        if (video != null && channel != null && channel.ownerUserId != user.uid) {
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = channel.ownerUserId,
                    category = "COMMENT",
                    title = "${user.username} commented on '${video.title.take(30)}'",
                    body = clean.take(120)
                )
            )
        }
        return Result.success(Unit)
    }

    private suspend fun canModerateVideoComments(actor: UserProfileEntity, video: VideoEntity): Boolean {
        if (actor.isAdmin) return true
        val channel = dao.getAllChannelsSync().firstOrNull { it.id == video.channelId }
        return channel?.ownerUserId == actor.uid
    }

    suspend fun togglePinComment(actor: UserProfileEntity, video: VideoEntity, comment: CommentEntity): Result<Unit> {
        if (!canModerateVideoComments(actor, video)) {
            return Result.failure(SecurityException("Only Owner or channel creator can pin comments."))
        }
        dao.updateCommentModeration(comment.id, isPinned = !comment.isPinned, isHidden = comment.isHidden)
        return Result.success(Unit)
    }

    suspend fun toggleHideComment(actor: UserProfileEntity, video: VideoEntity, comment: CommentEntity): Result<Unit> {
        if (!canModerateVideoComments(actor, video)) {
            return Result.failure(SecurityException("Only Owner or channel creator can hide comments."))
        }
        dao.updateCommentModeration(comment.id, isPinned = comment.isPinned, isHidden = !comment.isHidden)
        return Result.success(Unit)
    }

    suspend fun deleteComment(actor: UserProfileEntity, video: VideoEntity, comment: CommentEntity): Result<Unit> {
        if (!canModerateVideoComments(actor, video) && comment.authorUserId != actor.uid) {
            return Result.failure(SecurityException("Only Owner or channel creator can delete this comment."))
        }
        dao.deleteComment(comment.id)
        return Result.success(Unit)
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

    suspend fun removeVideoFromPlaylist(playlistId: String, videoId: String) {
        dao.removeVideoFromPlaylist(playlistId, videoId)
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

    fun resolveVideoDurationSeconds(uriString: String, format: VideoFormat): Int {
        val fallback = if (format == VideoFormat.SHORT) 45 else 320
        if (uriString.isBlank() || uriString.startsWith("http")) return fallback
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(uriString))
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val sec = (durationMs / 1000L).toInt()
            if (sec > 0) sec else fallback
        } catch (_: Exception) {
            fallback
        } finally {
            runCatching { retriever.release() }
        }
    }

    private suspend fun checkUploadEligibility(
        user: UserProfileEntity,
        format: VideoFormat,
        settings: PlatformSettingsEntity
    ): String? {
        if ((!settings.appEnabled || settings.maintenanceMode) && !user.isAdmin) {
            return "BharatTube is currently paused for maintenance by Owner."
        }
        if (user.isBanned || user.uploadRestricted) {
            return "Your account is currently restricted from uploading."
        }
        if (!settings.videoUploadsEnabled) {
            return "Uploads are temporarily disabled by Owner Control Center."
        }
        if (format == VideoFormat.SHORT && !settings.shortsEnabled) {
            return "Shorts creation is temporarily disabled by Owner Control Center."
        }
        if (format == VideoFormat.LIVE && !settings.liveStreamingEnabled) {
            return "Live streaming is temporarily paused by Owner Control Center."
        }
        val baseLimit = when (format) {
            VideoFormat.SHORT -> settings.dailyShortsUploadLimit
            VideoFormat.LIVE -> settings.dailyLiveLimit
            VideoFormat.LONG -> settings.dailyVideoUploadLimit
        }
        val effectiveLimit = if (user.workLimited) {
            if (baseLimit <= 0) 0 else (baseLimit / 3).coerceAtLeast(1)
        } else {
            baseLimit
        }
        val channels = dao.getAllChannelsSync().filter { it.ownerUserId == user.uid }.map { it.id }
        if (channels.isNotEmpty()) {
            val sinceMs = System.currentTimeMillis() - 86_400_000L
            val todayCount = dao.countChannelUploadsSince(channels, format.name, sinceMs)
            if (todayCount >= effectiveLimit) {
                return "Daily ${format.name.lowercase()} limit reached ($todayCount/$effectiveLimit)."
            }
        }
        return null
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
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        checkUploadEligibility(user, format, settings)?.let { msg ->
            return Result.failure(IllegalStateException(msg))
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
                    val settingsNow = dao.getPlatformSettings() ?: PlatformSettingsEntity()
                    val video = VideoEntity(
                        id = "vid_upl_${UUID.randomUUID().toString().take(8)}",
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
                        durationSeconds = resolveVideoDurationSeconds(
                            completed.fileUri,
                            if (completed.format == VideoFormat.SHORT.name) VideoFormat.SHORT else VideoFormat.LONG
                        ),
                        captionsText = "Auto-captions pending cloud transcoding configuration.",
                        copyrightStatus = "SELF_DECLARED_CLEARED",
                        chhotuCheckStatus = if (settingsNow.chhotuAiEnabled) "CHECKING" else "APPROVED",
                        chhotuCheckMode = if (settingsNow.chhotuAiStrictMode) "STRICT" else "NORMAL"
                    )
                    dao.upsertVideo(video)
                    if (settingsNow.chhotuAiEnabled) {
                        runChhotuUploadCheck(video.id, completed.ownerUserId)
                    } else {
                        dao.insertNotification(
                            NotificationEntity(
                                id = "notif_${UUID.randomUUID().toString().take(8)}",
                                userId = completed.ownerUserId,
                                category = "UPLOAD",
                                title = "Upload Ready: ${completed.title}",
                                body = "Published to BharatTube (${completed.visibility})."
                            )
                        )
                    }
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

    private fun extractVideoFrameThumbnail(uriString: String): String {
        if (uriString.isBlank() || uriString.startsWith("http")) return ""
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(uriString))
            val bmp = retriever.getFrameAtTime(500_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime
                ?: return ""
            val outDir = java.io.File(context.filesDir, "thumbnails").apply { mkdirs() }
            val outFile = java.io.File(outDir, "thumb_${UUID.randomUUID().toString().take(8)}.jpg")
            java.io.FileOutputStream(outFile).use { fos ->
                bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, fos)
            }
            bmp.recycle()
            Uri.fromFile(outFile).toString()
        } catch (_: Exception) {
            ""
        } finally {
            runCatching { retriever.release() }
        }
    }

    suspend fun publishVideoNow(
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
        countryCode: String,
        allowComments: Boolean,
        audienceType: String,
        playlistId: String?,
        existingVideoId: String? = null
    ): Result<VideoEntity> {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        checkUploadEligibility(user, format, settings)?.let { msg ->
            return Result.failure(IllegalStateException(msg))
        }
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Please add a video title before uploading."))
        }
        val effectiveUri = videoUri.ifBlank {
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        }
        val effectiveThumb = thumbnailUri.ifBlank {
            extractVideoFrameThumbnail(effectiveUri).ifBlank {
                when (category) {
                    "Music" -> "drawable:img_thumb_music"
                    "Travel" -> "drawable:img_thumb_travel"
                    else -> "drawable:img_thumb_tech"
                }
            }
        }
        val totalBytes = resolveFileSizeBytes(effectiveUri)
        val durationSec = resolveVideoDurationSeconds(effectiveUri, format)
        val now = System.currentTimeMillis()
        val task = UploadTaskEntity(
            id = "upl_${UUID.randomUUID().toString().take(8)}",
            ownerUserId = user.uid,
            channelId = channel.id,
            title = title.trim(),
            description = description.trim(),
            fileUri = effectiveUri,
            thumbnailUri = effectiveThumb,
            format = format.name,
            visibility = visibility.name,
            category = category,
            hashtags = hashtags.ifBlank { "#BharatTube" },
            languageCode = languageCode,
            countryCode = countryCode,
            totalBytes = totalBytes,
            uploadedBytes = totalBytes,
            speedBytesPerSec = totalBytes.coerceAtLeast(1L),
            status = UploadStatus.COMPLETED.name,
            createdAt = now
        )
        dao.upsertUploadTask(task)

        val existingVideo = if (!existingVideoId.isNullOrBlank()) dao.getVideo(existingVideoId) else null
        val videoId = existingVideo?.id ?: "vid_upl_${UUID.randomUUID().toString().take(8)}"
        val initialStatus = if (settings.chhotuAiEnabled) "CHECKING" else "APPROVED"
        val checkMode = if (settings.chhotuAiStrictMode) "STRICT" else "NORMAL"

        val video = VideoEntity(
            id = videoId,
            channelId = channel.id,
            channelName = channel.name,
            channelHandle = channel.handle,
            title = title.trim(),
            description = description.trim(),
            thumbnailKey = effectiveThumb,
            videoUri = effectiveUri,
            category = category,
            hashtags = hashtags.ifBlank { "#BharatTube" },
            languageCode = languageCode,
            countryCode = countryCode,
            format = format.name,
            visibility = visibility.name,
            durationSeconds = durationSec,
            viewCount = existingVideo?.viewCount ?: 0L,
            likeCount = existingVideo?.likeCount ?: 0L,
            allowComments = allowComments,
            audienceType = audienceType,
            captionsText = "",
            copyrightStatus = "SELF_DECLARED_CLEARED",
            chhotuCheckStatus = initialStatus,
            chhotuRejectionReason = "",
            chhotuCheckSummary = "",
            chhotuCheckMode = checkMode,
            chhotuCheckedAt = if (settings.chhotuAiEnabled) 0L else now,
            createdAt = now
        )
        dao.upsertVideo(video)

        if (!playlistId.isNullOrBlank()) {
            dao.addVideoToPlaylist(PlaylistVideoCrossRef(playlistId, video.id))
        }

        if (settings.chhotuAiEnabled) {
            uploadScope.launch {
                delay(1800L)
                runChhotuUploadCheck(video.id, user.uid)
            }
        } else {
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = user.uid,
                    category = "UPLOAD",
                    title = "Uploaded: ${video.title}",
                    body = "Your ${if (format == VideoFormat.SHORT) "Short" else "Video"} is now live (${visibility.name})."
                )
            )
        }
        return Result.success(video)
    }

    suspend fun runChhotuUploadCheck(videoId: String, ownerUserId: String? = null) {
        val video = dao.getVideo(videoId) ?: return
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        val format = runCatching { VideoFormat.valueOf(video.format) }.getOrDefault(VideoFormat.LONG)
        val result = ChhotuUploadModerator.evaluate(
            title = video.title,
            description = video.description,
            hashtags = video.hashtags,
            videoUri = video.videoUri,
            thumbnailUri = video.thumbnailKey,
            format = format,
            durationSeconds = video.durationSeconds,
            strictMode = settings.chhotuAiStrictMode
        )
        val fullReason = if (result.approved) {
            ""
        } else {
            "${result.rejectionReason}\n\nHow to Fix: ${result.fixSuggestion}"
        }
        val checkedAt = System.currentTimeMillis()
        dao.updateVideoChhotuCheck(
            videoId = video.id,
            status = result.status,
            reason = fullReason,
            summary = result.serializeSummary(),
            mode = result.modeLabel,
            checkedAt = checkedAt
        )
        val targetUserId = ownerUserId
            ?: dao.getAllChannelsSync().firstOrNull { it.id == video.channelId }?.ownerUserId
            ?: return
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = targetUserId,
                category = "CHHOTU_CHECK",
                title = if (result.approved) "Chhotu AI Approved: ${video.title}" else "Chhotu AI Rejected: ${video.title}",
                body = if (result.approved) {
                    "All 5 checks passed (${result.modeLabel} mode). Your video is now live on BharatTube Home Page!"
                } else {
                    "Action needed: ${result.fixSuggestion}"
                }
            )
        )
    }

    suspend fun startLiveBroadcast(
        user: UserProfileEntity,
        channel: ChannelEntity,
        title: String,
        languageCode: String,
        countryCode: String
    ): Result<VideoEntity> {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        checkUploadEligibility(user, VideoFormat.LIVE, settings)?.let { msg ->
            return Result.failure(IllegalStateException(msg))
        }
        val cleanTitle = title.trim().ifBlank { "${channel.name} Live Stream" }
        val liveVideo = VideoEntity(
            id = "vid_upl_${UUID.randomUUID().toString().take(8)}",
            channelId = channel.id,
            channelName = channel.name,
            channelHandle = channel.handle,
            title = "LIVE: $cleanTitle",
            description = "Live broadcast from ${channel.name} (${channel.handle})",
            thumbnailKey = channel.bannerUri.ifBlank { "drawable:img_hero_creators" },
            videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            category = channel.category,
            hashtags = "#Live #BharatTube",
            languageCode = languageCode,
            countryCode = countryCode,
            format = VideoFormat.LIVE.name,
            visibility = VideoVisibility.PUBLIC.name,
            durationSeconds = 0,
            isLive = true,
            liveViewerCount = 1,
            createdAt = System.currentTimeMillis()
        )
        dao.upsertVideo(liveVideo)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = user.uid,
                category = "LIVE",
                title = "Live Started: $cleanTitle",
                body = "${channel.name} is now LIVE on BharatTube!"
            )
        )
        return Result.success(liveVideo)
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

    // Creator Payout Validation & Automatic ₹2,000 Payout Engine
    suspend fun initiatePayout(
        user: UserProfileEntity,
        channel: ChannelEntity,
        availableBalanceInr: Double,
        method: PayoutMethod
    ): Result<PayoutEntity> {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.automaticPayoutEnabled || !settings.creatorMonetizationEnabled) {
            return Result.failure(IllegalStateException("Payouts are currently paused by Owner Control Center. Balances remain preserved."))
        }
        val thresholdInr = settings.indiaPayoutThresholdInr
        if (availableBalanceInr < thresholdInr) {
            return Result.failure(
                IllegalStateException(
                    "Available balance ₹${String.format("%.2f", availableBalanceInr)} is below the ₹${String.format("%.0f", thresholdInr)} minimum payout threshold."
                )
            )
        }
        val activeCount = dao.countActivePayoutsForChannel(channel.id)
        if (activeCount > 0) {
            return Result.failure(IllegalStateException("Duplicate payout blocked: an active payout is already pending confirmation."))
        }
        val destination = when (method) {
            PayoutMethod.UPI -> user.upiId.ifBlank { "${user.handle.removePrefix("@")}@bharattube" }
            PayoutMethod.BANK_TRANSFER, PayoutMethod.INTERNATIONAL_SWIFT -> user.bankAccountMasked.ifBlank { "Bank A/C •••• 4821" }
        }
        val autoPaid = user.kycVerified && user.paymentAccountVerified
        val payout = PayoutEntity(
            id = "pay_${UUID.randomUUID().toString().take(8)}",
            channelId = channel.id,
            userId = user.uid,
            amountInr = availableBalanceInr,
            method = method.name,
            destinationMasked = destination,
            transactionId = "BT-AUTO-${System.currentTimeMillis().toString().takeLast(6)}",
            status = if (autoPaid) PayoutStatus.CONFIRMED.name else PayoutStatus.INITIATED.name
        )
        dao.upsertPayout(payout)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = user.uid,
                category = "PAYOUT",
                title = if (autoPaid) "Automatic Payout Paid (${payout.transactionId})" else "Payout Queued (${payout.transactionId})",
                body = "₹${String.format("%.2f", availableBalanceInr)} via ${method.label} ($destination)."
            )
        )
        return Result.success(payout)
    }

    suspend fun evaluateAutoPayoutForChannel(channelId: String): Boolean {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.automaticPayoutEnabled || !settings.creatorMonetizationEnabled) return false
        val channel = dao.getAllChannelsSync().firstOrNull { it.id == channelId } ?: return false
        val owner = dao.getUser(channel.ownerUserId) ?: return false
        if (owner.isBanned) return false
        if (dao.countActivePayoutsForChannel(channel.id) > 0) return false
        val videos = dao.getAllVideosSync().filter { it.channelId == channel.id }
        val totalViews = videos.sumOf { it.viewCount }
        val totalNet = videos.sumOf { it.netRevenueInr }
        val earned = BharatTubeCatalog.calculateCreatorEarningsInr(
            viewCount = totalViews,
            netRevenueInr = totalNet,
            creatorSharePct = settings.creatorSharePct,
            per1000ViewsPaymentInr = settings.per1000ViewsPaymentInr,
            bonusPercent = settings.bonusPercent,
            userBoostPct = owner.boostPercent
        )
        val paidSoFar = dao.getNonFailedPayoutsForChannelSync(channel.id).sumOf { it.amountInr }
        val available = (earned - paidSoFar).coerceAtLeast(0.0)
        if (available < settings.indiaPayoutThresholdInr) return false

        val method = runCatching { PayoutMethod.valueOf(owner.preferredPayoutMethod) }.getOrDefault(PayoutMethod.UPI)
        val result = initiatePayout(owner, channel, available, method)
        return result.isSuccess
    }

    suspend fun runAutoPayoutSweep(actorId: String = "owner_auto_payout"): Int {
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.automaticPayoutEnabled || !settings.creatorMonetizationEnabled) return 0
        val channels = dao.getAllChannelsSync()
        var triggered = 0
        channels.forEach { ch ->
            if (evaluateAutoPayoutForChannel(ch.id)) {
                triggered++
            }
        }
        if (triggered > 0) {
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "audit_${UUID.randomUUID().toString().take(8)}",
                    actorId = actorId,
                    action = "AUTO_PAYOUT_SWEEP",
                    details = "Processed $triggered automatic payout(s) for creators >= ₹${settings.indiaPayoutThresholdInr.toInt()}.",
                    previousValue = "Eligible Creators",
                    newValue = "$triggered Payout(s) Triggered"
                )
            )
        }
        return triggered
    }

    suspend fun updatePayoutMethodDetails(
        user: UserProfileEntity,
        method: PayoutMethod,
        upiId: String,
        bankMasked: String
    ) {
        dao.upsertUser(
            user.copy(
                preferredPayoutMethod = method.name,
                upiId = upiId.trim(),
                bankAccountMasked = bankMasked.trim()
            )
        )
    }

    suspend fun updatePayoutStatus(admin: UserProfileEntity, payout: PayoutEntity, newStatus: PayoutStatus) {
        if (!admin.isAdmin) return
        dao.upsertPayout(payout.copy(status = newStatus.name))
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = "PAYOUT_STATUS_UPDATED",
                details = "Payout ${payout.transactionId} (₹${String.format("%.2f", payout.amountInr)}) updated",
                previousValue = payout.status,
                newValue = newStatus.name
            )
        )
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

    suspend fun warnUser(admin: UserProfileEntity, target: UserProfileEntity, reason: String) {
        if (!admin.isAdmin) return
        val cleanReason = reason.trim().ifBlank { "Community & Upload Guidelines Warning" }
        val updated = target.copy(
            warningCount = target.warningCount + 1,
            lastWarningReason = cleanReason
        )
        dao.upsertUser(updated)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = target.uid,
                category = "ACCOUNT_WARNING",
                title = "Official Warning (${updated.warningCount})",
                body = cleanReason
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = "USER_WARNED",
                details = "Warned ${target.handle}: $cleanReason",
                previousValue = "warnings=${target.warningCount}",
                newValue = "warnings=${updated.warningCount}"
            )
        )
    }

    suspend fun blockUserEngagement(admin: UserProfileEntity, target: UserProfileEntity, blocked: Boolean, reason: String) {
        if (!admin.isAdmin) return
        dao.upsertUser(target.copy(commentRestricted = blocked))
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = if (blocked) "USER_BLOCKED" else "USER_UNBLOCKED",
                details = "Engagement block for ${target.handle}: $reason",
                previousValue = "blocked=${target.commentRestricted}",
                newValue = "blocked=$blocked"
            )
        )
    }

    suspend fun limitUserWork(admin: UserProfileEntity, target: UserProfileEntity, limited: Boolean, reason: String) {
        if (!admin.isAdmin) return
        dao.upsertUser(target.copy(workLimited = limited))
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = if (limited) "USER_WORK_LIMITED" else "USER_WORK_UNLIMITED",
                details = "Upload/Live work limit for ${target.handle}: $reason",
                previousValue = "limited=${target.workLimited}",
                newValue = "limited=$limited"
            )
        )
    }

    suspend fun boostUserWork(admin: UserProfileEntity, target: UserProfileEntity, boostPercent: Int) {
        if (!admin.isAdmin) return
        val clamped = boostPercent.coerceIn(0, 100)
        dao.upsertUser(target.copy(boostPercent = clamped))
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = "USER_BOOST_UPDATED",
                details = "Visibility & work boost updated for ${target.handle}",
                previousValue = "boost=${target.boostPercent}%",
                newValue = "boost=$clamped%"
            )
        )
    }

    suspend fun setChannelVerified(admin: UserProfileEntity, channel: ChannelEntity, verified: Boolean) {
        if (!admin.isAdmin) return
        dao.updateChannelVerification(channel.id, verified)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = channel.ownerUserId,
                category = "CHANNEL",
                title = if (verified) "Blue Tick Verified: ${channel.name}" else "Verification Removed: ${channel.name}",
                body = if (verified) {
                    "Congratulations! Owner Control Center granted a verified Blue Tick badge to ${channel.handle}."
                } else {
                    "Your channel verification badge was updated by Owner Control Center."
                }
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = if (verified) "CHANNEL_VERIFIED" else "CHANNEL_UNVERIFIED",
                details = "Blue Tick ${if (verified) "granted" else "removed"} for ${channel.name} (${channel.handle})",
                previousValue = "verified=${channel.isVerified}",
                newValue = "verified=$verified"
            )
        )
    }

    suspend fun setChannelMonetization(admin: UserProfileEntity, channel: ChannelEntity, enabled: Boolean) {
        if (!admin.isAdmin) return
        dao.updateChannelMonetization(channel.id, enabled)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = channel.ownerUserId,
                category = "EARNINGS",
                title = if (enabled) "Monetization Enabled: ${channel.name}" else "Monetization Paused: ${channel.name}",
                body = if (enabled) {
                    "Your channel monetization is now ON. Keep creating on BharatTube!"
                } else {
                    "Channel monetization was paused by Owner Control Center."
                }
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = "CHANNEL_MONETIZATION_TOGGLED",
                details = "Monetization ${if (enabled) "ON" else "OFF"} for ${channel.name} (${channel.handle})",
                previousValue = "monetization=${channel.monetizationEnabled}",
                newValue = "monetization=$enabled"
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

    // Chhotu AI Diagnostics, Monitoring & Official Assistant Integration
    suspend fun runChhotuDiagnostics(notifyAdminsOnNewIssue: Boolean = true): SystemHealthSnapshot {
        SeedDataPopulator.ensureSeeded(dao)
        val snapshot = chhotuDiagnosticsEngine.runCompleteCheck()
        val newlyDetected = mutableListOf<String>()

        snapshot.items.forEach { item ->
            val openReport = dao.findOpenChhotuReportForModule(item.moduleName)
            if (item.status == CheckStatus.CONNECTED) {
                if (openReport != null) {
                    dao.resolveChhotuErrorReport(openReport.id)
                }
            } else {
                val severity = item.status.name
                val causeText = item.cause.ifBlank { item.detail }
                val solutionText = item.solution.ifBlank { "Check configuration in Chhotu AI Control Center." }
                if (openReport == null) {
                    dao.upsertChhotuErrorReport(
                        ChhotuErrorReportEntity(
                            id = "chrep_${UUID.randomUUID().toString().take(8)}",
                            moduleName = item.moduleName,
                            severity = severity,
                            cause = causeText,
                            solution = solutionText,
                            isResolved = false
                        )
                    )
                    if (item.status == CheckStatus.ERROR || item.status == CheckStatus.DEGRADED) {
                        newlyDetected += "${item.moduleName} (${item.status.label})"
                    }
                } else if (openReport.severity != severity || openReport.cause != causeText) {
                    dao.upsertChhotuErrorReport(
                        openReport.copy(
                            severity = severity,
                            cause = causeText,
                            solution = solutionText,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        if (notifyAdminsOnNewIssue && newlyDetected.isNotEmpty()) {
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = "user_founder",
                    category = "CHHOTU_ALERT",
                    title = "Chhotu AI Alert: ${newlyDetected.size} issue(s) detected",
                    body = "Modules needing attention: ${newlyDetected.joinToString(", ")}. Open Chhotu AI Control Center for causes & fixes."
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "audit_${UUID.randomUUID().toString().take(8)}",
                    actorId = "chhotu_monitor",
                    action = "CHHOTU_AUTO_ALERT",
                    details = "Automatic check flagged: ${newlyDetected.joinToString(", ")}",
                    previousValue = "Healthy",
                    newValue = "${newlyDetected.size} open alert(s)"
                )
            )
        }
        return snapshot
    }

    suspend fun sendChhotuMessage(
        user: UserProfileEntity,
        rawText: String,
        languageCode: String,
        healthSnapshot: SystemHealthSnapshot?
    ): Result<Unit> {
        val text = rawText.trim()
        if (text.isEmpty()) return Result.failure(IllegalArgumentException("Enter a message or use voice input."))
        val settings = dao.getPlatformSettings() ?: PlatformSettingsEntity()
        if (!settings.chhotuAiEnabled) {
            return Result.failure(IllegalStateException("Chhotu AI is currently paused by Admin."))
        }

        // Strict Security Guardrail (passwords, OTPs, API keys, cross-user privacy, unauthorized bans/payouts)
        val blockedReason = ChhotuSecurityGuard.isBlockedSensitiveRequest(text, user.isAdmin)
        if (blockedReason != null) {
            dao.insertChhotuChatMessage(
                ChhotuChatMessageEntity(
                    id = "cmsg_${UUID.randomUUID().toString().take(8)}",
                    userId = user.uid,
                    role = "USER",
                    text = "[Filtered sensitive query]",
                    languageCode = languageCode,
                    sourceMode = "SECURITY_BLOCK"
                )
            )
            dao.insertChhotuChatMessage(
                ChhotuChatMessageEntity(
                    id = "cmsg_${UUID.randomUUID().toString().take(8)}",
                    userId = user.uid,
                    role = "CHHOTU_AI",
                    text = blockedReason,
                    languageCode = languageCode,
                    sourceMode = "SECURITY_BLOCK"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "audit_${UUID.randomUUID().toString().take(8)}",
                    actorId = user.uid,
                    action = "CHHOTU_SECURITY_BLOCK",
                    details = "Blocked sensitive or unauthorized request in Chhotu AI assistant.",
                    previousValue = "Allowed",
                    newValue = "Blocked"
                )
            )
            return Result.success(Unit)
        }

        dao.insertChhotuChatMessage(
            ChhotuChatMessageEntity(
                id = "cmsg_${UUID.randomUUID().toString().take(8)}",
                userId = user.uid,
                role = "USER",
                text = text,
                languageCode = languageCode,
                sourceMode = "USER_INPUT"
            )
        )

        val recentHistory = dao.getRecentChhotuChatHistory(user.uid, 10).reversed()
        val totalUsers = dao.countUsersSync()
        val totalVideos = dao.countVideosSync()
        val totalChannels = dao.countChannelsSync()

        val replyResult = chhotuBackendClient.generateAssistantReply(
            userMessage = text,
            isAdmin = user.isAdmin,
            languageCode = languageCode,
            healthSnapshot = healthSnapshot,
            totalUsers = totalUsers,
            totalVideos = totalVideos,
            totalChannels = totalChannels,
            creatorSharePct = settings.creatorSharePct,
            platformSharePct = settings.platformSharePct,
            indiaThresholdInr = settings.indiaPayoutThresholdInr,
            recentHistory = recentHistory
        )

        dao.insertChhotuChatMessage(
            ChhotuChatMessageEntity(
                id = "cmsg_${UUID.randomUUID().toString().take(8)}",
                userId = user.uid,
                role = "CHHOTU_AI",
                text = replyResult.text,
                languageCode = languageCode,
                sourceMode = replyResult.sourceMode
            )
        )
        return Result.success(Unit)
    }

    fun isOpenAiAssistantConfigured(): Boolean = chhotuBackendClient.isOpenAiConfigured()

    suspend fun clearChhotuHistory(userId: String) {
        dao.clearChhotuChatHistory(userId)
    }

    suspend fun resolveChhotuErrorReport(admin: UserProfileEntity, report: ChhotuErrorReportEntity) {
        if (!admin.isAdmin) return
        dao.resolveChhotuErrorReport(report.id)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_${UUID.randomUUID().toString().take(8)}",
                actorId = admin.uid,
                action = "CHHOTU_REPORT_RESOLVED",
                details = "Marked ${report.moduleName} (${report.severity}) report resolved.",
                previousValue = "OPEN",
                newValue = "RESOLVED"
            )
        )
    }
}
