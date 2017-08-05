package com.gmail.uwriegel.superfit.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.gmail.uwriegel.superfit.AntPlusSensors.HeartRateMonitor
import com.gmail.uwriegel.superfit.R
import kotlinx.android.synthetic.main.activity_main.*

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
    }

    private var heartRateMonitor: HeartRateMonitor? = null
}
