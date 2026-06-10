package com.example.xbjsb.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.ui.components.DiaryCard
import com.example.xbjsb.ui.components.bounceClick
import com.example.xbjsb.ui.theme.*
import com.example.xbjsb.viewmodel.DiaryViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaryListScreen(
    viewModel: DiaryViewModel = viewModel(),
    onNavigateToEdit: (Long?) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTags: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val entries by viewModel.entries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()
    
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val animationSpeed by themePreferences.animationSpeedFlow.collectAsState(initial = ThemePreferences.AnimationSpeed.ELEGANT)
    val entryCount by viewModel.entryCount.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showMoodFilter by remember { mutableStateOf(false) }
    var showGroupFilter by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    
    // FAB 展开/收起逻辑
    val isScrollingUp by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                true
            } else {
                listState.firstVisibleItemScrollOffset < 100
            }
        }
    }
    
    val fabExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < 2 || isScrollingUp
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "拾光札记",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (entryCount > 0) {
                            Text(
                                text = "共 $entryCount 篇",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.High)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        val searchTint by animateColorAsState(
                            targetValue = if (showSearchBar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth),
                            label = "search_icon_tint"
                        )
                        AnimatedContent(
                            targetState = showSearchBar,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)) +
                                    scaleIn(initialScale = 0.94f, animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit)) +
                                            scaleOut(targetScale = 0.94f, animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit))
                                    )
                            },
                            label = "search_filter_icon"
                        ) { enabled ->
                            Icon(
                                if (enabled) Icons.Filled.SearchOff else Icons.Filled.Search,
                                contentDescription = if (enabled) "关闭搜索" else "搜索",
                                tint = searchTint
                            )
                        }
                    }
                    IconButton(onClick = { showMoodFilter = !showMoodFilter }) {
                        val moodTint by animateColorAsState(
                            targetValue = if (showMoodFilter || selectedMood != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth),
                            label = "mood_filter_tint"
                        )
                        AnimatedContent(
                            targetState = showMoodFilter || selectedMood != null,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)) +
                                    scaleIn(initialScale = 0.94f, animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit)) +
                                            scaleOut(targetScale = 0.94f, animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit))
                                    )
                            },
                            label = "mood_filter_icon"
                        ) { active ->
                            Icon(
                                if (active) Icons.Filled.FilterAlt else Icons.Filled.FilterAltOff,
                                contentDescription = "心情筛选",
                                tint = moodTint
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.toggleFavoritesOnly() }) {
                        AnimatedContent(
                            targetState = showFavoritesOnly,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)) +
                                    scaleIn(initialScale = 0.92f, animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit)) +
                                            scaleOut(targetScale = 0.92f, animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit))
                                    )
                            },
                            label = "favorite_filter_icon"
                        ) { enabled ->
                            Icon(
                                if (enabled) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (enabled) "显示全部" else "只看收藏",
                                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // 更多菜单（移到最右边）
                    Box {
                        IconButton(onClick = { showMoreMenu = !showMoreMenu }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "更多",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            offset = androidx.compose.ui.unit.DpOffset(x = (-8).dp, y = 0.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("日历") },
                                leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
                                onClick = {
                                    showMoreMenu = false
                                    onNavigateToCalendar()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("统计") },
                                leadingIcon = { Icon(Icons.Filled.Analytics, null) },
                                onClick = {
                                    showMoreMenu = false
                                    onNavigateToStats()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("标签") },
                                leadingIcon = { Icon(Icons.Filled.Label, null) },
                                onClick = {
                                    showMoreMenu = false
                                    onNavigateToTags()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("分组筛选") },
                                leadingIcon = { Icon(Icons.Filled.Folder, null) },
                                onClick = {
                                    showMoreMenu = false
                                    showGroupFilter = !showGroupFilter
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("设置") },
                                leadingIcon = { Icon(Icons.Filled.Settings, null) },
                                onClick = {
                                    showMoreMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEdit(null) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = Elevation.Large,
                    pressedElevation = Elevation.XLarge,
                    hoveredElevation = Elevation.XLarge
                ),
                shape = RoundedCornerShape(CornerRadius.Large)
            ) {
                AnimatedContent(
                    targetState = fabExpanded,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(Motion.Standard, easing = MotionEasing.Smooth))
                            .togetherWith(
                                fadeOut(animationSpec = tween(Motion.Exit, easing = MotionEasing.Exit))
                            )
                    },
                    label = "fab_content"
                ) { expanded ->
                    if (expanded) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.M),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.Medium)
                            )
                            Text(
                                "写日记",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(ComponentSize.FABSize)
                                .padding(Spacing.M),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "写日记",
                                modifier = Modifier.size(IconSize.Medium)
                            )
                        }
                    }
                }
            }
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Unified Filter Panel
            AnimatedVisibility(
                visible = showSearchBar || showMoodFilter || showGroupFilter,
                enter = fadeIn(
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = 380f
                    )
                ) + expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(
                        dampingRatio = 0.68f,
                        stiffness = 380f
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                ) + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                )
            ) {
                FilterPanel(
                    showSearch = showSearchBar,
                    showMood = showMoodFilter,
                    showGroup = showGroupFilter,
                    query = searchQuery,
                    selectedMood = selectedMood,
                    selectedGroup = selectedGroup,
                    usedGroups = viewModel.getUsedGroups(),
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onClearQuery = { viewModel.setSearchQuery("") },
                    onMoodSelected = { viewModel.setSelectedMood(it) },
                    onGroupSelected = { viewModel.setSelectedGroup(it) },
                    searchHistory = searchHistory,
                    onRemoveHistory = { viewModel.removeSearchHistory(it) }
                )
            }
            
            // Active Filters Indicator
            AnimatedVisibility(
                visible = (searchQuery.isNotBlank() || selectedMood != null || selectedGroup != null) && !showSearchBar && !showMoodFilter && !showGroupFilter,
                enter = fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
                ) + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
                )
            ) {
                FilterChipRow(
                    searchQuery = searchQuery,
                    selectedMood = selectedMood,
                    selectedGroup = selectedGroup,
                    showFavoritesOnly = false,
                    onClearFilters = { viewModel.clearFilters() }
                )
            }
            
            // Entry List
            if (entries.isEmpty()) {
                EmptyState(
                    hasFilters = searchQuery.isNotBlank() || selectedMood != null || showFavoritesOnly,
                    onCreateFirst = { onNavigateToEdit(null) }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = Spacing.S)
                ) {
                    itemsIndexed(
                        items = entries,
                        key = { _, entry -> entry.id }
                    ) { index, entry ->
                        // Google Keep 风格入场动画：scale + fade
                        var isVisible by remember(entry.id, entries.size) { 
                            mutableStateOf(false) 
                        }
                        
                        LaunchedEffect(entry.id, entries.size) {
                            delay((index.coerceAtMost(8) * 24L) + 32L)
                            isVisible = true
                        }
                        
                        // 根据用户设置动态调整动画参数
                        val (damping, stiffness) = when (animationSpeed) {
                            ThemePreferences.AnimationSpeed.ELEGANT -> 0.75f to 280f
                            ThemePreferences.AnimationSpeed.STANDARD -> 0.68f to 380f
                            ThemePreferences.AnimationSpeed.SWIFT -> 0.85f to 520f
                        }
                        
                        // scale 动画（0.92 → 1.0，参考 Google Keep）
                        val scale by animateFloatAsState(
                            targetValue = if (isVisible) 1f else 0.92f,
                            animationSpec = spring(
                                dampingRatio = damping,
                                stiffness = stiffness
                            ),
                            label = "card_scale"
                        )
                        
                        // alpha 淡入（0 → 1）
                        val alpha by animateFloatAsState(
                            targetValue = if (isVisible) 1f else 0f,
                            animationSpec = tween(durationMillis = 450),
                            label = "card_alpha"
                        )
                        
                        DiaryCard(
                            entry = entry,
                            onClick = { onNavigateToDetail(entry.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(entry) },
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                                .animateItemPlacement(
                                    animationSpec = MotionSpec.offsetSoft()
                                )
                        )
                    }
                    
                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(ComponentSize.FABSize + Spacing.L))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterPanel(
    showSearch: Boolean,
    showMood: Boolean,
    showGroup: Boolean,
    query: String,
    selectedMood: String?,
    selectedGroup: String?,
    usedGroups: List<String>,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onMoodSelected: (String?) -> Unit,
    onGroupSelected: (String?) -> Unit,
    searchHistory: List<String>,
    onRemoveHistory: (String) -> Unit
) {
    val moods = listOf(
        "happy" to "😊 开心",
        "calm" to "😌 平静",
        "excited" to "😆 兴奋",
        "sad" to "😔 难过",
        "neutral" to "😐 一般"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.L, vertical = Spacing.S),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
                AnimatedVisibility(
                    visible = showSearch,
                    enter = EnterTransitions.FadeInExpand,
                    exit = ExitTransitions.FadeOutShrink
                ) {
                    Column {
                        TextField(
                            value = query,
                            onValueChange = onQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("搜索日记...", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = query.isNotBlank(),
                                    enter = fadeIn(animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)) +
                                        scaleIn(initialScale = 0.92f, animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)),
                                    exit = fadeOut(animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit)) +
                                        scaleOut(targetScale = 0.92f, animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit))
                                ) {
                                    IconButton(onClick = onClearQuery) {
                                        Icon(Icons.Filled.Clear, contentDescription = "清除")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )

                        if (query.isBlank() && searchHistory.isNotEmpty()) {
                            Spacer(Modifier.height(Spacing.M))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                                verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                            ) {
                                searchHistory.take(5).forEach { historyQuery ->
                                    AssistChip(
                                        onClick = { onQueryChange(historyQuery) },
                                        label = { Text(historyQuery, style = MaterialTheme.typography.labelSmall) },
                                        leadingIcon = {
                                            Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { onRemoveHistory(historyQuery) },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(Icons.Filled.Close, contentDescription = "删除", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (showSearch && showMood) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.S),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                    )
                }

                AnimatedVisibility(
                    visible = showMood,
                    enter = EnterTransitions.FadeInExpand,
                    exit = ExitTransitions.FadeOutShrink
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.S)
                    ) {
                        Text(
                            text = "按心情筛选",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            modifier = Modifier.padding(horizontal = Spacing.S)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                            verticalArrangement = Arrangement.spacedBy(Spacing.S)
                        ) {
                            moods.forEach { (moodKey, moodLabel) ->
                                val isSelected = selectedMood == moodKey
                                Surface(
                                    modifier = Modifier.bounceClick {
                                        onMoodSelected(if (isSelected) null else moodKey)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = when (moodKey) {
                                        "happy" -> if (isSelected) MoodColors.happy else MoodColors.happy.copy(alpha = 0.15f)
                                        "calm" -> if (isSelected) MoodColors.calm else MoodColors.calm.copy(alpha = 0.15f)
                                        "excited" -> if (isSelected) MoodColors.excited else MoodColors.excited.copy(alpha = 0.15f)
                                        "sad" -> if (isSelected) MoodColors.sad else MoodColors.sad.copy(alpha = 0.15f)
                                        "neutral" -> if (isSelected) MoodColors.neutral else MoodColors.neutral.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    border = if (isSelected) BorderStroke(
                                        2.dp,
                                        when (moodKey) {
                                            "happy" -> MoodColors.happy
                                            "calm" -> MoodColors.calm
                                            "excited" -> MoodColors.excited
                                            "sad" -> MoodColors.sad
                                            "neutral" -> MoodColors.neutral
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    ) else null
                                ) {
                                    Text(
                                        text = moodLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) {
                                            contentColorFor(
                                                when (moodKey) {
                                                    "happy" -> MoodColors.happy
                                                    "calm" -> MoodColors.calm
                                                    "excited" -> MoodColors.excited
                                                    "sad" -> MoodColors.sad
                                                    "neutral" -> MoodColors.neutral
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            )
                                        } else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (showMood && showGroup) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.S),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                    )
                }

                AnimatedVisibility(
                    visible = showGroup,
                    enter = EnterTransitions.FadeInExpand,
                    exit = ExitTransitions.FadeOutShrink
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.S)
                    ) {
                        Text(
                            text = "按分组筛选",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            modifier = Modifier.padding(horizontal = Spacing.S)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                            verticalArrangement = Arrangement.spacedBy(Spacing.S)
                        ) {
                            if (usedGroups.isEmpty()) {
                                Text(
                                    text = "暂无分组，在编辑日记时添加分组",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            } else {
                                usedGroups.forEach { group ->
                                    FilterChip(
                                        selected = selectedGroup == group,
                                        onClick = { onGroupSelected(if (selectedGroup == group) null else group) },
                                        label = { Text(group) },
                                        shape = RoundedCornerShape(CornerRadius.Full),
                                        leadingIcon = if (selectedGroup == group) {
                                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(IconSize.Small)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selectedGroup == group,
                                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
                                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipRow(
    searchQuery: String,
    selectedMood: String?,
    selectedGroup: String?,
    showFavoritesOnly: Boolean,
    onClearFilters: () -> Unit
) {
    if (searchQuery.isBlank() && selectedMood == null && selectedGroup == null && !showFavoritesOnly) return
    
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f)
    val iconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.68f)
    val trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.L, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
        ) {
            if (searchQuery.isNotBlank()) {
                AssistChip(
                    onClick = onClearFilters,
                    label = { 
                        Text(
                            "\"$searchQuery\"",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "清除",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(CornerRadius.Full),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = containerColor,
                        labelColor = labelColor,
                        leadingIconContentColor = iconColor,
                        trailingIconContentColor = trailingIconColor
                    ),
                    border = BorderStroke(1.dp, borderColor)
                )
            }
            if (selectedMood != null) {
                AssistChip(
                    onClick = onClearFilters,
                    label = { 
                        Text(
                            "心情",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Mood,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "清除",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(CornerRadius.Full),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = containerColor,
                        labelColor = labelColor,
                        leadingIconContentColor = iconColor,
                        trailingIconContentColor = trailingIconColor
                    ),
                    border = BorderStroke(1.dp, borderColor)
                )
            }
            if (selectedGroup != null) {
                AssistChip(
                    onClick = onClearFilters,
                    label = { 
                        Text(
                            selectedGroup,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "清除",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(CornerRadius.Full),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = containerColor,
                        labelColor = labelColor,
                        leadingIconContentColor = iconColor,
                        trailingIconContentColor = trailingIconColor
                    ),
                    border = BorderStroke(1.dp, borderColor)
                )
            }
            if (showFavoritesOnly) {
                AssistChip(
                    onClick = onClearFilters,
                    label = { 
                        Text(
                            "收藏",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "清除",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(CornerRadius.Full),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = containerColor,
                        labelColor = labelColor,
                        leadingIconContentColor = iconColor,
                        trailingIconContentColor = trailingIconColor
                    ),
                    border = BorderStroke(1.dp, borderColor)
                )
            }
        }
    }
}
@Composable
fun EmptyState(
    hasFilters: Boolean,
    onCreateFirst: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.XL),
            modifier = Modifier.padding(Spacing.XXL)
        ) {
            Box(
                modifier = Modifier.size(IconSize.XXLarge * 1.35f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasFilters) Icons.Filled.SearchOff else Icons.Filled.EditNote,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.M))
            
            // 标题
            Text(
                text = if (hasFilters) "没有找到匹配的日记" else "还没有日记",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // 副标题
            Text(
                text = if (hasFilters) "试试调整筛选条件" else "点击下方按钮\n开始记录你的生活吧",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.High),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
            )
            
            // 创建按钮（仅在无筛选时显示）
            if (!hasFilters) {
                Spacer(modifier = Modifier.height(Spacing.L))
                
                Button(
                    onClick = onCreateFirst,
                    modifier = Modifier
                        .padding(top = Spacing.M)
                        .height(ComponentSize.ButtonHeightLarge),
                    shape = RoundedCornerShape(CornerRadius.Large),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = Elevation.Medium,
                        pressedElevation = Elevation.Small
                    )
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.Medium)
                    )
                    Spacer(modifier = Modifier.width(Spacing.S))
                    Text(
                        "写第一篇日记",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
