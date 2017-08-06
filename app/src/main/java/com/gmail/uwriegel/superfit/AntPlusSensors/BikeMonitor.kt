package com.gmail.uwriegel.superfit.AntPlusSensors

import android.content.Context
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle
import java.math.BigDecimal
import java.util.*


/**
 * Created by urieg on 05.08.2017.
 */
class BikeMonitor {
    constructor(context: Context, onSpeed: (speed: Float)->Unit, onDistance: (distance: Float)->Unit) {

        this.onSpeed = onSpeed
        this.onDistance = onDistance

        searchBike(context, {
            deviceName = it.deviceDisplayName
            deviceNumber = it.antDeviceNumber
            subscribe(context)
        })
    }

    private fun subscribe(context: Context) {
        bsdReleaseHandle = AntPlusBikeSpeedDistancePcc.requestAccess(context, deviceNumber, 0, true, { bikeController, resultCode, _ ->
            when (resultCode) {
                RequestAccessResult.SUCCESS -> subScribeToBike(bikeController)
            }
        }, {
        })
    }

    private fun subScribeToBike(bikeController: AntPlusBikeSpeedDistancePcc) {
        var lastTimeStamp = 0L
        var lastDistanceTimeStamp = 0L

        bikeController.subscribeCalculatedSpeedEvent(object:
                AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver(BigDecimal(wheelCircumference)) {
            override fun onNewCalculatedSpeed(estTimestamp: Long, flags: EnumSet<EventFlag>?, calculatedSpeedInMs: BigDecimal?) {
                if (calculatedSpeedInMs != null) {
                    if (lastTimeStamp + 500 < estTimestamp ) {
                        val calculatedSpeed = calculatedSpeedInMs.toFloat() * 3.6f
                        onSpeed(calculatedSpeed)
                        lastTimeStamp = estTimestamp
                    }
                }
            }
        })

        val mToKm = BigDecimal(1000)
        bikeController.subscribeCalculatedAccumulatedDistanceEvent(object: AntPlusBikeSpeedDistancePcc.CalculatedAccumulatedDistanceReceiver(BigDecimal(wheelCircumference)) {
            override fun onNewCalculatedAccumulatedDistance(estTimestamp: Long, flags: EnumSet<EventFlag>?, distance: BigDecimal?) {
                if (distance != null) {
                    if (lastDistanceTimeStamp + 1000 < estTimestamp ) {
                        onDistance(distance.divide(mToKm, 3, BigDecimal.ROUND_HALF_UP).toFloat())
                        lastDistanceTimeStamp = estTimestamp
                    }
                }
            }
        })

        if (bikeController.isSpeedAndCadenceCombinedSensor) {

        }
    }

    private fun destroy() {
        bsdReleaseHandle?.close();
    }

    private var onSpeed: (speed: Float)->Unit
    private var onDistance: (distance: Float)->Unit
    private var bsdReleaseHandle: PccReleaseHandle<AntPlusBikeSpeedDistancePcc>? = null
    private var deviceName = ""
    private var deviceNumber = 0
    private val wheelCircumference = 2.096
}