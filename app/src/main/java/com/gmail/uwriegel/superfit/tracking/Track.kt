package com.gmail.uwriegel.superfit.tracking

/**
 * Created by urieg on 07.01.2018.
 *
 * Track model
 */
data class Track (
            val latitude: Double,
            val longitude: Double,
            val time: Long,
            val speed: Float,
            val distance: Float){
}

