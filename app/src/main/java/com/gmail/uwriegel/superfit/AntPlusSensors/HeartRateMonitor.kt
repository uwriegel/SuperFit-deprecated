package com.gmail.uwriegel.superfit.AntPlusSensors

import android.content.Context
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult

/**
 * Created by urieg on 05.08.2017.
 *
 * Monitors the Heart Rate
 */
class HeartRateMonitor(context: Context, val onNewHeartRate: (newHeartRate: Int) -> Unit) {

    fun subscribe(context: Context) {
        AntPlusHeartRatePcc.requestAccess(context, deviceNumber, 0, { heartRateController, resultCode, _ ->
            if (resultCode == RequestAccessResult.SUCCESS)
                 subscribeToHeartRate(heartRateController)
        }, {
        })
    }

    fun subscribeToHeartRate(heartRateController: AntPlusHeartRatePcc) = heartRateController.subscribeHeartRateDataEvent { estTimestamp, _, computedHeartRate,_, _, _ ->
        run {
            if (lastTimeStamp + 1000 < estTimestamp ) {
                this@HeartRateMonitor.onNewHeartRate(computedHeartRate)
                lastTimeStamp = estTimestamp
            }
        }
    }

    var lastTimeStamp = 0L
    var deviceName = ""
    var deviceNumber = 0

    init {
        searchHeartRate(context, {
            deviceName = it.deviceDisplayName
            deviceNumber = it.antDeviceNumber
            subscribe(context)
        })
    }
}