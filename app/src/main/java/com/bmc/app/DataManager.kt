package com.bmc.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class DataManager(context: Context, connectionManager: ConnectionManager) {
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotation =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val rot = event.values.clone()
            val data = "{\"rot\":[${rot[0]},${rot[1]},${rot[2]}]}"
            connectionManager.sendData(data)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        sensorManager.registerListener(listener, rotation, SensorManager.SENSOR_DELAY_GAME)
    }
}