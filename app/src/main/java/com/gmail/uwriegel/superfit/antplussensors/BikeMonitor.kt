package com.gmail.uwriegel.superfit.antplussensors

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
 *
 * Monitors several Bike-Sensors and subscribable properties
 */
class BikeMonitor(context: Context,
                  private val onSpeed: (speed: Float) -> Unit,
                  private val onDistance: (distance: Float) -> Unit,
                  private val onCadence: (cadence: Int) -> Unit,
                  private val onMaxSpeed: (maxSpeed: Float) -> Unit,
                  private val onTimeSpan: (timeSpan: Long, averageSpeed: Float) -> Unit) {

    private fun subscribe(context: Context) {
        bsdReleaseHandle = AntPlusBikeSpeedDistancePcc.requestAccess(context, deviceNumber, 0, true, { bikeController, resultCode, _ ->
            // TODO: pattern matching
//            when (resultCode) {
//                RequestAccessResult.SUCCESS -> subScribeToBikeSpeed(context, bikeController)
//            }
            if (resultCode == RequestAccessResult.SUCCESS)
                subScribeToBikeSpeed(context, bikeController)
        }, {
        })
    }

    private fun subScribeToBikeSpeed(context: Context, bikeController: AntPlusBikeSpeedDistancePcc) {
        var speedIsNull = true

        bikeController.subscribeCalculatedSpeedEvent(object:
                AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver(BigDecimal(wheelCircumference)) {
            override fun onNewCalculatedSpeed(estTimestamp: Long, flags: EnumSet<EventFlag>?, calculatedSpeedInMs: BigDecimal?) {
                if (calculatedSpeedInMs != null) {
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
                }
            }
        })

        val mToKm = BigDecimal(1000)
        bikeController.subscribeCalculatedAccumulatedDistanceEvent(object: AntPlusBikeSpeedDistancePcc.CalculatedAccumulatedDistanceReceiver(BigDecimal(wheelCircumference)) {
            override fun onNewCalculatedAccumulatedDistance(estTimestamp: Long, flags: EnumSet<EventFlag>?, distance: BigDecimal?) {
                if (distance != null) {
                    currentDistance = distance.divide(mToKm, 3, BigDecimal.ROUND_HALF_UP).toFloat()
                    onDistance(currentDistance)
                }
            }
        })

        if (bikeController.isSpeedAndCadenceCombinedSensor) {
            bcReleaseHandle = AntPlusBikeCadencePcc.requestAccess(context, bikeController.antDeviceNumber, 0, true,{ bikeCadenceController, resultCode, _ ->
                if (resultCode == RequestAccessResult.SUCCESS)
                    subScribeToBikeCadence(bikeCadenceController)
            }, {
            })
        }
    }

    private fun subScribeToBikeCadence(bikeController: AntPlusBikeCadencePcc) {
        bikeController.subscribeCalculatedCadenceEvent { _, _, calculatedCadence ->
            onCadence((calculatedCadence.toInt()))
        }
    }

    // TODO:
    private fun destroy() {
        bsdReleaseHandle?.close()
        bcReleaseHandle?.close()
    }

    private val wheelCircumference = 2.096

    private var bsdReleaseHandle: PccReleaseHandle<AntPlusBikeSpeedDistancePcc>? = null
    private var bcReleaseHandle: PccReleaseHandle<AntPlusBikeCadencePcc>? = null
    private var deviceName = ""
    private var deviceNumber = 0
    private var maxSpeed = 0.0f
    private var currentDistance = 0f

    init {
        initializeTimer {
            onTimeSpan(it, (if (it > 0) currentDistance / it else 0f) * 3600f)
        }
        searchBike(context, {
            deviceName = it.deviceDisplayName
            deviceNumber = it.antDeviceNumber
            subscribe(context)
        })
    }
}