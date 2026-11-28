package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun BharatTubeVideoPlayer(
    videoUri: String,
    initialPositionMs: Long,
    captionsText: String,
    qualityLabel: String,
    isShortVertical: Boolean = false,
    autoPlayNext: Boolean = false,
    isAdPlaying: Boolean = false,
    onProgressUpdate: (positionMs: Long, durationMs: Long) -> Unit = { _, _ -> },
    onSavePosition: (Long) -> Unit,
    onVideoEnded: () -> Unit = {},
    onSelectQuality: (String) -> Unit = {},
    onEnterPip: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCaptions by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var speedMenuExpanded by remember { mutableStateOf(false) }
    var qualityMenuExpanded by remember { mutableStateOf(false) }

    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = if (isShortVertical) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            if (initialPositionMs > 0L && !isShortVertical) {
                seekTo(initialPositionMs)
            }
            prepare()
            playWhenReady = !isAdPlaying
        }
    }

    LaunchedEffect(exoPlayer, isAdPlaying) {
        if (isAdPlaying) {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
        } else {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        }
    }

    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    LaunchedEffect(exoPlayer, isAdPlaying) {
        var tick = 0
        while (true) {
            delay(500L)
            if (!isAdPlaying) {
                val rawDur = exoPlayer.duration
                val safeDur = if (rawDur != C.TIME_UNSET && rawDur > 0L) rawDur else 0L
                onProgressUpdate(exoPlayer.currentPosition, safeDur)
                tick++
                if (tick % 5 == 0 && exoPlayer.isPlaying) {
                    onSavePosition(exoPlayer.currentPosition)
                }
            }
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED && autoPlayNext && !isShortVertical) {
                    onVideoEnded()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            onSavePosition(exoPlayer.currentPosition)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = !isShortVertical && !isAdPlaying
                    resizeMode = if (isShortVertical) {
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    } else {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                if (view.player !== exoPlayer) {
                    view.player = exoPlayer
                }
                view.useController = !isShortVertical && !isAdPlaying
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isShortVertical && !isAdPlaying) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        onClick = { speedMenuExpanded = true },
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("player_speed_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Speed, contentDescription = "Playback speed", tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("${playbackSpeed}x", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    DropdownMenu(expanded = speedMenuExpanded, onDismissRequest = { speedMenuExpanded = false }) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = {
                                    playbackSpeed = speed
                                    speedMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Box {
                    Surface(
                        onClick = { qualityMenuExpanded = true },
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("player_quality_button")
                    ) {
                        Text(
                            text = qualityLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    DropdownMenu(expanded = qualityMenuExpanded, onDismissRequest = { qualityMenuExpanded = false }) {
                        listOf("4K", "1080p", "720p", "480p", "360p", "240p", "144p", "Auto").forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q) },
                                onClick = {
                                    onSelectQuality(q)
                                    qualityMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showCaptions = !showCaptions },
                    modifier = Modifier.testTag("player_captions_button")
                ) {
                    Icon(
                        Icons.Filled.ClosedCaption,
                        contentDescription = "Toggle captions",
                        tint = if (showCaptions) Color(0xFFFF6B2C) else Color.White
                    )
                }

                IconButton(
                    onClick = onEnterPip,
                    modifier = Modifier.testTag("player_pip_button")
                ) {
                    Icon(Icons.Filled.PictureInPictureAlt, contentDescription = "Picture in picture", tint = Color.White)
                }

                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier.testTag("player_fullscreen_button")
                ) {
                    Icon(Icons.Filled.Fullscreen, contentDescription = "Toggle fullscreen", tint = Color.White)
                }
            }
        }

        if (showCaptions && captionsText.isNotBlank()) {
            Surface(
                color = Color.Black.copy(alpha = 0.78f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 48.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Text(
                    text = captionsText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
