package com.gmail.uwriegel.superfit.tracking

import android.net.Uri
import android.util.Xml
import com.gmail.uwriegel.superfit.formatRfc3339
import com.gmail.uwriegel.superfit.extensions.document
import com.gmail.uwriegel.superfit.extensions.element
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.*

/**
 * Created by urieg on 06.01.2018.
 *
 * Exports a track in the dataBase to gpx file
 */
// TODO: Namespace, Headersection
fun exportToGpx(outputStream: OutputStream, trackPoints: Sequence<TrackPoint>) {

    val serializer = Xml.newSerializer()
    serializer.setOutput(outputStream, "UTF-8")

    serializer.document("UTF-8", true, {
        element(null, "gpx", {
            attribute(null,"version", "1.1")
            element(null, "trk", {
                element(null, "trkseg", {
                    trackPoints.forEach {
                        element(null, "trkpt", {
                            attribute(null,"lat", it.latitude.toString())
                            attribute(null,"lon", it.longitude.toString())
                            element(null, "ele", it.elevation.toString())
                            element(null, "time", formatRfc3339(Date(it.time)))
                            element(null, "pdop", it.precision.toString())
                            element(null, "heartrate", it.heartRate.toString())
                            element(null, "speed", it.speed.toString())
                        })
                    }
                })
            })
        })
    })
}



//
//<trkpt lat="54.8591470" lon="-1.5754310">
//    <ele>29.2</ele>
//    <time>2015-07-26T07:43:42Z</time>
//    <extensions>
//        <gpxtpx:TrackPointExtension>
//            <gpxtpx:hr>92</gpxtpx:hr>
//            <gpxtpx:cad>0</gpxtpx:cad>
//        </gpxtpx:TrackPointExtension>
//    </extensions>
//</trkpt>
