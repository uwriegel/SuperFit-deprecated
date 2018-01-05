package com.gmail.uwriegel.superfit


import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import android.util.Xml
import com.gmail.uwriegel.superfit.activities.MainActivity
import com.gmail.uwriegel.superfit.AntPlusSensors.BikeMonitor
import com.gmail.uwriegel.superfit.AntPlusSensors.HeartRateMonitor
import com.gmail.uwriegel.superfit.Tracking.DBTrackAccess
import com.gmail.uwriegel.superfit.Tracking.LocationManager
import com.gmail.uwriegel.superfit.Tracking.TrackPointsDataSource
import com.gmail.uwriegel.superfit.activities.formatRfc3339
import com.gmail.uwriegel.superfit.extensions.document
import com.gmail.uwriegel.superfit.extensions.element
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

private var isStarted = false
private var close = {->}
private var heartRateMonitor: HeartRateMonitor? = null
private var bikeMonitor: BikeMonitor? = null

// Daten:
private var heartRate = 0
private var speed = 0F
private var distance = 0F
private var cadence = 0
private var maxSpeed = 0F
private var timeSpan = 0L
private var averageSpeed = 0F
private var sendGpsActive = false

class SensorService : Service() {

    @Suppress("UNREACHABLE_CODE")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return when(intent?.action) {
            START -> {
                if (!isStarted) {
                    super.onStartCommand(intent, flags, startId)

                    dataSource = TrackPointsDataSource(this)
                    dataSource.open()

                    // TODO: create new record in "TRACKS", then take trackNumber
                    trackAccess = dataSource.getAccess(23)

                    locationManager = LocationManager(this, trackAccess)

                    val notificationIntent = Intent(this, MainActivity::class.java)
                    notificationIntent.action = "START"
                    notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

                    @Suppress("DEPRECATION")
                    val notification = NotificationCompat.Builder(this)
                            .setContentTitle("Super Fit")
                            .setContentText("Erfasst Fitness-Daten")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.ic_bike)
                            .setOngoing(true).build()

                    startForeground(NOTIFICATION_ID, notification)

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)

                    close = runServer()

                    heartRateMonitor = HeartRateMonitor(context = this) { hr -> heartRate = hr }

                    bikeMonitor = BikeMonitor(this, {
                        sp -> speed = sp
                    }, {
                        d -> distance = d
                    }, {
                        c -> cadence = c
                    }, {
                        msp -> maxSpeed = msp
                    }, {ts, asp ->
                        timeSpan = ts
                        averageSpeed = asp
                    })

                    isStarted = true
                }

                return START_STICKY
            }
            STOP -> {
                if (isStarted) {
                    close()
                    stopForeground(true)
                    locationManager.stop()
                    exportToGpx()
                    dataSource.close()
                    isStarted = false
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            else -> return START_NOT_STICKY
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // TODO: Namespace, Headersection
    private fun exportToGpx() {

        val filename = "/sdcard/oruxmaps/tracklogs/track.gpx"
        val serializer = Xml.newSerializer()
        val writer = FileOutputStream(filename)
        serializer.setOutput(writer, "UTF-8")

        serializer.document("UTF-8", true, {
            element(null, "gpx", {
                attribute(null,"version", "1.1")
                element(null, "trk", {
                    element(null, "trkseg", {
                        trackAccess.trackPoints().forEach {
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

    private val NOTIFICATION_ID = 34
    private lateinit var dataSource: TrackPointsDataSource
    private lateinit var trackAccess: DBTrackAccess
    private lateinit var locationManager: LocationManager

    companion object {
        val START = "START"
        val STOP = "STOP"
    }
}

fun runServer(): ()->Unit {
    var server: ServerSocket? = null
    Thread {
        try {
            server = ServerSocket (9865, 0, InetAddress.getLoopbackAddress())
            while (true) {
                val client = server!!.accept()
                clientConnected(client)
            }
        }
        catch (err: Exception) {}
        return@Thread
    }.start()
    return { -> server?.close() }
}

fun clientConnected(client: Socket) {

    fun getSendGpsActive(): ()->String {
        var gpsActiveSent = false

        return {
            if (sendGpsActive && !gpsActiveSent) {
                gpsActiveSent = true
                """"gps": true,
    """
            } else ""
        }
    }

    val sendGpsActive = getSendGpsActive()

    Thread {
        try {
            val istream = client.getInputStream()
            while (true) {
                val buffer = ByteArray(2000)
                istream.read(buffer)

                val responseBody =
"""{
    "heartRate": $heartRate,
    "speed": $speed,
    "distance": $distance,
    "cadence": $cadence,
    "maxSpeed": $maxSpeed,
    "timeSpan": $timeSpan,
    ${sendGpsActive()}"averageSpeed": $averageSpeed
}"""

                val contentLength = responseBody.length
                val response = "HTTP/1.1 200 OK\r\nContent-Type: text/json; charset=UTF-8\r\nContent-Length: $contentLength\r\n\r\n$responseBody"
                val ostream = client.getOutputStream()
                ostream.write(response.toByteArray())
                ostream.flush()
            }
        }
        catch (err: Exception) {}
        return@Thread
    }.start()
}

fun sendLocation() { sendGpsActive = true}