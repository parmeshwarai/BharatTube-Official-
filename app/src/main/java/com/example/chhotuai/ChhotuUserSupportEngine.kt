package com.example.chhotuai

object ChhotuUserSupportEngine {
    enum class SupportTopic {
        GREETING,
        ACCOUNT_CREATE,
        CHANNEL_CREATE,
        VIDEO_UPLOAD,
        SHORTS_CREATE,
        GO_LIVE,
        CREATE_POST,
        EARNINGS_MONETIZATION,
        CREATOR_STUDIO,
        APP_HEALTH_STATUS,
        FEATURES_OVERVIEW,
        NONE
    }

    fun detectTopic(query: String): SupportTopic {
        val q = query.lowercase().trim()
        return when {
            q in listOf("hi", "hello", "hey", "namaste", "hii", "नमस्ते", "हेलो", "हाय") ||
                q.startsWith("hi ") || q.startsWith("hello ") || q.startsWith("namaste") -> SupportTopic.GREETING
            q.contains("health") || q.contains("status") || q.contains("check") ||
                q.contains("error") || q.contains("जाँच") || q.contains("हेल्थ") -> SupportTopic.APP_HEALTH_STATUS
            q.contains("account") || q.contains("login") || q.contains("register") ||
                q.contains("अकाउंट") || q.contains("खाता") -> SupportTopic.ACCOUNT_CREATE
            q.contains("channel") || q.contains("banner") || q.contains("logo") ||
                q.contains("चैनल") || q.contains("बैनर") -> SupportTopic.CHANNEL_CREATE
            q.contains("live") || q.contains("stream") || q.contains("लाइव") -> SupportTopic.GO_LIVE
            q.contains("post") || q.contains("community") || q.contains("पोस्ट") -> SupportTopic.CREATE_POST
            q.contains("upload") || q.contains("camera") || q.contains("filter") ||
                q.contains("अपलोड") || q.contains("कैमरा") -> SupportTopic.VIDEO_UPLOAD
            q.contains("short") || q.contains("reel") || q.contains("शॉर्ट") -> SupportTopic.SHORTS_CREATE
            q.contains("earn") || q.contains("money") || q.contains("monetiz") ||
                q.contains("payout") || q.contains("कमाई") ||
                q.contains("पैसे") || q.contains("मोनेटाइज") -> SupportTopic.EARNINGS_MONETIZATION
            q.contains("studio") || q.contains("dashboard") || q.contains("analytics") ||
                q.contains("स्टूडियो") || q.contains("डैशबोर्ड") -> SupportTopic.CREATOR_STUDIO
            q.contains("feature") || q.contains("help") || q.contains("guide") ||
                q.contains("bharattube") || q.contains("मदद") || q.contains("कैसे") -> SupportTopic.FEATURES_OVERVIEW
            else -> SupportTopic.NONE
        }
    }

