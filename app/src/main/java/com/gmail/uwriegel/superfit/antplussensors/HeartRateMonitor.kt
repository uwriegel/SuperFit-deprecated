package com.gmail.uwriegel.superfit.antplussensors

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

    fun subscribeToHeartRate(heartRateController: AntPlusHeartRatePcc) = heartRateController.subscribeHeartRateDataEvent { _ /*estTimestamp*/, _, computedHeartRate,_, _, _ ->
        run { this@HeartRateMonitor.onNewHeartRate(computedHeartRate) }
    }

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