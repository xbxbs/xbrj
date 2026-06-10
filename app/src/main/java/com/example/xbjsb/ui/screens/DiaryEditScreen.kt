package com.example.xbjsb.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.xbjsb.ai.DiaryAiAction
import com.example.xbjsb.ai.DiaryGenerationContext
import com.example.xbjsb.ai.DiaryQaAnswer
import com.example.xbjsb.ai.DiaryQaTemplates
import com.example.xbjsb.ai.DiaryQaTemplate
import com.example.xbjsb.ai.OpenAiCompatibleDiaryService
import com.example.xbjsb.data.AiPreferences
import com.example.xbjsb.ui.components.EnhancedSnackbarHost
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.ui.components.ApplyFrostedDialogWindow
import com.example.xbjsb.ui.components.MoodChip
import com.example.xbjsb.ui.components.frostedTopAppBarColors
import com.example.xbjsb.ui.theme.EnterTransitions
import com.example.xbjsb.ui.theme.ExitTransitions
import com.example.xbjsb.ui.theme.IconSize
import com.example.xbjsb.ui.theme.Motion
import com.example.xbjsb.ui.theme.Spacing
import com.example.xbjsb.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryEditScreen(
    entryId: Long?,
    viewModel: DiaryViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("neutral") }
    var tags by remember { mutableStateOf("") }
    var tagInput by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var group by remember { mutableStateOf("") }
    var groupInput by remember { mutableStateOf("") }
    var images by remember { mutableStateOf("") }
    var originalTimestamp by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showMoodPicker by remember { mutableStateOf(false) }
    var showTagInput by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var showQaComposer by remember { mutableStateOf(false) }
    var showMoreAiActions by remember { mutableStateOf(false) }
    var isGeneratingDiary by remember { mutableStateOf(false) }

    val isEditing = entryId != null && entryId > 0
    val canSave = title.isNotBlank() && content.isNotBlank()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val aiPreferences = remember { AiPreferences(context) }
    val themePreferences = remember { ThemePreferences(context) }
    val frostedBlurEnabled by themePreferences.frostedBlurEnabledFlow.collectAsState(initial = false)
    val aiConfig by aiPreferences.configFlow.collectAsState(initial = AiPreferences.AiConfig())
    val diaryAiService = remember { OpenAiCompatibleDiaryService() }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    // 复制图片到应用目录
                    val fileName = "IMG_${System.currentTimeMillis()}.jpg"
                    val file = java.io.File(context.filesDir, "images/$fileName")
                    file.parentFile?.mkdirs()
                    
                    context.contentResolver.openInputStream(it)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    // 保存文件路径
                    val imagePath = file.absolutePath
                    val currentImages = images.split(',').filter { it.isNotBlank() }.toMutableList()
                    currentImages.add(imagePath)
                    images = currentImages.joinToString(",")
                    
                    snackbarHostState.showSnackbar("已添加图片")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("图片添加失败：${e.message}")
                }
            }
        }
    }
    
    val hasDraft by viewModel.hasDraft.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val usedTags = remember(allEntries) {
        allEntries
            .flatMap { it.getTagList() }
            .distinct()
            .sorted()
    }
    val usedGroups = remember(allEntries) {
        allEntries
            .mapNotNull { it.group.takeIf { groupName -> groupName.isNotBlank() } }
            .distinct()
            .sorted()
    }

    LaunchedEffect(entryId) {
        if (isEditing) {
            val entry = viewModel.getEntryById(entryId!!)
            entry?.let {
                title = it.title
                content = it.content
                mood = it.mood
                tags = it.tags
                isFavorite = it.isFavorite
                group = it.group
                images = it.images
                originalTimestamp = it.timestamp
            }
        } else if (hasDraft) {
            // 恢复草稿
            val draft = viewModel.restoreDraft()
            title = draft.title
            content = draft.content
            mood = draft.mood ?: "neutral"
            tags = draft.tags.joinToString(",")
        }
        isLoading = false
    }
    
    // 自动保存草稿
    LaunchedEffect(title, content, mood, tags) {
        if (!isEditing && (title.isNotBlank() || content.isNotBlank())) {
            kotlinx.coroutines.delay(1000) // 1秒防抖
            viewModel.saveDraft(
                title = title,
                content = content,
                mood = mood,
                tags = tags.split(',').filter { it.isNotBlank() }
            )
        }
    }

    fun saveEntry() {
        if (!canSave) {
            scope.launch { snackbarHostState.showSnackbar("请填写标题和内容") }
            return
        }
        val entry = DiaryEntry(
            id = entryId ?: System.currentTimeMillis(),
            title = title.trim(),
            content = content.trim(),
            timestamp = originalTimestamp ?: System.currentTimeMillis(),
            mood = mood,
            tags = tags,
            isFavorite = isFavorite,
            group = group,
            images = images
        )
        if (isEditing) viewModel.updateEntry(entry) else viewModel.insertEntry(entry)
        viewModel.clearDraft() // 保存后清除草稿
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "编辑" else "写日记"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank() || tags.isNotBlank()) {
                                showDiscardDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSystemInDarkTheme()) 0.18f else 0.20f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f))
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { isFavorite = !isFavorite },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isFavorite) Color(0xFFFF6B6B).copy(alpha = 0.18f) else Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "收藏",
                                        tint = if (isFavorite) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { saveEntry() },
                                enabled = canSave,
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("保存", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                },
                colors = frostedTopAppBarColors()
            )
        },
        snackbarHost = { EnhancedSnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.L, vertical = Spacing.L),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                EditorPaper(
                    title = title,
                    onTitleChange = { title = it },
                    content = content,
                    onContentChange = { content = it },
                    aiToolbar = {
                        InlineAiWritingBar(
                            hasContent = content.isNotBlank(),
                            isLoading = isGeneratingDiary,
                            showMore = showMoreAiActions,
                            onToggleMore = { showMoreAiActions = !showMoreAiActions },
                            onQaCompose = {
                                if (!isGeneratingDiary) showQaComposer = true
                            },
                            onAction = { action ->
                                when {
                                    !aiConfig.isConfigured -> {
                                        scope.launch { snackbarHostState.showSnackbar("请先到设置页配置 AI 模型和 API Key") }
                                    }
                                    content.isBlank() -> {
                                        scope.launch { snackbarHostState.showSnackbar("请先写一点正文，再使用${action.shortName}") }
                                    }
                                    else -> {
                                        isGeneratingDiary = true
                                        scope.launch {
                                            val dateText = java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm", java.util.Locale.CHINA)
                                                .format(java.util.Date(originalTimestamp ?: System.currentTimeMillis()))
                                            val result = diaryAiService.processDiaryWriting(
                                                config = aiConfig,
                                                action = action,
                                                generationContext = DiaryGenerationContext(
                                                    dateText = dateText,
                                                    existingTitle = title,
                                                    existingContent = content,
                                                    currentMood = mood,
                                                    currentTags = tags.split(',').filter { it.isNotBlank() },
                                                    currentGroup = group,
                                                    lengthPreference = "中，约 300-500 字",
                                                    stylePreference = "自然真实，像普通人认真写下的一篇日记"
                                                )
                                            )
                                            result.onSuccess { generated ->
                                                when (action) {
                                                    DiaryAiAction.EXPAND,
                                                    DiaryAiAction.SHORTEN,
                                                    DiaryAiAction.POLISH,
                                                    DiaryAiAction.CONTINUE -> {
                                                        if (generated.content.isNotBlank()) content = generated.content
                                                        if (generated.title.isNotBlank() && title.isBlank()) title = generated.title
                                                        mood = generated.mood
                                                        if (generated.tags.isNotEmpty()) tags = generated.tags.joinToString(",")
                                                        if (generated.group.isNotBlank()) group = generated.group
                                                        snackbarHostState.showSnackbar("已完成${action.shortName}")
                                                    }
                                                    DiaryAiAction.GENERATE_TITLE -> {
                                                        if (generated.title.isNotBlank()) title = generated.title
                                                        snackbarHostState.showSnackbar("已生成标题")
                                                    }
                                                    DiaryAiAction.SUMMARIZE_EMOTION -> {
                                                        mood = generated.mood
                                                        snackbarHostState.showSnackbar(generated.summary.ifBlank { "已总结情绪" })
                                                    }
                                                    DiaryAiAction.RECOMMEND_TAGS_GROUP -> {
                                                        if (generated.tags.isNotEmpty()) tags = generated.tags.joinToString(",")
                                                        if (generated.group.isNotBlank()) group = generated.group
                                                        snackbarHostState.showSnackbar("已推荐标签和分组")
                                                    }
                                                }
                                            }.onFailure { error ->
                                                snackbarHostState.showSnackbar(error.message ?: "AI 生成失败")
                                            }
                                            isGeneratingDiary = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                )

                SettingRow(
                    leadingIcon = { Icon(Icons.Filled.Mood, contentDescription = null, modifier = Modifier.size(IconSize.Medium), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.76f)) },
                    title = "心情",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = moodLabel(mood),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(Spacing.S))
                            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(IconSize.Small))
                        }
                    },
                    onClick = { showMoodPicker = !showMoodPicker }
                )

                AnimatedVisibility(
                    visible = showMoodPicker,
                    enter = EnterTransitions.FadeInExpand,
                    exit = ExitTransitions.FadeOutShrink
                ) {
                    MoodPicker(
                        selectedMood = mood,
                        onMoodSelected = {
                            mood = it
                            showMoodPicker = false
                        }
                    )
                }

                TagWorkspace(
                    tags = tags,
                    expanded = showTagInput,
                    onToggleExpanded = { showTagInput = !showTagInput },
                    onRemoveTag = { tag ->
                        tags = tags.split(',').filter { it.isNotBlank() && it != tag }.joinToString(",")
                    },
                    editor = {
                        TagEditor(
                            tags = tags,
                            tagInput = tagInput,
                            existingTags = usedTags,
                            onTagInputChange = { tagInput = it },
                            onAddTag = {
                                val newTag = tagInput.trim()
                                if (newTag.isNotBlank()) {
                                    val list = tags.split(',').filter { it.isNotBlank() }.toMutableList()
                                    if (!list.contains(newTag)) list.add(newTag)
                                    tags = list.joinToString(",")
                                    tagInput = ""
                                }
                            },
                            onSelectTag = { selectedTag ->
                                val list = tags.split(',').filter { it.isNotBlank() }.toMutableList()
                                if (!list.contains(selectedTag)) list.add(selectedTag)
                                tags = list.joinToString(",")
                                tagInput = ""
                            }
                        )
                    }
                )

                GroupWorkspace(
                    group = group,
                    expanded = showGroupPicker,
                    onToggleExpanded = { showGroupPicker = !showGroupPicker },
                    editor = {
                        GroupPickerPanel(
                            currentGroup = group,
                            groupInput = groupInput,
                            existingGroups = usedGroups,
                            onGroupInputChange = { groupInput = it },
                            onUseGroup = {
                                val newGroup = groupInput.trim()
                                if (newGroup.isNotBlank()) {
                                    group = newGroup
                                    groupInput = ""
                                    showGroupPicker = false
                                }
                            },
                            onSelectGroup = { selectedGroup ->
                                group = selectedGroup
                                groupInput = ""
                                showGroupPicker = false
                            },
                            onClearGroup = {
                                group = ""
                                groupInput = ""
                                showGroupPicker = false
                            }
                        )
                    }
                )
                
                // 图片附件
                var showImagePicker by remember { mutableStateOf(false) }
                SettingRow(
                    leadingIcon = { Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(IconSize.Medium), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.76f)) },
                    title = "图片",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (images.isBlank()) "添加" else "${images.split(',').filter { it.isNotBlank() }.size} 张",
                                modifier = Modifier.widthIn(max = 120.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(Spacing.S))
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(IconSize.Small))
                        }
                    },
                    onClick = { showImagePicker = !showImagePicker }
                )
                
                AnimatedVisibility(
                    visible = showImagePicker,
                    enter = EnterTransitions.FadeInExpand,
                    exit = ExitTransitions.FadeOutShrink
                ) {
                    PropertyPanel {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 图片预览网格
                        if (images.isNotBlank()) {
                            val imageList = images.split(',').filter { it.isNotBlank() }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                                verticalArrangement = Arrangement.spacedBy(Spacing.S)
                            ) {
                                imageList.forEach { imagePath ->
                                    ImagePreviewItem(
                                        imagePath = imagePath,
                                        onRemove = {
                                            // 先删除文件
                                            try {
                                                val file = java.io.File(imagePath)
                                                if (file.exists() && file.isFile) {
                                                    file.delete()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                            // 再从字符串移除
                                            images = images.split(',')
                                                .filter { it.isNotBlank() && it != imagePath }
                                                .joinToString(",")
                                        }
                                    )
                                }
                            }
                            Spacer(Modifier.height(Spacing.M))
                        }
                        
                        // 添加图片按钮
                        TextButton(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("添加图片")
                            }
                        }
                        
                        if (images.isNotBlank()) {
                            Spacer(Modifier.height(Spacing.S))
                            TextButton(
                                onClick = {
                                    images = ""
                                    showImagePicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("清除全部图片")
                            }
                        }
                    }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.XXL))
            }
        }
    }

    if (showQaComposer) {
        QaComposerDialog(
            isGenerating = isGeneratingDiary,
            frostedBlurEnabled = frostedBlurEnabled,
            onDismiss = {
                if (!isGeneratingDiary) showQaComposer = false
            },
            onGenerate = { selectedTemplate, qaAnswers ->
                if (!aiConfig.isConfigured) {
                    scope.launch {
                        snackbarHostState.showSnackbar("请先到设置页配置 AI 模型和 API Key")
                    }
                    return@QaComposerDialog
                }

                isGeneratingDiary = true
                scope.launch {
                    val dateText = java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm", java.util.Locale.CHINA)
                        .format(java.util.Date(originalTimestamp ?: System.currentTimeMillis()))
                    val result = diaryAiService.generateDiaryFromQa(
                        config = aiConfig,
                        template = selectedTemplate,
                        answers = qaAnswers,
                        generationContext = DiaryGenerationContext(
                            dateText = dateText,
                            existingTitle = title,
                            existingContent = content,
                            currentMood = mood,
                            currentTags = tags.split(',').filter { it.isNotBlank() },
                            currentGroup = group,
                            lengthPreference = "中，约 300-500 字",
                            stylePreference = "自然真实，像普通人认真写下的一篇日记"
                        )
                    )
                    result.onSuccess { generated ->
                        if (generated.title.isNotBlank()) title = generated.title
                        if (generated.content.isNotBlank()) content = generated.content
                        mood = generated.mood
                        tags = generated.tags.joinToString(",")
                        group = generated.group
                        showQaComposer = false
                        snackbarHostState.showSnackbar("已根据问答生成日记")
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar(error.message ?: "AI 生成失败")
                    }
                    isGeneratingDiary = false
                }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("放弃编辑？")
            },
            text = { Text("当前内容还没有保存，确定要离开吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack()
                }) { Text("离开") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("继续编辑") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InlineAiWritingBar(
    hasContent: Boolean,
    isLoading: Boolean,
    showMore: Boolean,
    onToggleMore: () -> Unit,
    onQaCompose: () -> Unit,
    onAction: (DiaryAiAction) -> Unit
) {
    val assistActions = listOf(
        "续写" to DiaryAiAction.CONTINUE,
        "标题" to DiaryAiAction.GENERATE_TITLE,
        "扩写" to DiaryAiAction.EXPAND,
        "缩写" to DiaryAiAction.SHORTEN,
        "情绪" to DiaryAiAction.SUMMARIZE_EMOTION,
        "标签" to DiaryAiAction.RECOMMEND_TAGS_GROUP
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AiPromptButton(
                isLoading = isLoading,
                onClick = onQaCompose
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AiMiniAction(
                    text = "润色",
                    enabled = !isLoading && hasContent,
                    emphasized = true,
                    onClick = { onAction(DiaryAiAction.POLISH) }
                )

                AiMiniAction(
                    text = if (showMore) "收起" else "更多",
                    enabled = !isLoading,
                    icon = if (showMore) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    onClick = onToggleMore
                )
            }
        }

        AnimatedVisibility(
            visible = showMore,
            enter = EnterTransitions.FadeInExpand,
            exit = ExitTransitions.FadeOutShrink
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.10f),
                tonalElevation = 0.dp,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "建议操作",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        assistActions.forEach { (label, action) ->
                            AiSuggestionToken(
                                text = label,
                                enabled = !isLoading && hasContent,
                                onClick = { onAction(action) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiPromptButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isLoading) 0.12f else 0.20f),
        tonalElevation = 0.dp
    ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 1.8.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.76f)
                    )
                } else {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
                    )
                }
                Text(
                    text = if (isLoading) "处理中" else "AI 问答",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
    }
}

