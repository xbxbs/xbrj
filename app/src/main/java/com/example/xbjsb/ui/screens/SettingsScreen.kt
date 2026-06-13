package com.example.xbjsb.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.data.AiPreferences
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.data.backup.RestoreMode
import com.example.xbjsb.ui.components.ApplyFrostedDialogWindow
import com.example.xbjsb.ui.components.AppMessageType
import com.example.xbjsb.ui.components.LocalAppMessageHostState
import com.example.xbjsb.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
onNavigateBack: () -> Unit,
onNavigateToRecycleBin: () -> Unit = {}
) {
    val context = LocalContext.current
    val messageHost = LocalAppMessageHostState.current
    val themePreferences = remember { ThemePreferences(context) }
    val aiPreferences = remember { AiPreferences(context) }
    val securityPreferences = remember { com.example.xbjsb.data.security.SecurityPreferences(context) }
    val themeMode by themePreferences.themeModeFlow.collectAsState(initial = ThemePreferences.ThemeMode.SYSTEM)
    val themeColor by themePreferences.themeColorFlow.collectAsState(initial = com.example.xbjsb.ui.theme.ThemeColor.ORANGE)
    val aiConfig by aiPreferences.configFlow.collectAsState(initial = AiPreferences.AiConfig())
    val frostedBlurEnabled by themePreferences.frostedBlurEnabledFlow.collectAsState(initial = false)
    val animationSpeed by themePreferences.animationSpeedFlow.collectAsState(initial = ThemePreferences.AnimationSpeed.ELEGANT)
    val securityConfig by securityPreferences.configFlow.collectAsState(initial = com.example.xbjsb.data.security.SecurityConfig())
    val scope = rememberCoroutineScope()
    val diaryViewModel: DiaryViewModel = viewModel()
    val backupUiState by diaryViewModel.backupUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            diaryViewModel.exportBackup(uri)
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            diaryViewModel.prepareRestore(uri)
        }
    }

    LaunchedEffect(backupUiState.message) {
        val message = backupUiState.message
        if (message != null) {
            messageHost?.show(message, if (message.contains("成功") || message.contains("完成")) AppMessageType.Success else if (message.contains("失败")) AppMessageType.Error else AppMessageType.Info)
            diaryViewModel.clearBackupMessage()
        }
    }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showThemeColorDialog by remember { mutableStateOf(false) }
    var showAiConfigDialog by remember { mutableStateOf(false) }
    var showAnimationSpeedDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    
    val themeModeText = when (themeMode) {
        ThemePreferences.ThemeMode.LIGHT -> "浅色"
        ThemePreferences.ThemeMode.DARK -> "深色"
        ThemePreferences.ThemeMode.SYSTEM -> "跟随系统"
    }
    
    val themeColorText = when (themeColor) {
        com.example.xbjsb.ui.theme.ThemeColor.ORANGE -> "暖橙"
        com.example.xbjsb.ui.theme.ThemeColor.BLUE -> "天蓝"
        com.example.xbjsb.ui.theme.ThemeColor.PURPLE -> "典雅紫"
        com.example.xbjsb.ui.theme.ThemeColor.GREEN -> "自然绿"
        com.example.xbjsb.ui.theme.ThemeColor.PINK -> "樱花粉"
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 外观设置
            Text(
                text = "外观",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )
            
            SettingItem(
                icon = Icons.Filled.DarkMode,
                title = "主题模式",
                subtitle = themeModeText,
                onClick = { showThemeModeDialog = true }
            )
            
            SettingItem(
                icon = Icons.Filled.Palette,
                title = "主题颜色",
                subtitle = themeColorText,
                onClick = { showThemeColorDialog = true }
            )

            SettingItem(
                icon = Icons.Filled.Speed,
                title = "动画速度",
                subtitle = animationSpeed.displayName,
                onClick = { showAnimationSpeedDialog = true }
            )

            SettingSwitchItem(
            icon = Icons.Filled.Gradient,
            title = "磨砂模糊效果",
                subtitle = "默认关闭，仅用于图片预览、弹窗控件等浮层",
                checked = frostedBlurEnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        themePreferences.setFrostedBlurEnabled(enabled)
                    }
                }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // AI 写作设置
            Text(
                text = "AI 写作",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )

            SettingItem(
                icon = Icons.Filled.AutoAwesome,
                title = "AI 模型配置",
                subtitle = if (aiConfig.isConfigured) {
                    "${if (aiConfig.provider == AiPreferences.AiProvider.DEEPSEEK) "DeepSeek" else "OpenAI 通用"} · ${aiConfig.model}"
                } else {
                    "未配置 · 支持 OpenAI 通用接口和 DeepSeek"
                },
                onClick = { showAiConfigDialog = true }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // 数据管理
            Text(
                text = "数据管理",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )

            SettingItem(
                icon = Icons.Filled.Backup,
                title = "导出备份",
                subtitle = if (backupUiState.isProcessing) {
                    "正在处理，请稍候…"
                } else {
                    "将全部日记和图片保存为一个备份文件"
                },
                onClick = {
                    if (!backupUiState.isProcessing) {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.CHINA).format(Date())
                        exportBackupLauncher.launch("xbjsb_backup_$timestamp.zip")
                    }
                }
            )

            SettingItem(
                icon = Icons.Filled.Restore,
                title = "恢复备份",
                subtitle = if (backupUiState.isProcessing) {
                    "正在处理，请稍候…"
                } else {
                    "从备份文件恢复日记，可选择合并或覆盖"
                },
                onClick = {
                    if (!backupUiState.isProcessing) {
                        restoreBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                    }
                }
            )

            SettingItem(
                icon = Icons.Filled.Delete,
                title = "回收站",
                subtitle = "查看和恢复已删除的日记",
                onClick = onNavigateToRecycleBin
            )

            if (backupUiState.isProcessing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text(
                        text = "正在处理备份数据…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            
            // 安全
            Text(
                text = "安全",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )

            SettingItem(
                icon = Icons.Filled.Lock,
                title = "隐私密码",
                subtitle = if (securityConfig.isEnabled) "已启用" else "未设置",
                onClick = { showPasswordDialog = true }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            
            // 关于
            Text(
                text = "关于",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )
            
            SettingItem(
                icon = Icons.Filled.Info,
                title = "关于拾光札记",
                subtitle = "版本 1.0.0",
                onClick = { showAboutDialog = true }
            )
        }
    }

    val pendingSummary = backupUiState.pendingSummary
    if (pendingSummary != null) {
        val backupTime = remember(pendingSummary.createdAt) {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(pendingSummary.createdAt))
        }
        AlertDialog(
            onDismissRequest = {
                if (!backupUiState.isProcessing) diaryViewModel.dismissRestoreDialog()
            },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "恢复备份",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "检测到备份文件",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "日记",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${pendingSummary.entryCount} 篇",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "图片",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${pendingSummary.imageCount} 张",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "备份时间",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = backupTime,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Filled.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "合并恢复（推荐）",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "保留当前日记，只导入备份中不存在的内容。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.22f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "覆盖恢复（危险）",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "清空当前所有日记，并替换为备份内容，不可撤销。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                FilledTonalButton(
                    enabled = !backupUiState.isProcessing,
                    onClick = { diaryViewModel.restoreBackup(RestoreMode.MERGE) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("合并恢复")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        enabled = !backupUiState.isProcessing,
                        onClick = { diaryViewModel.dismissRestoreDialog() }
                    ) {
                        Text("取消")
                    }
                    TextButton(
                        enabled = !backupUiState.isProcessing,
                        onClick = { diaryViewModel.restoreBackup(RestoreMode.REPLACE) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("覆盖恢复")
                    }
                }
            }
        )
    }
    
    // 主题模式对话框
    if (showThemeModeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeModeDialog = false },
            icon = {
                Icon(
                    Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("主题模式")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePreferences.ThemeMode.values().forEach { mode ->
                        val selected = mode == themeMode
                        val (icon, text) = when (mode) {
                            ThemePreferences.ThemeMode.LIGHT -> Icons.Filled.LightMode to "浅色"
                            ThemePreferences.ThemeMode.DARK -> Icons.Filled.DarkMode to "深色"
                            ThemePreferences.ThemeMode.SYSTEM -> Icons.Filled.SettingsBrightness to "跟随系统"
                        }
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        themePreferences.setThemeMode(mode)
                                    }
                                    showThemeModeDialog = false
                                },
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeModeDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
    
    // 主题颜色对话框
    if (showThemeColorDialog) {
        AlertDialog(
            onDismissRequest = { showThemeColorDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("主题颜色")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "选择适合你记录氛围的主色调",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    com.example.xbjsb.ui.theme.ThemeColor.values().forEach { color ->
                        val selected = color == themeColor
                        val (colorValue, name, desc) = when (color) {
                            com.example.xbjsb.ui.theme.ThemeColor.ORANGE -> Triple(Color(0xFFFF6B35), "暖橙", "温暖、积极、具有生活感")
                            com.example.xbjsb.ui.theme.ThemeColor.BLUE -> Triple(Color(0xFF2196F3), "天蓝", "清爽、专注、适合长期记录")
                            com.example.xbjsb.ui.theme.ThemeColor.PURPLE -> Triple(Color(0xFF9C27B0), "典雅紫", "克制、优雅、富有表达感")
                            com.example.xbjsb.ui.theme.ThemeColor.GREEN -> Triple(Color(0xFF4CAF50), "自然绿", "平静、自然、适合复盘沉淀")
                            com.example.xbjsb.ui.theme.ThemeColor.PINK -> Triple(Color(0xFFE91E63), "樱花粉", "柔和、温暖、偏情绪记录")
                        }
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    scope.launch {
                                        themePreferences.setThemeColor(color)
                                    }
                                    showThemeColorDialog = false
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.46f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                }
                            ),
                            tonalElevation = if (selected) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = colorValue,
                                    shadowElevation = if (selected) 4.dp else 1.dp
                                ) {
                                    if (selected) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeColorDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
// AI 配置对话框
    if (showAiConfigDialog) {
AiConfigDialog(
                initialConfig = aiConfig,
                frostedBlurEnabled = frostedBlurEnabled,
                onDismiss = { showAiConfigDialog = false },
            onSave = { provider, apiKey, baseUrl, model ->
                scope.launch {
                    aiPreferences.saveConfig(provider, apiKey, baseUrl, model)
                    showAiConfigDialog = false
                }
            }
        )
    }

    // 动画速度对话框
    if (showAnimationSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showAnimationSpeedDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("动画速度")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "调整页面切换和卡片入场动画的速度",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    ThemePreferences.AnimationSpeed.values().forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        themePreferences.setAnimationSpeed(speed)
                                        showAnimationSpeedDialog = false
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = animationSpeed == speed,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = speed.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (animationSpeed == speed) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    text = when (speed) {
                                        ThemePreferences.AnimationSpeed.ELEGANT -> "优雅柔和，推荐"
                                        ThemePreferences.AnimationSpeed.STANDARD -> "标准速度"
                                        ThemePreferences.AnimationSpeed.SWIFT -> "快速敏捷"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAnimationSpeedDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }

    
    // 关于对话框
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Icon(
                    Icons.Filled.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text(
                    "拾光札记",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "版本 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "一个简洁优雅的日记应用，记录生活中的每一个瞬间。",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp
                    )
                    Text(
                        "✨ 特色功能",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "• 极简设计，专注内容\n• 心情记录与筛选\n• 标签管理\n• 分组归类\n• 收藏功能\n• 日历视图\n• 数据统计",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("知道了")
                }
            }
        )
    }

    // 密码设置对话框
    if (showPasswordDialog) {
        PasswordDialog(
            isEnabled = securityConfig.isEnabled,
            frostedBlurEnabled = frostedBlurEnabled,
            onDismiss = { showPasswordDialog = false },
            onSet = { password ->
                scope.launch {
                    securityPreferences.setPassword(password)
                    showPasswordDialog = false
                }
            },
            onDisable = {
                scope.launch {
                    securityPreferences.clearSecurity()
                    showPasswordDialog = false
                }
            }
        )
    }
}

@Composable
private fun AiConfigDialog(
    initialConfig: AiPreferences.AiConfig,
    frostedBlurEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (AiPreferences.AiProvider, String, String, String) -> Unit
) {
    var provider by remember(initialConfig) { mutableStateOf(initialConfig.provider) }
    var apiKey by remember(initialConfig) { mutableStateOf(initialConfig.apiKey) }
    var baseUrl by remember(initialConfig) { mutableStateOf(initialConfig.baseUrl) }
    var model by remember(initialConfig) { mutableStateOf(initialConfig.model) }

    fun applyProviderPreset(nextProvider: AiPreferences.AiProvider) {
        provider = nextProvider
        when (nextProvider) {
            AiPreferences.AiProvider.DEEPSEEK -> {
                baseUrl = AiPreferences.DEEPSEEK_BASE_URL
                model = AiPreferences.DEEPSEEK_MODEL
            }
            AiPreferences.AiProvider.OPENAI_COMPATIBLE -> {
                baseUrl = AiPreferences.OPENAI_BASE_URL
                model = AiPreferences.OPENAI_MODEL
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
                ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
                Text("AI 模型配置")
            },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "支持 OpenAI 通用兼容接口和 DeepSeek。API Key 仅保存在本机 DataStore 中。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiProviderChip(
                        text = "DeepSeek",
                        selected = provider == AiPreferences.AiProvider.DEEPSEEK,
                        onClick = { applyProviderPreset(AiPreferences.AiProvider.DEEPSEEK) }
                    )
                    AiProviderChip(
                        text = "OpenAI 通用",
                        selected = provider == AiPreferences.AiProvider.OPENAI_COMPATIBLE,
                        onClick = { applyProviderPreset(AiPreferences.AiProvider.OPENAI_COMPATIBLE) }
                    )
                }

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("sk-...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("模型") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onSave(provider, apiKey, baseUrl, model) },
                enabled = apiKey.isNotBlank() && baseUrl.isNotBlank() && model.isNotBlank(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun AiProviderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            if (selected) 0.dp else 1.dp,
            if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onCheckedChange(!checked) },
        shape = shape,
        color = if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isDark) 0.dp else 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            if (isDark) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.20f else 0.32f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.72f else 0.78f)
                )
            }
            Spacer(Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isDark) 0.dp else 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            if (isDark) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.20f else 0.32f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.72f else 0.78f)
                )
            }
            Spacer(Modifier.width(16.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun PasswordDialog(
    isEnabled: Boolean,
    frostedBlurEnabled: Boolean,
    onDismiss: () -> Unit,
    onSet: (String) -> Unit,
    onDisable: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            ApplyFrostedDialogWindow(enabled = frostedBlurEnabled)
            Text(if (isEnabled) "修改密码" else "设置密码")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isEnabled) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; errorMessage = "" },
                        label = { Text("当前密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = "" },
                    label = { Text(if (isEnabled) "新密码" else "密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = "" },
                    label = { Text("确认密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        password.length < 4 -> errorMessage = "密码至少4位"
                        password != confirmPassword -> errorMessage = "两次密码不一致"
                        else -> onSet(password)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            if (isEnabled) {
                TextButton(onClick = onDisable, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("关闭密码")
                }
            }
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
