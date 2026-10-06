package com.personalvault.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.personalvault.domain.model.DeviceMode
import com.personalvault.presentation.agent.AgentDashboardScreen
import com.personalvault.presentation.agent.AgentViewModel
import com.personalvault.presentation.auth.AuthViewModel
import com.personalvault.presentation.auth.CreateAccountScreen
import com.personalvault.presentation.auth.LoginScreen
import com.personalvault.presentation.auth.WelcomeScreen
import com.personalvault.presentation.controller.ControllerDashboardScreen
import com.personalvault.presentation.controller.ControllerViewModel
import com.personalvault.presentation.controller.RemoteDeviceScreen
import com.personalvault.presentation.settings.SettingsScreen
import com.personalvault.presentation.settings.SettingsViewModel
import com.personalvault.presentation.setup.DeviceSetupScreen
import com.personalvault.presentation.setup.DeviceSetupViewModel

@Composable
fun VaultNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToCreateAccount = {
                    navController.navigate(Screen.CreateAccount.route)
                },
                onNavigateToSetup = {
                    navController.navigate(Screen.DeviceSetup.route)
                }
            )
        }

        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSetup = {
                    navController.navigate(Screen.DeviceSetup.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToCreateAccount = {
                    navController.navigate(Screen.CreateAccount.route)
                }
            )
        }

        composable(Screen.CreateAccount.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            CreateAccountScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSetup = {
                    navController.navigate(Screen.DeviceSetup.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.DeviceSetup.route) {
            val setupViewModel: DeviceSetupViewModel = hiltViewModel()
            DeviceSetupScreen(
                viewModel = setupViewModel,
                onNavigateToAgent = {
                    navController.navigate(Screen.AgentDashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToController = {
                    navController.navigate(Screen.ControllerDashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AgentDashboard.route) {
            val agentViewModel: AgentViewModel = hiltViewModel()
            AgentDashboardScreen(
                viewModel = agentViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.ControllerDashboard.route) {
            val controllerViewModel: ControllerViewModel = hiltViewModel()
            ControllerDashboardScreen(
                viewModel = controllerViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToRemoteDevice = {
                    navController.navigate(Screen.RemoteDevice.route)
                }
            )
        }

        composable(Screen.RemoteDevice.route) {
            val controllerViewModel: ControllerViewModel = hiltViewModel()
            RemoteDeviceScreen(
                viewModel = controllerViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRoleSwitched = {
                    val mode = settingsViewModel.uiState.value.session?.deviceMode ?: DeviceMode.AGENT
                    val targetRoute = if (mode == DeviceMode.CONTROLLER) {
                        Screen.ControllerDashboard.route
                    } else {
                        Screen.AgentDashboard.route
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                },
                onLoggedOut = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
