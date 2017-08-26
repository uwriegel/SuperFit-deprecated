package com.gmail.uwriegel.superfit.AntPlusSensors

import android.content.Context
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle
import com.gmail.uwriegel.superfit.continueTimer
import com.gmail.uwriegel.superfit.initializeTimer
import com.gmail.uwriegel.superfit.pauseTimer
import java.math.BigDecimal
import java.util.*


/**
 * Created by urieg on 05.08.2017.
 */
class BikeMonitor {
    constructor(context: Context, onSpeed: (speed: Float)->Unit, onDistance: (distance: Float)->Unit, onCadence: (cadence: Float)->Unit,
                onMaxSpeed: (maxSpeed: Float)->Unit, onTimeSpan: (timeSpan: Long, averageSpeed: Float)->Unit) {
        this.onSpeed = onSpeed
        this.onDistance = onDistance
        this.onCadence = onCadence
        this.onMaxSpeed = onMaxSpeed
        this.onTimeSpan = onTimeSpan

        initializeTimer {
            onTimeSpan(it, (if (it > 0) currentDistance / it else 0f) * 3600f)
        }

        searchBike(context, {
            deviceName = it.deviceDisplayName
            deviceNumber = it.antDeviceNumber
            subscribe(context)
        })
    }

    private fun subscribe(context: Context) {
        bsdReleaseHandle = AntPlusBikeSpeedDistancePcc.requestAccess(context, deviceNumber, 0, true, { bikeController, resultCode, _ ->
            when (resultCode) {
                RequestAccessResult.SUCCESS -> subScribeToBikeSpeed(context, bikeController)
            }
        }, {
        })
    }

    private fun subScribeToBikeSpeed(context: Context, bikeController: AntPlusBikeSpeedDistancePcc) {
        var lastTimeStamp = 0L
        var lastDistanceTimeStamp = 0L
        var speedIsNull = true

        bikeController.subscribeCalculatedSpeedEvent(object:
                AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver(BigDecimal(wheelCircumference)) {
            override fun onNewCalculatedSpeed(estTimestamp: Long, flags: EnumSet<EventFlag>?, calculatedSpeedInMs: BigDecimal?) {
                if (calculatedSpeedInMs != null) {
                    if (lastTimeStamp + 500 < estTimestamp ) {
                        val calculatedSpeed = calculatedSpeedInMs.toFloat() * 3.6f
                        onSpeed(calculatedSpeed)

                        if (calculatedSpeed > maxSpeed) {
                            maxSpeed = calculatedSpeed
                            onMaxSpeed(maxSpeed)
                        }

                        if (calculatedSpeed > 0.0f && speedIsNull) {
                            speedIsNull = false
                            continueTimer()
                        } else if (calculatedSpeed == 0.0f && !speedIsNull) {
                            speedIsNull = true
                            pauseTimer()
                        }

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
                        currentDistance = distance.divide(mToKm, 3, BigDecimal.ROUND_HALF_UP).toFloat()
                        onDistance(currentDistance)
                        lastDistanceTimeStamp = estTimestamp
                    }
                }
            }
        })

        if (bikeController.isSpeedAndCadenceCombinedSensor) {
            bcReleaseHandle = AntPlusBikeCadencePcc.requestAccess(context, bikeController.antDeviceNumber, 0, true,{ bikeController, resultCode, _ ->
                when (resultCode) {
                    RequestAccessResult.SUCCESS -> subScribeToBikeCadence(bikeController)
                }
            }, {
            })
        }
    }

    private fun subScribeToBikeCadence(bikeController: AntPlusBikeCadencePcc) {
        bikeController.subscribeCalculatedCadenceEvent { _, _, calculatedCadence ->
            onCadence((calculatedCadence.toFloat()))
        }
    }

    private fun destroy() {
        bsdReleaseHandle?.close();
        bcReleaseHandle?.close();
    }

    private val onSpeed: (speed: Float)->Unit
    private val onDistance: (distance: Float)->Unit
    private val onCadence: (cadence: Float)->Unit
    private val onMaxSpeed: (maxSpeed: Float)->Unit
    private val onTimeSpan: (timeSpan: Long, averageSpeed: Float)->Unit
    private val wheelCircumference = 2.096

    private var bsdReleaseHandle: PccReleaseHandle<AntPlusBikeSpeedDistancePcc>? = null
    private var bcReleaseHandle: PccReleaseHandle<AntPlusBikeCadencePcc>? = null
    private var deviceName = ""
    private var deviceNumber = 0
    private var maxSpeed = 0.0f
    private var currentDistance = 0f
}