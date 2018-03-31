package com.gmail.uwriegel.superfit.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.gmail.uwriegel.superfit.R
import android.view.View
import android.view.WindowManager
import com.gmail.uwriegel.superfit.sensor.SensorService
import com.gmail.uwriegel.superfit.sensor.data
import com.gmail.uwriegel.superfit.sensor.gpsActive
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import com.google.gson.Gson
import kotlin.concurrent.timerTask


class DisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        pager = findViewById(R.id.viewPager) as ViewPager
        pager.adapter = PagerAdapter(supportFragmentManager)
    }

    private class PagerAdapter(fm: FragmentManager?)
        : FragmentPagerAdapter(fm) {

        fun getMaps(): MapsFragment {
            return maps
        }

        fun getDisplay(): DisplayFragment? {
            return display
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    display = DisplayFragment()
                    return display!!
                }
                else -> {
                    maps = MapsFragment()
                    return maps
                }
            }
        }

        private lateinit var maps: MapsFragment
        private var display: DisplayFragment? = null
    }

    override fun onResume() {
        super.onResume()

        var gpsActiveSent = false

        var display: DisplayFragment? = null
        timer = Timer()
        timer?.schedule(timerTask {
            doAsync { uiThread {

                if (display == null)
                    display = (pager.adapter as PagerAdapter).getDisplay()

                if (display != null) {
                    display!!.onSensorData(data)

                    if (!gpsActiveSent && gpsActive) {
                        display!!.onGpsActive()
                        gpsActiveSent = true
                    }
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
    lateinit var pager: ViewPager
}
