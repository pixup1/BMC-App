package com.bmc.app

import com.bmc.app.models.ConnectionState
import com.bmc.app.ui.BmcViewModel

class ConnectionManager(
    val bmcViewModel: BmcViewModel // Same ViewModel instance as in the UI
) {
    // Native functions for use by Kotlin
    private external fun connectToHost(address: String)

    external fun disconnect()

    external fun sendData(data: String)

    fun connect(address: String) {
        if (Regex("""^(\d{1,3}\.){3}\d{1,3}:\d{1,5}$""").matches(address)) {
            connectToHost(address)
        } else {
            throw IllegalArgumentException("Invalid address format: $address")
        }
    }

    companion object {
        init {
            System.loadLibrary("bmc-cpp")
        }
    }

    // Kotlin functions for use by C++
    fun setUiConnectionState(state: Int, host: String = "host") {
        when (state) {
            0 -> bmcViewModel.updateConnectionState(ConnectionState.Disconnected)
            1 -> bmcViewModel.updateConnectionState(ConnectionState.Connecting(host))
            2 -> bmcViewModel.updateConnectionState(ConnectionState.Connected(host))
            else -> throw IllegalArgumentException("Invalid state value: $state")
        }
    }
}