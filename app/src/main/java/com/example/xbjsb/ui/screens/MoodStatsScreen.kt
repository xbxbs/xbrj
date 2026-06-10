package com.example.xbjsb.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.ui.theme.*
import com.example.xbjsb.viewmodel.DiaryViewModel
import java.time.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiaryViewModel = viewModel()
) {
    val entries by viewModel.entries.collectAsState()
    
    val now = LocalDate.now()
    val weekStart = now.minusDays(now.dayOfWeek.value.toLong() - 1)
    val monthStart = now.withDayOfMonth(1)
    
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
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
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
            // 本周心情
            MoodStatsCard(
                title = "本周心情",
                subtitle = "${weekStart.monthValue}月${weekStart.dayOfMonth}日 - ${now.monthValue}月${now.dayOfMonth}日",
                moodCounts = weekMoodCounts,
                totalCount = weekEntries.size
            )
            
            // 本月心情
            MoodStatsCard(
                title = "本月心情",
                subtitle = "${now.year}年${now.monthValue}月",
                moodCounts = monthMoodCounts,
                totalCount = monthEntries.size
            )
            
            // 总体统计
            OverallStatsCard(entries = entries)
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
    val moodColor = when (mood) {
        "happy" -> androidx.compose.ui.graphics.Color(0xFFFFB347)
        "calm" -> androidx.compose.ui.graphics.Color(0xFF87CEEB)
        "excited" -> androidx.compose.ui.graphics.Color(0xFFFF6B9D)
        "sad" -> androidx.compose.ui.graphics.Color(0xFF9B9B9B)
        else -> MaterialTheme.colorScheme.primary
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