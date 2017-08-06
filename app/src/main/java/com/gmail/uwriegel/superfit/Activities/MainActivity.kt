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
import com.gmail.uwriegel.superfit.AntPlusSensors.BikeMonitor


class MainActivity : AppCompatActivity() {

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


        heartRateMonitor = HeartRateMonitor(context = this) {
            this.runOnUiThread { webView.loadUrl("javascript:setHeartRate('$it')") }
        }

        bikeMonitor = BikeMonitor(this, {
            this.runOnUiThread { webView.loadUrl("javascript:setSpeed('$it')") }
        }, {
            this.runOnUiThread { webView.loadUrl("javascript:setDistance('$it')") }
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

    private var wakeLock: PowerManager.WakeLock? = null
    private var heartRateMonitor: HeartRateMonitor? = null
    private var bikeMonitor: BikeMonitor? = null
}
