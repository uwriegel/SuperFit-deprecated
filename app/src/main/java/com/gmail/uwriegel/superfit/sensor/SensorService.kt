package com.gmail.uwriegel.superfit.sensor


import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import com.gmail.uwriegel.superfit.R
import com.gmail.uwriegel.superfit.activities.MainActivity
import com.gmail.uwriegel.superfit.antplussensors.BikeMonitor
import com.gmail.uwriegel.superfit.antplussensors.HeartRateMonitor
import com.gmail.uwriegel.superfit.http.startServer
import com.gmail.uwriegel.superfit.http.stopServer
import com.gmail.uwriegel.superfit.tracking.DataSource
import com.gmail.uwriegel.superfit.tracking.LocationManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SensorService : Service(), ServiceCallback {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return when(intent?.action) {
            START -> {
                if (!isStarted) {
                    super.onStartCommand(intent, flags, startId)

                    dataSource = DataSource(this)

                    locationManager = LocationManager(this, dataSource)

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

                    startServer(this)

                    heartRateMonitor = HeartRateMonitor(context = this) { hr -> data.heartRate = hr }

                    bikeMonitor = BikeMonitor(this,
                            { sp -> data.speed = sp },
                            { d -> data.distance = d },
                            { c -> data.cadence = c },
                            { msp -> data.maxSpeed = msp },
                            { ts, asp ->
                                data.timeSpan = ts
                                data.averageSpeed = asp }
                    )

                    isStarted = true
                }

                START_STICKY
            }
            else -> START_NOT_STICKY
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun stopService() {
        if (isStarted) {

            stopServer()

            stopForeground(true)
            locationManager.stop()

            val track = locationManager.getTrackNumber()
            if (track != null)
                dataSource.updateTrack(track, data.distance, data.timeSpan.toInt(), data.averageSpeed)

//                    val fileToCopy = getDatabasePath("Tracks.db")
//                    val destinationFile = File("/sdcard/oruxmaps/tracklogs/tracks.db")
//                    val fis = FileInputStream(fileToCopy)
//                    val fos = FileOutputStream(destinationFile)
//
//                    val b = ByteArray(1024)
//                    var noOfBytesRead = 0
//
//                    while(noOfBytesRead != -1) {
//                        noOfBytesRead = fis.read(b)
//                        if (noOfBytesRead != -1)
//                            fos.write(b, 0, noOfBytesRead)
//                    }
//                    fis.close()
//                    fos.close()

            isStarted = false
            stopSelf()
        }
    }

    private val NOTIFICATION_ID = 34
    private lateinit var dataSource: DataSource
    private lateinit var locationManager: LocationManager
    private var isStarted = false
    private var heartRateMonitor: HeartRateMonitor? = null
    private var bikeMonitor: BikeMonitor? = null

    companion object {
        val START = "START"
    }
}

