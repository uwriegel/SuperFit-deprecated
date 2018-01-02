package com.gmail.uwriegel.superfit.Tracking;

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by urieg on 02.01.2018.
 */

// TODO: 1s oder 10
class TrackingDBHandler(context: Context, dbName: String)
    : SQLiteOpenHelper(context, dbName, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_TRACK_POINT ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_LATITUDE REAL, " +
                "$KEY_LONGITUDE REAL, " +
                "$KEY_LONGITUDE REAL, " +
                "$KEY_ELEVATION REAL, " +
                "$KEY_TIME INTEGER, " +
                "$KEY_SPEED REAL, " +
                "$KEY_HEART_RATE INTEGER, " +
                "$KEY_PRECISION REAL);")
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        val DB_VERSION = 1
        val TABLE_TRACK_POINT = "Trackpoint"
        val KEY_ID = "_id"
        val KEY_LATITUDE = "Latitude"
        val KEY_LONGITUDE = "Longitude"
        val KEY_ELEVATION = "Elevation"
        val KEY_TIME = "Time"
        val KEY_PRECISION = "Precision"
        val KEY_SPEED = "Speed"
        val KEY_HEART_RATE = "HeartRate"
        val COLUMN_ID = "_id"
    }
}
