package com.gmail.uwriegel.superfit.tracking

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import com.gmail.uwriegel.superfit.database.DBHandler
import com.gmail.uwriegel.superfit.provider.TracksContentProvider
import kotlin.coroutines.experimental.buildSequence

/**
 * Created by urieg on 03.01.2018.
 *
 * TrackPoints data access object
 */
class DataSource(context: Context) {

//    fun close() {
//
////        getAll.forEach {
////            val affe = it
////            val test = affe.longitude
////        }
//
//        dbHandler.close()
//    }

    fun createTrack(longitude: Double, latitude: Double, time: Long) : TrackPointsDataSource {
        val trackNumber = addTrack(longitude, latitude, time)
        return TrackPointsDataSource(
                trackNumber,
                { trackPoint: TrackPoint -> add(trackPoint, trackNumber) },
                { -> getTrackPoints(trackNumber) }
        )
    }

    fun updateTrack(trackNumber: Long, distance: Float, duration: Int, averageSpeed: Float) {
        val values = ContentValues()
        values.put(DBHandler.KEY_DISTANCE, distance)
        values.put(DBHandler.KEY_DURATION, duration)
        values.put(DBHandler.KEY_AVERAGE_SPEED, averageSpeed)
        contentResolver.update(TracksContentProvider.TRACKS_CONTENT_URI, values, "${DBHandler.KEY_ID}=$trackNumber", null)
    }

    private fun addTrack(longitude: Double, latitude: Double, time: Long): Long {
        val values = ContentValues()
        values.put(DBHandler.KEY_LONGITUDE, longitude)
        values.put(DBHandler.KEY_LATITUDE, latitude)
        values.put(DBHandler.KEY_TIME, time)


        val uri = contentResolver.insert(TracksContentProvider.TRACKS_CONTENT_URI, values)
        val cursor = contentResolver.query(uri, arrayOf(DBHandler.KEY_ID), null, null, null)
        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            val id = cursor.getLong(0)
            cursor.close()
            return id
        }
        else
            return -1
    }

    private fun add(trackPoint: TrackPoint, trackNumber: Long) {
        val values = ContentValues()
        values.put(DBHandler.KEY_TRACK_NR, trackNumber)
        values.put(DBHandler.KEY_LONGITUDE, trackPoint.longitude)
        values.put(DBHandler.KEY_LATITUDE, trackPoint.latitude)
        values.put(DBHandler.KEY_ELEVATION, trackPoint.elevation)
        values.put(DBHandler.KEY_PRECISION, trackPoint.precision)
        values.put(DBHandler.KEY_TIME, trackPoint.time)
        values.put(DBHandler.KEY_SPEED, trackPoint.speed)
        values.put(DBHandler.KEY_HEART_RATE, trackPoint.heartRate)
        contentResolver.insert(TracksContentProvider.TRACKPOINTS_CONTENT_URI, values)
    }

    private fun getTrackPoints(trackNumber: Long): Sequence<TrackPoint> {
//        return buildSequence {
//            val qb = SQLiteQueryBuilder()
//            qb.tables = DBHandler.TABLE_TRACK_POINTS
//
//            val cursor = qb.query(database, arrayOf(
//                    DBHandler.KEY_LATITUDE,
//                    DBHandler.KEY_LONGITUDE,
//                    DBHandler.KEY_ELEVATION,
//                    DBHandler.KEY_TIME,
//                    DBHandler.KEY_PRECISION,
//                    DBHandler.KEY_SPEED),
//                    "${DBHandler.KEY_ID} = $trackNumber", null, null, null, null)
//
//            if (cursor.count > 0) {
//                cursor.moveToFirst()
//                while (true) {
//                    yield(TrackPoint(
//                            cursor.getDouble(0),
//                            cursor.getDouble(1),
//                            cursor.getDouble(2),
//                            cursor.getLong(3),
//                            cursor.getFloat(4),
//                            cursor.getFloat(5), 0))
//                    if (!cursor.moveToNext())
//                        break
//                }
//            }
//        }
        throw NullPointerException()
    }

    private val contentResolver = context.contentResolver
}

