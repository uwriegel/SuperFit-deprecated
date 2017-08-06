package com.gmail.uwriegel.superfit.AntPlusSensors

import android.content.Context
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult

/**
 * Created by urieg on 05.08.2017.
 */
class HeartRateMonitor {
    constructor(context: Context, onNewHeartRate: (newHeartRate: Int)->Unit) {
        searchHeartRate(context, {
            deviceName = it.deviceDisplayName
            deviceNumber = it.antDeviceNumber
            subscribe(context)
        })
        this.onNewHeartRate = onNewHeartRate
    }

    fun subscribe(context: Context) {
        AntPlusHeartRatePcc.requestAccess(context, deviceNumber, 0, { heartRateController, resultCode, _ ->
            when (resultCode) {
                RequestAccessResult.SUCCESS -> subscribeToHeartRate(heartRateController)
            }
        }, {
        })
    }

    fun subscribeToHeartRate(heartRateController: AntPlusHeartRatePcc) = heartRateController.subscribeHeartRateDataEvent { estTimestamp, eventFlags, computedHeartRate,
                                                                                                                           heartBeatCount, heartBeatEventTime4, dataState ->
        run {
            if (lastTimeStamp + 1000 < estTimestamp ) {
                this@HeartRateMonitor.onNewHeartRate(computedHeartRate)
                lastTimeStamp = estTimestamp
            }
        }
    }

    val onNewHeartRate: (newHeartRate: Int)->Unit
    var lastTimeStamp = 0L
    var deviceName = ""
    var deviceNumber = 0
}