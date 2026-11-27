package com.example

import com.example.ads.LongVideoAdPolicy
import com.example.data.model.BharatTubeCatalog
import com.example.ui.components.BharatTubeShareHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
