package com.gmail.uwriegel.superfit.Tracking

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.gmail.uwriegel.superfit.getHeartRate
import com.gmail.uwriegel.superfit.getSpeed
import com.gmail.uwriegel.superfit.sendLocation

/**
 * Created by urieg on 03.01.2018.
 */
@SuppressLint("MissingPermission")
class LocationManager(context: Context, dataSource: DataSource) {

    fun stop() {
        locationManager.removeUpdates(locationListener)
    }

    fun getTrackNumber(): Long? = trackPoints?.trackNumber

    private val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {

            if (trackPoints == null)
                trackPoints = dataSource.createTrack(location.longitude, location.latitude, location.time)

            if (location.hasBearing()) {
                val affe = 2
                val aff = affe +8
            }
            trackPoints!!.add(TrackPoint(location.latitude, location.longitude, location.altitude,
                    location.time, location.accuracy, getSpeed(), getHeartRate()))
            sendLocation()
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
    private var trackPoints: TrackPointsDataSource? = null

    init {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener)
    }
}


