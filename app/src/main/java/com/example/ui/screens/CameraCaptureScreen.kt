package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.delay
import java.io.File

enum class CameraCaptureMode(val label: String) {
    VIDEO("Video"),
    SHORT("Short"),
    LIVE("Live")
}

enum class CameraFilter(val label: String, val overlayColor: Color) {
    NORMAL("Normal", Color.Transparent),
    BEAUTY("Beauty", Color(0x28FFB6C1)),
    WARM("Warm", Color(0x30FF8A3D)),
    COOL("Cool", Color(0x303D8BFF)),
    BLACK_WHITE("Black & White", Color(0x88333333)),
    VINTAGE("Vintage", Color(0x38C6924A))
}

enum class CameraTimer(val label: String, val seconds: Int) {
    OFF("Off", 0),
    THREE("3s", 3),
    TEN("10s", 10)
}

@Composable
fun CameraCaptureScreen(
    initialMode: CameraCaptureMode,
    onBack: () -> Unit,
    onMediaReady: (videoUri: String, isShort: Boolean) -> Unit,
    onStartLive: (title: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var captureMode by remember(initialMode) { mutableStateOf(initialMode) }
    var selectedFilter by remember { mutableStateOf(CameraFilter.NORMAL) }
    var selectedTimer by remember { mutableStateOf(CameraTimer.OFF) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var countdownRemaining by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingElapsedSec by remember { mutableIntStateOf(0) }
    var liveTitle by remember { mutableStateOf("") }
    var cameraErrorMessage by remember { mutableStateOf<String?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasCameraPermission = result[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasAudioPermission = result[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    val galleryVideoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onMediaReady(uri.toString(), captureMode == CameraCaptureMode.SHORT)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(hasCameraPermission, useFrontCamera) {
        if (!hasCameraPermission) return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                val selector = if (useFrontCamera && provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                if (!provider.hasCamera(selector)) {
                    cameraErrorMessage = "No physical camera detected. Pick a video from Gallery below."
                    return@addListener
                }
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(Quality.HD, FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))
                    )
                    .build()
                val capture = VideoCapture.withOutput(recorder)
                provider.unbindAll()
                val boundCamera = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                camera = boundCamera
                videoCapture = capture
                cameraErrorMessage = null
                boundCamera.cameraControl.enableTorch(flashEnabled && !useFrontCamera)
            } catch (e: Exception) {
                cameraErrorMessage = "Camera preview unavailable (${e.localizedMessage ?: "no sensor"}). Select a video from Gallery."
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(flashEnabled, useFrontCamera, camera) {
        runCatching {
            camera?.cameraControl?.enableTorch(flashEnabled && !useFrontCamera)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { activeRecording?.stop() }
        }
    }

    @SuppressLint("MissingPermission")
    fun beginActualRecording() {
        val vc = videoCapture
        if (vc == null || !hasCameraPermission) {
            galleryVideoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            return
        }
        val capturesDir = File(context.filesDir, "captures").apply { mkdirs() }
        val outputFile = File(capturesDir, "bt_cap_${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(outputFile).build()
        val pending = vc.output.prepareRecording(context, outputOptions).let { prep ->
            if (hasAudioPermission) prep.withAudioEnabled() else prep
        }
        activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    isRecording = true
                    recordingElapsedSec = 0
                }
                is VideoRecordEvent.Status -> {
                    recordingElapsedSec = (event.recordingStats.recordedDurationNanos / 1_000_000_000L).toInt()
                }
                is VideoRecordEvent.Finalize -> {
                    isRecording = false
                    activeRecording = null
                    if (!event.hasError() && outputFile.exists()) {
                        onMediaReady(Uri.fromFile(outputFile).toString(), captureMode == CameraCaptureMode.SHORT)
                    } else {
                        cameraErrorMessage = "Recording could not complete. Try picking a video from Gallery."
                    }
                }
            }
        }
    }

    LaunchedEffect(countdownRemaining) {
        if (countdownRemaining > 0) {
            delay(1000L)
            countdownRemaining -= 1
            if (countdownRemaining == 0) {
                beginActualRecording()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B10))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Top Header: Orange Lotus + BharatTube, Flip Camera, Flash, Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("camera_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            BharatTubeLogoMark(modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "BharatTube",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { useFrontCamera = !useFrontCamera },
                modifier = Modifier.testTag("camera_flip_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Cameraswitch,
                    contentDescription = "Flip Camera",
                    tint = if (useFrontCamera) SaffronFlame else Color.White
                )
            }
            IconButton(
                onClick = { flashEnabled = !flashEnabled },
                modifier = Modifier.testTag("camera_flash_button")
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = "Toggle Flash",
                    tint = if (flashEnabled) SaffronFlame else Color.White
                )
            }
            Box(contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                        val next = when (selectedTimer) {
                            CameraTimer.OFF -> CameraTimer.THREE
                            CameraTimer.THREE -> CameraTimer.TEN
                            CameraTimer.TEN -> CameraTimer.OFF
                        }
                        selectedTimer = next
                    },
                    modifier = Modifier.testTag("camera_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "Recording Timer: ${selectedTimer.label}",
                        tint = if (selectedTimer != CameraTimer.OFF) SaffronFlame else Color.White
                    )
                }
                if (selectedTimer != CameraTimer.OFF) {
                    Surface(
                        color = SaffronFlame,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = selectedTimer.label,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        // Camera Viewfinder with Live Filter Overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF151821))
        ) {
            if (hasCameraPermission && cameraErrorMessage == null) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (captureMode == CameraCaptureMode.LIVE) Icons.Filled.Sensors else Icons.Filled.Videocam,
                        contentDescription = null,
                        tint = SaffronFlame,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = cameraErrorMessage ?: "Allow Camera access to preview & record directly.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(14.dp))
                    if (!hasCameraPermission) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame)
                        ) {
                            Text("Allow Camera")
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            galleryVideoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        }
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Pick Video from Gallery", color = Color.White)
                    }
                }
            }

            // Live Visual Filter Tint Overlay
            if (selectedFilter != CameraFilter.NORMAL) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(selectedFilter.overlayColor)
                )
            }

            // Active Filter / Recording Pill
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isRecording) {
                    Surface(
                        color = LotusCoral,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "REC ${recordingElapsedSec / 60}:${String.format("%02d", recordingElapsedSec % 60)}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                if (selectedFilter != CameraFilter.NORMAL) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${selectedFilter.label} Filter",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Countdown Overlay
            if (countdownRemaining > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$countdownRemaining",
                        color = SaffronFlame,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Live Mode Broadcast Title Card
            if (captureMode == CameraCaptureMode.LIVE) {
                Surface(
                    color = Color(0xDD151822),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Go Live on BharatTube",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = liveTitle,
                            onValueChange = { liveTitle = it },
                            placeholder = { Text("Enter Live stream title...", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SaffronFlame,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("camera_live_title_input")
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Filter Chip Strip: Normal, Beauty, Warm, Cool, Black & White, Vintage
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CameraFilter.entries) { filter ->
                val selected = filter == selectedFilter
                Surface(
                    onClick = { selectedFilter = filter },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) SaffronFlame else Color(0xFF1D202A),
                    modifier = Modifier.testTag("camera_filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = filter.label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Controls Row: Gallery | Big Record / Go Live Button | Spacer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !isRecording) {
                        galleryVideoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    }
                    .testTag("camera_gallery_button"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1F222D))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "Pick from Gallery",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("Gallery", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }

            // Big Center Record / Go Live Button
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE94D2B))
                        .border(4.dp, Color.White, CircleShape)
                        .clickable {
                            when {
                                captureMode == CameraCaptureMode.LIVE -> {
                                    onStartLive(liveTitle.ifBlank { "BharatTube Live Session" })
                                }
                                isRecording -> {
                                    activeRecording?.stop()
                                }
                                countdownRemaining > 0 -> {
                                    countdownRemaining = 0
                                }
                                selectedTimer.seconds > 0 && videoCapture != null && hasCameraPermission -> {
                                    countdownRemaining = selectedTimer.seconds
                                }
                                else -> {
                                    beginActualRecording()
                                }
                            }
                        }
                        .testTag("camera_record_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                        )
                    } else if (captureMode == CameraCaptureMode.LIVE) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        // Bottom Tabs: Video | Short | Live
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E1017))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CameraCaptureMode.entries.forEach { mode ->
                val selected = captureMode == mode
                val icon = when (mode) {
                    CameraCaptureMode.VIDEO -> Icons.Filled.Videocam
                    CameraCaptureMode.SHORT -> Icons.Filled.ShortText
                    CameraCaptureMode.LIVE -> Icons.Filled.Sensors
                }
                val tint = if (selected) SaffronFlame else Color(0xFF8C92A4)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !isRecording) { captureMode = mode }
                        .padding(vertical = 6.dp)
                        .testTag("camera_mode_${mode.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, contentDescription = mode.label, tint = tint, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = mode.label,
                        color = tint,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(2.5.dp)
                            .background(if (selected) SaffronFlame else Color.Transparent, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
