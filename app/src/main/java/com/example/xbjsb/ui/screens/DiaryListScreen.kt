package com.example.xbjsb.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xbjsb.data.DiaryEntry
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.ui.components.DiaryCard
import com.example.xbjsb.ui.components.bounceClick
import com.example.xbjsb.ui.theme.*
import com.example.xbjsb.viewmodel.DiaryViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaryListScreen(
viewModel: DiaryViewModel = viewModel(),
onNavigateToEdit: (Long?) -> Unit,
onNavigateToDetail: (Long) -> Unit,
onNavigateToCalendar: () -> Unit,
onNavigateToStats: () -> Unit,
onNavigateToTags: () -> Unit = {},
onNavigateToGroups: () -> Unit = {},
onNavigateToSettings: () -> Unit = {},
onNavigateToRecycleBin: () -> Unit = {},
onNavigateToPrivateSpace: () -> Unit = {}
) {
    val entries by viewModel.entries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themePreferences = remember { ThemePreferences(context) }
    val animationSpeed by themePreferences.animationSpeedFlow.collectAsState(initial = ThemePreferences.AnimationSpeed.ELEGANT)
    val entryCount by viewModel.entryCount.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showMoodFilter by remember { mutableStateOf(false) }
    var showGroupFilter by remember { mutableStateOf(false) }
    var showTagFilter by remember { mutableStateOf(false) }
    var showDateFilter by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var moreMenuVisible by remember { mutableStateOf(false) }
    var moreMenuClosing by remember { mutableStateOf(false) }
    
    LaunchedEffect(moreMenuClosing, moreMenuVisible) {
        if (moreMenuClosing && !moreMenuVisible) {
            delay(120)
            showMoreMenu = false
            moreMenuClosing = false
        }
    }
    
    fun closeMenuAndNavigate(action: () -> Unit) {
        if (!showMoreMenu) return
        moreMenuClosing = true
        moreMenuVisible = false
        scope.launch {
            delay(150)
            action()
        }
    }
    
    val listState = rememberLazyListState()
    val isFilterPanelVisible = showSearchBar || showMoodFilter || showGroupFilter || showTagFilter
    var lastVisibleShowSearch by remember { mutableStateOf(false) }
    var lastVisibleShowMood by remember { mutableStateOf(false) }
    var lastVisibleShowGroup by remember { mutableStateOf(false) }
    var lastVisibleShowTag by remember { mutableStateOf(false) }
    var lastVisibleShowDate by remember { mutableStateOf(false) }
    if (isFilterPanelVisible) {
        SideEffect {
            lastVisibleShowSearch = showSearchBar
            lastVisibleShowMood = showMoodFilter
            lastVisibleShowGroup = showGroupFilter
            lastVisibleShowTag = showTagFilter
            lastVisibleShowDate = showDateFilter
        }
    }
    val panelContentShowSearch = if (isFilterPanelVisible) showSearchBar else lastVisibleShowSearch
    val panelContentShowMood = if (isFilterPanelVisible) showMoodFilter else lastVisibleShowMood
    val panelContentShowGroup = if (isFilterPanelVisible) showGroupFilter else lastVisibleShowGroup
    val panelContentShowTag = if (isFilterPanelVisible) showTagFilter else lastVisibleShowTag
    val panelContentShowDate = if (isFilterPanelVisible) showDateFilter else lastVisibleShowDate
    val filterPanelEnterStiffness: Float
    val filterPanelEnterDamping: Float
    val filterPanelExitDuration: Int
    val listReflowDamping: Float
    val listReflowStiffness: Float
    val filterCollapseEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    when (animationSpeed) {
        ThemePreferences.AnimationSpeed.ELEGANT -> {
            filterPanelEnterStiffness = 190f
            filterPanelEnterDamping = 0.90f
            filterPanelExitDuration = 420
            listReflowDamping = 0.92f
            listReflowStiffness = 230f
        }
        ThemePreferences.AnimationSpeed.STANDARD -> {
            filterPanelEnterStiffness = 270f
            filterPanelEnterDamping = 0.88f
            filterPanelExitDuration = 380
            listReflowDamping = 0.94f
            listReflowStiffness = 310f
        }
        ThemePreferences.AnimationSpeed.SWIFT -> {
            filterPanelEnterStiffness = 360f
            filterPanelEnterDamping = 0.88f
            filterPanelExitDuration = 300
            listReflowDamping = 0.96f
            listReflowStiffness = 400f
        }
    }
    val listReflowSpec = remember(animationSpeed) {
        spring<IntOffset>(
            dampingRatio = listReflowDamping,
            stiffness = listReflowStiffness
        )
    }
    
    
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
                    IconButton(onClick = {
                        val shouldShowFilters = !(showMoodFilter || showTagFilter || showGroupFilter)
                        showDateFilter = false
                        showMoodFilter = shouldShowFilters
                        showTagFilter = shouldShowFilters
                        showGroupFilter = shouldShowFilters
                    }) {
                        val hasActiveFilters = selectedMood != null || selectedGroup != null || selectedTags.isNotEmpty() || selectedDateMillis != null
                        val filtersExpanded = showDateFilter || showMoodFilter || showTagFilter || showGroupFilter
                        val filterTint by animateColorAsState(
                            targetValue = if (filtersExpanded || hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth),
                            label = "filter_tint"
                        )
                        AnimatedContent(
                            targetState = filtersExpanded || hasActiveFilters,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)) +
                                    scaleIn(initialScale = 0.94f, animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit)) +
                                            scaleOut(targetScale = 0.94f, animationSpec = tween(Motion.Micro, easing = MotionEasing.Exit))
                                    )
                            },
                            label = "filter_icon"
                        ) { active ->
                            Icon(
                                if (active) Icons.Filled.FilterAlt else Icons.Filled.FilterAltOff,
                                contentDescription = if (filtersExpanded) "收起筛选" else "筛选",
                                tint = filterTint
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
                        IconButton(onClick = {
                            if (showMoreMenu) {
                                moreMenuClosing = true
                                moreMenuVisible = false
                            } else {
                                moreMenuClosing = false
                                moreMenuVisible = false
                                showMoreMenu = true
                            }
                        }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "更多",
                                tint = if (showMoreMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (showMoreMenu) {
                            Popup(
                                alignment = Alignment.TopEnd,
                                onDismissRequest = {
                                    moreMenuClosing = true
                                    moreMenuVisible = false
                                },
                                properties = PopupProperties(focusable = true)
                            ) {
                                LaunchedEffect(Unit) {
                                    delay(16)
                                    moreMenuVisible = true
                                }
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = moreMenuVisible,
                                    enter = fadeIn(animationSpec = tween(120)) + scaleIn(
                                        initialScale = 0.96f,
                                        animationSpec = tween(160, easing = FastOutSlowInEasing)
                                    ),
                                    exit = fadeOut(animationSpec = tween(90)) + scaleOut(
                                        targetScale = 0.97f,
                                        animationSpec = tween(120, easing = FastOutLinearInEasing)
                                    )
                                ) {
                                    Surface(
                                        modifier = Modifier.padding(top = 46.dp, end = 12.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 6.dp,
                                        shadowElevation = 12.dp,
                                        border = BorderStroke(
                                            0.6.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)
                                        )
                                    ) {
                                    Column(
                                        modifier = Modifier
                                            .width(172.dp)
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        MoreMenuSection("查看")
                                        MoreMenuAction(
                                            icon = Icons.Filled.CalendarMonth,
                                            text = "日历",
                                            onClick = { closeMenuAndNavigate { onNavigateToCalendar() } }
                                        )
                                        MoreMenuAction(
                                            icon = Icons.Filled.Analytics,
                                            text = "统计",
                                            onClick = { closeMenuAndNavigate { onNavigateToStats() } }
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)
                                        )
                                        MoreMenuSection("管理")
                                        MoreMenuAction(
                                            icon = Icons.Filled.Label,
                                            text = "标签管理",
                                            onClick = { closeMenuAndNavigate { onNavigateToTags() } }
                                        )
                                        MoreMenuAction(
                                            icon = Icons.Filled.Folder,
                                            text = "分组管理",
                                            onClick = { closeMenuAndNavigate { onNavigateToGroups() } }
                                        )
                                        MoreMenuAction(
                                            icon = Icons.Filled.Lock,
                                            text = "私密空间",
                                            onClick = { closeMenuAndNavigate { onNavigateToPrivateSpace() } }
                                        )
                                        MoreMenuAction(
                                            icon = Icons.Filled.DeleteOutline,
                                            text = "回收站",
                                            onClick = { closeMenuAndNavigate { onNavigateToRecycleBin() } }
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)
                                        )
                                        MoreMenuSection("系统")
                                        MoreMenuAction(
                                            icon = Icons.Filled.Settings,
                                            text = "设置",
                                            onClick = { closeMenuAndNavigate { onNavigateToSettings() } }
                                        )
                                    }
                                }
                            }
                        }
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // 真正丝滑版：过滤区、已选筛选条和日记卡片全部放进同一个 LazyColumn。
        // 这样过滤区高度变化时，卡片作为列表 item 自然重排，不再由外层 Column 整体顶动 LazyColumn。
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = if (entries.isEmpty()) 0.dp else Spacing.S)
        ) {
            item(key = "filter_panel", contentType = "filter_panel") {
                AnimatedVisibility(
                    visible = isFilterPanelVisible,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = filterPanelExitDuration, easing = filterCollapseEasing)
                    ) + expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(durationMillis = filterPanelExitDuration, easing = filterCollapseEasing)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = filterPanelExitDuration, easing = filterCollapseEasing)
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(durationMillis = filterPanelExitDuration, easing = filterCollapseEasing)
                    )
                ) {
                    FilterPanel(
                        showSearch = panelContentShowSearch,
                        showMood = panelContentShowMood,
                        showGroup = panelContentShowGroup,
                        showTag = panelContentShowTag,
                        showDate = false,
                        query = searchQuery,
                        selectedMood = selectedMood,
                        selectedGroup = selectedGroup,
                        selectedTags = selectedTags,
                        selectedDateMillis = selectedDateMillis,
                        usedGroups = viewModel.getUsedGroups(),
                        usedTags = viewModel.getUsedTags(),
                        usedDates = viewModel.getUsedDates(),
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onClearQuery = { viewModel.setSearchQuery("") },
                        onMoodSelected = { viewModel.setSelectedMood(it) },
                        onGroupSelected = { viewModel.setSelectedGroup(it) },
                        onTagToggled = { viewModel.toggleSelectedTag(it) },
                        onDateSelected = { viewModel.toggleSelectedDate(it) },
                        searchHistory = searchHistory,
                        onRemoveHistory = { viewModel.removeSearchHistory(it) },
                        animationSpeed = animationSpeed
                    )
                }
            }

            item(key = "active_filters", contentType = "active_filters") {
                AnimatedVisibility(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = listReflowSpec
                    ),
                    visible = (searchQuery.isNotBlank() || selectedMood != null || selectedGroup != null || selectedTags.isNotEmpty() || selectedDateMillis != null) && !showSearchBar && !showMoodFilter && !showGroupFilter && !showTagFilter && !showDateFilter,
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
                        animationSpec = tween(durationMillis = 170, easing = filterCollapseEasing)
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(durationMillis = 170, easing = filterCollapseEasing)
                    )
                ) {
                    FilterChipRow(
                        searchQuery = searchQuery,
                        selectedMood = selectedMood,
                        selectedGroup = selectedGroup,
                        selectedTags = selectedTags,
                        selectedDateMillis = selectedDateMillis,
                        showFavoritesOnly = false,
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }
            }

            if (entries.isEmpty()) {
                item(key = "empty_state", contentType = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .animateItemPlacement(
                                animationSpec = listReflowSpec
                            )
                    ) {
                        EmptyState(
                            hasFilters = searchQuery.isNotBlank() || selectedMood != null || selectedGroup != null || selectedTags.isNotEmpty() || selectedDateMillis != null || showFavoritesOnly,
                            onClearFilters = { viewModel.clearFilters() },
                            onCreateFirst = { onNavigateToEdit(null) }
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = entries,
                    key = { _, entry -> entry.id },
                    contentType = { _, _ -> "diary_card" }
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
                                animationSpec = listReflowSpec
                            )
                    )
                }
            }

            // Bottom spacing for FAB：只有有日记卡片时才需要，避免空首页也能继续下滑
            if (entries.isNotEmpty()) {
                item(key = "fab_bottom_spacing", contentType = "spacing") {
                    Spacer(modifier = Modifier.height(ComponentSize.FABSize + Spacing.L))
                }
            }
        }
    }
}

