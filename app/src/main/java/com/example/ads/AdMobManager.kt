package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BharatTubeCatalog
import com.example.ui.theme.SaffronFlame
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AdMobIds {
    const val APP_ID = "ca-app-pub-8067096612618404~6306798058"
    const val BANNER_UNIT_ID = "ca-app-pub-8067096612618404/9416177124"
    const val INTERSTITIAL_UNIT_ID = "ca-app-pub-8067096612618404/7403870261"
}

class AdMobInterstitialController(private val appContext: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initializeAndPreload() {
        initScope.launch {
            runCatching {
                MobileAds.initialize(appContext) {
                    mainHandler.post { preloadInterstitial() }
                }
            }.onFailure { error ->
                Log.w(TAG, "MobileAds initialization skipped: ${error.message}")
            }
        }
    }

    fun preloadInterstitial() {
        if (interstitialAd != null || isLoading) return
        isLoading = true
        runCatching {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                appContext,
                AdMobIds.INTERSTITIAL_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoading = false
                        Log.d(TAG, "AdMob interstitial loaded.")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoading = false
                        Log.w(TAG, "AdMob interstitial failed to load: ${error.message}")
                    }
                }
            )
        }.onFailure { error ->
            isLoading = false
            Log.w(TAG, "AdMob interstitial load threw: ${error.message}")
        }
    }

    fun showThenProceed(activity: Activity?, onProceed: () -> Unit) {
        val currentAd = interstitialAd
        if (currentAd == null || activity == null || activity.isFinishing || activity.isDestroyed) {
            preloadInterstitial()
            onProceed()
            return
        }

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preloadInterstitial()
                onProceed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "AdMob interstitial failed to show: ${adError.message}")
                interstitialAd = null
                preloadInterstitial()
                onProceed()
            }

            override fun onAdShowedFullScreenContent() {
                interstitialAd = null
            }
        }
        runCatching {
            currentAd.show(activity)
        }.onFailure { error ->
            Log.w(TAG, "AdMob interstitial show failed: ${error.message}")
            interstitialAd = null
            onProceed()
        }
    }

    private companion object {
        const val TAG = "AdMobInterstitial"
    }
}

@Composable
fun AdMobBannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobIds.BANNER_UNIT_ID
) {
    val context = LocalContext.current
    val adView = remember(adUnitId) {
        runCatching {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w("AdMobBanner", "Banner failed to load: ${error.message}")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }.onFailure { error ->
            Log.w("AdMobBanner", "AdView unavailable: ${error.message}")
        }.getOrNull()
    }

    DisposableEffect(adView) {
        onDispose {
            runCatching { adView?.destroy() }
        }
    }

    if (adView != null) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = modifier
                .fillMaxWidth()
                .testTag("home_admob_banner")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { adView },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun LongVideoAdOverlay(
    activeAd: ActivePlayerAd,
    elapsedSeconds: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingTotal = (activeAd.totalSeconds - elapsedSeconds).coerceAtLeast(0)
    val progress = (elapsedSeconds.toFloat() / activeAd.totalSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)
    val context = LocalContext.current
    val adKey = when (activeAd) {
        is ActivePlayerAd.PreRoll -> "preroll"
        is ActivePlayerAd.MidRoll -> "midroll_${activeAd.cueSeconds}"
    }
    val adView = remember(adKey) {
        runCatching {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdMobIds.BANNER_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }.onFailure { error ->
            Log.w("LongVideoAdOverlay", "AdView unavailable: ${error.message}")
        }.getOrNull()
    }

    DisposableEffect(adView) {
        onDispose {
            runCatching { adView?.destroy() }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF20B0D16))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .testTag("player_ad_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top ad metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFFFC107),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "AD",
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    val adTitle = when (activeAd) {
                        is ActivePlayerAd.PreRoll -> "Pre-Roll Ad • ${remainingTotal}s"
                        is ActivePlayerAd.MidRoll ->
                            "Mid-Roll ${activeAd.breakIndex}/${activeAd.totalBreaks} (${BharatTubeCatalog.formatDuration(activeAd.cueSeconds)}) • ${remainingTotal}s"
                    }
                    Text(
                        text = adTitle,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = "Sponsored Ad",
                        tint = SaffronFlame,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "AdMob",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Center real AdMob AdView slot
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color(0xFF171B2C),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (adView != null) {
                            AndroidView(
                                factory = { adView },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "Sponsored Break • BharatTube",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                Text(
                    text = when (activeAd) {
                        is ActivePlayerAd.PreRoll -> "Supporting BharatTube Creators"
                        is ActivePlayerAd.MidRoll -> "20s Mid-Roll Break • Video resumes automatically"
                    },
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Bottom progress + Skip / Countdown pill
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (activeAd) {
                        is ActivePlayerAd.PreRoll -> {
                            val skipRemaining = (activeAd.skipAfterSeconds - elapsedSeconds).coerceAtLeast(0)
                            if (skipRemaining > 0) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("player_ad_skip_countdown")
                                ) {
                                    Text(
                                        text = "Skip in ${skipRemaining}s",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = onSkip,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("player_ad_skip_button")
                                ) {
                                    Text(
                                        text = "Skip Ad",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.SkipNext,
                                        contentDescription = "Skip Ad",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        is ActivePlayerAd.MidRoll -> {
                            Surface(
                                color = SaffronFlame.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("player_ad_midroll_countdown")
                            ) {
                                Text(
                                    text = "Ad • ${remainingTotal}s",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
                LinearProgressIndicator(
                    progress = { progress },
                    color = Color(0xFFFFC107),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }
        }
    }
}