@Composable
private fun AiMiniAction(
    text: String,
    enabled: Boolean,
    emphasized: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val container = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
        emphasized -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f)
    }
    val content = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
        emphasized -> MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
    }
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = container,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (icon == null) 12.dp else 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = content,
                maxLines = 1
            )
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = content.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun AiSuggestionToken(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    val container = if (enabled) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
    }
    val content = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.72f else 0.32f)

    Surface(
        modifier = Modifier
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = container,
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = content,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QaComposerDialog(
    isGenerating: Boolean,
    frostedBlurEnabled: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (DiaryQaTemplate, List<DiaryQaAnswer>) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf(DiaryQaTemplates.templates.first()) }
    var currentIndex by remember(selectedTemplate) { mutableStateOf(0) }
    var answers by remember(selectedTemplate) {
        mutableStateOf(List(selectedTemplate.questions.size) { "" })
    }
    val currentQuestion = selectedTemplate.questions[currentIndex]
    val progressText = "${currentIndex + 1} / ${selectedTemplate.questions.size}"
    val isDark = isSystemInDarkTheme()
    val qaAccent = Color(0xFF9CCBFF)
    val qaAccentContainer = if (isDark) Color(0xFF243242) else Color(0xFFE3F1FF)
    val qaButtonContainer = if (isDark) Color(0xFF2E4054) else Color(0xFFD7ECFF)
    val qaButtonContent = if (isDark) Color(0xFFEAF4FF) else Color(0xFF17324A)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("问答成文")
                Text(
                    text = "按模板逐步回答，AI 会融合成完整日记",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "选择模板",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiaryQaTemplates.templates.forEach { template ->
                        val selected = selectedTemplate.id == template.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable(enabled = !isGenerating) {
                                    selectedTemplate = template
                                    currentIndex = 0
                                    answers = List(template.questions.size) { "" }
                                },
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) qaAccentContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
                            border = BorderStroke(
                                1.dp,
                                if (selected) qaAccent.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
                            )
                        ) {
                            Text(
                                text = template.name,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (selected) qaButtonContent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSystemInDarkTheme()) 0.24f else 0.42f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = qaAccentContainer.copy(alpha = if (isDark) 0.72f else 1f)
                            ) {
                                Text(
                                    text = progressText,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = qaAccent
                                )
                            }
                            Text(
                                text = selectedTemplate.description,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = answers.getOrElse(currentIndex) { "" },
                            onValueChange = { value ->
                                answers = answers.toMutableList().also { list ->
                                    if (currentIndex in list.indices) list[currentIndex] = value
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 6,
                            placeholder = { Text(currentQuestion.placeholder) },
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isGenerating,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                focusedBorderColor = qaAccent.copy(alpha = 0.58f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.46f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = !isGenerating && currentIndex > 0
                ) { Text("上一题") }

                if (currentIndex < selectedTemplate.questions.lastIndex) {
                    FilledTonalButton(
                        onClick = { currentIndex++ },
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = qaButtonContainer,
                            contentColor = qaButtonContent,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    ) { Text("下一题") }
                } else {
                    FilledTonalButton(
                        onClick = {
                            val qaAnswers = selectedTemplate.questions.mapIndexed { index, question ->
                                DiaryQaAnswer(
                                    question = question.question,
                                    answer = answers.getOrElse(index) { "" }.trim()
                                )
                            }
                            onGenerate(selectedTemplate, qaAnswers)
                        },
                        enabled = !isGenerating && answers.any { it.isNotBlank() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = qaButtonContainer,
                            contentColor = qaButtonContent,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = qaButtonContent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("生成中")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("生成日记")
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isGenerating) { Text("取消") }
        }
    )
}

@Composable
private fun moodLabel(mood: String): String = when (mood) {
    "happy" -> "开心"
    "calm" -> "平静"
    "excited" -> "兴奋"
    "sad" -> "难过"
    else -> "一般"
}
@Composable
private fun EditorPaper(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    aiToolbar: @Composable () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isDark) 0.dp else 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.14f else 0.34f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                    fontSize = 26.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                decorationBox = { inner ->
                    Box {
                        if (title.isBlank()) {
                            Text(
                                text = "今天想记录什么？",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.4).sp,
                                    fontSize = 26.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.28f else 0.34f)
                            )
                        }
                        inner()
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = java.text.SimpleDateFormat("yyyy年M月d日 EEE", java.util.Locale.CHINA).format(java.util.Date()),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.62f else 0.68f)
                )

                Text(
                    text = "${content.length} 字",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.50f else 0.58f)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.10f else 0.18f),
                thickness = 0.6.dp
            )

            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 210.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                decorationBox = { inner ->
                    Box {
                        if (content.isBlank()) {
                            Text(
                                text = "写下此刻的想法，或者先留一句待会儿让 AI 帮你展开...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    lineHeight = 28.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.28f else 0.34f)
                            )
                        }
                        inner()
                    }
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.09f else 0.16f),
                thickness = 0.6.dp
            )

            aiToolbar()
        }
    }
}