private data class FilterPanelMotion(
    val enterFadeDamping: Float,
    val enterFadeStiffness: Float,
    val enterExpandDamping: Float,
    val enterExpandStiffness: Float,
    val exitFadeDuration: Int,
    val exitShrinkDuration: Int,
    val exitEasing: androidx.compose.animation.core.Easing
)

private data class FilterPanelState(
    val search: Boolean,
    val mood: Boolean,
    val group: Boolean,
    val tag: Boolean,
    val date: Boolean
) {
    val isVisible: Boolean get() = search || mood || group || tag || date

    fun include(other: FilterPanelState) = FilterPanelState(
        search = search || other.search,
        mood = mood || other.mood,
        group = group || other.group,
        tag = tag || other.tag,
        date = date || other.date
    )
}

@Composable
private fun MoreMenuSection(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 3.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f)
    )
}

@Composable
private fun MoreMenuAction(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FilterAnimatedGap(
    visible: Boolean,
    height: androidx.compose.ui.unit.Dp,
    durationMillis: Int,
    easing: androidx.compose.animation.core.Easing
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis, easing = easing),
        label = "filter_gap_progress"
    )
    Spacer(modifier = Modifier.height(height * progress))
}

@Composable
private fun FilterSectionSlot(
    visible: Boolean,
    modifier: Modifier = Modifier,
    fadeDuration: Int,
    easing: androidx.compose.animation.core.Easing,
    content: @Composable () -> Unit
) {
    var mounted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mounted = true }

    val progress by animateFloatAsState(
        targetValue = if (visible && mounted) 1f else 0f,
        animationSpec = tween(durationMillis = fadeDuration, easing = easing),
        label = "filter_section_progress"
    )

    Layout(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        content = {
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = progress
                    translationY = (1f - progress) * -6f
                }
            ) {
                content()
            }
        }
    ) { measurables, constraints ->
        val placeable = measurables.firstOrNull()?.measure(constraints.copy(minHeight = 0))
        val fullHeight = placeable?.height ?: 0
        val animatedHeight = (fullHeight * progress).roundToInt().coerceAtLeast(0)

        layout(constraints.maxWidth, animatedHeight) {
            placeable?.placeRelative(0, 0)
        }
    }
}

