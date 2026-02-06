package com.example.concurrencylab.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Concurrency Lab")
    object RaceCondition : Screen("race_condition", "Race Conditions")
    object Cooperation : Screen("cooperation", "Cooperation")
    object BlockingTrap : Screen("blocking_trap", "The Blocking Trap")
    object CallbackBridge : Screen("callback_bridge", "Callback Bridge")
}