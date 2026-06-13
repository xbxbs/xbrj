package com.example.xbjsb.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
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
import com.example.xbjsb.data.security.SecurityPreferences
import com.example.xbjsb.ui.components.ApplyFrostedDialogWindow
import com.example.xbjsb.ui.components.AppMessageType
import com.example.xbjsb.ui.components.LocalAppMessageHostState
import com.example.xbjsb.ui.components.MoodChip
import com.example.xbjsb.ui.components.AiAssistantMenuDialog
import com.example.xbjsb.ui.components.frostedTopAppBarColors
import com.example.xbjsb.ui.theme.EnterTransitions
import com.example.xbjsb.ui.theme.ExitTransitions
import com.example.xbjsb.ui.theme.IconSize
import com.example.xbjsb.ui.theme.Motion
import com.example.xbjsb.ui.theme.MotionEasing
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
    var isPrivate by remember { mutableStateOf(false) }
    var group by remember { mutableStateOf("") }
    var groupInput by remember { mutableStateOf("") }
    var images by remember { mutableStateOf("") }
    var originalTimestamp by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showMoodPicker by remember { mutableStateOf(false) }
    var showTagInput by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDraftRestoreDialog by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showQaComposer by remember { mutableStateOf(false) }
    var showAiAssistantMenu by remember { mutableStateOf(false) }
    var isGeneratingDiary by remember { mutableStateOf(false) }
    var skipDraftSave by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val isEditing = entryId != null && entryId > 0
    val canSave = title.isNotBlank() && content.isNotBlank() && !isSaving
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val messageHost = LocalAppMessageHostState.current
    val aiPreferences = remember { AiPreferences(context) }
    val themePreferences = remember { ThemePreferences(context) }
    val securityPreferences = remember { SecurityPreferences(context) }
    val frostedBlurEnabled by themePreferences.frostedBlurEnabledFlow.collectAsState(initial = false)
    val securityConfig by securityPreferences.configFlow.collectAsState(initial = com.example.xbjsb.data.security.SecurityConfig())
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
                    
                    messageHost?.show("已添加图片", AppMessageType.Success)
                } catch (e: Exception) {
                    messageHost?.show("图片添加失败：${e.message}", AppMessageType.Error)
                }
            }
        }
    }
    
    val hasDraft by viewModel.hasDraft.collectAsState()
    val privateGroups by viewModel.privateGroups.collectAsState()
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
        skipDraftSave = false
        if (isEditing) {
            val entry = viewModel.getEntryById(entryId!!)
            entry?.let {
                title = it.title
                content = it.content
                mood = it.mood
                tags = it.tags
                isFavorite = it.isFavorite
                isPrivate = it.isPrivate
                group = it.group
                images = it.images
                originalTimestamp = it.timestamp
            }
        } else if (hasDraft) {
            showDraftRestoreDialog = true
        } else {
            // 新建日记且没有草稿时，显式清空状态，避免 Compose 复用导致上次内容残留
            title = ""
            content = ""
            mood = "neutral"
            tags = ""
            tagInput = ""
            isFavorite = false
            isPrivate = false
            group = ""
            groupInput = ""
            images = ""
            originalTimestamp = null
        }
        isLoading = false
    }
    
    // 自动保存草稿
    LaunchedEffect(title, content, mood, tags, group, images, isPrivate, skipDraftSave) {
        if (!skipDraftSave && !isEditing && (title.isNotBlank() || content.isNotBlank() || tags.isNotBlank() || group.isNotBlank() || images.isNotBlank())) {
            kotlinx.coroutines.delay(1000) // 1秒防抖
            if (skipDraftSave) return@LaunchedEffect
            viewModel.saveDraft(
                title = title,
                content = content,
                mood = mood,
                tags = tags.split(',').filter { it.isNotBlank() },
                group = group,
                images = images,
                isPrivate = isPrivate
            )
        }
    }

    fun togglePrivateSetting(target: Boolean = !isPrivate) {
        if (target && !securityConfig.isEnabled) {
            scope.launch { messageHost?.show("请先到设置页设置隐私密码", AppMessageType.Warning) }
            return
        }
        isPrivate = target
    }

    fun saveEntry() {
        if (isSaving) return
        if (title.isBlank() || content.isBlank()) {
            scope.launch { messageHost?.show("请填写标题和内容", AppMessageType.Warning) }
            return
        }
        skipDraftSave = true
        isSaving = true
        val entry = DiaryEntry(
            id = entryId ?: System.currentTimeMillis(),
            title = title.trim(),
            content = content.trim(),
            timestamp = originalTimestamp ?: System.currentTimeMillis(),
            mood = mood,
            tags = tags,
            isFavorite = isFavorite,
            group = group,
            images = images,
            isPrivate = isPrivate
        )
        scope.launch {
            try {
                if (isEditing) viewModel.updateEntry(entry) else viewModel.insertEntry(entry)
                viewModel.clearDraft() // 保存后清除草稿
                messageHost?.show(
                    if (entry.isPrivate) "已保存到私密空间" else if (isEditing) "日记已更新" else "日记已保存",
                    if (entry.isPrivate) AppMessageType.Private else AppMessageType.Success
                )
                title = ""
                content = ""
                mood = "neutral"
                tags = ""
                tagInput = ""
                isFavorite = false
                isPrivate = false
                group = ""
                groupInput = ""
                images = ""
                originalTimestamp = null
                onNavigateBack()
            } catch (e: Exception) {
                skipDraftSave = false
                messageHost?.show("保存失败，请重试", AppMessageType.Error)
            } finally {
                isSaving = false
            }
        }
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
                            .height(34.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = if (isSystemInDarkTheme()) 0.92f else 0.96f),
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f))
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
                                modifier = Modifier.height(34.dp),
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
                            onShowAiAssistant = {
                                if (!isGeneratingDiary) showAiAssistantMenu = true
                            },
                            onAction = { action ->
                                when {
                                    !aiConfig.isConfigured -> {
                                        scope.launch { messageHost?.show("请先到设置页配置 AI 模型和 API Key", AppMessageType.Warning) }
                                    }
                                    action != DiaryAiAction.QUICK_GENERATE && content.isBlank() -> {
                                        scope.launch { messageHost?.show("请先写一点正文，再使用${action.shortName}", AppMessageType.Warning) }
                                    }
                                    action == DiaryAiAction.QUICK_GENERATE && content.isBlank() -> {
                                        scope.launch { messageHost?.show("请先输入关键词或简单描述", AppMessageType.Warning) }
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
                                                    DiaryAiAction.QUICK_GENERATE -> {
                                                        if (generated.content.isNotBlank()) content = generated.content
                                                        if (generated.title.isNotBlank()) title = generated.title
                                                        mood = generated.mood
                                                        if (generated.tags.isNotEmpty()) tags = generated.tags.joinToString(",")
                                                        if (generated.group.isNotBlank()) group = generated.group
                                                        messageHost?.show("已生成完整日记", AppMessageType.Success)
                                                    }
                                                    DiaryAiAction.EXPAND,
                                                    DiaryAiAction.SHORTEN,
                                                    DiaryAiAction.POLISH,
                                                    DiaryAiAction.CONTINUE -> {
                                                        if (generated.content.isNotBlank()) content = generated.content
                                                        if (generated.title.isNotBlank() && title.isBlank()) title = generated.title
                                                        mood = generated.mood
                                                        if (generated.tags.isNotEmpty()) tags = generated.tags.joinToString(",")
                                                        if (generated.group.isNotBlank()) group = generated.group
                                                        messageHost?.show("已完成${action.shortName}", AppMessageType.Success)
                                                    }
                                                    DiaryAiAction.GENERATE_TITLE -> {
                                                        if (generated.title.isNotBlank()) title = generated.title
                                                        messageHost?.show("已生成标题", AppMessageType.Success)
                                                    }
                                                    DiaryAiAction.SUMMARIZE_EMOTION -> {
                                                        mood = generated.mood
                                                        messageHost?.show(generated.summary.ifBlank { "已分析情绪" }, AppMessageType.Success)
                                                    }
                                                    DiaryAiAction.RECOMMEND_TAGS_GROUP -> {
                                                        if (generated.tags.isNotEmpty()) tags = generated.tags.joinToString(",")
                                                        if (generated.group.isNotBlank()) group = generated.group
                                                        messageHost?.show("已推荐标签和分组", AppMessageType.Success)
                                                    }
                                                }
                                            }.onFailure { error ->
                                                messageHost?.show(error.message ?: "AI 生成失败", AppMessageType.Error)
                                            }
                                            isGeneratingDiary = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                )

                EditorMetaCard(
                    mood = mood,
                    tags = tags,
                    group = group,
                    isPrivate = isPrivate,
                    imageCount = images.split(',').count { it.isNotBlank() },
                    showMoodPicker = showMoodPicker,
                    showTagInput = showTagInput,
                    showGroupPicker = showGroupPicker,
                    showImagePicker = showImagePicker,
                    onToggleMood = { showMoodPicker = !showMoodPicker },
                    onToggleTags = { showTagInput = !showTagInput },
                    onToggleGroup = { showGroupPicker = !showGroupPicker },
                    onTogglePrivate = { togglePrivateSetting() },
                    onPrivateChange = { togglePrivateSetting(it) },
                    onToggleImages = { showImagePicker = !showImagePicker },
                    onRemoveTag = { tag ->
                        tags = tags.split(',').filter { it.isNotBlank() && it != tag }.joinToString(",")
                    },
                    moodContent = {
                        MoodPicker(
                            selectedMood = mood,
                            onMoodSelected = {
                                mood = it
                                showMoodPicker = false
                            }
                        )
                    },
                    tagContent = {
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
                    },
                    groupContent = {
                        GroupPickerPanel(
                            currentGroup = group,
                            groupInput = groupInput,
                            existingGroups = usedGroups,
                            privateGroups = privateGroups,
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
                            },
                            onToggleGroupPrivate = { targetGroup ->
                                viewModel.toggleGroupPrivate(targetGroup)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (targetGroup in privateGroups) "已取消私密分组" else "已设为私密分组"
                                    )
                                }
                            }
                        )
                    },
                    imageContent = {
                        PropertyPanel {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
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
                                                    try {
                                                        val file = java.io.File(imagePath)
                                                        if (file.exists() && file.isFile) file.delete()
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                    images = images.split(',')
                                                        .filter { it.isNotBlank() && it != imagePath }
                                                        .joinToString(",")
                                                }
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(Spacing.M))
                                }

                                TextButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("添加图片")
                                    }
                                }

                                if (images.isNotBlank()) {
                                    TextButton(
                                        onClick = {
                                            images = ""
                                            showImagePicker = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("清除全部图片")
                                    }
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(Spacing.XXL))
            }
        }
        }
        
        if (showAiAssistantMenu) {
            AiAssistantMenuDialog(
                hasContent = content.isNotBlank(),
                isGenerating = isGeneratingDiary,
                frostedBlurEnabled = frostedBlurEnabled,
                onDismiss = { showAiAssistantMenu = false },
                onActionSelect = { action ->
                    when {
                        !aiConfig.isConfigured -> {
                            scope.launch { messageHost?.show("请先到设置页配置 AI 模型和 API Key", AppMessageType.Warning) }
                        }
                        action != DiaryAiAction.QUICK_GENERATE && content.isBlank() -> {
                            scope.launch { messageHost?.show("请先写一点正文，再使用${action.shortName}", AppMessageType.Warning) }
                        }
                        action == DiaryAiAction.QUICK_GENERATE && content.isBlank() -> {
                            scope.launch { messageHost?.show("请先输入关键词或简单描述", AppMessageType.Warning) }
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
                                        DiaryAiAction.QUICK_GENERATE -> {
                                            if (generated.content.isNotBlank()) content = generated.content
                                            if (generated.title.isNotBlank()) title = generated.title
                                            mood = generated.mood
                                            if (generated.tags.isNotEmpty()) tags = generated.tags.joinToString(",")
                                            if (generated.group.isNotBlank()) group = generated.group
                                            messageHost?.show("已生成完整日记", AppMessageType.Success)
                                        }
                                        DiaryAiAction.EXPAND,
                                        DiaryAiAction.SHORTEN,
                                        DiaryAiAction.POLISH,
                                        DiaryAiAction.CONTINUE -> {
                                            if (generated.content.isNotBlank()) content = generated.content
                                            if (generated.title.isNotBlank() && title.isBlank()) title = generated.title
                                            mood = generated.mood
                                            if (generated.tags.isNotEmpty()) tags = generated.tags.joinToString(",")
                                            if (generated.group.isNotBlank()) group = generated.group
                                            messageHost?.show("已完成${action.shortName}", AppMessageType.Success)
                                        }
                                        DiaryAiAction.GENERATE_TITLE -> {
                                            if (generated.title.isNotBlank()) title = generated.title
                                            messageHost?.show("已生成标题", AppMessageType.Success)
                                        }
                                        DiaryAiAction.SUMMARIZE_EMOTION -> {
                                            mood = generated.mood
                                            messageHost?.show(generated.summary.ifBlank { "已分析情绪" }, AppMessageType.Success)
                                        }
                                        DiaryAiAction.RECOMMEND_TAGS_GROUP -> {
                                            if (generated.tags.isNotEmpty()) tags = generated.tags.joinToString(",")
                                            if (generated.group.isNotBlank()) group = generated.group
                                            messageHost?.show("已推荐标签和分组", AppMessageType.Success)
                                        }
                                    }
                                }.onFailure { error ->
                                    messageHost?.show(error.message ?: "AI 生成失败", AppMessageType.Error)
                                }
                                isGeneratingDiary = false
                            }
                        }
                    }
                },
                onQaTemplateSelect = {
                    showQaComposer = true
                }
            )
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
                        messageHost?.show("请先到设置页配置 AI 模型和 API Key", AppMessageType.Warning)
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
                        messageHost?.show("已根据问答生成日记", AppMessageType.Success)
                    }.onFailure { error ->
                        messageHost?.show(error.message ?: "AI 生成失败", AppMessageType.Error)
                    }
                    isGeneratingDiary = false
                }
            }
        )
    }

    if (showDraftRestoreDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("发现未完成草稿")
            },
            text = { Text("是否恢复上次未完成的日记？放弃后会清除这份草稿。") },
            confirmButton = {
                TextButton(onClick = {
                    val draft = viewModel.restoreDraft()
                    title = draft.title
                    content = draft.content
                    mood = draft.mood ?: "neutral"
                    tags = draft.tags.joinToString(",")
                    group = draft.group
                    images = draft.images
                    isPrivate = draft.isPrivate
                    isFavorite = false
                    originalTimestamp = null
                    showDraftRestoreDialog = false
                }) { Text("恢复草稿") }
            },
            dismissButton = {
                TextButton(onClick = {
                    skipDraftSave = true
                    viewModel.clearDraft()
                    title = ""
                    content = ""
                    mood = "neutral"
                    tags = ""
                    tagInput = ""
                    isFavorite = false
                    isPrivate = false
                    group = ""
                    groupInput = ""
                    images = ""
                    originalTimestamp = null
                    showDraftRestoreDialog = false
                }) { Text("放弃草稿") }
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
    onShowAiAssistant: () -> Unit,
    onAction: (DiaryAiAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AiAssistantButton(
            isLoading = isLoading,
            onClick = onShowAiAssistant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AiMiniAction(
                text = "生成",
                enabled = !isLoading && hasContent,
                emphasized = true,
                onClick = { onAction(DiaryAiAction.QUICK_GENERATE) }
            )
            
            AiMiniAction(
                text = "润色",
                enabled = !isLoading && hasContent,
                emphasized = false,
                onClick = { onAction(DiaryAiAction.POLISH) }
            )
        }
    }
}