@Composable
private fun SettingRow(
    leadingIcon: @Composable () -> Unit,
    title: String,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isDark) 0.dp else 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.12f else 0.30f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.20f else 0.32f)),
                    contentAlignment = Alignment.Center
                ) { leadingIcon() }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            trailing()
        }
    }
}

@Composable
private fun PropertyPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isDark) 0.dp else 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.12f else 0.28f)
        )
    ) {
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodPicker(
    selectedMood: String,
    onMoodSelected: (String) -> Unit
) {
    PropertyPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("happy", "calm", "excited", "sad", "neutral").forEach { value ->
                val isSelected = selectedMood == value
                Surface(
                    modifier = Modifier.clickable { onMoodSelected(value) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    },
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    } else null
                ) {
                    MoodChip(mood = value)
                }
            }
        }
    }
}

@Composable
private fun InlineCreateBar(
    value: String,
    placeholder: String,
    actionText: String,
    actionIcon: ImageVector,
    enabled: Boolean,
    accentContainer: Color,
    accentContent: Color,
    focusedBorderColor: Color,
    focusedContainerColor: Color,
    onValueChange: (String) -> Unit,
    onAction: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ) {
                // 点击卡片空白区域时，不做任何操作（让输入框自己处理聚焦）
            },
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isDark) 0.dp else 1.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.12f else 0.28f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (enabled) onAction() }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            FilledTonalButton(
                onClick = onAction,
                enabled = enabled,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = accentContainer.copy(alpha = 0.48f),
                    contentColor = accentContent,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Icon(
                    actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupPickerPanel(
    currentGroup: String,
    groupInput: String,
    existingGroups: List<String>,
    onGroupInputChange: (String) -> Unit,
    onUseGroup: () -> Unit,
    onSelectGroup: (String) -> Unit,
    onClearGroup: () -> Unit
) {
    val query = groupInput.trim()
    val suggestions = existingGroups
        .filter { query.isBlank() || it.contains(query, ignoreCase = true) }
        .take(12)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            InlineCreateBar(
                value = groupInput,
                placeholder = "搜索或新建分组",
                actionText = "使用",
                actionIcon = Icons.Filled.Check,
                enabled = query.isNotBlank(),
                accentContainer = MaterialTheme.colorScheme.primaryContainer,
                accentContent = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                onValueChange = onGroupInputChange,
                onAction = onUseGroup
            )

            if (suggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (query.isBlank()) "已创建分组" else "匹配分组",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { groupName ->
                            val selected = currentGroup == groupName
                            Surface(
                                modifier = Modifier.clickable { onSelectGroup(groupName) },
                                shape = RoundedCornerShape(999.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
                                },
                                border = BorderStroke(
                                    if (selected) 2.dp else 1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        if (selected) Icons.Filled.Check else Icons.Filled.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = groupName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (query.isNotBlank()) {
                Text(
                    text = "没有匹配的分组，点击“使用”即可创建并应用。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (currentGroup.isNotBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
                TextButton(
                    onClick = onClearGroup,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("清除当前分组")
                }
            }
        }
}

@Composable
private fun MetaPropertyCard(
    icon: ImageVector,
    title: String,
    actionText: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    accentContainer: Color,
    accentContent: Color,
    preview: @Composable () -> Unit,
    editor: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(
                onClick = onToggleExpanded,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ),
        shape = shape,
        color = if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isDark) 0.dp else 1.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (expanded) 0.34f else 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accentContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.Medium),
                        tint = accentContent.copy(alpha = 0.76f)
                    )
                }

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.Small),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            preview()

            AnimatedVisibility(
                visible = expanded,
                enter = EnterTransitions.FadeInExpand,
                exit = ExitTransitions.FadeOutShrink
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
                    editor()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagWorkspace(
    tags: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onRemoveTag: (String) -> Unit,
    editor: @Composable () -> Unit
) {
    val selectedTags = tags.split(',').filter { it.isNotBlank() }
    val isDark = isSystemInDarkTheme()
    MetaPropertyCard(
        icon = Icons.Filled.Label,
        title = "标签",
        actionText = if (expanded) "收起" else if (selectedTags.isEmpty()) "添加" else "管理",
        expanded = expanded,
        onToggleExpanded = onToggleExpanded,
        accentContainer = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.20f else 0.32f),
        accentContent = MaterialTheme.colorScheme.primary,
        preview = {
            if (selectedTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    selectedTags.forEach { tag ->
                        EditorTagPill(
                            text = tag,
                            compact = true,
                            selected = true,
                            removable = expanded,
                            onClick = if (expanded) { { onRemoveTag(tag) } } else null
                        )
                    }
                }
            } else {
                Text(
                    text = "还没有标签。添加主题、人物、地点或心情关键词。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                )
            }
        },
        editor = editor
    )
}

@Composable
private fun GroupWorkspace(
    group: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    editor: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    MetaPropertyCard(
        icon = Icons.Filled.Folder,
        title = "分组",
        actionText = if (expanded) "收起" else if (group.isBlank()) "选择" else "更改",
        expanded = expanded,
        onToggleExpanded = onToggleExpanded,
        accentContainer = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.20f else 0.32f),
        accentContent = MaterialTheme.colorScheme.primary,
        preview = {
            Text(
                text = if (group.isBlank()) "未分组" else group,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (group.isBlank()) 0.58f else 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        editor = editor
    )
}

@Composable
private fun TagPreviewPills(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty()) {
        EditorTagPill(
            text = "添加",
            showHash = false,
            compact = true,
            muted = true,
            modifier = modifier
        )
        return
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tags.take(1).forEach { tag ->
            EditorTagPill(
                text = tag,
                compact = true,
                showIcon = true
            )
        }
        if (tags.size > 1) {
            EditorTagPill(
                text = "+${tags.size - 1}",
                showHash = false,
                showIcon = false,
                compact = true,
                muted = true
            )
        }
    }
}

@Composable
private fun EditorTagPill(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    removable: Boolean = false,
    muted: Boolean = false,
    compact: Boolean = false,
    showHash: Boolean = false,
    showIcon: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(13.dp)
    val containerAlpha = when {
        selected -> 0.42f
        muted -> 0.22f
        else -> 0.32f
    }
    val borderAlpha = when {
        selected -> 0.20f
        muted -> 0.08f
        else -> 0.12f
    }
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        modifier = if (onClick != null) modifier.clip(shape).clickable { onClick() } else modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = containerAlpha),
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = borderAlpha)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                start = if (compact) 8.dp else 9.dp,
                end = if (removable) 7.dp else if (compact) 8.dp else 9.dp,
                top = 5.dp,
                bottom = 5.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = Icons.Filled.Label,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor.copy(alpha = if (muted) 0.46f else 0.66f)
                )
            }
            Text(
                text = if (showHash) "#$text" else text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                color = contentColor.copy(alpha = if (muted) 0.62f else 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (removable) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "移除标签",
                    modifier = Modifier.size(14.dp),
                    tint = contentColor.copy(alpha = 0.58f)
                )
            }
        }
    }
}

