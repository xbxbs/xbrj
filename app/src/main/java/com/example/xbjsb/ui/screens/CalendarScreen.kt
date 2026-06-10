package com.example.xbjsb.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.ui.theme.*
import com.example.xbjsb.viewmodel.DiaryViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    viewModel: DiaryViewModel = viewModel()
) {
    val entries by viewModel.entries.collectAsState()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    
    // 获取当月有日记的日期集合
    val datesWithEntries = remember(entries, currentMonth) {
        entries
            .map { LocalDate.ofEpochDay(it.timestamp / (24 * 60 * 60 * 1000)) }
            .filter { 
                it.year == currentMonth.year && it.monthValue == currentMonth.monthValue 
            }
            .toSet()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        currentMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上个月")
                    }
                    TextButton(onClick = { currentMonth = YearMonth.now() }) {
                        Text("今天", style = MaterialTheme.typography.labelLarge)
                    }
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
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
                .padding(horizontal = Spacing.M)
        ) {
            Spacer(Modifier.height(Spacing.M))
            
            // 星期标题行
            WeekdayHeader()
            
            Spacer(Modifier.height(Spacing.S))
            
            // 日历网格
            CalendarGrid(
                currentMonth = currentMonth,
                today = today,
                datesWithEntries = datesWithEntries,
                onDateClick = onDateClick
            )
        }
    }
}

@Composable
fun WeekdayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    today: LocalDate,
    datesWithEntries: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 周日=0
    
    // 生成日历网格数据（包含前后月份的空白日期）
    val calendarDates = buildList {
        // 前置空白
        repeat(firstDayOfWeek) { add(null) }
        
        // 当月日期
        for (day in 1..lastDayOfMonth.dayOfMonth) {
            add(currentMonth.atDay(day))
        }
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        contentPadding = PaddingValues(vertical = Spacing.S),
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalArrangement = Arrangement.spacedBy(Spacing.S)
    ) {
        items(calendarDates) { date ->
            if (date != null) {
                CalendarDayCell(
                    date = date,
                    isToday = date == today,
                    hasEntry = date in datesWithEntries,
                    onClick = { onDateClick(date) }
                )
            } else {
                // 空白占位
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isToday: Boolean,
    hasEntry: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(Spacing.XXS),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                ),
                color = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (hasEntry) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