@Composable
private fun AiAssistantButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isLoading) 0.08f else 0.13f),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isLoading) 0.08f else 0.14f))
    ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                    text = if (isLoading) "处理中" else "AI 助手",
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
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
    }
    Surface(
        modifier = Modifier
            .height(34.dp)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = if (isDark) 0.dp else 2.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.20f else 0.30f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    lineHeight = 30.sp,
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
                                    lineHeight = 30.sp
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
private fun MetaSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.88f,
                    stiffness = 380f
                )
            ),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.8.dp,
        shadowElevation = if (isDark) 0.dp else 0.5.dp,
        border = BorderStroke(
            0.8.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.14f else 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingRow(
    leadingIcon: @Composable () -> Unit,
    title: String,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit,
    embedded: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (embedded) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (embedded) 0.dp else 2.dp,
        shadowElevation = if (embedded || isDark) 0.dp else 1.dp,
        border = if (embedded) {
            null
        } else {
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.18f else 0.28f)
            )
        }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (embedded) 4.dp else 16.dp,
                vertical = if (embedded) 9.dp else 14.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (embedded) 36.dp else 42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.16f else 0.24f)),
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
            Spacer(modifier = Modifier.width(16.dp))
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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = if (isDark) 0.dp else 1.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.18f else 0.26f)
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
            .height(34.dp),
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.10f else 0.16f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            0.6.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.06f else 0.10f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (enabled) onAction() }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.36f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (enabled) {
                Surface(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAction),
                    shape = RoundedCornerShape(8.dp),
                    color = accentContainer.copy(alpha = 0.13f),
                    border = BorderStroke(0.6.dp, accentContent.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            actionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = accentContent.copy(alpha = 0.82f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = accentContent.copy(alpha = 0.86f)
                        )
                    }
                }
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
    privateGroups: Set<String>,
    onGroupInputChange: (String) -> Unit,
    onUseGroup: () -> Unit,
    onSelectGroup: (String) -> Unit,
    onClearGroup: () -> Unit,
    onToggleGroupPrivate: (String) -> Unit
) {
    val query = groupInput.trim()
    val suggestions = existingGroups
        .filter { query.isBlank() || it.contains(query, ignoreCase = true) }
        .take(12)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
            InlineCreateBar(
                value = groupInput,
                placeholder = "输入分组",
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
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
            }

            if (currentGroup.isNotBlank()) {
                val currentIsPrivateGroup = currentGroup in privateGroups
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
                Text(
                    text = "私密分组下的所有日记会显示在私密空间。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { onToggleGroupPrivate(currentGroup) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            if (currentIsPrivateGroup) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentIsPrivateGroup) "取消分组私密" else "设为私密分组")
                    }
                    TextButton(
                        onClick = onClearGroup,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("清除分组")
                    }
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
    editor: @Composable () -> Unit,
    embedded: Boolean = false
) {
    val isDark = isSystemInDarkTheme()

    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.88f,
                    stiffness = 380f
                )
            )
            .clip(shape),
        shape = RoundedCornerShape(if (embedded) 14.dp else 16.dp),
        color = if (embedded) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (embedded) 0.dp else 0.8.dp,
        shadowElevation = if (embedded || isDark) 0.dp else 0.5.dp,
        border = if (embedded) {
            null
        } else {
            BorderStroke(
                0.8.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (expanded) 0.28f else if (isDark) 0.14f else 0.20f)
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (embedded) 4.dp else 14.dp,
                vertical = if (embedded) 9.dp else 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        onClick = onToggleExpanded,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, radius = 200.dp)
                    )
                    .padding(horizontal = if (embedded) 4.dp else 0.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (embedded) 36.dp else 40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.Medium),
                        tint = accentContent.copy(alpha = 0.78f)
                    )
                }

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
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
                exit = fadeOut(animationSpec = tween(220, easing = MotionEasing.Exit)) +
                    scaleOut(targetScale = 0.992f, animationSpec = tween(220, easing = MotionEasing.Exit)) +
                    shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(220, easing = MotionEasing.Exit)
                    )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f))
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
        editor = editor,
        embedded = true
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
        editor = editor,
        embedded = true
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
            InlineCreateBar(
                value = tagInput,
                placeholder = "输入标签",
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
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
            }
        }
}

