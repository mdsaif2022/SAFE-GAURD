package com.personalvault.presentation.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object CreateAccount : Screen("create_account")
    object DeviceSetup : Screen("device_setup")
    object AgentDashboard : Screen("agent_dashboard")
    object ControllerDashboard : Screen("controller_dashboard")
    object RemoteDevice : Screen("remote_device")
    object Settings : Screen("settings")
}
