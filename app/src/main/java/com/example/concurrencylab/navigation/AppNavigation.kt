package com.example.concurrencylab.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.concurrencylab.blockingtrap.BlockingTrapScreen
import com.example.concurrencylab.blockingtrap.BlockingTrapViewModel
import com.example.concurrencylab.callbackbridge.CallbackBridgeScreen
import com.example.concurrencylab.callbackbridge.CallbackBridgeViewModel
import com.example.concurrencylab.cooperation.CooperationScreen
import com.example.concurrencylab.cooperation.CooperationViewModel
import com.example.concurrencylab.dashboard.DashboardScreen
import com.example.concurrencylab.racecondition.RaceConditionScreen
import com.example.concurrencylab.racecondition.RaceConditionViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val raceViewModel: RaceConditionViewModel = viewModel()
    val cooperationViewModel: CooperationViewModel = viewModel()
    val blockingTrapViewModel: BlockingTrapViewModel = viewModel()
    val callbackBridgeViewModel: CallbackBridgeViewModel = viewModel()


    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.RaceCondition.route) {
            RaceConditionScreen(raceViewModel)
        }
        composable(Screen.Cooperation.route) {
            CooperationScreen(cooperationViewModel)
        }
        composable(Screen.BlockingTrap.route) {
            BlockingTrapScreen(blockingTrapViewModel)
        }
        composable(Screen.CallbackBridge.route) {
            CallbackBridgeScreen(callbackBridgeViewModel)
        }
    }
}