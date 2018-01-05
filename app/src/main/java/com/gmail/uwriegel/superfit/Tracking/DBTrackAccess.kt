package com.gmail.uwriegel.superfit.Tracking

/**
 * Created by urieg on 05.01.2018.
 */
data class DBTrackAccess(
    val add: (trackPoint: TrackPoint) -> Unit,
    val trackPoints: () -> Sequence<TrackPoint>){
}