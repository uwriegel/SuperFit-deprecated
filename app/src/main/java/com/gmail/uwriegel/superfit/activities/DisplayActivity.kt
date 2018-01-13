package com.gmail.uwriegel.superfit.activities

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.gmail.uwriegel.superfit.R
import kotlinx.android.synthetic.main.activity_display.*
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import com.gmail.uwriegel.superfit.sensor.SensorService
import com.gmail.uwriegel.superfit.sensor.data
import com.gmail.uwriegel.superfit.sensor.gpsActive
import com.gmail.uwriegel.superfit.tracking.DataSource
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import com.google.gson.Gson
import kotlin.concurrent.timerTask


class DisplayActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        displayWebView.setBackgroundColor(0)
        val webSettings = displayWebView.settings
        webSettings.javaScriptEnabled = true
        // webSettings.domStorageEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        // CORS allowed
        webSettings.allowUniversalAccessFromFileURLs = true
        displayWebView.webChromeClient = WebChromeClient()

        displayWebView.isHapticFeedbackEnabled = true
        displayWebView.loadUrl("file:///android_asset/display.html")
    }

    override fun onResume() {
        super.onResume()

        var gpsActiveSent = false
        timer = Timer()
        timer?.schedule(timerTask {
            doAsync { uiThread {
                val gson = Gson()
                val json = gson.toJson(data)
                displayWebView.evaluateJavascript("onSensorData($json)", null)

                if (!gpsActiveSent && gpsActive) {
                    displayWebView.evaluateJavascript("onGpsActive()", null)
                    gpsActiveSent = true
                }
            } }
        }, 0L, 500L)

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // TODO: dimmable sreen on
        //val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        //this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag")
        //this.wakeLock?.acquire()
    }

    override fun onPause() {
        super.onPause()

        timer?.cancel()
        timer = null
        // this.wakeLock?.release()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

//    override fun onBackPressed() = displayWebView.loadUrl("javascript:onBackPressed()")

    //private var wakeLock: PowerManager.WakeLock? = null
    private var service: SensorService? = null
    var timer: Timer? = null
    var gpsActiveSent = false
}
