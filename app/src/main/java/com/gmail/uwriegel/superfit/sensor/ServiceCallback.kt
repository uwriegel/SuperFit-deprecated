package com.gmail.uwriegel.superfit.sensor

import android.content.Context

interface ServiceCallback {
    fun stopService()
    fun getContext(): Context
}