package com.bmc.app

import com.bmc.app.ui.BmcViewModel

class ConnectionManager(
    val bmcViewModel: BmcViewModel // Same ViewModel instance as in the UI
) {
    // Native functions for use by Kotlin
    external fun connect(address: String)

    external fun disconnect()

    external fun sendData(data: String)

    companion object {
        init {
            System.loadLibrary("bmc-cpp")
        }
    }

    // Kotlin functions for use by C++
    fun setUiConnectionState(state: Int, host: String = "host") {
        when (state) {
            0 -> bmcViewModel.updateConnectionState(com.bmc.app.ui.models.ConnectionState.Disconnected)
            1 -> bmcViewModel.updateConnectionState(com.bmc.app.ui.models.ConnectionState.Connecting(host))
            2 -> bmcViewModel.updateConnectionState(com.bmc.app.ui.models.ConnectionState.Connected(host))
            else -> throw IllegalArgumentException("Invalid state value: $state")
        }
    }
}