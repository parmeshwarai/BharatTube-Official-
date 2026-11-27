package com.example

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ads.AdMobInterstitialController
import com.example.data.model.VideoEntity
import com.example.ui.components.BharatTab
import com.example.ui.components.BharatTubeBottomBar
import com.example.ui.components.BharatTubeTopBar
import com.example.ui.screens.AdvertiserScreen
import com.example.ui.screens.ChannelScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreateChannelScreen
import com.example.ui.screens.CreatorStudioScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LiveStreamScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.SuperAdminScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.BharatTubeTheme
import com.example.ui.viewmodel.BharatTubeViewModel

class MainActivity : ComponentActivity() {
    private lateinit var adMobController: AdMobInterstitialController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adMobController = AdMobInterstitialController(applicationContext).apply {
            initializeAndPreload()
        }
        setContent {
            val viewModel: BharatTubeViewModel = viewModel(factory = BharatTubeViewModel.Factory)
            val prefs by viewModel.preferences.collectAsStateWithLifecycle()

            BharatTubeTheme(darkTheme = prefs.isDarkTheme) {
                BharatTubeApp(
                    viewModel = viewModel,
                    activity = this,
                    adMobController = adMobController,
                    onEnterPip = { enterPipIfSupported() }
                )
            }
        }
    }

    private fun enterPipIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }
}

