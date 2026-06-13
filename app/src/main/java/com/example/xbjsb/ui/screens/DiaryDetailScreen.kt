package com.example.xbjsb.ui.screens

import android.os.Build
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.data.security.SecurityPreferences
import com.example.xbjsb.ui.components.ApplyFrostedDialogWindow
import com.example.xbjsb.ui.components.MoodChip
import com.example.xbjsb.ui.components.TagChip
import com.example.xbjsb.ui.components.AppMessageType
import com.example.xbjsb.ui.components.LocalAppMessageHostState
import com.example.xbjsb.ui.components.frostedTopAppBarColors
import com.example.xbjsb.ui.theme.CornerRadius
import com.example.xbjsb.ui.theme.Motion
import com.example.xbjsb.ui.theme.MotionEasing
import com.example.xbjsb.ui.theme.Spacing
import com.example.xbjsb.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryDetailScreen(
    entryId: Long,
    viewModel: DiaryViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    var entry by remember { mutableStateOf<DiaryEntry?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val securityPreferences = remember { SecurityPreferences(context) }
    val frostedBlurEnabled by themePreferences.frostedBlurEnabledFlow.collectAsState(initial = false)
    val securityConfig by securityPreferences.configFlow.collectAsState(initial = com.example.xbjsb.data.security.SecurityConfig())
    val messageHost = LocalAppMessageHostState.current

    LaunchedEffect(entryId) {
        entry = viewModel.getEntryById(entryId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日记") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    entry?.let { current ->
                        IconButton(
                            onClick = {
                                val updated = current.copy(isFavorite = !current.isFavorite)
                                entry = updated
                                scope.launch {
                                    viewModel.updateEntry(updated)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (current.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (current.isFavorite) "取消收藏" else "收藏",
                                tint = if (current.isFavorite) androidx.compose.ui.graphics.Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                if (!current.isPrivate && !securityConfig.isEnabled) {
                                    scope.launch { messageHost?.show("请先设置隐私密码", AppMessageType.Warning) }
                                } else {
                                    val updated = current.copy(isPrivate = !current.isPrivate)
                                    entry = updated
                                    scope.launch {
                                        viewModel.updateEntry(updated)
                                        messageHost?.show(
                                            if (updated.isPrivate) "已设为私密" else "已取消私密",
                                            if (updated.isPrivate) AppMessageType.Private else AppMessageType.Success
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (current.isPrivate) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = if (current.isPrivate) "取消私密" else "设为私密",
                                tint = if (current.isPrivate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onNavigateToEdit(entryId) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = frostedTopAppBarColors()
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            entry == null -> MissingDiary(onNavigateBack = onNavigateBack, modifier = Modifier.padding(padding))

            else -> DiaryReader(
                entry = entry!!,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showDeleteDialog) {
AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("移至回收站？")
            },
            text = { Text("删除后可在回收站中恢复，30 天后自动清除。") },
            confirmButton = {
                    TextButton(
                        onClick = {
val target = entry
                                showDeleteDialog = false
                                if (target != null) {
                                    // 软删除，进入回收站
                                    scope.launch {
                                        viewModel.softDeleteEntry(target)
                                        onNavigateBack()
                                    }
                                } else {
                                    onNavigateBack()
                                }
                        },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiaryReader(
    entry: DiaryEntry,
    modifier: Modifier = Modifier
) {
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val frostedBlurEnabled by themePreferences.frostedBlurEnabledFlow.collectAsState(initial = false)
    val imageList = entry.getImageList()
    val tagList = entry.getTagList()
    val cardShape = RoundedCornerShape(20.dp)
    val subtleBorder = BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 阅读页头部：标题优先，日期 / 心情 / 标签作为辅助信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = subtleBorder
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = entry.title.ifBlank { "未命名日记" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp,
                        fontSize = 29.sp,
                        lineHeight = 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = entry.getFormattedDate(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                    )

                    MoodChip(mood = entry.mood)
                }

                if (tagList.isNotEmpty()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                        thickness = 0.5.dp
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tagList.forEach { TagChip(tag = it) }
                    }
                }
            }
        }

        // 正文阅读区：独立成卡，避免标题、标签、图片互相抢层级
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = subtleBorder
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "正文",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.54f)
                )

                SelectionContainer {
                    Text(
                        text = entry.content.ifBlank { "没有内容" },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 30.sp
                        ),
                        color = if (entry.content.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        if (imageList.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = subtleBorder
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "图片",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.54f)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        imageList.forEachIndexed { index, imagePath ->
                            Box(
                                modifier = Modifier
                                    .size(112.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f))
                                    .clickable { selectedImageIndex = index }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imagePath)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "日记图片",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.XXL))
    }
    
    selectedImageIndex?.let { index ->
        if (imageList.isNotEmpty()) {
            ImageLightbox(
                images = imageList,
                currentIndex = index.coerceIn(0, imageList.lastIndex),
                onIndexChange = { selectedImageIndex = it },
                onDismiss = { selectedImageIndex = null },
                frostedBlurEnabled = frostedBlurEnabled
            )
        } else {
            selectedImageIndex = null
        }
    }
}

@Composable
private fun ImageLightbox(
    images: List<String>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    frostedBlurEnabled: Boolean
) {
    if (images.isEmpty()) return

    val context = LocalContext.current
    val safeIndex = currentIndex.coerceIn(0, images.lastIndex)
    var previousIndex by remember { mutableStateOf(safeIndex) }
    val slideForward = safeIndex >= previousIndex

    LaunchedEffect(safeIndex) {
        previousIndex = safeIndex
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogView = LocalView.current

        DisposableEffect(dialogView, frostedBlurEnabled) {
            val window = (dialogView.parent as? DialogWindowProvider)?.window
            val previousDimAmount = window?.attributes?.dimAmount

            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && frostedBlurEnabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    window.attributes = window.attributes.apply {
                        blurBehindRadius = 72
                        dimAmount = 0f
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                }
            }

            onDispose {
                if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    window.attributes = window.attributes.apply {
                        if (previousDimAmount != null) dimAmount = previousDimAmount
                        blurBehindRadius = 0
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (frostedBlurEnabled) Color.Black.copy(alpha = 0.58f) else Color.Black)
        ) {
            if (frostedBlurEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF202124).copy(alpha = 0.96f),
                                    Color.Black.copy(alpha = 0.98f)
                                ),
                                radius = 1200f
                            )
                        )
                )
            }
            AnimatedContent(
                targetState = safeIndex,
                transitionSpec = {
                    val direction = if (slideForward) 1 else -1
                    (slideInHorizontally(
                        animationSpec = tween(Motion.Standard, easing = MotionEasing.Smooth),
                        initialOffsetX = { it / 4 * direction }
                    ) + fadeIn(tween(Motion.Fast, easing = MotionEasing.Smooth)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(Motion.Exit, easing = MotionEasing.Exit),
                                targetOffsetX = { -it / 5 * direction }
                            ) + fadeOut(tween(Motion.Exit, easing = MotionEasing.Exit))
                        )
                },
                modifier = Modifier.align(Alignment.Center),
                label = "lightbox_image"
            ) { index ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(images[index])
                        .crossfade(true)
                        .build(),
                    contentDescription = "图片预览",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 88.dp),
                    contentScale = ContentScale.Fit
                )
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(Motion.Fast, easing = MotionEasing.Smooth)) +
                    slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)),
                exit = fadeOut(tween(Motion.Exit, easing = MotionEasing.Exit)),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LightboxTopBar(
                    current = safeIndex + 1,
                    total = images.size,
                    onDismiss = onDismiss,
                    frostedBlurEnabled = frostedBlurEnabled
                )
            }

            if (images.size > 1 && safeIndex > 0) {
                LightboxNavButton(
                    icon = Icons.Filled.ChevronLeft,
                    contentDescription = "上一张",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp),
                    frostedBlurEnabled = frostedBlurEnabled,
                    onClick = { onIndexChange(safeIndex - 1) }
                )
            }

            if (images.size > 1 && safeIndex < images.lastIndex) {
                LightboxNavButton(
                    icon = Icons.Filled.ChevronRight,
                    contentDescription = "下一张",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp),
                    frostedBlurEnabled = frostedBlurEnabled,
                    onClick = { onIndexChange(safeIndex + 1) }
                )
            }

            if (images.size > 1) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(Motion.Fast, easing = MotionEasing.Smooth)) +
                        slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)),
                    exit = fadeOut(tween(Motion.Exit, easing = MotionEasing.Exit)) +
                        slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(Motion.Exit, easing = MotionEasing.Exit)),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    LightboxThumbnailStrip(
                        images = images,
                        selectedIndex = safeIndex,
                        onSelect = onIndexChange,
                        frostedBlurEnabled = frostedBlurEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun LightboxTopBar(
    current: Int,
    total: Int,
    onDismiss: () -> Unit,
    frostedBlurEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = RoundedCornerShape(CornerRadius.Full),
            color = Color.White.copy(alpha = if (frostedBlurEnabled) 0.18f else 0.12f)
        ) {
            Text(
                text = "$current / $total",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "关闭",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun LightboxNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    frostedBlurEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(CornerRadius.Full),
        color = Color.White.copy(alpha = if (frostedBlurEnabled) 0.16f else 0.12f),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
private fun LightboxThumbnailStrip(
    images: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    frostedBlurEnabled: Boolean
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedIndex) {
        val target = (selectedIndex * 68).coerceAtLeast(0)
        scrollState.animateScrollTo(target)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = if (frostedBlurEnabled) 0.12f else 0.04f))
            .horizontalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        images.forEachIndexed { index, imagePath ->
            val selected = index == selectedIndex
            Surface(
                modifier = Modifier
                    .size(if (selected) 58.dp else 50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Color.White.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.18f)
                ),
                color = Color.White.copy(alpha = if (selected) 0.18f else 0.08f),
                onClick = { onSelect(index) }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = "第 ${index + 1} 张图片",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun MissingDiary(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.padding(Spacing.XXL)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "日记不存在",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onNavigateBack, shape = RoundedCornerShape(CornerRadius.Full)) {
                Text("返回")
            }
        }
    }
}