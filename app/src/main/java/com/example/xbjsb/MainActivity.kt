package com.example.xbjsb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.xbjsb.data.ThemePreferences
import com.example.xbjsb.ui.screens.DiaryDetailScreen
import com.example.xbjsb.ui.screens.DiaryEditScreen
import com.example.xbjsb.ui.screens.DiaryListScreen
import com.example.xbjsb.ui.screens.CalendarScreen
import com.example.xbjsb.ui.screens.MoodStatsScreen
import com.example.xbjsb.ui.screens.TagManagerScreen
import com.example.xbjsb.ui.screens.GroupManagerScreen
import com.example.xbjsb.ui.screens.SettingsScreen
import com.example.xbjsb.ui.screens.RecycleBinScreen
import com.example.xbjsb.ui.screens.PrivateSpaceScreen
import com.example.xbjsb.ui.screens.UnlockScreen
import com.example.xbjsb.ui.components.AppMessageHost
import com.example.xbjsb.ui.components.AppMessageType
import com.example.xbjsb.ui.components.LocalAppMessageHostState
import com.example.xbjsb.ui.components.rememberAppMessageHostState
import com.example.xbjsb.data.security.PrivateAccessManager
import com.example.xbjsb.data.security.SecurityPreferences
import com.example.xbjsb.ui.theme.DiaryTheme
import java.time.LocalDate
import com.example.xbjsb.ui.theme.PageTransitions
import com.example.xbjsb.viewmodel.DiaryViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val context = LocalContext.current
            val themePreferences = ThemePreferences(context)
            val themeMode by themePreferences.themeModeFlow.collectAsState(initial = ThemePreferences.ThemeMode.SYSTEM)
            val themeColor by themePreferences.themeColorFlow.collectAsState(initial = com.example.xbjsb.ui.theme.ThemeColor.ORANGE)
            
            val darkTheme = when (themeMode) {
                ThemePreferences.ThemeMode.LIGHT -> false
                ThemePreferences.ThemeMode.DARK -> true
                ThemePreferences.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            DiaryTheme(darkTheme = darkTheme, themeColor = themeColor) {
                // 在主题内部设置系统栏，确保响应主题变化
                val isDark = !darkTheme
                SideEffect {
                    enableEdgeToEdge(
                        statusBarStyle = if (isDark) {
                            SystemBarStyle.light(
                                scrim = android.graphics.Color.TRANSPARENT,
                                darkScrim = android.graphics.Color.TRANSPARENT
                            )
                        } else {
                            SystemBarStyle.dark(
                                scrim = android.graphics.Color.TRANSPARENT
                            )
                        },
                        navigationBarStyle = if (isDark) {
                            SystemBarStyle.light(
                                scrim = android.graphics.Color.TRANSPARENT,
                                darkScrim = android.graphics.Color.TRANSPARENT
                            )
                        } else {
                            SystemBarStyle.dark(
                                scrim = android.graphics.Color.TRANSPARENT
                            )
                        }
                    )
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiaryApp()
                }
            }
        }
    }
}

@Composable
fun DiaryApp() {
    val navController = rememberNavController()
    val viewModel: DiaryViewModel = viewModel()
    val messageHostState = rememberAppMessageHostState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val securityPreferences = SecurityPreferences(context)
    
    CompositionLocalProvider(LocalAppMessageHostState provides messageHostState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                navController = navController,
                startDestination = "diary_list",
        enterTransition = { PageTransitions.Enter },
        exitTransition = { PageTransitions.Exit },
        popEnterTransition = { PageTransitions.PopEnter },
        popExitTransition = { PageTransitions.PopExit }
    ) {
        // List Screen
        composable("diary_list") {
            DiaryListScreen(
                viewModel = viewModel,
                onNavigateToEdit = { entryId ->
                    if (entryId == null) {
                        navController.navigate("diary_edit/-1")
                    } else {
                        navController.navigate("diary_edit/$entryId")
                    }
                },
                onNavigateToDetail = { entryId ->
                    navController.navigate("diary_detail/$entryId")
                },
                onNavigateToCalendar = {
                    navController.navigate("calendar")
                },
                onNavigateToStats = {
                    navController.navigate("mood_stats")
                },
                onNavigateToTags = {
                    navController.navigate("tag_manager")
                },
                onNavigateToGroups = {
                    scope.launch {
                        val config = securityPreferences.configFlow.first()
                        if (!config.isEnabled) {
                            messageHostState.show("请先设置隐私密码", AppMessageType.Warning)
                            navController.navigate("settings")
                        } else if (PrivateAccessManager.isUnlocked()) {
                            navController.navigate("group_manager")
                        } else {
                            navController.navigate("private_unlock?target=group_manager")
                        }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToRecycleBin = {
                    navController.navigate("recycle_bin")
                },
                onNavigateToPrivateSpace = {
                    scope.launch {
                        val config = securityPreferences.configFlow.first()
                        if (!config.isEnabled) {
                            messageHostState.show("请先设置隐私密码", AppMessageType.Warning)
                            navController.navigate("settings")
                        } else if (PrivateAccessManager.isUnlocked()) {
                            navController.navigate("private_space")
                        } else {
                            navController.navigate("private_unlock?target=private_space")
                        }
                    }
                }
            )
        }
        
        // Settings Screen
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRecycleBin = {
                    navController.navigate("recycle_bin")
                }
            )
        }
        
        // Private Unlock Screen
        composable(
            route = "private_unlock?target={target}",
            arguments = listOf(navArgument("target") { 
                type = NavType.StringType
                defaultValue = "private_space"
            })
        ) { backStackEntry ->
            val target = backStackEntry.arguments?.getString("target") ?: "private_space"
            UnlockScreen(
                title = "私密空间",
                subtitle = "请输入隐私密码",
                onUnlocked = {
                    PrivateAccessManager.unlock()
                    navController.navigate(target) {
                        popUpTo("private_unlock?target={target}") { inclusive = true }
                    }
                }
            )
        }
        
        // Private Space Screen
        composable("private_space") {
            PrivateSpaceScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { entryId -> navController.navigate("diary_detail/$entryId") },
                onNavigateToGroups = { navController.navigate("group_manager") }
            )
        }
        
        // Calendar Screen
        composable("calendar") {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { entryId ->
                    navController.navigate("diary_detail/$entryId")
                }
            )
        }
        
        // Mood Stats Screen
        composable("mood_stats") {
            MoodStatsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Tag Manager Screen
        composable("tag_manager") {
            TagManagerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTagClick = { tag ->
                    // 点击标签后筛选该标签的日记并返回列表页
                    navController.popBackStack()
                }
            )
        }
        
        // Group Manager Screen
        composable("group_manager") {
            GroupManagerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Recycle Bin Screen
composable("recycle_bin") {
    RecycleBinScreen(
        onNavigateBack = {
            navController.popBackStack()
        },
        onNavigateHome = {
            val popped = navController.popBackStack("diary_list", inclusive = false)
            if (!popped) {
                navController.navigate("diary_list") {
                    launchSingleTop = true
                }
            }
        }
    )
}
        
        // Edit Screen
        composable(
            route = "diary_edit/{entryId}",
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            DiaryEditScreen(
                entryId = if (entryId == -1L) null else entryId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Detail Screen
        composable(
            route = "diary_detail/{entryId}",
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            DiaryDetailScreen(
                entryId = entryId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { editEntryId ->
                    navController.navigate("diary_edit/$editEntryId")
                }
            )
        }
            }
            AppMessageHost(hostState = messageHostState)
        }
    }
}