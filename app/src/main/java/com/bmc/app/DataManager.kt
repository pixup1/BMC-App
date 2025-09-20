package com.bmc.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.bmc.app.models.Settings
import com.bmc.app.ui.BmcUiState
import com.bmc.app.ui.BmcViewModel
import com.bmc.app.ui.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class DataManager(
    context: Context,
    bmcViewModel: BmcViewModel,
    scope: CoroutineScope,
    connectionManager: ConnectionManager
) {
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

    private var speed = Vector3(0.0, 0.0, 0.0)
    private var position = Vector3(0.0, 0.0, 0.0)
    private var rotation = Quaternion(1.0, 0.0, 0.0, 0.0)

    private var accUnlocked = true
    private var gyroUnlocked = true

    private var lockPosition = Vector3(0.0, 0.0, 0.0)
    private var lockRotation = Quaternion(1.0, 0.0, 0.0, 0.0)

    private var inverseRotation = Quaternion(1.0, 0.0, 0.0, 0.0)
    private var absoluteAxisRotation = getAbsoluteAxisRotation()

    private val gyroListener = object : SensorEventListener {
        private val dataManager = this@DataManager

        override fun onSensorChanged(event: SensorEvent) {
            val srot = event.values.clone()
            dataManager.rotation = Quaternion(srot[3].toDouble(), srot[0].toDouble(), srot[1].toDouble(), srot[2].toDouble()).normalize()
            val rot = if (dataManager.currentSettings.absoluteRotation) {
                dataManager.rotation * dataManager.absoluteAxisRotation
            } else if (dataManager.gyroUnlocked) {
                (dataManager.rotation * dataManager.inverseRotation)
            } else {
                dataManager.lockRotation
            }
            val pos = if (dataManager.accUnlocked) {
                dataManager.position
            } else {
                dataManager.lockPosition
            }

            var data = "{"

            data += "\"rot\":[${rot.w},${rot.x},${rot.y},${rot.z}],"

            data += "\"loc\":[${pos.x},${pos.y},${pos.z}]"

            data += "}"

            if (!bmcViewModel.uiState.value.dataMuted) {
                connectionManager.sendData(data)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val accListener = object : SensorEventListener {
        private val dataManager = this@DataManager
        var lastTimestamp: Long = 0

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

    fun lockAccelerometer() {
        if (accUnlocked) {
            lockPosition = position
            accUnlocked = false
        }
    }

    fun lockGyroscope() {
        if (gyroUnlocked) {
            lockRotation = (rotation * inverseRotation).normalize()
            gyroUnlocked = false
        }
    }

    fun unlockAccelerometer() {
        if (!accUnlocked) {
            position -= position - lockPosition
            accUnlocked = true
        }
    }

    fun unlockGyroscope() {
        if (!gyroUnlocked) {
            inverseRotation = rotation.conjugate().normalize() * lockRotation
            gyroUnlocked = true
        }
    }

    fun resetPosition() {
        position = Vector3(0.0, 0.0, 0.0)
    }

    fun resetRotation() {
        inverseRotation = rotation.conjugate().normalize()
    }

    fun resetTransform() {
        resetPosition()
        resetRotation()
    }

    fun getAbsoluteAxisRotation(): Quaternion {
        val top = currentSettings.topAxis
        val right = currentSettings.rightAxis

        if (top < 1 || top > 6 || right < 1 || right > 6 || top % 3 == right % 3) {
            return Quaternion(1.0, 0.0, 0.0, 0.0)
        }

        val topVector = getAxisVector(top)
        val rightVector = getAxisVector(right)
        val frontVector = rightVector.cross(topVector).normalize()

        return createRotationFromAxes(rightVector, topVector, frontVector)
    }

    private fun getAxisVector(axis: Int): Vector3 {
        return when (axis) {
            1 -> Vector3(1.0, 0.0, 0.0)
            2 -> Vector3(0.0, 1.0, 0.0)
            3 -> Vector3(0.0, 0.0, 1.0)
            4 -> Vector3(-1.0, 0.0, 0.0)
            5 -> Vector3(0.0, -1.0, 0.0)
            6 -> Vector3(0.0, 0.0, -1.0)
            else -> Vector3(0.0, 1.0, 0.0)
        }
    }

    private fun createRotationFromAxes(right: Vector3, top: Vector3, front: Vector3): Quaternion {
        val m00 = right.x; val m01 = top.x; val m02 = front.x
        val m10 = right.y; val m11 = top.y; val m12 = front.y
        val m20 = right.z; val m21 = top.z; val m22 = front.z

        var t: Double
        var q: Quaternion

        if (m22 < 0) {
            if (m00 > m11) {
                t = 1 + m00 - m11 - m22;
                q = Quaternion(m12-m21, t, m01+m10, m20+m02);
            } else {
                t = 1 - m00 + m11 - m22;
                q = Quaternion(m20-m02, m01+m10, t, m12+m21);
            }
        } else {
            if (m00 < -m11) {
                t = 1 - m00 - m11 + m22;
                q = Quaternion(m01-m10, m20+m02, m12+m21, t);
            } else {
                t = 1 + m00 + m11 + m22;
                q = Quaternion(t, m12-m21, m20-m02, m01-m10);
            }
        }
        return q * (0.5 / sqrt(t))
    }

    init {
        sensorManager.registerListener(gyroListener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(accListener, accelerationSensor, SensorManager.SENSOR_DELAY_GAME)

        scope.launch { // Auto-reset position when useAccelerometer setting changes
            var lastUseAccelerometer = currentSettings.useAccelerometer
            settingsFlow.collect { settings ->
                if (settings.useAccelerometer != lastUseAccelerometer) {
                    resetPosition()
                    accListener.lastTimestamp = 0
                    lastUseAccelerometer = settings.useAccelerometer
                }
            }
        }

        scope.launch { // Update axis when axis setting changes
            var lastTopAxis = currentSettings.topAxis
            var lastRightAxis = currentSettings.rightAxis
            settingsFlow.collect { settings ->
                if (settings.topAxis != lastTopAxis || settings.rightAxis != lastRightAxis) {
                    absoluteAxisRotation = getAbsoluteAxisRotation()
                    lastTopAxis = settings.topAxis
                    lastRightAxis = settings.rightAxis
                }
            }
        }
    }
}