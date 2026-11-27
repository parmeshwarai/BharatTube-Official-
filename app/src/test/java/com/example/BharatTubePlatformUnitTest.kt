package com.example

import com.example.ads.LongVideoAdPolicy
import com.example.chhotuai.BackendConnectionState
import com.example.chhotuai.ChhotuAiBackendClient
import com.example.chhotuai.ChhotuSecurityGuard
import com.example.chhotuai.ChhotuUserSupportEngine
import com.example.data.model.BharatTubeCatalog
import com.example.ui.components.BharatTubeShareHelper
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
    fun chhotuBackendClientReportsNotConnectedWhenUnconfigured() = runBlocking {
        val client = ChhotuAiBackendClient(baseUrlProvider = { "" }, tokenProvider = { "" })
        assertFalse(client.isConfigured())
        val state = client.probeHealth()
        assertFalse(state.isConfigured)
        assertFalse(state.isOnline)
        assertTrue(state.statusLabel.contains("Not Connected"))
    }

    @Test
    fun chhotuUserSupportProvidesVerifiedHindiAndEnglishGuides() {
        val topic = ChhotuUserSupportEngine.detectTopic("कमाई और मोनेटाइजेशन नियम बताओ")
        assertEquals(ChhotuUserSupportEngine.SupportTopic.EARNINGS_MONETIZATION, topic)

        val reply = ChhotuUserSupportEngine.buildGuideReply(
            topic = topic,
            languageCode = "hi",
            health = null,
            creatorSharePct = 50,
            platformSharePct = 50,
            indiaThresholdInr = 1000.0,
            backendState = BackendConnectionState(
                isConfigured = false,
                isOnline = false,
                statusLabel = "Not Connected"
            )
        )
        assertTrue(reply.contains("50%"))
        assertTrue(reply.contains("1000"))
        assertTrue(reply.contains("Not Connected"))
    }
}
