package com.gmail.uwriegel.superfit


import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import com.gmail.uwriegel.superfit.Activities.MainActivity
import com.gmail.uwriegel.superfit.AntPlusSensors.HeartRateMonitor
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

private var isStarted = false
private var close = {->}
private var heartRateMonitor: HeartRateMonitor? = null
private var heartRate = 0

class SensorService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return when(intent?.action) {
            "START_SERVICE" -> {
                if (!isStarted) {
                    super.onStartCommand(intent, flags, startId)

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

                    startForeground(NOTIFICATION_ID, notification);

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)

                    close = runServer()

                    heartRateMonitor = HeartRateMonitor(context = this) { hr -> heartRate = hr }

                    isStarted = true
                }

                return START_STICKY
            }
            "STOP_SERVICE" -> {
                if (isStarted) {
                    close()
                    stopForeground(true)
                    isStarted = false
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            else -> return START_NOT_STICKY
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        return null
    }

    private val NOTIFICATION_ID= 34
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
        catch (err: Exception) {
            val affe = err.toString()
            val mist = affe + "l"
        }
        return@Thread
    }.start()
    return { -> server?.close() }
}

fun clientConnected(client: Socket) {
    Thread {
        try {
            val istream = client.getInputStream()
            while (true) {
                val buffer = ByteArray(2000)
                val read = istream.read(buffer)

                val responseBody =
"""{
    "heartRate": $heartRate
}"""
                val contentLength = responseBody.length
                val response = "HTTP/1.1 200 OK\r\nContent-Type: text/json; charset=UTF-8\r\nContent-Length: $contentLength\r\n\r\n$responseBody"
                val ostream = client.getOutputStream()
                ostream.write(response.toByteArray())
                ostream.flush()
            }
        }
        catch (err: Exception) {
            val affe = err.toString()
            val mist = affe + "l"
        }
        return@Thread
    }.start()
}