@Composable
fun ImagePreviewItem(
    imagePath: String,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val messageHost = LocalAppMessageHostState.current
    
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorMetaCard(
    mood: String,
    tags: String,
    group: String,
    isPrivate: Boolean,
    imageCount: Int,
    showMoodPicker: Boolean,
    showTagInput: Boolean,
    showGroupPicker: Boolean,
    showImagePicker: Boolean,
    onToggleMood: () -> Unit,
    onToggleTags: () -> Unit,
    onToggleGroup: () -> Unit,
    onTogglePrivate: () -> Unit,
    onPrivateChange: (Boolean) -> Unit,
    onToggleImages: () -> Unit,
    onRemoveTag: (String) -> Unit,
    moodContent: @Composable () -> Unit,
    tagContent: @Composable () -> Unit,
    groupContent: @Composable () -> Unit,
    imageContent: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val selectedTags = tags.split(',').filter { it.isNotBlank() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.8.dp,
        shadowElevation = if (isDark) 0.dp else 0.6.dp,
        border = BorderStroke(
            width = 0.8.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.14f else 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetaChipBlock(
                    icon = Icons.Filled.Mood,
                    title = "心情",
                    value = moodLabel(mood),
                    active = showMoodPicker,
                    modifier = Modifier.weight(1f),
                    onClick = onToggleMood
                )
                MetaChipBlock(
                    icon = Icons.Filled.Label,
                    title = "标签",
                    value = if (selectedTags.isEmpty()) "添加" else "${selectedTags.size} 个",
                    active = showTagInput,
                    modifier = Modifier.weight(1f),
                    onClick = onToggleTags
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetaChipBlock(
                    icon = Icons.Filled.Folder,
                    title = "分组",
                    value = if (group.isBlank()) "选择" else group,
                    active = showGroupPicker,
                    modifier = Modifier.weight(1f),
                    onClick = onToggleGroup
                )
                MetaChipBlock(
                    icon = Icons.Filled.Lock,
                    title = "私密",
                    value = if (isPrivate) "已开启" else "关闭",
                    active = isPrivate,
                    modifier = Modifier.weight(1f),
                    trailing = {
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = onPrivateChange,
                            modifier = Modifier.graphicsLayer(scaleX = 0.72f, scaleY = 0.72f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                            )
                        )
                    },
                    onClick = onTogglePrivate
                )
            }

            MetaChipBlock(
                icon = Icons.Filled.Image,
                title = "图片",
                value = if (imageCount == 0) "添加" else "$imageCount 张",
                active = showImagePicker,
                modifier = Modifier.fillMaxWidth(),
                onClick = onToggleImages
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaAnimatedPanel(visible = showMoodPicker) {
                    MetaPanelTitle("心情")
                    moodContent()
                }
                MetaAnimatedPanel(visible = showTagInput) {
                    MetaPanelTitle("标签")
                    if (selectedTags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedTags.forEach { tag ->
                                EditorTagPill(
                                    text = tag,
                                    selected = true,
                                    removable = true,
                                    compact = true,
                                    onClick = { onRemoveTag(tag) }
                                )
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f),
                            thickness = 0.7.dp
                        )
                    }
                    tagContent()
                }
                MetaAnimatedPanel(visible = showGroupPicker) {
                    MetaPanelTitle("分组")
                    groupContent()
                }
                MetaAnimatedPanel(visible = showImagePicker) {
                    MetaPanelTitle("图片")
                    imageContent()
                }
            }
        }
    }
}

