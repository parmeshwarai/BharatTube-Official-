package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

enum class CropTarget(
    val title: String,
    val aspectRatio: Float,
    val filePrefix: String,
    val outputMaxWidth: Int,
    val outputMaxHeight: Int
) {
    LOGO_CIRCLE(
        title = "Crop Profile Logo",
        aspectRatio = 1f,
        filePrefix = "logo",
        outputMaxWidth = 512,
        outputMaxHeight = 512
    ),
    BANNER_WIDE(
        title = "Crop Channel Banner",
        aspectRatio = 16f / 5f,
        filePrefix = "banner",
        outputMaxWidth = 1280,
        outputMaxHeight = 400
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropDialog(
    sourceUri: Uri,
    cropTarget: CropTarget,
    onDismiss: () -> Unit,
    onCropped: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loadedBitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
    var loadError by remember(sourceUri) { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(sourceUri) {
        loadedBitmap = null
        loadError = null
        val bmp = withContext(Dispatchers.IO) {
            decodeSampledBitmap(context, sourceUri, maxDimension = 1600)
        }
        if (bmp == null) {
            loadError = "Unable to open selected image."
        } else {
            loadedBitmap = bmp
        }
    }

    fun clampOffsets(rawX: Float, rawY: Float, targetZoom: Float, bmp: Bitmap, box: IntSize): Pair<Float, Float> {
        if (box.width <= 0 || box.height <= 0) return 0f to 0f
        val baseScale = max(box.width.toFloat() / bmp.width, box.height.toFloat() / bmp.height)
        val totalScale = baseScale * targetZoom
        val maxX = ((bmp.width * totalScale - box.width) / 2f).coerceAtLeast(0f)
        val maxY = ((bmp.height * totalScale - box.height) / 2f).coerceAtLeast(0f)
        return rawX.coerceIn(-maxX, maxX) to rawY.coerceIn(-maxY, maxY)
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E101A))
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = cropTarget.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (!isSaving) onDismiss() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel crop", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val bmp = loadedBitmap ?: return@Button
                            if (viewportSize.width <= 0 || viewportSize.height <= 0 || isSaving) return@Button
                            isSaving = true
                            scope.launch {
                                val savedUri = withContext(Dispatchers.IO) {
                                    cropAndSaveBitmap(
                                        context = context,
                                        bitmap = bmp,
                                        cropTarget = cropTarget,
                                        viewportSize = viewportSize,
                                        zoom = zoom,
                                        offsetX = offsetX,
                                        offsetY = offsetY
                                    )
                                }
                                isSaving = false
                                if (savedUri != null) {
                                    onCropped(savedUri)
                                } else {
                                    loadError = "Could not save cropped image."
                                }
                            }
                        },
                        enabled = loadedBitmap != null && !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("crop_confirm_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text(if (isSaving) "Saving..." else "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141827))
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    loadError != null -> {
                        Text(loadError ?: "Error", color = Color.White)
                    }
                    loadedBitmap == null -> {
                        CircularProgressIndicator(color = SaffronFlame)
                    }
                    else -> {
                        val bmp = loadedBitmap!!
                        val imageBitmap = remember(bmp) { bmp.asImageBitmap() }
                        val baseScale = if (viewportSize.width > 0 && viewportSize.height > 0) {
                            max(viewportSize.width.toFloat() / bmp.width, viewportSize.height.toFloat() / bmp.height)
                        } else {
                            1f
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(cropTarget.aspectRatio)
                                .clipToBounds()
                                .background(Color.Black)
                                .onSizeChanged { newSize ->
                                    viewportSize = newSize
                                    val clamped = clampOffsets(offsetX, offsetY, zoom, bmp, newSize)
                                    offsetX = clamped.first
                                    offsetY = clamped.second
                                }
                                .pointerInput(bmp, viewportSize) {
                                    detectTransformGestures { _, pan, gestureZoom, _ ->
                                        val nextZoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                                        val nextOffsets = clampOffsets(
                                            offsetX + pan.x,
                                            offsetY + pan.y,
                                            nextZoom,
                                            bmp,
                                            viewportSize
                                        )
                                        zoom = nextZoom
                                        offsetX = nextOffsets.first
                                        offsetY = nextOffsets.second
                                    }
                                }
                                .testTag("crop_viewport")
                        ) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = cropTarget.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = zoom
                                        scaleY = zoom
                                        translationX = offsetX
                                        translationY = offsetY
                                    }
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (cropTarget == CropTarget.LOGO_CIRCLE) {
                                    val diameter = size.minDimension
                                    val radius = diameter / 2f
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val overlayPath = Path().apply {
                                        fillType = PathFillType.EvenOdd
                                        addRect(Rect(Offset.Zero, size))
                                        addOval(
                                            Rect(
                                                left = center.x - radius,
                                                top = center.y - radius,
                                                right = center.x + radius,
                                                bottom = center.y + radius
                                            )
                                        )
                                    }
                                    drawPath(overlayPath, color = Color.Black.copy(alpha = 0.62f))
                                    drawCircle(
                                        color = Color.White,
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                } else {
                                    // Rule-of-thirds guides for wide BharatTube channel banner
                                    val thirdW = size.width / 3f
                                    val thirdH = size.height / 3f
                                    val guideColor = Color.White.copy(alpha = 0.35f)
                                    drawLine(guideColor, Offset(thirdW, 0f), Offset(thirdW, size.height), 1.dp.toPx())
                                    drawLine(guideColor, Offset(thirdW * 2, 0f), Offset(thirdW * 2, size.height), 1.dp.toPx())
                                    drawLine(guideColor, Offset(0f, thirdH), Offset(size.width, thirdH), 1.dp.toPx())
                                    drawLine(guideColor, Offset(0f, thirdH * 2), Offset(size.width, thirdH * 2), 1.dp.toPx())
                                    drawRect(
                                        color = Color.White,
                                        topLeft = Offset.Zero,
                                        size = Size(size.width, size.height),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Zoom & Reset Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141827))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (cropTarget == CropTarget.LOGO_CIRCLE) {
                        "Drag or pinch to adjust your circular profile logo"
                    } else {
                        "Drag or pinch to frame your wide channel banner"
                    },
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom", tint = SaffronFlame)
                    Spacer(Modifier.width(8.dp))
                    Slider(
                        value = zoom,
                        onValueChange = { nextZoom ->
                            zoom = nextZoom
                            loadedBitmap?.let { bmp ->
                                val clamped = clampOffsets(offsetX, offsetY, nextZoom, bmp, viewportSize)
                                offsetX = clamped.first
                                offsetY = clamped.second
                            }
                        },
                        valueRange = 1f..4f,
                        colors = SliderDefaults.colors(
                            thumbColor = SaffronFlame,
                            activeTrackColor = SaffronFlame
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            zoom = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Reset", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun decodeSampledBitmap(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val w = info.size.width
                val h = info.size.height
                val largest = max(w, h)
                if (largest > maxDimension) {
                    val sample = (largest.toFloat() / maxDimension).roundToInt().coerceAtLeast(1)
                    decoder.setTargetSampleSize(sample)
                }
            }
        } else {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            var sampleSize = 1
            val largest = max(boundsOpts.outWidth, boundsOpts.outHeight)
            while (largest / sampleSize > maxDimension) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            }
        }
    } catch (_: Exception) {
        null
    }
}

