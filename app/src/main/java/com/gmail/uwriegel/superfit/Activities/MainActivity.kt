package com.gmail.uwriegel.superfit.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gmail.uwriegel.superfit.AntPlusSensors.HeartRateMonitor
import com.gmail.uwriegel.superfit.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        heartRateMonitor = HeartRateMonitor(this, {
            val herzschlag = it
            val nochmal = herzschlag
        })
    }

    private var heartRateMonitor: HeartRateMonitor? = null
}
