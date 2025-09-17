package com.bmc.app

import kotlin.math.sqrt

data class Quaternion(val w: Double, val x: Double, val y: Double, val z: Double)
data class Vector3(val x: Double, val y: Double, val z: Double)

operator fun Quaternion.plus(o: Quaternion) =
    Quaternion(w + o.w, x + o.x, y + o.y, z + o.z)

operator fun Quaternion.minus(o: Quaternion) =
    Quaternion(w - o.w, x - o.x, y - o.y, z - o.z)

operator fun Quaternion.times(o: Quaternion): Quaternion =
    Quaternion(
        w*o.w - x*o.x - y*o.y - z*o.z,
        w*o.x + x*o.w + y*o.z - z*o.y,
        w*o.y - x*o.z + y*o.w + z*o.x,
        w*o.z + x*o.y - y*o.x + z*o.w
    )

fun Quaternion.conjugate() = Quaternion(w, -x, -y, -z)

fun Quaternion.normalize(): Quaternion {
    val len = sqrt(w*w + x*x + y*y + z*z)
    return Quaternion(w/len, x/len, y/len, z/len)
}

fun Quaternion.rotate(v: Vector3): Vector3 {
    val qVec = Quaternion(0.0, v.x, v.y, v.z)
    val qInv = this.conjugate().normalize()
    val res = this * qVec * qInv
    return Vector3(res.x, res.y, res.z)
}

fun Quaternion.toArray() = arrayOf(w, x, y, z)

operator fun Vector3.plus(o: Vector3) =
    Vector3(x + o.x, y + o.y, z + o.z)

operator fun Vector3.minus(o: Vector3) =
    Vector3(x - o.x, y - o.y, z - o.z)

operator fun Vector3.times(s: Double) =
    Vector3(x * s, y * s, z * s)

fun Vector3.cross(o: Vector3) =
    Vector3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x
    )

fun Vector3.normalize(): Vector3 {
    val len = magnitude()
    return Vector3(x/len, y/len, z/len)
}

fun Vector3.magnitude() = sqrt(x*x + y*y + z*z)

fun Vector3.toArray() = arrayOf(x, y, z)