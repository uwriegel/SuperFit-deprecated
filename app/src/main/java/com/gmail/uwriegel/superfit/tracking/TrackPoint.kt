package com.gmail.uwriegel.superfit.tracking

/**
 * Created by urieg on 03.01.2018.
 *
 * TrackPoint Model
 */
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val time: Long,
    val precision: Float,
    val speed: Float,
    val heartRate: Int) {
}

