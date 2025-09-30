package com.crymzee.spenomatic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

import androidx.lifecycle.LifecycleObserver
import dagger.hilt.android.HiltAndroidApp
import kotlin.apply
import kotlin.jvm.java

@HiltAndroidApp
class SpenoMaticApp : Application(), LifecycleObserver {

    private val TAG = "AppController"

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }


    companion object {
        @get:Synchronized
        var instance: SpenoMaticApp? = null
            private set
    }


}