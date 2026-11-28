package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.data.model.VideoFormat
import com.example.data.model.VideoVisibility
import com.example.data.model.WatchHistoryEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ChannelEntity::class,
        VideoEntity::class,
        CommentEntity::class,
        PlaylistEntity::class,
        PlaylistVideoCrossRef::class,
        WatchHistoryEntity::class,
        UploadTaskEntity::class,
        NotificationEntity::class,
        ChatMessageEntity::class,
        AdCampaignEntity::class,
        PayoutEntity::class,
        AuditLogEntity::class,
        ReportEntity::class,
        SupportTicketEntity::class,
        CommunityPostEntity::class,
        SearchHistoryEntity::class,
        PlatformSettingsEntity::class,
        ChhotuChatMessageEntity::class,
        ChhotuErrorReportEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class BharatTubeDatabase : RoomDatabase() {
    abstract fun dao(): BharatTubeDao

    companion object {
        @Volatile
        private var INSTANCE: BharatTubeDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE users ADD COLUMN bannerUri TEXT NOT NULL DEFAULT 'drawable:img_hero_creators'"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE platform_settings ADD COLUMN chhotuAiEnabled INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `chhotu_chat_messages` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `text` TEXT NOT NULL,
                        `languageCode` TEXT NOT NULL,
                        `sourceMode` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `chhotu_health_reports` (
                        `id` TEXT NOT NULL,
                        `moduleName` TEXT NOT NULL,
                        `severity` TEXT NOT NULL,
                        `cause` TEXT NOT NULL,
                        `solution` TEXT NOT NULL,
                        `isResolved` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE videos ADD COLUMN allowComments INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE videos ADD COLUMN audienceType TEXT NOT NULL DEFAULT 'NOT_FOR_KIDS'")
            }
        }

        fun getInstance(context: Context): BharatTubeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BharatTubeDatabase::class.java,
                    "bharattube_local.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

object SeedDataPopulator {
    suspend fun ensureSeeded(dao: BharatTubeDao) {
        if (dao.getPlatformSettings() != null) return

        val now = System.currentTimeMillis()
        dao.upsertPlatformSettings(PlatformSettingsEntity())

        // Users
        dao.upsertUser(
            UserProfileEntity(
                uid = "user_founder",
                username = "Aarav Sharma",
                handle = "@aarav_creates",
                email = "aarav@bharattube.local",
                phone = "+91 98XXXXXX10",
                authProvider = "Local Session (Configure Google OAuth for Cloud)",
                bio = "Tech builder, filmmaker & educator sharing tutorials from Bengaluru.",
                countryCode = "IN",
                languageCode = "en",
                kycVerified = true,
                paymentAccountVerified = true,
                taxVerified = true,
                upiId = "aarav@okaxis",
                bankAccountMasked = "HDFC •••• 4821",
                isAdmin = true,
                twoFactorEnabled = true,
                createdAt = now - 86_400_000L * 30
            )
        )
        dao.upsertUser(
            UserProfileEntity(
                uid = "user_priya",
                username = "Priya Verma",
                handle = "@priya_melodies",
                email = "priya@bharattube.local",
                bio = "Indie fusion musician & live acoustic performer.",
                countryCode = "IN",
                languageCode = "hi",
                kycVerified = true,
                paymentAccountVerified = true,
                taxVerified = false,
                upiId = "priya@ybl",
                createdAt = now - 86_400_000L * 20
            )
        )
        dao.upsertUser(
            UserProfileEntity(
                uid = "user_kabir",
                username = "Kabir Expeditions",
                handle = "@kabir_trails",
                bio = "Documenting mountain trails across India and Asia in 4K.",
                countryCode = "IN",
                languageCode = "hi",
                createdAt = now - 86_400_000L * 15
            )
        )

        // Channels
        dao.upsertChannel(
            ChannelEntity(
                id = "ch_bharattube_tech",
                ownerUserId = "user_founder",
                name = "Aarav Tech Lab",
                handle = "@aaravtech",
                description = "Deep-dives into Android engineering, studio workflows, and creator hardware.",
                category = "Technology",
                bannerUri = "drawable:img_hero_creators",
                socialLinks = "bharattube.in/@aaravtech",
                subscriberCount = 148_500L,
                isSubscribedByMe = false,
                notificationsEnabled = true,
                monetizationEnabled = true,
                createdAt = now - 86_400_000L * 28
            )
        )
        dao.upsertChannel(
            ChannelEntity(
                id = "ch_indie_waves",
                ownerUserId = "user_priya",
                name = "Indie Waves Bharat",
                handle = "@indiewaves",
                description = "Live acoustic sessions, classical-electronic fusion, and regional music.",
                category = "Music",
                bannerUri = "drawable:img_thumb_music",
                socialLinks = "indiewaves.music",
                subscriberCount = 312_400L,
                isSubscribedByMe = true,
                notificationsEnabled = true,
                monetizationEnabled = true,
                createdAt = now - 86_400_000L * 19
            )
        )
        dao.upsertChannel(
            ChannelEntity(
                id = "ch_himalaya_lens",
                ownerUserId = "user_kabir",
                name = "Himalaya Lens",
                handle = "@himalayalens",
                description = "4K aerial journeys across Ladakh, Spiti, Sikkim, and Nepal.",
                category = "Travel",
                bannerUri = "drawable:img_thumb_travel",
                socialLinks = "himalayalens.travel",
                subscriberCount = 94_200L,
                isSubscribedByMe = true,
                notificationsEnabled = true,
                monetizationEnabled = true,
                createdAt = now - 86_400_000L * 12
            )
        )

        // Videos & Shorts (Using public HTTPS Creative Commons sample MP4 streams)
        val videos = listOf(
            VideoEntity(
                id = "vid_1",
                channelId = "ch_bharattube_tech",
                channelName = "Aarav Tech Lab",
                channelHandle = "@aaravtech",
                title = "Building a Global Creator Studio Desk & 4K Workflow in 2026",
                description = "Complete breakdown of our studio lighting, audio chain, and adaptive video pipeline for multi-language creators.",
                thumbnailKey = "drawable:img_thumb_tech",
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                category = "Technology",
                hashtags = "#CreatorStudio #TechIndia #4KWorkflow",
                languageCode = "en",
                countryCode = "IN",
                format = VideoFormat.LONG.name,
                visibility = VideoVisibility.PUBLIC.name,
                durationSeconds = 740,
                viewCount = 86_420L,
                likeCount = 6_410L,
                chaptersSummary = "0:00 Studio Tour • 0:05 Audio Chain • 0:10 Color Grading",
                captionsText = "Welcome to Aarav Tech Lab! Today we optimize a creator studio for crisp global streaming.",
                musicAttribution = "Licensed Studio Lo-Fi Beat (CC-BY)",
                netRevenueInr = 2480.0,
                createdAt = now - 3_600_000L * 6
            ),
            VideoEntity(
                id = "vid_2",
                channelId = "ch_indie_waves",
                channelName = "Indie Waves Bharat",
                channelHandle = "@indiewaves",
                title = "Midnight Raga & Synth — Live Unplugged Session from Mumbai",
                description = "An intimate acoustic and modular synth performance celebrating Indian indie fusion.",
                thumbnailKey = "drawable:img_thumb_music",
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                category = "Music",
                hashtags = "#IndieMusic #LiveSession #BharatMusic",
                languageCode = "hi",
                countryCode = "IN",
                format = VideoFormat.LONG.name,
                visibility = VideoVisibility.PUBLIC.name,
                durationSeconds = 653,
                viewCount = 241_900L,
                likeCount = 19_850L,
                chaptersSummary = "0:00 Alap Intro • 2:15 Synth Drop • 5:40 Acoustic Finale",
                captionsText = "Live from our Mumbai soundstage — blending acoustic strings with analog warmth.",
                musicAttribution = "Indie Waves Original Master (Cleared)",
                netRevenueInr = 4120.0,
                createdAt = now - 3_600_000L * 18
            ),
            VideoEntity(
                id = "vid_3",
                channelId = "ch_himalaya_lens",
                channelName = "Himalaya Lens",
                channelHandle = "@himalayalens",
                title = "Golden Sunrise Over Spiti Valley — Cinematic 4K Travel Film",
                description = "Filmed over 14 days at 4,200m altitude with regional commentary in Hindi and English.",
                thumbnailKey = "drawable:img_thumb_travel",
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                category = "Travel",
                hashtags = "#SpitiValley #Himalayas #4KTravel",
                languageCode = "hi",
                countryCode = "IN",
                format = VideoFormat.LONG.name,
                visibility = VideoVisibility.PUBLIC.name,
                durationSeconds = 915,
                viewCount = 152_300L,
                likeCount = 14_200L,
                chaptersSummary = "0:00 Dawn Ascent • 0:06 River Gorge • 0:11 Monastery Ridge",
                captionsText = "As first light hits the valley floor, the Himalayan peaks turn molten gold.",
                musicAttribution = "Mountain Wind Ambient (Licensed)",
                netRevenueInr = 1950.0,
                createdAt = now - 3_600_000L * 32
            ),
            VideoEntity(
                id = "vid_4",
                channelId = "ch_bharattube_tech",
                channelName = "Aarav Tech Lab",
                channelHandle = "@aaravtech",
                title = "LIVE: Creator Economy Q&A — Monetization & Global Reach",
                description = "Live townhall discussing creator growth, regional subtitles, and global reach on BharatTube.",
                thumbnailKey = "drawable:img_hero_creators",
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                category = "Education",
                hashtags = "#LiveQA #CreatorEconomy #BharatTube",
                languageCode = "en",
                countryCode = "IN",
                format = VideoFormat.LIVE.name,
                visibility = VideoVisibility.PUBLIC.name,
                durationSeconds = 1800,
                viewCount = 18_940L,
                likeCount = 2_310L,
                isLive = true,
                liveViewerCount = 1284,
                captionsText = "Live Q&A stream active — drop your creator questions in live chat!",
                netRevenueInr = 620.0,
                createdAt = now - 1_800_000L
            ),
            VideoEntity(
                id = "short_1",
                channelId = "ch_indie_waves",
                channelName = "Indie Waves Bharat",
                channelHandle = "@indiewaves",
                title = "60s Sitar Loop Beat Drop 🔥",
                description = "Built live in one take! #Shorts #Music",
                thumbnailKey = "drawable:img_thumb_music",
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
                category = "Music",
                hashtags = "#Shorts #SitarLoop #MusicIndia",
                languageCode = "hi",
                countryCode = "IN",
                format = VideoFormat.SHORT.name,
                visibility = VideoVisibility.PUBLIC.name,
                durationSeconds = 15,
                viewCount = 492_000L,
                likeCount = 48_100L,
                captionsText = "Watch how this 4-bar sitar riff turns into a festival anthem.",
                musicAttribution = "Indie Waves Licensed Loop #104",
                netRevenueInr = 840.0,
                createdAt = now - 3_600_000L * 4
            ),
            VideoEntity(
                id = "short_2",
                channelId = "ch_himalaya_lens",
                channelName = "Himalaya Lens",
                channelHandle = "@himalayalens",
                title = "Cloud Waterfall in Sikkim ☁️🏔️",
                description = "Nature's best timelapse from 12,000ft.",
                thumbnailKey = "drawable:img_thumb_travel",
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                category = "Travel",
                hashtags = "#Shorts #Sikkim #Nature",
                languageCode = "en",
                countryCode = "IN",
                format = VideoFormat.SHORT.name,
                visibility = VideoVisibility.PUBLIC.name,
                durationSeconds = 15,
                viewCount = 318_500L,
                likeCount = 31_900L,
                captionsText = "Morning mist spilling over the eastern ridge.",
                musicAttribution = "Horizon Pulse (Cleared)",
                netRevenueInr = 560.0,
                createdAt = now - 3_600_000L * 9
            ),
            VideoEntity(
                id = "short_3",
                channelId = "ch_bharattube_tech",
                channelName = "Aarav Tech Lab",
                channelHandle = "@aaravtech",
                title = "3 Desk Lighting Tricks for Crisp Shorts",
                description = "Better video quality with just one warm key light.",
                thumbnailKey = "drawable:img_thumb_tech",
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                category = "Technology",
                hashtags = "#Shorts #Filmmaking #Tips",
                languageCode = "ta",
                countryCode = "IN",
                format = VideoFormat.SHORT.name,
                visibility = VideoVisibility.PUBLIC.name,
                durationSeconds = 45,
                viewCount = 129_400L,
                likeCount = 11_400L,
                captionsText = "Set your key light at 45 degrees and keep background contrast warm.",
                musicAttribution = "Tech Minimal Beat (Licensed)",
                netRevenueInr = 410.0,
                createdAt = now - 3_600_000L * 14
            ),
            VideoEntity(
                id = "vid_5",
                channelId = "ch_bharattube_tech",
                channelName = "Aarav Tech Lab",
                channelHandle = "@aaravtech",
                title = "2-Minute Quick Mic Setup for Crisp Creator Voiceovers",
                description = "Fast under-3-minute tutorial covering USB & XLR gain staging for beginner creators.",
                thumbnailKey = "drawable:img_thumb_tech",
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                category = "Technology",
                hashtags = "#CreatorTips #AudioSetup #BharatTube",
                languageCode = "hi",
                countryCode = "IN",
                format = VideoFormat.LONG.name,
                visibility = VideoVisibility.PUBLIC.name,
                durationSeconds = 135,
                viewCount = 42_180L,
                likeCount = 3_920L,
                chaptersSummary = "0:00 Mic Placement • 1:05 Gain Check",
                captionsText = "Quick under-3-minute guide to clean creator audio.",
                musicAttribution = "Original Licensed Audio",
                netRevenueInr = 680.0,
                createdAt = now - 3_600_000L * 2
            )
        )
        dao.insertVideos(videos)

        // Comments
        dao.insertComment(
            CommentEntity(
                id = "cmt_1",
                videoId = "vid_1",
                authorUserId = "user_priya",
                authorName = "Priya Verma",
                text = "The warm amber desk lighting looks super clean! Which mic arm are you using?",
                originalLanguage = "en",
                likeCount = 42,
                createdAt = now - 1_200_000L
            )
        )
        dao.insertComment(
            CommentEntity(
                id = "cmt_2",
                videoId = "vid_2",
                authorUserId = "user_kabir",
                authorName = "Kabir Expeditions",
                text = "बहुत ही शानदार संगीत! Pure goosebumps on the synth drop.",
                originalLanguage = "hi",
                likeCount = 95,
                createdAt = now - 2_400_000L
            )
        )

        // Community posts
        dao.insertCommunityPost(
            CommunityPostEntity(
                id = "post_1",
                channelId = "ch_bharattube_tech",
                channelName = "Aarav Tech Lab",
                text = "Which regional language subtitles should we prioritize next week — Tamil, Telugu, Marathi, or Bengali?",
                likeCount = 640,
                createdAt = now - 7_200_000L
            )
        )

        // Playlists
        dao.upsertPlaylist(
            PlaylistEntity(
                id = "pl_favorites",
                ownerUserId = "user_founder",
                name = "Creator Inspiration",
                description = "Top studio, music, and cinematography benchmarks"
            )
        )
        dao.addVideoToPlaylist(PlaylistVideoCrossRef("pl_favorites", "vid_2"))
        dao.addVideoToPlaylist(PlaylistVideoCrossRef("pl_favorites", "vid_3"))

        // Notifications
        dao.insertNotification(
            NotificationEntity(
                id = "notif_1",
                userId = "user_founder",
                category = "EARNINGS",
                title = "50/50 Creator Revenue Credited",
                body = "Your channel earned ₹1,240.00 net share (50% of ₹2,480.00 eligible ad revenue). Eligible for ₹1,000 India payout!",
                createdAt = now - 900_000L
            )
        )
        dao.insertNotification(
            NotificationEntity(
                id = "notif_2",
                userId = "user_founder",
                category = "SUBSCRIBER",
                title = "Milestone: 148.5K Subscribers",
                body = "Aarav Tech Lab gained 1,240 new subscribers across India and UAE this week.",
                createdAt = now - 3_600_000L
            )
        )

        // Chat Messages
        dao.insertChatMessage(
            ChatMessageEntity(
                id = "msg_1",
                roomId = "room_creators_hub",
                roomTitle = "India Creators Guild",
                isGroup = true,
                senderId = "user_priya",
                senderName = "Priya Verma",
                text = "Hey everyone! Anyone testing multi-language audio tracks on BharatTube?",
                createdAt = now - 5_400_000L
            )
        )
        dao.insertChatMessage(
            ChatMessageEntity(
                id = "msg_2",
                roomId = "room_dm_kabir",
                roomTitle = "Kabir Expeditions",
                isGroup = false,
                senderId = "user_kabir",
                senderName = "Kabir Expeditions",
                text = "Would love to collaborate on a 4K mountain drone episode!",
                createdAt = now - 3_000_000L
            )
        )

        // Ad Campaign (transparently marked test/pending approval)
        dao.upsertAdCampaign(
            AdCampaignEntity(
                id = "cmp_1",
                advertiserName = "Swadeshi Audio Tech",
                title = "Studio Pro Wireless Mic — Creator Launch",
                targetCountry = "IN",
                targetLanguage = "hi",
                placement = "PRE_ROLL",
                budgetInr = 25_000.0,
                spentInr = 0.0,
                validImpressions = 0L,
                clicks = 0L,
                approvalStatus = "PENDING_ADMIN_REVIEW",
                createdAt = now - 10_800_000L
            )
        )

        // Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                id = "audit_init",
                actorId = "system_policy",
                action = "INITIALIZE_REVENUE_POLICY",
                details = "Default global revenue share set to 50% Creator / 50% BharatTube; India minimum payout ₹1,000.",
                previousValue = "N/A",
                newValue = "50:50 | IN_MIN=1000",
                createdAt = now - 86_400_000L
            )
        )
    }
}
