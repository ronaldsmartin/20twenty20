package com.itsronald.twenty2020

import android.app.Application

import timber.log.Timber

/**
 * Created by Ronald Martin on 11/6/16.
 */
class Twenty2020Application : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.i("Timber logger planted.")
        }
    }
}
