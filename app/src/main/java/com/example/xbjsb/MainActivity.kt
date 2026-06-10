package com.example.xbjsb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.xbjsb.ui.screens.SettingsScreen
import com.example.xbjsb.ui.theme.DiaryTheme
import java.time.LocalDate
import com.example.xbjsb.ui.theme.PageTransitions
import com.example.xbjsb.viewmodel.DiaryViewModel

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
    
    NavHost(
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
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        
        // Settings Screen
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Calendar Screen
        composable("calendar") {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDateClick = { date ->
                    // 点击日期后筛选当天日记并返回列表页
                    // TODO: 传递日期参数给列表页进行筛选
                    navController.popBackStack()
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
}