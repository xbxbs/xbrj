package com.example.xbjsb.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.ui.theme.*
import com.example.xbjsb.viewmodel.DiaryViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.*

// ============================================
// Data Models
// ============================================

data class MoodDataPoint(
    val date: LocalDate,
    val mood: String,
    val title: String
)

// ============================================
// Mood Stats Screen
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiaryViewModel = viewModel()
) {
    val entries by viewModel.entries.collectAsState()
    var selectedRange by remember { mutableIntStateOf(30) }
    var animationPlayed by remember { mutableStateOf(false) }
    
    // Trigger animation on first composition
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    val now = LocalDate.now()
    val weekStart = now.minusDays(now.dayOfWeek.value.toLong() - 1)
    
    // 本周心情统计
    val weekEntries = remember(entries) {
        entries.filter {
            val date = LocalDate.ofEpochDay(it.timestamp / (24 * 60 * 60 * 1000))
            !date.isBefore(weekStart) && !date.isAfter(now)
        }
    }
    
    // 本月心情统计
    val monthEntries = remember(entries) {
        entries.filter {
            val date = LocalDate.ofEpochDay(it.timestamp / (24 * 60 * 60 * 1000))
            date.year == now.year && date.monthValue == now.monthValue
        }
    }
    
    // 趋势数据
    val trendData = remember(entries, selectedRange) {
        val startDate = now.minusDays(selectedRange.toLong())
        entries
            .filter { entry ->
                val date = Instant.ofEpochMilli(entry.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                !date.isBefore(startDate) && !date.isAfter(now)
            }
            .groupBy { entry ->
                Instant.ofEpochMilli(entry.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .map { (date, dayEntries) ->
                val latest = dayEntries.maxByOrNull { it.timestamp }!!
                MoodDataPoint(date, latest.mood, latest.title)
            }
            .sortedBy { it.date }
    }
    
    val weekMoodCounts = weekEntries.groupingBy { it.mood }.eachCount()
    val monthMoodCounts = monthEntries.groupingBy { it.mood }.eachCount()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "心情统计",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.M),
            verticalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            // 时间范围选择器
            AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                TimeRangeSelector(
                    selectedRange = selectedRange,
                    onRangeSelected = { selectedRange = it }
                )
            }
            
            // 趋势折线图
            AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 100)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(500, delayMillis = 100)
                        )
            ) {
                MoodTrendChart(
                    data = trendData,
                    selectedRange = selectedRange
                )
            }
            
            // 本周心情
            AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 200)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(500, delayMillis = 200)
                        )
            ) {
                MoodStatsCard(
                    title = "本周心情",
                    subtitle = "${weekStart.monthValue}月${weekStart.dayOfMonth}日 - ${now.monthValue}月${now.dayOfMonth}日",
                    moodCounts = weekMoodCounts,
                    totalCount = weekEntries.size
                )
            }
            
            // 本月心情
            AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 300)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(500, delayMillis = 300)
                        )
            ) {
                MoodStatsCard(
                    title = "本月心情",
                    subtitle = "${now.year}年${now.monthValue}月",
                    moodCounts = monthMoodCounts,
                    totalCount = monthEntries.size
                )
            }
            
            // 总体统计
            AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 400)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(500, delayMillis = 400)
                        )
            ) {
                OverallStatsCard(entries = entries)
            }
        }
    }
}

