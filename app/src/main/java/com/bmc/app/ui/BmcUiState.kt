package com.bmc.app.ui

import com.bmc.app.models.ConnectionState

data class BmcUiState (
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val dataMuted: Boolean = false
)