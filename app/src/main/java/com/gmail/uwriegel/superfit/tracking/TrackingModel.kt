package com.gmail.uwriegel.superfit.tracking

import android.location.Location

data class LocationData (
        var longitude: Double,
        var latitude: Double
){}

fun setCurrentLocation(location: Location) {
    currentLocation = LocationData( location.longitude, location.latitude)
}

var currentLocation: LocationData? = null