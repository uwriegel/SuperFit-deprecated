package com.gmail.uwriegel.superfit.tracking

/**
 * Created by urieg on 05.01.2018.
 *
 * Access trackpoints without specifying trackNumber
 */
data class TrackPointsDataSource(
        val trackNumber: Long,
        val add: (trackPoint: TrackPoint) -> Unit){
}