package com.gmail.uwriegel.superfit.Tracking

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

/**
 * Created by urieg on 03.01.2018.
 */
@SuppressLint("MissingPermission")
class LocationManager(context: Context, dataSource: TrackPointsDataSource) {

    fun stop() {
        locationManager.removeUpdates(locationListener)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (location.hasBearing()) {
                val affe = 2
                val aff = affe +8
            }
            dataSource.add(TrackPoint(location.latitude, location.longitude, location.altitude, location.time, location.accuracy, 0F, 0))
    //        mapView.setCenter(LatLong(location.latitude, location.longitude))
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }

        override fun onProviderDisabled(p0: String?) {
        }
    }

    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    private val LOCATION_REFRESH_TIME = 1000L
    private val LOCATION_REFRESH_DISTANCE = 10.0F

    init {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener)
    }
}


