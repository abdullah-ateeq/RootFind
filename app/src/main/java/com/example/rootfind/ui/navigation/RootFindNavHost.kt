package com.example.rootfind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rootfind.ui.about.AboutScreen
import com.example.rootfind.ui.history.HistoryScreen
import com.example.rootfind.ui.home.HomeScreen
import com.example.rootfind.ui.home.RootFindViewModel
import com.example.rootfind.ui.settings.SettingsScreen
import com.example.rootfind.ui.splash.SplashScreen

@Composable
fun RootFindNavHost(
    viewModel: RootFindViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToHistory = { navController.navigate(NavRoutes.HISTORY) },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToAbout = { navController.navigate(NavRoutes.ABOUT) }
            )
        }

        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
