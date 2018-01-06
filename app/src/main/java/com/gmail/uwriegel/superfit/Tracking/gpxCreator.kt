package com.gmail.uwriegel.superfit.Tracking

import android.util.Xml
import com.gmail.uwriegel.superfit.formatRfc3339
import com.gmail.uwriegel.superfit.extensions.document
import com.gmail.uwriegel.superfit.extensions.element
import java.io.FileOutputStream
import java.util.*

/**
 * Created by urieg on 06.01.2018.
 */
    // TODO: Namespace, Headersection
    fun exportToGpx(trackPoints: TrackPointsDataSource) {

        val filename = "/sdcard/oruxmaps/tracklogs/track.gpx"
        val serializer = Xml.newSerializer()
        val writer = FileOutputStream(filename)
        serializer.setOutput(writer, "UTF-8")

        serializer.document("UTF-8", true, {
            element(null, "gpx", {
                attribute(null,"version", "1.1")
                element(null, "trk", {
                    element(null, "trkseg", {
                        trackPoints.getAll().forEach {
                            element(null, "trkpt", {
                                attribute(null,"lat", it.latitude.toString())
                                attribute(null,"lon", it.longitude.toString())
                                element(null, "ele", it.elevation.toString())
                                element(null, "time", formatRfc3339(Date(it.time)))
                                element(null, "pdop", it.precision.toString())
                            })
                        }
                    })
                })
            })
        })

        writer.close()
    }
