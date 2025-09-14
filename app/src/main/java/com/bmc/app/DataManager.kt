package com.bmc.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.bmc.app.models.Settings
import com.bmc.app.models.SettingsSerializer
import com.bmc.app.ui.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class DataManager(context: Context, scope: CoroutineScope, connectionManager: ConnectionManager) {
    private val settingsFlow = context.settingsDataStore.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = Settings.getDefaultInstance()
        )
    private val currentSettings: Settings
        get() = settingsFlow.value

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var speed = Vector3(0.0, 0.0, 0.0);
    private var position = Vector3(0.0, 0.0, 0.0);
    private var rotation = Quaternion(1.0, 0.0, 0.0, 0.0);

    private val gyrListener = object : SensorEventListener {
        private val dataManager = this@DataManager

        override fun onSensorChanged(event: SensorEvent) {
            val rot = event.values.clone() //TODO: "Subtract" inverse rotation (somehow) and add UI button to reset inverse (do this for location as well)
            dataManager.rotation = Quaternion(rot[3].toDouble(), rot[0].toDouble(), rot[1].toDouble(), rot[2].toDouble())
            val pos = dataManager.position.toArray()
            var data = "{\"rot\":[${rot[3]},${rot[0]},${rot[1]},${rot[2]}]"

            if (dataManager.currentSettings.useAccelerometer) {
                data += ",\"loc\":[${pos[0]},${pos[1]},${pos[2]}]"
            }

            data += "}"

            connectionManager.sendData(data)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val accListener = object : SensorEventListener {
        private val dataManager = this@DataManager
        private var lastTimestamp: Long = 0

        override fun onSensorChanged(event: SensorEvent) {
            if (dataManager.currentSettings.useAccelerometer) {
                val acc = event.values.clone()
                val acceleration = Vector3(acc[0].toDouble(), acc[1].toDouble(), acc[2].toDouble())
                val accelerationWorld = dataManager.rotation.rotate(acceleration)

                val dt = if (lastTimestamp == 0L) 0.0 else (event.timestamp - lastTimestamp).toDouble() * 1e-9
                lastTimestamp = event.timestamp

                dataManager.speed += accelerationWorld * dt

                val magnitude = accelerationWorld.magnitude()
                if (magnitude < dataManager.currentSettings.accelerometerCutoff) {
                    dataManager.speed *= magnitude / dataManager.currentSettings.accelerometerCutoff
                }

                dataManager.position += dataManager.speed * dt
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        sensorManager.registerListener(gyrListener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(accListener, accelerationSensor, SensorManager.SENSOR_DELAY_GAME)
    }
}