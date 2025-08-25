package com.bmc.app.ui

import com.bmc.app.ui.models.ConnectionState

data class BmcUiState (
    val connectionState: ConnectionState = ConnectionState.Disconnected
)