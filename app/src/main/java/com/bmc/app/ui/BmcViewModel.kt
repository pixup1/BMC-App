package com.bmc.app.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.bmc.app.models.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BmcViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(BmcUiState())
    val uiState: StateFlow<BmcUiState> = _uiState.asStateFlow()

    fun setConnectionState(newState: ConnectionState) {
        _uiState.value = _uiState.value.copy(connectionState = newState)
    }
}