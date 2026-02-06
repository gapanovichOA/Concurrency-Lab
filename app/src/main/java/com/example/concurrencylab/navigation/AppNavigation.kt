package com.example.concurrencylab.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.concurrencylab.dashboard.DashboardScreen
import com.example.concurrencylab.racecondition.RaceConditionScreen
import com.example.concurrencylab.racecondition.RaceConditionViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val raceViewModel: RaceConditionViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.RaceCondition.route) {
            RaceConditionScreen(raceViewModel)
        }
    }
}