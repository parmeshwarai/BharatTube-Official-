package com.example

import com.example.data.model.BharatTubeCatalog
import org.junit.Assert.assertEquals
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
}
