package com.example.data.model

enum class VideoFormat { LONG, SHORT, LIVE }

enum class VideoVisibility { PUBLIC, PRIVATE, UNLISTED, SCHEDULED, DRAFT }

enum class UploadStatus {
    QUEUED,
    UPLOADING,
    PAUSED,
    TRANSCODING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class AdPlacement(val label: String) {
    PRE_ROLL("Pre-Roll Video Ad"),
    MID_ROLL("Mid-Roll Video Ad"),
    SHORTS_INTERSTITIAL("Between Shorts Ad"),
    HOME_NATIVE("Home Feed Native Ad"),
    BANNER("Banner Ad")
}

enum class PayoutMethod(val label: String) {
    UPI("UPI Instant Transfer"),
    BANK_TRANSFER("Domestic Bank Transfer (NEFT/IMPS)"),
    INTERNATIONAL_SWIFT("International Wire (SWIFT)")
}

enum class PayoutStatus {
    PENDING_VERIFICATION,
    INITIATED,
    CONFIRMED,
    FAILED
}

data class SupportedLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val isRtl: Boolean = false
)

data class SupportedCountry(
    val code: String,
    val name: String,
    val currencyCode: String,
    val currencySymbol: String,
    val minPayoutThresholdLocal: Double,
    val exchangeRateFromInr: Double
)

data class RevenueBreakdown(
    val grossRevenueInr: Double,
    val networkAndTaxDeductionInr: Double,
    val netRevenueInr: Double,
    val creatorSharePct: Int,
    val platformSharePct: Int,
    val creatorEarningsInr: Double,
    val platformEarningsInr: Double
)

object BharatTubeCatalog {
    val initialLanguages: List<SupportedLanguage> = listOf(
        SupportedLanguage("hi", "Hindi", "हिन्दी"),
        SupportedLanguage("en", "English", "English"),
        SupportedLanguage("bho", "Bhojpuri", "भोजपुरी"),
        SupportedLanguage("bn", "Bengali", "বাংলা"),
        SupportedLanguage("ta", "Tamil", "தமிழ்"),
        SupportedLanguage("te", "Telugu", "తెలుగు"),
        SupportedLanguage("mr", "Marathi", "मराठी"),
        SupportedLanguage("gu", "Gujarati", "ગુજરાતી"),
        SupportedLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ"),
        SupportedLanguage("zh", "Chinese", "中文"),
        SupportedLanguage("ja", "Japanese", "日本語"),
        SupportedLanguage("ko", "Korean", "한국어"),
        SupportedLanguage("ar", "Arabic", "العربية", isRtl = true),
        SupportedLanguage("fr", "French", "Français"),
        SupportedLanguage("de", "German", "Deutsch"),
        SupportedLanguage("es", "Spanish", "Español"),
        SupportedLanguage("pt", "Portuguese", "Português"),
        SupportedLanguage("ru", "Russian", "Русский"),
        SupportedLanguage("id", "Indonesian", "Bahasa Indonesia"),
        SupportedLanguage("th", "Thai", "ไทย")
    )

    val countries: List<SupportedCountry> = listOf(
        SupportedCountry("IN", "India", "INR", "₹", 1000.0, 1.0),
        SupportedCountry("US", "United States", "USD", "$", 25.0, 0.012),
        SupportedCountry("GB", "United Kingdom", "GBP", "£", 20.0, 0.0095),
        SupportedCountry("JP", "Japan", "JPY", "¥", 3000.0, 1.82),
        SupportedCountry("AE", "United Arab Emirates", "AED", "AED ", 100.0, 0.044),
        SupportedCountry("DE", "Germany", "EUR", "€", 25.0, 0.011),
        SupportedCountry("BR", "Brazil", "BRL", "R$", 120.0, 0.068),
        SupportedCountry("ID", "Indonesia", "IDR", "Rp", 300000.0, 190.0),
        SupportedCountry("TH", "Thailand", "THB", "฿", 800.0, 0.42),
        SupportedCountry("KR", "South Korea", "KRW", "₩", 30000.0, 16.5)
    )

    val categories: List<String> = listOf(
        "All",
        "Technology",
        "Music",
        "Travel",
        "Education",
        "Gaming",
        "News",
        "Comedy",
        "Cinema",
        "Sports",
        "Podcasts"
    )

    fun calculateRevenueSplit(
        grossRevenueInr: Double,
        networkFeeRate: Double = 0.15,
        taxWithholdingRate: Double = 0.05,
        creatorSharePct: Int = 50,
        platformSharePct: Int = 50
    ): RevenueBreakdown {
        require(creatorSharePct + platformSharePct == 100) {
            "Revenue share percentages must equal 100%"
        }
        val safeGross = grossRevenueInr.coerceAtLeast(0.0)
        val deductions = safeGross * (networkFeeRate + taxWithholdingRate)
        val net = (safeGross - deductions).coerceAtLeast(0.0)
        val creator = net * (creatorSharePct / 100.0)
        val platform = net - creator
        return RevenueBreakdown(
            grossRevenueInr = safeGross,
            networkAndTaxDeductionInr = deductions,
            netRevenueInr = net,
            creatorSharePct = creatorSharePct,
            platformSharePct = platformSharePct,
            creatorEarningsInr = creator,
            platformEarningsInr = platform
        )
    }

    fun calculateCreatorEarningsInr(
        viewCount: Long,
        netRevenueInr: Double,
        creatorSharePct: Int,
        per1000ViewsPaymentInr: Double,
        bonusPercent: Int,
        userBoostPct: Int = 0
    ): Double {
        val cpmPool = (viewCount.coerceAtLeast(0L) / 1000.0) * per1000ViewsPaymentInr.coerceAtLeast(0.0)
        val combinedPool = cpmPool + netRevenueInr.coerceAtLeast(0.0)
        val creatorBase = combinedPool * (creatorSharePct.coerceIn(0, 100) / 100.0)
        val totalBonusPct = (bonusPercent.coerceAtLeast(0) + (userBoostPct.coerceAtLeast(0) / 5)).coerceAtMost(200)
        return creatorBase * (1.0 + totalBonusPct / 100.0)
    }

    fun formatCurrency(amountInr: Double, countryCode: String): String {
        val country = countries.firstOrNull { it.code == countryCode } ?: countries.first()
        val converted = amountInr * country.exchangeRateFromInr
        return if (country.code == "JP" || country.code == "ID" || country.code == "KR") {
            "${country.currencySymbol}${String.format("%,.0f", converted)}"
        } else {
            "${country.currencySymbol}${String.format("%,.2f", converted)}"
        }
    }

    fun formatCompactCount(count: Long): String {
        return when {
            count >= 1_000_000_000L -> String.format("%.1fB", count / 1_000_000_000.0)
            count >= 1_000_000L -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000L -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun formatDuration(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%d:%02d", mins, secs)
        }
    }
}