private fun cropAndSaveBitmap(
    context: Context,
    bitmap: Bitmap,
    cropTarget: CropTarget,
    viewportSize: IntSize,
    zoom: Float,
    offsetX: Float,
    offsetY: Float
): String? {
    return try {
        val cropW = viewportSize.width.toFloat()
        val cropH = viewportSize.height.toFloat()
        val baseScale = max(cropW / bitmap.width, cropH / bitmap.height)
        val totalScale = (baseScale * zoom).coerceAtLeast(0.001f)

        val left = (bitmap.width / 2f + (-cropW / 2f - offsetX) / totalScale)
            .roundToInt()
            .coerceIn(0, (bitmap.width - 1).coerceAtLeast(0))
        val top = (bitmap.height / 2f + (-cropH / 2f - offsetY) / totalScale)
            .roundToInt()
            .coerceIn(0, (bitmap.height - 1).coerceAtLeast(0))
        val right = (bitmap.width / 2f + (cropW / 2f - offsetX) / totalScale)
            .roundToInt()
            .coerceIn((left + 1).coerceAtMost(bitmap.width), bitmap.width)
        val bottom = (bitmap.height / 2f + (cropH / 2f - offsetY) / totalScale)
            .roundToInt()
            .coerceIn((top + 1).coerceAtMost(bitmap.height), bitmap.height)

        val cropWidthPx = (right - left).coerceAtLeast(1)
        val cropHeightPx = (bottom - top).coerceAtLeast(1)
        val cropped = Bitmap.createBitmap(bitmap, left, top, cropWidthPx, cropHeightPx)

        val targetW = cropWidthPx.coerceAtMost(cropTarget.outputMaxWidth)
        val targetH = cropHeightPx.coerceAtMost(cropTarget.outputMaxHeight)
        val finalBitmap = if (targetW < cropWidthPx || targetH < cropHeightPx) {
            Bitmap.createScaledBitmap(cropped, targetW, targetH, true)
        } else {
            cropped
        }

        val mediaDir = File(context.filesDir, "profile_media").apply { mkdirs() }
        val outFile = File(mediaDir, "${cropTarget.filePrefix}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        if (finalBitmap !== cropped) finalBitmap.recycle()
        cropped.recycle()

        Uri.fromFile(outFile).toString()
    } catch (_: Exception) {
        null
    }
}
