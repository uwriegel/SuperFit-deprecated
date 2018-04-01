package com.gmail.uwriegel.superfit.sensor

data class Event (
    var data: SensorData,
    var gpsActive: Boolean?
){}

/**
 * Created by urieg on 10.01.2018.
 */
data class SensorData (
    var heartRate: Int,
    var speed: Float,
    var distance: Float,
    var cadence: Int,
    var maxSpeed: Float,
    var timeSpan: Long,
    var averageSpeed: Float){}