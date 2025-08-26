package com.bmc.app.ui.models

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val host: String) : ConnectionState()
}