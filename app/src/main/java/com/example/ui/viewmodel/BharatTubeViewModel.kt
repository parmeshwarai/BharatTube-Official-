package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chhotuai.ChhotuPendingActionType
import com.example.chhotuai.ChhotuPendingAdminAction
import com.example.chhotuai.SystemHealthSnapshot
import com.example.data.local.BharatTubeDatabase
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AdCampaignEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.ChannelEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChhotuChatMessageEntity
import com.example.data.model.ChhotuErrorReportEntity
import com.example.data.model.CommentEntity
import com.example.data.model.CommunityPostEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PayoutEntity
import com.example.data.model.PayoutMethod
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistVideoCrossRef
import com.example.data.model.ReportEntity
import com.example.data.model.SearchHistoryEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.UploadTaskEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoFormat
import com.example.data.model.VideoVisibility
import com.example.data.model.WatchHistoryEntity
import com.example.data.repository.BharatTubeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BharatTubeViewModel(
    private val repository: BharatTubeRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val preferences: StateFlow<UserPreferences> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserPreferences())

    val platformSettings: StateFlow<PlatformSettingsEntity> = repository.platformSettings
        .combine(preferences) { settings, _ -> settings ?: PlatformSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), PlatformSettingsEntity())

    val currentUser: StateFlow<UserProfileEntity?> = preferences
        .flatMapLatest { prefs -> repository.observeUser(prefs.activeUserId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val allUsers: StateFlow<List<UserProfileEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allChannels: StateFlow<List<ChannelEntity>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val myChannels: StateFlow<List<ChannelEntity>> = preferences
        .flatMapLatest { prefs -> repository.observeMyChannels(prefs.activeUserId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allVideos: StateFlow<List<VideoEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = preferences
        .flatMapLatest { prefs -> repository.observeWatchHistory(prefs.activeUserId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val recommendedVideos: StateFlow<List<VideoEntity>> = combine(
        combine(allVideos, allChannels, myChannels) { videos, channels, myCh ->
            Triple(videos, channels, myCh)
        },
        combine(watchHistory, preferences, selectedCategory) { history, prefs, category ->
            Triple(history, prefs, category)
        },
        combine(allUsers, platformSettings) { users, settings ->
            users.associate { it.uid to it.boostPercent } to settings
        }
    ) { (videos, channels, myCh), (history, prefs, category), (boostMap, settings) ->
        val ranked = repository.rankVideosForUser(
            videos = videos,
            channels = channels,
            history = history,
            preferredLanguage = prefs.preferredLanguage,
            preferredCountry = prefs.preferredCountry,
            restrictedMode = prefs.restrictedMode,
            myChannelIds = myCh.map { it.id }.toSet(),
            boostedUserIds = boostMap,
            homePageBoostEnabled = settings.homePageBoostEnabled,
            workBoostPercent = settings.workBoostPercent
        )
        if (category == "All") ranked else ranked.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val shortsFeed: StateFlow<List<VideoEntity>> = allVideos
        .combine(preferences) { videos, _ ->
            videos.filter {
                it.format == VideoFormat.SHORT.name &&
                    it.visibility == VideoVisibility.PUBLIC.name &&
                    it.chhotuCheckStatus == "APPROVED"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val liveStreams: StateFlow<List<VideoEntity>> = allVideos
        .combine(preferences) { videos, _ ->
            videos.filter {
                (it.isLive || it.format == VideoFormat.LIVE.name) &&
                    it.chhotuCheckStatus == "APPROVED"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val uploads: StateFlow<List<UploadTaskEntity>> = preferences
        .flatMapLatest { prefs -> repository.observeUploads(prefs.activeUserId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = preferences
        .flatMapLatest { prefs -> repository.observeNotifications(prefs.activeUserId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = preferences
        .flatMapLatest { prefs -> repository.observePlaylists(prefs.activeUserId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val playlistRefs: StateFlow<List<PlaylistVideoCrossRef>> = repository.observePlaylistRefs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val adCampaigns: StateFlow<List<AdCampaignEntity>> = repository.adCampaigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val payouts: StateFlow<List<PayoutEntity>> = repository.payouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val reports: StateFlow<List<ReportEntity>> = repository.reports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val supportTickets: StateFlow<List<SupportTicketEntity>> = preferences
        .flatMapLatest { prefs -> repository.observeSupportTickets(prefs.activeUserId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val communityPosts: StateFlow<List<CommunityPostEntity>> = repository.communityPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val chhotuChatHistory: StateFlow<List<ChhotuChatMessageEntity>> = preferences
        .flatMapLatest { prefs -> repository.observeChhotuChatHistory(prefs.activeUserId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val chhotuErrorReports: StateFlow<List<ChhotuErrorReportEntity>> = repository.chhotuErrorReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _systemHealthSnapshot = MutableStateFlow<SystemHealthSnapshot?>(null)
    val systemHealthSnapshot: StateFlow<SystemHealthSnapshot?> = _systemHealthSnapshot.asStateFlow()

    private val _chhotuBusy = MutableStateFlow(false)
    val chhotuBusy: StateFlow<Boolean> = _chhotuBusy.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
            // Initial + periodic automatic Chhotu AI health monitoring
            while (isActive) {
                runCatching {
                    _systemHealthSnapshot.value = repository.runChhotuDiagnostics(notifyAdminsOnNewIssue = true)
                }
                delay(60_000L)
            }
        }
    }

    fun observeVideo(videoId: String) = repository.observeVideo(videoId)
    fun observeComments(videoId: String) = repository.observeComments(videoId)
    fun observeChannel(channelId: String) = repository.observeChannel(channelId)

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            repository.setDarkTheme(!preferences.value.isDarkTheme)
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            repository.setLanguage(code)
            currentUser.value?.let { repository.updateUserProfile(it.copy(languageCode = code)) }
            _snackbarMessage.value = "Language updated"
        }
    }

    fun setCountry(code: String) {
        viewModelScope.launch {
            repository.setCountry(code)
            currentUser.value?.let { repository.updateUserProfile(it.copy(countryCode = code)) }
            _snackbarMessage.value = "Country feed & currency updated"
        }
    }

    fun toggleHistoryPaused() {
        viewModelScope.launch {
            repository.setHistoryPaused(!preferences.value.historyPaused)
        }
    }

    fun toggleRestrictedMode() {
        viewModelScope.launch {
            val next = !preferences.value.restrictedMode
            repository.setRestrictedMode(next)
            currentUser.value?.let { repository.updateUserProfile(it.copy(restrictedMode = next)) }
        }
    }

    fun setPushOptIn(enabled: Boolean) {
        viewModelScope.launch { repository.setPushOptIn(enabled) }
    }

    fun setNotifyUploads(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifyUploads(enabled) }
    }

    fun setNotifyComments(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifyComments(enabled) }
    }

    fun setNotifyLive(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifyLive(enabled) }
    }

    fun setPlaybackAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch { repository.setPlaybackAutoPlayNext(enabled) }
    }

    fun setPlaybackDefaultQuality(quality: String) {
        viewModelScope.launch {
            repository.setPlaybackDefaultQuality(quality)
            _snackbarMessage.value = "Default playback quality set to $quality"
        }
    }

    fun setPlaybackCaptionsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setPlaybackCaptionsEnabled(enabled) }
    }

    fun setPlaybackDataSaver(enabled: Boolean) {
        viewModelScope.launch { repository.setPlaybackDataSaver(enabled) }
    }

    fun setDownloadsWifiOnly(enabled: Boolean) {
        viewModelScope.launch { repository.setDownloadsWifiOnly(enabled) }
    }

    fun setDownloadsQuality(quality: String) {
        viewModelScope.launch {
            repository.setDownloadsQuality(quality)
            _snackbarMessage.value = "Download quality set to $quality"
        }
    }

    fun toggleDownloadVideo(video: VideoEntity) {
        viewModelScope.launch {
            val added = repository.toggleDownloadedVideo(video.id)
            _snackbarMessage.value = if (added) {
                "Saved '${video.title}' to Downloads"
            } else {
                "Removed '${video.title}' from Downloads"
            }
        }
    }

    fun clearDownloads() {
        viewModelScope.launch {
            repository.clearDownloadedVideos()
            _snackbarMessage.value = "All downloads cleared"
        }
    }

    fun setPrivacySubscriptionsPrivate(isPrivate: Boolean) {
        viewModelScope.launch { repository.setPrivacySubscriptionsPrivate(isPrivate) }
    }

    fun setPrivacyPlaylistsPrivate(isPrivate: Boolean) {
        viewModelScope.launch { repository.setPrivacyPlaylistsPrivate(isPrivate) }
    }

    fun setActiveChannel(channelId: String) {
        viewModelScope.launch { repository.setActiveChannel(channelId) }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _snackbarMessage.value = "Signed out of BharatTube"
        }
    }

    fun signInBack(uid: String? = null) {
        viewModelScope.launch {
            if (uid != null) {
                repository.switchExistingUser(uid)
            } else {
                repository.signInBack()
            }
            _snackbarMessage.value = "Signed in to BharatTube"
        }
    }

    fun createLocalAccount(
        username: String,
        email: String,
        phone: String,
        countryCode: String,
        languageCode: String,
        isMinor: Boolean
    ) {
        viewModelScope.launch {
            repository.createOrSwitchLocalAccount(username, email, phone, countryCode, languageCode, isMinor)
                .onSuccess { _snackbarMessage.value = "Created BharatTube profile ${it.handle}" }
                .onFailure { _snackbarMessage.value = it.message }
        }
    }

    fun switchUser(uid: String) {
        viewModelScope.launch {
            repository.switchExistingUser(uid)
            _snackbarMessage.value = "Switched account"
        }
    }

    fun updateUserProfile(user: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateUserProfile(user)
            _snackbarMessage.value = "Profile saved"
        }
    }

    fun updateChannel(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.updateChannel(channel)
            _snackbarMessage.value = "Channel branding updated"
        }
    }

    fun saveChannelEdit(
        channel: ChannelEntity,
        name: String,
        handle: String,
        description: String,
        socialLinks: String,
        avatarUri: String,
        bannerUri: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateChannelDetails(
                channel = channel,
                name = name,
                handle = handle,
                description = description,
                socialLinks = socialLinks,
                avatarUri = avatarUri,
                bannerUri = bannerUri
            ).onSuccess {
                _snackbarMessage.value = "Channel profile saved"
                onSuccess()
            }.onFailure {
                _snackbarMessage.value = it.message ?: "Unable to save channel"
            }
        }
    }

    fun updateVideoMetadata(
        videoId: String,
        title: String,
        description: String,
        visibility: VideoVisibility,
        category: String
    ) {
        viewModelScope.launch {
            repository.updateVideoMetadata(videoId, title, description, visibility, category)
                .onSuccess { _snackbarMessage.value = "Video updated" }
                .onFailure { _snackbarMessage.value = it.message ?: "Could not update video" }
        }
    }

    fun toggleHideVideo(video: VideoEntity) {
        viewModelScope.launch {
            val next = if (video.visibility == VideoVisibility.PRIVATE.name) {
                VideoVisibility.PUBLIC
            } else {
                VideoVisibility.PRIVATE
            }
            repository.setVideoVisibility(video.id, next)
            _snackbarMessage.value = if (next == VideoVisibility.PRIVATE) {
                "Video hidden (set to Private)"
            } else {
                "Video is now Public"
            }
        }
    }

    fun deleteVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.deleteVideo(video.id)
            _snackbarMessage.value = "Deleted '${video.title}'"
        }
    }

    fun createChannel(
        name: String,
        handle: String,
        description: String,
        category: String,
        socialLinks: String,
        avatarUri: String,
        bannerUri: String,
        onSuccess: () -> Unit = {}
    ) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.createChannel(uid, name, handle, description, category, socialLinks, avatarUri, bannerUri)
                .onSuccess {
                    _snackbarMessage.value = "Channel ${it.handle} created!"
                    onSuccess()
                }
                .onFailure { _snackbarMessage.value = it.message }
        }
    }

    fun toggleSubscribe(channel: ChannelEntity) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            if (!platformSettings.value.subscriptionsEnabled) {
                _snackbarMessage.value = "Subscriptions are currently disabled by Super Admin"
                return@launch
            }
            repository.toggleSubscribe(channel, uid)
        }
    }

    fun toggleChannelBell(channel: ChannelEntity) {
        viewModelScope.launch { repository.toggleChannelBell(channel) }
    }

    fun recordVideoPlay(video: VideoEntity) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.recordVideoPlay(video, uid, preferences.value.historyPaused)
        }
    }

    fun recordVideoShare(videoId: String) {
        viewModelScope.launch {
            repository.recordVideoShare(videoId)
        }
    }

    fun savePlaybackPosition(videoId: String, positionMs: Long) {
        viewModelScope.launch { repository.savePlaybackPosition(videoId, positionMs) }
    }

    fun updatePlaybackQuality(videoId: String, quality: String) {
        viewModelScope.launch { repository.updatePlaybackQuality(videoId, quality) }
    }

    fun toggleLike(video: VideoEntity) {
        viewModelScope.launch {
            if (!platformSettings.value.likesEnabled) {
                _snackbarMessage.value = "Likes are disabled by Super Admin"
                return@launch
            }
            repository.toggleLike(video)
        }
    }

    fun toggleWatchLater(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleWatchLater(video)
            _snackbarMessage.value = if (!video.isWatchLater) "Saved to Watch Later" else "Removed from Watch Later"
        }
    }

    fun addComment(videoId: String, text: String, parentCommentId: String? = null) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            if (!platformSettings.value.commentsEnabled) {
                _snackbarMessage.value = "Comments are disabled by Super Admin"
                return@launch
            }
            repository.addComment(videoId, user, text, parentCommentId)
        }
    }

    fun likeComment(commentId: String) {
        viewModelScope.launch { repository.likeComment(commentId) }
    }

    fun createPlaylist(name: String, description: String, isPrivate: Boolean) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.createPlaylist(uid, name, description, isPrivate)
            _snackbarMessage.value = "Playlist '$name' created"
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch { repository.deletePlaylist(playlistId) }
    }

    fun addVideoToPlaylist(playlistId: String, videoId: String) {
        viewModelScope.launch {
            repository.addVideoToPlaylist(playlistId, videoId)
            _snackbarMessage.value = "Added to playlist"
        }
    }

    fun clearWatchHistory() {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.clearWatchHistory(uid)
            _snackbarMessage.value = "Watch history cleared"
        }
    }

    fun recordSearch(query: String) {
        viewModelScope.launch { repository.recordSearch(query) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    fun markNotificationsRead() {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch { repository.markAllNotificationsRead(uid) }
    }

    fun publishVideoNow(
        title: String,
        description: String,
        videoUri: String,
        thumbnailUri: String,
        format: VideoFormat,
        visibility: VideoVisibility,
        category: String,
        hashtags: String,
        allowComments: Boolean,
        audienceType: String,
        playlistId: String?,
        existingVideoId: String? = null,
        onSuccess: (VideoEntity) -> Unit
    ) {
        val user = currentUser.value ?: return
        val channel = myChannels.value.firstOrNull() ?: allChannels.value.firstOrNull()
        if (channel == null) {
            _snackbarMessage.value = "Create a channel in Profile before uploading."
            return
        }
        viewModelScope.launch {
            repository.publishVideoNow(
                user = user,
                channel = channel,
                title = title,
                description = description,
                videoUri = videoUri,
                thumbnailUri = thumbnailUri,
                format = format,
                visibility = visibility,
                category = category,
                hashtags = hashtags,
                languageCode = preferences.value.preferredLanguage,
                countryCode = preferences.value.preferredCountry,
                allowComments = allowComments,
                audienceType = audienceType,
                playlistId = playlistId,
                existingVideoId = existingVideoId
            ).onSuccess { video ->
                _selectedCategory.value = "All"
                _snackbarMessage.value = if (platformSettings.value.chhotuAiEnabled) {
                    "Uploaded! Chhotu AI is checking your video..."
                } else if (format == VideoFormat.SHORT) {
                    "Short uploaded to BharatTube!"
                } else {
                    "Video uploaded to top of Home!"
                }
                onSuccess(video)
            }.onFailure {
                _snackbarMessage.value = it.message ?: "Upload failed"
            }
        }
    }

    fun recheckVideoWithChhotuAi(videoId: String) {
        viewModelScope.launch {
            repository.runChhotuUploadCheck(videoId)
        }
    }

    fun startLiveBroadcast(
        title: String,
        onStarted: (VideoEntity) -> Unit
    ) {
        val user = currentUser.value ?: return
        val channel = myChannels.value.firstOrNull() ?: allChannels.value.firstOrNull()
        if (channel == null) {
            _snackbarMessage.value = "Create a channel in Profile before going live."
            return
        }
        viewModelScope.launch {
            repository.startLiveBroadcast(
                user = user,
                channel = channel,
                title = title,
                languageCode = preferences.value.preferredLanguage,
                countryCode = preferences.value.preferredCountry
            ).onSuccess { liveVideo ->
                _snackbarMessage.value = "You are Live on BharatTube!"
                onStarted(liveVideo)
            }.onFailure {
                _snackbarMessage.value = it.message ?: "Could not start live stream"
            }
        }
    }

    fun startUpload(
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
    ) {
        val user = currentUser.value ?: return
        val channel = myChannels.value.firstOrNull() ?: allChannels.value.firstOrNull()
        if (channel == null) {
            _snackbarMessage.value = "Create a channel in Profile before uploading."
            return
        }
        viewModelScope.launch {
            repository.startChunkedUpload(
                user, channel, title, description, videoUri, thumbnailUri,
                format, visibility, category, hashtags, languageCode, countryCode
            ).onSuccess {
                _snackbarMessage.value = "Chunked upload started: ${it.title}"
            }.onFailure {
                _snackbarMessage.value = it.message
            }
        }
    }

    fun pauseUpload(taskId: String) {
        viewModelScope.launch { repository.pauseUpload(taskId) }
    }

    fun resumeUpload(taskId: String) {
        val channel = myChannels.value.firstOrNull() ?: allChannels.value.firstOrNull() ?: return
        viewModelScope.launch { repository.resumeUpload(taskId, channel) }
    }

    fun cancelUpload(taskId: String) {
        viewModelScope.launch { repository.cancelUpload(taskId) }
    }

    fun initiatePayout(channel: ChannelEntity, availableBalanceInr: Double, method: PayoutMethod) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.initiatePayout(user, channel, availableBalanceInr, method)
                .onSuccess {
                    val statusWord = if (it.status == "CONFIRMED") "Paid" else "Pending"
                    _snackbarMessage.value = "Payout $statusWord: ${it.transactionId} (₹${String.format("%.0f", it.amountInr)})"
                }
                .onFailure { _snackbarMessage.value = it.message }
        }
    }

    fun updatePayoutMethodDetails(method: PayoutMethod, upiId: String, bankMasked: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updatePayoutMethodDetails(user, method, upiId, bankMasked)
            _snackbarMessage.value = "Payout method (${method.label}) saved"
        }
    }

    fun triggerAutoPayoutSweep() {
        val admin = currentUser.value ?: return
        if (!admin.isAdmin) return
        viewModelScope.launch {
            val count = repository.runAutoPayoutSweep(admin.uid)
            _snackbarMessage.value = if (count > 0) {
                "Auto-Payout processed $count creator payout(s)!"
            } else {
                "No creators currently awaiting auto-payout above minimum threshold."
            }
        }
    }

    fun updatePayoutStatus(payout: PayoutEntity, status: com.example.data.model.PayoutStatus) {
        val admin = currentUser.value ?: return
        if (!admin.isAdmin) return
        viewModelScope.launch {
            repository.updatePayoutStatus(admin, payout, status)
            val label = if (status == com.example.data.model.PayoutStatus.CONFIRMED) "Paid" else status.name
            _snackbarMessage.value = "Payout ${payout.transactionId} marked $label"
        }
    }

    fun sendChatMessage(roomId: String, roomTitle: String, isGroup: Boolean, text: String, voiceDurationSec: Int = 0) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.sendChatMessage(roomId, roomTitle, isGroup, user, text, voiceDurationSec)
                .onFailure { _snackbarMessage.value = it.message }
        }
    }

    fun createAdCampaign(
        advertiserName: String,
        title: String,
        targetCountry: String,
        targetLanguage: String,
        placement: String,
        budgetInr: Double
    ) {
        viewModelScope.launch {
            repository.createAdCampaign(advertiserName, title, targetCountry, targetLanguage, placement, budgetInr)
                .onSuccess { _snackbarMessage.value = "Campaign submitted for Super Admin review" }
                .onFailure { _snackbarMessage.value = it.message }
        }
    }

    fun reviewAdCampaign(campaign: AdCampaignEntity, approved: Boolean) {
        val admin = currentUser.value ?: return
        if (!admin.isAdmin) {
            _snackbarMessage.value = "Admin authorization required"
            return
        }
        viewModelScope.launch {
            repository.reviewAdCampaign(admin.uid, campaign, approved)
            _snackbarMessage.value = if (approved) "Campaign approved (Test Mode)" else "Campaign rejected"
        }
    }

    fun updatePlatformSettings(
        updated: PlatformSettingsEntity,
        actionLabel: String,
        details: String,
        previousValue: String,
        newValue: String
    ) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            repository.updatePlatformSettings(admin, updated, actionLabel, details, previousValue, newValue)
                .onSuccess { _snackbarMessage.value = "Saved: $actionLabel" }
                .onFailure { _snackbarMessage.value = it.message }
        }
    }

    fun moderateUser(target: UserProfileEntity, banned: Boolean, reason: String) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            repository.moderateUser(admin, target, banned, reason)
            _snackbarMessage.value = if (banned) "Account ${target.handle} banned" else "Account ${target.handle} unbanned"
        }
    }

    fun warnUser(target: UserProfileEntity, reason: String) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            repository.warnUser(admin, target, reason)
            _snackbarMessage.value = "Warning sent to ${target.handle}"
        }
    }

    fun blockUserEngagement(target: UserProfileEntity, blocked: Boolean, reason: String) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            repository.blockUserEngagement(admin, target, blocked, reason)
            _snackbarMessage.value = if (blocked) "Blocked ${target.handle}" else "Unblocked ${target.handle}"
        }
    }

    fun limitUserWork(target: UserProfileEntity, limited: Boolean, reason: String) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            repository.limitUserWork(admin, target, limited, reason)
            _snackbarMessage.value = if (limited) "Upload limit applied to ${target.handle}" else "Removed limit for ${target.handle}"
        }
    }

    fun boostUserWork(target: UserProfileEntity, boostPercent: Int) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            repository.boostUserWork(admin, target, boostPercent)
            _snackbarMessage.value = "Boost set to $boostPercent% for ${target.handle}"
        }
    }

    fun submitReport(targetType: String, targetId: String, targetTitle: String, reason: String, details: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.submitReport(uid, targetType, targetId, targetTitle, reason, details)
            _snackbarMessage.value = "Report submitted for safety & copyright review"
        }
    }

    fun resolveReport(report: ReportEntity, status: String) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            repository.resolveReport(admin, report, status)
            _snackbarMessage.value = "Report marked $status"
        }
    }

    fun submitSupportTicket(category: String, subject: String, message: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.submitSupportTicket(uid, category, subject, message)
            _snackbarMessage.value = "Support ticket logged"
        }
    }

    fun createCommunityPost(channel: ChannelEntity, text: String) {
        viewModelScope.launch {
            if (!platformSettings.value.communityPostsEnabled) {
                _snackbarMessage.value = "Community posts are disabled by Super Admin"
                return@launch
            }
            repository.createCommunityPost(channel, text)
            _snackbarMessage.value = "Community post published"
        }
    }

    fun runChhotuHealthCheck(manualTrigger: Boolean = true) {
        viewModelScope.launch {
            _chhotuBusy.value = true
            try {
                val snapshot = repository.runChhotuDiagnostics(notifyAdminsOnNewIssue = true)
                _systemHealthSnapshot.value = snapshot
                if (manualTrigger) {
                    _snackbarMessage.value = "System check: ${snapshot.connectedCount}/${snapshot.totalCount} Connected"
                }
            } finally {
                _chhotuBusy.value = false
            }
        }
    }

    fun sendChhotuMessage(text: String, languageCode: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _chhotuBusy.value = true
            try {
                repository.sendChhotuMessage(user, text, languageCode, _systemHealthSnapshot.value)
                    .onFailure { _snackbarMessage.value = it.message }
            } finally {
                _chhotuBusy.value = false
            }
        }
    }

    fun clearChhotuHistory() {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.clearChhotuHistory(uid)
            _snackbarMessage.value = "Chhotu AI chat cleared for current account"
        }
    }

    fun resolveChhotuErrorReport(report: ChhotuErrorReportEntity) {
        val admin = currentUser.value ?: return
        if (!admin.isAdmin) {
            _snackbarMessage.value = "Admin confirmation required"
            return
        }
        viewModelScope.launch {
            repository.resolveChhotuErrorReport(admin, report)
            _snackbarMessage.value = "${report.moduleName} report resolved & logged"
        }
    }

    fun executeConfirmedAdminAction(action: ChhotuPendingAdminAction) {
        val admin = currentUser.value
        if (admin?.isAdmin != true) {
            _snackbarMessage.value = "Security Policy: Admin authorization required"
            return
        }
        viewModelScope.launch {
            when (action.type) {
                ChhotuPendingActionType.TOGGLE_CHHOTU_AI -> {
                    val current = platformSettings.value
                    repository.updatePlatformSettings(
                        admin = admin,
                        updated = current.copy(chhotuAiEnabled = action.boolValue),
                        actionLabel = "CHHOTU_AI_TOGGLE",
                        details = "Admin confirmed Chhotu AI ${if (action.boolValue) "ONLINE" else "OFFLINE"}",
                        previousValue = "${current.chhotuAiEnabled}",
                        newValue = "${action.boolValue}"
                    ).onSuccess {
                        _snackbarMessage.value = "Chhotu AI is now ${if (action.boolValue) "Online" else "Offline"}"
                    }
                }
                ChhotuPendingActionType.BAN_OR_RESTORE_USER -> {
                    val target = allUsers.value.firstOrNull { it.uid == action.targetId } ?: return@launch
                    repository.moderateUser(admin, target, action.boolValue, "Admin-confirmed via Chhotu AI Control Center")
                    _snackbarMessage.value = if (action.boolValue) "Suspended ${target.handle}" else "Restored ${target.handle}"
                }
                ChhotuPendingActionType.TRIGGER_ADMIN_PAYOUT_REVIEW -> {
                    val current = platformSettings.value
                    repository.updatePlatformSettings(
                        admin = admin,
                        updated = current.copy(automaticPayoutEnabled = action.boolValue),
                        actionLabel = "CHHOTU_PAYOUT_CONTROL",
                        details = "Admin confirmed automatic payout switch change",
                        previousValue = "${current.automaticPayoutEnabled}",
                        newValue = "${action.boolValue}"
                    ).onSuccess {
                        _snackbarMessage.value = "Payout switch updated & logged"
                    }
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val db = BharatTubeDatabase.getInstance(app)
                val prefs = UserPreferencesRepository(app)
                BharatTubeViewModel(BharatTubeRepository(app, db.dao(), prefs))
            }
        }
    }
}
