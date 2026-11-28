package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistVideoCrossRef
import com.example.data.model.ReportEntity
import com.example.data.model.SearchHistoryEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.UploadTaskEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BharatTubeDao {
    // Users
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun observeAllUsers(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun observeUser(uid: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUser(uid: String): UserProfileEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<UserProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserProfileEntity)

    // Channels
    @Query("SELECT * FROM channels ORDER BY subscriberCount DESC")
    fun observeAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE ownerUserId = :ownerId ORDER BY createdAt DESC")
    fun observeMyChannels(ownerId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :channelId LIMIT 1")
    fun observeChannel(channelId: String): Flow<ChannelEntity?>

    @Query("SELECT * FROM channels WHERE handle = :handle LIMIT 1")
    suspend fun findChannelByHandle(handle: String): ChannelEntity?

    @Query("SELECT * FROM channels")
    suspend fun getAllChannelsSync(): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChannel(channel: ChannelEntity)

    @Query("UPDATE channels SET isSubscribedByMe = :subscribed, subscriberCount = subscriberCount + :delta WHERE id = :channelId")
    suspend fun updateSubscription(channelId: String, subscribed: Boolean, delta: Long)

    @Query("UPDATE channels SET notificationsEnabled = :enabled WHERE id = :channelId")
    suspend fun updateChannelNotification(channelId: String, enabled: Boolean)

    @Query("UPDATE channels SET monetizationEnabled = :enabled WHERE id = :channelId")
    suspend fun updateChannelMonetization(channelId: String, enabled: Boolean)

    // Videos
    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    fun observeAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    fun observeVideo(videoId: String): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    suspend fun getVideo(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos")
    suspend fun getAllVideosSync(): List<VideoEntity>

    @Query("SELECT COUNT(*) FROM videos WHERE channelId IN (:channelIds) AND format = :format AND createdAt >= :sinceMs")
    suspend fun countChannelUploadsSince(channelIds: List<String>, format: String, sinceMs: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("UPDATE videos SET viewCount = viewCount + 1 WHERE id = :videoId")
    suspend fun incrementVideoView(videoId: String)

    @Query("UPDATE videos SET isLiked = :liked, likeCount = likeCount + :delta WHERE id = :videoId")
    suspend fun updateVideoLike(videoId: String, liked: Boolean, delta: Long)

    @Query("UPDATE videos SET isWatchLater = :watchLater WHERE id = :videoId")
    suspend fun updateWatchLater(videoId: String, watchLater: Boolean)

    @Query("UPDATE videos SET lastPositionMs = :positionMs WHERE id = :videoId")
    suspend fun updatePlaybackPosition(videoId: String, positionMs: Long)

    @Query("UPDATE videos SET playbackQuality = :quality WHERE id = :videoId")
    suspend fun updatePlaybackQuality(videoId: String, quality: String)

    @Query("UPDATE videos SET liveViewerCount = :viewers WHERE id = :videoId")
    suspend fun updateLiveViewerCount(videoId: String, viewers: Int)

    @Query("UPDATE videos SET channelName = :channelName, channelHandle = :channelHandle WHERE channelId = :channelId")
    suspend fun updateChannelInfoOnVideos(channelId: String, channelName: String, channelHandle: String)

    @Query("UPDATE videos SET visibility = :visibility WHERE id = :videoId")
    suspend fun updateVideoVisibility(videoId: String, visibility: String)

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteVideo(videoId: String)

    // Comments
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY createdAt DESC")
    fun observeCommentsForVideo(videoId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("UPDATE comments SET likeCount = likeCount + 1 WHERE id = :commentId")
    suspend fun likeComment(commentId: String)

    // Playlists
    @Query("SELECT * FROM playlists WHERE ownerUserId = :userId ORDER BY createdAt DESC")
    fun observePlaylists(userId: String): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addVideoToPlaylist(ref: PlaylistVideoCrossRef)

    @Query("SELECT * FROM playlist_videos")
    fun observePlaylistVideoRefs(): Flow<List<PlaylistVideoCrossRef>>

    // Watch History
    @Query("SELECT * FROM watch_history WHERE userId = :userId ORDER BY watchedAt DESC")
    fun observeWatchHistory(userId: String): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE userId = :userId")
    suspend fun clearWatchHistory(userId: String)

    // Uploads
    @Query("SELECT * FROM upload_tasks WHERE ownerUserId = :userId ORDER BY createdAt DESC")
    fun observeUploads(userId: String): Flow<List<UploadTaskEntity>>

    @Query("SELECT * FROM upload_tasks WHERE id = :taskId LIMIT 1")
    suspend fun getUploadTask(taskId: String): UploadTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUploadTask(task: UploadTaskEntity)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeNotifications(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: String)

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY createdAt ASC")
    fun observeAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    // Ad Campaigns
    @Query("SELECT * FROM ad_campaigns ORDER BY createdAt DESC")
    fun observeAdCampaigns(): Flow<List<AdCampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAdCampaign(campaign: AdCampaignEntity)

    // Payouts
    @Query("SELECT * FROM payouts ORDER BY createdAt DESC")
    fun observePayouts(): Flow<List<PayoutEntity>>

    @Query("SELECT COUNT(*) FROM payouts WHERE channelId = :channelId AND status IN ('INITIATED', 'PENDING_VERIFICATION')")
    suspend fun countActivePayoutsForChannel(channelId: String): Int

    @Query("SELECT * FROM payouts WHERE channelId = :channelId AND status != 'FAILED'")
    suspend fun getNonFailedPayoutsForChannelSync(channelId: String): List<PayoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayout(payout: PayoutEntity)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC")
    fun observeAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Reports
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun observeReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReport(report: ReportEntity)

    // Support Tickets
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeSupportTickets(userId: String): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupportTicket(ticket: SupportTicketEntity)

    // Community Posts
    @Query("SELECT * FROM community_posts ORDER BY createdAt DESC")
    fun observeCommunityPosts(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPost(post: CommunityPostEntity)

    // Search History
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    // Platform Settings
    @Query("SELECT * FROM platform_settings WHERE id = 1 LIMIT 1")
    fun observePlatformSettings(): Flow<PlatformSettingsEntity?>

    @Query("SELECT * FROM platform_settings WHERE id = 1 LIMIT 1")
    suspend fun getPlatformSettings(): PlatformSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlatformSettings(settings: PlatformSettingsEntity)

    @Update
    suspend fun updatePlatformSettings(settings: PlatformSettingsEntity)

    // Chhotu AI Chat History (strictly isolated per userId)
    @Query("SELECT * FROM chhotu_chat_messages WHERE userId = :userId ORDER BY createdAt ASC")
    fun observeChhotuChatHistory(userId: String): Flow<List<ChhotuChatMessageEntity>>

    @Query("SELECT * FROM chhotu_chat_messages WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentChhotuChatHistory(userId: String, limit: Int = 10): List<ChhotuChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChhotuChatMessage(message: ChhotuChatMessageEntity)

    @Query("DELETE FROM chhotu_chat_messages WHERE userId = :userId")
    suspend fun clearChhotuChatHistory(userId: String)

    // Chhotu AI Error & Health Reports
    @Query("SELECT * FROM chhotu_health_reports ORDER BY createdAt DESC")
    fun observeChhotuErrorReports(): Flow<List<ChhotuErrorReportEntity>>

    @Query("SELECT * FROM chhotu_health_reports WHERE moduleName = :moduleName AND isResolved = 0 LIMIT 1")
    suspend fun findOpenChhotuReportForModule(moduleName: String): ChhotuErrorReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChhotuErrorReport(report: ChhotuErrorReportEntity)

    @Query("UPDATE chhotu_health_reports SET isResolved = 1 WHERE id = :reportId")
    suspend fun resolveChhotuErrorReport(reportId: String)

    // Real DB Health & Count Probes
    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsersSync(): Int

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun countChannelsSync(): Int

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun countVideosSync(): Int

    @Query("SELECT COUNT(*) FROM videos WHERE format = 'SHORT'")
    suspend fun countShortsSync(): Int

    @Query("SELECT COUNT(*) FROM comments")
    suspend fun countCommentsSync(): Int

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun countNotificationsSync(): Int

    @Query("SELECT COUNT(*) FROM upload_tasks")
    suspend fun countUploadsSync(): Int
}
