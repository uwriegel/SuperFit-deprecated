package com.gmail.uwriegel.superfit.Activities

import android.content.Context
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
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Nur ein Linux test
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
//        webSettings.domStorageEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        webView.webChromeClient = WebChromeClient()

        webView.isHapticFeedbackEnabled = true
        webView.loadUrl("file:///android_asset/index.html")


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
    }

    override fun onResume() {
        super.onResume()

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag")
        this.wakeLock?.acquire()
    }

    override fun onPause() {
        super.onPause()

        this.wakeLock?.release()
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

    private var wakeLock: PowerManager.WakeLock? = null
    private var heartRateMonitor: HeartRateMonitor? = null
    private var bikeMonitor: BikeMonitor? = null
}
