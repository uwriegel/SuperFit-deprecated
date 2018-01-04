package com.gmail.uwriegel.superfit.Tracking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.text.TextUtils
import java.io.File
import kotlin.coroutines.experimental.buildSequence

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

        trackPoints.forEach {
            val affe = it
            val test = affe.longitude
        }

        dbHandler.close()
    }

    fun add(trackPoint: TrackPoint) {
        val values = ContentValues()
        values.put(DBHandler.KEY_LONGITUDE, trackPoint.longitude)
        values.put(DBHandler.KEY_LATITUDE, trackPoint.latitude)
        values.put(DBHandler.KEY_ELEVATION, trackPoint.elevation)
        values.put(DBHandler.KEY_PRECISION, trackPoint.precision)
        values.put(DBHandler.KEY_TIME, trackPoint.time)
        values.put(DBHandler.KEY_SPEED, trackPoint.speed)
        database.insert(DBHandler.TABLE_TRACK_POINTS, null, values)
    }

    val trackPoints = buildSequence {
        val qb = SQLiteQueryBuilder()
        qb.tables = DBHandler.TABLE_TRACK_POINTS

        val cursor = qb.query(database,arrayOf(
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

    private lateinit var database: SQLiteDatabase
    private val dbHandler: DBHandler
}

