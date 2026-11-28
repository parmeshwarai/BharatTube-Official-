package com.example

import android.app.PictureInPictureParams
import android.net.Uri
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.CreateOptionsBottomSheet
import com.example.ui.screens.AboutChannelScreen
import com.example.ui.screens.AdvertiserScreen
import com.example.ui.screens.CameraCaptureMode
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.ChannelScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ChhotuAdminCenterScreen
import com.example.ui.screens.ChhotuAiAssistantScreen
import com.example.ui.screens.CreateChannelScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.CreatorStudioScreen
import com.example.ui.screens.EditChannelScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LiveStreamScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileMenuScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.screens.SuperAdminScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.UploadDetailsScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.YourVideosScreen
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
    val chhotuChatHistory by viewModel.chhotuChatHistory.collectAsStateWithLifecycle()
    val chhotuErrorReports by viewModel.chhotuErrorReports.collectAsStateWithLifecycle()
    val systemHealthSnapshot by viewModel.systemHealthSnapshot.collectAsStateWithLifecycle()
    val chhotuBusy by viewModel.chhotuBusy.collectAsStateWithLifecycle()
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

    var showCreateSheet by remember { mutableStateOf(false) }

    val topLevelRoutes = setOf(
        BharatTab.HOME.route,
        BharatTab.SHORTS.route,
        BharatTab.SUBSCRIPTIONS.route,
        BharatTab.PROFILE.route
    )
    val isTopLevel = currentRoute in topLevelRoutes || currentRoute?.startsWith("shorts") == true
    val showTopBar = currentRoute in setOf(
        BharatTab.HOME.route,
        BharatTab.SUBSCRIPTIONS.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showTopBar) {
                BharatTubeTopBar(
                    unreadNotificationCount = notifications.count { !it.isRead },
                    onChhotuAiClick = { navController.navigate("chhotu_ai") },
                    onNotificationsClick = { navController.navigate("notifications") },
                    onSearchClick = { navController.navigate("search") }
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
                    },
                    onCreateClick = { showCreateSheet = true }
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
                        onOpenSubscriptionsFeed = { navController.navigate(BharatTab.SUBSCRIPTIONS.route) }
                    )
                }

                composable(BharatTab.SUBSCRIPTIONS.route) {
                    SubscriptionsScreen(
                        channels = allChannels,
                        videos = allVideos,
                        onOpenVideo = openVideoWithAd,
                        onOpenChannel = { chId -> navController.navigate("channel/$chId") },
                        onToggleSubscribe = viewModel::toggleSubscribe,
                        onToggleWatchLater = viewModel::toggleWatchLater
                    )
                }

                composable(
                    route = "camera?mode={mode}",
                    arguments = listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "VIDEO" })
                ) { entry ->
                    val modeName = entry.arguments?.getString("mode") ?: "VIDEO"
                    val initialMode = runCatching { CameraCaptureMode.valueOf(modeName) }
                        .getOrDefault(CameraCaptureMode.VIDEO)
                    CameraCaptureScreen(
                        initialMode = initialMode,
                        onBack = { navController.popBackStack() },
                        onMediaReady = { uriString, isShort ->
                            val encoded = Uri.encode(uriString)
                            navController.navigate("upload_details?uri=$encoded&isShort=$isShort")
                        },
                        onStartLive = { streamTitle ->
                            viewModel.startLiveBroadcast(streamTitle) { liveVideo ->
                                navController.navigate("live/${liveVideo.id}") {
                                    popUpTo(BharatTab.HOME.route) { inclusive = false }
                                }
                            }
                        }
                    )
                }

                composable(
                    route = "upload_details?uri={uri}&isShort={isShort}",
                    arguments = listOf(
                        navArgument("uri") { type = NavType.StringType; defaultValue = "" },
                        navArgument("isShort") { type = NavType.BoolType; defaultValue = false }
                    )
                ) { entry ->
                    val encodedUri = entry.arguments?.getString("uri") ?: ""
                    val decodedUri = Uri.decode(encodedUri)
                    val isShort = entry.arguments?.getBoolean("isShort") ?: false
                    UploadDetailsScreen(
                        videoUri = decodedUri,
                        initialIsShort = isShort,
                        playlists = playlists,
                        onBack = { navController.popBackStack() },
                        onCreatePlaylist = viewModel::createPlaylist,
                        onPublish = { title, desc, vidUri, thumbUri, format, vis, cat, tags, allowComments, audience, plId ->
                            viewModel.publishVideoNow(
                                title = title,
                                description = desc,
                                videoUri = vidUri,
                                thumbnailUri = thumbUri,
                                format = format,
                                visibility = vis,
                                category = cat,
                                hashtags = tags,
                                allowComments = allowComments,
                                audienceType = audience,
                                playlistId = plId
                            ) {
                                navController.navigate(BharatTab.HOME.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable("create_post") {
                    val activeChannel = myChannels.firstOrNull() ?: allChannels.firstOrNull()
                    CreatePostScreen(
                        activeChannel = activeChannel,
                        onBack = { navController.popBackStack() },
                        onPublishPost = viewModel::createCommunityPost
                    )
                }

                composable("explore") {
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

                composable("upload") {
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
                        allVideos = allVideos,
                        playlists = playlists,
                        playlistRefs = playlistRefs,
                        prefs = prefs,
                        onSelectChannel = viewModel::setActiveChannel,
                        onCreateChannel = { navController.navigate("create_channel") },
                        onEditChannel = { navController.navigate("edit_channel") },
                        onManageVideos = { navController.navigate("your_videos") },
                        onOpenAbout = { chId -> navController.navigate("about_channel/$chId") },
                        onOpenProfileMenu = { navController.navigate("profile_menu") },
                        onOpenSettings = { navController.navigate("settings") },
                        onOpenSearch = { navController.navigate("search") },
                        onOpenChhotuAi = { navController.navigate("chhotu_ai") },
                        onOpenVideo = openVideoWithAd,
                        onOpenPlaylistsLibrary = { navController.navigate("library?tab=3") },
                        onOpenUpload = { navController.navigate("camera?mode=VIDEO") },
                        onSwitchUser = viewModel::switchUser,
                        onCreateLocalAccount = viewModel::createLocalAccount,
                        onSignInBack = viewModel::signInBack
                    )
                }

                composable("profile_menu") {
                    val activeChannel = myChannels.firstOrNull { it.id == prefs.activeChannelId }
                        ?: myChannels.firstOrNull()
                    ProfileMenuScreen(
                        user = currentUser,
                        activeChannel = activeChannel,
                        allUsers = allUsers,
                        prefs = prefs,
                        onBack = { navController.popBackStack() },
                        onManageAccount = { navController.navigate("settings/account") },
                        onOpenYourChannel = {
                            navController.navigate(BharatTab.PROFILE.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onOpenHistory = { navController.navigate("library?tab=1") },
                        onOpenYourVideos = { navController.navigate("your_videos") },
                        onOpenDownloads = { navController.navigate("settings/downloads") },
                        onOpenWatchLater = { navController.navigate("library?tab=2") },
                        onOpenSettings = { navController.navigate("settings") },
                        onOpenHelpFeedback = { navController.navigate("support") },
                        onOpenCreatorStudio = { navController.navigate("studio") },
                        onOpenChhotuAi = { navController.navigate("chhotu_ai") },
                        onOpenChat = { navController.navigate("chat") },
                        onOpenAdvertiser = { navController.navigate("advertiser") },
                        onOpenSuperAdmin = { navController.navigate("admin") },
                        onOpenChhotuAdmin = { navController.navigate("chhotu_admin") },
                        onSwitchUser = viewModel::switchUser,
                        onCreateLocalAccount = viewModel::createLocalAccount,
                        onSignOut = viewModel::signOut
                    )
                }

                composable("edit_channel") {
                    val activeChannel = myChannels.firstOrNull { it.id == prefs.activeChannelId }
                        ?: myChannels.firstOrNull()
                    EditChannelScreen(
                        channel = activeChannel,
                        onBack = { navController.popBackStack() },
                        onSave = viewModel::saveChannelEdit
                    )
                }

                composable("your_videos") {
                    val activeChannel = myChannels.firstOrNull { it.id == prefs.activeChannelId }
                        ?: myChannels.firstOrNull()
                    YourVideosScreen(
                        channel = activeChannel,
                        myChannels = myChannels,
                        allVideos = allVideos,
                        onBack = { navController.popBackStack() },
                        onUploadClick = { navController.navigate("camera?mode=VIDEO") },
                        onOpenProfileMenu = { navController.navigate("profile_menu") },
                        onOpenVideo = openVideoWithAd,
                        onUpdateVideo = viewModel::updateVideoMetadata,
                        onToggleHideVideo = viewModel::toggleHideVideo,
                        onDeleteVideo = viewModel::deleteVideo
                    )
                }

                composable(
                    route = "about_channel/{channelId}",
                    arguments = listOf(navArgument("channelId") { type = NavType.StringType })
                ) { entry ->
                    val chId = entry.arguments?.getString("channelId") ?: ""
                    val targetChannel = allChannels.firstOrNull { it.id == chId }
                        ?: myChannels.firstOrNull()
                    val chVideos = allVideos.filter { it.channelId == targetChannel?.id }
                    AboutChannelScreen(
                        channel = targetChannel,
                        channelVideos = chVideos,
                        countryCode = prefs.preferredCountry,
                        onBack = { navController.popBackStack() },
                        onReportChannel = viewModel::submitReport
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenSection = { section -> navController.navigate("settings/$section") }
                    )
                }

                composable(
                    route = "settings/{section}",
                    arguments = listOf(navArgument("section") { type = NavType.StringType })
                ) { entry ->
                    val section = entry.arguments?.getString("section") ?: "general"
                    val activeChannel = myChannels.firstOrNull { it.id == prefs.activeChannelId }
                        ?: myChannels.firstOrNull()
                    SettingDetailScreen(
                        sectionId = section,
                        user = currentUser,
                        activeChannel = activeChannel,
                        allUsers = allUsers,
                        allVideos = allVideos,
                        prefs = prefs,
                        onBack = { navController.popBackStack() },
                        onToggleDarkTheme = viewModel::toggleDarkTheme,
                        onSelectLanguage = viewModel::setLanguage,
                        onSelectCountry = viewModel::setCountry,
                        onToggleRestrictedMode = viewModel::toggleRestrictedMode,
                        onUpdateProfile = viewModel::updateUserProfile,
                        onSwitchUser = viewModel::switchUser,
                        onCreateLocalAccount = viewModel::createLocalAccount,
                        onOpenEditChannel = { navController.navigate("edit_channel") },
                        onOpenCreatorStudio = { navController.navigate("studio") },
                        onOpenNotificationsCenter = { navController.navigate("notifications") },
                        onOpenHistoryLibrary = { navController.navigate("library?tab=1") },
                        onOpenSupport = { navController.navigate("support") },
                        onOpenChhotuAi = { navController.navigate("chhotu_ai") },
                        onOpenVideo = openVideoWithAd,
                        onSetPushOptIn = viewModel::setPushOptIn,
                        onSetNotifyUploads = viewModel::setNotifyUploads,
                        onSetNotifyComments = viewModel::setNotifyComments,
                        onSetNotifyLive = viewModel::setNotifyLive,
                        onSetPlaybackAutoPlay = viewModel::setPlaybackAutoPlayNext,
                        onSetPlaybackQuality = viewModel::setPlaybackDefaultQuality,
                        onSetPlaybackCaptions = viewModel::setPlaybackCaptionsEnabled,
                        onSetPlaybackDataSaver = viewModel::setPlaybackDataSaver,
                        onSetDownloadsWifiOnly = viewModel::setDownloadsWifiOnly,
                        onSetDownloadsQuality = viewModel::setDownloadsQuality,
                        onToggleDownloadVideo = viewModel::toggleDownloadVideo,
                        onClearDownloads = viewModel::clearDownloads,
                        onSetPrivacySubsPrivate = viewModel::setPrivacySubscriptionsPrivate,
                        onSetPrivacyPlaylistsPrivate = viewModel::setPrivacyPlaylistsPrivate,
                        onToggleHistoryPaused = viewModel::toggleHistoryPaused,
                        onClearWatchHistory = viewModel::clearWatchHistory,
                        onClearSearchHistory = viewModel::clearSearchHistory,
                        onSignOut = viewModel::signOut
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
                        onOpenChhotuAdmin = { navController.navigate("chhotu_admin") },
                        onUpdateSettings = viewModel::updatePlatformSettings,
                        onModerateUser = viewModel::moderateUser,
                        onResolveReport = viewModel::resolveReport
                    )
                }

                composable("chhotu_ai") {
                    ChhotuAiAssistantScreen(
                        user = currentUser,
                        settings = settings,
                        messages = chhotuChatHistory,
                        healthSnapshot = systemHealthSnapshot,
                        isBusy = chhotuBusy,
                        defaultLanguage = prefs.preferredLanguage,
                        onBack = { navController.popBackStack() },
                        onOpenAdminControlCenter = { navController.navigate("chhotu_admin") },
                        onSendMessage = viewModel::sendChhotuMessage,
                        onClearHistory = viewModel::clearChhotuHistory,
                        onShowSnackbar = viewModel::showMessage
                    )
                }

                composable("chhotu_admin") {
                    ChhotuAdminCenterScreen(
                        currentUser = currentUser,
                        settings = settings,
                        users = allUsers,
                        channels = allChannels,
                        videos = allVideos,
                        payouts = payouts,
                        reports = reports,
                        auditLogs = auditLogs,
                        chhotuErrorReports = chhotuErrorReports,
                        healthSnapshot = systemHealthSnapshot,
                        isChecking = chhotuBusy,
                        onBack = { navController.popBackStack() },
                        onRunSystemCheck = { viewModel.runChhotuHealthCheck(manualTrigger = true) },
                        onResolveErrorReport = viewModel::resolveChhotuErrorReport,
                        onExecuteConfirmedAction = viewModel::executeConfirmedAdminAction
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

    if (showCreateSheet) {
        CreateOptionsBottomSheet(
            onDismiss = { showCreateSheet = false },
            onUploadVideo = {
                showCreateSheet = false
                navController.navigate("camera?mode=VIDEO")
            },
            onCreateShort = {
                showCreateSheet = false
                navController.navigate("camera?mode=SHORT")
            },
            onGoLive = {
                showCreateSheet = false
                navController.navigate("camera?mode=LIVE")
            },
            onCreatePost = {
                showCreateSheet = false
                navController.navigate("create_post")
            }
        )
    }
}