@Composable
fun MoodStatsCard(
    title: String,
    subtitle: String,
    moodCounts: Map<String, Int>,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(Modifier.height(20.dp))
            
            if (totalCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                val moodData = listOf(
                    "happy" to "😊 开心",
                    "calm" to "😌 平静",
                    "excited" to "😆 兴奋",
                    "sad" to "😔 难过",
                    "neutral" to "😐 一般"
                )
                
                moodData.forEach { (mood, label) ->
                    val count = moodCounts[mood] ?: 0
                    if (count > 0) {
                        val percentage = (count.toFloat() / totalCount * 100).toInt()
                        
                        MoodStatItem(
                            label = label,
                            count = count,
                            percentage = percentage,
                            totalCount = totalCount,
                            mood = mood
                        )
                        
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MoodStatItem(
    label: String,
    count: Int,
    percentage: Int,
    totalCount: Int,
    mood: String
) {
    // 使用统一的颜色定义
    val moodColor = when (mood) {
        "happy" -> MoodColors.happy
        "calm" -> MoodColors.calm
        "excited" -> MoodColors.excited
        "sad" -> MoodColors.sad
        else -> MoodColors.neutral
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "$count 次 ($percentage%)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(count.toFloat() / totalCount)
                    .clip(RoundedCornerShape(6.dp))
                    .background(moodColor)
            )
        }
    }
}

@Composable
fun OverallStatsCard(entries: List<com.example.xbjsb.data.DiaryEntry>) {
    val totalCount = entries.size
    val favoriteCount = entries.count { it.isFavorite }
    val moodCounts = entries.groupingBy { it.mood }.eachCount()
    val mostCommonMood = moodCounts.maxByOrNull { it.value }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "总体统计",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            
            Spacer(Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "总日记", value = totalCount.toString())
                StatItem(label = "收藏数", value = favoriteCount.toString())
                StatItem(
                    label = "最常心情",
                    value = when (mostCommonMood?.key) {
                        "happy" -> "😊"
                        "calm" -> "😌"
                        "excited" -> "😆"
                        "sad" -> "😔"
                        "neutral" -> "😐"
                        else -> "-"
                    }
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

// ============================================
// Time Range Selector
// ============================================

@Composable
fun TimeRangeSelector(
    selectedRange: Int,
    onRangeSelected: (Int) -> Unit
) {
    val ranges = listOf(7 to "7天", 14 to "14天", 30 to "30天")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.S)
    ) {
        ranges.forEach { (days, label) ->
            val isSelected = selectedRange == days
            
            FilterChip(
                selected = isSelected,
                onClick = { onRangeSelected(days) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// ============================================
// Mood Trend Chart (Canvas)
// ============================================

@Composable
fun MoodTrendChart(
    data: List<MoodDataPoint>,
    selectedRange: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Animation
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label = "chart_animation"
    )
    
    // Selected point for tooltip
    var selectedPoint by remember { mutableStateOf<MoodDataPoint?>(null) }
    var selectedOffset by remember { mutableStateOf<Offset?>(null) }
    
    // Mood value mapping
    fun moodToValue(mood: String): Float = when (mood) {
        "sad" -> 1f
        "neutral" -> 2f
        "calm" -> 3f
        "happy" -> 4f
        "excited" -> 5f
        else -> 2f
    }
    
    fun moodToEmoji(mood: String): String = when (mood) {
        "happy" -> "😊"
        "calm" -> "😌"
        "excited" -> "😆"
        "sad" -> "😔"
        "neutral" -> "😐"
        else -> "❓"
    }
    
    fun moodToColor(mood: String): Color = when (mood) {
        "happy" -> MoodColors.happy
        "calm" -> MoodColors.calm
        "excited" -> MoodColors.excited
        "sad" -> MoodColors.sad
        "neutral" -> MoodColors.neutral
        else -> outlineVariant
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceVariant.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.L)
        ) {
            Text(
                text = "情绪趋势",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            
            Spacer(Modifier.height(Spacing.XS))
            
            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .pointerInput(data) {
                            detectTapGestures(
                                onLongPress = { offset ->
                                    // Find nearest point
                                    val chartWidth = size.width.toFloat()
                                    val chartHeight = size.height.toFloat()
                                    val padding = 40f
                                    val usableWidth = chartWidth - padding * 2
                                    val usableHeight = chartHeight - padding * 2
                                    
                                    val stepX = if (data.size > 1) usableWidth / (data.size - 1) else 0f
                                    
                                    val nearest = data.minByOrNull { point ->
                                        val index = data.indexOf(point)
                                        val x = padding + index * stepX
                                        val y = padding + usableHeight * (1 - (moodToValue(point.mood) - 1f) / 4f)
                                        val dx = offset.x - x
                                        val dy = offset.y - y
                                        sqrt(dx * dx + dy * dy)
                                    }
                                    
                                    if (nearest != null) {
                                        selectedPoint = nearest
                                        val index = data.indexOf(nearest)
                                        val x = padding + index * stepX
                                        val y = padding + usableHeight * (1 - (moodToValue(nearest.mood) - 1f) / 4f)
                                        selectedOffset = Offset(x, y)
                                    }
                                },
                                onTap = {
                                    selectedPoint = null
                                    selectedOffset = null
                                }
                            )
                        }
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val padding = 40f
                        val chartWidth = canvasWidth - padding * 2
                        val chartHeight = canvasHeight - padding * 2
                        
                        // Draw grid lines
                        val gridLevels = listOf(1f, 2f, 3f, 4f, 5f)
                        val gridLabels = listOf("难过", "一般", "平静", "开心", "兴奋")
                        
                        gridLevels.forEachIndexed { index, level ->
                            val y = padding + chartHeight * (1 - (level - 1f) / 4f)
                            
                            // Dashed line
                            drawLine(
                                color = outlineVariant.copy(alpha = 0.3f),
                                start = Offset(padding, y),
                                end = Offset(canvasWidth - padding, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                            )
                            
                            // Y-axis label
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = onSurfaceVariant.copy(alpha = 0.6f).toArgb()
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.RIGHT
                                    isAntiAlias = true
                                }
                                drawText(gridLabels[index], padding - 8f, y + 4f, paint)
                            }
                        }
                        
                        // Draw X-axis labels
                        val stepX = if (data.size > 1) chartWidth / (data.size - 1) else 0f
                        val labelStep = when {
                            selectedRange <= 7 -> 1
                            selectedRange <= 14 -> 2
                            else -> 3
                        }
                        
                        data.forEachIndexed { index, point ->
                            if (index % labelStep == 0 || index == data.size - 1) {
                                val x = padding + index * stepX
                                drawContext.canvas.nativeCanvas.apply {
                                    val paint = android.graphics.Paint().apply {
                                        color = onSurfaceVariant.copy(alpha = 0.6f).toArgb()
                                        textSize = 9.sp.toPx()
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isAntiAlias = true
                                    }
                                    val label = "${point.date.monthValue}/${point.date.dayOfMonth}"
                                    drawText(label, x, canvasHeight - 4f, paint)
                                }
                            }
                        }
                        
                        // Draw gradient fill
                        if (data.size >= 2) {
                            val fillPath = Path().apply {
                                moveTo(padding, padding + chartHeight)
                                
                                data.forEachIndexed { index, point ->
                                    val x = padding + index * stepX
                                    val y = padding + chartHeight * (1 - (moodToValue(point.mood) - 1f) / 4f)
                                    
                                    if (index == 0) {
                                        lineTo(x, y)
                                    } else {
                                        // Smooth bezier
                                        val prevX = padding + (index - 1) * stepX
                                        val prevY = padding + chartHeight * (1 - (moodToValue(data[index - 1].mood) - 1f) / 4f)
                                        val controlX1 = prevX + stepX * 0.4f
                                        val controlX2 = x - stepX * 0.4f
                                        cubicTo(controlX1, prevY, controlX2, y, x, y)
                                    }
                                }
                                
                                lineTo(padding + (data.size - 1) * stepX, padding + chartHeight)
                                close()
                            }
                            
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.2f * animationProgress),
                                        Color.Transparent
                                    ),
                                    startY = padding,
                                    endY = padding + chartHeight
                                )
                            )
                        }
                        
                        // Draw line
                        if (data.size >= 2) {
                            val linePath = Path().apply {
                                data.forEachIndexed { index, point ->
                                    val x = padding + index * stepX
                                    val y = padding + chartHeight * (1 - (moodToValue(point.mood) - 1f) / 4f)
                                    
                                    if (index == 0) {
                                        moveTo(x, y)
                                    } else {
                                        val prevX = padding + (index - 1) * stepX
                                        val prevY = padding + chartHeight * (1 - (moodToValue(data[index - 1].mood) - 1f) / 4f)
                                        val controlX1 = prevX + stepX * 0.4f
                                        val controlX2 = x - stepX * 0.4f
                                        cubicTo(controlX1, prevY, controlX2, y, x, y)
                                    }
                                }
                            }
                            
                            // Animated clip
                            val clipWidth = canvasWidth * animationProgress
                            drawIntoCanvas { canvas ->
                                val nativeCanvas = canvas.nativeCanvas
                                val paint = android.graphics.Paint().apply {
                                    color = primaryColor.toArgb()
                                    strokeWidth = 3.dp.toPx()
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeCap = android.graphics.Paint.Cap.ROUND
                                    strokeJoin = android.graphics.Paint.Join.ROUND
                                    isAntiAlias = true
                                }
                                nativeCanvas.save()
                                nativeCanvas.clipRect(0f, 0f, clipWidth, canvasHeight)
                                nativeCanvas.drawPath(linePath.asAndroidPath(), paint)
                                nativeCanvas.restore()
                            }
                        }
                        
                        // Draw data points
                        data.forEachIndexed { index, point ->
                            val x = padding + index * stepX
                            val y = padding + chartHeight * (1 - (moodToValue(point.mood) - 1f) / 4f)
                            val pointAlpha = if (index <= (data.size * animationProgress).toInt()) 1f else 0f
                            
                            // White stroke
                            drawCircle(
                                color = Color.White.copy(alpha = pointAlpha),
                                radius = 6.dp.toPx(),
                                center = Offset(x, y)
                            )
                            
                            // Mood color fill
                            drawCircle(
                                color = moodToColor(point.mood).copy(alpha = pointAlpha),
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                    
                    // Tooltip
                    selectedPoint?.let { point ->
                        selectedOffset?.let { offset ->
                            TooltipCard(
                                point = point,
                                offset = offset,
                                moodToEmoji = ::moodToEmoji
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TooltipCard(
    point: MoodDataPoint,
    offset: Offset,
    moodToEmoji: (String) -> String
) {
    val density = LocalDensity.current
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .offset(
                    x = with(density) { (offset.x - 60).toDp() },
                    y = with(density) { (offset.y - 80).toDp() }
                )
                .widthIn(min = 120.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.S),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${point.date.monthValue}月${point.date.dayOfMonth}日",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = moodToEmoji(point.mood),
                    style = MaterialTheme.typography.headlineMedium
                )
                if (point.title.isNotBlank()) {
                    Text(
                        text = point.title,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}