@Composable
private fun FilterDivider(
    visible: Boolean,
    fadeDuration: Int,
    easing: androidx.compose.animation.core.Easing
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = fadeDuration, easing = easing),
        label = "filter_divider_progress"
    )

    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds(),
        content = {
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = Spacing.S)
                    .graphicsLayer { alpha = progress },
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
            )
        }
    ) { measurables, constraints ->
        val placeable = measurables.firstOrNull()?.measure(constraints.copy(minHeight = 0))
        val fullHeight = placeable?.height ?: 0
        val animatedHeight = (fullHeight * progress).roundToInt().coerceAtLeast(0)

        layout(constraints.maxWidth, animatedHeight) {
            placeable?.placeRelative(0, 0)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterPanel(
    showSearch: Boolean,
    showMood: Boolean,
    showGroup: Boolean,
    showTag: Boolean,
    showDate: Boolean,
    query: String,
    selectedMood: String?,
    selectedGroup: String?,
    selectedTags: Set<String>,
    selectedDateMillis: Long?,
    usedGroups: List<String>,
    usedTags: List<String>,
    usedDates: List<Long>,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onMoodSelected: (String?) -> Unit,
    onGroupSelected: (String?) -> Unit,
    onTagToggled: (String) -> Unit,
    onDateSelected: (Long) -> Unit,
    searchHistory: List<String>,
    onRemoveHistory: (String) -> Unit,
    animationSpeed: ThemePreferences.AnimationSpeed
) {
    val moods = listOf(
        "happy" to "😊 开心",
        "calm" to "😌 平静",
        "excited" to "😆 兴奋",
        "sad" to "😔 难过",
        "neutral" to "😐 一般"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val fallbackMoodColor = MaterialTheme.colorScheme.surfaceVariant
    val compactDateFormatter = remember { SimpleDateFormat("M月d日", Locale.getDefault()) }

    fun moodColor(moodKey: String) = when (moodKey) {
        "happy" -> if (isDark) MoodColors.happyDark else MoodColors.happy
        "calm" -> if (isDark) MoodColors.calmDark else MoodColors.calm
        "excited" -> if (isDark) MoodColors.excitedDark else MoodColors.excited
        "sad" -> if (isDark) MoodColors.sadDark else MoodColors.sad
        "neutral" -> if (isDark) MoodColors.neutralDark else MoodColors.neutral
        else -> fallbackMoodColor
    }

    // 叠加过滤区采用“单一主物理系统”：
    // 主 Column 统一负责整体高度变化；子区域只做透明度 + 高度展开/收缩，
    // 不做独立滑动/缩放，避免叠加态各块抢节奏；同时避免纯淡出导致高度最后一帧才释放。
    // 同时接入用户的“动画速度”设置：优雅档慢半拍、更柔和、收尾更稳；迅速档保持干脆但不生硬。
    val filterMotion = remember(animationSpeed) {
        when (animationSpeed) {
            ThemePreferences.AnimationSpeed.ELEGANT -> FilterPanelMotion(
                enterFadeDamping = 0.94f,
                enterFadeStiffness = 230f,
                enterExpandDamping = 0.92f,
                enterExpandStiffness = 220f,
                exitFadeDuration = 360,
                exitShrinkDuration = 420,
                exitEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
            )
            ThemePreferences.AnimationSpeed.STANDARD -> FilterPanelMotion(
                enterFadeDamping = 0.90f,
                enterFadeStiffness = 320f,
                enterExpandDamping = 0.88f,
                enterExpandStiffness = 300f,
                exitFadeDuration = 320,
                exitShrinkDuration = 380,
                exitEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
            )
            ThemePreferences.AnimationSpeed.SWIFT -> FilterPanelMotion(
                enterFadeDamping = 0.90f,
                enterFadeStiffness = 420f,
                enterExpandDamping = 0.88f,
                enterExpandStiffness = 400f,
                exitFadeDuration = 240,
                exitShrinkDuration = 300,
                exitEasing = CubicBezierEasing(0.24f, 0f, 0.12f, 1f)
            )
        }
    }

    val targetState = remember(showSearch, showMood, showGroup, showTag, showDate) {
        FilterPanelState(
            search = showSearch,
            mood = showMood,
            group = showGroup,
            tag = showTag,
            date = showDate
        )
    }
    var renderState by remember { mutableStateOf(targetState) }

    LaunchedEffect(targetState, filterMotion.exitShrinkDuration) {
        val expandedRenderState = renderState.include(targetState)
        renderState = expandedRenderState
        if (expandedRenderState != targetState) {
            delay(filterMotion.exitShrinkDuration.toLong())
            renderState = targetState
        }
    }

    // 分隔线不再单独持有布局动画生命周期，统一交给父 Column 的 animateContentSize 处理。

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
            verticalArrangement = Arrangement.Top
        ) {
                if (renderState.search) {
                    FilterSectionSlot(
                        visible = targetState.search,
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
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
            }

                if (renderState.search && (renderState.date || renderState.mood || renderState.tag || renderState.group)) {
                    FilterAnimatedGap(
                        visible = targetState.search && (targetState.date || targetState.mood || targetState.tag || targetState.group),
                        height = Spacing.M,
                        durationMillis = filterMotion.exitShrinkDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.search && (renderState.date || renderState.mood || renderState.tag || renderState.group)) {
                    FilterDivider(
                        visible = targetState.search && (targetState.date || targetState.mood || targetState.tag || targetState.group),
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.search && (renderState.date || renderState.mood || renderState.tag || renderState.group)) {
                    FilterAnimatedGap(
                        visible = targetState.search && (targetState.date || targetState.mood || targetState.tag || targetState.group),
                        height = Spacing.M,
                        durationMillis = filterMotion.exitShrinkDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.date) {
                    FilterSectionSlot(
                        visible = targetState.date,
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.S)
                        ) {
                            Text(
                                text = "只看某一天",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                modifier = Modifier.padding(horizontal = Spacing.S)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                                verticalArrangement = Arrangement.spacedBy(Spacing.S)
                            ) {
                                if (usedDates.isEmpty()) {
                                    Text(
                                        text = "还没有可选日期，写下日记后就能按天查看",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                } else {
                                    usedDates.take(10).forEach { dateMillis ->
                                        val selected = selectedDateMillis == dateMillis
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onDateSelected(dateMillis) },
                                            label = { Text(compactDateFormatter.format(Date(dateMillis))) },
                                            shape = RoundedCornerShape(CornerRadius.Full),
                                            leadingIcon = if (selected) {
                                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(IconSize.Small)) }
                                            } else {
                                                { Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(IconSize.Small)) }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                iconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.46f),
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = selected,
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

                if (renderState.date && (renderState.mood || renderState.tag || renderState.group)) {
                    FilterAnimatedGap(
                        visible = targetState.date && (targetState.mood || targetState.tag || targetState.group),
                        height = Spacing.M,
                        durationMillis = filterMotion.exitShrinkDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.date && (renderState.mood || renderState.tag || renderState.group)) {
                    FilterDivider(
                        visible = targetState.date && (targetState.mood || targetState.tag || targetState.group),
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.date && (renderState.mood || renderState.tag || renderState.group)) {
                    FilterAnimatedGap(
                        visible = targetState.date && (targetState.mood || targetState.tag || targetState.group),
                        height = Spacing.M,
                        durationMillis = filterMotion.exitShrinkDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.mood) {
                    FilterSectionSlot(
                        visible = targetState.mood,
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
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
                                val moodBaseColor = moodColor(moodKey)
                                val chipContainerColor by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        moodBaseColor
                                    } else {
                                        moodBaseColor.copy(alpha = if (isDark) 0.34f else 0.18f)
                                    },
                                    animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth),
                                    label = "mood_chip_container_$moodKey"
                                )
                                val chipBorderWidth by animateDpAsState(
                                    targetValue = if (isSelected) 2.dp else 1.dp,
                                    animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth),
                                    label = "mood_chip_border_width_$moodKey"
                                )
                                val chipBorderColor by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        contentColorFor(moodBaseColor).copy(alpha = 0.78f)
                                    } else {
                                        moodBaseColor.copy(alpha = if (isDark) 0.46f else 0.28f)
                                    },
                                    animationSpec = tween(Motion.Fast, easing = MotionEasing.Smooth),
                                    label = "mood_chip_border_$moodKey"
                                )
                                Surface(
                                    modifier = Modifier.bounceClick {
                                        onMoodSelected(if (isSelected) null else moodKey)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = chipContainerColor,
                                    border = BorderStroke(chipBorderWidth, chipBorderColor)
                                ) {
                                    Text(
                                        text = moodLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) {
                                            contentColorFor(moodColor(moodKey))
                                        } else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

                if (renderState.mood && (renderState.tag || renderState.group)) {
                    FilterAnimatedGap(
                        visible = targetState.mood && (targetState.tag || targetState.group),
                        height = Spacing.M,
                        durationMillis = filterMotion.exitShrinkDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.mood && (renderState.tag || renderState.group)) {
                    FilterDivider(
                        visible = targetState.mood && (targetState.tag || targetState.group),
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.mood && (renderState.tag || renderState.group)) {
                    FilterAnimatedGap(
                        visible = targetState.mood && (targetState.tag || targetState.group),
                        height = Spacing.S,
                        durationMillis = filterMotion.exitShrinkDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.tag) {
                    FilterSectionSlot(
                        visible = targetState.tag,
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.S)
                        ) {
                            Text(
                                text = "选择标签",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                modifier = Modifier.padding(horizontal = Spacing.S)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                                verticalArrangement = Arrangement.spacedBy(Spacing.S)
                            ) {
                                if (usedTags.isEmpty()) {
                                    FilterEmptyHint(
                                        icon = Icons.Filled.Label,
                                        title = "还没有标签",
                                        message = "写日记时添加标签后，就能在这里快速筛选。"
                                    )
                                } else {
                                    usedTags.forEach { tag ->
                                        val selected = tag in selectedTags
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onTagToggled(tag) },
                                            label = { Text(tag) },
                                            shape = RoundedCornerShape(CornerRadius.Full),
                                            leadingIcon = if (selected) {
                                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(IconSize.Small)) }
                                            } else {
                                                { Icon(Icons.Filled.Label, contentDescription = null, modifier = Modifier.size(IconSize.Small)) }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                iconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.46f),
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = selected,
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

                if (renderState.tag && renderState.group) {
                    FilterAnimatedGap(
                        visible = targetState.tag && targetState.group,
                        height = Spacing.M,
                        durationMillis = filterMotion.exitShrinkDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.tag && renderState.group) {
                    FilterDivider(
                        visible = targetState.tag && targetState.group,
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.tag && renderState.group) {
                    FilterAnimatedGap(
                        visible = targetState.tag && targetState.group,
                        height = Spacing.S,
                        durationMillis = filterMotion.exitShrinkDuration,
                        easing = filterMotion.exitEasing
                    )
                }

                if (renderState.group) {
                    FilterSectionSlot(
                        visible = targetState.group,
                        fadeDuration = filterMotion.exitFadeDuration,
                        easing = filterMotion.exitEasing
                    ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.S)
                    ) {
                        Text(
                            text = "选择分组",
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
                                FilterEmptyHint(
                                    icon = Icons.Filled.Folder,
                                    title = "还没有分组",
                                    message = "给日记选择分组后，这里会显示可筛选的分组。"
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
}

@Composable
private fun FilterEmptyHint(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.68f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                )
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
    selectedTags: Set<String>,
    selectedDateMillis: Long?,
    showFavoritesOnly: Boolean,
    onClearFilters: () -> Unit
) {
    if (searchQuery.isBlank() && selectedMood == null && selectedGroup == null && selectedTags.isEmpty() && selectedDateMillis == null && !showFavoritesOnly) return
    
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f)
    val iconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.68f)
    val trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
    val compactDateFormatter = remember { SimpleDateFormat("M月d日", Locale.getDefault()) }
    
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
            if (selectedTags.isNotEmpty()) {
                AssistChip(
                    onClick = onClearFilters,
                    label = {
                        Text(
                            selectedTags.joinToString("、"),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Label,
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
            if (selectedDateMillis != null) {
                AssistChip(
                    onClick = onClearFilters,
                    label = {
                        Text(
                            compactDateFormatter.format(Date(selectedDateMillis)),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Event,
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
    onClearFilters: () -> Unit,
    onCreateFirst: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
            
            // 操作按钮
            Spacer(modifier = Modifier.height(Spacing.L))
            
            Button(
                onClick = if (hasFilters) onClearFilters else onCreateFirst,
                modifier = Modifier
                    .padding(top = Spacing.M)
                    .height(ComponentSize.ButtonHeightLarge),
                shape = RoundedCornerShape(CornerRadius.Large),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasFilters) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (hasFilters) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (hasFilters) Elevation.Small else Elevation.Medium,
                    pressedElevation = Elevation.Small
                )
            ) {
                Icon(
                    if (hasFilters) Icons.Filled.FilterAltOff else Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.Medium)
                )
                Spacer(modifier = Modifier.width(Spacing.S))
                Text(
                    if (hasFilters) "清除筛选" else "写第一篇日记",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
