package com.bmc.app.ui

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bmc.app.ConnectionManager
import com.bmc.app.DataManager
import com.bmc.app.models.ConnectionPageData
import com.bmc.app.models.ConnectionPageDataSerializer
import com.bmc.app.models.Settings
import com.bmc.app.models.SettingsSerializer
import com.bmc.app.ui.theme.BlenderMotionControlTheme

val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer
)

@Composable
@Preview
fun BmcApp(
    bmcViewModel: BmcViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()

    val bmcUiState by bmcViewModel.uiState.collectAsState()
    val sensorData by bmcViewModel.sensorData.collectAsState()

    val connectionManager = ConnectionManager(context, bmcViewModel = bmcViewModel)
    val dataManager = DataManager(context, scope, connectionManager)

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