@Composable
private fun MetaChipBlock(
    icon: ImageVector,
    title: String,
    value: String,
    active: Boolean = false,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val containerColor = if (active) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
    }
    val borderColor = if (active) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f)
    }
    val iconColor = if (active) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.68f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
    }

    Surface(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(0.7.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = iconColor
            )
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    imageVector = if (active) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (active) 0.38f else 0.44f)
                )
            }
        }
    }
}

@Composable
private fun MetaPanelTitle(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 3.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.32f))
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
            maxLines = 1
        )
    }
}

@Composable
private fun MetaAnimatedPanel(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = EnterTransitions.FadeInExpand,
        exit = fadeOut(animationSpec = tween(200, easing = MotionEasing.Exit)) +
            shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(200, easing = MotionEasing.Exit))
    ) {
        MetaExpandPanel(content = content)
    }
}

@Composable
private fun MetaExpandPanel(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
        border = BorderStroke(
            width = 0.6.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun MetaDivider(stronger: Boolean = false) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp, end = 18.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (stronger) 0.15f else 0.11f),
        thickness = if (stronger) 0.9.dp else 0.7.dp
    )
}

@Composable
private fun MetaActionRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    expanded: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.86f)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                trailing()
            } else if (value != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.widthIn(min = 86.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (expanded) 0.45f else 0.58f)
                    )
                }
            }
        }
    }
}