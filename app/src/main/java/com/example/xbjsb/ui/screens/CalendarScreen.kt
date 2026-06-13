package com.example.xbjsb.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.viewmodel.DiaryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: DiaryViewModel = viewModel()
) {
    val allEntries by viewModel.allEntries.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()

    val visibleEntries = remember(allEntries) {
        allEntries
            .filter { !it.isDeleted && !it.isPrivate }
            .sortedByDescending { it.timestamp }
    }
    val entriesByDate = remember(visibleEntries) {
        visibleEntries.groupBy { it.timestamp.toLocalDate() }
    }
    val datesWithEntries = remember(entriesByDate, currentMonth) {
        entriesByDate.keys
            .filter { it.year == currentMonth.year && it.monthValue == currentMonth.monthValue }
            .toSet()
    }
    val selectedEntries = remember(entriesByDate, selectedDate) {
        entriesByDate[selectedDate].orEmpty().sortedByDescending { it.timestamp }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    TextButton(onClick = {
                        currentMonth = YearMonth.now()
                        selectedDate = LocalDate.now()
                    }) {
                        Text("今天", style = MaterialTheme.typography.labelLarge)
                    }
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 1.dp,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
                        WeekdayHeader()
                        Spacer(Modifier.height(8.dp))
                        CalendarGrid(
                            currentMonth = currentMonth,
                            today = today,
                            selectedDate = selectedDate,
                            datesWithEntries = datesWithEntries,
                            entryCountForDate = { date -> entriesByDate[date].orEmpty().size },
                            onDateClick = { date -> selectedDate = date }
                        )
                    }
                }
            }

            if (selectedEntries.isEmpty()) {
                item { CalendarEmptyDateCard(selectedDate = selectedDate) }
            } else {
                item { SelectedDateHeader(selectedDate = selectedDate, count = selectedEntries.size) }
                items(selectedEntries, key = { it.id }) { entry ->
                    CalendarEntryPreview(
                        entry = entry,
                        onClick = { onNavigateToDetail(entry.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    datesWithEntries: Set<LocalDate>,
    entryCountForDate: (LocalDate) -> Int,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val calendarDates = buildList<LocalDate?> {
        repeat(firstDayOfWeek) { add(null) }
        for (day in 1..currentMonth.lengthOfMonth()) add(currentMonth.atDay(day))
    }

    val rowCount = ((calendarDates.size + 6) / 7).coerceAtLeast(5)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height((rowCount * 46).dp),
        userScrollEnabled = false,
        contentPadding = PaddingValues(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(calendarDates) { date ->
            if (date == null) {
                Box(modifier = Modifier.aspectRatio(1f))
            } else {
                CalendarDayCell(
                    date = date,
                    isToday = date == today,
                    isSelected = date == selectedDate,
                    hasEntry = date in datesWithEntries,
                    entryCount = entryCountForDate(date),
                    onClick = { onDateClick(date) }
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasEntry: Boolean,
    entryCount: Int,
    onClick: () -> Unit
) {
    val targetContainer = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.62f)
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    val container by animateColorAsState(targetContainer, label = "calendar_day_container")
    val targetContent = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val content by animateColorAsState(targetContent, label = "calendar_day_content")
    val cellSize by animateDpAsState(
        targetValue = if (isSelected) 40.dp else 38.dp,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 480f),
        label = "calendar_day_size"
    )

    Box(
        modifier = Modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(cellSize)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = container,
            border = when {
                isToday && !isSelected -> BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.26f))
                else -> null
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = content
                )
                if (hasEntry) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.66f))
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedDateHeader(selectedDate: LocalDate, count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("M月d日")),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (count > 0) "$count 篇日记" else "没有日记",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
            )
        }
    }
}

@Composable
private fun CalendarEmptyDateCard(selectedDate: LocalDate) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Book,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "${selectedDate.format(DateTimeFormatter.ofPattern("M月d日"))} 还没有记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarEntryPreview(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val timeText = Instant.ofEpochMilli(entry.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.5.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.title.ifBlank { "无标题日记" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
                    ) {
                        Text(
                            text = timeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
                        )
                    }
                }

                Spacer(Modifier.height(5.dp))

                Text(
                    text = entry.content.ifBlank { "没有正文内容" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    if (entry.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (entry.isFavorite) "取消收藏" else "收藏",
                    tint = if (entry.isFavorite) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                    modifier = Modifier.size(23.dp)
                )
            }
        }
    }
}

private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}
