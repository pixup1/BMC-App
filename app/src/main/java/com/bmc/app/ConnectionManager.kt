package com.bmc.app

import android.content.Context
import com.bmc.app.models.ConnectionState
import com.bmc.app.ui.BmcViewModel
import android.provider.Settings

class ConnectionManager(
    private val context: Context,
    val bmcViewModel: BmcViewModel // Same ViewModel instance as in the UI
) {
    // Native functions for use by Kotlin
    private external fun connectToHost(address: String, device: String)

    external fun disconnect()

    external fun sendData(data: String)

    fun connect(address: String) {
        if (Regex("""^(\d{1,3}\.){3}\d{1,3}:\d{1,5}$""").matches(address)) {
            val deviceName = Settings.Global.getString(
                context.contentResolver,
                Settings.Global.DEVICE_NAME
            )
            connectToHost(address, deviceName)
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