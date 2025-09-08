package com.bmc.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bmc.app.ConnectionManager
import com.bmc.app.ui.models.ConnectionState
import com.bmc.app.ui.theme.BlenderMotionControlTheme

@Composable
@Preview
fun BmcApp(
    bmcViewModel: BmcViewModel = viewModel()
) {
    val navController = rememberNavController()

    val bmcUiState by bmcViewModel.uiState.collectAsState()
    val sensorData by bmcViewModel.sensorData.collectAsState()

    val connectionManager = ConnectionManager(bmcViewModel = bmcViewModel)

    BlenderMotionControlTheme {
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable(
                "main"
            ) {
                MainPage(
                    openSettingsPage = { navController.navigate("settings") },
                    openConnectionPage = { navController.navigate("connection") },
                    connectionState = bmcUiState.connectionState,
                    onDisconnect = {
                        connectionManager.disconnect()
                    },
                    sensorData = sensorData
                )
            }
            composable(
                "settings",
                enterTransition = {
                    when (initialState.destination.route) {
                        "main" ->
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(700)
                            )
                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        "main" ->
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(700)
                            )
                        else -> null
                    }
                },
            ) {
                SettingsPage(
                    closeSettings = { navController.popBackStack() }
                )
            }
            composable(
                "connection",
                enterTransition = {
                    when (initialState.destination.route) {
                        "main" ->
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Up,
                                animationSpec = tween(700)
                            )
                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        "main" ->
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Down,
                                animationSpec = tween(700)
                            )
                        else -> null
                    }
                },
            ) {
                ConnectionPage(
                    onExit = { navController.popBackStack() },
                    onAddressSubmit = {
                        connectionManager.connect(it)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}