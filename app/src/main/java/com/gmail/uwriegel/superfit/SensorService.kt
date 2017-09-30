package com.gmail.uwriegel.superfit

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import com.gmail.uwriegel.superfit.Activities.MainActivity

class SensorService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action.equals("START_SERVICE")) {
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

            return START_STICKY
        }
        else if (intent?.action.equals("STOP_SERVICE")) {
            stopForeground(true)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        return null
    }

    private val NOTIFICATION_ID= 34
}
