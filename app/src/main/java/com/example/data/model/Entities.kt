package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserProfileEntity(
    @PrimaryKey val uid: String,
    val username: String,
    val handle: String,
    val email: String = "",
    val phone: String = "",
    val authProvider: String = "Local Account",
    val bio: String = "",
    val countryCode: String = "IN",
    val languageCode: String = "hi",
    val avatarUri: String = "",
    val bannerUri: String = "drawable:img_hero_creators",
    val isMinor: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val restrictedMode: Boolean = false,
    val kycVerified: Boolean = false,
    val paymentAccountVerified: Boolean = false,
    val taxVerified: Boolean = false,
    val upiId: String = "",
    val bankAccountMasked: String = "",
    val isBanned: Boolean = false,
    val banReason: String = "",
    val uploadRestricted: Boolean = false,
    val commentRestricted: Boolean = false,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val name: String,
    val handle: String,
    val description: String,
    val category: String,
    val bannerUri: String = "drawable:img_hero_creators",
    val avatarUri: String = "",
    val socialLinks: String = "",
    val subscriberCount: Long = 0L,
    val isSubscribedByMe: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val monetizationEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val channelName: String,
    val channelHandle: String,
    val title: String,
    val description: String,
    val thumbnailKey: String,
    val videoUri: String,
    val category: String,
    val hashtags: String,
    val languageCode: String,
    val countryCode: String,
    val format: String = VideoFormat.LONG.name,
    val visibility: String = VideoVisibility.PUBLIC.name,
    val durationSeconds: Int,
    val viewCount: Long = 0L,
    val likeCount: Long = 0L,
    val isLiked: Boolean = false,
    val isWatchLater: Boolean = false,
    val lastPositionMs: Long = 0L,
    val playbackQuality: String = "1080p",
    val chaptersSummary: String = "",
    val captionsText: String = "",
    val musicAttribution: String = "Original Licensed Audio",
    val copyrightStatus: String = "CLEARED",
    val adEligible: Boolean = true,
    val netRevenueInr: Double = 0.0,
    val isLive: Boolean = false,
    val liveViewerCount: Int = 0,
    val allowComments: Boolean = true,
    val audienceType: String = "NOT_FOR_KIDS",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val parentCommentId: String? = null,
    val authorUserId: String,
    val authorName: String,
    val text: String,
    val originalLanguage: String = "en",
    val likeCount: Int = 0,
    val isReported: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val name: String,
    val description: String = "",
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_videos", primaryKeys = ["playlistId", "videoId"])
data class PlaylistVideoCrossRef(
    val playlistId: String,
    val videoId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val videoId: String,
    val format: String,
    val progressMs: Long,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "upload_tasks")
data class UploadTaskEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val channelId: String,
    val title: String,
    val description: String,
    val fileUri: String,
    val thumbnailUri: String,
    val format: String,
    val visibility: String,
    val category: String,
    val hashtags: String,
    val languageCode: String,
    val countryCode: String,
    val totalBytes: Long,
    val uploadedBytes: Long,
    val speedBytesPerSec: Long,
    val status: String,
    val errorMessage: String = "",
    val copyrightDeclarationAccepted: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val roomTitle: String,
    val isGroup: Boolean = false,
    val senderId: String,
    val senderName: String,
    val text: String,
    val mediaUri: String = "",
    val voiceDurationSec: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_campaigns")
data class AdCampaignEntity(
    @PrimaryKey val id: String,
    val advertiserName: String,
    val title: String,
    val targetCountry: String,
    val targetLanguage: String,
    val placement: String,
    val budgetInr: Double,
    val spentInr: Double = 0.0,
    val validImpressions: Long = 0L,
    val clicks: Long = 0L,
    val approvalStatus: String = "PENDING_ADMIN_REVIEW",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payouts")
data class PayoutEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val userId: String,
    val amountInr: Double,
    val method: String,
    val destinationMasked: String,
    val transactionId: String,
    val status: String,
    val failureReason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val actorId: String,
    val action: String,
    val details: String,
    val previousValue: String,
    val newValue: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val targetType: String,
    val targetId: String,
    val targetTitle: String,
    val reason: String,
    val details: String,
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String,
    val subject: String,
    val message: String,
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val channelName: String,
    val text: String,
    val likeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "platform_settings")
data class PlatformSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val userRegistrationEnabled: Boolean = true,
    val googleLoginEnabled: Boolean = false,
    val videoUploadsEnabled: Boolean = true,
    val longVideosEnabled: Boolean = true,
    val shortsEnabled: Boolean = true,
    val liveStreamingEnabled: Boolean = true,
    val commentsEnabled: Boolean = true,
    val likesEnabled: Boolean = true,
    val subscriptionsEnabled: Boolean = true,
    val chatEnabled: Boolean = true,
    val searchEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val advertisementsEnabled: Boolean = true,
    val preRollAdsEnabled: Boolean = true,
    val midRollAdsEnabled: Boolean = true,
    val shortsAdsEnabled: Boolean = true,
    val nativeAdsEnabled: Boolean = true,
    val bannerAdsEnabled: Boolean = true,
    val metaAudienceNetworkEnabled: Boolean = false,
    val metaTestMode: Boolean = true,
    val creatorMonetizationEnabled: Boolean = true,
    val automaticPayoutEnabled: Boolean = true,
    val downloadsEnabled: Boolean = true,
    val communityPostsEnabled: Boolean = true,
    val chhotuAiEnabled: Boolean = true,
    val creatorSharePct: Int = 50,
    val platformSharePct: Int = 50,
    val indiaPayoutThresholdInr: Double = 1000.0,
    val adFrequencyMinutes: Int = 8,
    val enabledCountriesCsv: String = "IN,US,GB,JP,AE,DE,BR,ID,TH,KR",
    val enabledLanguagesCsv: String = "hi,en,bho,bn,ta,te,mr,gu,pa,zh,ja,ko,ar,fr,de,es,pt,ru,id,th"
)

@Entity(tableName = "chhotu_chat_messages")
data class ChhotuChatMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val role: String, // "USER" or "CHHOTU_AI"
    val text: String,
    val languageCode: String = "hi",
    val sourceMode: String = "LOCAL_GUIDE", // "EXISTING_BACKEND" | "LOCAL_GUIDE" | "SECURITY_BLOCK"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chhotu_health_reports")
data class ChhotuErrorReportEntity(
    @PrimaryKey val id: String,
    val moduleName: String,
    val severity: String, // "ERROR" | "DEGRADED" | "NOT_CONNECTED"
    val cause: String,
    val solution: String,
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
