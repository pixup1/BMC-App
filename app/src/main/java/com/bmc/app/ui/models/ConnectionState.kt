package com.bmc.app.ui.models

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    data class Connecting(val host: String) : ConnectionState()
    data class Connected(val host: String) : ConnectionState()
}