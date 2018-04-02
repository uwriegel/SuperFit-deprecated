package com.gmail.uwriegel.superfit.events

import com.gmail.uwriegel.superfit.sensor.SensorData
import com.gmail.uwriegel.superfit.tracking.LocationData

data class EventData (
        var data: SensorData,
        var gpsActive: Boolean?,
        var location: LocationData?
){}