package com.gmail.uwriegel.superfit.Tracking

/**
 * Created by urieg on 05.01.2018.
 */
data class TrackPointsDataSource(
    val add: (trackPoint: TrackPoint) -> Unit,
    val getAll: () -> Sequence<TrackPoint>){
}