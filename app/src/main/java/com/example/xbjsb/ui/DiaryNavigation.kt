package com.example.xbjsb.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.xbjsb.ui.screens.DiaryDetailScreen
import com.example.xbjsb.ui.screens.DiaryEditScreen
import com.example.xbjsb.ui.screens.DiaryListScreen
import com.example.xbjsb.ui.screens.CalendarScreen
import com.example.xbjsb.ui.screens.MoodStatsScreen
import com.example.xbjsb.ui.screens.TagManagerScreen
import com.example.xbjsb.ui.theme.PageTransitions

sealed class Screen(val route: String) {
    object List : Screen("list")
    object Edit : Screen("edit/{entryId}") {
        fun createRoute(entryId: Long?) = "edit/${entryId ?: 0}"
    }
    object Detail : Screen("detail/{entryId}") {
        fun createRoute(entryId: Long) = "detail/$entryId"
    }
    object Calendar : Screen("calendar")
    object MoodStats : Screen("mood_stats")
    object TagManager : Screen("tag_manager")
}

@Composable
fun DiaryNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route,
        enterTransition = { PageTransitions.Enter },
        exitTransition = { PageTransitions.Exit },
        popEnterTransition = { PageTransitions.PopEnter },
        popExitTransition = { PageTransitions.PopExit }
    ) {
        // List Screen
        composable(Screen.List.route) {
            DiaryListScreen(
                onNavigateToEdit = { entryId ->
                    navController.navigate(Screen.Edit.createRoute(entryId))
                },
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.Detail.createRoute(entryId))
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.MoodStats.route)
                },
                onNavigateToTags = {
                    navController.navigate(Screen.TagManager.route)
                }
            )
        }
        
        // Calendar Screen
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDateClick = { date ->
                    navController.popBackStack()
                }
            )
        }
        
        // Mood Stats Screen
        composable(Screen.MoodStats.route) {
            MoodStatsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Tag Manager Screen
        composable(Screen.TagManager.route) {
            TagManagerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTagClick = { tag ->
                    navController.popBackStack()
                }
            )
        }
        
        // Edit Screen
        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId")
            DiaryEditScreen(
                entryId = if (entryId == 0L) null else entryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Detail Screen
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            DiaryDetailScreen(
                entryId = entryId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.Edit.createRoute(id))
                }
            )
        }
    }
}