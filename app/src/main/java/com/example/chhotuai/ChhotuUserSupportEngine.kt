package com.example.chhotuai

object ChhotuUserSupportEngine {
    enum class SupportTopic {
        ACCOUNT_CREATE,
        CHANNEL_CREATE,
        VIDEO_UPLOAD,
        SHORTS_CREATE,
        EARNINGS_MONETIZATION,
        CREATOR_STUDIO,
        APP_HEALTH_STATUS,
        FEATURES_OVERVIEW,
        NONE
    }

    fun detectTopic(query: String): SupportTopic {
        val q = query.lowercase()
        return when {
            q.contains("health") || q.contains("status") || q.contains("check") ||
                q.contains("error") || q.contains("जाँच") || q.contains("हेल्थ") -> SupportTopic.APP_HEALTH_STATUS
            q.contains("account") || q.contains("login") || q.contains("register") ||
                q.contains("अकाउंट") || q.contains("खाता") -> SupportTopic.ACCOUNT_CREATE
            q.contains("channel") || q.contains("banner") || q.contains("logo") ||
                q.contains("चैनल") || q.contains("बैनर") -> SupportTopic.CHANNEL_CREATE
            q.contains("upload") || q.contains("अपलोड") -> SupportTopic.VIDEO_UPLOAD
            q.contains("short") || q.contains("reel") || q.contains("शॉर्ट") -> SupportTopic.SHORTS_CREATE
            q.contains("earn") || q.contains("money") || q.contains("monetiz") ||
                q.contains("payout") || q.contains("ad") || q.contains("कमाई") ||
                q.contains("पैसे") || q.contains("मोनेटाइज") -> SupportTopic.EARNINGS_MONETIZATION
            q.contains("studio") || q.contains("dashboard") || q.contains("analytics") ||
                q.contains("स्टूडियो") || q.contains("डैशबोर्ड") -> SupportTopic.CREATOR_STUDIO
            q.contains("feature") || q.contains("help") || q.contains("guide") ||
                q.contains("bharattube") || q.contains("मदद") || q.contains("कैसे") -> SupportTopic.FEATURES_OVERVIEW
            else -> SupportTopic.NONE
        }
    }

