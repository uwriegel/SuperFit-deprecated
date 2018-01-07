package com.gmail.uwriegel.superfit.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import com.gmail.uwriegel.superfit.database.DBHandler

class TracksContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        dbHandler = DBHandler(context)
        return false
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val uriType = uriMatcher.match(uri)
        val db = dbHandler.writableDatabase

        val rowId = when(uriType) {
            TRACKS -> db.insert(DBHandler.TABLE_TRACKS, null, values)
                //Uri.parse("${DBHandler.TABLE_TRACKS}/id")
            TRACKPOINTS -> db.insert(DBHandler.TABLE_TRACK_POINTS, null, values)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        context.contentResolver.notifyChange(uri, null)
        return ContentUris.withAppendedId(uri, rowId)
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = DBHandler.TABLE_TRACKS

        val uriType = uriMatcher.match(uri)
        when(uriType) {
            TRACKS -> {}
            TRACKS_ID -> queryBuilder.appendWhere("${DBHandler.KEY_ID}=${uri.lastPathSegment}")
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val cursor = queryBuilder.query(dbHandler.readableDatabase, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context.contentResolver, uri)
        return cursor
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        val uriType = uriMatcher.match(uri)
        val rowsUpdated = when(uriType) {
            TRACKS -> dbHandler.writableDatabase.update(DBHandler.TABLE_TRACKS, values, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = throw UnsupportedOperationException("Not yet implemented")

    override fun getType(uri: Uri): String? {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw UnsupportedOperationException("Not yet implemented")
    }

    private val TRACKS = 1
    private val TRACKS_ID = 2
    private val TRACKPOINTS = 3
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    private lateinit var dbHandler: DBHandler

    init {
        uriMatcher.addURI(Authority, TRACKS_TABLE, TRACKS)
        uriMatcher.addURI(Authority, TRACKS_TABLE + "/#", TRACKS_ID)
        uriMatcher.addURI(Authority, TRACKPOINTS_TABLE, TRACKPOINTS)
    }

    companion object {
        val Authority = "com.gmail.uwriegel.superfit.provider.tracks"
        private val TRACKS_TABLE = "tracks"
        private val TRACKPOINTS_TABLE = "trackpoints"

        val TRACKS_CONTENT_URI = Uri.parse("content://$Authority/$TRACKS_TABLE")
        val TRACKPOINTS_CONTENT_URI = Uri.parse("content://$Authority/$TRACKPOINTS_TABLE")
    }
}
