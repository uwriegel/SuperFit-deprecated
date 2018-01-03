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
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.view.SoundEffectConstants
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.gmail.uwriegel.superfit.SensorService
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import android.util.Xml
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkPermissions())
            initialize()

        // TODO: Create XmlFile on SD card with 3 TrackPoints
        // TODO: Use extension functions for XmlSerializer

        val filename = "/sdcard/oruxmaps/tracklogs/affe.xml"
        val serializer = Xml.newSerializer()
        val writer = FileOutputStream(filename)
        serializer.setOutput(writer, "UTF-8")
        serializer.startDocument("UTF-8", true)
        serializer.startTag(null, "messages")
        serializer.endTag(null, "messages")
        serializer.endDocument()
        serializer.flush()
        writer.close()
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

    override fun onBackPressed() = webView.loadUrl("javascript:onBackPressed()")

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                val perms = HashMap<String, Int>()
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED)
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED)
                // Fill with results
                for ((index, value) in permissions.withIndex())
                    perms.put(value, grantResults[index])
                // Check for ACCESS_FINE_LOCATION
                if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED)
                    // All Permissions Granted
                    initialize()
                else
                    // Permission Denied
                    Toast.makeText(this, "Some Permission is Denied", Toast.LENGTH_SHORT).show()
            }
            else ->
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

     private fun checkPermissions(): Boolean {
        val permissionsList = ArrayList<String>()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
             permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        val permissions = permissionsList.toTypedArray()
        if (permissions.count() > 0) {
            requestPermissions(permissions, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initialize() {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        // webSettings.domStorageEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        // CORS allowed
        webSettings.allowUniversalAccessFromFileURLs = true
        webView.webChromeClient = WebChromeClient()

        webView.isHapticFeedbackEnabled = true
        webView.loadUrl("file:///android_asset/index.html")
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun doHapticFeedback() = doAsync { uiThread { webView.playSoundEffect(SoundEffectConstants.CLICK) } }
            @JavascriptInterface
            fun start() = doAsync { uiThread {
                val startIntent = Intent(this@MainActivity, SensorService::class.java)
                startIntent.action = SensorService.START
                startService(startIntent)
            } }
            @JavascriptInterface
            fun stop() = doAsync { uiThread {
                val startIntent = Intent(this@MainActivity, SensorService::class.java)
                startIntent.action = SensorService.STOP
                startService(startIntent)
            } }
            @JavascriptInterface
            fun close() = doAsync { uiThread { this@MainActivity.finish() } }
        }, "Native")
    }

    //private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1000
    }
}
