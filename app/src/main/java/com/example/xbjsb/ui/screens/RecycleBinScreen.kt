package com.example.xbjsb.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.ui.components.AppMessageType
import com.example.xbjsb.ui.components.LocalAppMessageHostState
import com.example.xbjsb.ui.theme.Motion
import com.example.xbjsb.ui.theme.Spacing
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.ui.components.ApplyFrostedDialogWindow
import com.example.xbjsb.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecycleBinScreen(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val viewModel: DiaryViewModel = viewModel()
    val deletedEntries by viewModel.deletedEntries.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val frostedBlurEnabled by themePreferences.frostedBlurEnabledFlow.collectAsState(initial = false)
    val messageHost = LocalAppMessageHostState.current
    val scope = rememberCoroutineScope()
    var showClearAllDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<DiaryEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("回收站") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (deletedEntries.isNotEmpty()) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = "清空回收站")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (deletedEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                    Text(
                        text = "回收站为空",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "删除的日记会在这里保留",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(deletedEntries, key = { it.id }) { entry ->
                    DeletedDiaryCard(
                        entry = entry,
                        onRestore = {
                            scope.launch {
                                kotlinx.coroutines.delay(450)
                                viewModel.restoreEntry(entry)
                                messageHost?.show("已恢复到首页", AppMessageType.Success)
                                kotlinx.coroutines.delay(150)
                                onNavigateHome()
                            }
                        },
                        onDelete = {
                            entryToDelete = entry
                        },
                        modifier = Modifier.animateItemPlacement(
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = 260f
                            )
                        )
                    )
                }
                
                item {
                    Text(
                        text = "日记在回收站中保留 30 天后自动删除",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.M),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { 
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("清空回收站") 
            },
            text = {
                Text("将彻底删除回收站中的所有日记和图片，此操作不可撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            deletedEntries.forEach { viewModel.permanentlyDeleteEntry(it) }
                            showClearAllDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { 
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("彻底删除") 
            },
            text = {
                Text("将彻底删除这篇日记和相关图片，此操作不可撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.permanentlyDeleteEntry(entry)
                            entryToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun DeletedDiaryCard(
    entry: DiaryEntry,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deletedDays = remember(entry.deletedAt) {
        val days = ((System.currentTimeMillis() - (entry.deletedAt ?: 0L)) / (1000 * 60 * 60 * 24)).toInt()
        if (days == 0) "今天" else "$days 天前"
    }
    
    var isRestoring by remember { mutableStateOf(false) }
    
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isRestoring) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(360),
        label = "restore_alpha"
    )
    val scaleX by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isRestoring) Motion.ListExitScaleX else 1f,
        animationSpec = androidx.compose.animation.core.tween(360),
        label = "restore_scale_x"
    )
    val scaleY by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isRestoring) Motion.ListExitScaleY else 1f,
        animationSpec = androidx.compose.animation.core.tween(360),
        label = "restore_scale_y"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scaleX
                this.scaleY = scaleY
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title.ifBlank { "未命名日记" },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 内容预览
            if (entry.content.isNotBlank()) {
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 图片缩略图
            if (entry.images.isNotBlank()) {
                val context = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val imageList = entry.getImageList()
                    imageList.take(4).forEach { imagePath ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imagePath)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "图片",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (imageList.size > 4) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${imageList.size - 4}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 底部：日期 + 删除时间 + 按钮
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = entry.getFormattedDate(),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f)
                        )
                        Text(
                            text = "$deletedDays 删除",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("彻底删除")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        enabled = !isRestoring,
                        onClick = {
                            isRestoring = true
                            onRestore()
                        }
                    ) {
                        Icon(Icons.Filled.RestoreFromTrash, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("恢复")
                    }
                }
            }
        }
    }
}
