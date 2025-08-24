package com.bmc.app.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BmcViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(BmcUiState())
    val uiState: StateFlow<BmcUiState> = _uiState.asStateFlow()

    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotation =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Sensor state
    private val _sensorData = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val sensorData: StateFlow<FloatArray> = _sensorData.asStateFlow()

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            _sensorData.value = event.values.clone()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        sensorManager.registerListener(listener, rotation, SensorManager.SENSOR_DELAY_UI)
    }
}