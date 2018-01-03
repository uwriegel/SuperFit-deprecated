package com.gmail.uwriegel.superfit.Tracking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Created by urieg on 03.01.2018.
 *
 * TrackPoints data access object
 */
class TrackPointsDataSource {

    constructor(context: Context) {
        val dbNamePart = "tracking"

        fun getNewDatabaseFile(index: Int): File {
            val filename = "$dbNamePart-$index.db"
            val dbFilePart = context.getDatabasePath(filename)
            if (dbFilePart.exists())
                return getNewDatabaseFile(index + 1)
            else
                return dbFilePart
        }

        val dbName = getNewDatabaseFile(0).name

        dbHandler = DBHandler(context, dbName)
    }

    fun open() {
        database = dbHandler.writableDatabase
    }

    fun close() {
        dbHandler.close()
    }

    fun add(trackPoint: TrackPoint) {
        val values = ContentValues()
        values.put(DBHandler.KEY_LONGITUDE, trackPoint.longitude)
        values.put(DBHandler.KEY_LATITUDE, trackPoint.latitude)
        values.put(DBHandler.KEY_ELEVATION, trackPoint.elevation)
        values.put(DBHandler.KEY_PRECISION, trackPoint.precision)
        values.put(DBHandler.KEY_TIME, trackPoint.time.time)
        values.put(DBHandler.KEY_SPEED, trackPoint.speed)
        database.insert(DBHandler.TABLE_TRACK_POINT, null, values)
    }

    private lateinit var database: SQLiteDatabase
    private lateinit var dbHandler: DBHandler
}

