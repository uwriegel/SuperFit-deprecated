package com.gmail.uwriegel.superfit.Activities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.gmail.uwriegel.superfit.AntPlusSensors.HeartRateMonitor
import com.gmail.uwriegel.superfit.R
import kotlinx.android.synthetic.main.activity_main.*
import android.os.PowerManager
import android.view.View
import com.gmail.uwriegel.superfit.AntPlusSensors.BikeMonitor
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.view.SoundEffectConstants
import android.view.WindowManager
import android.webkit.JavascriptInterface
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
//        webSettings.domStorageEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        webView.webChromeClient = WebChromeClient()

        webView.isHapticFeedbackEnabled = true
        webView.loadUrl("file:///android_asset/index.html")
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun doHapticFeedback() = doAsync { uiThread { webView.playSoundEffect(SoundEffectConstants.CLICK) } }
            @JavascriptInterface
            fun close() = doAsync { uiThread { this@MainActivity.finish() } }
        }, "Native")

        heartRateMonitor = HeartRateMonitor(context = this) {
            this.runOnUiThread { webView.loadUrl("javascript:setHeartRate('$it')") }
        }

        bikeMonitor = BikeMonitor(this, {
            this.runOnUiThread { webView.loadUrl("javascript:setSpeed('$it')") }
        }, {
            this.runOnUiThread { webView.loadUrl("javascript:setDistance('$it')") }
        }, {
            this.runOnUiThread { webView.loadUrl("javascript:setCadence('$it')") }
        }, {
            this.runOnUiThread { webView.loadUrl("javascript:setMaxSpeed('$it')") }
        }, { timeSpan: Long, avgSpeed: Float -> this.runOnUiThread({
            webView.loadUrl("javascript:setTimeSpan('$timeSpan', '$avgSpeed')")})
        })

        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        @Suppress("DEPRECATION")
        val builder = Notification.Builder(this)
                .setContentTitle("Super Fit")
                .setContentText("Erfasst Fitness-Daten")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_bike)
        val notification = builder.build()
        notification.flags = notification.flags or (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onResume() {
        super.onResume()

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // TODO: dimmable sreen on
        //val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        //this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag")
        //this.wakeLock?.acquire()
    }

    override fun onPause() {
        super.onPause()

        this.wakeLock?.release()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val decorView = window.decorView
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onBackPressed() = webView.loadUrl("javascript:onBackPressed()")

    private var wakeLock: PowerManager.WakeLock? = null
    private var heartRateMonitor: HeartRateMonitor? = null
    private var bikeMonitor: BikeMonitor? = null

    private val NOTIFICATION_ID= 34
}