    fun buildDirectReply(
        rawQuery: String,
        topic: SupportTopic,
        isAdmin: Boolean,
        languageCode: String,
        health: SystemHealthSnapshot?,
        totalUsers: Int,
        totalVideos: Int,
        totalChannels: Int,
        creatorSharePct: Int,
        platformSharePct: Int,
        indiaThresholdInr: Double,
        historyCount: Int
    ): String {
        val isHindi = languageCode.equals("hi", ignoreCase = true)

        return when (topic) {
            SupportTopic.GREETING -> if (isHindi) {
                "नमस्ते! मैं Chhotu AI हूँ — BharatTube का Official Assistant। मैं बिना किसी बाहरी Key के सीधे ऐप में काम करता हूँ।\n" +
                    "आप मुझसे Video/Short अपलोड, Camera फिल्टर, Live, Channel Logo/Banner, Account, Creator Studio या App Health के बारे में पूछ सकते हैं!"
            } else {
                "Namaste! I am Chhotu AI, BharatTube's built-in Official Assistant (running directly inside BharatTube with no API key needed).\n" +
                    "Ask me about uploading Videos or Shorts, Camera filters, Go Live, Channel Logo/Banner, Accounts, Creator Studio, or System Health!"
            }

            SupportTopic.ACCOUNT_CREATE -> if (isHindi) {
                "BharatTube में Account बनाने के आसान स्टेप्स:\n" +
                    "1. नीचे दाईं ओर 'Profile' टैब खोलें और 'Accounts' बटन दबाएँ।\n" +
                    "2. अपना पूरा नाम, ईमेल या मोबाइल नंबर डालकर 'Create' पर टैप करें।\n" +
                    "3. आपका यूनीक BharatTube User ID और @handle तुरंत तैयार हो जाएगा।"
            } else {
                "How to create an account on BharatTube:\n" +
                    "1. Open the 'Profile' tab at the bottom right and tap 'Accounts'.\n" +
                    "2. Enter your Full Name, optional Email/Phone, and tap 'Create'.\n" +
                    "3. Your unique BharatTube User ID and @handle are created immediately."
            }

            SupportTopic.CHANNEL_CREATE -> if (isHindi) {
                "BharatTube पर Channel बनाने और Logo/Banner लगाने का तरीका:\n" +
                    "1. Profile या Home से 'Create Channel' चुनें।\n" +
                    "2. Channel Name, यूनीक @handle, कैटेगरी और विवरण भरें।\n" +
                    "3. गोल Profile Photo/Logo और ऊपर लंबा Channel Banner लगाने के लिए Gallery से फोटो चुनकर Crop करें और Save करें।"
            } else {
                "How to create and brand your BharatTube Channel:\n" +
                    "1. Tap 'Create Channel' from Profile or Home.\n" +
                    "2. Enter your Channel Name, unique @handle, category, and description.\n" +
                    "3. Tap 'Change Logo' (circular crop) or 'Banner' (wide crop) to pick and crop photos from your Gallery."
            }

            SupportTopic.VIDEO_UPLOAD -> if (isHindi) {
                "YouTube जैसे Camera और Upload Details से वीडियो अपलोड करने का तरीका:\n" +
                    "1. नीचे बीच में बड़ा '+' बटन दबाएँ और 'Upload Video' चुनें।\n" +
                    "2. Camera स्क्रीन में Front/Back कैमरा, Flash, Timer, और Normal/Beauty/Warm/Cool/B&W/Vintage फिल्टर से रिकॉर्ड करें या Gallery से वीडियो चुनें।\n" +
                    "3. Upload Details में Title, Description, Visibility (Public/Private/Unlisted), Audience, Playlist और Comments सेट करके 'Upload' दबाएँ — वीडियो तुरंत Home Page के ऊपर आ जाएगा!"
            } else {
                "How to record & upload a video on BharatTube:\n" +
                    "1. Tap the center '+' button in the bottom bar and pick 'Upload Video'.\n" +
                    "2. On the Camera screen, use Front/Back flip, Flash, Timer, live Filters (Normal, Beauty, Warm, Cool, Black & White, Vintage), or pick from Gallery.\n" +
                    "3. Fill in Title, Description, Visibility (Public/Private/Unlisted), Audience, Playlist, and Comments ON/OFF, then tap 'Upload' — your video appears right at the top of Home!"
            }

            SupportTopic.SHORTS_CREATE -> if (isHindi) {
                "BharatTube Shorts बनाने और देखने का तरीका:\n" +
                    "1. '+' बटन दबाकर 'Create Short' चुनें (या Camera स्क्रीन में नीचे 'Short' टैब चुनें)।\n" +
                    "2. फिल्टर और Timer के साथ रिकॉर्ड करें या Gallery से चुनें, Details भरें और Upload करें।\n" +
                    "3. देखने के लिए नीचे 'Shorts' टैब खोलें।"
            } else {
                "How to create & watch Shorts on BharatTube:\n" +
                    "1. Tap '+' -> 'Create Short' (or switch to the 'Short' tab in the Camera screen).\n" +
                    "2. Record with live Filters & Timer or pick from Gallery, add details, and tap Upload.\n" +
                    "3. Tap the 'Shorts' tab at the bottom to watch and swipe."
            }

            SupportTopic.GO_LIVE -> if (isHindi) {
                "BharatTube पर Live जाने का तरीका:\n" +
                    "1. नीचे '+' बटन दबाएँ और 'Go Live' चुनें।\n" +
                    "2. Live टाइटल लिखें और 'Start Live' दबाएँ — आपका Live स्ट्रीम तुरंत Home Page पर 'Live Now' में जुड़ जाएगा और Live Chat चालू हो जाएगी।"
            } else {
                "How to Go Live on BharatTube:\n" +
                    "1. Tap the center '+' button and choose 'Go Live'.\n" +
                    "2. Enter your stream title and start broadcasting — your Live session immediately appears under 'Live Now' on Home with live chat."
            }

            SupportTopic.CREATE_POST -> if (isHindi) {
                "Community Post बनाने का तरीका:\n" +
                    "1. नीचे '+' बटन दबाएँ और 'Create Post' चुनें।\n" +
                    "2. अपने विचार, अपडेट या सवाल लिखें और 'Publish Post' दबाएँ — यह आपके चैनल की Community फ़ीड में दिखने लगेगा।"
            } else {
                "How to publish a Community Post:\n" +
                    "1. Tap the center '+' button and choose 'Create Post'.\n" +
                    "2. Write your channel update or poll prompt and tap 'Publish Post' to share it on your channel."
            }

            SupportTopic.EARNINGS_MONETIZATION -> {
                val adminSplitNoteHi = if (isAdmin) {
                    "\n• Admin Revenue Split Rule: $creatorSharePct% Creator / $platformSharePct% BharatTube।"
                } else ""
                val adminSplitNoteEn = if (isAdmin) {
                    "\n• Admin Revenue Split Rule: $creatorSharePct% Creator / $platformSharePct% BharatTube."
                } else ""
                if (isHindi) {
                    "BharatTube Creator Earnings और Payout जानकारी:\n" +
                        "• Creator Studio में आप अपने चैनल के कुल Views, Eligible Earnings और Available Balance देख सकते हैं।\n" +
                        "• भारत में न्यूनतम ₹${indiaThresholdInr.toInt()} पूरा होने तथा KYC, UPI/Bank और Tax सत्यापन के बाद Payout शुरू किया जा सकता है।$adminSplitNoteHi"
                } else {
                    "BharatTube Creator Earnings & Payout Info:\n" +
                        "• Open Creator Studio in your Profile to track views, eligible earnings, and available balance.\n" +
                        "• In India, payouts can be initiated once your balance reaches ₹${indiaThresholdInr.toInt()} with KYC, Payment, and Tax verified.$adminSplitNoteEn"
                }
            }

            SupportTopic.CREATOR_STUDIO -> if (isHindi) {
                "Creator Studio का उपयोग:\n" +
                    "1. Profile > 'Creator Studio' खोलें।\n" +
                    "2. यहाँ Views, Subscribers, Watch Time, Video Performance और Payout स्टेटस एक जगह देखें।"
            } else {
                "Using Creator Studio:\n" +
                    "1. Go to Profile > 'Creator Studio'.\n" +
                    "2. Monitor channel Views, Subscribers, Watch Time, per-video stats, and payout readiness."
            }

            SupportTopic.APP_HEALTH_STATUS -> {
                if (health == null) {
                    if (isHindi) "सिस्टम हेल्थ जाँच अभी चल रही है। कृपया कुछ सेकंड में दोबारा पूछें।"
                    else "System health check is initializing. Please check again in a moment."
                } else {
                    val attention = health.items.filter { it.status != CheckStatus.CONNECTED }.joinToString { "${it.moduleName} (${it.status.label})" }
                    if (isHindi) {
                        buildString {
                            append("BharatTube लाइव सिस्टम रिपोर्ट (${health.connectedCount}/${health.totalCount} Connected):\n")
                            append("• Chhotu AI Engine: Active (No API Key Needed)\n")
                            append("• कुल Accounts: $totalUsers | Channels: $totalChannels | Videos: $totalVideos\n")
                            if (attention.isNotBlank()) append("• ध्यान देने योग्य: $attention\n")
                            if (isAdmin) append("पूरी रिपोर्ट Control Center में देखी जा सकती है।")
                        }
                    } else {
                        buildString {
                            append("BharatTube Live System Report (${health.connectedCount}/${health.totalCount} Connected):\n")
                            append("• Chhotu AI Engine: Active (No API Key Needed)\n")
                            append("• Accounts: $totalUsers | Channels: $totalChannels | Videos: $totalVideos\n")
                            if (attention.isNotBlank()) append("• Needs Attention: $attention\n")
                            if (isAdmin) append("Open Control Center for detailed diagnostics and audit logs.")
                        }
                    }
                }
            }

            SupportTopic.FEATURES_OVERVIEW -> if (isHindi) {
                "नमस्ते! मैं Chhotu AI हूँ — BharatTube का Official Built-in Assistant।\n" +
                    "• '+' बटन से Video Upload, Short, Live और Community Post बनाएँ\n" +
                    "• Camera फिल्टर (Normal, Beauty, Warm, Cool, B&W, Vintage) इस्तेमाल करें\n" +
                    "• Channel Logo/Banner, Subscriptions और Creator Studio मैनेज करें\n" +
                    "• लाइव System Health रिपोर्ट पूछें।"
            } else {
                "Namaste! I am Chhotu AI, BharatTube's built-in Official Assistant:\n" +
                    "• Tap '+' to Upload Video, Create Short, Go Live, or Create Post\n" +
                    "• Use Camera Filters (Normal, Beauty, Warm, Cool, Black & White, Vintage)\n" +
                    "• Manage Channel Logo/Banner, Subscriptions & Creator Studio\n" +
                    "• Check live System Health anytime."
            }

            SupportTopic.NONE -> if (isHindi) {
                "मैंने आपका सवाल पढ़ा (\"${rawQuery.take(60)}\")। मैं BharatTube का इन-बिल्ट Chhotu AI हूँ (बिन किसी बाहरी Key के सक्रिय)।\n" +
                    "वर्तमान में ऐप में $totalUsers यूज़र, $totalChannels चैनल और $totalVideos वीडियो मौजूद हैं। " +
                    "आप मुझसे Video Upload, Camera Filter, Shorts, Go Live, Create Post, Channel Logo/Banner, Creator Studio या System Status के बारे में सीधे पूछ सकते हैं।"
            } else {
                "I received your message (\"${rawQuery.take(60)}\"). Chhotu AI is active directly inside BharatTube with no external API keys needed.\n" +
                    "Currently tracking $totalUsers profiles, $totalChannels channels, and $totalVideos videos (conversation history: $historyCount messages). " +
                    "Ask me how to Upload Videos, use Camera Filters, Create Shorts, Go Live, Create Posts, manage your Channel, or check System Status!"
            }
        }
    }
}