@Composable
fun BharatTubeApp(
    viewModel: BharatTubeViewModel,
    activity: MainActivity,
    adMobController: AdMobInterstitialController,
    onEnterPip: () -> Unit
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val settings by viewModel.platformSettings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allChannels by viewModel.allChannels.collectAsStateWithLifecycle()
    val myChannels by viewModel.myChannels.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val recommendedVideos by viewModel.recommendedVideos.collectAsStateWithLifecycle()
    val shortsFeed by viewModel.shortsFeed.collectAsStateWithLifecycle()
    val liveStreams by viewModel.liveStreams.collectAsStateWithLifecycle()
    val uploads by viewModel.uploads.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playlistRefs by viewModel.playlistRefs.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val adCampaigns by viewModel.adCampaigns.collectAsStateWithLifecycle()
    val payouts by viewModel.payouts.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val supportTickets by viewModel.supportTickets.collectAsStateWithLifecycle()
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val openVideoWithAd: (VideoEntity) -> Unit = { video ->
        if (video.isLive) {
            if (settings.advertisementsEnabled) {
                adMobController.showThenProceed(activity) {
                    navController.navigate("live/${video.id}")
                }
            } else {
                navController.navigate("live/${video.id}")
            }
        } else {
            navController.navigate("player/${video.id}")
        }
    }

    val topLevelRoutes = setOf(
        BharatTab.HOME.route,
        BharatTab.EXPLORE.route,
        BharatTab.UPLOAD.route,
        BharatTab.SHORTS.route,
        BharatTab.PROFILE.route
    )
    val isTopLevel = currentRoute in topLevelRoutes || currentRoute?.startsWith("shorts") == true
    val showTopBar = currentRoute in setOf(
        BharatTab.HOME.route,
        BharatTab.EXPLORE.route,
        BharatTab.UPLOAD.route,
        BharatTab.PROFILE.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showTopBar) {
                BharatTubeTopBar(
                    isDarkTheme = prefs.isDarkTheme,
                    unreadNotificationCount = notifications.count { !it.isRead },
                    countryCode = prefs.preferredCountry,
                    languageCode = prefs.preferredLanguage,
                    onSearchClick = { navController.navigate("search") },
                    onNotificationsClick = { navController.navigate("notifications") },
                    onToggleTheme = { viewModel.toggleDarkTheme() },
                    onRegionClick = { navController.navigate(BharatTab.EXPLORE.route) }
                )
            }
        },
        bottomBar = {
            if (isTopLevel) {
                val activeTabRoute = if (currentRoute?.startsWith("shorts") == true) BharatTab.SHORTS.route else currentRoute
                BharatTubeBottomBar(
                    currentRoute = activeTabRoute,
                    onSelectTab = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = BharatTab.HOME.route
            ) {
                composable(BharatTab.HOME.route) {
                    HomeScreen(
                        recommendedVideos = recommendedVideos,
                        shorts = shortsFeed,
                        liveStreams = liveStreams,
                        channels = allChannels,
                        selectedCategory = selectedCategory,
                        settings = settings,
                        countryCode = prefs.preferredCountry,
                        languageCode = prefs.preferredLanguage,
                        onSelectCategory = viewModel::selectCategory,
                        onOpenVideo = openVideoWithAd,
                        onOpenShort = { idx -> navController.navigate("shorts?index=$idx") },
                        onOpenLive = openVideoWithAd,
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") },
                        onToggleWatchLater = viewModel::toggleWatchLater,
                        onOpenCreateChannel = { navController.navigate("create_channel") },
                        onOpenSubscriptionsFeed = { navController.navigate("library?tab=0") }
                    )
                }

                composable(BharatTab.EXPLORE.route) {
                    ExploreScreen(
                        videos = allVideos,
                        channels = allChannels,
                        currentCountry = prefs.preferredCountry,
                        currentLanguage = prefs.preferredLanguage,
                        onSelectCountry = viewModel::setCountry,
                        onSelectLanguage = viewModel::setLanguage,
                        onOpenVideo = openVideoWithAd,
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") },
                        onToggleSubscribe = viewModel::toggleSubscribe,
                        onToggleWatchLater = viewModel::toggleWatchLater
                    )
                }

                composable(BharatTab.UPLOAD.route) {
                    UploadScreen(
                        uploads = uploads,
                        defaultLanguage = prefs.preferredLanguage,
                        defaultCountry = prefs.preferredCountry,
                        onStartUpload = viewModel::startUpload,
                        onPauseUpload = viewModel::pauseUpload,
                        onResumeUpload = viewModel::resumeUpload,
                        onCancelUpload = viewModel::cancelUpload
                    )
                }

                composable(BharatTab.SHORTS.route) {
                    ShortsScreen(
                        shorts = shortsFeed,
                        initialIndex = 0,
                        channels = allChannels,
                        settings = settings,
                        observeComments = viewModel::observeComments,
                        onRecordPlay = viewModel::recordVideoPlay,
                        onToggleLike = viewModel::toggleLike,
                        onToggleSubscribe = viewModel::toggleSubscribe,
                        onAddComment = viewModel::addComment,
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") }
                    )
                }

                composable(
                    route = "shorts?index={index}",
                    arguments = listOf(navArgument("index") { type = NavType.IntType; defaultValue = 0 })
                ) { entry ->
                    val idx = entry.arguments?.getInt("index") ?: 0
                    ShortsScreen(
                        shorts = shortsFeed,
                        initialIndex = idx,
                        channels = allChannels,
                        settings = settings,
                        observeComments = viewModel::observeComments,
                        onRecordPlay = viewModel::recordVideoPlay,
                        onToggleLike = viewModel::toggleLike,
                        onToggleSubscribe = viewModel::toggleSubscribe,
                        onAddComment = viewModel::addComment,
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") }
                    )
                }

                composable(BharatTab.PROFILE.route) {
                    ProfileScreen(
                        user = currentUser,
                        allUsers = allUsers,
                        myChannels = myChannels,
                        prefs = prefs,
                        onCreateChannel = { navController.navigate("create_channel") },
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") },
                        onOpenCreatorStudio = { navController.navigate("studio") },
                        onOpenLibrary = { navController.navigate("library?tab=1") },
                        onOpenAdvertiser = { navController.navigate("advertiser") },
                        onOpenSuperAdmin = { navController.navigate("admin") },
                        onOpenChat = { navController.navigate("chat") },
                        onOpenSupport = { navController.navigate("support") },
                        onUpdateProfile = viewModel::updateUserProfile,
                        onCreateLocalAccount = viewModel::createLocalAccount,
                        onSwitchUser = viewModel::switchUser,
                        onToggleRestrictedMode = viewModel::toggleRestrictedMode,
                        onSelectLanguage = viewModel::setLanguage,
                        onSelectCountry = viewModel::setCountry
                    )
                }

                composable(
                    route = "player/{videoId}",
                    arguments = listOf(navArgument("videoId") { type = NavType.StringType })
                ) { entry ->
                    val videoId = entry.arguments?.getString("videoId") ?: ""
                    VideoPlayerScreen(
                        videoId = videoId,
                        observeVideo = viewModel::observeVideo,
                        observeComments = viewModel::observeComments,
                        allVideos = allVideos,
                        channels = allChannels,
                        playlists = playlists,
                        settings = settings,
                        userLanguage = prefs.preferredLanguage,
                        onBack = { navController.popBackStack() },
                        onRecordPlay = viewModel::recordVideoPlay,
                        onSavePosition = viewModel::savePlaybackPosition,
                        onSelectQuality = viewModel::updatePlaybackQuality,
                        onToggleLike = viewModel::toggleLike,
                        onToggleWatchLater = viewModel::toggleWatchLater,
                        onToggleSubscribe = viewModel::toggleSubscribe,
                        onToggleBell = viewModel::toggleChannelBell,
                        onAddComment = viewModel::addComment,
                        onLikeComment = viewModel::likeComment,
                        onAddToPlaylist = viewModel::addVideoToPlaylist,
                        onOpenVideo = openVideoWithAd,
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") },
                        onSubmitReport = viewModel::submitReport,
                        onEnterPip = onEnterPip
                    )
                }

                composable(
                    route = "live/{videoId}",
                    arguments = listOf(navArgument("videoId") { type = NavType.StringType })
                ) { entry ->
                    val videoId = entry.arguments?.getString("videoId") ?: ""
                    LiveStreamScreen(
                        videoId = videoId,
                        observeVideo = viewModel::observeVideo,
                        user = currentUser,
                        messages = chatMessages,
                        onBack = { navController.popBackStack() },
                        onSendLiveChat = { rId, rTitle, isGrp, txt -> viewModel.sendChatMessage(rId, rTitle, isGrp, txt) },
                        onReportStream = { v -> viewModel.submitReport("LIVE_STREAM", v.id, v.title, "Stream Report", "Reported from Live player") }
                    )
                }

                composable(
                    route = "channel/{channelId}",
                    arguments = listOf(navArgument("channelId") { type = NavType.StringType })
                ) { entry ->
                    val chId = entry.arguments?.getString("channelId") ?: ""
                    ChannelScreen(
                        channelId = chId,
                        observeChannel = viewModel::observeChannel,
                        allVideos = allVideos,
                        communityPosts = communityPosts,
                        currentUserId = currentUser?.uid ?: "",
                        onBack = { navController.popBackStack() },
                        onToggleSubscribe = viewModel::toggleSubscribe,
                        onToggleBell = viewModel::toggleChannelBell,
                        onOpenVideo = openVideoWithAd,
                        onToggleWatchLater = viewModel::toggleWatchLater,
                        onCreatePost = viewModel::createCommunityPost,
                        onUpdateChannel = viewModel::updateChannel
                    )
                }

                composable("create_channel") {
                    CreateChannelScreen(
                        onBack = { navController.popBackStack() },
                        onCreateChannel = { name, handle, desc, cat, links, avatar, banner ->
                            viewModel.createChannel(name, handle, desc, cat, links, avatar, banner) {
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable("studio") {
                    CreatorStudioScreen(
                        user = currentUser,
                        myChannels = myChannels,
                        allChannels = allChannels,
                        allVideos = allVideos,
                        payouts = payouts,
                        settings = settings,
                        countryCode = prefs.preferredCountry,
                        onBack = { navController.popBackStack() },
                        onInitiatePayout = viewModel::initiatePayout
                    )
                }

                composable("admin") {
                    SuperAdminScreen(
                        currentUser = currentUser,
                        settings = settings,
                        users = allUsers,
                        channels = allChannels,
                        videos = allVideos,
                        payouts = payouts,
                        reports = reports,
                        auditLogs = auditLogs,
                        onBack = { navController.popBackStack() },
                        onUpdateSettings = viewModel::updatePlatformSettings,
                        onModerateUser = viewModel::moderateUser,
                        onResolveReport = viewModel::resolveReport
                    )
                }

                composable("advertiser") {
                    AdvertiserScreen(
                        campaigns = adCampaigns,
                        settings = settings,
                        isAdmin = currentUser?.isAdmin == true,
                        onBack = { navController.popBackStack() },
                        onCreateCampaign = viewModel::createAdCampaign,
                        onReviewCampaign = viewModel::reviewAdCampaign
                    )
                }

                composable("search") {
                    SearchScreen(
                        videos = allVideos,
                        channels = allChannels,
                        history = searchHistory,
                        onBack = { navController.popBackStack() },
                        onRecordSearch = viewModel::recordSearch,
                        onClearHistory = viewModel::clearSearchHistory,
                        onOpenVideo = openVideoWithAd,
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") },
                        onToggleWatchLater = viewModel::toggleWatchLater,
                        onVoiceUnavailable = { viewModel.showMessage("Voice search requires Google Speech Services on this device.") }
                    )
                }

                composable("notifications") {
                    NotificationsScreen(
                        notifications = notifications,
                        onBack = { navController.popBackStack() },
                        onMarkAllRead = viewModel::markNotificationsRead
                    )
                }

                composable("chat") {
                    ChatScreen(
                        user = currentUser,
                        messages = chatMessages,
                        onBack = { navController.popBackStack() },
                        onSendMessage = viewModel::sendChatMessage,
                        onReportChat = { rId, rTitle ->
                            viewModel.submitReport("CHAT", rId, rTitle, "Chat Safety", "Reported conversation for moderation")
                        }
                    )
                }

                composable(
                    route = "library?tab={tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
                ) { entry ->
                    val initialTab = entry.arguments?.getInt("tab") ?: 0
                    LibraryScreen(
                        videos = allVideos,
                        channels = allChannels,
                        watchHistory = watchHistory,
                        playlists = playlists,
                        playlistRefs = playlistRefs,
                        historyPaused = prefs.historyPaused,
                        initialTab = initialTab,
                        onBack = { navController.popBackStack() },
                        onToggleHistoryPaused = viewModel::toggleHistoryPaused,
                        onClearHistory = viewModel::clearWatchHistory,
                        onCreatePlaylist = viewModel::createPlaylist,
                        onDeletePlaylist = viewModel::deletePlaylist,
                        onOpenVideo = openVideoWithAd,
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") },
                        onToggleWatchLater = viewModel::toggleWatchLater
                    )
                }

                composable("support") {
                    SupportScreen(
                        tickets = supportTickets,
                        onBack = { navController.popBackStack() },
                        onSubmitTicket = viewModel::submitSupportTicket
                    )
                }
            }
        }
    }
}
