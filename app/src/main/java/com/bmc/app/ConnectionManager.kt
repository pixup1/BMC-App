package com.bmc.app

import android.content.Context
import com.bmc.app.models.ConnectionState
import com.bmc.app.ui.BmcViewModel
import android.provider.Settings
import android.util.Log

class ConnectionManager(
    private val context: Context,
    val bmcViewModel: BmcViewModel, // Same ViewModel instance as in the UI
    var onConnect: () -> Unit = {},
    val onDisconnect: () -> Unit = {}
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

    fun setOnConnectCallback(onConnect: () -> Unit) {
        this.onConnect = onConnect
    }

    companion object {
        init {
            System.loadLibrary("bmc-cpp")
        }
    }

    // Kotlin functions for use by C++
    @Suppress("unused")
    fun updateConnectionState(state: Int, host: String = "host") {
        when (state) {
            0 -> {
                bmcViewModel.setConnectionState(ConnectionState.Disconnected)
                onDisconnect()
            }
            1 -> {
                bmcViewModel.setConnectionState(ConnectionState.Connecting(host))
            }
            2 -> {
                bmcViewModel.setConnectionState(ConnectionState.Connected(host))
                onConnect()
            }
            else -> throw IllegalArgumentException("Invalid state value: $state")
        }
    }
}