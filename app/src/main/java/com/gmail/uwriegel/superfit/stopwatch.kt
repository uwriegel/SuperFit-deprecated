package com.gmail.uwriegel.superfit

import java.util.*

fun initializeTimer(tick: (timeSpan: Long)->Unit)
{
    com.gmail.uwriegel.superfit.tick = tick
    previousTimeSpan = 0;
}

fun pauseTimer()
{
    val now = System.currentTimeMillis()
    timer.cancel()
    previousTimeSpan += now - startTime
}

fun continueTimer()
{
    start()
}

private fun start()
{
    startTime = System.currentTimeMillis()
    timer = Timer()
    timer.schedule(object: TimerTask() {
        override fun run() {
            val now = System.currentTimeMillis()
            tick((now - startTime + previousTimeSpan) / 1000)
        }
    }, 500, 500)
}

private var timer: Timer = Timer()
private var startTime: Long = 0
private var previousTimeSpan: Long = 0
private var tick: (timeSpan: Long)->Unit = {_->}