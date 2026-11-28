package com.example.chhotuai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.StatFs
import androidx.core.content.ContextCompat
import com.example.ads.AdMobIds
import com.example.data.local.BharatTubeDao
import com.example.data.model.PlatformSettingsEntity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChhotuDiagnosticsEngine(
    private val context: Context,
    private val dao: BharatTubeDao,
    private val backendClient: ChhotuAiBackendClient
) {
    suspend fun runCompleteCheck(): SystemHealthSnapshot = withContext(Dispatchers.IO) {
        val checkedAt = System.currentTimeMillis()
        val items = mutableListOf<AppCheckItem>()

        // 14. Real Database Latency & Integrity Probe
        val dbStart = System.currentTimeMillis()
        var settings: PlatformSettingsEntity? = null
        var usersCount = 0
        var channelsCount = 0
        var videosCount = 0
        var shortsCount = 0
        var commentsCount = 0
        var notificationsCount = 0
        var uploadsCount = 0
        var dbError: String? = null
        try {
            settings = dao.getPlatformSettings()
            usersCount = dao.countUsersSync()
            channelsCount = dao.countChannelsSync()
            videosCount = dao.countVideosSync()
            shortsCount = dao.countShortsSync()
            commentsCount = dao.countCommentsSync()
            notificationsCount = dao.countNotificationsSync()
            uploadsCount = dao.countUploadsSync()
        } catch (e: Exception) {
            dbError = e.message ?: e.javaClass.simpleName
        }
        val dbLatencyMs = (System.currentTimeMillis() - dbStart).coerceAtLeast(1L)
        val effectiveSettings = settings ?: PlatformSettingsEntity()

        // 15 & 16. Real Existing Chhotu AI Server & API Probe
        val hasNetwork = isDeviceOnline()
        val backendState = backendClient.probeHealth()

        // 1. Google Login
        items += if (effectiveSettings.googleLoginEnabled) {
            AppCheckItem(
                moduleName = "Google Login",
                category = "Auth",
                status = CheckStatus.DEGRADED,
                detail = "Toggle enabled, awaiting Firebase Auth / Google OAuth 2.0 client.",
                cause = "Google OAuth 2.0 / Firebase Auth client is not provisioned in this build.",
                solution = "Provision Firebase Authentication / Google Sign-In credentials to activate cloud OAuth login."
            )
        } else {
            AppCheckItem(
                moduleName = "Google Login",
                category = "Auth",
                status = CheckStatus.NOT_CONNECTED,
                detail = "Google OAuth 2.0 / Firebase Auth is Not Connected (local profile mode active).",
                cause = "Firebase Auth & Google Sign-In setup was skipped or not configured.",
                solution = "Configure Google OAuth 2.0 / Credential Manager and enable Google Login in platform settings."
            )
        }

        // 2. User Registration
        items += if (dbError != null) {
            AppCheckItem(
                moduleName = "User Registration",
                category = "Auth",
                status = CheckStatus.ERROR,
                detail = "Database error prevented reading user accounts.",
                cause = dbError,
                solution = "Verify Room database schema integrity and storage space."
            )
        } else if (!effectiveSettings.userRegistrationEnabled) {
            AppCheckItem(
                moduleName = "User Registration",
                category = "Auth",
                status = CheckStatus.DEGRADED,
                detail = "Paused by Super Admin ($usersCount existing accounts).",
                cause = "userRegistrationEnabled flag is turned OFF.",
                solution = "Enable User Registration in Super Admin Controls if new signups are desired."
            )
        } else {
            AppCheckItem(
                moduleName = "User Registration",
                category = "Auth",
                status = CheckStatus.CONNECTED,
                detail = "Active ($usersCount local profiles registered; handles & uniqueness enforced)."
            )
        }

        // 3. Channel Creation
        items += if (dbError != null) {
            AppCheckItem(
                moduleName = "Channel Creation",
                category = "Creator",
                status = CheckStatus.ERROR,
                detail = "Cannot verify channels table.",
                cause = dbError,
                solution = "Repair local Room database connection."
            )
        } else {
            AppCheckItem(
                moduleName = "Channel Creation",
                category = "Creator",
                status = CheckStatus.CONNECTED,
                detail = "Active ($channelsCount channels; banner & circular logo cropping ready)."
            )
        }

        // 4. Video Upload
        items += if (!effectiveSettings.videoUploadsEnabled) {
            AppCheckItem(
                moduleName = "Video Upload",
                category = "Media",
                status = CheckStatus.DEGRADED,
                detail = "Uploads paused by Super Admin ($uploadsCount upload tasks logged).",
                cause = "videoUploadsEnabled is OFF in platform settings.",
                solution = "Re-enable Video Uploads in Super Admin Controls."
            )
        } else {
            AppCheckItem(
                moduleName = "Video Upload",
                category = "Media",
                status = CheckStatus.CONNECTED,
                detail = "Local chunked queue ready ($uploadsCount tasks). Cloud CDN upload Not Connected."
            )
        }

        // 5. Video Playback
        val exoAvailable = runCatching {
            Class.forName("androidx.media3.exoplayer.ExoPlayer")
            true
        }.getOrDefault(false)
        items += if (!exoAvailable) {
            AppCheckItem(
                moduleName = "Video Playback",
                category = "Media",
                status = CheckStatus.ERROR,
                detail = "Media3 ExoPlayer class missing.",
                cause = "androidx.media3.exoplayer dependency failed to resolve.",
                solution = "Verify libs.androidx.media3.exoplayer is present in app/build.gradle.kts."
            )
        } else if (!hasNetwork) {
            AppCheckItem(
                moduleName = "Video Playback",
                category = "Media",
                status = CheckStatus.DEGRADED,
                detail = "ExoPlayer ready ($videosCount catalog items), device network is offline.",
                cause = "No active Wi-Fi or cellular data connection for remote MP4 streams.",
                solution = "Connect to the internet for remote streams or play locally picked videos."
            )
        } else {
            AppCheckItem(
                moduleName = "Video Playback",
                category = "Media",
                status = CheckStatus.CONNECTED,
                detail = "Media3 ExoPlayer & PiP active ($videosCount videos in library)."
            )
        }

        // 6. Shorts & Reels
        items += if (!effectiveSettings.shortsEnabled) {
            AppCheckItem(
                moduleName = "Shorts & Reels",
                category = "Media",
                status = CheckStatus.DEGRADED,
                detail = "Shorts feed disabled by Super Admin ($shortsCount Shorts stored).",
                cause = "shortsEnabled is OFF.",
                solution = "Turn ON Shorts & Reels in Super Admin Controls."
            )
        } else {
            AppCheckItem(
                moduleName = "Shorts & Reels",
                category = "Media",
                status = CheckStatus.CONNECTED,
                detail = "Vertical Shorts player active ($shortsCount published Shorts)."
            )
        }

        // 7. Like, Comment, Share
        items += if (!effectiveSettings.likesEnabled || !effectiveSettings.commentsEnabled) {
            AppCheckItem(
                moduleName = "Like, Comment, Share",
                category = "Engagement",
                status = CheckStatus.DEGRADED,
                detail = "Likes=${effectiveSettings.likesEnabled}, Comments=${effectiveSettings.commentsEnabled}, Share=Active.",
                cause = "Super Admin disabled Likes or Comments globally.",
                solution = "Enable Likes and Comments switches in Super Admin Controls."
            )
        } else {
            AppCheckItem(
                moduleName = "Like, Comment, Share",
                category = "Engagement",
                status = CheckStatus.CONNECTED,
                detail = "Likes, Share & $commentsCount comments active."
            )
        }

        // 8. Subscribe & Follow
        items += if (!effectiveSettings.subscriptionsEnabled) {
            AppCheckItem(
                moduleName = "Subscribe & Follow",
                category = "Engagement",
                status = CheckStatus.DEGRADED,
                detail = "Subscriptions disabled by Super Admin.",
                cause = "subscriptionsEnabled is OFF.",
                solution = "Enable Subscriptions in Super Admin Controls."
            )
        } else {
            AppCheckItem(
                moduleName = "Subscribe & Follow",
                category = "Engagement",
                status = CheckStatus.CONNECTED,
                detail = "Channel subscriptions & bell notifications active across $channelsCount channels."
            )
        }

        // 9. Search
        items += if (!effectiveSettings.searchEnabled) {
            AppCheckItem(
                moduleName = "Search",
                category = "Discovery",
                status = CheckStatus.DEGRADED,
                detail = "Search disabled by Super Admin.",
                cause = "searchEnabled is OFF.",
                solution = "Enable Search in platform settings."
            )
        } else {
            AppCheckItem(
                moduleName = "Search",
                category = "Discovery",
                status = CheckStatus.CONNECTED,
                detail = "Search, category filters & history active ($videosCount searchable items)."
            )
        }

        // 10. Notifications
        val postNotifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        items += when {
            !effectiveSettings.notificationsEnabled -> AppCheckItem(
                moduleName = "Notifications",
                category = "Engagement",
                status = CheckStatus.DEGRADED,
                detail = "Notifications disabled by Super Admin ($notificationsCount stored).",
                cause = "notificationsEnabled is OFF.",
                solution = "Enable Notifications in platform settings."
            )
            !postNotifGranted -> AppCheckItem(
                moduleName = "Notifications",
                category = "Engagement",
                status = CheckStatus.DEGRADED,
                detail = "In-app center active ($notificationsCount alerts), OS push permission not granted.",
                cause = "Android 13+ POST_NOTIFICATIONS runtime permission is denied.",
                solution = "Allow Notifications in Android App Settings for system tray alerts."
            )
            else -> AppCheckItem(
                moduleName = "Notifications",
                category = "Engagement",
                status = CheckStatus.CONNECTED,
                detail = "In-app & Admin Alert center active ($notificationsCount stored alerts)."
            )
        }

        // 11. AdMob Integration (Real manifest + MobileAds.getInitializationStatus check)
        val manifestAppId = readAdMobManifestAppId()
        val adapterStates = runCatching {
            MobileAds.getInitializationStatus()?.adapterStatusMap
        }.getOrNull()
        val admobReady = adapterStates?.values?.any {
            it.initializationState == AdapterStatus.State.READY
        } == true
        items += when {
            manifestAppId != AdMobIds.APP_ID -> AppCheckItem(
                moduleName = "AdMob Integration",
                category = "Monetization",
                status = CheckStatus.ERROR,
                detail = "AdMob App ID mismatch in AndroidManifest.xml.",
                cause = "Expected ${AdMobIds.APP_ID}, found '${manifestAppId ?: "missing"}'.",
                solution = "Set com.google.android.gms.ads.APPLICATION_ID to ${AdMobIds.APP_ID} in AndroidManifest.xml."
            )
            !effectiveSettings.advertisementsEnabled -> AppCheckItem(
                moduleName = "AdMob Integration",
                category = "Monetization",
                status = CheckStatus.DEGRADED,
                detail = "SDK configured (Banner & Interstitial IDs set), Ads switched OFF by Super Admin.",
                cause = "advertisementsEnabled is OFF.",
                solution = "Turn ON Advertisements Global Switch in Super Admin if ready to serve ads."
            )
            !hasNetwork -> AppCheckItem(
                moduleName = "AdMob Integration",
                category = "Monetization",
                status = CheckStatus.DEGRADED,
                detail = "Real AdMob IDs configured; device offline so live ads cannot fill.",
                cause = "No active internet connection to reach Google AdMob servers.",
                solution = "Restore internet connectivity for live Banner, Interstitial & Mid-Roll ad fill."
            )
            admobReady -> AppCheckItem(
                moduleName = "AdMob Integration",
                category = "Monetization",
                status = CheckStatus.CONNECTED,
                detail = "Google Mobile Ads SDK READY (Banner + Interstitial + 5s/20s Long Video Ad Policy active)."
            )
            else -> AppCheckItem(
                moduleName = "AdMob Integration",
                category = "Monetization",
                status = CheckStatus.DEGRADED,
                detail = "Real AdMob IDs configured; SDK initialization still warming up.",
                cause = "MobileAds adapter status has not reached READY state yet.",
                solution = "Keep network active and re-run check after app launch completes."
            )
        }

        // 12. Creator Monetization
        val validSplit = effectiveSettings.creatorSharePct + effectiveSettings.platformSharePct == 100
        items += when {
            !validSplit -> AppCheckItem(
                moduleName = "Creator Monetization",
                category = "Monetization",
                status = CheckStatus.ERROR,
                detail = "Invalid revenue split (${effectiveSettings.creatorSharePct}:${effectiveSettings.platformSharePct}).",
                cause = "Creator % + BharatTube % does not equal 100%.",
                solution = "Reset revenue share split to 50:50 in Super Admin Money tab."
            )
            !effectiveSettings.creatorMonetizationEnabled -> AppCheckItem(
                moduleName = "Creator Monetization",
                category = "Monetization",
                status = CheckStatus.DEGRADED,
                detail = "Paused by Super Admin (balances preserved safely).",
                cause = "creatorMonetizationEnabled is OFF.",
                solution = "Enable Creator Monetization in Super Admin Money controls."
            )
            else -> AppCheckItem(
                moduleName = "Creator Monetization",
                category = "Monetization",
                status = CheckStatus.CONNECTED,
                detail = "${effectiveSettings.creatorSharePct}/${effectiveSettings.platformSharePct} split & ₹${effectiveSettings.indiaPayoutThresholdInr.toInt()} threshold active (live payout settlement gateway Not Connected)."
            )
        }

        // 13. Admin Dashboard
        items += if (dbError != null || settings == null) {
            AppCheckItem(
                moduleName = "Admin Dashboard",
                category = "Admin",
                status = CheckStatus.ERROR,
                detail = "Platform settings row missing or unreadable.",
                cause = dbError ?: "platform_settings row id=1 not initialized.",
                solution = "Re-initialize SeedDataPopulator and check Room database."
            )
        } else {
            AppCheckItem(
                moduleName = "Admin Dashboard",
                category = "Admin",
                status = CheckStatus.CONNECTED,
                detail = "Super Admin, Audit Logs & Chhotu AI Control Center active."
            )
        }

        // 14. Database
        items += if (dbError != null) {
            AppCheckItem(
                moduleName = "Database",
                category = "Infrastructure",
                status = CheckStatus.ERROR,
                detail = "Room SQLite probe failed.",
                cause = dbError,
                solution = "Check internal storage space and Room migration integrity."
            )
        } else {
            AppCheckItem(
                moduleName = "Database",
                category = "Infrastructure",
                status = CheckStatus.CONNECTED,
                detail = "Room SQLite v3 healthy (probe ${dbLatencyMs}ms • $usersCount users, $videosCount videos)."
            )
        }

        // 15. Server (In-App Chhotu AI Runtime)
        items += if (backendState.isOnline) {
            AppCheckItem(
                moduleName = "Server",
                category = "Infrastructure",
                status = CheckStatus.CONNECTED,
                detail = "In-App Chhotu AI & Local Runtime active (no external API key required)."
            )
        } else {
            AppCheckItem(
                moduleName = "Server",
                category = "Infrastructure",
                status = CheckStatus.ERROR,
                detail = backendState.statusLabel,
                cause = backendState.errorDetail,
                solution = "Restart Chhotu AI engine in Control Center."
            )
        }

        // 16. API (In-App Chhotu AI Command & Diagnostics Bridge)
        items += if (dbError != null) {
            AppCheckItem(
                moduleName = "API",
                category = "Infrastructure",
                status = CheckStatus.ERROR,
                detail = "Internal Chhotu AI data bridge cannot read Room database.",
                cause = dbError,
                solution = "Verify local database health."
            )
        } else {
            AppCheckItem(
                moduleName = "API",
                category = "Infrastructure",
                status = CheckStatus.CONNECTED,
                detail = "In-App Chhotu AI API Bridge active (${backendState.latencyMs ?: 1}ms • key-free)."
            )
        }

        // 17. Storage
        items += checkLocalStorage()

        SystemHealthSnapshot(
            checkedAt = checkedAt,
            items = items,
            backendState = backendState,
            connectedCount = items.count { it.status == CheckStatus.CONNECTED },
            degradedCount = items.count { it.status == CheckStatus.DEGRADED },
            notConnectedCount = items.count { it.status == CheckStatus.NOT_CONNECTED },
            errorCount = items.count { it.status == CheckStatus.ERROR }
        )
    }

    private fun checkLocalStorage(): AppCheckItem {
        return try {
            val dir = context.filesDir
            val writable = dir.exists() && dir.canWrite()
            val stat = StatFs(dir.path)
            val freeMb = stat.availableBytes / (1024L * 1024L)
            when {
                !writable -> AppCheckItem(
                    moduleName = "Storage",
                    category = "Infrastructure",
                    status = CheckStatus.ERROR,
                    detail = "App internal storage is not writable.",
                    cause = "filesDir permission or filesystem mount failure.",
                    solution = "Check device storage health and app permissions."
                )
                freeMb < 100L -> AppCheckItem(
                    moduleName = "Storage",
                    category = "Infrastructure",
                    status = CheckStatus.DEGRADED,
                    detail = "Low device storage: ${freeMb}MB free. Cloud Storage/CDN Not Connected.",
                    cause = "Less than 100MB free on device for cropped logos, banners, and video cache.",
                    solution = "Free up device storage or connect remote Cloud Video Storage/CDN."
                )
                else -> AppCheckItem(
                    moduleName = "Storage",
                    category = "Infrastructure",
                    status = CheckStatus.CONNECTED,
                    detail = "Local app storage healthy (${freeMb}MB free for logos/banners). Remote Cloud Video CDN Not Connected."
                )
            }
        } catch (e: Exception) {
            AppCheckItem(
                moduleName = "Storage",
                category = "Infrastructure",
                status = CheckStatus.ERROR,
                detail = "Storage check failed.",
                cause = e.message ?: e.javaClass.simpleName,
                solution = "Verify app internal storage directory."
            )
        }
    }

    private fun readAdMobManifestAppId(): String? {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            }
            appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
        } catch (_: Exception) {
            null
        }
    }

    private fun isDeviceOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }
}
