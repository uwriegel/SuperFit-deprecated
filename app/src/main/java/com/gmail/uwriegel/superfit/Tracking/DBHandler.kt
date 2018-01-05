package com.gmail.uwriegel.superfit.Tracking

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by urieg on 02.01.2018.
 */
class DBHandler(context: Context)
    : SQLiteOpenHelper(context, dbName, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_TRACK_POINTS ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_TRACK_NR INTEGER, " +
                "$KEY_LATITUDE REAL, " +
                "$KEY_LONGITUDE REAL, " +
                "$KEY_ELEVATION REAL, " +
                "$KEY_TIME INTEGER, " +
                "$KEY_SPEED REAL, " +
                "$KEY_HEART_RATE INTEGER, " +
                "$KEY_PRECISION REAL);")

        db.execSQL("CREATE TABLE $TABLE_TRACKS ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_NAME TEXT, " +
                "$KEY_LATITUDE REAL, " +
                "$KEY_LONGITUDE REAL, " +
                "$KEY_TIME INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF IT EXISTS $TABLE_TRACK_POINTS")
        db.execSQL("DROP TABLE IF IT EXISTS $TABLE_TRACKS")
        onCreate(db)
    }

    companion object {
        val DB_VERSION = 1
        val TABLE_TRACK_POINTS = "Trackpoints"
        val TABLE_TRACKS = "Tracks"
        val KEY_ID = "_id"
        val KEY_TRACK_NR = "TrackNr"
        val KEY_NAME = "Name"
        val KEY_LATITUDE = "Latitude"
        val KEY_LONGITUDE = "Longitude"
        val KEY_ELEVATION = "Elevation"
        val KEY_TIME = "Time"
        val KEY_PRECISION = "Precision"
        val KEY_SPEED = "Speed"
        val KEY_HEART_RATE = "HeartRate"

        private val dbName = "Tracks.db"
    }
}
