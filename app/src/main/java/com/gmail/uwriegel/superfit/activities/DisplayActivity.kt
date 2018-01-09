package com.gmail.uwriegel.superfit.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.gmail.uwriegel.superfit.R
import kotlinx.android.synthetic.main.activity_display.*
import android.view.View
import android.view.SoundEffectConstants
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.gmail.uwriegel.superfit.SensorService
import com.gmail.uwriegel.superfit.tracking.DataSource
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import com.google.gson.Gson



class DisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        displayWebView.setBackgroundColor(0)
        initialize()
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

    override fun onBackPressed() = displayWebView.loadUrl("javascript:onBackPressed()")

    @SuppressLint("SetJavaScriptEnabled")
    private fun initialize() {
        val webSettings = displayWebView.settings
        webSettings.javaScriptEnabled = true
        // webSettings.domStorageEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        // CORS allowed
        webSettings.allowUniversalAccessFromFileURLs = true
        displayWebView.webChromeClient = WebChromeClient()

        displayWebView.isHapticFeedbackEnabled = true
        displayWebView.loadUrl("file:///android_asset/index.html")
        displayWebView.addJavascriptInterface(object {
            @JavascriptInterface
            fun doHapticFeedback() = doAsync { uiThread { displayWebView.playSoundEffect(SoundEffectConstants.CLICK) } }

            @JavascriptInterface
            fun start() = doAsync { uiThread {
            } }

            @JavascriptInterface
            fun stop() = doAsync { uiThread {
                val startIntent = Intent(this@DisplayActivity, SensorService::class.java)
                startIntent.action = SensorService.STOP
                startService(startIntent)
            } }

            @JavascriptInterface
            fun close() = doAsync { uiThread { this@DisplayActivity.finish() } }

            @JavascriptInterface
            fun getTracks() = doAsync { uiThread {
                val dataSource = DataSource(this@DisplayActivity)
                val tracks = dataSource.getTracks().toList()

                val gson = Gson()
                val json = gson.toJson(tracks)

                displayWebView.evaluateJavascript("onJasonBekommen($json)", null)
            } }

        }, "Native")
    }

    //private var wakeLock: PowerManager.WakeLock? = null
}
