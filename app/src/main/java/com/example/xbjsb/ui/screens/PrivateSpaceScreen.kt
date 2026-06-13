package com.example.xbjsb.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.ui.components.AppMessageType
import com.example.xbjsb.ui.components.DiaryCard
import com.example.xbjsb.ui.components.LocalAppMessageHostState
import com.example.xbjsb.ui.theme.ExitTransitions
import com.example.xbjsb.ui.theme.Spacing
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.ui.components.ApplyFrostedDialogWindow
import com.example.xbjsb.viewmodel.DiaryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrivateSpaceScreen(
    viewModel: DiaryViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToGroups: () -> Unit = {}
) {
    val privateEntries by viewModel.privateEntries.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val frostedBlurEnabled by themePreferences.frostedBlurEnabledFlow.collectAsState(initial = false)
    val privateGroups by viewModel.privateGroups.collectAsState()
    val messageHost = LocalAppMessageHostState.current
    val scope = rememberCoroutineScope()
    var entryToUnprivate by remember { mutableStateOf<DiaryEntry?>(null) }
    var groupPrivateEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    var removingEntryId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("私密空间")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        if (privateEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                    Text(
                        text = "暂无私密日记",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "在编辑页或详情页把日记设为私密后，会集中显示在这里。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                contentPadding = PaddingValues(vertical = Spacing.S),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Text(
                        text = "共 ${privateEntries.size} 篇私密日记",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(privateEntries, key = { it.id }) { entry ->
                    AnimatedVisibility(
                        visible = removingEntryId != entry.id,
                        enter = fadeIn(animationSpec = tween(180)) + scaleIn(
                            initialScale = 0.98f,
                            animationSpec = spring(dampingRatio = 0.86f, stiffness = 300f)
                        ),
                        exit = ExitTransitions.FadeOutShrink,
                        modifier = Modifier.animateItemPlacement(
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = 260f
                            )
                        )
                    ) {
                        Box {
                            val isGroupPrivate = entry.group.isNotBlank() && entry.group in privateGroups
                            val sourceText = when {
                                entry.isPrivate && isGroupPrivate -> "私密 · 私密分组"
                                isGroupPrivate -> "私密分组 · ${entry.group}"
                                else -> "私密"
                            }
                            DiaryCard(
                                entry = entry,
                                onClick = { onNavigateToDetail(entry.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(entry) }
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 20.dp, bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = sourceText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.58f)
                                )
                                IconButton(
                                    onClick = {
                                        if (!entry.isPrivate && isGroupPrivate) groupPrivateEntry = entry else entryToUnprivate = entry
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LockOpen,
                                        contentDescription = "取消私密",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    groupPrivateEntry?.let { target ->
        AlertDialog(
            onDismissRequest = { groupPrivateEntry = null },
            icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            title = { Text("属于私密分组") },
            text = { Text("这篇日记属于私密分组「${target.group}」。如果想让它回到首页，请取消该分组的私密状态。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        groupPrivateEntry = null
                        onNavigateToGroups()
                    }
                ) { Text("管理分组") }
            },
            dismissButton = {
                TextButton(onClick = { groupPrivateEntry = null }) { Text("知道了") }
            }
        )
    }

    entryToUnprivate?.let { target ->
        val alsoGroupPrivate = target.group.isNotBlank() && target.group in privateGroups
        AlertDialog(
            onDismissRequest = { entryToUnprivate = null },
            icon = { Icon(Icons.Filled.LockOpen, contentDescription = null) },
            title = { Text(if (alsoGroupPrivate) "取消单篇私密？" else "取消私密？") },
            text = { Text(if (alsoGroupPrivate) "取消后，这篇日记仍属于私密分组「${target.group}」，所以还会留在私密空间。" else "取消后，这篇日记会重新显示在首页。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        entryToUnprivate = null
                        removingEntryId = target.id
                        scope.launch {
                            delay(260)
                            viewModel.setEntryPrivate(target, false)
                            removingEntryId = null
                            messageHost?.show(if (alsoGroupPrivate) "已取消单篇私密，仍属于私密分组" else "已取消私密", AppMessageType.Success)
                        }
                    }
                ) { Text("取消私密") }
            },
            dismissButton = {
                TextButton(onClick = { entryToUnprivate = null }) { Text("再想想") }
            }
        )
    }
}