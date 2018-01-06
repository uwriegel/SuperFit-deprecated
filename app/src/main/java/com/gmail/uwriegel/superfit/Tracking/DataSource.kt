package com.gmail.uwriegel.superfit.Tracking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import kotlin.coroutines.experimental.buildSequence

/**
 * Created by urieg on 03.01.2018.
 *
 * TrackPoints data access object
 */
class DataSource(context: Context) {

    fun open() {
        database = dbHandler.writableDatabase
    }

    fun close() {

//        getAll.forEach {
//            val affe = it
//            val test = affe.longitude
//        }

        dbHandler.close()
    }

    fun createTrack(longitude: Double, latitude: Double, time: Long) : TrackPointsDataSource {
        // TODO: create new record in "TRACKS", then take trackNumber

        val trackNumber = 456
        return TrackPointsDataSource({ trackPoint: TrackPoint -> add(trackPoint, trackNumber) },
                { -> getTrackPoints(trackNumber) }
        )
    }

    private fun add(trackPoint: TrackPoint, trackNumber: Int) {
        val values = ContentValues()
        values.put(DBHandler.KEY_LONGITUDE, trackPoint.longitude)
        values.put(DBHandler.KEY_LATITUDE, trackPoint.latitude)
        values.put(DBHandler.KEY_ELEVATION, trackPoint.elevation)
        values.put(DBHandler.KEY_PRECISION, trackPoint.precision)
        values.put(DBHandler.KEY_TIME, trackPoint.time)
        values.put(DBHandler.KEY_SPEED, trackPoint.speed)
        database.insert(DBHandler.TABLE_TRACK_POINTS, null, values)
    }

    private fun getTrackPoints(trackNumber: Int): Sequence<TrackPoint> {
        return buildSequence {
            val qb = SQLiteQueryBuilder()
            qb.tables = DBHandler.TABLE_TRACK_POINTS

            val cursor = qb.query(database, arrayOf(
                    DBHandler.KEY_LATITUDE,
                    DBHandler.KEY_LONGITUDE,
                    DBHandler.KEY_ELEVATION,
                    DBHandler.KEY_TIME,
                    DBHandler.KEY_PRECISION,
                    DBHandler.KEY_SPEED),
                    null, null, null, null, null)

            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (true) {
                    yield(TrackPoint(
                            cursor.getDouble(0),
                            cursor.getDouble(1),
                            cursor.getDouble(2),
                            cursor.getLong(3),
                            cursor.getFloat(4),
                            cursor.getFloat(5), 0))
                    if (!cursor.moveToNext())
                        break
                }
            }
        }
    }

    private lateinit var database: SQLiteDatabase
    private val dbHandler: DBHandler = DBHandler(context)
}

