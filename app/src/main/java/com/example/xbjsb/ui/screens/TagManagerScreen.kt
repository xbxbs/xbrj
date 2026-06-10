package com.example.xbjsb.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.ui.theme.*
import com.example.xbjsb.ui.components.ApplyFrostedDialogWindow
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TagManagerScreen(
    onNavigateBack: () -> Unit,
    onTagClick: (String) -> Unit,
    viewModel: DiaryViewModel = viewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val frostedBlurEnabled by themePreferences.frostedBlurEnabledFlow.collectAsState(initial = false)
    
    // 统计所有标签及其使用次数
    val tagStats = remember(entries) {
        entries
            .flatMap { it.getTagList() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { TagStat(it.key, it.value) }
    }
    
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "标签中心",
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
        ) {
            if (tagStats.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.M)
                    ) {
                        Icon(
                            Icons.Filled.Label,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.Medium)
                        )
                        Text(
                            text = "暂无标签",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "在日记中添加标签后会显示在这里",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.Medium)
                        )
                    }
                }
            } else {
                // 标签列表
                LazyColumn(
                    contentPadding = PaddingValues(Spacing.M),
                    verticalArrangement = Arrangement.spacedBy(Spacing.S)
                ) {
                    item {
                        TagCenterHeader(
                            tagCount = tagStats.size,
                            entryCount = entries.count { it.getTagList().isNotEmpty() }
                        )
                    }
                    items(tagStats, key = { it.tag }) { tagStat ->
                        TagItem(
                            tag = tagStat.tag,
                            count = tagStat.count,
                            onClick = { onTagClick(tagStat.tag) },
                            onDelete = { showDeleteDialog = tagStat.tag },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
    
    // 移除标签确认对话框
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("移除标签")
            },
            text = { 
                Text("确定要从所有日记中移除标签「${showDeleteDialog}」吗？\n日记内容不会被删除，只会删除这个标签关联。") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { tag ->
                            // 从所有日记中移除该标签
                            entries.forEach { entry ->
                                val tagList = entry.getTagList()
                                if (tag in tagList) {
                                    val updatedEntry = entry.copy(
                                        tags = tagList.filter { it != tag }.joinToString(",")
                                    )
                                    viewModel.updateEntry(updatedEntry)
                                }
                            }
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TagCenterHeader(
    tagCount: Int,
    entryCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Filled.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "标签索引",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "共 $tagCount 个标签，覆盖 $entryCount 篇日记。点击标签可查看相关日记，右侧按钮用于从所有日记中移除该标签。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun TagItem(
    tag: String,
    count: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Label,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "点击查看相关日记",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)
            ) {
                Text(
                    text = "$count 篇",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(6.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "移除标签",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.68f)
                )
            }
        }
    }
}

data class TagStat(
    val tag: String,
    val count: Int
)