@Composable
private fun TagSectionTitle(
    text: String,
    count: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
        )
        if (count != null) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagEditor(
    tags: String,
    tagInput: String,
    existingTags: List<String>,
    onTagInputChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onSelectTag: (String) -> Unit
) {
    val selectedTags = tags.split(',').filter { it.isNotBlank() }
    val query = tagInput.trim()
    val suggestions = existingTags
        .filter { it !in selectedTags }
        .filter { query.isBlank() || it.contains(query, ignoreCase = true) }
        .take(12)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            InlineCreateBar(
                value = tagInput,
                placeholder = "搜索或新建标签",
                actionText = "添加",
                actionIcon = Icons.Filled.Add,
                enabled = query.isNotBlank(),
                accentContainer = MaterialTheme.colorScheme.primaryContainer,
                accentContent = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.46f),
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f),
                onValueChange = onTagInputChange,
                onAction = onAddTag
            )

            if (suggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagSectionTitle(if (query.isBlank()) "推荐" else "匹配标签", suggestions.size)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { tag ->
                            EditorTagPill(
                                text = tag,
                                selected = false,
                                onClick = { onSelectTag(tag) }
                            )
                        }
                    }
                }
            } else if (query.isNotBlank()) {
                Text(
                    text = "没有匹配标签，点击“添加”即可创建。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                )
            }
        }
}

@Composable
fun ImagePreviewItem(
    imagePath: String,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // 使用 AsyncImage 加载图片
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imagePath)
                .crossfade(true)
                .build(),
            contentDescription = "图片预览",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        
        // 删除按钮
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .padding(2.dp)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "删除",
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                        RoundedCornerShape(50)
                    )
                    .padding(2.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}