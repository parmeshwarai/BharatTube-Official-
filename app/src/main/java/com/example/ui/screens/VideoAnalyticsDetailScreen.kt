package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AnalyticsTimeframe
import com.example.data.model.BharatTubeAnalyticsEngine
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.CommentEntity
import com.example.data.model.LabelShare
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.BharatTubeThumbnail
import com.example.ui.components.ProfileFormatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCobalt
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoAnalyticsDetailScreen(
    videoId: String,
    observeVideo: (String) -> Flow<VideoEntity?>,
    observeComments: (String) -> Flow<List<CommentEntity>>,
    user: UserProfileEntity?,
    settings: PlatformSettingsEntity,
    countryCode: String,
    onBack: () -> Unit,
    onShareAnalytics: (VideoEntity) -> Unit
) {
    val context = LocalContext.current
    val video by observeVideo(videoId).collectAsStateWithLifecycle(initialValue = null)
    val comments by observeComments(videoId).collectAsStateWithLifecycle(initialValue = emptyList())
    var timeframe by remember { mutableStateOf(AnalyticsTimeframe.LAST_28_DAYS) }

    val snapshot = remember(video, comments.size, timeframe, settings, user?.boostPercent) {
        video?.let {
            BharatTubeAnalyticsEngine.buildVideoAnalytics(
                video = it,
                commentCount = comments.size,
                timeframe = timeframe,
                settings = settings,
                userBoostPct = user?.boostPercent ?: 0
            )
        }
    }

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("BharatTube", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Analytics", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA0A5B8))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("video_analytics_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    video?.let { v ->
                        IconButton(onClick = {
                            onShareAnalytics(v)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "BharatTube Video Analytics — ${v.title}: ${BharatTubeCatalog.formatCompactCount(snapshot?.views ?: v.viewCount)} views"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Video Analytics"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share Analytics", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        }
    ) { padding ->
        if (video == null || snapshot == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Video analytics unavailable", color = Color(0xFFA0A5B8))
            }
            return@Scaffold
        }

        val currentVideo = video!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Video Header Preview Card (Ref WA0099)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF151822),
                    border = BorderStroke(1.dp, Color(0xFF252938)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            BharatTubeThumbnail(
                                thumbnailKey = currentVideo.thumbnailKey,
                                contentDescription = currentVideo.title,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.78f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                            ) {
                                Text(
                                    text = BharatTubeCatalog.formatDuration(currentVideo.durationSeconds),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = currentVideo.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Uploaded ${ProfileFormatters.formatRelativeDate(currentVideo.createdAt)} • ${currentVideo.visibility.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA0A5B8)
                            )
                        }
                    }
                }
            }

            // Timeframe Filter Chips: Last 7 Days | 28 Days | 90 Days | All Time
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AnalyticsTimeframe.entries) { option ->
                        FilterChip(
                            selected = timeframe == option,
                            onClick = { timeframe = option },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronFlame,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1A1E2B),
                                labelColor = Color.White
                            ),
                            modifier = Modifier.testTag("video_analytics_filter_${option.name}")
                        )
                    }
                }
            }

            // 4 KPI Cards: Views, Likes, Comments, Shares
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AnalyticsMiniStatCard(
                        icon = Icons.Filled.Visibility,
                        value = BharatTubeCatalog.formatCompactCount(snapshot.views),
                        label = "Views",
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsMiniStatCard(
                        icon = Icons.Filled.ThumbUp,
                        value = BharatTubeCatalog.formatCompactCount(snapshot.likes),
                        label = "Likes",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AnalyticsMiniStatCard(
                        icon = Icons.Filled.ChatBubbleOutline,
                        value = BharatTubeCatalog.formatCompactCount(snapshot.comments),
                        label = "Comments",
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsMiniStatCard(
                        icon = Icons.Filled.Share,
                        value = BharatTubeCatalog.formatCompactCount(snapshot.shares),
                        label = "Shares",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Watch Time Graph Card
            item {
                AnalyticsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Watch Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(timeframe.label, style = MaterialTheme.typography.labelMedium, color = SaffronFlame, fontWeight = FontWeight.SemiBold)
                    }
                    AnalyticsLineGraph(
                        values = snapshot.watchTimeSeries,
                        lineColor = SaffronFlame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("video_watch_time_graph")
                    )
                    val hoursText = String.format("%,.1f", snapshot.watchTimeHours)
                    Text(
                        text = "Total watch time: $hoursText hours (${snapshot.watchTimeMinutes} min) • +${String.format("%.1f", snapshot.watchTimeChangePct)}% vs previous",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldVerified
                    )
                }
            }

            // Per-Video Earnings & Subscribers Gained / Lost Row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AnalyticsCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = SaffronFlame, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Per Video Earning", style = MaterialTheme.typography.labelLarge, color = Color(0xFFB5BACB))
                        }
                        Text(
                            text = BharatTubeCatalog.formatCurrency(snapshot.earningsInr, countryCode),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaffronFlame
                        )
                        Text(
                            text = "+${String.format("%.1f", snapshot.earningsChangePct)}% trend",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldVerified
                        )
                    }
                    AnalyticsCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Subscribers", style = MaterialTheme.typography.labelLarge, color = Color(0xFFB5BACB))
                        }
                        Text(
                            text = "${if (snapshot.netSubscribers >= 0) "+" else ""}${snapshot.netSubscribers} net",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldVerified
                        )
                        Text(
                            text = "+${snapshot.subscribersGained} gained • -${snapshot.subscribersLost} lost",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA0A5B8)
                        )
                    }
                }
            }

            // Audience Retention Graph Card
            item {
                AnalyticsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Audience Retention", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = "Avg. ${String.format("%.1f", snapshot.avgRetentionPct)}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = SaffronFlame,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier.height(100.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("100%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9AA1B6))
                            Text("60%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9AA1B6))
                            Text("20%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9AA1B6))
                        }
                        Spacer(Modifier.width(8.dp))
                        AnalyticsLineGraph(
                            values = snapshot.retentionSeries,
                            lineColor = AmberGold,
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .testTag("video_retention_graph")
                        )
                    }
                }
            }

            // Traffic Source Card (Home, Search, Suggested, Shorts Feed)
            item {
                AnalyticsCard {
                    Text("Traffic Source", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    snapshot.trafficSources.forEachIndexed { idx, source ->
                        val barColor = when (idx) {
                            0 -> SaffronFlame
                            1 -> ElectricCobalt
                            2 -> EmeraldVerified
                            else -> AmberGold
                        }
                        ShareProgressRow(item = source, barColor = barColor)
                    }
                }
            }

            // Audience Demographics: Age & Gender
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AnalyticsCard(modifier = Modifier.weight(1f)) {
                        Text("Age", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        snapshot.ageGroups.forEach { age ->
                            ShareProgressRow(item = age, barColor = SaffronFlame)
                        }
                    }
                    AnalyticsCard(modifier = Modifier.weight(1f)) {
                        Text("Gender", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        snapshot.genderSplit.forEachIndexed { idx, g ->
                            ShareProgressRow(
                                item = g,
                                barColor = when (idx) {
                                    0 -> SaffronFlame
                                    1 -> LotusCoral
                                    else -> ElectricCobalt
                                }
                            )
                        }
                    }
                }
            }

            // Top Cities & Top Countries
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AnalyticsCard(modifier = Modifier.weight(1f)) {
                        Text("Top Cities", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        snapshot.topCities.forEach { city ->
                            ShareProgressRow(item = city, barColor = EmeraldVerified)
                        }
                    }
                    AnalyticsCard(modifier = Modifier.weight(1f)) {
                        Text("Top Countries", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        snapshot.topCountries.forEach { country ->
                            ShareProgressRow(item = country, barColor = AmberGold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun AnalyticsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF151822),
        border = BorderStroke(1.dp, Color(0xFF252938)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun AnalyticsMiniStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF151822),
        border = BorderStroke(1.dp, Color(0xFF252938)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = SaffronFlame, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA0A5B8))
        }
    }
}

@Composable
internal fun AnalyticsLineGraph(
    values: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val padTop = 8.dp.toPx()
        val padBottom = 10.dp.toPx()
        val usableH = (h - padTop - padBottom).coerceAtLeast(10f)

        // Subtle grid lines
        for (i in 0..2) {
            val y = padTop + usableH * (i / 2f)
            drawLine(
                color = Color.White.copy(alpha = 0.07f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        val points = values.mapIndexed { index, v ->
            val x = (index.toFloat() / (values.size - 1)) * w
            val y = padTop + (1f - v.coerceIn(0f, 1f)) * usableH
            Offset(x, y)
        }

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2f
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, h - padBottom)
            lineTo(points.first().x, h - padBottom)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(lineColor.copy(alpha = 0.34f), Color.Transparent)
            )
        )
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun ShareProgressRow(item: LabelShare, barColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.label, style = MaterialTheme.typography.bodySmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (item.subtitle.isNotBlank()) "${item.percent}% (${item.subtitle})" else "${item.percent}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB5BACB),
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(
            progress = { (item.percent / 100f).coerceIn(0f, 1f) },
            color = barColor,
            trackColor = Color(0xFF232736),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}
