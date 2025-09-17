package com.bmc.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.bmc.app.models.Settings
import com.bmc.app.ui.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sqrt

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

    private var speed = Vector3(0.0, 0.0, 0.0)
    private var position = Vector3(0.0, 0.0, 0.0)
    private var rotation = Quaternion(1.0, 0.0, 0.0, 0.0)

    private var accUnlocked = true
    private var gyroUnlocked = true

    private var lockPosition = Vector3(0.0, 0.0, 0.0)
    private var lockRotation = Quaternion(1.0, 0.0, 0.0, 0.0)

    private var inverseRotation = Quaternion(1.0, 0.0, 0.0, 0.0) //TODO: make up always be up and make rotation match translation (edit: this may be fixed now ?)
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

            connectionManager.sendData(data)
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
                val accelerationWorld = dataManager.rotation.rotate(acceleration) //TODO: apply inverse ? to sync location space with rotation space

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

        if (top < 1 || top > 6 || right < 1 || right > 6 || top % 3 == right) {
            return Quaternion(1.0, 0.0, 0.0, 0.0)
        }

        val topVector = getAxisVector(top)
        var rightVector = getAxisVector(right)

        val frontVector = rightVector.cross(topVector).normalize()
        rightVector = topVector.cross(frontVector).normalize()

        return createRotationFromAxes(rightVector, topVector, frontVector)
    }

    private fun getAxisVector(axis: Int): Vector3 {
        return when (axis) {
            1 -> Vector3(1.0, 0.0, 0.0)  // X
            2 -> Vector3(0.0, 1.0, 0.0)  // Y
            3 -> Vector3(0.0, 0.0, 1.0)  // Z
            4 -> Vector3(-1.0, 0.0, 0.0) // -X
            5 -> Vector3(0.0, -1.0, 0.0) // -Y
            6 -> Vector3(0.0, 0.0, -1.0) // -Z
            else -> Vector3(0.0, 1.0, 0.0) // Par défaut, Y
        }
    }

    private fun createRotationFromAxes(right: Vector3, top: Vector3, front: Vector3): Quaternion { //ngl I barely understand what this does
        val m00 = right.x; val m01 = top.x; val m02 = front.x
        val m10 = right.y; val m11 = top.y; val m12 = front.y
        val m20 = right.z; val m21 = top.z; val m22 = front.z

        val trace = m00 + m11 + m22

        return if (trace > 0) {
            val s = 0.5 / sqrt(trace + 1.0)
            Quaternion(
                0.25 / s,
                (m21 - m12) * s,
                (m02 - m20) * s,
                (m10 - m01) * s
            )
        } else if (m00 > m11 && m00 > m22) {
            val s = 2.0 * sqrt(1.0 + m00 - m11 - m22)
            Quaternion(
                (m21 - m12) / s,
                0.25 * s,
                (m01 + m10) / s,
                (m02 + m20) / s
            )
        } else if (m11 > m22) {
            val s = 2.0 * sqrt(1.0 + m11 - m00 - m22)
            Quaternion(
                (m02 - m20) / s,
                (m01 + m10) / s,
                0.25 * s,
                (m12 + m21) / s
            )
        } else {
            val s = 2.0 * sqrt(1.0 + m22 - m00 - m11)
            Quaternion(
                (m10 - m01) / s,
                (m02 + m20) / s,
                (m12 + m21) / s,
                0.25 * s
            )
        }
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