    fun buildGuideReply(
        topic: SupportTopic,
        languageCode: String,
        health: SystemHealthSnapshot?,
        creatorSharePct: Int,
        platformSharePct: Int,
        indiaThresholdInr: Double,
        backendState: BackendConnectionState
    ): String {
        val isHindi = languageCode.equals("hi", ignoreCase = true)
        val backendNotice = if (backendState.isOnline) {
            ""
        } else if (isHindi) {
            "\n\n[Existing Chhotu AI Server: Not Connected — यह सत्यापित BharatTube सहायता गाइड है।]"
        } else {
            "\n\n[Existing Chhotu AI Server: Not Connected — Showing verified BharatTube app guide.]"
        }

        val body = when (topic) {
            SupportTopic.ACCOUNT_CREATE -> if (isHindi) {
                "BharatTube में Account बनाने के आसान स्टेप्स:\n" +
                    "1. नीचे Profile टैब खोलें और 'Accounts' बटन दबाएँ।\n" +
                    "2. अपना पूरा नाम, ईमेल या मोबाइल नंबर डालें और 'Create' पर टैप करें।\n" +
                    "3. आपका यूनीक BharatTube User ID और @handle तुरंत बन जाएगा।\n" +
                    "4. नोट: अभी Google OAuth 2.0 / Cloud Login 'Not Connected' है, इसलिए प्रोफाइल आपके डिवाइस पर सुरक्षित रहती है।"
            } else {
                "How to create an account on BharatTube:\n" +
                    "1. Open the Profile tab at the bottom and tap 'Accounts'.\n" +
                    "2. Enter your Full Name, optional Email/Phone, and tap 'Create'.\n" +
                    "3. Your unique BharatTube User ID and @handle are created immediately.\n" +
                    "4. Note: Google OAuth 2.0 cloud sign-in is currently Not Connected, so profiles are stored locally on your device."
            }

            SupportTopic.CHANNEL_CREATE -> if (isHindi) {
                "BharatTube पर Channel बनाने और Logo/Banner लगाने का तरीका:\n" +
                    "1. Profile या Home से 'Create Channel' चुनें।\n" +
                    "2. Channel Name, यूनीक @handle, कैटेगरी और विवरण भरें।\n" +
                    "3. गोल Profile Photo/Logo और ऊपर लंबा Channel Banner लगाने के लिए Gallery से फोटो चुनकर Crop करें और Save करें।"
            } else {
                "How to create and brand your BharatTube Channel:\n" +
                    "1. Tap 'Create Channel' from Profile or Home.\n" +
                    "2. Enter your Channel Name, unique @handle, category, and bio.\n" +
                    "3. Tap 'Change Logo' (circular crop) or 'Banner' (wide crop) to pick and crop photos from your Gallery."
            }

            SupportTopic.VIDEO_UPLOAD -> if (isHindi) {
                "वीडियो अपलोड करने का तरीका:\n" +
                    "1. नीचे 'Upload' टैब पर जाएँ।\n" +
                    "2. वीडियो फॉर्मेट (Long Video या Short), भाषा और देश चुनें।\n" +
                    "3. Gallery से वीडियो चुनें, Title/Description/Hashtags लिखें और Upload शुरू करें।\n" +
                    "4. आप Chunked Upload को कभी भी Pause, Resume या Cancel कर सकते हैं।"
            } else {
                "How to upload videos on BharatTube:\n" +
                    "1. Open the 'Upload' tab from the bottom navigation bar.\n" +
                    "2. Choose Long Video or Short format, language, country, and visibility.\n" +
                    "3. Pick your video and thumbnail, add title/hashtags, and start uploading.\n" +
                    "4. Chunked uploads support Pause, Resume, and Cancel at any time."
            }

            SupportTopic.SHORTS_CREATE -> if (isHindi) {
                "BharatTube Shorts कैसे बनाएँ और देखें:\n" +
                    "1. बनाने के लिए 'Upload' टैब में Format = 'Short' चुनें (15–60 सेकंड वर्टिकल वीडियो)।\n" +
                    "2. देखने के लिए नीचे 'Shorts' टैब खोलें और ऊपर-नीचे स्वाइप करें, Like/Comment/Subscribe करें।"
            } else {
                "How to create & watch Shorts on BharatTube:\n" +
                    "1. To publish a Short, open the 'Upload' tab and select Format = 'Short'.\n" +
                    "2. To watch, tap the 'Shorts' tab in the bottom bar and swipe vertically to like, comment, or subscribe."
            }

            SupportTopic.EARNINGS_MONETIZATION -> if (isHindi) {
                "BharatTube से कमाई (Monetization) और Payout नियम:\n" +
                    "• Revenue Share: योग्य नेट Ad कमाई का $creatorSharePct% Creator को और $platformSharePct% BharatTube को मिलता है।\n" +
                    "• Long Video Ads: 3 मिनट से छोटे वीडियो में शुरू में 5 सेकंड का Skip Ad; 8 मिनट से लंबे वीडियो में हर 8 मिनट पर 20 सेकंड का Mid-Roll Ad।\n" +
                    "• Payout सीमा: भारत में न्यूनतम ₹${indiaThresholdInr.toInt()} पूरा होने, KYC + Payment + Tax सत्यापन के बाद Creator Studio से UPI/Bank Payout शुरू किया जा सकता है।"
            } else {
                "BharatTube Earnings & Monetization Policy:\n" +
                    "• Revenue Split: Creators earn $creatorSharePct% and BharatTube keeps $platformSharePct% of eligible net ad revenue.\n" +
                    "• Long Video Ad Rules: Videos under 3 min have only a 5s skippable pre-roll ad; videos over 8 min include a 20s mid-roll ad every 8 minutes.\n" +
                    "• Payout Threshold: In India, once your balance reaches ₹${indiaThresholdInr.toInt()} and KYC, Payment & Tax are verified, you can initiate payouts in Creator Studio."
            }

            SupportTopic.CREATOR_STUDIO -> if (isHindi) {
                "Creator Dashboard / Studio का उपयोग:\n" +
                    "1. Profile > 'Creator Studio & Monetization' खोलें।\n" +
                    "2. यहाँ अपने चैनल के Views, Subscribers, Net Revenue और $creatorSharePct/$platformSharePct कमाई का पूरा हिसाब देखें।\n" +
                    "3. यहीं से आप Payout status और Ad eligibility भी ट्रैक कर सकते हैं।"
            } else {
                "Using the BharatTube Creator Studio:\n" +
                    "1. Go to Profile > 'Creator Studio & Monetization'.\n" +
                    "2. View real-time channel views, subscribers, net ad revenue, and your $creatorSharePct% creator share.\n" +
                    "3. Check KYC/Tax readiness and initiate payouts once above the threshold."
            }

            SupportTopic.APP_HEALTH_STATUS -> {
                if (health == null) {
                    if (isHindi) "सिस्टम हेल्थ जाँच अभी चल रही है। कृपया कुछ सेकंड में दोबारा पूछें।"
                    else "System health check is initializing. Please try again in a moment."
                } else {
                    val notConnNames = health.items.filter { it.status == CheckStatus.NOT_CONNECTED }.joinToString { it.moduleName }
                    val errNames = health.items.filter { it.status == CheckStatus.ERROR }.joinToString { it.moduleName }
                    val degNames = health.items.filter { it.status == CheckStatus.DEGRADED }.joinToString { it.moduleName }
                    if (isHindi) {
                        buildString {
                            append("BharatTube सिस्टम जाँच (${health.connectedCount}/${health.totalCount} Connected):\n")
                            append("• Chhotu AI Backend: ${health.backendState.statusLabel}\n")
                            if (notConnNames.isNotBlank()) append("• Not Connected: $notConnNames\n")
                            if (degNames.isNotBlank()) append("• Needs Attention: $degNames\n")
                            if (errNames.isNotBlank()) append("• Errors: $errNames\n")
                            append("विस्तृत रिपोर्ट Admin के लिए Chhotu AI Control Center में उपलब्ध है।")
                        }
                    } else {
                        buildString {
                            append("BharatTube System Check (${health.connectedCount}/${health.totalCount} Connected):\n")
                            append("• Chhotu AI Backend: ${health.backendState.statusLabel}\n")
                            if (notConnNames.isNotBlank()) append("• Not Connected: $notConnNames\n")
                            if (degNames.isNotBlank()) append("• Needs Attention: $degNames\n")
                            if (errNames.isNotBlank()) append("• Errors: $errNames\n")
                            append("Full causes & fixes are logged in the Chhotu AI Control Center.")
                        }
                    }
                }
            }

            SupportTopic.FEATURES_OVERVIEW -> if (isHindi) {
                "नमस्ते! मैं Chhotu AI हूँ — BharatTube का Official Assistant। मैं आपकी मदद कर सकता हूँ:\n" +
                    "• Account और Channel बनाने में (Logo/Banner सहित)\n" +
                    "• Video और Shorts अपलोड करने में\n" +
                    "• कमाई ($creatorSharePct/$platformSharePct Share) और Creator Studio समझने में\n" +
                    "• ऐप की रियल सिस्टम हेल्थ जाँच बताने में।"
            } else {
                "Namaste! I am Chhotu AI, BharatTube's Official Assistant. I can guide you with:\n" +
                    "• Creating an Account & Channel (with Logo & Banner)\n" +
                    "• Uploading Long Videos & Shorts\n" +
                    "• Understanding Earnings ($creatorSharePct/$platformSharePct split) & Creator Studio\n" +
                    "• Checking real BharatTube system health."
            }

            SupportTopic.NONE -> if (isHindi) {
                "आपके मौजूदा Chhotu AI सर्वर से कनेक्शन अभी 'Not Connected' है (CHHOTU_AI_BASE_URL कॉन्फ़िगर नहीं है)।\n" +
                    "मैं अभी आपको Account, Channel, Video Upload, Shorts, Earnings/Monetization, Creator Studio और System Status से जुड़े सत्यापित गाइड दे सकता हूँ।"
            } else {
                "Your existing Chhotu AI backend is currently 'Not Connected' (CHHOTU_AI_BASE_URL is not configured in Secrets).\n" +
                    "Without fabricating an AI model response, I can immediately guide you on Account creation, Channel setup, Video Upload, Shorts, Earnings/Monetization, Creator Studio, or System Status."
            }
        }

        return body + backendNotice
    }
}
