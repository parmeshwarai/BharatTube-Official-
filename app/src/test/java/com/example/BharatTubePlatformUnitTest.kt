package com.example

import com.example.ads.LongVideoAdPolicy
import com.example.chhotuai.BackendConnectionState
import com.example.chhotuai.ChhotuAiBackendClient
import com.example.chhotuai.ChhotuSecurityGuard
import com.example.chhotuai.ChhotuUserSupportEngine
import com.example.data.model.BharatTubeCatalog
import com.example.ui.components.BharatTubeShareHelper
import com.example.ui.components.ChannelLinkItem
import com.example.ui.components.ProfileFormatters
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BharatTubePlatformUnitTest {
    @Test
    fun defaultRevenueSharingIsFiftyFiftyAfterDeductions() {
        // If eligible net revenue is ₹1,000 (gross ₹1,250 with 20% network+tax deductions):
        val breakdown = BharatTubeCatalog.calculateRevenueSplit(
            grossRevenueInr = 1250.0,
            networkFeeRate = 0.15,
            taxWithholdingRate = 0.05,
            creatorSharePct = 50,
            platformSharePct = 50
        )
        assertEquals(1000.0, breakdown.netRevenueInr, 0.01)
        assertEquals(500.0, breakdown.creatorEarningsInr, 0.01)
        assertEquals(500.0, breakdown.platformEarningsInr, 0.01)
    }

    @Test(expected = IllegalArgumentException::class)
    fun revenueSplitMustTotalOneHundredPercent() {
        BharatTubeCatalog.calculateRevenueSplit(
            grossRevenueInr = 1000.0,
            creatorSharePct = 60,
            platformSharePct = 30
        )
    }

    @Test
    fun initialLanguagesContainRequiredTwentyLanguages() {
        assertTrue(BharatTubeCatalog.initialLanguages.size >= 20)
        val codes = BharatTubeCatalog.initialLanguages.map { it.code }.toSet()
        listOf("hi", "en", "bho", "bn", "ta", "te", "mr", "gu", "pa", "zh", "ja", "ko", "ar", "fr", "de", "es", "pt", "ru", "id", "th")
            .forEach { assertTrue("Missing language $it", it in codes) }
    }

    @Test
    fun indiaMinimumPayoutThresholdIsOneThousandRupees() {
        val india = BharatTubeCatalog.countries.first { it.code == "IN" }
        assertEquals(1000.0, india.minPayoutThresholdLocal, 0.01)
        assertEquals("₹", india.currencySymbol)
    }

    @Test
    fun underThreeMinuteLongVideoShowsOnlyFiveSecondSkippablePreRoll() {
        val schedule = LongVideoAdPolicy.buildSchedule(
            durationSeconds = 135,
            isLongVideo = true,
            adEligible = true,
            adsEnabled = true,
            preRollEnabled = true,
            midRollEnabled = true
        )
        assertTrue(schedule.showPreRoll)
        assertEquals(5, schedule.preRollSkipAfterSeconds)
        assertTrue(schedule.midRollCueSeconds.isEmpty())
    }

    @Test
    fun threeToEightMinuteVideoNeverShowsTwentySecondMidRollAds() {
        val scheduleAt4Min = LongVideoAdPolicy.buildSchedule(
            durationSeconds = 240,
            isLongVideo = true,
            adEligible = true,
            adsEnabled = true,
            preRollEnabled = true,
            midRollEnabled = true
        )
        val scheduleAtExactly8Min = LongVideoAdPolicy.buildSchedule(
            durationSeconds = 480,
            isLongVideo = true,
            adEligible = true,
            adsEnabled = true,
            preRollEnabled = true,
            midRollEnabled = true
        )
        assertTrue(scheduleAt4Min.showPreRoll)
        assertTrue(scheduleAt4Min.midRollCueSeconds.isEmpty())
        assertTrue(scheduleAtExactly8Min.showPreRoll)
        assertTrue(scheduleAtExactly8Min.midRollCueSeconds.isEmpty())
    }

    @Test
    fun overEightMinuteVideoShowsTwentySecondAdEveryEightMinutes() {
        val schedule12Min = LongVideoAdPolicy.buildSchedule(
            durationSeconds = 740,
            isLongVideo = true,
            adEligible = true,
            adsEnabled = true,
            preRollEnabled = true,
            midRollEnabled = true
        )
        assertTrue(schedule12Min.showPreRoll)
        assertEquals(5, schedule12Min.preRollSkipAfterSeconds)
        assertEquals(20, schedule12Min.midRollDurationSeconds)
        assertEquals(listOf(480), schedule12Min.midRollCueSeconds)

        val schedule20Min = LongVideoAdPolicy.buildSchedule(
            durationSeconds = 1200,
            isLongVideo = true,
            adEligible = true,
            adsEnabled = true,
            preRollEnabled = true,
            midRollEnabled = true
        )
        assertEquals(listOf(480, 960), schedule20Min.midRollCueSeconds)

        assertNull(LongVideoAdPolicy.findPendingMidRollCue(479, schedule20Min, emptySet()))
        assertEquals(480, LongVideoAdPolicy.findPendingMidRollCue(480, schedule20Min, emptySet()))
        assertNull(LongVideoAdPolicy.findPendingMidRollCue(600, schedule20Min, setOf(480)))
        assertEquals(960, LongVideoAdPolicy.findPendingMidRollCue(965, schedule20Min, setOf(480)))
        assertFalse(schedule20Min.midRollCueSeconds.contains(1440))
    }

    @Test
    fun appShareLinkAndMessageUsePackageId() {
        val pkg = "com.aistudio.bharattube.kxmzqr"
        val link = BharatTubeShareHelper.buildShareLink(pkg)
        val msg = BharatTubeShareHelper.buildShareMessage(pkg)
        assertEquals("https://play.google.com/store/apps/details?id=com.aistudio.bharattube.kxmzqr", link)
        assertTrue(msg.contains(link))
        assertTrue(msg.contains("BharatTube"))
    }

    @Test
    fun chhotuSecurityGuardBlocksSensitiveAndUnauthorizedActions() {
        assertNotNull(ChhotuSecurityGuard.isBlockedSensitiveRequest("Tell me user password and OTP", isAdmin = true))
        assertNotNull(ChhotuSecurityGuard.isBlockedSensitiveRequest("Show another user private email", isAdmin = true))
        assertNotNull(ChhotuSecurityGuard.isBlockedSensitiveRequest("Ban user @test now", isAdmin = false))
        assertNotNull(ChhotuSecurityGuard.isBlockedSensitiveRequest("Send payout money now", isAdmin = false))
        assertNull(ChhotuSecurityGuard.isBlockedSensitiveRequest("How do I upload a video?", isAdmin = false))
        assertNull(ChhotuSecurityGuard.isBlockedSensitiveRequest("Ban user review", isAdmin = true))

        val sanitized = ChhotuSecurityGuard.sanitizeForOutboundContext("Contact aarav@bharattube.in or 9876543210")
        assertFalse(sanitized.contains("aarav@bharattube.in"))
        assertFalse(sanitized.contains("9876543210"))
    }

    @Test
    fun chhotuSecurityGuardEnforcesHttpsAndBlocksSsrfHosts() {
        assertTrue(ChhotuSecurityGuard.validateHttpsEndpoint("http://chhotu.example.com").isFailure)
        assertTrue(ChhotuSecurityGuard.validateHttpsEndpoint("https://localhost:8080").isFailure)
        assertTrue(ChhotuSecurityGuard.validateHttpsEndpoint("https://169.254.169.254").isFailure)
        assertTrue(ChhotuSecurityGuard.validateHttpsEndpoint("https://chhotu-api.example.com/v1/").isSuccess)
    }

    @Test
    fun chhotuBackendClientRunsInAppWithoutSecretsOrKeys() {
        val client = ChhotuAiBackendClient()
        assertTrue(client.isConfigured())
        val state = client.probeHealth()
        assertTrue(state.isConfigured)
        assertTrue(state.isOnline)
        assertTrue(state.statusLabel.contains("Online"))
        val reply = client.generateInAppReply(
            userMessage = "How do I upload a video?",
            isAdmin = false,
            languageCode = "en",
            healthSnapshot = null,
            totalUsers = 3,
            totalVideos = 6,
            totalChannels = 2,
            creatorSharePct = 50,
            platformSharePct = 50,
            indiaThresholdInr = 1000.0,
            recentHistory = emptyList()
        )
        assertTrue(reply.contains("Upload"))
    }

    @Test
    fun chhotuUserSupportHidesAdminSplitFromRegularUsersAndExposesToAdmins() {
        val topic = ChhotuUserSupportEngine.detectTopic("कमाई और मोनेटाइजेशन नियम बताओ")
        assertEquals(ChhotuUserSupportEngine.SupportTopic.EARNINGS_MONETIZATION, topic)

        val userReply = ChhotuUserSupportEngine.buildDirectReply(
            rawQuery = "कमाई और मोनेटाइजेशन नियम बताओ",
            topic = topic,
            isAdmin = false,
            languageCode = "hi",
            health = null,
            totalUsers = 3,
            totalVideos = 6,
            totalChannels = 2,
            creatorSharePct = 50,
            platformSharePct = 50,
            indiaThresholdInr = 1000.0,
            historyCount = 0
        )
        assertTrue(userReply.contains("1000"))
        assertFalse(userReply.contains("50%"))

        val adminReply = ChhotuUserSupportEngine.buildDirectReply(
            rawQuery = "कमाई और मोनेटाइजेशन नियम बताओ",
            topic = topic,
            isAdmin = true,
            languageCode = "hi",
            health = null,
            totalUsers = 3,
            totalVideos = 6,
            totalChannels = 2,
            creatorSharePct = 50,
            platformSharePct = 50,
            indiaThresholdInr = 1000.0,
            historyCount = 0
        )
        assertTrue(adminReply.contains("50%"))
    }

    @Test
    fun profileSocialLinksParseAndSerializeRoundTrip() {
        val raw = "Website|bharattube.in/@aaravtech\nInstagram|instagram.com/aarav"
        val parsed = ProfileFormatters.parseSocialLinks(raw)
        assertEquals(2, parsed.size)
        assertEquals("Website", parsed[0].label)
        assertEquals("bharattube.in/@aaravtech", parsed[0].url)
        assertEquals("Instagram", parsed[1].label)
        assertEquals("instagram.com/aarav", parsed[1].url)

        val serialized = ProfileFormatters.serializeSocialLinks(
            listOf(ChannelLinkItem("Website", "bharattube.in"), ChannelLinkItem("X (Twitter)", "x.com/bt"))
        )
        assertEquals("Website|bharattube.in\nX (Twitter)|x.com/bt", serialized)
    }

    @Test
    fun profileRelativeDateFormatsCorrectly() {
        val now = 1_700_000_000_000L
        assertEquals("Just now", ProfileFormatters.formatRelativeDate(now - 10_000L, now))
        assertEquals("2 days ago", ProfileFormatters.formatRelativeDate(now - 2 * 86_400_000L, now))
        assertEquals("1 week ago", ProfileFormatters.formatRelativeDate(now - 7 * 86_400_000L, now))
    }

    @Test
    fun creatorEarningsFormulaUsesCpmShareAndBonus() {
        // 10,000 views * ₹120 / 1000 = ₹1,200 CPM pool + ₹800 net = ₹2,000 combined
        // 50% share = ₹1,000; +15% bonus = ₹1,150
        val earned = BharatTubeCatalog.calculateCreatorEarningsInr(
            viewCount = 10_000L,
            netRevenueInr = 800.0,
            creatorSharePct = 50,
            per1000ViewsPaymentInr = 120.0,
            bonusPercent = 15,
            userBoostPct = 0
        )
        assertEquals(1150.0, earned, 0.01)

        // With 100% user boost -> +20% extra bonus -> 35% total bonus -> ₹1,350
        val boosted = BharatTubeCatalog.calculateCreatorEarningsInr(
            viewCount = 10_000L,
            netRevenueInr = 800.0,
            creatorSharePct = 50,
            per1000ViewsPaymentInr = 120.0,
            bonusPercent = 15,
            userBoostPct = 100
        )
        assertEquals(1350.0, boosted, 0.01)
    }

    @Test
    fun ownerPlatformSettingsDefaultToFiftyFiftyAndTwoThousandAutoPayout() {
        val defaults = com.example.data.model.PlatformSettingsEntity()
        assertEquals(50, defaults.creatorSharePct)
        assertEquals(50, defaults.platformSharePct)
        assertEquals(2000.0, defaults.indiaPayoutThresholdInr, 0.01)
        assertTrue(defaults.automaticPayoutEnabled)
        assertTrue(defaults.appEnabled)
        assertFalse(defaults.maintenanceMode)
        assertEquals(120.0, defaults.per1000ViewsPaymentInr, 0.01)
        assertFalse(defaults.chhotuAiStrictMode)
    }

    @Test
    fun chhotuUploadModeratorApprovesCleanVideoAndRejectsViolations() {
        val clean = com.example.chhotuai.ChhotuUploadModerator.evaluate(
            title = "Jaipur Street Food Tour 2026",
            description = "Exploring authentic Rajasthani kachori and lassi spots in the old city.",
            hashtags = "#jaipur #foodvlog #bharattube",
            videoUri = "content://media/video/101",
            thumbnailUri = "content://media/img/101",
            format = com.example.data.model.VideoFormat.LONG,
            durationSeconds = 420,
            strictMode = false
        )
        assertTrue(clean.approved)
        assertEquals("APPROVED", clean.status)
        assertEquals(5, clean.checks.size)
        assertTrue(clean.checks.all { it.status == com.example.chhotuai.CheckItemStatus.PASSED })

        // Copyright violation should reject in both modes
        val copyright = com.example.chhotuai.ChhotuUploadModerator.evaluate(
            title = "New Full Movie Leak HDCam Download",
            description = "Watch now pirated film",
            hashtags = "#movie",
            videoUri = "content://media/video/102",
            thumbnailUri = "thumb_1",
            format = com.example.data.model.VideoFormat.LONG,
            durationSeconds = 600,
            strictMode = false
        )
        assertFalse(copyright.approved)
        assertEquals("REJECTED", copyright.status)
        assertEquals(
            com.example.chhotuai.CheckItemStatus.FAILED,
            copyright.checks.first { it.key == "COPYRIGHT" }.status
        )
        assertTrue(copyright.rejectionReason.contains("Copyright Check"))
        assertTrue(copyright.fixSuggestion.isNotBlank())

        // Unsafe / abusive keyword should reject
        val unsafe = com.example.chhotuai.ChhotuUploadModerator.evaluate(
            title = "Ganda Video Viral Clip",
            description = "Watch clip",
            hashtags = "",
            videoUri = "content://media/video/103",
            thumbnailUri = "thumb_1",
            format = com.example.data.model.VideoFormat.SHORT,
            durationSeconds = 30,
            strictMode = false
        )
        assertFalse(unsafe.approved)
        assertEquals(
            com.example.chhotuai.CheckItemStatus.FAILED,
            unsafe.checks.first { it.key == "SAFETY" }.status
        )
    }

    @Test
    fun chhotuUploadModeratorStrictModeEnforcesStricterMetadataAndCaps() {
        val normalRes = com.example.chhotuai.ChhotuUploadModerator.evaluate(
            title = "DAILY TECH NEWS UPDATE",
            description = "Quick clip",
            hashtags = "#tech",
            videoUri = "content://media/video/104",
            thumbnailUri = "",
            format = com.example.data.model.VideoFormat.LONG,
            durationSeconds = 180,
            strictMode = false
        )
        assertTrue(normalRes.approved)
        assertEquals("NORMAL", normalRes.modeLabel)

        val strictRes = com.example.chhotuai.ChhotuUploadModerator.evaluate(
            title = "DAILY TECH NEWS UPDATE",
            description = "Quick clip",
            hashtags = "#tech",
            videoUri = "content://media/video/104",
            thumbnailUri = "",
            format = com.example.data.model.VideoFormat.LONG,
            durationSeconds = 180,
            strictMode = true
        )
        assertFalse(strictRes.approved)
        assertEquals("STRICT", strictRes.modeLabel)
        assertEquals(
            com.example.chhotuai.CheckItemStatus.FAILED,
            strictRes.checks.first { it.key == "QUALITY" }.status
        )
        assertEquals(
            com.example.chhotuai.CheckItemStatus.FAILED,
            strictRes.checks.first { it.key == "TITLE_THUMB" }.status
        )
    }

    @Test
    fun analyticsEngineComputesTimeframesRetentionTrafficAndEarnings() {
        val sampleVideo = com.example.data.model.VideoEntity(
            id = "v_test_1",
            channelId = "ch_1",
            channelName = "Test Studio",
            channelHandle = "@teststudio",
            title = "Compose Architecture Deep Dive",
            description = "Detailed walkthrough of modern Android UI",
            category = "Technology",
            languageCode = "en",
            countryCode = "IN",
            format = com.example.data.model.VideoFormat.LONG.name,
            durationSeconds = 720,
            thumbnailKey = "thumb_1",
            videoUri = "sample://1",
            hashtags = "#android",
            viewCount = 50_000L,
            likeCount = 4_200L,
            shareCount = 500L,
            watchTimeMinutes = 180_000L,
            subscribersGained = 420,
            subscribersLost = 18,
            netRevenueInr = 2500.0
        )
        val settings = com.example.data.model.PlatformSettingsEntity()

        val snap7 = com.example.data.model.BharatTubeAnalyticsEngine.buildVideoAnalytics(
            video = sampleVideo,
            commentCount = 140,
            timeframe = com.example.data.model.AnalyticsTimeframe.LAST_7_DAYS,
            settings = settings
        )
        val snapAll = com.example.data.model.BharatTubeAnalyticsEngine.buildVideoAnalytics(
            video = sampleVideo,
            commentCount = 140,
            timeframe = com.example.data.model.AnalyticsTimeframe.ALL_TIME,
            settings = settings
        )

        assertTrue(snapAll.views > snap7.views)
        assertEquals(50_000L, snapAll.views)
        assertEquals(500L, snapAll.shares)
        assertEquals(3000.0, snapAll.watchTimeHours, 0.01)
        assertEquals(420, snapAll.subscribersGained)
        assertEquals(18, snapAll.subscribersLost)
        assertEquals(402, snapAll.netSubscribers)
        assertTrue(snapAll.earningsInr > snap7.earningsInr)
        assertEquals(100, snapAll.trafficSources.sumOf { it.percent })
        assertTrue(snapAll.trafficSources.none { it.label.contains("YouTube", ignoreCase = true) })
        assertEquals(100, snapAll.ageGroups.sumOf { it.percent })
        assertEquals(100, snapAll.genderSplit.sumOf { it.percent })
        assertTrue(snapAll.topCities.isNotEmpty())
        assertTrue(snapAll.topCountries.any { it.label == "India" })
    }

    @Test
    fun chhotuCommentModeratorBlocksAbuseAndAllowsCleanComments() {
        val cleanEn = com.example.chhotuai.ChhotuUploadModerator.moderateComment(
            text = "Bahut badhiya video hai bhai! Informative content.",
            strictMode = false
        )
        assertTrue(cleanEn.allowed)

        val abusive = com.example.chhotuai.ChhotuUploadModerator.moderateComment(
            text = "You are a fraud scammer idiot!",
            strictMode = false
        )
        assertFalse(abusive.allowed)
        assertTrue(abusive.reason.contains("Chhotu AI", ignoreCase = true))

        val normalPromo = com.example.chhotuai.ChhotuUploadModerator.moderateComment(
            text = "Join now https://promo-spam-site.com earn 50000 daily",
            strictMode = true
        )
        assertFalse(normalPromo.allowed)
    }
}
